package dht.messages;

import babel.protocol.event.ProtocolMessage;
import dht.Node;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

import java.net.UnknownHostException;

public class FindSuccessorPredecessorMessage extends ProtocolMessage {
    public final static short MSG_CODE = 802;
    Node n;

    public FindSuccessorPredecessorMessage(Node n) {
        super(FindSuccessorPredecessorMessage.MSG_CODE);
        this.n = n;
       // System.out.println(n.getId());

    }
    public Node getN(){return n;}

    public static final ISerializer<FindSuccessorPredecessorMessage> serializer = new ISerializer<FindSuccessorPredecessorMessage>() {
        @Override
        public void serialize(FindSuccessorPredecessorMessage findSuccessorPredecessorMessage, ByteBuf byteBuf) {
            findSuccessorPredecessorMessage.n.serialize(byteBuf);
        }

        @Override
        public FindSuccessorPredecessorMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            Node theN = Node.deserialize(byteBuf);
            return new FindSuccessorPredecessorMessage(theN);
        }

        @Override
        public int serializedSize(FindSuccessorPredecessorMessage findSuccessorPredecessorMessage) {
            return findSuccessorPredecessorMessage.n.serializedSize();
        }
    };
}
