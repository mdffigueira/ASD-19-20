package publishsubscribe.requests;

import babel.requestreply.ProtocolRequest;
import utils.Membership;

public class StartRequest extends ProtocolRequest {
public static final short REQUEST_ID=505;
private Membership membership;
private int instancePaxos;

    public StartRequest(int instancePaxos ,Membership membership) {
        super(StartRequest.REQUEST_ID);
        this.instancePaxos=instancePaxos;
        this.membership=membership;
    }
    public int getInstancePaxos(){
        return instancePaxos;
    }
    public Membership getMembership(){
        return membership;
    }
}
