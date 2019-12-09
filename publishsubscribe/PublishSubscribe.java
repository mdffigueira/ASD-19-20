package publishsubscribe;

import java.util.HashMap;
import java.util.Map;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
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
import multipaxos.Notification.OperationDone;
import publishsubscribe.messages.AddReplicaMessage;
import publishsubscribe.messages.AddReplicaMessageReply;
import utils.Node;
import dht.notification.RouteDelivery;
import dissemination.Dissemination;
import utils.Membership;
import utils.Message;
import dissemination.notification.MessageDelivery;
import dissemination.notification.UpdatePopularity;
import dissemination.requests.RouteRequest;
import floodbcast.FloodBCast;
import floodbcast.delivers.FloodBCastDeliver;
import floodbcast.requests.FloodBCastRequest;
import multipaxos.MultiPaxos;
import network.INetwork;
import network.Host;
import publishsubscribe.delivers.PSDeliver;
import publishsubscribe.messages.GetCurrMembshipMessage;
import publishsubscribe.messages.RemoveReplicaMessage;
import publishsubscribe.messages.ReturnCurrMembshipMessage;
import publishsubscribe.requests.DisseminateRequest;
import publishsubscribe.requests.PSPublishRequest;
import publishsubscribe.requests.PSSubscribeRequest;
import publishsubscribe.requests.PSUnsubscribeRequest;
import publishsubscribe.requests.*;
import utils.Operation;

public class PublishSubscribe extends GenericProtocol {

    public final static short PROTOCOL_ID = 500;
    public final static String PROTOCOL_NAME = "Publish Subscribe";

    public final static int SUBSCRIBE = 1;
    public final static int UNSUBSCRIBE = 2;
    public final static int PUBLISH = 3;
    public final static int POPULARITY = 4;
    public final static float POP_PERCENTAGE = 0.7f;


    //Set of Topics
    private Set<byte[]> topics;
    private static int REPLICA_SIZE = 3;
    private Set<Host> membership;
    private Host leader;
    private int paxosN;
    private Map<Integer, TreeSet<Node>> topicSubs;
    private Set<Node> activeKnownNodes;
    private Map

    @SuppressWarnings("deprecation")
    public PublishSubscribe(INetwork net) throws HandlerRegistrationException {
        super("Publish Subscribe", PROTOCOL_ID, net);

        //Messages
        registerMessageHandler(GetCurrMembshipMessage.MSG_CODE, uponGetCurrentMembershipMessage, GetCurrMembshipMessage.serializer);
        registerMessageHandler(AddReplicaMessageReply.MSG_CODE, uponAddReplicaMessageReply, AddReplicaMessageReply.serializer);
        registerMessageHandler(AddReplicaMessage.MSG_CODE, uponAddReplicaMessage, AddReplicaMessage.serializer);
        registerMessageHandler(RemoveReplicaMessage.MSG_CODE, uponRemoveReplicaMessage, RemoveReplicaMessage.serializer);

        //Requests
        registerRequestHandler(PSSubscribeRequest.REQUEST_ID, uponSubscribeRequest);
        registerRequestHandler(PSUnsubscribeRequest.REQUEST_ID, uponUnsubscribeRequest);
        registerRequestHandler(PSPublishRequest.REQUEST_ID, uponPublishRequest);

        //Notifications
		registerNotificationHandler(OperationDone.NOTIFICATION_ID, uponOperationDone);
        //Notifications Produced
        registerNotification(PSDeliver.NOTIFICATION_ID, PSDeliver.NOTIFICATION_NAME);
        registerNotificationHandler(MessageDelivery.NOTIFICATION_ID, uponMessageDelivery);
        registerNotificationHandler(RouteDelivery.NOTIFICATION_ID, uponRouteDelivery);
        registerNotificationHandler(UpdatePopularity.NOTIFICATION_ID, uponUpdatePopularityNotification);
        registerNotificationHandler(FloodBCastDeliver.NOTIFICATION_ID, uponFloodBCastDeliver);
    }

