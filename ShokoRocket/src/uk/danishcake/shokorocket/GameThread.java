package uk.danishcake.shokorocket;

import java.util.concurrent.Semaphore;

import uk.danishcake.shokorocket.moding.GameStateMachine;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.SurfaceHolder;


public class GameThread extends Thread {
	private SurfaceHolder mSurfaceHolder;
	private boolean mRunning = true;
	private GameStateMachine mGame;
	private Semaphore mSemaphore;
	
	public GameThread(SurfaceHolder surfaceHolder, Handler handler, Context context, GameStateMachine game, Semaphore semaphore)
	{
		mSurfaceHolder = surfaceHolder;
		mSemaphore = semaphore;
		mGame = game;
		mGame.setContext(context);
		mGame.setSemaphore(mSemaphore);
	}
	
	public void StopRunning() {mRunning = false;}
	
	@Override
	public void run() {
		while(mRunning)
		{
			long update_start = System.nanoTime();
			//Update game
			try
			{
				mSemaphore.acquire();
				boolean exit = mGame.Tick(20);
				
				if(exit)
				{ //TODO provide a proper exit
					
				}
				
				//Draw
				Canvas canvas = null;
				try
				{
					canvas = mSurfaceHolder.lockCanvas();
					
					synchronized (mSurfaceHolder) {
						canvas.drawRGB(0, 0, 0);
						//Now draw my sprites
						mGame.Redraw(canvas);
					}
				} finally
				{
					if(canvas != null)
						mSurfaceHolder.unlockCanvasAndPost(canvas);
				}
				mSemaphore.release();
			} catch(InterruptedException int_ex)
			{
				
			}
			
			//Sleep for remainder of frame
			long running_time = System.nanoTime() - update_start;
			//16ms frames is the target
			int sleep_time = (int)((20000000L - running_time) / 1000000L);
			if(sleep_time > 0)
			{
				try {
					sleep(sleep_time);
				} catch (InterruptedException e) {
					// This basically means that the thread has been interrupted, so it will 
					// already have already been stopped
				}
			
			}
		}
	}
	
}
