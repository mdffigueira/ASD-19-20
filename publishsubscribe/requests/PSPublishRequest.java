package publishsubscribe.requests;

import babel.requestreply.ProtocolRequest;

public class PSPublishRequest extends ProtocolRequest {

    public static final short REQUEST_ID = 504;

    private byte[] message;
    private byte[] topic;

    public PSPublishRequest(byte[] message, byte[] topic) {
        super(PSPublishRequest.REQUEST_ID);
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
