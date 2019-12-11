package publishsubscribe.requests;

import babel.requestreply.ProtocolRequest;

public class PSSubscribeRequest extends ProtocolRequest {

    public static final short REQUEST_ID = 505;
    private byte[] message;
    private byte[] topic;

    public PSSubscribeRequest(byte[] topic) {
        super(PSSubscribeRequest.REQUEST_ID);
        if(topic != null) {
            this.topic = new byte[topic.length];
            System.arraycopy(topic, 0, this.topic, 0, topic.length);
        } else {
            this.topic = new byte[0];
        }
    }

	public byte[] getTopic() {
		return topic;
	}

	public byte[] getMessage() {
		return message;
	}
}
