package uk.danishcake.shokorocket.moding;

import java.io.IOException;

import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.simulation.World;
import uk.danishcake.shokorocket.simulation.World.WorldState;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

public class ModeGame extends Mode {
	private Context mContext;
	private WidgetPage mWidgetPage = new WidgetPage();
	private GameDrawer mGameDrawer = new GameDrawer();
	private World mWorld;
	private ModeMenu mModeMenu;
	private int mResetTimer = 0;
	private static final int ResetTime = 1500;
	
	enum RunningMode { Stopped, Running, RunningFast }
	private RunningMode mRunningMode = RunningMode.Stopped;
	
	public ModeGame(World world, ModeMenu menu)
	{
		mWorld = world;
		mModeMenu = menu;
	}
	
	@Override
	public void Setup(Context context) {
		mContext = context;
		//Setup autoscaling?
		int required_width = mWorld.getWidth() * 32;
		float scale = mScreenWidth / (float)required_width;
		if(required_width > mScreenWidth)
			mGameDrawer.Setup(mContext, (int)(32 * scale));
		else
			mGameDrawer.Setup(mContext, 32);
		mGameDrawer.CreateBackground(mWorld);
		mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * mGameDrawer.getGridSize() / 2), 16);
		
		//Setup widgets
		NinePatchData btn_np;
		try {
			btn_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/Blank64x64.png")), 24, 24, 24, 24);
			
			Widget reset = new Widget(btn_np, new Rect(mScreenWidth - 64 - 16, mScreenHeight - 64, mScreenWidth - 16, mScreenHeight - 16));
			reset.setText("Reset");
			reset.setOnClickListener(new OnClickListener() {				
				@Override
				public void OnClick(Widget widget) {
					if(mRunningMode == RunningMode.Stopped)
					{
						mWorld.Reset();
						mWorld.ClearArrows();
					}
					else if(mRunningMode == RunningMode.Running || mRunningMode == RunningMode.RunningFast)
					{
						mWorld.Reset();
						mRunningMode = RunningMode.Stopped;
					}
				}
			});
			
			Widget go = new Widget(btn_np, new Rect(mScreenWidth - 64 - 64 - 4 - 16, mScreenHeight - 64, mScreenWidth - 64 - 4 - 16, mScreenHeight - 16));
			go.setText("Go");
			go.setOnClickListener(new OnClickListener() {				
				@Override
				public void OnClick(Widget widget) {
					if(mRunningMode == RunningMode.Stopped)
						mRunningMode = RunningMode.Running;
					else if(mRunningMode == RunningMode.Running)
						mRunningMode = RunningMode.RunningFast;
				}
			});
			
			
			Widget back = new Widget(btn_np, new Rect(16, mScreenHeight - 64, 16 + 64, mScreenHeight - 16));
			back.setText("Back");
			back.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					mPendMode = mModeMenu;
				}
			});
			
			mWidgetPage.addWidget(reset);
			mWidgetPage.addWidget(go);
			mWidgetPage.addWidget(back);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public Mode Teardown() {
		return super.Teardown();
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		final int TimeRate = 5;
		int rate = 1;
		switch(mRunningMode)
		{
		case Stopped:
			break;
		case RunningFast:
			rate = 3; //Fall through
		case Running:
			mWorld.Tick(timespan * rate * TimeRate);
			WorldState state = mWorld.getWorldState();
			if(state == WorldState.Failed)
			{
				mResetTimer += timespan;
				if(mResetTimer > ResetTime)
				{
					mWorld.Reset();
					mRunningMode = RunningMode.Stopped;
				}
			} else
				mResetTimer = 0;
			break;
		}
		
		mWidgetPage.Tick(timespan);
		mGameDrawer.Tick(timespan);
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		mGameDrawer.Draw(canvas, mWorld);
		mWidgetPage.Draw(canvas);
		super.Redraw(canvas);
	}
	
	@Override
	public void handleTap(int x, int y) {
		mWidgetPage.handleTap(x, y);
	}
	
	@Override
	public boolean handleBack() {
		mWorld.Reset();
		mWorld.ClearArrows();
		mPendMode = mModeMenu;
		return true;
	}
}
