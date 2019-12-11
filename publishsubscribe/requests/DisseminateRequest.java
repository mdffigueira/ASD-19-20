package publishsubscribe.requests;

import babel.requestreply.ProtocolRequest;
import utils.Message;

public class DisseminateRequest extends ProtocolRequest {

    public static final short REQUEST_ID = 502;

    Message m;
    private byte[] topic;

    public DisseminateRequest(byte[] topic, Message m) {
        super(DisseminateRequest.REQUEST_ID);
        this.m = m;
        if (topic != null) {
            this.topic = new byte[topic.length];
            System.arraycopy(topic, 0, this.topic, 0, topic.length);
        } else {
            this.topic = new byte[0];
        }
    }

    public Message getMessage() {
        return m;
    }

    public byte[] getTopic() {
        return topic;
    }

}
