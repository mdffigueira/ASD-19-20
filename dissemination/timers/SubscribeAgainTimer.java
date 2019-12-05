package dissemination.timers;

import babel.timer.ProtocolTimer;
import utils.Message;

public class SubscribeAgainTimer extends ProtocolTimer {
    public static final short TimerCode = 902;
    private int msgId;
    private Message msg;
    public SubscribeAgainTimer(int msgId, Message msg) {
        super(SubscribeAgainTimer.TimerCode);
        this.msgId = msgId;
        this.msg = msg;
    }
    public int getMsgId(){
        return msgId;
    }
    public Message getMsg(){
        return msg;
    }
    @Override
    public Object clone() {
        return this;
    }
}
