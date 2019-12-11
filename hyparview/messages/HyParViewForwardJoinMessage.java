package hyparview.messages;

import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;
import babel.protocol.event.ProtocolMessage;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.print.attribute.standard.MediaSize.Other;

public class HyParViewForwardJoinMessage extends ProtocolMessage {

    public final static short MSG_CODE = 402;
   // private final Host n;
    private final Host newNode;
    private final Host myself;
    private final int timeToLive;

    public HyParViewForwardJoinMessage(Host newNode,int timeToLive, Host myself ) {
        super(HyParViewForwardJoinMessage.MSG_CODE);
     //   this.n = n;
        this.newNode= newNode;
        this.timeToLive = timeToLive;
        this.myself = myself;
    }

    
//    public Host getN() {
//    	return n;
//    }
    
    public Host getNewNode() {
    	return newNode;
    }
    
    public Host getMyself() {
    	return myself;
    }
    
    public int getTTL() {
    	return timeToLive;
    }

    @Override
    public String toString() {
        return "ForwardJoinMessage{" +
                "payload="/* + "Dest:" + n*/ + "/Sender:" + myself + "/Node:" + newNode + "/TTL:" + timeToLive +
                '}';
    }

    public static final ISerializer<HyParViewForwardJoinMessage> serializer = new ISerializer<HyParViewForwardJoinMessage>() {
        @Override
        public void serialize(HyParViewForwardJoinMessage hyp, ByteBuf out) {
        	//hyp.n.serialize(out);
        	hyp.newNode.serialize(out);
            out.writeInt(hyp.timeToLive);
        	hyp.myself.serialize(out);
        }

        @Override
        public HyParViewForwardJoinMessage deserialize(ByteBuf in) throws UnknownHostException {
        	//Host recN = Host.deserialize(in);
        	Host recNewNode = Host.deserialize(in);
            int recttl =in.readInt();
        	Host recMySelf = Host.deserialize(in);
            return new HyParViewForwardJoinMessage( recNewNode,recttl,recMySelf);
        }

        @Override
        public int serializedSize(HyParViewForwardJoinMessage m) {
        
            return /*m.n.serializedSize()+*/m.newNode.serializedSize()+Integer.BYTES+m.myself.serializedSize();
        }
    };
}
