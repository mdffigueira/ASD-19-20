package dissemination.requests;

import babel.requestreply.ProtocolRequest;
import dissemination.Message;

public class RouteRequest extends ProtocolRequest {

    public static final short REQUEST_ID = 901;

    private int topic;
    private Message message;
    

    public RouteRequest(int topic, Message msg) {
        super(RouteRequest.REQUEST_ID);
        this.topic = topic;
        this.message = msg;
    }

	public int getTopic() {
		return topic;
	}
	
	public Message getMsg() {
		return message;
	}
}
