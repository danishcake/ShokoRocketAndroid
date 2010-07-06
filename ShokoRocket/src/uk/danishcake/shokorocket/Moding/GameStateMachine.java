package uk.danishcake.shokorocket.moding;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

public class GameStateMachine {
	private Mode mMode;
	private Context mContext;
	private int mScreenWidth = 240;
	private int mScreenHeight = 320;

	public GameStateMachine(Context context)
	{
		mContext = context;
		mMode = new ModeIntro();
		mMode.Setup(mContext);
	}

	public boolean Tick(int timespan) {
		ModeAction action = mMode.Tick(timespan);
		if(action == ModeAction.ChangeMode)
		{
			mMode = mMode.Teardown();
			mMode.ScreenChanged(mScreenWidth, mScreenHeight);
			mMode.Setup(mContext);
		} else if(action == ModeAction.Exit)
		{
			return true;
		}
		return false;
	}
	
	public void ScreenChanged(int width, int height) {
		mMode.ScreenChanged(width, height);
		mScreenWidth = width;
		mScreenHeight = height;
	}
	
	public void Redraw(Canvas canvas) {
		mMode.Redraw(canvas);
	}
	
	public void HandleTouch(MotionEvent event) {
		//Recognise gestures here and pass to modes
		if(event.getAction() == MotionEvent.ACTION_UP && (event.getEventTime() - event.getDownTime()) < 500)
		{
			mMode.handleTap((int)event.getX(), (int)event.getY());
		}
		
	}

	public void setContext(Context context) {
		mContext = context;
	}
}