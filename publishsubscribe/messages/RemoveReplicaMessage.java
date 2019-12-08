package publishsubscribe.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;
import java.net.UnknownHostException;

public class RemoveReplicaMessage extends ProtocolMessage {
    public static final short MSG_CODE = 506;
    private Host h;

    public RemoveReplicaMessage(Host h) {
        super(RemoveReplicaMessage.MSG_CODE);
        this.h = h;
    }

    public Host getH() {
        return h;
    }

    public static final ISerializer<RemoveReplicaMessage> serializer = new ISerializer<RemoveReplicaMessage>() {
        @Override
        public void serialize(RemoveReplicaMessage addReplicaMessage, ByteBuf byteBuf) {
            addReplicaMessage.h.serialize(byteBuf);
        }

        @Override
        public RemoveReplicaMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            return new RemoveReplicaMessage(Host.deserialize(byteBuf));
        }

        @Override
        public int serializedSize(RemoveReplicaMessage addReplicaMessage) {
            return addReplicaMessage.h.serializedSize();
        }
    };

}
