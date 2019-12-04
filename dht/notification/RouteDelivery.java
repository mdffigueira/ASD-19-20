package dht.notification;

import babel.notification.ProtocolNotification;
import dht.Node;
import dissemination.Message;


public class RouteDelivery extends ProtocolNotification {
    public final static short NOTIFICATION_ID = 101;
    public final static String NOTIFICATION_NAME = "node";

    private final int msgId;
    private int popular;
    private Message msg;
    private int protocolId;

    public RouteDelivery(int msgId , Message msg, int idProt) {
        super(RouteDelivery.NOTIFICATION_ID, RouteDelivery.NOTIFICATION_NAME);
        this.msgId = msgId;
        this.msg = msg;
        this.popular = 0;
        this.protocolId = idProt;
    }
    
    public RouteDelivery(int msgId , Message msg, int idProt, int pop) {
        super(RouteDelivery.NOTIFICATION_ID, RouteDelivery.NOTIFICATION_NAME);
        this.msgId = msgId;
        this.msg = msg;
        this.popular = pop;
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

	public int isPopular() {
		return popular;
	}
	
	public int getProtocolId() {
		return protocolId;
	}
}
