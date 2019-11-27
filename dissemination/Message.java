package dissemination;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import dht.Node;

public class Message {
    // UUID mid;
    private byte[] message, topic;
    private int typeM;
    private Node nodeInterested;
    private Node nodeSender;

    public Message(byte[] message, int typeM, byte[] topic) {
        //   this.mid = UUID.randomUUID();
        this.message = message;
        this.typeM = typeM;
        this.topic = topic;

    }


//    public UUID getMid() {
//        return mid;
//
//    }

    public byte[] getMessage() {
        return message;

    }

    public byte[] getTopic() {
        return topic;
    }

    public int getTypeM() {
        return typeM;
    }

    public void serialize(ByteBuf byteBuf) {
        if (message != null) {
            byteBuf.writeInt(message.length);
            byteBuf.writeBytes(message);
            byteBuf.writeInt(typeM);
            byteBuf.writeInt(topic.length);
            byteBuf.writeBytes(topic);
        } else
            byteBuf.writeInt(0);

    }

    public static Message deserialize(ByteBuf byteBuf) {
        int msgSize = byteBuf.readInt();
        if (msgSize == 0) {
            return new Message(null, 0, null);
        }
        byte[] msg = new byte[msgSize];
        byteBuf.readBytes(msg);
        int typ = byteBuf.readInt();
        int topSize = byteBuf.readInt();
        byte[] top = new byte[topSize];
        byteBuf.readBytes(top);
        return new Message(msg, typ, top);
    }

    public int serializedSize() {
        if (message == null)
            return Integer.BYTES;
        return Integer.BYTES + message.length + Integer.BYTES * 2 + topic.length;
    }


    public void setNodeInterested(Node nodeID) {
        nodeInterested = nodeID;
    }


    public Node getNodeInterested() {
        return nodeInterested;
    }


    public void setSender(Node nodeID) {
        nodeSender = nodeID;
    }


    public Node getNodeSender() {
        return nodeSender;
    }

}
