package publishsubscribe.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class ReturnCurrMembshipMessage extends ProtocolMessage {
    public final static short MSG_CODE = 503;
    private Set<Host> membership;
    private volatile int size = -1;

    public ReturnCurrMembshipMessage(Set<Host> membership) {
        super(ReturnCurrMembshipMessage.MSG_CODE);
        this.membership = membership;
    }

    public static final ISerializer<ReturnCurrMembshipMessage> serializer = new ISerializer<ReturnCurrMembshipMessage>() {
        @Override
        public void serialize(ReturnCurrMembshipMessage m, ByteBuf byteBuf) {
            byteBuf.writeShort(m.membership.size());
            for (Host h : m.membership) {
                h.serialize(byteBuf);
            }
        }

        @Override
        public ReturnCurrMembshipMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            short size = byteBuf.readShort();
            Set<Host> membership = new HashSet<>();
            for (short i = 0; i < size; i++) {
                membership.add(Host.deserialize(byteBuf));
            }
            return new ReturnCurrMembshipMessage(membership);
        }

        @Override
        public int serializedSize(ReturnCurrMembshipMessage m) {
            if (m.size == -1) {
                m.size = 2;
                for (Host h : m.membership) {
                    m.size += h.serializedSize();
                }
            }
            return m.size;
        }
    };
}
