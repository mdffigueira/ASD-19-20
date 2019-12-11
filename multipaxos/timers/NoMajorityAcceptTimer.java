package multipaxos.timers;

import babel.timer.ProtocolTimer;

public class NoMajorityAcceptTimer extends ProtocolTimer {
    public static final short TimerCode = 202;

    public NoMajorityAcceptTimer() {
        super(NoMajorityAcceptTimer.TimerCode);
    }

    @Override
    public Object clone() {
        return this;
    }
}
