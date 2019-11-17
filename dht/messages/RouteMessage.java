package dht.messages;

import babel.protocol.event.ProtocolMessage;
import dht.Node;
import dissemination.Message;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;


public class RouteMessage extends ProtocolMessage {
    public final static short MSG_CODE = 807;
    int nId;
    Message message;

    public RouteMessage(int nId, Message message) {
        super(RouteMessage.MSG_CODE);
        this.nId = nId;
        this.message = message;

    }

    public int getNId() {
        return nId;
    }

    public Message getMessage() {
        return message;
    }

    public static final ISerializer<RouteMessage> serializer = new ISerializer<RouteMessage>() {
        @Override
        public void serialize(RouteMessage routeMessage, ByteBuf byteBuf) {
            byteBuf.writeInt(routeMessage.nId);
            routeMessage.message.serialize(byteBuf);
        }

        @Override
        public RouteMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            int n = byteBuf.readInt();
            Message msg = Message.deserialize(byteBuf);
            return new RouteMessage(n, msg);
        }

        @Override
        public int serializedSize(RouteMessage routeMessage) {
            return Integer.BYTES + routeMessage.getMessage().serializedSize();
        }
    };

}
