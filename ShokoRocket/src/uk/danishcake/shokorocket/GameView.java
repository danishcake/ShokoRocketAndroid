package uk.danishcake.shokorocket;

import uk.danishcake.shokorocket.Moding.GameStateMachine;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

	private Context mContext;
	private GameThread mThread;
	private GameStateMachine mGame;
	
	public GameView(Context context)
	{
		super(context);
		mContext = context;
		mGame = new GameStateMachine();
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		//mGame.SizeChanged(width, height);
	}


	public void surfaceCreated(SurfaceHolder holder) {
		mThread = new GameThread(getHolder(), new Handler(), mContext, mGame);
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
			}
		}
	}
	
}
