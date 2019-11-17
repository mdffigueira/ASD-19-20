package hyparview.messages;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

public class HyParViewShuffleReplyMessage extends ProtocolMessage{
	public final static short MSG_CODE = 406;
	private final Host sender,destination;
	private final Set<Host>  shuffleReplySet;
	private volatile int size = -1;
	public HyParViewShuffleReplyMessage(Host sender,Host destination,Set<Host> shuffleReplySet) {
		super(HyParViewShuffleReplyMessage.MSG_CODE);
		this.sender=sender;
		this.destination=destination;
		this.shuffleReplySet=shuffleReplySet;
		// TODO Auto-generated constructor stub
	}
	public Host getDestination() {
		return destination;
	}

	public Host getSender() {
		return sender;
	}

	public Set<Host> getshuffleReply(){
		return shuffleReplySet;
	}
	public String toString() {
		return "ShuffleReplyMessage{" +
				"payload=" + "Dest:" + destination + "/Sender:" + sender +
				'}';
	}

	public static final ISerializer<HyParViewShuffleReplyMessage> serializer = new ISerializer<HyParViewShuffleReplyMessage>() {
		@Override
		public void serialize(HyParViewShuffleReplyMessage hyp, ByteBuf out) {
			hyp.destination.serialize(out);
			hyp.sender.serialize(out);
            out.writeShort(hyp.shuffleReplySet.size());
            for(Host h: hyp.shuffleReplySet) {
            	h.serialize(out);
            }
		}

		@Override
		public HyParViewShuffleReplyMessage deserialize(ByteBuf in) throws UnknownHostException {
			Set<Host>kpPayload = new HashSet<>();
			
			Host dest = Host.deserialize(in);
			Host send = Host.deserialize(in);
			short kpSize = in.readShort();
			for (short i = 0; i<kpSize; i++)
				kpPayload.add(Host.deserialize(in));
			return new HyParViewShuffleReplyMessage(dest, send, kpPayload);
		}

		@Override
		public int serializedSize(HyParViewShuffleReplyMessage m) {
			if(m.size == -1) {
                m.size = Short.BYTES; //short size
                m.size+=m.destination.serializedSize()+m.sender.serializedSize();
                for (Host h : m.shuffleReplySet)
                	m.size+=h.serializedSize();
            }
            return m.size;
		}
	};
}