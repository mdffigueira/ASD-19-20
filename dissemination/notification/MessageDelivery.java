package dissemination.notification;

import babel.notification.ProtocolNotification;
import utils.Message;

public class MessageDelivery extends ProtocolNotification {

    public static final short NOTIFICATION_ID = 901;
    public static final String NOTIFICATION_NAME = "MessageDelivery";

    private Message message;
    private int msgId;

    public MessageDelivery(int msgId, Message msg) {
        super(MessageDelivery.NOTIFICATION_ID, MessageDelivery.NOTIFICATION_NAME);
        message = msg;
        this.msgId = msgId;

    }

    public Message getMessage() {
        return message;
    }

   public int getMsgId(){
        return msgId;
   }
}
