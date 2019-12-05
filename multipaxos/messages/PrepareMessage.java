package multipaxos.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;


public class PrepareMessage extends ProtocolMessage {
    public final static short MSG_CODE = 201;
    private int N, psn;

    public PrepareMessage(int psn) {
        super(PrepareMessage.MSG_CODE);
        //this.N = N;
        this.psn = this.psn;
    }

//    public int getN() {
//        return N;
//    }
    public int getPsn(){
        return psn;
    }

    public static final ISerializer<PrepareMessage> serializer = new ISerializer<PrepareMessage>() {
        @Override
        public void serialize(PrepareMessage prepareMessage, ByteBuf byteBuf) {
            //byteBuf.writeInt(prepareMessage.N);
            byteBuf.writeInt(prepareMessage.psn);
        }

        @Override
        public PrepareMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
          //  int N=byteBuf.readInt();
            int seqNumber=byteBuf.readInt();
            return new PrepareMessage(seqNumber);
        }

        @Override
        public int serializedSize(PrepareMessage prepareMessage) {
            return Integer.BYTES;
        }
    };
}

