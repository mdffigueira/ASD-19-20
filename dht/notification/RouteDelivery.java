package dht.notification;

import babel.notification.ProtocolNotification;
import dht.Node;
import dissemination.Message;


public class RouteDelivery extends ProtocolNotification {
    public final static short NOTIFICATION_ID = 101;
    public final static String NOTIFICATION_NAME = "node";

    private final int msgId;
    private Message msg;

    public RouteDelivery(int msgId , Message m ) {
        super(RouteDelivery.NOTIFICATION_ID, RouteDelivery.NOTIFICATION_NAME);
        this.msgId = msgId;
        this.msg = msg;
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
}
