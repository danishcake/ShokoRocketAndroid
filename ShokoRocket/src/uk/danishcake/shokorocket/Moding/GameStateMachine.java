package uk.danishcake.shokorocket.moding;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import uk.danishcake.shokorocket.animation.BackgroundDrawer;
import uk.danishcake.shokorocket.simulation.Direction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

public class GameStateMachine {
	private Mode mMode;
	private Context mContext;
	private int mScreenWidth = 240;
	private int mScreenHeight = 320;
	
	private int mTapStartX = 0;
	private int mTapStartY = 0;
	boolean mDragInProgress = false;
	private Semaphore mSemaphore = null;

	private BackgroundDrawer mBackground = null;


	public GameStateMachine(Context context)
	{
		mContext = context;
		mMode = new ModeIntro();
		mMode.Setup(mContext);
	}

	public boolean Tick(int timespan) {
		if(mBackground != null)
			mBackground.tick(timespan);
		
		ModeAction action = mMode.Tick(timespan);
		if(action == ModeAction.ChangeMode)
		{
			mMode = mMode.Teardown();
			mMode.ScreenChanged(mScreenWidth, mScreenHeight);
			mMode.setSemaphore(mSemaphore);
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
		mBackground = new BackgroundDrawer(mContext, mScreenWidth, mScreenHeight);
	}
	
	public void Redraw(Canvas canvas) {
		if(mMode.getBackgroundDrawn() && mBackground != null)
		{
			mBackground.draw(canvas);
		}
		mMode.Redraw(canvas);
	}
	
	/**
	 * Called when back pressed.
	 * @return True if default back behaviour to be overriden
	 */
	public boolean HandleBack() {
		return mMode.handleBack();
	}
	
	private Direction detectGesture(int x1, int y1, int x2, int y2)
	{
		int deltaX = x2 - x1;
		int deltaY = y2 - y1;
		int lengthSq = deltaX * deltaX + deltaY * deltaY;
		int lengthReqSq = (mScreenHeight / 8) * (mScreenHeight / 8);

		
		double angularError = Math.cos(Math.PI / 3);
		
		if(lengthSq > lengthReqSq)
		{
			double angle = Math.atan2(deltaX, deltaY);
			double dot_north = Math.sin(angle) * 0 + Math.cos(angle) * -1;
			double dot_east = Math.sin(angle) * 1 + Math.cos(angle) * 0;
			double dot_west = Math.sin(angle) * -1 + Math.cos(angle) * 0;
			double dot_south = Math.sin(angle) * 0 + Math.cos(angle) * 1;
			
			if(dot_north > angularError)
				return Direction.North;
			if(dot_east > angularError)
				return Direction.East;
			if(dot_west > angularError)
				return Direction.West;
			if(dot_south > angularError)
				return Direction.South;
		}
		return Direction.Invalid;
	}
	
	public void HandleTouch(MotionEvent event) {
		final int TapTime = 250;

		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			mTapStartX = (int)event.getX();
			mTapStartY = (int)event.getY();
			mDragInProgress = true;
		}
		
		if(event.getAction() == MotionEvent.ACTION_MOVE && mDragInProgress)
		{
			Direction dir = detectGesture(mTapStartX, mTapStartY, (int)event.getX(), (int)event.getY());
			mMode.previewGesture(mTapStartX, mTapStartY, (int)event.getX(), (int)event.getY(), dir);
		}
		
		//Recognise gestures here and pass to modes
		//If not moved more than a certain length and help shortly then a tap
		//If moved more then a gesture
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			Direction dir = detectGesture(mTapStartX, mTapStartY, (int)event.getX(), (int)event.getY());
			if(dir == Direction.Invalid)
			{
				if(event.getEventTime() - event.getDownTime() < TapTime) 
				{
					mMode.handleTap((int)event.getX(), (int)event.getY());
					mDragInProgress = false;
				}
			} else
			{
				mMode.handleGesture(dir);
			}
		}
		
		if(event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP)
		{
			mDragInProgress = false;
			mMode.clearPreviewGesture();
		}		
	}
	
	public void HandleDPad(Direction direction) {
		mMode.handleDPad(direction);
	}

	public void setContext(Context context) {
		mContext = context;
	}
	
	public boolean getMenu(Menu menu) {
		return mMode.getMenu(menu, true);
	}
	
	public boolean handleMenuSelection(MenuItem item) {
		return mMode.handleMenuSelection(item);
	}
	
	public void setSemaphore(Semaphore semaphore) {
		mSemaphore = semaphore;
	}
}