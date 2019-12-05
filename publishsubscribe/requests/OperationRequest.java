package publishsubscribe.requests;

import babel.requestreply.ProtocolRequest;

public class OperationRequest extends ProtocolRequest {
    public static final short REQUEST_ID=506;

    public OperationRequest(){
        super(REQUEST_ID);
    }
}
