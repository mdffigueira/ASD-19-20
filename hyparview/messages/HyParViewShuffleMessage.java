package hyparview.messages;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

public class HyParViewShuffleMessage extends ProtocolMessage{
	public final static short MSG_CODE = 401;
	private final Host sender,destination;
	private final Set<Host> shuffleSet;
	private int shuffleTtl;
	private volatile int size = -1;

	public HyParViewShuffleMessage(Host sender,Host destination, Set<Host> shuffleSet, int shuffleTtl) {
		super(HyParViewShuffleMessage.MSG_CODE);
		this.sender=sender;
		this.shuffleSet=shuffleSet;
		this.shuffleTtl=shuffleTtl;
		this.destination=destination;
	}

	public Host getDestination() {
		return destination;
	}

	public Host getSender() {
		return sender;
	}
	public int getTTL() {
		return shuffleTtl;
	}
	public Set<Host> getShuffleSet(){
		return shuffleSet;
	}

	@Override
	public String toString() {
		return "ShuffleMessage{" +
				"payload=" + "Dest:" + destination + "/Sender:" + sender +
				'}';
	}

	public static final ISerializer<HyParViewShuffleMessage> serializer = new ISerializer<HyParViewShuffleMessage>() {
		@Override
		public void serialize(HyParViewShuffleMessage hyp, ByteBuf out) {
			hyp.destination.serialize(out);
			hyp.sender.serialize(out);
			out.writeShort(hyp.shuffleSet.size());
            for(Host h: hyp.shuffleSet) {
                h.serialize(out);
            }
            out.writeInt(hyp.shuffleTtl);

		}

		@Override
		public HyParViewShuffleMessage deserialize(ByteBuf in) throws UnknownHostException {
			Set<Host>shuffleSetPayload = new HashSet<>();
			
			Host dest = Host.deserialize(in);
			Host send = Host.deserialize(in);
			short shuffleSetSize =in.readShort();
			for(short i = 0 ; i< shuffleSetSize; i++)
				shuffleSetPayload.add(Host.deserialize(in));
			int ttl= in.readInt();
			return new HyParViewShuffleMessage(dest, send,shuffleSetPayload, ttl);
		}

		@Override
		public int serializedSize(HyParViewShuffleMessage m) {
			if(m.size == -1) {
                m.size = Short.BYTES; //short size
                m.size+=m.destination.serializedSize()+m.sender.serializedSize();
                for (Host h : m.shuffleSet) {
                    m.size += h.serializedSize();
                }
            }
            return m.size;
		}
	};
}
