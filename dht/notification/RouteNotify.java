package dht.notification;

import babel.notification.ProtocolNotification;
import dht.Node;

public class RouteNotify extends ProtocolNotification {

    public final static short NOTIFICATION_ID = 101;
    public final static String NOTIFICATION_NAME = "node";

    public final Node node;
    public int isToAdd;



    public RouteNotify(Node n , int isToAdd) {
        super(RouteNotify.NOTIFICATION_ID, RouteNotify.NOTIFICATION_NAME);
        this.node = n;
        this.isToAdd= isToAdd;

    }
    public Node getNode() {
        return node;
    }
    public int getIsToAdd(){
        return isToAdd;
    }

    @Override
    public String toString() {
        return "RouteDeliveryNotification{" +
                "node=" + node +"isToAdd="+ isToAdd+
                '}';
    }

}
