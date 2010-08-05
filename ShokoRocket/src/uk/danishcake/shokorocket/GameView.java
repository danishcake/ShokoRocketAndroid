package uk.danishcake.shokorocket;

import java.util.concurrent.Semaphore;

import uk.danishcake.shokorocket.moding.GameStateMachine;
import uk.danishcake.shokorocket.simulation.Direction;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

	private Context mContext;
	private GameThread mThread;
	private GameStateMachine mGame;
	private Semaphore mSemaphore = null;
	
	public GameView(Context context)
	{
		super(context);
		requestFocus();
		mContext = context;
		mGame = new GameStateMachine(mContext);
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		mSemaphore = new Semaphore(1);
		
		setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event)
			{
				try {
					mSemaphore.acquire();
					mGame.HandleTouch(event);
					mSemaphore.release();
				} catch (InterruptedException e) {
					Log.e("GameView.GameView", "Semaphore interupted");
				}
				return true;
			}
		});
	} 
	
	public boolean OverrideBack()
	{
		boolean result = false;
		try {
			mSemaphore.acquire();
			result = mGame.HandleBack();
			mSemaphore.release();
		} catch (InterruptedException e) {
			Log.e("GameView.OverrideBack", "Semaphore interupted");
		}
		return result;
	}
	
	public void HandleDPad(Direction direction)
	{
		try {
			mSemaphore.acquire();
			mGame.HandleDPad(direction);
			mSemaphore.release();
		} catch (InterruptedException e) {
			Log.e("GameView.OverrideBack", "Semaphore interupted");
		}
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {
			mSemaphore.acquire();
			mGame.ScreenChanged(width, height);
			mSemaphore.release();
		} catch (InterruptedException e) {
			Log.e("GameView.OverrideBack", "Semaphore interupted");
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mThread = new GameThread(getHolder(), new Handler(), mContext, mGame, mSemaphore);
		mThread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		mThread.StopRunning();
		boolean retry = true;
		while(retry)
		{
			try
			{
				mThread.join();
				retry = false;
			} catch(InterruptedException e)
			{
				Log.e("GameView.surfaceDestroyed", "Semaphore interupted");
			}
		}
	}
	
	public boolean getMenu(Menu menu) {
		boolean result = false;
		try {
			mSemaphore.acquire();
			result = mGame.getMenu(menu);
			mSemaphore.release();
		} catch (InterruptedException e) {
			Log.e("GameView.getMenu", "Semaphore interupted");
		}
		return result;
	}
	
	public boolean handleMenuSelection(MenuItem item) {
		boolean result = false;
		try {
			mSemaphore.acquire();
			result = mGame.handleMenuSelection(item); 
			mSemaphore.release();
		} catch (InterruptedException e) {
			Log.e("GameView.handleMenuSelected", "Semaphore interupted");
		}
		return result;
	}
}
