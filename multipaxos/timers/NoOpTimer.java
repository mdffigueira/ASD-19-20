package multipaxos.timers;

import babel.timer.ProtocolTimer;

public class NoOpTimer extends ProtocolTimer {

    public static final short TimerCode = 204;

    public NoOpTimer() {
        super(NoOpTimer.TimerCode);
    }

    @Override
    public Object clone() {
        return this;
    }
}
