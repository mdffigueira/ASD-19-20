package dht.messages;

import babel.protocol.event.ProtocolMessage;
import utils.Node;
import io.netty.buffer.ByteBuf;
import network.ISerializer;

import java.net.UnknownHostException;


public class UpdatePopularityMessage extends ProtocolMessage {
    public final static short MSG_CODE = 808;
    private int msgId;
    private Node nodeInterested;
    private int typeMsg;

    public UpdatePopularityMessage(int msgId, Node node, int type) {
        super(UpdatePopularityMessage.MSG_CODE);
        this.msgId = msgId;
        this.setNodeInterested(node);
        this.setTypeMsg(type);
    }

    public int getMsgId() {
        return msgId;
    }

    public Node getNodeInterested() {
		return nodeInterested;
	}

	public void setNodeInterested(Node nodeInterested) {
		this.nodeInterested = nodeInterested;
	}

	public int getTypeMsg() {
		return typeMsg;
	}

	public void setTypeMsg(int typeMsg) {
		this.typeMsg = typeMsg;
	}

	public static final ISerializer<UpdatePopularityMessage> serializer = new ISerializer<UpdatePopularityMessage>() {
        @Override
        public void serialize(UpdatePopularityMessage routeMessage, ByteBuf byteBuf) {
            byteBuf.writeInt(routeMessage.msgId);
            byteBuf.writeInt(routeMessage.typeMsg);
            routeMessage.nodeInterested.serialize(byteBuf);
        }

        @Override
        public UpdatePopularityMessage deserialize(ByteBuf byteBuf) throws UnknownHostException {
            int n = byteBuf.readInt();
            int type = byteBuf.readInt();
            Node node = Node.deserialize(byteBuf);
            return new UpdatePopularityMessage(n, node, type);
        }

        @Override
        public int serializedSize(UpdatePopularityMessage routeMessage) {
            return Integer.BYTES * 2 + routeMessage.getNodeInterested().serializedSize();
        }
    };

}
