package floodbcast.timers;
import babel.timer.ProtocolTimer;
import hyparview.timers.ShuffleTimer;

public class AntiEntropyTimer extends ProtocolTimer{
public static final short TimerCode = 301;
    public AntiEntropyTimer() {
        super(AntiEntropyTimer.TimerCode);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Object clone() {
        return null;
    }
}
