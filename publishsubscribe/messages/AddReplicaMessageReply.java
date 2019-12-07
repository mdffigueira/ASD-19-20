package publishsubscribe.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class AddReplicaMessageReply extends ProtocolMessage {
    public static final short MSG_CODE = 507;
    private Host h;
    private Set<Host> replica;
    private int n;
    private volatile int size;

    public AddReplicaMessageReply(Set<Host> replica, int n, Host h) {
        super(AddReplicaMessageReply.MSG_CODE);
        this.replica = replica;
        this.n = n;
        this.h = h;
    }

    public Host getH() {
        return h;
    }

    public static final ISerializer<AddReplicaMessageReply> serializer = new ISerializer<AddReplicaMessageReply>() {
        @Override
        public void serialize(AddReplicaMessageReply p, ByteBuf byteBuf) {
            p.h.serialize(byteBuf);
            byteBuf.writeInt(p.replica.size());
            for (Host h : p.replica) {
                h.serialize(byteBuf);
            }
            byteBuf.writeInt(p.n);
        }

        @Override
        public AddReplicaMessageReply deserialize(ByteBuf byteBuf) throws UnknownHostException {
            Host h = Host.deserialize(byteBuf);
            Set<Host> replica = new HashSet<>();
            for (int i = 0; i < byteBuf.readInt(); i++) {
                replica.add(Host.deserialize(byteBuf));
            }
            int n = byteBuf.readInt();
            return new AddReplicaMessageReply(replica, n, h);
        }

        @Override
        public int serializedSize(AddReplicaMessageReply m) {
            if( m.size ==-1)
                m.size =2;
            for(Host h : m.replica){
                m.size+=h.serializedSize();
            }
            return (m.size+Integer.BYTES+m.h.serializedSize());
        }
    };

}
