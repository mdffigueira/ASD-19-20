package dht.timers;

import babel.timer.ProtocolTimer;

public class FixFingerTimer extends ProtocolTimer {
    public static final short TimerCode = 101;

    public FixFingerTimer() {
        super(FixFingerTimer.TimerCode);
    }

    public Object clone() {
        return this;

    }
}
