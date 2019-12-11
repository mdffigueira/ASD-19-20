package multipaxos.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import utils.Operation;

import java.net.UnknownHostException;

public class PrepareOkMessage extends ProtocolMessage {
    public static final short MSG_CODE = 205;
    private int seqN, instN;

    public PrepareOkMessage(int seqN, int instN) {
        super(PrepareOkMessage.MSG_CODE);
        this.seqN = seqN;
        this.instN = instN;
    }

    public int getSeqN() {
        return seqN;
    }

    public int getInstN() {
        return instN;
    }

    public static final ISerializer<PrepareOkMessage> serializer = new ISerializer<PrepareOkMessage>() {
        @Override
        public void serialize(PrepareOkMessage m, ByteBuf byteBuf) {
            // byteBuf.writeInt(m.seqNumb);
            byteBuf.writeInt(m.seqN);
            byteBuf.writeInt(m.instN);
        }

        @Override
        public PrepareOkMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            int seqN = byteBuf.readInt();
            int instN = byteBuf.readInt();
            //Operation op = Operation.deserialize(byteBuf);
            return new PrepareOkMessage(seqN, instN);
        }

        @Override
        public int serializedSize(PrepareOkMessage m) {
            return Integer.BYTES * 2;
        }
    };
}
