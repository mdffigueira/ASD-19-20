package multipaxos.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;
import utils.Operation;

import java.net.UnknownHostException;

public class AcceptMessage extends ProtocolMessage {
    public static final short MSG_CODE = 201;
    private int instanceNumber, np;
    private Operation op;

    public AcceptMessage(int instanceNumber, int np, Operation op) {
        super(AcceptMessage.MSG_CODE);
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

    public static final ISerializer<AcceptMessage> serializer = new ISerializer<AcceptMessage>() {
        @Override
        public void serialize(AcceptMessage m, ByteBuf byteBuf) {
            byteBuf.writeInt(m.instanceNumber);
            byteBuf.writeInt(m.np);
            m.op.serialize(byteBuf);
        }

        @Override
        public AcceptMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            int instanceNumber = byteBuf.readInt();
            int np = byteBuf.readInt();
            Operation op = Operation.deserialize(byteBuf);
            return new AcceptMessage(instanceNumber,np, op);
        }

        @Override
        public int serializedSize(AcceptMessage m) {
            return Integer.BYTES * 2 + m.op.serializedSize();
        }
    };
}
