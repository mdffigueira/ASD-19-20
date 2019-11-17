package dissemination.requests;

import babel.requestreply.ProtocolRequest;

public class RouteRequest extends ProtocolRequest {

    public static final short REQUEST_ID = 901;

    private byte[] message;
    private byte[] topic;
    private int typeM;
    

    public RouteRequest(byte[] message, byte[] topic, int type) {
        super(RouteRequest.REQUEST_ID);
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
        	this.typeM = type;
    }

    public byte[] getMessage() {
        return message;
    }

	public byte[] getTopic() {
		return topic;
	}
	
	public int getTypeM() {
		return typeM;
	}
}
