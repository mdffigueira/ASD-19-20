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
import dissemination.Message;
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

public class PublishSubscribe extends GenericProtocol {

    public final static short PROTOCOL_ID = 500;
    public final static String PROTOCOL_NAME = "Publish Subscribe";

    public final static int SUBSCRIBE = 1;
    public final static int UNSUBSCRIBE = 2;
    public final static int PUBLISH = 3;
    private boolean isGossip;

    //Set of Topics
    private Set<String> topics;

    public PublishSubscribe(INetwork net) throws HandlerRegistrationException {
        super("Publish Subscribe", PROTOCOL_ID, net);

        //Register Events

        //Requests
        registerRequestHandler(PSSubscribeRequest.REQUEST_ID, uponSubscribeRequest);
        registerRequestHandler(PSUnsubscribeRequest.REQUEST_ID, uponUnsubscribeRequest);
        registerRequestHandler(PSPublishRequest.REQUEST_ID, uponPublishRequest);

        //Notifications Produced
        registerNotification(PSDeliver.NOTIFICATION_ID, PSDeliver.NOTIFICATION_NAME);

        registerNotificationHandler(FloodBCastDeliver.NOTIFICATION_ID, uponFloodBCastDeliver);
    }

    @Override
    public void init(Properties props) {

        //Initialize State
        this.topics = new TreeSet<>();
        this.isGossip = false;
    }


    private ProtocolRequestHandler uponSubscribeRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest r) {

            PSSubscribeRequest req = (PSSubscribeRequest) r;

            //GOSSIP
            if (isGossip) {
                String topic = new String(req.getTopic(), StandardCharsets.UTF_8);
                topics.add(topic);
            }


            //DISSEMINATION
            else {
                disseminateRequest(req.getTopic(), null, SUBSCRIBE);
            }
        }
    };

    private ProtocolRequestHandler uponUnsubscribeRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest r) {

            PSUnsubscribeRequest req = (PSUnsubscribeRequest) r;

            //GOSSIP
            if (isGossip) {
                String topic = new String(req.getTopic(), StandardCharsets.UTF_8);
                topics.remove(topic);
            } else {
                disseminateRequest(req.getTopic(), null, UNSUBSCRIBE);
            }
        }
    };


    private ProtocolRequestHandler uponPublishRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest r) {


            PSPublishRequest req = (PSPublishRequest) r;

            //GOSSIP
            if (isGossip) {
                //Create Message
                FloodBCastRequest floodReq = new FloodBCastRequest(req.getMessage(), req.getTopic());
                //falta adicionar a lsita de topicos? visto que vai receber o seu....

                floodReq.setDestination(FloodBCast.PROTOCOL_ID);
                try {
                    sendRequest(floodReq);
                } catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
                    destinationProtocolDoesNotExist.printStackTrace();
                    System.exit(1);
                }
            } else {
                disseminateRequest(req.getTopic(), req.getMessage(), PUBLISH);
            }
        }
    };

    private void disseminateRequest(byte[] top, byte[] m, int typeM) {
        String topic = new String(top, StandardCharsets.UTF_8);
    	Message message = new Message(m, typeM,top);
        DisseminateRequest dissReq = new DisseminateRequest(top, message);
        dissReq.setDestination(Dissemination.PROTOCOL_ID);

        try {
            sendRequest(dissReq);
        } catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
            destinationProtocolDoesNotExist.printStackTrace();
            System.exit(1);
        }
    }

    private ProtocolNotificationHandler uponFloodBCastDeliver = new ProtocolNotificationHandler() {

        @Override
        public void uponNotification(ProtocolNotification not) {
            FloodBCastDeliver req = (FloodBCastDeliver) not;
            String topicNotif = new String(req.getTopic(), StandardCharsets.UTF_8);
            if (topics.contains(topicNotif)) {
                PSDeliver deliver = new PSDeliver(req.getTopic(), req.getMessage());
                triggerNotification(deliver);
            }
        }
    };
}
