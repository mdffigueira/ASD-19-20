package dissemination.requests;

import babel.requestreply.ProtocolRequest;
import dht.Node;
import dissemination.Message;

public class RouteRequest extends ProtocolRequest {

    public static final short REQUEST_ID = 901;

    private int id;
    private Message message;
    

    public RouteRequest(int id, Message msg) {
        super(RouteRequest.REQUEST_ID);
        this.id = id;
        this.message = msg;
    }

	public int getID() {
		return id;
	}
	
	public Message getMsg() {
		return message;
	}
}
