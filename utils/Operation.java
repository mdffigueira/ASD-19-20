package utils;

import io.netty.buffer.ByteBuf;

public class Operation {
    public static final int ADD_REPLICA = 1;
    public static final int REMOVE_REPLICA = 2;
    public static final int PUBLISH = 3;

    private int type;
    private Message msg;

    public Operation(int type, Message msg) {
        this.type = type;
        this.msg = msg;
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
    }
    public static Operation deserialize(ByteBuf byteBuf){
        int t=byteBuf.readInt();
        Message m= Message.deserialize(byteBuf);
        return new Operation(t,m);
    }
    public int serializedSize(){
        return Integer.BYTES+msg.serializedSize();
    }
}
