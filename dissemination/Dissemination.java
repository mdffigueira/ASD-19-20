package dissemination;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import babel.exceptions.DestinationProtocolDoesNotExist;
import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolNotificationHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.notification.ProtocolNotification;
import babel.protocol.GenericProtocol;
import babel.requestreply.ProtocolRequest;
import dht.DHT;
import dht.Node;
import dht.notification.RouteDelivery;
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

		registerRequestHandler(DisseminateRequest.REQUEST_ID, uponDisseminateRequest);


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

			switch(msg.getTypeM()) {
			case SUBSCRIBE:
				subscribe(req.getTopic(), msg);
				break;
			case UNSUBSCRIBE:
				unsubscribe(req.getTopic(), msg);
				break;
			default:
				publish(req.getTopic(), msg);
			}
		}
	};

	private void subscribe(byte[] topic, Message msg) {
		String topicS = new String(topic, StandardCharsets.UTF_8);
		Topic thisTopic = null;

		if(topics.containsKey(topicS)){
			thisTopic = topics.get(topicS);
			if(!thisTopic.nodeExists(nodeID)) {
				thisTopic.addNode(nodeID);
			}
		}
		else {
			routeMessage(topic, msg);
		}
		
	}



	private void unsubscribe(byte[] topic, Message msg) {
		String topicS = new String(topic, StandardCharsets.UTF_8);
		Topic thisTopic;

		if(topics.containsKey(topicS)){
			thisTopic = topics.get(topicS);
			int size = thisTopic.removeNode(nodeID);
			if(size == 0) {
				//sendUnsub to next
			}
		}

	}

	private void publish(byte[] topic, Message msg) {
		boolean sendM = false;

		String topicS = new String(topic, StandardCharsets.UTF_8);

		if(topics.containsKey(topicS)) {
			TreeSet<Node> nodes = topics.get(topicS);
			
			for(Node n: nodes) {
				if(n == nodeID) {
					sendM = true;
				}
				else {
					DisseminationMessage msgOut = new DisseminationMessage(topic, msg.getMessage(), nodes);
					sendMessage(msgOut, n.getMyself());
				}
			}

			if(sendM) {
				MessageDelivery notification = new MessageDelivery(topic, msg);
				triggerNotification(notification);
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

	private ProtocolNotificationHandler uponRouteDelivery = new ProtocolNotificationHandler() {

		@Override
		public void uponNotification(ProtocolNotification not) {
			RouteDelivery req = (RouteDelivery) not;

			int typeM = req.getTypeM();
			
			switch(typeM) {
			case SUBSCRIBE:
				subscribe();
				break;
			case UNSUBSCRIBE:
				unsubscribte();
				break;
			case PUBLISH:
				publish();
				break;
			
			}
		}
	};	



}
