package hyparview.messages;

import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;
import babel.protocol.event.ProtocolMessage;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HyParViewJoinReplyMessage extends ProtocolMessage {

    public final static short MSG_CODE = 408;
    private final Host destination;
    private final Host sender;
    private volatile int size = -1;

    public HyParViewJoinReplyMessage(Host destination, Host sender) {
        super(HyParViewJoinReplyMessage.MSG_CODE);
        this.destination = destination;
        this.sender = sender;
    }

    public Host getDestination() {
        return destination;
    }
    
    public Host getSender() {
        return sender;
    }


    @Override
    public String toString() {
        return "JoinReplyMessage{" +
                "payload=" + "Dest:" + destination + "/Sender:" + sender +
                '}';
    }

    public static final ISerializer<HyParViewJoinReplyMessage> serializer = new ISerializer<HyParViewJoinReplyMessage>() {
        @Override
        public void serialize(HyParViewJoinReplyMessage hyp, ByteBuf out) {
        	hyp.destination.serialize(out);
        	hyp.sender.serialize(out);
           
        }

        @Override
        public HyParViewJoinReplyMessage deserialize(ByteBuf in) throws UnknownHostException {
        	Host dest = Host.deserialize(in);
        	Host send = Host.deserialize(in);
            return new HyParViewJoinReplyMessage(dest, send);
        }

        @Override
        public int serializedSize(HyParViewJoinReplyMessage m) {
            return m.sender.serializedSize()+m.destination.serializedSize();
        }
    };
}
