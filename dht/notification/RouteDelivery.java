package dht.notification;

import babel.notification.ProtocolNotification;
import utils.Message;


public class RouteDelivery extends ProtocolNotification {
    public final static short NOTIFICATION_ID = 101;
    public final static String NOTIFICATION_NAME = "node";

    private final int msgId;
    private Message msg;
    private int protocolId;

    public RouteDelivery(int msgId , Message msg, int idProt) {
        super(RouteDelivery.NOTIFICATION_ID, RouteDelivery.NOTIFICATION_NAME);
        this.msgId = msgId;
        this.msg = msg;
        this.protocolId = idProt;
    }

    public int getMsgId() {
        return msgId;
    }

    public Message getMsg(){
        return msg;
    }

    @Override
    public String toString() {
        return "RouteDeliveryNotification{" +
                "message id=" + msgId +
                '}';
    }

	public int getProtocolId() {
		return protocolId;
	}
}
