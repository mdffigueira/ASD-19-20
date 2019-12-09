package dissemination;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import babel.exceptions.DestinationProtocolDoesNotExist;
import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolMessageHandler;
import babel.handlers.ProtocolNotificationHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.handlers.ProtocolTimerHandler;
import babel.notification.ProtocolNotification;
import babel.protocol.GenericProtocol;
import babel.protocol.event.ProtocolMessage;
import babel.requestreply.ProtocolRequest;
import babel.timer.ProtocolTimer;
import dht.DHT;
import utils.Node;
import dht.notification.RouteDelivery;
import dht.notification.RouteNotify;
import dissemination.message.DisseminationMessage;
import dissemination.notification.MessageDelivery;
import dissemination.notification.UpdatePopularity;
import dissemination.requests.RouteRequest;
import dissemination.timers.SubscribeAgainTimer;
import dissemination.timers.SubscriptionTimeLimit;
import network.INetwork;
import publishsubscribe.requests.DisseminateRequest;
import utils.Message;
import utils.Topic;

public class Dissemination extends GenericProtocol {

	public final static short PROTOCOL_ID = 900;

	private final static int SUBSCRIBE = 1;
	private final static int UNSUBSCRIBE = 2;
	private final static int PUBLISH = 3;

	private Map<Integer, Topic> topics;
	private Node nodeID;


	@SuppressWarnings("deprecation")
	public Dissemination(INetwork net) throws HandlerRegistrationException {

		super("Dissemination", PROTOCOL_ID, net);

		//Notification
		registerNotificationHandler(RouteDelivery.NOTIFICATION_ID, uponRouteDelivery);
		registerNotificationHandler(RouteNotify.NOTIFICATION_ID, uponRouteNotify);

		//Requests
		registerRequestHandler(DisseminateRequest.REQUEST_ID, uponDisseminateRequest);

		//Notifications Produced
		registerNotification(MessageDelivery.NOTIFICATION_ID, MessageDelivery.NOTIFICATION_NAME);
		registerNotification(UpdatePopularity.NOTIFICATION_ID, UpdatePopularity.NOTIFICATION_NAME);

		//Messages
		registerMessageHandler(DisseminationMessage.MSG_CODE, uponDisseminationMessage, DisseminationMessage.serializer);

		//Timers
		registerTimerHandler(SubscribeAgainTimer.TimerCode, uponSubscribeAgainTimer);
		registerTimerHandler(SubscriptionTimeLimit.TimerCode, uponSubscriptionTimeLimit);
	}

	@Override
	public void init(Properties properties) {
		this.topics = new HashMap<>();
		this.nodeID = new Node(myself.hashCode(), myself);
	}

	private ProtocolRequestHandler uponDisseminateRequest = new ProtocolRequestHandler() {

		@Override
		public void uponRequest(ProtocolRequest r) {
			//Create Message
			DisseminateRequest m = (DisseminateRequest) r;

			Message msg = m.getMessage();
			msg.setNodeInterested(nodeID);
			msg.setSender(nodeID);
			int msgId = msg.getTopic().hashCode();

			switch (msg.getTypeM()) {
				case SUBSCRIBE:
					subscribe(msgId, msg, null, false);
					setupPeriodicTimer(new SubscribeAgainTimer(msgId, msg), 30000, 30000);
					break;
				case UNSUBSCRIBE:
					unsubscribe(msgId, msg);
					break;
				case PUBLISH:
					publish(msgId, msg, false);
			}
		}
	};

	private void subscribe(int msgId, Message msg, Node upStream, boolean isResponsible) {
		int hasUpstream = upStream != null ? 1 : 0;
		Topic topic = topics.get(msgId);
		UUID timer;
		if (topic != null) {
			topic.addNode(msg.getNodeInterested());
			if (topic.getTimer(msg.getNodeInterested()) != null)
				cancelTimer(topic.getTimer(msg.getNodeInterested()));
			timer = setupTimer(new SubscriptionTimeLimit(msgId, msg.getNodeInterested()), 90000);
			topic.addTimer(msg.getNodeInterested(), timer);
			if (!topic.nodeExists(nodeID) && !isResponsible)
				routeRequest(msgId, msg, hasUpstream);
			else {
				UpdatePopularity not = new UpdatePopularity(msgId, msg.getNodeInterested(), SUBSCRIBE, DHT.PROTOCOL_ID);
				triggerNotification(not);
			}

		} else {
			if (isResponsible) {
				topic = topics.put(msgId, new Topic(upStream));
				topic.addNode(msg.getNodeInterested());
				timer = setupTimer(new SubscriptionTimeLimit(msgId, msg.getNodeInterested()), 90000);
				topic.addTimer(msg.getNodeInterested(), timer);
			} else {
				if (hasUpstream == 1) {
					topic = topics.put(msgId, new Topic(upStream));
					topic.addNode(msg.getNodeInterested());
					timer = setupTimer(new SubscriptionTimeLimit(msgId, msg.getNodeInterested()), 90000);
					topic.addTimer(msg.getNodeInterested(), timer);
				}
				routeRequest(msgId, msg, hasUpstream);
			}
		}
	}


