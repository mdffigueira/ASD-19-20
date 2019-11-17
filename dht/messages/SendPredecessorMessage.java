package dht.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

import java.net.UnknownHostException;

public class SendPredecessorMessage extends ProtocolMessage {
    public final static short MSG_CODE = 806;
    Host predecessor;

    public SendPredecessorMessage(Host predecessor) {

        super(SendPredecessorMessage.MSG_CODE);
        this.predecessor = predecessor;
    }
    public Host getPredecessor(){
        return predecessor;
    }
    public static final ISerializer<SendPredecessorMessage> serializer = new ISerializer<SendPredecessorMessage>() {
        @Override
        public void serialize(SendPredecessorMessage sendPredecessorMessage, ByteBuf byteBuf) {
            sendPredecessorMessage.predecessor.serialize(byteBuf);
        }

        @Override
        public SendPredecessorMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            Host pred = Host.deserialize(byteBuf);
            return new SendPredecessorMessage(pred);
        }

        @Override
        public int serializedSize(SendPredecessorMessage sendPredecessorMessage) {
            return 0;
        }
    };

}
