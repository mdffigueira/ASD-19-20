package multipaxos.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import utils.Operation;

import java.net.UnknownHostException;

public class AcceptOkMessage extends ProtocolMessage {
    public static final short MSG_CODE = 203;
    private int n;
    private Operation op;
    //   private int seqNumb;

    public AcceptOkMessage(int n, Operation op) {
        super(AcceptOkMessage.MSG_CODE);
        this.n = n;
        this.op = op;
        //  this.seqNumb = seqNumb;
    }

    public int getN() {
        return n;
    }

//    public int getSeqNumb() {
//        return seqNumb;
//    }

    public Operation getOp() {
        return op;
    }

    public static final ISerializer<AcceptOkMessage> serializer = new ISerializer<AcceptOkMessage>() {
        @Override
        public void serialize(AcceptOkMessage m, ByteBuf byteBuf) {
            // byteBuf.writeInt(m.seqNumb);
            byteBuf.writeInt(m.n);
            m.op.serialize(byteBuf);
        }

        @Override
        public AcceptOkMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            int seqNumb = byteBuf.readInt();
            int n = byteBuf.readInt();
            Operation op = Operation.deserialize(byteBuf);
            return new AcceptOkMessage(n, op);
        }

        @Override
        public int serializedSize(AcceptOkMessage m) {
            return Integer.BYTES * 2 + m.op.serializedSize();
        }
    };
}
