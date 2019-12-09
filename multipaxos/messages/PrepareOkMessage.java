package multipaxos.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import utils.Operation;

import java.net.UnknownHostException;

public class PrepareOkMessage extends ProtocolMessage {
    public static final short MSG_CODE = 204;
    private int n;
    private Operation op;

    public PrepareOkMessage(int n, Operation op) {
        super(PrepareOkMessage.MSG_CODE);
        this.n = n;
        this.op = op;
    }

    public int getN() {
        return n;
    }

    public Operation getOp() {
        return op;
    }

    public static final ISerializer<PrepareOkMessage> serializer = new ISerializer<PrepareOkMessage>() {
        @Override
        public void serialize(PrepareOkMessage m, ByteBuf byteBuf) {
            // byteBuf.writeInt(m.seqNumb);
            byteBuf.writeInt(m.n);
            m.op.serialize(byteBuf);
        }

        @Override
        public PrepareOkMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            int seqNumb = byteBuf.readInt();
            int n = byteBuf.readInt();
            Operation op = Operation.deserialize(byteBuf);
            return new PrepareOkMessage(n, op);
        }

        @Override
        public int serializedSize(PrepareOkMessage m) {
            return Integer.BYTES * 2 + m.op.serializedSize();
        }
    };
}
