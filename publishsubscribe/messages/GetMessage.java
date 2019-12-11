package publishsubscribe.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;

public class GetMessage extends ProtocolMessage {
    public final static short MSG_CODE = 504;
    byte[] topic;
    int seqNumber;

    public GetMessage(byte[] topic, int seqNumber) {
        super(GetMessage.MSG_CODE);
        this.topic = topic;
        this.seqNumber = seqNumber;
    }

    public byte[] getTopic() {
        return topic;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public static final ISerializer<GetMessage> serializer = new ISerializer<GetMessage>() {
        @Override
        public void serialize(GetMessage m, ByteBuf byteBuf) {
            byteBuf.writeInt(m.topic.length);
            byteBuf.writeBytes(m.topic);
            byteBuf.writeInt(m.seqNumber);
        }

        @Override
        public GetMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            int size = byteBuf.readInt();
            byte[] topic = new byte[size];
            byteBuf.writeBytes(topic);
            int seqNumber = byteBuf.readInt();
            return new GetMessage(topic, seqNumber);
        }

        @Override
        public int serializedSize(GetMessage m) {
            return Integer.BYTES * 2 + m.topic.length;
        }
    };
}
