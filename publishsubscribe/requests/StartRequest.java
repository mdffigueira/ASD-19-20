package publishsubscribe.requests;

import babel.requestreply.ProtocolRequest;
import utils.Membership;

public class StartRequest extends ProtocolRequest {
public static final short REQUEST_ID=505;
private Membership membership;
private int initState;

    public StartRequest(int initState ,Membership membership) {
        super(StartRequest.REQUEST_ID);
        this.initState=initState;
        this.membership=membership;
    }
    public int getInitState(){
        return initState;
    }
    public Membership getMembership(){
        return membership;
    }
}
