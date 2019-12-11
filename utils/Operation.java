package utils;

import java.net.UnknownHostException;

import io.netty.buffer.ByteBuf;
import network.Host;

public class Operation {
    public static final int ADD_REPLICA = 1;
    public static final int REMOVE_REPLICA = 2;
    public static final int PUBLISH = 3;

    private int type;
    private Message msg;
    private Host replica;
    private Host leader;
    
    public Operation(int type, Message msg, Host replica, Host leader) {
        this.type = type;
        this.msg = msg;
        this.replica = replica;
        this.leader = leader;
    }

    public Message getMsg() {
        return msg;
    }

    public int getType() {
        return type;
    }
    public Host getReplica() {
        return replica;
    }
    public Host getLeader(){
        return leader;
    }

    public void serialize(ByteBuf byteBuf){
        byteBuf.writeInt(type);
        msg.serialize(byteBuf);
        replica.serialize(byteBuf);
        replica.serialize(byteBuf);
    }
    public static Operation deserialize(ByteBuf byteBuf) throws UnknownHostException{
        int t=byteBuf.readInt();
        Message m= Message.deserialize(byteBuf);
        Host rep = Host.deserialize(byteBuf);
        Host leader =Host.deserialize(byteBuf);
        return new Operation(t,m, rep,leader);
    }
    public int serializedSize(){
        return Integer.BYTES+msg.serializedSize()+replica.serializedSize()+leader.serializedSize();
    }


}
