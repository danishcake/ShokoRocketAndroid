package uk.danishcake.shokorocket.moding;

import java.util.concurrent.Semaphore;

import uk.danishcake.shokorocket.simulation.Direction;
import android.content.Context;
import android.graphics.Canvas;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Base class representing a state in the {@link GameStateMachine}.
 * One a subclass assigns mPendMode the GameStateMachine will automatically transition to it,
 * and mFade will be set appropriately to represent the alpha level of a black layer drawn over the
 * top of this
 */
public class Mode {
	protected Semaphore mSemaphore = null;
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
	
	/**
	 * Handles a swipe motion on the screen
	 * @param direction
	 */
	public void handleGesture(Direction direction) {
		
	}
	
	/**
	 * Allows a mode to display an indication of what type of gesture is to be performed 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param direction
	 */
	public void previewGesture(int x1, int y1, int x2, int y2, Direction direction)
	{
	
	}
	
	public void clearPreviewGesture()
	{
		
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
	
	/**
	 * Called when mode changes, before Setup. If not overriden then no menu 
	 * will be available.
	 * @return Menu to display for the current mode
	 */
	public boolean getMenu(Menu menu)
	{
		return false;
	}

	/**
	 * Called when a menu item has been selected, allowing modes to handle the selection
	 * @param item
	 * @return
	 */
	public boolean handleMenuSelection(MenuItem item)
	{
		
		return false;
	}
	
	/**
	 * Provides the semaphore to the mode
	 * @param semaphore Used to prevent concurrent access to data from UI and game thread
	 */
	public void setSemaphore(Semaphore semaphore)
	{
		mSemaphore = semaphore;
	}
}