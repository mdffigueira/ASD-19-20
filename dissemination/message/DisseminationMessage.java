package dissemination.message;

import babel.protocol.event.ProtocolMessage;
import dht.Node;
import dissemination.Message;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.TreeSet;
import java.util.UUID;

public class DisseminationMessage extends ProtocolMessage {

	public final static short MSG_CODE = 908;

	private Message payload;
	private byte[] topic;
	
	public DisseminationMessage(byte[] topic,Message msg) {
		super(DisseminationMessage.MSG_CODE);
		this.payload = msg;
		this.topic = topic;


	}
	
	public Message getPayload() {
		return this.payload;
	}
	public static final ISerializer<DisseminationMessage> serializer = new ISerializer<DisseminationMessage>() {
		@Override
		public void serialize(DisseminationMessage m, ByteBuf out) {
			m.payload.serialize(out);
			out.writeInt(m.topic.length);
			out.writeBytes(m.topic);
		}

		@Override
		public DisseminationMessage deserialize(ByteBuf in) throws UnknownHostException {
			Message thisMsg = Message.deserialize(in);
			byte[] topic = new byte[in.readInt()];
			in.readBytes(topic);
			return new DisseminationMessage( topic, thisMsg);
		}

		@Override
		public int serializedSize(DisseminationMessage m) {
			return m.payload.serializedSize() + Integer.BYTES + m.topic.length;
		}
	};


	public byte[] getTopic() {
		return topic;
	}
}