    @Override
    public void init(Properties props) {

        //Initialize State
        this.topics = new TreeSet<>();
        this.membership = new HashSet<>();
        this.topicSubs = new HashMap<Integer, TreeSet<Node>>();
        this.activeKnownNodes = new TreeSet<Node>();
        //TODO: Tens que descobrir o que é o initialState
        paxosN = 0;
        if (props.contains("NetworkContactNode")) {

            try {
                String[] hostElems = props.getProperty("NetworkContactNode").split(":");
                System.out.println("Multi-Paxos: I'm " + myself);
                Host contactNode = new Host(InetAddress.getByName(hostElems[0]), Short.parseShort(hostElems[1]));

                if (contactNode.equals(myself)) {
                    StartRequest req = new StartRequest(paxosN, null);
                    req.setDestination(MultiPaxos.PROTOCOL_ID);
                    sendRequest(req);
                    membership.add(myself);
                    leader = myself;
                } else {
                    AddReplicaMessage msg = new AddReplicaMessage(contactNode);
                    sendMessage(msg, contactNode);
                    leader = contactNode;
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (DestinationProtocolDoesNotExist e) {
                e.printStackTrace();
            }


        }
    }

    private ProtocolNotificationHandler uponOperationDone = new ProtocolNotificationHandler() {
        @Override
        public void uponNotification(ProtocolNotification protocolNotification) {
            OperationDone
        }
    };

    private ProtocolMessageHandler uponAddReplicaMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            AddReplicaMessage m = (AddReplicaMessage) protocolMessage;
            //TODO: Não tem que mandar para o paxos para os seus visinhos saberem que tem um garino novo?
            if (membership.size() < REPLICA_SIZE) {
                membership.add(m.getH());
                AddReplicaMessageReply msg = new AddReplicaMessageReply(membership, paxosN, leader);
                sendMessage(msg, m.getFrom());
            }
        }
    };

    private ProtocolMessageHandler uponRemoveReplicaMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            RemoveReplicaMessage m = (RemoveReplicaMessage) protocolMessage;
            membership.remove(m.getH());
            if (m.getH() == leader) {
                //TODO: decide new leader
            }
        }
    };

    private ProtocolMessageHandler uponAddReplicaMessageReply = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            AddReplicaMessageReply m = (AddReplicaMessageReply) protocolMessage;
            //TODO: por defenir
            int seqNumber = 0;
            Membership members = new Membership(m.getH(), seqNumber, m.getReplicas());
            StartRequest req = new StartRequest(m.getInstancePaxos(), members);
            req.setDestination(MultiPaxos.PROTOCOL_ID);
            try {
                sendRequest(req);
            } catch (DestinationProtocolDoesNotExist e) {
                e.printStackTrace();
            }
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

    private ProtocolNotificationHandler uponRouteDelivery = new ProtocolNotificationHandler() {
        @Override
        public void uponNotification(ProtocolNotification not) {
            RouteDelivery req = (RouteDelivery) not;
            if (PROTOCOL_ID == req.getProtocolId()) {
                Message msg = req.getMsg();
                int msgId = req.getMsgId();
                boolean popular = topicSubs.containsKey(msgId) ? (topicSubs.get(msgId).size() / activeKnownNodes.size() > POP_PERCENTAGE ? true : false) : false;
                if (!popular) {
                    disseminateRequest(msg.getTopic(), msg.getMessage(), msg.getTypeM());
                } else {
                    sendToFlood(msg);
                }
            }
        }
    };

    private ProtocolNotificationHandler uponUpdatePopularityNotification = new ProtocolNotificationHandler() {
        @Override
        public void uponNotification(ProtocolNotification not) {
            UpdatePopularity req = (UpdatePopularity) not;
            if (PROTOCOL_ID == req.getProtocolId()) {
                int topic = req.getMsgId();
                int msgType = req.getTypeMsg();
                Node nodeInt = req.getNodeInterested();
                if (topicSubs.containsKey(topic)) {
                    if (msgType == SUBSCRIBE) {
                        topicSubs.get(topic).add(nodeInt);
                        activeKnownNodes.add(nodeInt);
                    } else if (msgType == UNSUBSCRIBE) {
                        topicSubs.get(topic).remove(nodeInt);
                        boolean removeFromActiveNodes = true;
                        for (TreeSet<Node> subs : topicSubs.values()) {
                            if (subs.contains(nodeInt))
                                removeFromActiveNodes = false;
                        }
                        if (removeFromActiveNodes) activeKnownNodes.remove(nodeInt);
                    }
                } else {
                    if (msgType == SUBSCRIBE) {
                        TreeSet<Node> nodes = new TreeSet<>();
                        nodes.add(nodeInt);
                        topicSubs.put(topic, nodes);
                        activeKnownNodes.add(nodeInt);
                    }
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
            Message message = new Message(topic, req.getMessage(), POPULARITY);
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

    private ProtocolMessageHandler uponGetCurrentMembershipMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            GetCurrMembshipMessage msg = (GetCurrMembshipMessage) protocolMessage;
            ReturnCurrMembshipMessage reply = new ReturnCurrMembshipMessage(membership);
            sendMessage(reply, msg.getFrom());
        }
    };

    private void getCurrentMembership(Host node) {
        GetCurrMembshipMessage msg = new GetCurrMembshipMessage();
        sendMessage(msg, node);
    }
}
