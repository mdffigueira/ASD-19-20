package dissemination.requests;

import babel.requestreply.ProtocolRequest;

public class RouteRequest extends ProtocolRequest {

    public static final short REQUEST_ID = 901;

    private byte[] topic;
    private byte[] message;
    

    public RouteRequest(byte[] topic, byte[] msg) {
        super(RouteRequest.REQUEST_ID);
        if(topic != null) {
            this.topic = new byte[topic.length];
            System.arraycopy(topic, 0, this.topic, 0, topic.length);
        } else {
            this.topic = new byte[0];
        }
        if(message != null) {
            this.topic = new byte[message.length];
            System.arraycopy(message, 0, this.message, 0, message.length);
        } else {
            this.message = new byte[0];
        }
    }

	public byte[] getTopic() {
		return topic;
	}
	
	public byte[] getMsg() {
		return message;
	}
}
