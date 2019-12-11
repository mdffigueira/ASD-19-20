package publishsubscribe.requests;

import babel.requestreply.ProtocolRequest;

public class PSUnsubscribeRequest extends ProtocolRequest {

    public static final short REQUEST_ID = 506;

    private byte[] topic;

    public PSUnsubscribeRequest(byte[] topic) {
        super(PSUnsubscribeRequest.REQUEST_ID);
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
}
