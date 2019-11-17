package dht.notification;

import babel.notification.ProtocolNotification;
import dht.Node;


public class RouteDelivery extends ProtocolNotification {
    public final static short NOTIFICATION_ID = 101;
    public final static String NOTIFICATION_NAME = "node";

    public final Node node;

    public RouteDelivery(Node n) {
        super(RouteDelivery.NOTIFICATION_ID, RouteDelivery.NOTIFICATION_NAME);
        this.node = n;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "RouteDeliveryNotification{" +
                "node=" + node +
                '}';
    }
}
