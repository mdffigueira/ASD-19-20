package hyparview.messages;

import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;
import babel.protocol.event.ProtocolMessage;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HyParViewJoinMessage extends ProtocolMessage {

    public final static short MSG_CODE = 404;
    //private final Host destination;
    private final Host sender;
    private volatile int size = -1;

    public HyParViewJoinMessage( Host sender) {
        super(HyParViewJoinMessage.MSG_CODE);
        //this.destination = destination;
        this.sender = sender;
    }

//   public Host getDestination() {
//        return destination;
//    }
//
    public Host getSender() {
        return sender;
    }


    @Override
    public String toString() {
        return "JoinMessage{" +
                "payload=" + /*"Dest:" + destination + */"/Sender:" + sender +
                '}';
    }

    public static final ISerializer<HyParViewJoinMessage> serializer = new ISerializer<HyParViewJoinMessage>() {
        @Override
        public void serialize(HyParViewJoinMessage hyp, ByteBuf out) {
        	//hyp.destination.serialize(out);
        	hyp.sender.serialize(out);
           
        }

        @Override
        public HyParViewJoinMessage deserialize(ByteBuf in) throws UnknownHostException {
        	//Host dest = Host.deserialize(in);
        	Host send = Host.deserialize(in);
            return new HyParViewJoinMessage(send);
        }

        @Override
        public int serializedSize(HyParViewJoinMessage m) {
            return m.sender.serializedSize();//+m.destination.serializedSize();
        }
    };
}
