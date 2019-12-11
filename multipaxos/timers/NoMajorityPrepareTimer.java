package multipaxos.timers;

import babel.timer.ProtocolTimer;

public class NoMajorityPrepareTimer extends ProtocolTimer {

    public static final short TimerCode = 203;

    public NoMajorityPrepareTimer(){
        super(NoMajorityPrepareTimer.TimerCode);
    }
    @Override
    public Object clone() {
        return this;
    }
}
