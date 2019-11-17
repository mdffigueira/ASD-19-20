package dht;

import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolMessageHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.handlers.ProtocolTimerHandler;
import babel.protocol.GenericProtocol;
import babel.protocol.event.ProtocolMessage;
import babel.requestreply.ProtocolRequest;
import babel.timer.ProtocolTimer;
import dht.messages.*;
import dht.notification.*;

import dht.notification.RouteDelivery;
import dht.timers.FixFingerTimer;
import dht.timers.StabilizeTimer;
import dissemination.Message;
import dissemination.requests.RouteRequest;
import network.Host;
import network.INetwork;
import network.INodeListener;
import org.apache.logging.log4j.core.appender.routing.Route;

import java.net.InetAddress;
import java.util.Properties;

public class DHT extends GenericProtocol implements INodeListener {
    public final static short PROTOCOL_ID = 800;
    private final static int M = 32;
    private Node[] finger;
    private Node successor, predecessor;
    private Node nodeID;
    private int next = 0;

    public DHT(INetwork net) throws HandlerRegistrationException {
        super("DHT", PROTOCOL_ID, net);
        //messages
        registerMessageHandler(FindSuccessorMessage.MSG_CODE, uponFindSuccessorMessage, FindSuccessorMessage.serializer);
        registerMessageHandler(FindSuccessorResponseMessage.MSG_CODE, uponFindSuccessorResponseMessage, FindSuccessorResponseMessage.serializer);

        registerMessageHandler(FindSuccessorPredecessorMessage.MSG_CODE, uponFindSuccessorPredecessorMessage, FindSuccessorPredecessorMessage.serializer);
        registerMessageHandler(FindSuccessorPredecessorResponseMessage.MSG_CODE, uponFindSuccessorPredecessorResponseMessage, FindSuccessorPredecessorResponseMessage.serializer);

        registerMessageHandler(NotifyMessage.MSG_CODE, uponNotification, NotifyMessage.serializer);

        registerMessageHandler(RouteMessage.MSG_CODE,uponRouteMessage,RouteMessage.serializer);
        //Requests
        registerRequestHandler(RouteRequest.REQUEST_ID, uponRouteRequest);

        //Timers
        registerTimerHandler(StabilizeTimer.TimerCode, uponStabilizeTimer);
        registerTimerHandler(FixFingerTimer.TimerCode, uponFixFingerTimer);
    }

    @Override
    public void init(Properties props) {
        this.finger = new Node[M];
        if (props.containsKey("Contact")) {
            try {
                String[] hostElems = props.getProperty("Contact").split(":");
                Host contactNode = new Host(InetAddress.getByName(hostElems[0]), Short.parseShort(hostElems[1]));
                System.out.println(myself.hashCode());
                nodeID = new Node(myself.hashCode(), myself);
                System.out.println("I'm " + nodeID.getId());
                if (!contactNode.equals(myself)) {
                    addNetworkPeer(contactNode);
                    FindSuccessorMessage m = new FindSuccessorMessage(nodeID, -1);
                    sendMessage(m, contactNode);
                } else
                    createChordRing();


            } catch (Exception e) {
                System.err.println("Invalid contact on configuration: '" + props.getProperty("Contact"));
            }
        }

        setupPeriodicTimer(new StabilizeTimer(), 5000, 5000);
        setupPeriodicTimer(new FixFingerTimer(), 5000, 5000);

    }

    private void createChordRing() {
        System.out.println("I'm the creator of Chord network");
        System.out.println("################################################################################################################################################");
        predecessor = new Node(0, null);
        successor = new Node(myself.hashCode(), myself);
        System.out.println("my SUCCESSOR= " + successor.getId() + " & PREDECESSOR= " + predecessor.getId());
        System.out.println("################################################################################################################################################");
    }

    private Node findSuccessor(int id) {
        if (successor.getId() == nodeID.getId()) {
            return nodeID;
        }
        if (inclusionOrderRIncluded(id, nodeID.getId(), successor.getId())) {
            return successor;
        }
        Node newN = closestPrecedingNode(id);
        newN.isToSendAgain(true);
        return newN;
    }

    private Node closestPrecedingNode(int id) {
        for (int i = M - 1; i > 1; i--) {
            if (finger[i] != null) {
                if (inclusionOrder(finger[i].getId(), nodeID.getId(), id))
                    return finger[i];
            }
        }
        return nodeID;

    }

