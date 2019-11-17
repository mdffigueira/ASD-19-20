package hyparview;

import babel.exceptions.DestinationProtocolDoesNotExist;
import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolMessageHandler;
//import babel.handlers.ProtocolReplyHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.handlers.ProtocolTimerHandler;
import babel.protocol.GenericProtocol;
import babel.requestreply.ProtocolRequest;
import babel.timer.ProtocolTimer;
import hyparview.messages.HyParViewDisconnectMessage;
import hyparview.messages.HyParViewForwardJoinMessage;
import hyparview.messages.HyParViewJoinReplyMessage;
import hyparview.messages.HyParViewJoinMessage;
import hyparview.messages.HyParViewNeighborMessage;
import hyparview.messages.HyParViewNeighborReplyMessage;
import hyparview.messages.HyParViewShuffleMessage;
import hyparview.messages.HyParViewShuffleReplyMessage;
import hyparview.replys.HyParViewMembershipReply;
import hyparview.requests.HyParViewMembershipRequest;
import hyparview.timers.ShuffleTimer;
import network.Host;
import network.INetwork;
import network.INodeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import babel.protocol.event.*;

import java.net.InetAddress;
import java.util.*;

public class HyParViewMembership extends GenericProtocol implements INodeListener {

    //Numeric identifier of the protocol
    public final static short PROTOCOL_ID = 400;
    public final static String PROTOCOL_NAME = "HyParView Membership";
    private final static int aViewSize = 4;//Fanout +1
    private final static int pViewSize = 30;
    private final static int KA = 3;
    private final static int KP = 4;
    private final static int SHUFFLE_TTL = 8;
    private final static int HIGH_PRIORITY = 1;
    private final static int LOW_PRIORITY = 0;
    private final static int ACCEPTED = 1;
    private final static int N_ACCEPTED = 0;


    private Set<Host> activeView; //a node active partial view
    private Set<Host> passiveView; //a node passive view
    private Host contactNode; //a node already present in the overlay
    //newNode: the node joining the overlay
    private int ARWL = 5; //Active random walk length
    private int PRWL = 3; //Passive random walk length

    private static final Logger logger = LogManager.getLogger(HyParViewMembership.class);

    public HyParViewMembership(INetwork net) throws HandlerRegistrationException {
        super(PROTOCOL_NAME, HyParViewMembership.PROTOCOL_ID, net);

        //Declare Messages sent/received of the protocol
        registerMessageHandler(HyParViewJoinMessage.MSG_CODE, uponJoinMessage, HyParViewJoinMessage.serializer);
        registerMessageHandler(HyParViewJoinReplyMessage.MSG_CODE, uponJoinReplyMessage, HyParViewJoinReplyMessage.serializer);
        registerMessageHandler(HyParViewForwardJoinMessage.MSG_CODE, uponJoinForwardMessage, HyParViewForwardJoinMessage.serializer);
        registerMessageHandler(HyParViewDisconnectMessage.MSG_CODE, uponDisconnectMessage, HyParViewDisconnectMessage.serializer);
        registerMessageHandler(HyParViewShuffleMessage.MSG_CODE, uponShuffleMessage, HyParViewShuffleMessage.serializer);
        registerMessageHandler(HyParViewShuffleReplyMessage.MSG_CODE, uponShuffleReplyMessage, HyParViewShuffleReplyMessage.serializer);

        registerMessageHandler(HyParViewNeighborMessage.MSG_CODE, uponNeighbourMessage, HyParViewNeighborMessage.serializer);
        registerMessageHandler(HyParViewNeighborReplyMessage.MSG_CODE, uponNeighbourReplyMessage, HyParViewNeighborReplyMessage.serializer);

        //Declare Timers of the Protocol
        registerTimerHandler(ShuffleTimer.TimerCode, uponShuffleTimer);
        //registerTimerHandler(EchoProtocolTimer.TimerCode, uponEchoProtocolTimer);

        //declare requests exposed by the protocol
        registerRequestHandler(HyParViewMembershipRequest.REQUEST_ID, uponGetMembershipRequest);


        //declare replies consumed by the protocol
    }

