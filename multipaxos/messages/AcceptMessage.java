package multipaxos.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import utils.Operation;

import java.net.UnknownHostException;

public class AcceptMessage extends ProtocolMessage {
    public static final short MSG_CODE = 202;
    private int n;
    private Operation op;
    private int seqNumb;

    AcceptMessage(int n, Operation op, int seqNumb) {
        super(AcceptMessage.MSG_CODE);
        this.n = n;
        this.op = op;
        this.seqNumb = seqNumb;
    }

    public int getN() {
        return n;
    }

    public int getSeqNumb() {
        return seqNumb;
    }

    public Operation getOp() {
        return op;
    }

    public static final ISerializer<AcceptMessage> serializer = new ISerializer<AcceptMessage>() {
        @Override
        public void serialize(AcceptMessage m, ByteBuf byteBuf) {
            byteBuf.writeInt(m.seqNumb);
            byteBuf.writeInt(m.n);
            m.op.serialize(byteBuf);
        }

        @Override
        public AcceptMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            int seqNumb = byteBuf.readInt();
            int n = byteBuf.readInt();
            Operation op = Operation.deserialize(byteBuf);
            return new AcceptMessage(n, op, seqNumb);
        }

        @Override
        public int serializedSize(AcceptMessage m) {
            return Integer.BYTES * 2 + m.op.serializedSize();
        }
    };
}
