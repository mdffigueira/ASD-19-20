package dht.timers;

import babel.timer.ProtocolTimer;

public class StabilizeTimer extends ProtocolTimer {
    public static final short TimerCode = 102;

    public StabilizeTimer() {
        super(StabilizeTimer.TimerCode);
    }

    public Object clone() {
        return this;
    }
}
