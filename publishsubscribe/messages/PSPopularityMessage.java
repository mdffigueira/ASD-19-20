package publishsubscribe.messages;

import babel.protocol.event.ProtocolMessage;
import dht.Node;
import dissemination.Message;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.TreeSet;
import java.util.UUID;

public class PSPopularityMessage extends ProtocolMessage {

	public final static short MSG_CODE = 908;

	private Message msg;
	private int msgId;
	private int popular;
	
	public PSPopularityMessage(int msgId,Message msg, int pop) {
		super(PSPopularityMessage.MSG_CODE);
		this.msg = msg;
		this.msgId = msgId;
		this.popular = pop;

	}
	
	public Message getMsg() {
		return this.msg;
	}
	
	public int isPopular() {
		return popular;
	}
	
	public static final ISerializer<PSPopularityMessage> serializer = new ISerializer<PSPopularityMessage>() {
		@Override
		public void serialize(PSPopularityMessage m, ByteBuf out) {
			m.msg.serialize(out);
			out.writeInt(m.msgId);
			out.writeInt(m.popular);
		}

		@Override
		public PSPopularityMessage deserialize(ByteBuf in) throws UnknownHostException {
			Message thisMsg = Message.deserialize(in);
			int msgID = in.readInt();
			int pop = in.readInt();
			return new PSPopularityMessage( msgID, thisMsg, pop);
		}

		@Override
		public int serializedSize(PSPopularityMessage m) {
			return m.msg.serializedSize() + Integer.BYTES * 2;
		}
	};


	public int  getMsgId() {
		return msgId;
	}
}
