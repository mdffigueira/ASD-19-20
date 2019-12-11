package multipaxos.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import utils.Operation;

import java.net.UnknownHostException;

public class AcceptOkMessage extends ProtocolMessage {
    public static final short MSG_CODE = 202;
    private int instanceNumber, np;
    private Operation op;

    public AcceptOkMessage(int instanceNumber, int np, Operation op) {
        super(AcceptOkMessage.MSG_CODE);
        this.instanceNumber = instanceNumber;
        this.np = np;
        this.op = op;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public int getNp() {
        return np;
    }

    public Operation getOp() {
        return op;
    }

    public static final ISerializer<AcceptOkMessage> serializer = new ISerializer<AcceptOkMessage>() {
        @Override
        public void serialize(AcceptOkMessage m, ByteBuf byteBuf) {
            byteBuf.writeInt(m.instanceNumber);
            byteBuf.writeInt(m.np);
            m.op.serialize(byteBuf);
        }

        @Override
        public AcceptOkMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            int instanceNumber = byteBuf.readInt();
            int np = byteBuf.readInt();
            Operation op = Operation.deserialize(byteBuf);
            return new AcceptOkMessage(instanceNumber, np, op);
        }

        @Override
        public int serializedSize(AcceptOkMessage m) {
            return Integer.BYTES * 2 + m.op.serializedSize();
        }
    };
}
