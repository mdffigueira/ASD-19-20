package hyparview.messages;

import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;
import babel.protocol.event.ProtocolMessage;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HyParViewDisconnectMessage extends ProtocolMessage {

    public final static short MSG_CODE = 401;
    private final Host destination;
    private final Host sender;
    private volatile int size = -1;

    public HyParViewDisconnectMessage(Host destination, Host sender) {
        super(HyParViewDisconnectMessage.MSG_CODE);
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
        return "DisconnectMessage{" +
                "payload=" + "Dest:" + destination + "/Sender:" + sender +
                '}';
    }

    public static final ISerializer<HyParViewDisconnectMessage> serializer = new ISerializer<HyParViewDisconnectMessage>() {
        @Override
        public void serialize(HyParViewDisconnectMessage hyp, ByteBuf out) {
        	hyp.destination.serialize(out);
        	hyp.sender.serialize(out);
        }

        @Override
        public HyParViewDisconnectMessage deserialize(ByteBuf in) throws UnknownHostException {
        	Host dest = Host.deserialize(in);
        	Host send = Host.deserialize(in);
            return new HyParViewDisconnectMessage(dest, send);
        }

        @Override
        public int serializedSize(HyParViewDisconnectMessage m) {
        	return m.sender.serializedSize()+m.destination.serializedSize();
        }
    };
}
