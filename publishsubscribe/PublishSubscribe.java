package publishsubscribe;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import babel.exceptions.DestinationProtocolDoesNotExist;
import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolNotificationHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.notification.ProtocolNotification;
import babel.protocol.GenericProtocol;
import babel.requestreply.ProtocolRequest;
import dht.DHT;
import dht.notification.RouteDelivery;
import dissemination.Dissemination;
import dissemination.Message;
import dissemination.notification.MessageDelivery;
import dissemination.requests.RouteRequest;
import floodbcast.FloodBCast;
import floodbcast.delivers.FloodBCastDeliver;
import floodbcast.requests.FloodBCastRequest;
import network.INetwork;
import publishsubscribe.delivers.PSDeliver;
import publishsubscribe.requests.DisseminateRequest;
import publishsubscribe.requests.PSPublishRequest;
import publishsubscribe.requests.PSSubscribeRequest;
import publishsubscribe.requests.PSUnsubscribeRequest;

public class PublishSubscribe extends GenericProtocol {

	public final static short PROTOCOL_ID = 500;
	public final static String PROTOCOL_NAME = "Publish Subscribe";

	public final static int SUBSCRIBE = 1;
	public final static int UNSUBSCRIBE = 2;
	public final static int PUBLISH = 3;
	public final static int POPULARITY = 4;

	//Set of Topics
	private Set<byte[]> topics;

	@SuppressWarnings("deprecation")
	public PublishSubscribe(INetwork net) throws HandlerRegistrationException {
		super("Publish Subscribe", PROTOCOL_ID, net);

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

	private ProtocolNotificationHandler uponRouteDelivery = new ProtocolNotificationHandler() {
		@Override
		public void uponNotification(ProtocolNotification not) {
			RouteDelivery req = (RouteDelivery) not;
			if(PROTOCOL_ID == req.getProtocolId()) {
				Message msg = req.getMsg();
				int popular = req.isPopular();
				if(popular == 0) {
					disseminateRequest(msg.getTopic(), msg.getMessage(), msg.getTypeM());
				}
				else {
					sendToFlood(msg);
				}
			}
		}
	};

	private void sendToFlood(Message msg) {
		//Create Message
		FloodBCastRequest floodReq = new FloodBCastRequest(msg.getMessage(), msg.getTopic());
		floodReq.setDestination(FloodBCast.PROTOCOL_ID);
		try {
			sendRequest(floodReq);
		} catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
			destinationProtocolDoesNotExist.printStackTrace();
			System.exit(1);
		}
	}


	private ProtocolRequestHandler uponSubscribeRequest = new ProtocolRequestHandler() {
		@Override
		public void uponRequest(ProtocolRequest r) {
			PSSubscribeRequest req = (PSSubscribeRequest) r;
			byte[] topic = req.getTopic();
			topics.add(topic);
			disseminateRequest(req.getTopic(), null, SUBSCRIBE);
		}
	};

	private ProtocolRequestHandler uponUnsubscribeRequest = new ProtocolRequestHandler() {
		@Override
		public void uponRequest(ProtocolRequest r) {
			PSUnsubscribeRequest req = (PSUnsubscribeRequest) r;
			byte[] topic = req.getTopic();
			topics.remove(topic);
			disseminateRequest(req.getTopic(), null, UNSUBSCRIBE);
		}
	};


	private ProtocolRequestHandler uponPublishRequest = new ProtocolRequestHandler() {
		@Override
		public void uponRequest(ProtocolRequest r) {
			PSPublishRequest req = (PSPublishRequest) r;
			byte[] topic = req.getTopic();
			Message message = new Message(topic, req.getMessage() , POPULARITY);
			RouteRequest routeReq = new RouteRequest(topic.hashCode(), message, 0);
			routeReq.setDestination(DHT.PROTOCOL_ID);
			try {
				sendRequest(routeReq);
			} catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
				destinationProtocolDoesNotExist.printStackTrace();
				System.exit(1);
			}
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
