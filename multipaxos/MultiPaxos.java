package multipaxos;

import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolMessageHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.protocol.GenericProtocol;
import babel.protocol.event.ProtocolMessage;
import babel.requestreply.ProtocolRequest;
import multipaxos.Notification.OperationDone;
import multipaxos.messages.AcceptMessage;
import multipaxos.messages.AcceptOkMessage;
import multipaxos.messages.PrepareMessage;
import network.Host;
import network.INetwork;
import network.INodeListener;
import publishsubscribe.requests.StartRequest;
import utils.Operation;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class MultiPaxos extends GenericProtocol implements INodeListener {
    public final static short PROTOCOL_ID = 200;
    public final static int MAXREPLICAS = 3;
    private int seqNumber;
    private int maxSeqNumber;
    private int np = 0;// numero de instancia do paxos
    private int na; //TODO: Não perceo o que é este na?
    private Operation va; //TODO: Highest accept? wtf
    Set<Host> replicas;
    Set<Host> aset;
    Host leader;

    public MultiPaxos(INetwork net) throws HandlerRegistrationException {
        super("MultiPaxos", PROTOCOL_ID, net);
//Notification Produced
        registerNotification(OperationDone.NOTIFICATION_ID, OperationDone.NOTIFICATION_NAME);

        //messages
        registerMessageHandler(PrepareMessage.MSG_CODE, uponPrepareMessage, PrepareMessage.serializer);
        registerMessageHandler(AcceptMessage.MSG_CODE, uponAcceptMessage, AcceptMessage.serializer);
        registerMessageHandler(AcceptOkMessage.MSG_CODE, uponAcceptOKMessage, AcceptOkMessage.serializer);
        //requests
        registerRequestHandler(StartRequest.REQUEST_ID, uponStartRequest);
        //   registerRequestHandler(OperationRequest.REQUEST_ID, uponOperationRequest);

    }

    @Override
    public void init(Properties props) {
        replicas = new HashSet<>();
        aset = new HashSet<>();
    }

    private final ProtocolRequestHandler uponStartRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest protocolRequest) {
            StartRequest req = (StartRequest) protocolRequest;
            if (req.getMembership() == null) {
                leader = myself;
                replicas = new HashSet<>();
                replicas.add(leader);
                np = req.getInstancePaxos();
            } else {

            }


        }
    };

    //LEADER ELECTION


    private final ProtocolRequestHandler uponOperationRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest protocolRequest) {
            //int psn = (OperationRequest) protocolRequest;
            int n = np + 1;
            //  OperationRequest r =protocolRequest(Operat)
            //--->> AcceptMessage msg = new AcceptMessage(n,op);
        }
    };
    private final ProtocolMessageHandler uponPrepareMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
//            int n = ((PrepareMessage) protocolMessage).getPsn();
//            if(n>np){
//                np = n;
//                PrepareOkMessage m = new PrepareOkMessage(np,null)
//            }

        }
    };

    private final ProtocolMessageHandler uponAcceptMessage = new ProtocolMessageHandler() {
        @Override
        public void receive(ProtocolMessage protocolMessage) {
            AcceptMessage m = (AcceptMessage) protocolMessage;
            int n = m.getN();
            Operation v = m.getOp();
            if (n > np) {
                na = n;
                // va = v; TODO: No algoritmo diz para guardar, mas acho que não é preciso na nossa implementação
                AcceptOkMessage msg = new AcceptOkMessage(na, va);
                for (Host h : replicas) {
                    if (h != myself)
                        sendMessage(msg, h);
                }
            }
        }
    };
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
                if (aset.size() >= (replicas.size() / 2)) {
                    if (leader == myself) {
                        OperationDone notification = new OperationDone(va);
                        triggerNotification(notification);
                        aset.clear();
                    }
                }
            }
        }
    };

    @Override
    public void nodeDown(Host host) {

    }

    @Override
    public void nodeUp(Host host) {

    }

    @Override
    public void nodeConnectionReestablished(Host host) {

    }
}
