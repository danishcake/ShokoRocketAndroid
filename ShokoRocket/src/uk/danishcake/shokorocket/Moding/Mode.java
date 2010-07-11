package uk.danishcake.shokorocket.moding;

import uk.danishcake.shokorocket.simulation.Direction;
import android.content.Context;
import android.graphics.Canvas;

/**
 * Base class representing a state in the {@link GameStateMachine}.
 * One a subclass assigns mPendMode the GameStateMachine will automatically transition to it,
 * and mFade will be set appropriately to represent the alpha level of a black layer drawn over the
 * top of this
 */
public class Mode {
	protected Mode mPendMode = null;
	protected int mPendTime = 500;
	protected int mPendTimer = 0; 
	protected int mAge = 0;
	protected int mFade = 255;
	protected int mFadeR = 255;
	protected int mFadeB = 255;
	protected int mFadeG = 255;
	
	protected int mScreenWidth = 240;
	protected int mScreenHeight = 320;

	/**
	 * Called before the first Tick to allow creation of state, after the previous 
	 * Mode has had Teardown called
	 */
	public void Setup(Context context)
	{
	}

	/**
	 * Called at the end of the modes life, before Setup is called on the successor. 
	 */
	public Mode Teardown()
	{
		return mPendMode;
	}

	/**
	 * Called regularly while the state is active
	 */
	public ModeAction Tick(int timespan)
	{
		mAge += timespan;
		if(mPendMode != null)
		{
			mPendTimer += timespan;
		}

		if(mAge < mPendTime)
			mFade = 255 - (255 * mAge / mPendTime);
		else
			mFade = 255 * mPendTimer / mPendTime;
		if(mFade > 255) mFade = 255;
		if(mFade < 0) mFade = 0;
		
		if(mPendTimer > mPendTime)
			return ModeAction.ChangeMode;
		return ModeAction.NoAction;
	}

	/**
	 * Called regularly while the state is active, with the canvas blank
	 */
	public void Redraw(Canvas canvas)
	{
		if(mFade > 0)
			canvas.drawARGB(mFade, mFadeR, mFadeG, mFadeB);
	}
	
	/**
	 * Called when the screen is tapped
	 * @param x the x position of the event
	 * @param y the y position of the event
	 */
	public void handleTap(int x, int y)	{
	
	}
	
	/**
	 * Called when back pressed.
	 * @return True if default behaviour to be overriden
	 */
	public boolean handleBack() {
		return false;
	}
	
	/**
	 * Handles the direction pad input
	 */
	public void handleDPad(Direction direction) {
		
	}
	
	public void handleGesture(Direction direction) {
		
	}
	
	
	/**
	 * ScreenChanged
	 * Called when the screen geometry changes, or just before Setup
	 * @param width
	 * @param height
	 */
	public void ScreenChanged(int width, int height)
	{
		mScreenWidth = width;
		mScreenHeight = height;
	}
}