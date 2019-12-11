package multipaxos.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

import java.net.UnknownHostException;

public class NoOpMessage extends ProtocolMessage {
    public static final short MSG_CODE = 203;

    public NoOpMessage() {
        super(NoOpMessage.MSG_CODE);
    }

    public static final ISerializer<NoOpMessage> serializer = new ISerializer<NoOpMessage>() {
        @Override
        public void serialize(NoOpMessage m, ByteBuf byteBuf) {

        }

        @Override
        public NoOpMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {

            return new NoOpMessage();
        }

        @Override
        public int serializedSize(NoOpMessage m) {
            return 0;
        }
    };
}
