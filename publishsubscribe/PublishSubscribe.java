package publishsubscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import babel.exceptions.DestinationProtocolDoesNotExist;
import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolMessageHandler;
import babel.handlers.ProtocolNotificationHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.notification.ProtocolNotification;
import babel.protocol.GenericProtocol;
import babel.protocol.event.ProtocolMessage;
import babel.requestreply.ProtocolRequest;
import dht.DHT;
import dht.notification.RouteDelivery;
import dissemination.Message;
import dissemination.notification.MessageDelivery;
import dissemination.requests.RouteRequest;
import floodbcast.FloodBCast;
import dissemination.Dissemination;
import floodbcast.delivers.FloodBCastDeliver;
import floodbcast.requests.FloodBCastRequest;
import publishsubscribe.delivers.PSDeliver;
import publishsubscribe.messages.PSPopularityMessage;
import publishsubscribe.requests.DisseminateRequest;
import publishsubscribe.requests.PSPublishRequest;
import publishsubscribe.requests.PSSubscribeRequest;
import publishsubscribe.requests.PSUnsubscribeRequest;
import network.INetwork;

public class PublishSubscribe extends GenericProtocol {

	public final static short PROTOCOL_ID = 500;
	public final static String PROTOCOL_NAME = "Publish Subscribe";

	public final static int SUBSCRIBE = 1;
	public final static int UNSUBSCRIBE = 2;
	public final static int PUBLISH = 3;
	public final static int POPULARITY = 4;

	//Set of Topics
	private Set<byte[]> topics;
	private Map<Integer, Set<Message>> pending;

	@SuppressWarnings("deprecation")
	public PublishSubscribe(INetwork net) throws HandlerRegistrationException {
		super("Publish Subscribe", PROTOCOL_ID, net);

		//Messages
		registerMessageHandler(PSPopularityMessage.MSG_CODE, uponPSPopularityMessage, PSPopularityMessage.serializer);

		//Requests
		registerRequestHandler(PSSubscribeRequest.REQUEST_ID, uponSubscribeRequest);
		registerRequestHandler(PSUnsubscribeRequest.REQUEST_ID, uponUnsubscribeRequest);
		registerRequestHandler(PSPublishRequest.REQUEST_ID, uponPublishRequest);

		//Notifications Produced
		registerNotification(PSDeliver.NOTIFICATION_ID, PSDeliver.NOTIFICATION_NAME);
		registerNotificationHandler(MessageDelivery.NOTIFICATION_ID, uponMessageDelivery);
		registerNotificationHandler(RouteDelivery.NOTIFICATION_ID, uponRouteDelivery);
		registerNotificationHandler(FloodBCastDeliver.NOTIFICATION_ID, uponFloodBCastDeliver);
	}

	@Override
	public void init(Properties props) {

		//Initialize State
		this.topics = new TreeSet<>();
		this.pending = new HashMap<Integer, Set<Message>>();
	}

	private void disseminateRequest(byte[] top, byte[] m, int typeM) {
		Message message = new Message(top, m, typeM);
		DisseminateRequest dissReq = new DisseminateRequest(top, message);
		dissReq.setDestination(Dissemination.PROTOCOL_ID);

		try {
			sendRequest(dissReq);
		} catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
			destinationProtocolDoesNotExist.printStackTrace();
			System.exit(1);
		}
	}
	
	private final ProtocolMessageHandler uponPSPopularityMessage = new ProtocolMessageHandler() {
		@Override
		public void receive(ProtocolMessage protocolMessage) {
			PSPopularityMessage req = (PSPopularityMessage) protocolMessage;
			Message m = req.getMsg();
			
			switch(m.getTypeM()) {
			case POPULARITY:
				int popular = req.isPopular();
				if(popular == 0) {
					
					Set<Message> msgs = pending.remove(m.getTopic().hashCode());
					for(Message msg: msgs)
						disseminateRequest(msg.getTopic(), msg.getMessage(), msg.getTypeM());
				}
				else {
					sendToFlood(m);
				}
				break;
			}

		}
	};

	private void checkIfPopular(byte[] topic, byte[] msg, int typeOriginalMsg) {
		Message pendMsg = new Message(topic, msg, typeOriginalMsg);
		int top = topic.hashCode();
		if(pending.containsKey(top)) {
			pending.get(top).add(pendMsg);
		}
		else {
			TreeSet<Message> addMsg = new TreeSet<Message>();
			addMsg.add(pendMsg);
			pending.put(top, addMsg);
		}

		Message message = new Message(topic, msg , POPULARITY);
		RouteRequest r = new RouteRequest(topic.hashCode(), message, 0);
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
		public void uponNotification(ProtocolNotification not) {
			RouteDelivery req = (RouteDelivery) not;
			Message m = req.getMsg();
			PSPopularityMessage msg = new PSPopularityMessage(m.getTopic().hashCode(), m, req.isPopular());
			sendMessage(msg, m.getNodeInterested().getMyself());
		}
	};

	private void sendToFlood(Message m) {

		byte[] topic = m.getTopic();

		Set<Message> msgs = pending.remove(m.getTopic().hashCode());

		for(Message msg: msgs) {
			switch(msg.getTypeM()) {
			case SUBSCRIBE:
				topics.add(topic);
				break;
			case UNSUBSCRIBE:
				topics.remove(topic);
				break;
			case PUBLISH:
				//Create Message
				FloodBCastRequest floodReq = new FloodBCastRequest(msg.getMessage(), msg.getTopic());
				floodReq.setDestination(FloodBCast.PROTOCOL_ID);
				try {
					sendRequest(floodReq);
				} catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
					destinationProtocolDoesNotExist.printStackTrace();
					System.exit(1);
				}
				break;
			}
		}
	}


	private ProtocolRequestHandler uponSubscribeRequest = new ProtocolRequestHandler() {
		@Override
		public void uponRequest(ProtocolRequest r) {
			PSSubscribeRequest req = (PSSubscribeRequest) r;
			checkIfPopular(req.getTopic(), req.getMessage(), SUBSCRIBE);
		}
	};

	private ProtocolRequestHandler uponUnsubscribeRequest = new ProtocolRequestHandler() {
		@Override
		public void uponRequest(ProtocolRequest r) {
			PSUnsubscribeRequest req = (PSUnsubscribeRequest) r;
			checkIfPopular(req.getTopic(), null, UNSUBSCRIBE);
		}
	};


	private ProtocolRequestHandler uponPublishRequest = new ProtocolRequestHandler() {
		@Override
		public void uponRequest(ProtocolRequest r) {
			PSPublishRequest req = (PSPublishRequest) r;
			checkIfPopular(req.getTopic(), req.getMessage(), PUBLISH);
		}
	};

	private ProtocolNotificationHandler uponFloodBCastDeliver = new ProtocolNotificationHandler() {

		@Override
		public void uponNotification(ProtocolNotification not) {
			FloodBCastDeliver req = (FloodBCastDeliver) not;
			if (topics.contains(req.getTopic())) {
				PSDeliver deliver = new PSDeliver(req.getTopic(), req.getMessage());
				triggerNotification(deliver);
			}
		}
	};

	private ProtocolNotificationHandler uponMessageDelivery = new ProtocolNotificationHandler() {

		@Override
		public void uponNotification(ProtocolNotification not) {
			MessageDelivery req = (MessageDelivery) not;
			PSDeliver deliver = new PSDeliver(req.getMessage().getTopic(), req.getMessage().getMessage());
			triggerNotification(deliver);
		}
	};
}
