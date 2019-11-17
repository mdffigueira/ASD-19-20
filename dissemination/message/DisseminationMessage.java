package dissemination.message;

import babel.protocol.event.ProtocolMessage;
import dht.Node;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.TreeSet;
import java.util.UUID;

public class DisseminationMessage extends ProtocolMessage {

	public final static short MSG_CODE = 908;

	private byte[] payload;
	private byte[] topic;
	private TreeSet<Integer> nodes;
	
	public DisseminationMessage(byte[] topic,byte[] payload, TreeSet<Integer> nodes) {
		super(DisseminationMessage.MSG_CODE);
		this.nodes=nodes;
		this.payload = payload;
		this.topic = topic;


	}
	public TreeSet<Integer> getOwners(){
		return nodes;
	}

	public int getLength() {
		return this.payload.length;
	}

	public byte[] getPayload() {
		return this.payload;
	}
	public static final ISerializer<DisseminationMessage> serializer = new ISerializer<DisseminationMessage>() {
		@Override
		public void serialize(DisseminationMessage m, ByteBuf out) {
			out.writeInt(m.nodes.size());
			for(Integer n: m.nodes) {
				out.writeInt(n);
			}
			out.writeInt(m.payload.length);
			out.writeBytes(m.payload);
			out.writeInt(m.topic.length);
			out.writeBytes(m.topic);
		}

		@Override
		public DisseminationMessage deserialize(ByteBuf in) throws UnknownHostException {
			int nSize = in.readInt();
			TreeSet<Integer> owners = new TreeSet<Integer>();
			for(int i = 0; i < nSize; i++)
				owners.add(in.readInt());
			int size = in.readInt();
			byte[] payload = new byte[size];
			in.readBytes(payload);
			byte[] topic = new byte[in.readInt()];
			in.readBytes(topic);
			return new DisseminationMessage(payload, topic, owners);
		}

		@Override
		public int serializedSize(DisseminationMessage m) {
			return Integer.BYTES + m.nodes.size()*Integer.BYTES + Integer.BYTES + m.payload.length + Integer.BYTES + m.topic.length;
		}
	};


	public byte[] getTopic() {
		return topic;
	}
}
