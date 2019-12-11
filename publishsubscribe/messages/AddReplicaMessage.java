package publishsubscribe.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;
import java.net.UnknownHostException;

public class AddReplicaMessage extends ProtocolMessage {
    public static final short MSG_CODE = 501;
    private Host h;

    public AddReplicaMessage(Host h) {
        super(AddReplicaMessage.MSG_CODE);
        this.h = h;
    }

    public Host getH() {
        return h;
    }

    public static final ISerializer<AddReplicaMessage> serializer = new ISerializer<AddReplicaMessage>() {
        @Override
        public void serialize(AddReplicaMessage addReplicaMessage, ByteBuf byteBuf) {
            addReplicaMessage.h.serialize(byteBuf);
        }

        @Override
        public AddReplicaMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            return new AddReplicaMessage(Host.deserialize(byteBuf));
        }

        @Override
        public int serializedSize(AddReplicaMessage addReplicaMessage) {
            return addReplicaMessage.h.serializedSize();
        }
    };

}
