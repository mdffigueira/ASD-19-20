package dissemination.requests;

import babel.requestreply.ProtocolRequest;
import utils.Message;

public class RouteRequest extends ProtocolRequest {

    public static final short REQUEST_ID = 601;

    private int id;
    private Message message;
    int hasUpStream;
    

    public RouteRequest(int id, Message msg,int hasUpStream) {
        super(RouteRequest.REQUEST_ID);
        this.id = id;
        this.message = msg;
        this.hasUpStream=hasUpStream;
    }

	public int getID() {
		return id;
	}
	
	public Message getMsg() {
		return message;
	}
	public int getHasUpStream(){
        return hasUpStream;
    }
}
