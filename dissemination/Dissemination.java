package dissemination;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolRequestHandler;
import babel.protocol.GenericProtocol;
import babel.requestreply.ProtocolRequest;
import dht.DHT;
import dht.Node;
import dissemination.requests.RouteRequest;
import publishsubscribe.requests.DisseminateRequest;
import network.INetwork;

public class Dissemination extends GenericProtocol {

	public final static short PROTOCOL_ID= 900;

	public final static int SUBSCRIBE = 1;
	public final static int PUBLISH = 2;
	public final static int UNSUBSCRIBE = 3;

	private Map<String,TreeSet<Node>> topics;
	Node nodeID;

	public Dissemination(INetwork net) throws HandlerRegistrationException{

		super("Dissemination",PROTOCOL_ID,net);
		//Notification
		//      registerNotification(RouteDelivery.NOTIFICATION_ID,RouteDelivery.NOTIFICATION_NAME );

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

			int typeMessage = req.getTypeM();

			switch(typeMessage) {
			case SUBSCRIBE:
				subscribe(req);
				break;
			case UNSUBSCRIBE:
				unsubscribe(req);
				break;
			case PUBLISH:
				publish(req);
				break;
			default: ;

			}
		}
	};

	public void subscribe(DisseminateRequest req) {
		String topic = new String(req.getTopic(), StandardCharsets.UTF_8);
		TreeSet<Node> nodes = null;

		if(topics.containsKey(topic)){
			nodes = topics.get(topic);
			if(!nodes.contains(nodeID)) {
				nodes.add(nodeID);
			}
		}
		else {
			nodes = new TreeSet<Node>();
			nodes.add(nodeID);
		}

		topics.put(topic, nodes);

		RouteRequest r = new RouteRequest(req.getMessage(), req.getTopic());
		r.setDestination(DHT.PROTOCOL_ID);
	}

	public void unsubscribe(DisseminateRequest req) {
		String topic = new String(req.getTopic(), StandardCharsets.UTF_8);
		TreeSet<Node> nodes = null;

		if(topics.containsKey(topic)){
			nodes = topics.get(topic);
			nodes.remove(nodeID);
		}
		topics.put(topic, nodes);
		if (nodes.size() > 0) {

		}

	}

	public void publish(DisseminateRequest req) {

	}

}
