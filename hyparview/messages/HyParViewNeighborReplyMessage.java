package hyparview.messages;


import java.net.UnknownHostException;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;


public class HyParViewNeighborReplyMessage extends ProtocolMessage {
	public static final short MSG_CODE = 407;
	private Host myself;
	private final int answer;
	
	public HyParViewNeighborReplyMessage(Host myself,int answer) {
		super(HyParViewNeighborReplyMessage.MSG_CODE);
		this.myself=myself;
		this.answer=answer;
		
	}
	public int getAnswer() {
		return answer;
	}
	public Host getIdentifier() {
		return myself;
	}
	public static final ISerializer<HyParViewNeighborReplyMessage> serializer = new ISerializer<HyParViewNeighborReplyMessage>() {

		@Override
		public void serialize(HyParViewNeighborReplyMessage hyp, ByteBuf out) {
			// TODO Auto-generated method stub
			hyp.myself.serialize(out);
			out.writeInt(hyp.answer);
		}
		
		@Override
		public HyParViewNeighborReplyMessage deserialize(ByteBuf in) throws UnknownHostException {
			// TODO Auto-generated method stub
			Host node = Host.deserialize(in);
			int a = in.readInt();
			return new HyParViewNeighborReplyMessage(node,a);
		}

		@Override
		public int serializedSize(HyParViewNeighborReplyMessage m) {
			// TODO Auto-generated method stub
            return m.myself.serializedSize()+Integer.BYTES;
		}
	
	};
}
