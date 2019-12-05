package dissemination.message;

import babel.protocol.event.ProtocolMessage;
import utils.Message;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;

public class DisseminationMessage extends ProtocolMessage {

	public final static short MSG_CODE = 908;

	private Message msg;
	private int msgId;
	
	public DisseminationMessage(int msgId,Message msg) {
		super(DisseminationMessage.MSG_CODE);
		this.msg = msg;
		this.msgId = msgId;


	}
	
	public Message getMsg() {
		return this.msg;
	}
	public static final ISerializer<DisseminationMessage> serializer = new ISerializer<DisseminationMessage>() {
		@Override
		public void serialize(DisseminationMessage m, ByteBuf out) {
			m.msg.serialize(out);
			out.writeInt(m.msgId);
		}

		@Override
		public DisseminationMessage deserialize(ByteBuf in) throws UnknownHostException {
			Message thisMsg = Message.deserialize(in);
			int msgID = in.readInt();
			return new DisseminationMessage( msgID, thisMsg);
		}

		@Override
		public int serializedSize(DisseminationMessage m) {
			return m.msg.serializedSize() + Integer.BYTES;
		}
	};


	public int  getMsgId() {
		return msgId;
	}
}
