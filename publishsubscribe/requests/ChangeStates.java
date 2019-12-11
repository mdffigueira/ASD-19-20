package publishsubscribe.requests;

import babel.requestreply.ProtocolRequest;
import network.Host;

import java.util.Set;

public class ChangeStates extends ProtocolRequest {
    private Host leader;
    private Set<Host> replicas;
    public static final short REQUEST_ID = 501;

    public ChangeStates(Host leader, Set<Host> replicas) {
        super(ChangeStates.REQUEST_ID);
        this.leader = leader;
        this.replicas = replicas;

    }

    public Host getLeader() {
        return leader;
    }

    public Set<Host> getReplicas() {
        return replicas;
    }
}
