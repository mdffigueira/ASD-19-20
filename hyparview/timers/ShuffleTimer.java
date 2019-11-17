package hyparview.timers;

import babel.timer.ProtocolTimer;

public class ShuffleTimer extends ProtocolTimer {
	public static final short TimerCode = 401;
	public ShuffleTimer() {
		super(ShuffleTimer.TimerCode);
		// TODO Auto-generated constructor stub
	}
	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
