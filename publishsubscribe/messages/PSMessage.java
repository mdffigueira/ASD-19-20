package publishsubscribe.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.UUID;

public class PSMessage extends ProtocolMessage {

    public final static short MSG_CODE = 502;

    private final UUID mid;
    private byte[] payload;
    private String topic;


    public PSMessage() {
        super(PSMessage.MSG_CODE);
        this.mid = UUID.randomUUID();
        this.payload = new byte[0];

    }

    public PSMessage(UUID mid, byte[] payload) {
        super(PSMessage.MSG_CODE);
        this.mid = mid;
        this.payload = payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = new byte[payload.length];
        System.arraycopy(payload, 0, this.payload, 0, payload.length);
    }

    public int getLength() {
        return this.payload.length;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public UUID getMessageId() {
        return mid;
    }

    public static final ISerializer<PSMessage> serializer = new ISerializer<PSMessage>() {
        @Override
        public void serialize(PSMessage m, ByteBuf out) {
            out.writeLong(m.mid.getMostSignificantBits());
            out.writeLong(m.mid.getLeastSignificantBits());
            out.writeInt(m.payload.length);
            out.writeBytes(m.payload);
        }

        @Override
        public PSMessage deserialize(ByteBuf in) throws UnknownHostException {
            UUID mid = new UUID(in.readLong(), in.readLong());
            int size = in.readInt();
            byte[] payload = new byte[size];
            in.readBytes(payload);
            return new PSMessage(mid, payload);
        }

        @Override
        public int serializedSize(PSMessage m) {
            return (2*Long.BYTES) + Integer.BYTES + m.payload.length;
        }
    };


	public void setTopic(String topic) {
		this.topic = topic;
	}
}
