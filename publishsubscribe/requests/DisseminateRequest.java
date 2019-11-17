package publishsubscribe.requests;

import babel.requestreply.ProtocolRequest;
import dissemination.message.DisseminationMessage;

public class DisseminateRequest extends ProtocolRequest {

    public static final short REQUEST_ID = 901;

    private byte[] message;
    private byte[] topic;

    public DisseminateRequest(byte[] topic, DisseminationMessage message2) {
        super(DisseminateRequest.REQUEST_ID);
        if (message2 != null) {
            this.message = new byte[message2.getLength()];
            System.arraycopy(message2, 0, this.message, 0, message2.getLength());
        } else {
            this.message = new byte[0];
        }
        if (topic != null) {
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
