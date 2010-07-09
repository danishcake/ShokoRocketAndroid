package uk.danishcake.shokorocket;

import uk.danishcake.shokorocket.moding.GameStateMachine;
import uk.danishcake.shokorocket.simulation.Direction;
import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

	private Context mContext;
	private GameThread mThread;
	private GameStateMachine mGame;
	
	public GameView(Context context)
	{
		super(context);
		requestFocus();
		mContext = context;
		mGame = new GameStateMachine(mContext);
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event)
			{
				mGame.HandleTouch(event);
				return true;
			}
		});
		/*
		setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
					mGame.HandleDPad(Direction.South);
				else if(keyCode == KeyEvent.KEYCODE_DPAD_UP)
					mGame.HandleDPad(Direction.North);
				else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
					mGame.HandleDPad(Direction.West);
				else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
					mGame.HandleDPad(Direction.East);
				return false;
			}
		});*/
	} 
	
	public boolean OverrideBack()
	{
		return mGame.HandleBack();
	}
	
	public void HandleDPad(Direction direction)
	{
		mGame.HandleDPad(direction);
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mGame.ScreenChanged(width, height);
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
