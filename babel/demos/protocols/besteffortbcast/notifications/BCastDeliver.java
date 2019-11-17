package babel.demos.protocols.besteffortbcast.notifications;

import babel.notification.ProtocolNotification;

public class BCastDeliver extends ProtocolNotification {

    public static final short NOTIFICATION_ID = 201;
    public static final String NOTIFICATION_NAME = "BcastDeliver";

    private byte[] message;

    public BCastDeliver(byte[] message) {
        super(BCastDeliver.NOTIFICATION_ID, BCastDeliver.NOTIFICATION_NAME);
        if(message != null) {
            this.message = new byte[message.length];
            System.arraycopy(message, 0, this.message, 0, message.length);
        } else {
            this.message = new byte[0];
        }
    }

    public byte[] getMessage() {
        return message;
    }
}
