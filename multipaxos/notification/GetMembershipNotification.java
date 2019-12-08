package multipaxos.notification;

import java.util.Set;

import babel.notification.ProtocolNotification;
import utils.Node;
import dht.notification.RouteDelivery;
import network.Host;


public class GetMembershipNotification extends ProtocolNotification {
    public final static short NOTIFICATION_ID = 211;
    public final static String NOTIFICATION_NAME = "getMembershipNotification";

    private Host h;
    private Set<Host> replicas;
    private int n;
    
    public GetMembershipNotification(int msgId, Node nodeInterested, int type, int protocol) {

	}

    @Override
    public String toString() {
        return "GetMembershipNotification{" +
                "Host=" + host. +
                "TypeOfAction = " + typeMsg +
                '}';
    }

	public Host getH() {
		return h;
	}

	public void setH(Host h) {
		this.h = h;
	}

	public Set<Host> getReplicas() {
		return replicas;
	}

	public void setReplicas(Set<Host> replicas) {
		this.replicas = replicas;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

}
