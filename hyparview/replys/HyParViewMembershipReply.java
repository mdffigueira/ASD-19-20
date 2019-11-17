package hyparview.replys;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import babel.requestreply.ProtocolReply;
import hyparview.requests.HyParViewMembershipRequest;
import network.Host;

public class HyParViewMembershipReply extends ProtocolReply {

    public static final short REPLY_ID = HyParViewMembershipRequest.REQUEST_ID;

    private final UUID requestID;
    private final Set<Host> activeView;

	public HyParViewMembershipReply(UUID requestID, Set<Host> activeView) {
        super(HyParViewMembershipReply.REPLY_ID);
        this.requestID = requestID;
        this.activeView = new HashSet<>(activeView);
    }

    public UUID getRequestID() {
        return requestID;
    }

    public Set<Host> getPeers() {
        return this.activeView;
    }
}
