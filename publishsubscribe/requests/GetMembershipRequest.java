package publishsubscribe.requests;

import babel.requestreply.ProtocolRequest;
import utils.Membership;

public class GetMembershipRequest extends ProtocolRequest {
public static final short REQUEST_ID=506;

    public GetMembershipRequest() {
        super(GetMembershipRequest.REQUEST_ID);
    }
    
}
