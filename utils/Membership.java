package utils;

import network.Host;

import java.util.Set;

public class Membership {
    private Host currLeader;
    private int seqNumber;
    private Set<Host> replicas;

    public Membership(Host currLeader, int seqNumber,Set<Host>replicas) {
        this.currLeader = currLeader;
        this.replicas = replicas;
        this.seqNumber = seqNumber;
    }

    public Host getCurrLeader() {
        return currLeader;

    }

    public Set<Host> getReplicas(){
        return replicas;
    }
    public int getSeqNumber() {
        return seqNumber;
    }
}
