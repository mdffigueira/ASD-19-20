package dht.messages;

import babel.protocol.event.ProtocolMessage;
import utils.Node;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;

public class FindSuccessorMessage extends ProtocolMessage {

    public final static short MSG_CODE = 801;

    Node n;
    int next;
    private volatile int size = -1;

    public FindSuccessorMessage(Node n, int next) {
        super(FindSuccessorMessage.MSG_CODE);
        this.n = n;
        this.next = next;
    }

    public Node getN() {
        return n;
    }

    public int getNext(){
        return next;
    }

    public static final ISerializer<FindSuccessorMessage> serializer = new ISerializer<FindSuccessorMessage>() {
        @Override
        public void serialize(FindSuccessorMessage findSuccessorMessage, ByteBuf byteBuf) {
            findSuccessorMessage.n.serialize(byteBuf);
            byteBuf.writeInt(findSuccessorMessage.next);
        }

        @Override
        public FindSuccessorMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            Node theN = Node.deserialize(byteBuf);
            return new FindSuccessorMessage(theN,byteBuf.readInt());
        }

        @Override
        public int serializedSize(FindSuccessorMessage findSuccessorMessage) {
            return findSuccessorMessage.n.serializedSize()+Integer.BYTES;
        }
    };
}
