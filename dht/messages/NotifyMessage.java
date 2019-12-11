package dht.messages;

import babel.protocol.event.ProtocolMessage;
import utils.Node;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;

public class NotifyMessage extends ProtocolMessage {
    public final static short MSG_CODE = 105;
    Node n;

    public NotifyMessage(Node n) {
        super(NotifyMessage.MSG_CODE);
        this.n = n;
    }

    public Node getN() {
        return n;
    }

    public static final ISerializer<NotifyMessage> serializer = new ISerializer<NotifyMessage>() {
        @Override
        public void serialize(NotifyMessage notifyMessage, ByteBuf byteBuf) {
            notifyMessage.n.serialize(byteBuf);
        }

        @Override
        public NotifyMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            Node theN = Node.deserialize(byteBuf);
            return new NotifyMessage(theN);
        }

        @Override
        public int serializedSize(NotifyMessage notifyMessage) {
            return notifyMessage.n.serializedSize();
        }
    };

}
