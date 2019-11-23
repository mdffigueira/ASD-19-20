package dht.notification;

import babel.notification.ProtocolNotification;
import dht.Node;
import dissemination.Message;

public class RouteNotify extends ProtocolNotification {

    public final static short NOTIFICATION_ID = 101;
    public final static String NOTIFICATION_NAME = "node";

    public final Node node;
    public int isUpStream;
    Message msg;


    public RouteNotify(Node n , Message msg, int isUpStream) {
        super(RouteNotify.NOTIFICATION_ID, RouteNotify.NOTIFICATION_NAME);
        this.node = n;
        this.isUpStream= isUpStream;
        this.msg= msg;


    }
    public Node getNode() {
        return node;
    }
    public int getIsUpstream(){
        return isUpStream;
    }
    public Message getMsg(){
        return msg;
    }

    @Override
    public String toString() {
        return "RouteNotifyNotification{" +
                "node=" + node +"isUpStream="+ isUpStream+
                '}';
    }

}
