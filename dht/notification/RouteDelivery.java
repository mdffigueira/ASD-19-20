package dht.notification;

import babel.notification.ProtocolNotification;
import dht.Node;
import dissemination.Message;


public class RouteDelivery extends ProtocolNotification {
    public final static short NOTIFICATION_ID = 101;
    public final static String NOTIFICATION_NAME = "node";

    public final Node node;
    Message m;

    public RouteDelivery(Node n , Message m ) {
        super(RouteDelivery.NOTIFICATION_ID, RouteDelivery.NOTIFICATION_NAME);
        this.node = n;
        this.m = m;
    }

    public Node getNode() {
        return node;
    }

    public Message getM(){
        return m;
    }
    @Override
    public String toString() {
        return "RouteDeliveryNotification{" +
                "node=" + node +
                '}';
    }
}
