package multipaxos;

import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolMessageHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.handlers.ProtocolTimerHandler;
import babel.protocol.GenericProtocol;
import babel.protocol.event.ProtocolMessage;
import babel.requestreply.ProtocolRequest;
import babel.timer.ProtocolTimer;
import multipaxos.notifications.OperationDone;
import multipaxos.messages.AcceptMessage;
import multipaxos.messages.AcceptOkMessage;
import multipaxos.messages.PrepareMessage;
import multipaxos.messages.PrepareOkMessage;
import multipaxos.timers.NoMajorityTimer;
import multipaxos.timers.NoOpTimer;
import network.Host;
import network.INetwork;
import network.INodeListener;
import publishsubscribe.requests.OperationRequest;
import publishsubscribe.requests.StartRequest;
import utils.Membership;
import utils.Operation;

import java.util.*;

public class MultiPaxos extends GenericProtocol implements INodeListener {
    public final static short PROTOCOL_ID = 200;
    public final static int MAXREPLICAS = 3;

    private int np = 0;// (highest prepare) nº do lider
    private int sequenceNumber;//n=>psn
    private int instanceNumber;//(nºo de instanceNumberpaxos)
    private int na;
    private Operation va;
    UUID opTimer;
    UUID majorityTimer;
    Set<Host> replicas;
    Set<Host> aset;
    Set<Host> leaderAset;
    LinkedList<AcceptMessage> pending;
    Host leader;

    public MultiPaxos(INetwork net) throws HandlerRegistrationException {
        super("MultiPaxos", PROTOCOL_ID, net);

        //Notification Produced
        registerNotification(OperationDone.NOTIFICATION_ID, OperationDone.NOTIFICATION_NAME);

        //messages
        registerMessageHandler(PrepareMessage.MSG_CODE, uponPrepareMessage, PrepareMessage.serializer);
        registerMessageHandler(PrepareOkMessage.MSG_CODE, uponPrepareOKMessage, PrepareOkMessage.serializer);
        registerMessageHandler(AcceptMessage.MSG_CODE, uponAcceptMessage, AcceptMessage.serializer);
        registerMessageHandler(AcceptOkMessage.MSG_CODE, uponAcceptOKMessage, AcceptOkMessage.serializer);


        //requests
        registerRequestHandler(StartRequest.REQUEST_ID, uponStartRequest);
        registerRequestHandler(OperationRequest.REQUEST_ID, uponOperationRequest);

        //timers
        registerTimerHandler(NoOpTimer.TimerCode, uponNoOpTimer);
        registerTimerHandler(NoMajorityTimer.TimerCode, uponNoMajorityTimer);
    }

    @Override
    public void init(Properties props) {
        replicas = new HashSet<>();
        aset = new HashSet<>();
        pending = new LinkedList<AcceptMessage>();
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
                sequenceNumber = replicas.size();
                np = req.getInstancePaxos();
            } else {
                Membership membership = req.getMembership();
                leader = membership.getCurrLeader();
                replicas = membership.getReplicas();
                sequenceNumber = replicas.size();
                np = req.getInstancePaxos();
                //TODO: temos de ir copiar as cenas de outra replica
            }

        }
    };


    //Operation deliver
    private final ProtocolRequestHandler uponOperationRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest protocolRequest) {
            OperationRequest op = (OperationRequest) protocolRequest;
            //TODO: Not sure if right
            sequenceNumber = sequenceNumber * replicas.size();
            AcceptMessage msg = new AcceptMessage(na, op.getOp(), sequenceNumber);
            pending.add(msg);
            if (opTimer != null)
                cancelTimer(opTimer);
            opTimer = setupTimer(new NoOpTimer(), 120000);

            if (pending.size() == 1) {
                for (Host h : replicas)
                    sendMessage(msg, h);
            }
            //int psn = (OperationRequest) protocolRequest;

            //int n = np + 1;
            //  OperationRequest r =protocolRequest(Operat)
            //--->> AcceptMessage msg = new AcceptMessage(n,op);
        }
    };


    //Accept Ok


    //Accept
    private final ProtocolMessageHandler uponAcceptMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            AcceptMessage m = (AcceptMessage) protocolMessage;
            int n = m.getN();
            Operation v = m.getOp();
            if (n > np) {
                na = n;
                va = v;
                AcceptOkMessage msg = new AcceptOkMessage(na, va);
                for (Host h : replicas) {
                    if (h != myself)
                        sendMessage(msg, h);
                }
            }
        }
    };

    //Accept Ok
    private final ProtocolMessageHandler uponAcceptOKMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            AcceptOkMessage m = (AcceptOkMessage) protocolMessage;
            int n = m.getN();
            Operation v = m.getOp();
            if (!(n < na)) {
                if (n > na) {
                    na = n;
                    va = v;
                    aset.clear();
                }
                aset.add(m.getFrom());
                if (aset.size() >= (replicas.size() / 2) + 1) {
                    if (leader == myself) {
                        OperationDone notification = new OperationDone(va);
                        triggerNotification(notification);
                        aset.clear();
                        pending.poll();
                        for (Host h : replicas)
                            sendMessage(pending.getFirst(), h);
                    }
                }
            }
        }
    };
    private final ProtocolTimerHandler uponNoOpTimer = new ProtocolTimerHandler() {
        @Override
        public void uponTimer(ProtocolTimer protocolTimer) {
            Prepare();
        }
    };
    private final ProtocolTimerHandler uponNoMajorityTimer = new ProtocolTimerHandler() {
        @Override
        public void uponTimer(ProtocolTimer protocolTimer) {
            Prepare();
        }
    };

    public void Prepare() {
        sequenceNumber = sequenceNumber + replicas.size();
        PrepareMessage prepare = new PrepareMessage(instanceNumber, sequenceNumber);
		leaderAset.clear();
        for (Host h : replicas)
            sendMessage(prepare, h);
        majorityTimer = setupTimer(new NoMajorityTimer(), 120000);

    }

    private final ProtocolMessageHandler uponPrepareMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            int n = ((PrepareMessage) protocolMessage).getSequenceNumber();
            Host leader = protocolMessage.getFrom();
            int instN = ((PrepareMessage) protocolMessage).getInstanceNumber();
            if (n > np) {
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

                cancelTimer(majorityTimer);
                leaderAset.clear();//TODO: possivelmente isto tem que ser verificado no timer para nao dar merda( muito probavelente é pra cagar)
            }
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
