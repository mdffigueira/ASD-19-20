package dissemination.notification;

import babel.notification.ProtocolNotification;
import dht.Node;
import dht.notification.RouteDelivery;


public class UpdatePopularity extends ProtocolNotification {
    public final static short NOTIFICATION_ID = 901;
    public final static String NOTIFICATION_NAME = "popularityUpdate";

    private Node nodeInterested;
    private int typeMsg;
    private int msgId;
    private int protocolId;
    
    public UpdatePopularity(int msgId, Node nodeInterested, int type, int protocol) {

        super(RouteDelivery.NOTIFICATION_ID, RouteDelivery.NOTIFICATION_NAME);
		this.setNodeInterested(nodeInterested);
		this.setTypeMsg(type);
		this.setMsgId(msgId);
		this.setProtocolId(protocol);
	}

    @Override
    public String toString() {
        return "UpdatePopularityNotification{" +
        		//"Topic= " + msgId +
                "NodeInterested=" + nodeInterested.getId() +
                "TypeOfAction = " + typeMsg +
                '}';
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

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public int getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(int protocolId) {
		this.protocolId = protocolId;
	}
}
