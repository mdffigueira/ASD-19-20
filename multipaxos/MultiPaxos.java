package multipaxos;

import babel.exceptions.HandlerRegistrationException;
import babel.handlers.ProtocolMessageHandler;
import babel.handlers.ProtocolRequestHandler;
import babel.protocol.GenericProtocol;
import babel.protocol.event.ProtocolMessage;
import babel.requestreply.ProtocolRequest;
import multipaxos.messages.AcceptMessage;
import multipaxos.messages.PrepareMessage;
import multipaxos.messages.PrepareOkMessage;
import network.Host;
import network.INetwork;
import network.INodeListener;
import org.apache.logging.log4j.core.net.Protocol;
import publishsubscribe.requests.OperationRequest;
import publishsubscribe.requests.StartRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class MultiPaxos extends GenericProtocol implements INodeListener {
    public final static short PROTOCOL_ID = 200;
    private int Membsize;
    private int np = 0;
    Set<Host> replicas;

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

            }


        }
    };
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