    @Override
    public void init(Properties props) {

        //upon init do
        //TODO   addNodeActiveView(contactNode)
        //TODO Send(Join, contactNode, myself);

        //Setup configuration of the protocol
        registerNodeListener(this);

        this.activeView = new HashSet<>();
        this.passiveView = new HashSet<>();
        if (props.containsKey("Contact")) {
            try {
                String[] hostElems = props.getProperty("Contact").split(":");
                contactNode = new Host(InetAddress.getByName(hostElems[0]), Short.parseShort(hostElems[1]));
                if (!activeView.contains(contactNode) && !contactNode.equals(myself)) {
                    addNetworkPeer(contactNode);
                    addNodeActiveView(contactNode);
                    System.out.println("Active view de " + myself.getPort());
                    for (Host a : activeView) {
                        System.out.println(a);
                    }
                    HyParViewJoinMessage m = new HyParViewJoinMessage(myself);
                    System.out.println("I will join Myself to the contactNode " + contactNode.getAddress() + ":" + contactNode.getPort());
                    sendMessage(m, contactNode);
                }
            } catch (Exception e) {
                System.err.println("Invalid contact on configuration: '" + props.getProperty("Contact"));
            }
        }


        //Setup timers
        UUID shuffleTimerUUID = setupPeriodicTimer(new ShuffleTimer(), 1000, 30000);
        //UUID echoTimerUUID = setupPeriodicTimer(new EchoProtocolTimer(), 1000, 5000);
    }

    private ProtocolTimerHandler uponShuffleTimer = new ProtocolTimerHandler() {

        @Override
        public void uponTimer(ProtocolTimer timer) {
            periodicShuffle();
        }

    };
    //Active View Management
    private final ProtocolMessageHandler uponNeighbourMessage = new ProtocolMessageHandler() {

        @Override
        public void receive(ProtocolMessage msg) {
            // TODO Auto-generated method stub
            HyParViewNeighborReplyMessage m;
            Host origin = ((HyParViewNeighborMessage) msg).getIdentifier();
            if (((HyParViewNeighborMessage) msg).getPriorityLevel() == (HIGH_PRIORITY)) {
                if (activeView.size() >= aViewSize) {
                    dropRandomElementFromActiveView();
                    activeView.add(origin);
                }
                m = new HyParViewNeighborReplyMessage(myself, ACCEPTED);
            } else {
                if (activeView.size() >= aViewSize) {
                    m = new HyParViewNeighborReplyMessage(myself, N_ACCEPTED);
                } else

                    m = new HyParViewNeighborReplyMessage(myself, ACCEPTED);
            }
            sendMessage(m, origin);

        }

    };
    //Active View Management
    private final ProtocolMessageHandler uponNeighbourReplyMessage = new ProtocolMessageHandler() {

        @Override
        public void receive(ProtocolMessage msg) {
            // TODO Auto-generated method stub
            int answer = ((HyParViewNeighborReplyMessage) msg).getAnswer();
            Host node = ((HyParViewNeighborReplyMessage) msg).getIdentifier();
            if (answer == ACCEPTED) {
                passiveView.remove(node);
            }

        }
    };

    //Passive View Management
    private final ProtocolMessageHandler uponShuffleMessage = new ProtocolMessageHandler() {


        @Override
        public void receive(ProtocolMessage msg) {
            int ttl = ((HyParViewShuffleMessage) msg).getTTL();
            Host sender = ((HyParViewShuffleMessage) msg).getSender();
            Host destination = ((HyParViewShuffleMessage) msg).getDestination();
            Set<Host> shuffleSet = ((HyParViewShuffleMessage) msg).getShuffleSet();
            ttl--;

            if (ttl > 0 && activeView.size() > 1) {
                Host newNode = randomNode(activeView);
                HyParViewShuffleMessage m = new HyParViewShuffleMessage(sender, newNode, shuffleSet, ttl);
                sendMessage(m, newNode);
            } else {
                Set<Host> shuffleSetRepply = randomSet(passiveView, shuffleSet.size());
                HyParViewShuffleReplyMessage m = new HyParViewShuffleReplyMessage(destination, sender, shuffleSetRepply);
                sendMessageSideChannel(m, sender);
                addNewElementstoPV(shuffleSet, shuffleSetRepply);

            }
        }
    };
    //Passive View Management
    private final ProtocolMessageHandler uponShuffleReplyMessage = new ProtocolMessageHandler() {

        @Override
        public void receive(ProtocolMessage msg) {
            Set<Host> receivedShuffleSet = ((HyParViewShuffleReplyMessage) msg).getshuffleReply();
            // TODO Auto-generated method stub
            //uponShuffleReplyMessage
            int count = receivedShuffleSet.size() + passiveView.size() - pViewSize;
            for (Host h : receivedShuffleSet) {
                if (h == myself || activeView.contains(h) || passiveView.contains(h))
                    receivedShuffleSet.remove(h);

            }
            while (count > 0) {
                Host h = randomNode(passiveView);
                if (passiveView.remove(h))
                    count--;
            }
            passiveView.addAll(receivedShuffleSet);
        }

    };

