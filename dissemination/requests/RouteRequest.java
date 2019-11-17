package dissemination.requests;

import babel.requestreply.ProtocolRequest;
import dht.Node;
import dissemination.Message;

public class RouteRequest extends ProtocolRequest {

    public static final short REQUEST_ID = 901;

    private Node id;
    private Message message;
    

    public RouteRequest(Node id, Message msg) {
        super(RouteRequest.REQUEST_ID);
        this.id = id;
        this.message = msg;
    }

	public Node getID() {
		return id;
	}
	
	public Message getMsg() {
		return message;
	}
}
