package dht.notification;

import babel.notification.ProtocolNotification;
import dht.Node;
import dissemination.Message;

public class RouteNotify extends ProtocolNotification {

    public final static short NOTIFICATION_ID = 101;
    public final static String NOTIFICATION_NAME = "node";

    private  Node upStream;
    private int isUpStream;
    private Message msg;
    private int msgID;


    public RouteNotify(int msgId, Node upStream, Message msg, int isUpStream) {
        super(RouteNotify.NOTIFICATION_ID, RouteNotify.NOTIFICATION_NAME);
        this.msgID = msgId;
        this.upStream = upStream;
        this.isUpStream = isUpStream;
        this.msg = msg;


    }

    public Node getUpStream() {
        return upStream;
    }

    public int getMsgID() {
        return msgID;
    }

    public Message getMsg() {
        return msg;
    }

    public int getIsUpStream() {
        return isUpStream;
    }

    @Override
    public String toString() {
        return "RouteNotifyNotification{" +
                "node=" + node + "isUpStream=" + isUpStream +
                '}';
    }

}
