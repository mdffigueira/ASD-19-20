package publishsubscribe;

import java.nio.charset.StandardCharsets;
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
import dissemination.Message;
import dissemination.notification.MessageDelivery;
import dissemination.requests.RouteRequest;
import floodbcast.FloodBCast;
import dissemination.Dissemination;
import floodbcast.delivers.FloodBCastDeliver;
import floodbcast.requests.FloodBCastRequest;
import publishsubscribe.delivers.PSDeliver;
import publishsubscribe.requests.DisseminateRequest;
import publishsubscribe.requests.PSPublishRequest;
import publishsubscribe.requests.PSSubscribeRequest;
import publishsubscribe.requests.PSUnsubscribeRequest;
import network.INetwork;

public class PublishSubscribe2 extends GenericProtocol {

    public final static short PROTOCOL_ID = 500;
    public final static String PROTOCOL_NAME = "Publish Subscribe";

    public final static int SUBSCRIBE = 1;
    public final static int UNSUBSCRIBE = 2;
    public final static int PUBLISH = 3;
    public final static int POPULARITY = 4;

    //Set of Topics
    private Set<byte[]> topics;

    @SuppressWarnings("deprecation")
	public PublishSubscribe2(INetwork net) throws HandlerRegistrationException {
        super("Publish Subscribe", PROTOCOL_ID, net);

        //Register Events


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

    private ProtocolRequestHandler uponSubscribeRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest r) {

            PSSubscribeRequest req = (PSSubscribeRequest) r;
            checkIfPopular(req.getTopic(), req.getMessage());
        }
    };
    
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

    private void checkIfPopular(byte[] topic, byte[] msg) {
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

            switch(m.getTypeM()) {
            case POPULARITY:
                int popular = req.isPopular();
                if(popular == 0) {
                	//TODO:
                	//PSMessage(usaFlood, messageId)
                   // disseminateRequest(m.getTopic(), m.getMessage(), m.getTypeM());
                }
                else {
                    //sendToFlood(m);
                }
                break;
            }
        }
    };

    private void sendToFlood(Message msg) {

        byte[] topic = msg.getTopic();
        
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




    private ProtocolRequestHandler uponUnsubscribeRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest r) {

            PSUnsubscribeRequest req = (PSUnsubscribeRequest) r;
            checkIfPopular(req.getTopic(), null);
        }
    };


    private ProtocolRequestHandler uponPublishRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest r) {


            PSPublishRequest req = (PSPublishRequest) r;
            checkIfPopular(req.getTopic(), req.getMessage());
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
