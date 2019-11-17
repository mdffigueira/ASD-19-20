package dht.messages;

import babel.protocol.event.ProtocolMessage;
import dht.Node;
import dissemination.Message;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;


public class RouteMessage extends ProtocolMessage {
    public final static short MSG_CODE = 807;
    Node nId;
    Message message;

    public RouteMessage(Node nId, Message message) {
        super(RouteMessage.MSG_CODE);
        this.nId = nId;
        this.message = message;

    }

    public Node getNId() {
        return nId;
    }

    public Message getMessage() {
        return message;
    }

    public static final ISerializer<RouteMessage> serializer = new ISerializer<RouteMessage>() {
        @Override
        public void serialize(RouteMessage routeMessage, ByteBuf byteBuf) {
            routeMessage.nId.serialize(byteBuf);
            routeMessage.message.serialize(byteBuf);
        }

        @Override
        public RouteMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            Node n = Node.deserialize(byteBuf);
            Message msg = Message.deserialize(byteBuf);
            return new RouteMessage(n, msg);
        }

        @Override
        public int serializedSize(RouteMessage routeMessage) {
            return routeMessage.getNId().serializedSize() + routeMessage.getMessage().serializedSize();
        }
    };

}
