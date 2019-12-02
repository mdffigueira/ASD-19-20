package dissemination.timers;

import babel.timer.ProtocolTimer;
import dht.Node;

public class SubscriptionTimeLimit extends ProtocolTimer {
    public static final short TimerCode =901;
    private int msgId;
    private Node nodeToRemove;

    public SubscriptionTimeLimit(int msgId, Node nodeToRemove){
        super(SubscriptionTimeLimit.TimerCode);
        this.msgId=msgId;
        this.nodeToRemove=nodeToRemove;
    }
    public int getMsgId(){
        return msgId;
    }
    public Node getNodeToRemove(){
        return nodeToRemove;
    }
    public Object clone(){
        return this;
    }
}