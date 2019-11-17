package floodbcast.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AntiEntropyMessage extends ProtocolMessage {
    public final static short MSG_CODE = 302;
    private final Set<UUID> knownMessages;
    private volatile int size = -1;

    public AntiEntropyMessage(Set<UUID> knownMessages) {
        super(AntiEntropyMessage.MSG_CODE);
        this.knownMessages = knownMessages;

    }

    public Set<UUID> getKnownMessages() {
        return knownMessages;
    }

    public static final ISerializer<AntiEntropyMessage> serializer = new ISerializer<AntiEntropyMessage>() {
        @Override
        public void serialize(AntiEntropyMessage m, ByteBuf out) {
            out.writeShort(m.knownMessages.size());
            for (UUID u : m.knownMessages) {
                out.writeLong(u.getMostSignificantBits());
                out.writeLong(u.getLeastSignificantBits());
            }
        }

        @Override
        public AntiEntropyMessage deserialize(ByteBuf in) {
            short size = in.readShort();
            Set<UUID> mids = new HashSet<>();
            for (short i = 0; i < size * 2; i = +2) {
                mids.add(new UUID(in.readLong(), in.readLong()));
            }
            return new AntiEntropyMessage(mids);

        }

        @Override
        public int serializedSize(AntiEntropyMessage m) {
            if (m.size == -1) {
                m.size = 2;
                for (UUID u : m.knownMessages)
                    m.size += Long.BYTES * 2;
            }
            return m.size;
        }
    };
}