	private void unsubscribe(int msgId, Message msg) {

		if (topics.containsKey(msgId)) {
			Node upStream = topics.get(msgId).getUpStream();
			Node nodeInterested = msg.getNodeInterested();
			int size = topics.get(msgId).removeNode(nodeInterested);
			if (size == 0) {
				//sendUnsub to upstream
				DisseminationMessage m = new DisseminationMessage(msgId, msg);
				sendMessage(m, upStream.getMyself());
				topics.remove(msgId);
			}
			UpdatePopularity not = new UpdatePopularity(msgId, msg.getNodeInterested(), UNSUBSCRIBE, DHT.PROTOCOL_ID);
			triggerNotification(not);
		}

	}

	private void publish(int msgId, Message msg, boolean isResponsible) {

		if (topics.containsKey(msgId)) {
			Topic thisTopic = topics.get(msgId);

			for (Node n : thisTopic.getNodes()) {
				if (n.getId() == nodeID.getId()) {
					MessageDelivery notification = new MessageDelivery(msgId, msg);
					triggerNotification(notification);
				} else if (n != msg.getNodeSender()) {
					msg.setSender(nodeID);
					DisseminationMessage msgOut = new DisseminationMessage(msgId, msg);
					sendMessage(msgOut, n.getMyself());
				}
			}

			Node upStream = thisTopic.getUpStream();
			if (!isResponsible && upStream != null && msg.getNodeSender() != upStream) {
				msg.setSender(nodeID);
				DisseminationMessage msgOut = new DisseminationMessage(msgId, msg);
				sendMessage(msgOut, upStream.getMyself());
			}

		} else {
			if (!isResponsible)
				routeRequest(msgId, msg, 0);
		}
	}

	private void routeRequest(int msgId, Message msg, int hasUpStream) {

		RouteRequest r = new RouteRequest(msgId, msg, hasUpStream);
		r.setDestination(DHT.PROTOCOL_ID);
		try {
			sendRequest(r);
		} catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
			destinationProtocolDoesNotExist.printStackTrace();
			System.exit(1);
		}
	}

	private ProtocolNotificationHandler uponRouteDelivery = new ProtocolNotificationHandler() {
		@Override
		public void uponNotification(ProtocolNotification notif) {
			RouteDelivery req = (RouteDelivery) notif;
			if(req.getProtocolId() == PROTOCOL_ID) {
				Message msg = req.getMsg();
				int msgId = req.getMsgId();
				switch (msg.getTypeM()) {
					case SUBSCRIBE:
						subscribe(msgId, msg, null, true);
						break;
					case PUBLISH:
						publish(msgId, msg, true);
						break;

				}
			}
		}
	};

	private final ProtocolMessageHandler uponDisseminationMessage = new ProtocolMessageHandler() {
		@Override
		public void receive(ProtocolMessage protocolMessage) {
			DisseminationMessage req = (DisseminationMessage) protocolMessage;
			Message msg = req.getMsg();
			int msgId = req.getMsgId();

			switch (msg.getTypeM()) {
				case UNSUBSCRIBE:
					unsubscribe(msgId, msg);
					break;
				case PUBLISH:
					publish(msgId, msg, false);
					break;
			}


		}
	};

	private ProtocolNotificationHandler uponRouteNotify = new ProtocolNotificationHandler() {
		//TODO:
		@Override
		public void uponNotification(ProtocolNotification not) {
			RouteNotify req = (RouteNotify) not;
			Message msg = req.getMsg();
			int msgID = req.getMsgID();
			Node upStream = req.getUpStream();
			switch (msg.getTypeM()) {
				case SUBSCRIBE:
					subscribe(msgID, msg, upStream, false);
					break;
				case PUBLISH:
					publish(msgID, msg, false);
					break;

			}
		}
	};
	private ProtocolTimerHandler uponSubscribeAgainTimer = new ProtocolTimerHandler() {
		@Override
		public void uponTimer(ProtocolTimer protocolTimer) {
			SubscribeAgainTimer timer = (SubscribeAgainTimer) protocolTimer;
			int msgId = timer.getMsgId();
			Message msg = timer.getMsg();
			subscribe(msgId, msg, null, false);
		}
	};
	private ProtocolTimerHandler uponSubscriptionTimeLimit = new ProtocolTimerHandler() {
		@Override
		public void uponTimer(ProtocolTimer protocolTimer) {
			SubscriptionTimeLimit timer = (SubscriptionTimeLimit) protocolTimer;
			int msgId = timer.getMsgId();
			Node nodeToRemove = timer.getNodeToRemove();
			topics.get(msgId).removeNode(nodeToRemove);
		}
	};
}