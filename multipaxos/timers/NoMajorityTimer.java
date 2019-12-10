package multipaxos.timers;

import babel.timer.ProtocolTimer;

public class NoMajorityTimer extends ProtocolTimer {

    public static final short TimerCode = 202;

    public NoMajorityTimer(){
        super(NoMajorityTimer.TimerCode);
    }
    @Override
    public Object clone() {
        return null;
    }
}
