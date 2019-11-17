package floodbcast.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.UUID;

public class FloodBCastProtocolMessage extends ProtocolMessage {

    public final static short MSG_CODE = 301;

    private final UUID mid;
    private byte[] payload;
	private byte[] topic;
    private Host owner;
    public FloodBCastProtocolMessage(Host owner,byte[] topic,byte[] payload) {
        super(FloodBCastProtocolMessage.MSG_CODE);
        this.owner=owner;
        this.mid = UUID.randomUUID();
        this.payload = payload;
        this.topic = topic;


    }

    public FloodBCastProtocolMessage(Host owner,UUID mid, byte[] payload, byte[] topic) {
        super(FloodBCastProtocolMessage.MSG_CODE);
        this.owner=owner;
        this.mid = mid;
        this.payload = payload;
        this.topic = topic;
    }

//    public void setPayload(byte[] payload) {
//        this.payload = new byte[payload.length];
//        System.arraycopy(payload, 0, this.payload, 0, payload.length);
//    }
public Host getOwner(){return owner;}
    public int getLength() {
        return this.payload.length;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public UUID getMessageId() {
        return mid;
    }

    public static final ISerializer<FloodBCastProtocolMessage> serializer = new ISerializer<FloodBCastProtocolMessage>() {
        @Override
        public void serialize(FloodBCastProtocolMessage m, ByteBuf out) {
            m.owner.serialize(out);
            out.writeLong(m.mid.getMostSignificantBits());
            out.writeLong(m.mid.getLeastSignificantBits());
            out.writeInt(m.payload.length);
            out.writeBytes(m.payload);
            out.writeInt(m.topic.length);
            out.writeBytes(m.topic);
        }

        @Override
        public FloodBCastProtocolMessage deserialize(ByteBuf in) throws UnknownHostException {
            Host own= Host.deserialize(in);
            UUID mid = new UUID(in.readLong(), in.readLong());
            int size = in.readInt();
            byte[] payload = new byte[size];
            in.readBytes(payload);
            byte[] topic = new byte[in.readInt()];
            in.readBytes(topic);
            return new FloodBCastProtocolMessage(own,mid, payload, topic);
        }

        @Override
        public int serializedSize(FloodBCastProtocolMessage m) {
            return m.owner.serializedSize()+(2*Long.BYTES) + Integer.BYTES + m.payload.length + Integer.BYTES + m.topic.length;
        }
    };


	public byte[] getTopic() {
		return topic;
	}
}
