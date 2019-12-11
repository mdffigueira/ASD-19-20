package publishsubscribe.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.Set;

public class GetCurrMembshipMessage extends ProtocolMessage {
    public final static short MSG_CODE = 503;
    //private final Host node;
    public GetCurrMembshipMessage() {
        super(GetCurrMembshipMessage.MSG_CODE);
        // this.node=node;

    }

    public static final ISerializer<GetCurrMembshipMessage> serializer = new ISerializer<GetCurrMembshipMessage>() {
        @Override
        public void serialize(GetCurrMembshipMessage getCurrMembshipMessage, ByteBuf byteBuf) {

        }

        @Override
        public GetCurrMembshipMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            return null;
        }

        @Override
        public int serializedSize(GetCurrMembshipMessage getCurrMembshipMessage) {
            return 0;
        }
    };
}
