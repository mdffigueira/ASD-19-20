package utils;

import network.Host;

public class Membership {
    private Host currLeader;
    private int seqNumber;

    public Membership(Host currLeader, int seqNumber) {
        this.currLeader = currLeader;
        this.seqNumber = seqNumber;
    }

    public Host getCurrLeader() {
        return currLeader;

    }

    public int getSeqNumber() {
        return seqNumber;
    }
}
