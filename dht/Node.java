package dht;

import io.netty.buffer.ByteBuf;
import network.Host;
import java.net.UnknownHostException;

public class Node {
    private Host myself;
    private int id;
    private Boolean sendAgain;
    // private BigInteger finger;

    public Node(int id, Host myself) {
        this.myself = myself;
        this.id = id;
        sendAgain = false;
        //    finger =null;
    }

    public Host getMyself() {
        return myself;
    }

    public int getId() {
        return id;
    }

    public Boolean getSendAgain() {
        return sendAgain;
    }

    public void isToSendAgain(boolean s) {
        sendAgain = s;
    }
    //   public void addFinger(BigInteger finger) {
    //finger=finger;
    //\  }

    public void serialize(ByteBuf byteBuf) {
        if (getId() != 0) {
            byteBuf.writeInt(getId());
            myself.serialize(byteBuf);
        } else
            byteBuf.writeInt(0);
    }

    public static Node deserialize(ByteBuf byteBuf) throws UnknownHostException {
        int nId = byteBuf.readInt();
        if (nId == 0) {
            return new Node(0, null);
        }

        Host ms = Host.deserialize(byteBuf);
        return new Node(nId, ms);
    }

    public int serializedSize() {
        if (getId() == 0)
            return Integer.BYTES;
        return myself.serializedSize() + Integer.BYTES;
    }
}
