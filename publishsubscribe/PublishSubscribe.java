package publishsubscribe;

import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import babel.notification.INotificationConsumer;
import org.apache.logging.log4j.core.util.FileUtils;

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
import publishsubscribe.messages.*;
import utils.Node;
import utils.Operation;
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
import io.netty.channel.unix.Buffer;
import multipaxos.MultiPaxos;
import multipaxos.notifications.OperationDone;
import network.INetwork;
import network.Host;
import publishsubscribe.delivers.PSDeliver;
import publishsubscribe.requests.*;

import javax.sound.midi.SysexMessage;

public class PublishSubscribe extends GenericProtocol implements INotificationConsumer {

    public final static short PROTOCOL_ID = 500;
    public final static String PROTOCOL_NAME = "Publish Subscribe";

    public final static int SUBSCRIBE = 1;
    public final static int UNSUBSCRIBE = 2;
    public final static int PUBLISH = 3;
    public final static int POPULARITY = 4;
    public final static float POP_PERCENTAGE = 0.7f;
    public static final int ADD_REPLICA = 1;
    public static final int REMOVE_REPLICA = 2;

    //Set of Topics
    private Set<byte[]> topics;
    private static int REPLICA_SIZE = 3;
    private Set<Host> membership;
    private Host leader;
    private int paxosN;
    private Map<Integer, TreeSet<Node>> topicSubs;
    private LinkedList<Operation> pending;
    private Set<Node> activeKnownNodes;
    private Path topicsFile, topSubsFile;

    @SuppressWarnings("deprecation")
    public PublishSubscribe(INetwork net) throws HandlerRegistrationException {
        super("Publish Subscribe", PROTOCOL_ID, net);

        //Messages
        registerMessageHandler(GetCurrMembshipMessage.MSG_CODE, uponGetCurrentMembershipMessage, GetCurrMembshipMessage.serializer);
        registerMessageHandler(AddReplicaMessageReply.MSG_CODE, uponAddReplicaMessageReply, AddReplicaMessageReply.serializer);
        registerMessageHandler(AddReplicaMessage.MSG_CODE, uponAddReplicaMessage, AddReplicaMessage.serializer);
        registerMessageHandler(GetMessage.MSG_CODE, uponGetMessage, GetMessage.serializer);
        //Requests
        registerRequestHandler(PSSubscribeRequest.REQUEST_ID, uponSubscribeRequest);
        registerRequestHandler(PSUnsubscribeRequest.REQUEST_ID, uponUnsubscribeRequest);
        registerRequestHandler(PSPublishRequest.REQUEST_ID, uponPublishRequest);

        //Notifications Produced
        registerNotification(PSDeliver.NOTIFICATION_ID, PSDeliver.NOTIFICATION_NAME);

        //Notifications
//        registerNotificationHandler(OperationDone.NOTIFICATION_ID, uponOperationDone);
        //registerNotificationHandler(MessageDelivery.NOTIFICATION_ID, uponMessageDelivery);
       // registerNotificationHandler(RouteDelivery.NOTIFICATION_ID, uponRouteDelivery);
        //registerNotificationHandler(UpdatePopularity.NOTIFICATION_ID, uponUpdatePopularityNotification);
        registerNotificationHandler(FloodBCastDeliver.NOTIFICATION_ID, uponFloodBCastDeliver);
    }

