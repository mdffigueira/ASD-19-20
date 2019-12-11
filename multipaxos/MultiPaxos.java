package multipaxos;

import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolMessageHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.handlers.ProtocolTimerHandler;
import babel.protocol.GenericProtocol;
import babel.protocol.event.ProtocolMessage;
import babel.requestreply.ProtocolRequest;
import babel.timer.ProtocolTimer;
import multipaxos.messages.*;
import multipaxos.notifications.OperationDone;
import multipaxos.timers.NewLeaderTimer;
import multipaxos.timers.NoMajorityAcceptTimer;
import multipaxos.timers.NoMajorityPrepareTimer;
import multipaxos.timers.NoOpTimer;
import network.Host;
import network.INetwork;
import network.INodeListener;
import publishsubscribe.requests.ChangeStates;
import publishsubscribe.requests.OperationRequest;
import publishsubscribe.requests.ResetMP;
import publishsubscribe.requests.StartRequest;
import utils.Membership;
import utils.Operation;

import java.util.*;

public class MultiPaxos extends GenericProtocol implements INodeListener {
    public final static short PROTOCOL_ID = 200;
    public final static int MAXREPLICAS = 3;

    private int np;// (highest prepare) nº do lider
    private int sequenceNumber;//n=>psn
    private int instanceNumber;//(nºo de instanceNumberpaxos)
    private Operation op;
    UUID noOpTimer, noMajorityPrepareTimer, noMajorityAcceptTimer, newLeaderTimer;
    Set<Host> replicas;
    Set<Host> acceptAset;
    Set<Host> leaderAset;

    Host leader;

    public MultiPaxos(INetwork net) throws HandlerRegistrationException {
        super("MultiPaxos", MultiPaxos.PROTOCOL_ID, net);

        //Notification Produced
        registerNotification(OperationDone.NOTIFICATION_ID, OperationDone.NOTIFICATION_NAME);

        //messages
        registerMessageHandler(PrepareMessage.MSG_CODE, uponPrepareMessage, PrepareMessage.serializer);
        registerMessageHandler(PrepareOkMessage.MSG_CODE, uponPrepareOKMessage, PrepareOkMessage.serializer);
        registerMessageHandler(AcceptMessage.MSG_CODE, uponAcceptMessage, AcceptMessage.serializer);
        registerMessageHandler(AcceptOkMessage.MSG_CODE, uponAcceptOKMessage, AcceptOkMessage.serializer);
        registerMessageHandler(NoOpMessage.MSG_CODE, uponNoOpMessage, NoOpMessage.serializer);

        //requests

        registerRequestHandler(StartRequest.REQUEST_ID, uponStartRequest);
        registerRequestHandler(OperationRequest.REQUEST_ID, uponOperationRequest);
        registerRequestHandler(ChangeStates.REQUEST_ID, uponChangeStates);
        registerRequestHandler(ResetMP.REQUEST_ID, uponResetMP);
        //timers
        registerTimerHandler(NewLeaderTimer.TimerCode, uponNewLeaderTimer);
        registerTimerHandler(NoOpTimer.TimerCode, uponNoOpTimer);
        registerTimerHandler(NoMajorityPrepareTimer.TimerCode, uponNoMajorityPrepareTimer);
        registerTimerHandler(NoMajorityAcceptTimer.TimerCode, uponNoMajorityAcceptTimer);
    }

    @Override
    public void init(Properties props) {
        replicas = new HashSet<>();
        acceptAset = new HashSet<>();
        leaderAset = new HashSet<>();
        System.out.println("Multipaxos set");
        noOpTimer = null;
        np =0;
        sequenceNumber=0;
        instanceNumber=0;
        noMajorityPrepareTimer = null;
        noMajorityAcceptTimer = null;
        newLeaderTimer = null;
    }

