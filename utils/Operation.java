package utils;

import java.net.UnknownHostException;

import io.netty.buffer.ByteBuf;

public class Operation {
    public static final int ADD_REPLICA = 1;
    public static final int REMOVE_REPLICA = 2;
    public static final int PUBLISH = 3;

    private int type;
    private Message msg;
    private Node replica;
    
    public Operation(int type, Message msg, Node replica) {
        this.type = type;
        this.msg = msg;
        this.replica = replica;
    }

    public Message getMsg() {
        return msg;
    }

    public int getType() {
        return type;
    }

    public void serialize(ByteBuf byteBuf){
        byteBuf.writeInt(type);
        msg.serialize(byteBuf);
        replica.serialize(byteBuf);
    }
    public static Operation deserialize(ByteBuf byteBuf) throws UnknownHostException{
        int t=byteBuf.readInt();
        Message m= Message.deserialize(byteBuf);
        Node rep = Node.deserialize(byteBuf);
        return new Operation(t,m, rep);
    }
    public int serializedSize(){
        return Integer.BYTES+msg.serializedSize()+replica.serializedSize();
    }

	public Node getReplica() {
		return replica;
	}
}
