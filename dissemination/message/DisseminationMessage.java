package dissemination.message;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.UUID;

public class DisseminationMessage {

    public final static short MSG_CODE = 906;

    private final UUID mid;
    private byte[] message;
    private String topic;
    private int typeM;

    public DisseminationMessage(byte[] message, int typeM, String topic2) {
        this.mid = UUID.randomUUID();
        this.message = message;
        this.typeM = typeM;
        this.topic = topic2;
    }

    public int getLength() {
        return this.message.length + this.topic.length() + typeM;
    }


    public UUID getMessageId() {
        return mid;
    }

    public static final ISerializer<DisseminationMessage> serializer = new ISerializer<DisseminationMessage>() {
        @Override
        public void serialize(DisseminationMessage m, ByteBuf out) {
            out.writeLong(m.mid.getMostSignificantBits());
            out.writeLong(m.mid.getLeastSignificantBits());
            out.writeInt(m.message.length);
            out.writeBytes(m.message);
            out.writeInt(m.typeM);
            out.writeInt(m.topic.length);
            out.writeBytes(m.topic);
        }

        @Override
        public DisseminationMessage deserialize(ByteBuf in) throws UnknownHostException {
            UUID mid = new UUID(in.readLong(), in.readLong());
            int size = in.readInt();
            byte[] payload = new byte[size];
            in.readBytes(payload);
            int typeM = in.readInt();
            byte[] topic = new byte[in.readInt()];
            in.readBytes(topic);
            return new DisseminationMessage(payload, typeM, topic);
        }

        @Override
        public int serializedSize(DisseminationMessage m) {
            return (2*Long.BYTES) + Integer.BYTES + m.message.length + Integer.BYTES + Integer.BYTES 
            		+ m.topic.length;
        }
    };


	public void setTopic(byte[] topic) {
		this.topic = topic;
	}
}
