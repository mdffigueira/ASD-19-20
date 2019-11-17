package floodbcast.delivers;

import babel.notification.ProtocolNotification;

public class FloodBCastDeliver extends ProtocolNotification {

    public static final short NOTIFICATION_ID = 301;
    public static final String NOTIFICATION_NAME = "BcastDeliver";

    private byte[] message;
    private byte[] topic;

    public FloodBCastDeliver(byte[] message, byte[] topic) {
        super(FloodBCastDeliver.NOTIFICATION_ID, FloodBCastDeliver.NOTIFICATION_NAME);
        if(message != null) {
            this.message = new byte[message.length];
            System.arraycopy(message, 0, this.message, 0, message.length);
        } else {
            this.message = new byte[0];
        }
        if(topic != null) {
            this.topic = new byte[topic.length];
            System.arraycopy(topic, 0, this.topic, 0, topic.length);
        } else {
            this.topic = new byte[0];
        }
    }

    public byte[] getMessage() {
        return message;
    }

	public byte[] getTopic() {
		return topic;
	}
}
