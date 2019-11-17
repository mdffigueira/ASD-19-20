package publishsubscribe.delivers;

import babel.notification.ProtocolNotification;

public class PSDeliver extends ProtocolNotification {

    public static final short NOTIFICATION_ID = 501;
    public static final String NOTIFICATION_NAME = "PSDeliver";

    private byte[] message;
    private byte[] topic;

    public PSDeliver(byte[] topic, byte[] message) {
        super(PSDeliver.NOTIFICATION_ID, PSDeliver.NOTIFICATION_NAME);
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
