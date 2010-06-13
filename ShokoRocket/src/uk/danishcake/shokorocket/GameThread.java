package uk.danishcake.shokorocket;

import uk.danishcake.shokorocket.Moding.GameStateMachine;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.view.SurfaceHolder;


public class GameThread extends Thread {
	private SurfaceHolder mSurfaceHolder;
	private Handler mHandler;
	private Context mContext;
	private boolean mRunning = true;
	private GameStateMachine mGame;
	
	public GameThread(SurfaceHolder surfaceHolder, Handler handler, Context context, GameStateMachine game)
	{
		mSurfaceHolder = surfaceHolder;
		mHandler = handler;
		mContext = context;
		mGame = game;
	}
	
	public void StopRunning() {mRunning = false;}
	
	@Override
	public void run() {
		while(mRunning)
		{
			long update_start = System.nanoTime();
			//Update game
			mGame.Tick(20);
			
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
