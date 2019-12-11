package publishsubscribe.requests;

import babel.requestreply.ProtocolRequest;
import utils.Operation;

public class OperationRequest extends ProtocolRequest {
    public static final short REQUEST_ID = 503;
    private Operation op;

    public OperationRequest(Operation op) {
        super(OperationRequest.REQUEST_ID);
        this.op=op;
    }

    public Operation getOp() {
        return op;
    }
}
