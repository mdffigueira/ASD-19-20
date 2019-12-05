package dht.messages;

import babel.protocol.event.ProtocolMessage;
import utils.Message;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;


public class RouteMessage extends ProtocolMessage {
    public final static short MSG_CODE = 807;
    private int msgId;
    private Message message;

    public RouteMessage(int msgId, Message message) {
        super(RouteMessage.MSG_CODE);
        this.msgId = msgId;
        this.message = message;

    }

    public int getMsgId() {
        return msgId;
    }

    public Message getMessage() {
        return message;
    }

    public static final ISerializer<RouteMessage> serializer = new ISerializer<RouteMessage>() {
        @Override
        public void serialize(RouteMessage routeMessage, ByteBuf byteBuf) {
            byteBuf.writeInt(routeMessage.msgId);
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
