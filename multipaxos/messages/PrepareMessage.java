package multipaxos.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;


public class PrepareMessage extends ProtocolMessage {
    public final static short MSG_CODE = 201;
    private int instanceNumber, sequenceNumber;

    public PrepareMessage(int instanceNumber, int sequenceNumber) {
        super(PrepareMessage.MSG_CODE);
        this.instanceNumber = instanceNumber;
        this.sequenceNumber = sequenceNumber;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public static final ISerializer<PrepareMessage> serializer = new ISerializer<PrepareMessage>() {
        @Override
        public void serialize(PrepareMessage prepareMessage, ByteBuf byteBuf) {
            byteBuf.writeInt(prepareMessage.instanceNumber);
            byteBuf.writeInt(prepareMessage.sequenceNumber);
        }

        @Override
        public PrepareMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            int instanceNumber = byteBuf.readInt();
            int sequenceNumber = byteBuf.readInt();
            return new PrepareMessage(instanceNumber,sequenceNumber);
        }

        @Override
        public int serializedSize(PrepareMessage prepareMessage) {
            return Integer.BYTES*2;
        }
    };
}

