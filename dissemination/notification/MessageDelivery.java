package dissemination.notification;

import babel.notification.ProtocolNotification;
import dissemination.Message;

public class MessageDelivery extends ProtocolNotification {

    public static final short NOTIFICATION_ID = 901;
    public static final String NOTIFICATION_NAME = "MessageDelivery";

    private Message message;
    private byte[] topic;

    public MessageDelivery(byte[] topic, Message msg) {
        super(MessageDelivery.NOTIFICATION_ID, MessageDelivery.NOTIFICATION_NAME);
        message = msg;
        if(topic != null) {
            this.topic = new byte[topic.length];
            System.arraycopy(topic, 0, this.topic, 0, topic.length);
        } else {
            this.topic = new byte[0];
        }
    }

    public Message getMessage() {
        return message;
    }
    
    public byte[] getMessageBody() {
        return message.getMessage();
    }
    
    public byte[] getTopic() {
    	return topic;
    }
}