    //Requests:	GetMembershipReq()
    private ProtocolRequestHandler uponGetMembershipRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest request) {
            HyParViewMembershipRequest req = (HyParViewMembershipRequest) request;
            HyParViewMembershipReply reply = new HyParViewMembershipReply(req.getIdentifier(), activeView);


            reply.invertDestination(req);
            try {
                sendReply(reply);
            } catch (DestinationProtocolDoesNotExist destinationProtocolDoesNotExist) {
                destinationProtocolDoesNotExist.printStackTrace();
                System.out.println("failed");
                System.exit(1);
            }
        }
    };


    //Receive(JOIN,newNode)
    private final ProtocolMessageHandler uponJoinMessage = new ProtocolMessageHandler() {

        @Override
        public void receive(ProtocolMessage msg) {
            Host newNode = ((HyParViewJoinMessage) msg).getSender();
            addNetworkPeer(newNode);
            addNodeActiveView(newNode);
            System.out.println("Active view de " + myself.getPort());
            for (Host n : activeView) {
                System.out.println(n);
                if (!n.equals(newNode)) {

                    //HyParViewJoinMessage m = new HyParViewJoinMessage(contactNode, myself);
                    HyParViewForwardJoinMessage m = new HyParViewForwardJoinMessage(newNode, ARWL, myself);
                    sendMessage(m, n);
                }
            }
        }
    };

    private final ProtocolMessageHandler uponJoinReplyMessage = new ProtocolMessageHandler() {

        @Override
        public void receive(ProtocolMessage msg) {
            Host newNode = ((HyParViewJoinReplyMessage) msg).getSender();
            addNetworkPeer(newNode);
            addNodeActiveView(newNode);
            System.out.println("Active view de " + myself.getPort());
            for (Host a : activeView) {
                System.out.println(a);
            }
        }
    };

    //Receive(FORWARDJOIN, newNode, timeToLive, sender)
    private final ProtocolMessageHandler uponJoinForwardMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage msg) {
            Host newNode = ((HyParViewForwardJoinMessage) msg).getNewNode();
            int ttl = ((HyParViewForwardJoinMessage) msg).getTTL();
            Host sender = ((HyParViewForwardJoinMessage) msg).getMyself();
            if (ttl == 0 || activeView.size() == 1) {
                addNodeActiveView(newNode);
                addNetworkPeer(newNode);
                System.out.println("Active view de " + myself.getPort());
                for (Host a : activeView) {
                    System.out.println(a);
                }
                HyParViewJoinReplyMessage msgReply = new HyParViewJoinReplyMessage(newNode, myself);
                sendMessage(msgReply, newNode);
            } else {
                if (ttl == PRWL)
                    addNodePassiveView(newNode);
                Host n = sender;
                while (n.equals(sender)) {
                    n = randomNode(activeView);
                }
                System.out.println("Envia FowardJoin para " + n);
                HyParViewForwardJoinMessage m = new HyParViewForwardJoinMessage(newNode, ttl - 1, myself);
                sendMessage(m, n);


            }
        }

    };

    private final ProtocolMessageHandler uponDisconnectMessage = new ProtocolMessageHandler() {

        @Override
        public void receive(ProtocolMessage msg) {
            Host peer = ((HyParViewDisconnectMessage) msg).getSender();
            if (activeView.contains(peer)) {
                activeView.remove(peer);
                addNodePassiveView(peer);
            }
        }
    };

    public void addNewElementstoPV(Set<Host> myShuffleSet, Set<Host> receivedShuffleSet) {
        System.out.println(receivedShuffleSet.size());
        for (Host h : receivedShuffleSet) {
            System.out.println("Eis o null: "+h);
            if (h == myself || activeView.contains(h) || passiveView.contains(h))
                receivedShuffleSet.remove(h);

        }
        int count = receivedShuffleSet.size() + passiveView.size() - pViewSize;
        if (count > 0) {
            for (Host h : myShuffleSet) {
                passiveView.remove(h);
                count--;
                if (count == 0)
                    break;
            }
            while (count >= 0) {
                Host h = randomNode(passiveView);
                if (passiveView.remove(h))
                    count--;

            }
        }
        passiveView.addAll(receivedShuffleSet);
        System.out.println("woow vou meter cenas na PassiveView "+ passiveView.size());
        for (Host p : passiveView)
            System.out.println(p);


    }

    public void addNodeActiveView(Host node) {
        //verificação se a active view está cheia
        if (!node.equals(myself)) {
            if (activeView.size() >= aViewSize) {
                dropRandomElementFromActiveView();
            }
            activeView.add(node);
            passiveView.remove(node);
        }
    }

    public void dropRandomElementFromActiveView() {

        Host h = randomNode(activeView);
        HyParViewDisconnectMessage m = new HyParViewDisconnectMessage(h, myself);
        sendMessage(m, h);
        activeView.remove(h);
        passiveView.add(h);
    }

    public void addNodePassiveView(Host node) {
        if (node.getAddress() != myself.getAddress() ||
                (node.getAddress() == myself.getAddress() && node.getPort() != myself.getPort())) {
            if (!activeView.contains(node) && !passiveView.contains(node)) {
                if (passiveView.size() >= pViewSize) {
                    Host nodeToRemove = randomNode(passiveView);
                    passiveView.remove(nodeToRemove);
                }
                removeNetworkPeer(node);
                passiveView.add(node);
            }
        }
        System.out.println("PassiveView");
        for (Host p : passiveView)
            System.out.println(p);

    }

    public void periodicShuffle() {
        Set<Host> kaSet = randomSet(this.activeView, KA);
        System.out.println("Ka Set:");
        for (Host ka : kaSet)
            System.out.println(ka);
        Set<Host> kpSet = randomSet(this.passiveView, KP);
        System.out.println("Kp Set:");
        for (Host kp : kpSet)
            System.out.println(kp);
        System.out.println("Shuffle primo!!");
        Set<Host> shuffleSet = new HashSet<>();
        shuffleSet.addAll(kaSet);
        shuffleSet.addAll(kpSet);
        if (activeView.size() != 0) {
            Host destinationNode = randomNode(activeView);
            HyParViewShuffleMessage m = new HyParViewShuffleMessage(myself, destinationNode, shuffleSet, SHUFFLE_TTL);

            sendMessage(m, destinationNode);
        }
    }
    //Implementation of the INodeListener interface

    public void nodeDown(Host peer) {
        System.out.println("node down: " + peer);
        int priorityLevel;
        removeNetworkPeer(peer);
        activeView.remove(peer);
        if (activeView.size() == 0)
            priorityLevel = HIGH_PRIORITY;
        else
            priorityLevel = LOW_PRIORITY;
        Host newNode = randomNode(passiveView);
        addNetworkPeer(newNode);
        HyParViewNeighborMessage m = new HyParViewNeighborMessage(myself, priorityLevel);
        sendMessage(m, newNode);
        activeView.add(newNode);
        passiveView.remove(newNode);


    }

    @Override
    public void nodeUp(Host peer) {
        System.out.println("node up: " + peer);

    }

    @Override
    public void nodeConnectionReestablished(Host peer) {
        //System.out.println("connection reestablished: " + peer);
    }

    //Aux Methods
    private Set<Host> randomSet(Set<Host> set, int size) {
        HashSet<Host> tempSet = new HashSet<>();
        Random rand = new Random();
        while (tempSet.size() < set.size() && tempSet.size() < size) {
            int temp = rand.nextInt(set.size());
            int counter = 0;
            for (Host h : set) {
                if (counter == temp) {
                    tempSet.add(h);
                }
                counter++;
            }
        }
        return tempSet;

    }

    private Host randomNode(Set<Host> s) {
        int number = new Random().nextInt(s.size());
        int count = 0;
        Host aux = null;
        for (Host h : s) {
            if (number == count) {
                aux = h;
                break;
            }
            count++;
        }
        return aux;
    }

}
