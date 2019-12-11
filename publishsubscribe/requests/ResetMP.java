package publishsubscribe.requests;
import babel.requestreply.ProtocolRequest;

public class ResetMP extends ProtocolRequest {
    public static final short REQUEST_ID = 507;

    public ResetMP() {
        super(ResetMP.REQUEST_ID);
    }
}
