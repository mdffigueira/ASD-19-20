package hyparview.messages;

import java.net.UnknownHostException;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

public class HyParViewFowardJoinFinishMessage extends ProtocolMessage {
	public static final short MSG_CODE = 409;
	private Host myself;
	public HyParViewFowardJoinFinishMessage(Host myself) {
		super(MSG_CODE);
		this.myself=myself;
		// TODO Auto-generated constructor stub
	}
	public Host getNode() {
		return myself;
	}
	public static final ISerializer<HyParViewFowardJoinFinishMessage> serializer = new ISerializer<HyParViewFowardJoinFinishMessage>() {

		@Override
		public void serialize(HyParViewFowardJoinFinishMessage hyp, ByteBuf out) {
			// TODO Auto-generated method stub
			hyp.myself.serialize(out);
		}
		@Override
		public HyParViewFowardJoinFinishMessage deserialize(ByteBuf in) throws UnknownHostException {
			// TODO Auto-generated method stub
			Host node = Host.deserialize(in);
			return new HyParViewFowardJoinFinishMessage(node);
		}

		

		@Override
		public int serializedSize(HyParViewFowardJoinFinishMessage m) {
			// TODO Auto-generated method stub
			return m.myself.serializedSize();
		}
		
		
	};

}
