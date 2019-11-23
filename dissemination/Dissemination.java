package dissemination;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
import dht.Node;
import dht.notification.RouteDelivery;
import dht.notification.RouteNotify;
import dissemination.message.DisseminationMessage;
import dissemination.notification.MessageDelivery;
import dissemination.requests.RouteRequest;
import network.INetwork;
import publishsubscribe.requests.DisseminateRequest;

public class Dissemination extends GenericProtocol {

	public final static short PROTOCOL_ID= 900;

	public final static int SUBSCRIBE = 1;
	public final static int UNSUBSCRIBE = 2;
	public final static int PUBLISH = 3;

	private Map<String,Topic> topics;
	Node nodeID;


	@SuppressWarnings("deprecation")
	public Dissemination(INetwork net) throws HandlerRegistrationException{

		super("Dissemination",PROTOCOL_ID,net);

		//Notification
		registerNotificationHandler(RouteDelivery.NOTIFICATION_ID, uponRouteDelivery);
		registerNotificationHandler(RouteNotify.NOTIFICATION_ID, uponRouteNotify);

		registerRequestHandler(DisseminateRequest.REQUEST_ID, uponDisseminateRequest);
		registerMessageHandler(DisseminationMessage.MSG_CODE, uponDisseminationMessage, DisseminationMessage.serializer);

	}
	@Override
	public void init(Properties properties) {
		this.topics = new HashMap<String, Topic>();
		this.nodeID = new Node(myself.hashCode(), myself);
	}

	private ProtocolRequestHandler uponDisseminateRequest = new ProtocolRequestHandler() {

		@Override
		public void uponRequest(ProtocolRequest r) {
			//Create Message
			DisseminateRequest req = (DisseminateRequest) r;

			Message msg = req.getMessage();
			msg.setNodeInterested(nodeID);
			msg.setSender(nodeID);

			switch(msg.getTypeM()) {
				case SUBSCRIBE:
					subscribe(req.getTopic(), msg, nodeID);
					break;
				case UNSUBSCRIBE:
					unsubscribe(req.getTopic(), msg, nodeID);
					break;
				case PUBLISH:
					publish(req.getTopic(), msg, nodeID, nodeID);
			}
		}
	};

	private void subscribe(byte[] topic, Message msg, Node nodeInterested) {
		String topicS = new String(topic, StandardCharsets.UTF_8);
		Topic thisTopic = null;
		
		if(topics.containsKey(topicS)){
			thisTopic = topics.get(topicS);
	
			if(!thisTopic.nodeExists(nodeInterested)) {
				thisTopic.addNode(nodeInterested);
			}
		}
		else {

			TreeSet<Node> nodes = new TreeSet<Node>();
			nodes.add(nodeInterested);
			Topic t = new Topic(null, nodes);
			topics.put(topicS, t);
			routeMessage(topic, msg);
		}

	}



	private void unsubscribe(byte[] topic, Message msg, Node nodeInterested) {
		String topicS = new String(topic, StandardCharsets.UTF_8);
		Topic thisTopic;
		
		if(topics.containsKey(topicS)){
			thisTopic = topics.get(topicS);
			int size = thisTopic.removeNode(nodeInterested);
			if(size == 0) {
				//sendUnsub to upstream
				topics.remove(topicS);
				DisseminationMessage m = new DisseminationMessage(topic, msg);
				sendMessage(m, thisTopic.getUpStream().getMyself());
			}
		}
		else {
			routeMessage(topic, msg);
		}

	}

	private void publish(byte[] topic, Message msg, Node nodeInterested) {
		String topicS = new String(topic, StandardCharsets.UTF_8);

		if(topics.containsKey(topicS)) {
			Topic thisTopic = topics.get(topicS);

			for(Node n: thisTopic.getNodes()) {
				if(n == nodeID) {
					MessageDelivery notification = new MessageDelivery(topic, msg);
					triggerNotification(notification);
				}
				else if(n != msg.getNodeSender()){
					msg.setSender(nodeID);
					DisseminationMessage msgOut = new DisseminationMessage(topic, msg);
					sendMessage(msgOut, n.getMyself());
				}
			}
			
			Node upStream =  thisTopic.getUpStream();
			if(upStream!= null && msg.getNodeSender() != upStream) {
				msg.setSender(nodeID);
				DisseminationMessage msgOut = new DisseminationMessage(topic, msg);
				
				sendMessage(msgOut, upStream.getMyself());
			}
			
		}
		else {
			routeMessage(topic, msg);
		}
	}