    //StartRequest
    private final ProtocolRequestHandler uponStartRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest protocolRequest) {
            StartRequest req = (StartRequest) protocolRequest;
            if (req.getMembership() == null) {
                leader = myself;
                replicas = new HashSet<>();
                replicas.add(leader);
            } else {
                Membership membership = req.getMembership();
                leader = membership.getCurrLeader();
                replicas = membership.getReplicas();
                System.out.println("my Leader" + leader.getPort());
                //TODO: temos de ir copiar as cenas de outra replica
            }
            sequenceNumber = replicas.size();
            np = req.getInstancePaxos();

        }
    };


    //Operation deliver
    private final ProtocolRequestHandler uponOperationRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest protocolRequest) {
            OperationRequest opR = (OperationRequest) protocolRequest;
            op = opR.getOp();
            AcceptMessage msg = new AcceptMessage(instanceNumber, np, op);
            System.out.println("Replicas size: " + replicas.size());
            for (Host h : replicas)
                sendMessage(msg, h);

            cancelTimer(noOpTimer);
            noOpTimer = setupTimer(new NoOpTimer(), 40000);
        }
    };


    //Accept Ok


    //Accept
    private final ProtocolMessageHandler uponAcceptMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            if (newLeaderTimer != null)
                cancelTimer(newLeaderTimer);
            newLeaderTimer = setupTimer(new NewLeaderTimer(), 120000);
            AcceptMessage m = (AcceptMessage) protocolMessage;
            int n = m.getNp();
            int inst = m.getInstanceNumber();
            op = m.getOp();
            if (n >= np && inst >= instanceNumber) {
                np = n;
                instanceNumber = inst;
                AcceptOkMessage msg = new AcceptOkMessage(instanceNumber, np, op);
                for (Host h : replicas) {
                    sendMessage(msg, h);
                }
                noMajorityAcceptTimer = setupTimer(new NoMajorityAcceptTimer(), 60000);
            }
        }
    };

    //Accept Ok
    private final ProtocolMessageHandler uponAcceptOKMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            AcceptOkMessage m = (AcceptOkMessage) protocolMessage;
            int inst = m.getInstanceNumber();
            int n = m.getNp();
            op = m.getOp();
            if (inst > instanceNumber) {
                acceptAset.clear();
            }
            if (n >= np && inst >= instanceNumber) {
                np = n;
                instanceNumber = inst;
                acceptAset.add(m.getFrom());
                if (acceptAset.size() >= (replicas.size() / 2) + 1) {
                    OperationDone notification = new OperationDone(op, instanceNumber, np);
                    triggerNotification(notification);
                    instanceNumber++;
                    acceptAset.clear();//Todo:rever isto
                    cancelTimer(noMajorityAcceptTimer);
                }
            }
        }
    };


    private final ProtocolMessageHandler uponPrepareMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            int n = ((PrepareMessage) protocolMessage).getSequenceNumber();

            int instN = ((PrepareMessage) protocolMessage).getInstanceNumber();
            if (n > np) {
                Host leader = protocolMessage.getFrom();
                np = n;
                PrepareOkMessage m = new PrepareOkMessage(n, instN);
                sendMessage(m, leader);
            }
        }
    };
    private final ProtocolMessageHandler uponPrepareOKMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            Host replica = protocolMessage.getFrom();
            leaderAset.add(replica);
            if (leaderAset.size() >= (replicas.size() / 2) + 1) {

                cancelTimer(noMajorityPrepareTimer);
                leaderAset.clear();//TODO: possivelmente isto tem que ser verificado no timer para nao dar porcaria
            }
        }
    };
    private final ProtocolMessageHandler uponNoOpMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            cancelTimer(newLeaderTimer);
            newLeaderTimer = setupTimer(new NewLeaderTimer(), 12000);
        }
    };
    private final ProtocolRequestHandler uponChangeStates = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest protocolRequest) {
            ChangeStates changes = (ChangeStates) protocolRequest;
            leader = changes.getLeader();
            replicas = changes.getReplicas();
            for (Host h : replicas) {
                addNetworkPeer(h);
            }
        }

    };
    private final ProtocolRequestHandler uponResetMP = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest protocolRequest) {
            cancelTimer(newLeaderTimer);
            cancelTimer(noMajorityPrepareTimer);
            cancelTimer(noOpTimer);
            cancelTimer(noMajorityAcceptTimer);
            replicas.clear();
            leader = null;
        }
    };
    private void Prepare() {
        sequenceNumber = sequenceNumber + replicas.size();
        PrepareMessage prepare = new PrepareMessage(instanceNumber, sequenceNumber);
        leaderAset.clear();
        for (Host h : replicas)
            sendMessage(prepare, h);
        noMajorityPrepareTimer = setupTimer(new NoMajorityPrepareTimer(), 120000);

    }

    //TIMERS
    private final ProtocolTimerHandler uponNewLeaderTimer = new ProtocolTimerHandler() {
        @Override
        public void uponTimer(ProtocolTimer protocolTimer) {
            System.out.println("NewLeaderTimer");
            Prepare();
        }
    };
    private final ProtocolTimerHandler uponNoOpTimer = new ProtocolTimerHandler() {
        @Override
        public void uponTimer(ProtocolTimer protocolTimer) {
            System.out.println("NoOpMessage");
            NoOpMessage msg = new NoOpMessage();
            for (Host h : replicas) {
                sendMessage(msg, h);
            }
        }
    };
    private final ProtocolTimerHandler uponNoMajorityPrepareTimer = new ProtocolTimerHandler() {
        @Override
        public void uponTimer(ProtocolTimer protocolTimer) {
            System.out.println("NoMajorityPrepareTimer");
            Prepare();
        }
    };
    private final ProtocolTimerHandler uponNoMajorityAcceptTimer = new ProtocolTimerHandler() {
        @Override
        public void uponTimer(ProtocolTimer protocolTimer) {
            System.out.println("NoMajorityAcceptTimer");
            AcceptOkMessage msg = new AcceptOkMessage(instanceNumber, np, op);
            for (Host h : replicas) {
                sendMessage(msg, h);
            }
            noMajorityAcceptTimer = setupTimer(new NoMajorityAcceptTimer(), 60000);
        }
    };

    @Override
    public void nodeDown(Host host) {
        //TODO: Decidir novo leader ou atraves de Node Down ou através de timer
    }

    @Override
    public void nodeUp(Host host) {

    }

    @Override
    public void nodeConnectionReestablished(Host host) {

    }
}
