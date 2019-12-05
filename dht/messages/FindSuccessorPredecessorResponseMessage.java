package dht.messages;

import babel.protocol.event.ProtocolMessage;
import utils.Node;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;

public class FindSuccessorPredecessorResponseMessage extends ProtocolMessage {
public final static short MSG_CODE = 803;
Node n ;
public FindSuccessorPredecessorResponseMessage(Node n){
    super(FindSuccessorPredecessorResponseMessage.MSG_CODE);
    this.n=n;
}
public Node getN(){return n;}

public static final ISerializer<FindSuccessorPredecessorResponseMessage> serializer = new ISerializer<FindSuccessorPredecessorResponseMessage>() {
    @Override
    public void serialize(FindSuccessorPredecessorResponseMessage findSuccessorPredecessorResponseMessage, ByteBuf byteBuf) {
        findSuccessorPredecessorResponseMessage.n.serialize(byteBuf);
    }

    @Override
    public FindSuccessorPredecessorResponseMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
       Node theN = Node.deserialize(byteBuf);

        return new FindSuccessorPredecessorResponseMessage(theN);
    }

    @Override
    public int serializedSize(FindSuccessorPredecessorResponseMessage findSuccessorPredecessorResponseMessage) {
        return findSuccessorPredecessorResponseMessage.n.serializedSize();
    }
};
}
