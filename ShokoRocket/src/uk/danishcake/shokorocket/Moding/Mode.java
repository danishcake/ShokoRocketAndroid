package uk.danishcake.shokorocket.Moding;

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
	protected int mFade = 0;

	/**
	 * Called before the first Tick to allow creation of state, after the previous 
	 * Mode has had Teardown called
	 */
	public void Setup()
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

		if(mAge <  mPendTime)
			mFade = 255 - (255 * mAge / mPendTime);
		else
			mFade = 255 * mPendTimer / mPendTime;
		if(mFade > 255) mFade = 255;
		if(mFade < 0) mFade = 0;
		return ModeAction.NoAction;
	}

	/**
	 * Called regularly while the state is active, with the canvas blank
	 */
	public void Redraw(Canvas canvas)
	{
	}

}