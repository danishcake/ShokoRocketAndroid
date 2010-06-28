package uk.danishcake.shokorocket.moding;

import android.graphics.Canvas;

public class GameStateMachine {
	private Mode mMode;

	public GameStateMachine()
	{
		mMode = new ModeIntro();
		mMode.Setup();
	}

	public boolean Tick(int timespan) {
		ModeAction action = mMode.Tick(timespan);
		if(action == ModeAction.ChangeMode)
		{
			mMode = mMode.Teardown();
			mMode.Setup();
		} else if(action == ModeAction.Exit)
		{
			return true;
		}
		return false;
	}
	
	public void Redraw(Canvas canvas) {
		mMode.Redraw(canvas);
	}

}