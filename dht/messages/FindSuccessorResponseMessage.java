package dht.messages;

import babel.protocol.event.ProtocolMessage;
import utils.Node;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;

public class FindSuccessorResponseMessage extends ProtocolMessage {
    public final static short MSG_CODE = 804;

    Node n;
    int next;

    public FindSuccessorResponseMessage(Node n, int next) {
        super(FindSuccessorResponseMessage.MSG_CODE);
        this.n = n;
        this.next = next;
    }

    public Node getN() {
        return n;
    }
    public int getNext(){ return next;}

    public static final ISerializer<FindSuccessorResponseMessage> serializer = new ISerializer<FindSuccessorResponseMessage>() {
        @Override
        public void serialize(FindSuccessorResponseMessage findSuccessorResponseMessage, ByteBuf byteBuf) {
            findSuccessorResponseMessage.n.serialize(byteBuf);
            byteBuf.writeInt(findSuccessorResponseMessage.next);
        }

        @Override
        public FindSuccessorResponseMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            Node theN = Node.deserialize(byteBuf);
            return new FindSuccessorResponseMessage(theN, byteBuf.readInt());
        }

        @Override
        public int serializedSize(FindSuccessorResponseMessage findSuccessorResponseMessage) {
            return findSuccessorResponseMessage.n.serializedSize()+Integer.BYTES;
        }
    };
}
