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

    public final static short PROTOCOL_ID = 900;

    private final static int SUBSCRIBE = 1;
    private final static int UNSUBSCRIBE = 2;
    private final static int PUBLISH = 3;
    private final static int POPULARITY = 4;

    private Map<Integer, Topic> topics;
    private Node nodeID;


    @SuppressWarnings("deprecation")
    public Dissemination(INetwork net) throws HandlerRegistrationException {

        super("Dissemination", PROTOCOL_ID, net);

        //Notification
        registerNotificationHandler(RouteDelivery.NOTIFICATION_ID, uponRouteDelivery);
        registerNotificationHandler(RouteNotify.NOTIFICATION_ID, uponRouteNotify);

        registerRequestHandler(DisseminateRequest.REQUEST_ID, uponDisseminateRequest);
        registerMessageHandler(DisseminationMessage.MSG_CODE, uponDisseminationMessage, DisseminationMessage.serializer);

    }

    @Override
    public void init(Properties properties) {
        this.topics = new HashMap<Integer, Topic>();
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
                    break;
                case UNSUBSCRIBE:
                    unsubscribe(msgId, msg, false);
                    break;
                case PUBLISH:
                    publish(msgId, msg, nodeID, false);
            }
        }
    };

    private void subscribe(int msgId, Message msg, Node upStream, boolean isResponsible) {
        if (topics.containsKey(msgId)) {
            topics.get(msgId).addNode(msg.getNodeInterested());
        } else {

            topics.put(msgId, new Topic(upStream));
            topics.get(msgId).addNode(msg.getNodeInterested());

            if (upStream != null) {
                routeRequest(msgId, msg, 1);
            } else {
                if (!isResponsible)
                    routeRequest(msgId, msg, 0);


            }
        }

    }

    private void unsubscribe(int msgId, Message msg, boolean isResponsible) {

        if (topics.containsKey(msgId)) {
            Node upStream = topics.get(msgId).getUpStream();
            Node nodeInterested = msg.getNodeInterested();
            int size = topics.get(msgId).removeNode(nodeInterested);
            if (size == 0 || (msg.getNodeInterested().getMyself() == myself)) {
                //sendUnsub to upstream
                //Todo este if não faz sentido... O upStream de um nodo pode ser ele mesmo?
                if (upStream.getMyself() != nodeID.getMyself()) {
                    DisseminationMessage m = new DisseminationMessage(msgId, msg);
                    sendMessage(m, upStream.getMyself());
                }
                topics.remove(msgId);
            }

        } else {
            if (!isResponsible)
                routeRequest(msgId, msg);
        }

    }

    private void publish(int msgId, Message msg, Node nodeInterested, boolean isResponsible) {
        String topicS = new String(topic, StandardCharsets.UTF_8);

        if (topics.containsKey(topicS)) {
            Topic thisTopic = topics.get(topicS);

            for (Node n : thisTopic.getNodes()) {
                if (n == nodeID) {
                    MessageDelivery notification = new MessageDelivery(topic, msg);
                    triggerNotification(notification);
                } else if (n != msg.getNodeSender()) {
                    msg.setSender(nodeID);
                    DisseminationMessage msgOut = new DisseminationMessage(topic, msg);
                    sendMessage(msgOut, n.getMyself());
                }
            }

            Node upStream = thisTopic.getUpStream();
            if (!isResponsible && upStream != null && msg.getNodeSender() != upStream) {
                msg.setSender(nodeID);
                DisseminationMessage msgOut = new DisseminationMessage(topic, msg);
                sendMessage(msgOut, upStream.getMyself());
            }

        } else {
            if (!isResponsible)
                routeRequest(topic, msg);
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
            Message msg = req.getMsg();
            int msgId = req.getMsgId();
            switch (msg.getTypeM()) {
                case SUBSCRIBE:
                    subscribe(msgId, msg, null, true);
                    break;
                case UNSUBSCRIBE:
                    unsubscribe(m.topic, m, m.getNodeInterested(), true);
                    break;
                case PUBLISH:
                    publish(m.topic, m, m.getNodeInterested(), true);
                    break;

            }
        }
    };

    private final ProtocolMessageHandler uponDisseminationMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            DisseminationMessage req = (DisseminationMessage) protocolMessage;
            Message m = req.getPayload();

            switch (m.getTypeM()) {
                case UNSUBSCRIBE:
                    unsubscribe(m.topic, m, m.getNodeInterested(), false);
                    break;
                case PUBLISH:
                    publish(m.topic, m, m.getNodeInterested(), false);
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
                case UNSUBSCRIBE:
                    unsubscribe(msgID, msg, false);
                    break;
                case PUBLISH:
                    publish(m.topic, m, m.getNodeInterested(), false);
                    break;

//                }
            }
        }
    };

}