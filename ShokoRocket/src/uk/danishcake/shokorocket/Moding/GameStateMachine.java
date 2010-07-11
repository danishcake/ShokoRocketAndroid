package uk.danishcake.shokorocket.moding;

import uk.danishcake.shokorocket.simulation.Direction;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

public class GameStateMachine {
	private Mode mMode;
	private Context mContext;
	private int mScreenWidth = 240;
	private int mScreenHeight = 320;
	
	private int mTapStartX = 0;
	private int mTapStartY = 0;
	boolean mDragInProgress = false;

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
	
	/**
	 * Called when back pressed.
	 * @return True if default back behaviour to be overriden
	 */
	public boolean HandleBack() {
		return mMode.handleBack();
	}
	
	public void HandleTouch(MotionEvent event) {
		final int TapTime = 250;
		//final int IgnoreTime = 1500;
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			mTapStartX = (int)event.getX();
			mTapStartY = (int)event.getY();
			mDragInProgress = true;
		}
		
		
		
		//Recognise gestures here and pass to modes
		//If not moved more than 14px and less than 250ms then a tap
		//If moved more then a gesture
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			int deltaX = (int)event.getX() - mTapStartX;
			int deltaY = (int)event.getY() - mTapStartY;
			int lengthSq = deltaX * deltaX + deltaY * deltaY;
			int tapLengthMaxSq = (mScreenWidth / 12) * (mScreenWidth / 12) * 2;
			if(lengthSq < tapLengthMaxSq)
			{
				if(event.getEventTime() - event.getDownTime() < TapTime) 
				{
					mMode.handleTap((int)event.getX(), (int)event.getY());
					mDragInProgress = false;
				}
			} else
			{
				int lengthReqSq = (mScreenHeight / 4) * (mScreenHeight / 4);
				int shortReqX = mScreenWidth / 8;
				int shortReqY = mScreenHeight / 8;
				
				if(lengthSq > lengthReqSq)
				{
					if(deltaX < shortReqX && deltaX > -shortReqX)
					{
						if(deltaY < 0)
							mMode.handleGesture(Direction.North);
						else
							mMode.handleGesture(Direction.South);
							
					}
					if(deltaY < shortReqY && deltaY > -shortReqY)
					{
						if(deltaX < 0)
							mMode.handleGesture(Direction.West);
						else
							mMode.handleGesture(Direction.East);
					}
				}				
			}
		}
		
		if(event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP)
		{
			mDragInProgress = false;
		}		
	}
	
	public void HandleDPad(Direction direction) {
		mMode.handleDPad(direction);
	}

	public void setContext(Context context) {
		mContext = context;
	}
}