package publishsubscribe.messages;

import babel.protocol.event.ProtocolMessage;
import io.netty.buffer.ByteBuf;
import network.Host;
import network.ISerializer;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class AddReplicaMessageReply extends ProtocolMessage {
    public static final short MSG_CODE = 502;
    private Host leader;
    private Set<Host> replicas;
    private int instanceNumber, sequenceNumber;
    private volatile int size;

    public AddReplicaMessageReply(Set<Host> replica, int instanceNumber, int sequenceNumber, Host leader) {
        super(AddReplicaMessageReply.MSG_CODE);
        this.replicas = replica;
        this.instanceNumber = instanceNumber;
        this.sequenceNumber = sequenceNumber;
        this.leader = leader;
    }

    public Host getLeader() {
        return leader;
    }

    public Set<Host> getReplicas() {
        return replicas;
    }

    public int getInstancePaxos() {
        return instanceNumber;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public static final ISerializer<AddReplicaMessageReply> serializer = new ISerializer<AddReplicaMessageReply>() {
        @Override
        public void serialize(AddReplicaMessageReply p, ByteBuf byteBuf) {
            p.leader.serialize(byteBuf);
            byteBuf.writeInt(p.replicas.size());
            for (Host h : p.replicas) {
                h.serialize(byteBuf);
            }
            byteBuf.writeInt(p.instanceNumber);
            byteBuf.writeInt(p.sequenceNumber);
        }

        @Override
        public AddReplicaMessageReply deserialize(ByteBuf byteBuf) throws UnknownHostException {
            Host leader = Host.deserialize(byteBuf);
            Set<Host> replica = new HashSet<>();
            int size = byteBuf.readInt();
            for (int i = 0; i < size; i++) {
                replica.add(Host.deserialize(byteBuf));
            }
            int instNumb = byteBuf.readInt();
            int seqNumb = byteBuf.readInt();
            return new AddReplicaMessageReply(replica, instNumb, seqNumb, leader);
        }

        @Override
        public int serializedSize(AddReplicaMessageReply m) {
            if (m.size == -1)
                m.size = 2;
            for (Host h : m.replicas) {
                m.size += h.serializedSize();
            }

            return (m.size + Integer.BYTES * 3 + m.leader.serializedSize());
        }
    };

}
