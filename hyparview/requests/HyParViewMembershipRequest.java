package hyparview.requests;

import java.util.UUID;

import babel.requestreply.ProtocolRequest;

public class HyParViewMembershipRequest extends ProtocolRequest {
	

	public static final short REQUEST_ID = 401;
	private UUID identifier;

	public HyParViewMembershipRequest() {
		super(HyParViewMembershipRequest.REQUEST_ID);
		this.identifier = UUID.randomUUID();
	}

	public HyParViewMembershipRequest(UUID identifier ) {
		super(HyParViewMembershipRequest.REQUEST_ID);
		this.identifier = identifier;
	}

	public UUID getIdentifier() {
		return identifier;
	}
}