	private void routeMessage(byte[] topic, Message msg) {

		String topicS = new String(topic, StandardCharsets.UTF_8);

		RouteRequest r = new RouteRequest(topicS.hashCode(), msg);
		r.setDestination(DHT.PROTOCOL_ID);
		try {
			sendRequest(r);
		} catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
			destinationProtocolDoesNotExist.printStackTrace();
			System.exit(1);
		}
	}

//	private ProtocolNotificationHandler uponRouteDelivery = new ProtocolNotificationHandler() {
//		//TODO:
//		@Override
//		public void uponNotification(ProtocolNotification not) {
//			RouteDelivery req = (RouteDelivery) not;
//			Message m = req.getM();
//
//			String topicS = new String(m.getTopic(), StandardCharsets.UTF_8);
//
//			switch(m.getTypeM()) {
//				case SUBSCRIBE:
//					if(topics.containsKey(topicS)) {
//						Topic thisTop = topics.get(topicS);
//						thisTop.addNode(nodeID);
//					}
//					else {
//						TreeSet<Node> nodes = new TreeSet<Node>();
//						nodes.add(nodeID);
//						Topic t = new Topic(null,nodes);
//						topics.put(topicS, t);
//					}
//					break;
//				case UNSUBSCRIBE:
//					if(topics.containsKey(topicS)) {
//						Topic t = topics.get(topicS);
//						t.removeNode(nodeID);
//					}
//					else {
//						Topic t = new Topic(null, new TreeSet<Node>());
//						topics.put(topicS, t);
//					}
//					break;
//				case PUBLISH:
//					if(topics.containsKey(topicS)) {
//						Topic thisTopic = topics.get(topicS);
//
//						boolean sendM = false;
//						for(Node n: thisTopic.getNodes()) {
//							if(n == nodeID) {
//								sendM = true;
//							}
//							else {
//								DisseminationMessage msgOut = new DisseminationMessage(m.topic, m);
//								sendMessage(msgOut, n.getMyself());
//							}
//						}
//						DisseminationMessage msgOut = new DisseminationMessage(m.topic, m);
//						sendMessage(msgOut, thisTopic.getResponsible().getMyself());
//
//						if(sendM) {
//							MessageDelivery notification = new MessageDelivery(m.topic, m);
//							triggerNotification(notification);
//						}
//					}
//					break;
//
//			}
//		}
//	};

	private final ProtocolMessageHandler uponDisseminationMessage = new ProtocolMessageHandler() {
		@Override
		public void receive(ProtocolMessage protocolMessage) {
			DisseminationMessage req = (DisseminationMessage) protocolMessage;
			Message m = req.getPayload();

			switch(m.getTypeM()) {
				case UNSUBSCRIBE:
					unsubscribe(m.topic, m, m.getNodeInterested());
					break;
				case PUBLISH:
					publish(m.topic, m, m.getNodeInterested(), req.getFrom());
					break;
			}


		}
	};

	private ProtocolNotificationHandler uponRouteNotify = new ProtocolNotificationHandler() {
		//TODO:
		@Override
		public void uponNotification(ProtocolNotification not) {
			RouteNotify req = (RouteNotify) not;
			Message m = req.getMsg();
			String topicS = new String(m.getTopic(), StandardCharsets.UTF_8);
			int isUpStream = req.getIsUpstream();
			if(isUpStream == 1) {
				Topic t = topics.get(topicS);
				t.setUpStream(req.node);
			}
			switch(m.getTypeM()) {
				case SUBSCRIBE:
					if(isUpStream == 0)
						subscribe(m.topic, m);
					break;
				case UNSUBSCRIBE:
					if(isUpStream == 0)
						unsubscribe(m.topic, m);
					break;
				case PUBLISH:
					if(isUpStream == 0)
						publish(m.topic, m);
					break;
			}
		}
	};

}