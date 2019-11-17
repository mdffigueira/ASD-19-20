package hyparview.messages;

import java.net.UnknownHostException;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

public class HyParViewNeighborMessage extends ProtocolMessage  {
	private  int priorityLevel;
	private Host myself;
	public final static short MSG_CODE = 402;
	public HyParViewNeighborMessage( Host myself,int priorityLevel) {
		super(HyParViewNeighborMessage.MSG_CODE);
		this.priorityLevel= priorityLevel;
		this.myself=myself;
	}
	
	public int getPriorityLevel() {
		return priorityLevel;
	}
	public Host getIdentifier() {
		return myself;
	}
	public static final ISerializer<HyParViewNeighborMessage> serializer = new ISerializer<HyParViewNeighborMessage>() {
		@Override
		public void serialize(HyParViewNeighborMessage hyp, ByteBuf out) {
			hyp.myself.serialize(out);
			out.writeInt(hyp.priorityLevel);
			//falta a String
		}

		@Override
		public HyParViewNeighborMessage deserialize(ByteBuf in) throws UnknownHostException {
			
			Host ident = Host.deserialize(in);
			int priority = in.readInt();
			return new HyParViewNeighborMessage(ident,priority);
		}

		@Override
		public int serializedSize(HyParViewNeighborMessage m) {
            return m.myself.serializedSize()+Integer.BYTES;
		}
	};
}