    @Override
    public void init(Properties props) {
        //Initialize State

        this.topics = new TreeSet<>();
        //		topicsFile = Paths.get("src/storage/topics.txt");
        //		topSubsFile = Paths.get("src/storage/topicsSubs.txt");
        //		if(Files.exists(topicsFile)) {
        //			try {
        //				List<String> file = Files.readAllLines(topicsFile);
        //
        //				for(String s : file) {
        //					topics.add(s.getBytes());
        //				}
        //			} catch (IOException e) {
        //				e.printStackTrace();
        //			}
        //		}
        //		else {
        //			try {
        //				Files.createFile(topicsFile);
        //			} catch (IOException e) {
        //				e.printStackTrace();
        //			}
        //		}


        this.membership = new HashSet<>();
        this.topicSubs = new HashMap<Integer, TreeSet<Node>>();
        this.activeKnownNodes = new TreeSet<Node>();
        this.pending = new LinkedList<>();
        paxosN = 0;
        if (props.containsKey("NetworkContactNode")) {
            try {
                String[] hostElems = props.getProperty("NetworkContactNode").split(":");
                System.out.println("Multi-Paxos: I'm " + myself);
                Host cNode = new Host(InetAddress.getByName(hostElems[0]), Short.parseShort(hostElems[1]));

                if (cNode.equals(myself)) {
                    StartRequest req = new StartRequest(paxosN, null);
                    req.setDestination(MultiPaxos.PROTOCOL_ID);
                    try {
                        sendRequest(req);
                    } catch (DestinationProtocolDoesNotExist e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    membership.add(myself);
                    leader = myself;
                } else {
                    addNetworkPeer(cNode);
                    AddReplicaMessage msg = new AddReplicaMessage(cNode);
                    System.out.println("Send Add Replica");
                    sendMessage(msg, cNode);
                    leader = cNode;
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();

            }
        }
    }

    @Override
    public void deliverNotification(ProtocolNotification notif) {
        if (notif instanceof OperationDone) {
        uponOperationDone(notif);
        }
        else if(notif instanceof MessageDelivery){
            uponMessageDelivery(notif);
        }
        else if (notif instanceof RouteDelivery){
            uponRouteDelivery(notif);
        }
        else if ( notif instanceof UpdatePopularity){
            uponUpdatePopularityNotification(notif);
        }
    }


    private void uponOperationDone(ProtocolNotification notif) {

        OperationDone m = (OperationDone) notif;
        Operation op = m.getOp();
        int iN = m.getInstanceNumber();
        int sN = m.getSequenceNumber();
        pending.remove(op);
        runOperation(op, iN, sN);
        if (!pending.isEmpty()) {
            sendOperationToMPaxos(pending.getFirst());
            System.out.println("not empty");
        }
    }


    private ProtocolMessageHandler uponAddReplicaMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            AddReplicaMessage m = (AddReplicaMessage) protocolMessage;
            if (membership.size() < REPLICA_SIZE) {
                Host repToAdd = m.getFrom();
                Operation op = new Operation(ADD_REPLICA, null, repToAdd, leader);
                pending.add(op);
                System.out.println("A mandar po paxos");
                sendOperationToMPaxos(op);
            }
        }
    };

    private ProtocolMessageHandler uponAddReplicaMessageReply = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            AddReplicaMessageReply m = (AddReplicaMessageReply) protocolMessage;
            Membership members = new Membership(m.getLeader(), m.getSequenceNumber(), m.getReplicas());
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


