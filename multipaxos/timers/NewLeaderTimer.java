package multipaxos.timers;

import babel.timer.ProtocolTimer;

public class NewLeaderTimer extends ProtocolTimer {

    public static final short TimerCode = 201;

    public NewLeaderTimer(){
        super(NewLeaderTimer.TimerCode);
    }
    @Override
    public Object clone() {
        return this;
    }
}