    private final ProtocolMessageHandler uponFindSuccessorMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            Node msg = ((FindSuccessorMessage) protocolMessage).getN();
            int nxt = ((FindSuccessorMessage) protocolMessage).getNext();
            Host peerH = msg.getMyself();
            int peerId = msg.getId();
            Node peerSuccessor = findSuccessor(peerId);
            if (!peerSuccessor.getSendAgain()) {
                System.out.println(peerId + " successor is " + peerSuccessor.getId());
                System.out.println("################################################################################################################################################");
                addNetworkPeer(peerH);
                FindSuccessorResponseMessage m = new FindSuccessorResponseMessage(peerSuccessor, nxt);
                sendMessage(m, peerH);

            } else {
                peerSuccessor.isToSendAgain(false);
                System.out.println("Asking to " + peerSuccessor.getId());
                FindSuccessorMessage m = new FindSuccessorMessage(msg, nxt);
                sendMessage(m, peerSuccessor.getMyself());
            }
            if (successor.getId() == nodeID.getId()) {
                successor = msg;
                System.out.println("my new SUCCESSOR is: " + successor.getId());
                for (int i = 1; i < M; i++) {
                    finger[i] = new Node(fingerNode(i), successor.getMyself());
                }
            }
        }
    };
    private final ProtocolMessageHandler uponFindSuccessorResponseMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            Node n = ((FindSuccessorResponseMessage) protocolMessage).getN();
            int nxt = ((FindSuccessorResponseMessage) protocolMessage).getNext();
            if (nxt == -1) {
                predecessor = new Node(0, null);
                successor = n;

                System.out.println("my successor= " + successor.getId() + " & PREDECESSOR= " + predecessor.getId());
                System.out.println("################################################################################################################################################");
            } else {
                finger[nxt] = new Node(fingerNode(next), n.getMyself());
                System.out.println(nxt + "-> " + finger[nxt].getId() + " and " + finger[nxt].getMyself());
            }
        }
    };


    private final ProtocolTimerHandler uponFixFingerTimer = new ProtocolTimerHandler() {
        @Override
        public void uponTimer(ProtocolTimer protocolTimer) {

            if (successor.getId() != nodeID.getId()) {
                System.out.println("-------Fix Finger------");
                next++;
                if (next > M)
                    next = 1;

                Node success = findSuccessor(fingerNode(next));
                if (success.getSendAgain()) {
                    success.isToSendAgain(false);
                    FindSuccessorMessage m = new FindSuccessorMessage(success, next);

                } else {
                    finger[next] = new Node(fingerNode(next), success.getMyself());
                    System.out.println(next + "-> " + finger[next].getId() + " and " + finger[next].getMyself());
                }
            }
        }
    };
    private final ProtocolTimerHandler uponStabilizeTimer = new ProtocolTimerHandler() {
        @Override
        public void uponTimer(ProtocolTimer protocolTimer) {
            if (successor.getId() != 0)
                if (successor.getId() != nodeID.getId()) {
                    //    System.out.println("let's stabilize!!");
                    System.out.println("--Stabilize--");
                    FindSuccessorPredecessorMessage m = new FindSuccessorPredecessorMessage(nodeID);
                    addNetworkPeer(successor.getMyself());
                    sendMessage(m, successor.getMyself());
                }

        }
    };
    private final ProtocolMessageHandler uponFindSuccessorPredecessorMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            Host n = ((FindSuccessorPredecessorMessage) protocolMessage).getN().getMyself();
            FindSuccessorPredecessorResponseMessage m = new FindSuccessorPredecessorResponseMessage(predecessor);
            addNetworkPeer(n);
            sendMessage(m, n);
        }
    };
    private final ProtocolMessageHandler uponFindSuccessorPredecessorResponseMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            //predecessor do meu sucessor
            Node x = ((FindSuccessorPredecessorResponseMessage) protocolMessage).getN();
            if (x.getId() != 0) {
                if (inclusionOrder(x.getId(), nodeID.getId(), successor.getId())) {
                    successor = x;
                    System.out.println("my new SUCCESSOR is: " + successor.getId());
                }
            }
            NotifyMessage m = new NotifyMessage(nodeID);
            addNetworkPeer(successor.getMyself());
            sendMessage(m, successor.getMyself());
        }
    };
    private final ProtocolMessageHandler uponNotification = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            Node msg = ((NotifyMessage) protocolMessage).getN();
            int nId = msg.getId();
            if (predecessor.getId() == 0 || inclusionOrder(nId, predecessor.getId(), nodeID.getId())) {
                predecessor = msg;
                System.out.println("my PREDECESSOR " + predecessor.getId());
            }

        }
    };
    private final ProtocolRequestHandler uponRouteRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest protocolRequest) {
            RouteRequest req = (RouteRequest) protocolRequest;
            int msgId = req.getID();
            Message msg = req.getMsg();
            Node toSend = findSuccessor(msgId);
            if (toSend.getId() != nodeID.getId()) {
                RouteMessage m = new RouteMessage(msgId, msg);
                sendMessage(m, toSend.getMyself());
                RouteNotify deliverN = new RouteNotify(toSend, null,0);
                triggerNotification(deliverN);
            } else {
                RouteDelivery routeDelivery = new RouteDelivery(msgId, msg);
                triggerNotification(routeDelivery);
            }
            // Node ID= req.getId();
            // int T = req.get

        }
    };

    private final ProtocolMessageHandler uponRouteMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            RouteMessage req = (RouteMessage) protocolMessage;
            int msgId = req.getNId();
            Message msg = req.getMessage();
            Node toSend = findSuccessor(msgId);
            if(toSend.getId()!=nodeID.getId()){
                RouteMessage m = new RouteMessage(msgId,msg);
                sendMessage(m,toSend.getMyself());
                RouteNotify deliverN = new RouteNotify(toSend,msg,1 );
                triggerNotification(deliverN);
            }
            else {
                RouteDelivery routeDelivery=new RouteDelivery(msgId,msg);
                triggerNotification(routeDelivery);
            }


        }
    };

    @Override
    public void nodeDown(Host peer) {
        System.out.println("Node" + peer + "is down");
        if (predecessor.getMyself().equals(peer))
            predecessor = new Node(0, null);
    }

    @Override
    public void nodeUp(Host host) {
        System.out.println("Node " + host + "is up");
    }

    @Override
    public void nodeConnectionReestablished(Host host) {
        System.out.println("Node " + host + "has Reconnected");
    }

    private boolean inclusionOrder(int id, int lower, int bigger) {
        if (lower < bigger)
            return id > lower && id < bigger;
        // else if(lower.compareTo(bigger)>0)
        return id < bigger || id > lower;

    }

    private boolean inclusionOrderRIncluded(int id, int lower, int bigger) {
        if (lower < bigger)
            return id > lower && id <= bigger;
        // else if(lower.compareTo(bigger)>0)
        return id <= bigger || id > lower;
    }

    private int fingerNode(int k) {
        return (int) ((nodeID.getId() + (Math.pow(2, k - 1))) % (Math.pow(2, M)));
    }
}