        public void uponRouteDelivery(ProtocolNotification not) {
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



        public void uponUpdatePopularityNotification(ProtocolNotification not) {
            UpdatePopularity req = (UpdatePopularity) not;
            if (PROTOCOL_ID == req.getProtocolId()) {
                int topic = req.getMsgId();
                int msgType = req.getTypeMsg();
                Node nodeInt = req.getNodeInterested();
                if (topicSubs.containsKey(topic)) {
                    if (msgType == SUBSCRIBE) {
                        topicSubs.get(topic).add(nodeInt);
                        //TODO:
                        //						addNodeToTopic(topic, nodeInt);
                        activeKnownNodes.add(nodeInt);
                    } else if (msgType == UNSUBSCRIBE) {
                        topicSubs.get(topic).remove(nodeInt);
                        //TODO:
                        //						removeNodeFromTopic(topic, nodeInt);
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
                        //TODO:
                        //						addNodeToTopic(topic, nodeInt);
                        activeKnownNodes.add(nodeInt);
                    }
                }
            }
        }


    //TODO:
//	private void removeNodeFromTopic(int topic, Node nodeInt) throws IOException{
//		Path topSubsFile = Paths.get("src/storage/topicsSubs.txt");
//		String nodeInterested = nodeInt.getMyself().getAddress()+":"+nodeInt.getMyself().getPort();
//		BufferedWriter buf;
//
//		if(Files.exists(topSubsFile)) {
//			List<String> topics = Files.readAllLines(topSubsFile);
//			Path temp = Files.createTempFile(null, null, ".txt");
//			buf = Files.newBufferedWriter(temp);
//
//			for(String str: topics) {
//				if(str.contains(topic+";"))
//					if(str.contains(nodeInterested)) {
//						str = str.replace(";"+nodeInterested+";",";");
//					}
//				buf.write(str);
//				buf.newLine();
//			}
//			buf.flush();
//			buf.close();
//			Files.copy(temp, topSubsFile);
//		}
//	}

    //TODO:
//		private void addNodeToTopic(int topic, Node nodeInt) throws IOException{
//
//			String nodeInterested = nodeInt.getMyself().getAddress()+":"+nodeInt.getMyself().getPort();
//			BufferedWriter buf;
//
//			if(Files.exists(topSubsFile)) {
//				List<String> topics = Files.readAllLines(topSubsFile);
//				Path temp = Files.createTempFile(null, null, ".txt");
//				buf = Files.newBufferedWriter(temp);
//
//				for(String str: topics) {
//					if(str.contains(topic+";"))
//						if(!str.contains(nodeInterested)) {
//							str = str + nodeInterested+";";
//						}
//					buf.write(str);
//					buf.newLine();
//				}
//				Files.copy(temp, topSubsFile);
//			}
//			else {
//				Files.createFile(topSubsFile);
//				buf = Files.newBufferedWriter(topSubsFile);
//				buf.write(nodeInt.getId()+ ";"+nodeInterested+";");
//				buf.newLine();
//			}
//			buf.flush();
//			buf.close();
//		}

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

    private void getMessage(Host h, byte[] topic, int seqNumber) {
        GetMessage msg = new GetMessage(topic, seqNumber);
        sendMessage(msg, h);
    }

    private ProtocolMessageHandler uponGetMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            //TODO: enviar de volta a mensagem relativa aqueel topico e seqNumber

        }
    };

    private ProtocolRequestHandler uponSubscribeRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest r) {
            PSSubscribeRequest req = (PSSubscribeRequest) r;
            byte[] topic = req.getTopic();
            topics.add(topic);
            //TODO:
            //			BufferedWriter buffer = Files.newBufferedWriter(topicsFile);
            //			buffer.write(new String(topic, StandardCharsets.UTF_8));
            disseminateRequest(req.getTopic(), null, SUBSCRIBE);
        }
    };

    private ProtocolRequestHandler uponUnsubscribeRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest r) {
            PSUnsubscribeRequest req = (PSUnsubscribeRequest) r;
            byte[] topic = req.getTopic();
            topics.remove(topic);
            //TODO:
            //			List<String> topicsStr = Files.readAllLines(topicsFile);
            //			Path temp = Files.createTempFile(null, null, null);
            //			BufferedWriter in = Files.newBufferedWriter(temp);
            //			String topString = new String(topic, StandardCharsets.UTF_8);
            //			for(String s: topicsStr)
            //				if(!s.equals(topString))
            //					in.write(s);
            //			in.flush();
            //			in.close();
            //			Files.copy(temp, topicsFile);
            disseminateRequest(req.getTopic(), null, UNSUBSCRIBE);
        }
    };


    private ProtocolRequestHandler uponPublishRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest r) {
            PSPublishRequest req = (PSPublishRequest) r;
            byte[] topic = req.getTopic();
            Message message = new Message(topic, req.getMessage(), POPULARITY);
            Operation op = new Operation(PUBLISH, message, null, leader);
            pending.add(op);
            sendOperationToMPaxos(op);
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

    private void uponMessageDelivery(ProtocolNotification not){

            MessageDelivery req = (MessageDelivery) not;
            PSDeliver deliver = new PSDeliver(req.getMessage().getTopic(), req.getMessage().getMessage());
            triggerNotification(deliver);
        }


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

    private void runOperation(Operation op, int iN, int sN) {
        switch (op.getType()) {
            case REMOVE_REPLICA:
                Host repToRemove = op.getReplica();
                membership.remove(repToRemove);
                if (repToRemove == myself) {
                    ResetMP req = new ResetMP();
                    req.setDestination(MultiPaxos.PROTOCOL_ID);
                    AddReplicaMessage msg = new AddReplicaMessage(leader);
                    sendMessage(msg, leader);

                } else {
                    ChangeStates r = new ChangeStates(leader, membership);
                    r.setDestination(MultiPaxos.PROTOCOL_ID);
                    try {
                        sendRequest(r);
                    } catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
                        destinationProtocolDoesNotExist.printStackTrace();
                    }

                }
                break;
            case ADD_REPLICA:
                Host repToAdd = op.getReplica();
                membership.add(repToAdd);
                ChangeStates r = new ChangeStates(leader, membership);
                r.setDestination(MultiPaxos.PROTOCOL_ID);
                try {
                    sendRequest(r);
                } catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
                    destinationProtocolDoesNotExist.printStackTrace();
                }
                if (myself == leader) {
                    AddReplicaMessageReply msg = new AddReplicaMessageReply(membership, iN, sN, leader);
                    addNetworkPeer(repToAdd);
                    sendMessage(msg, repToAdd);
                }
                break;
            case PUBLISH:
                if (myself == leader) {
                    Message message = op.getMsg();
                    RouteRequest routeReq = new RouteRequest(message.getTopic().hashCode(), message, 0);
                    routeReq.setDestination(DHT.PROTOCOL_ID);
                    try {
                        sendRequest(routeReq);
                    } catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
                        destinationProtocolDoesNotExist.printStackTrace();
                        System.exit(1);
                    }
                }
                break;
        }
    }

    private void sendOperationToMPaxos(Operation op) {
        if (pending.size() <= 1) {
            OperationRequest opReq = new OperationRequest(op);
            opReq.setDestination(MultiPaxos.PROTOCOL_ID);
            try {
                sendRequest(opReq);
            } catch (DestinationProtocolDoesNotExist e) {
                e.printStackTrace();
            }
        }
    }
}
