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
import dissemination.requests.RouteRequest;
import network.INetwork;
import publishsubscribe.requests.DisseminateRequest;

public class Dissemination extends GenericProtocol {

	public final static short PROTOCOL_ID= 900;

    public final static int SUBSCRIBE = 1;
    public final static int UNSUBSCRIBE = 2;
    public final static int PUBLISH = 3;

	private Map<String,TreeSet<Node>> topics;
	Node nodeID;
	

	public Dissemination(INetwork net) throws HandlerRegistrationException{

		super("Dissemination",PROTOCOL_ID,net);
		//Notification
		registerNotificationHandler(RouteDelivery.NOTIFICATION_ID, uponRouteDelivery);

		registerRequestHandler(DisseminateRequest.REQUEST_ID, uponDisseminateRequest);
		
		
	}
	@Override
	public void init(Properties properties) {
		this.topics = new HashMap<String, TreeSet<Node>>();
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
		TreeSet<Node> nodes = null;
		
		if(topics.containsKey(topicS)){
			nodes = topics.get(topicS);
			if(!nodes.contains(nodeID)) {
				nodes.add(nodeID);
			}
		}
		else {
			nodes = new TreeSet<Node>();
			nodes.add(nodeID);
		}
		
		topics.put(topicS, nodes);
		
		routeMessage(topic, msg);
	}
	
	

	private void unsubscribe(byte[] topic, Message msg) {
		String topicS = new String(topic, StandardCharsets.UTF_8);
		TreeSet<Node> nodes = null;
		
		if(topics.containsKey(topicS)){
			nodes = topics.get(topicS);
			nodes.remove(nodeID);
		}
		topics.put(topicS, nodes);
		if (nodes.size() > 0) {
			routeMessage(topic, msg);
		}
		
	}
	
	private void publish(byte[] topic, Message msg) {
		
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
			
			Node node = req.getNode();
			if(node != nodeID) {
				
			}	
		}
	};	



}
