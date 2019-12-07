package multipaxos;

import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolMessageHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.protocol.GenericProtocol;
import babel.protocol.event.ProtocolMessage;
import babel.requestreply.ProtocolRequest;
import multipaxos.messages.AcceptMessage;
import multipaxos.messages.PrepareMessage;
import network.Host;
import network.INetwork;
import network.INodeListener;
import publishsubscribe.requests.OperationRequest;
import publishsubscribe.requests.StartRequest;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class MultiPaxos extends GenericProtocol implements INodeListener {
    public final static short PROTOCOL_ID = 200;
    public final static int MAXREPLICAS=3;
    private int seqNumber;
    private int maxSeqNumber;
    private int np = 0;// numero de instancia do paxos
    Set<Host> replicas;
    Host leader;

    public MultiPaxos(INetwork net) throws HandlerRegistrationException {
        super("MultiPaxos", PROTOCOL_ID, net);
        //messages
registerMessageHandler(PrepareMessage.MSG_CODE,uponPrepareMessage,PrepareMessage.serializer);
        //requests
        registerRequestHandler(StartRequest.REQUEST_ID, uponStartRequest);
        registerRequestHandler(OperationRequest.REQUEST_ID, uponOperationRequest);

    }

    @Override
    public void init(Properties props) {
        replicas = new HashSet<>();
    }

    private final ProtocolRequestHandler uponStartRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest protocolRequest) {
            StartRequest req = (StartRequest) protocolRequest;
            if (req.getMembership() == null) {
                leader=myself;
                replicas=new HashSet<>();
                replicas.add(leader);
                np=req.getInstancePaxos();
            }
            else{

            }


        }
    };

    //LEADER ELECTION


    private final ProtocolRequestHandler uponOperationRequest = new ProtocolRequestHandler() {
        @Override
        public void uponRequest(ProtocolRequest protocolRequest) {
            //int psn = (OperationRequest) protocolRequest;
            int n = np + 1;
            AcceptMessage msg = new AcceptMessage();
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
