package uk.danishcake.shokorocket.moding;

import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.simulation.MPWorld;
import android.content.Context;
import android.graphics.Canvas;

public class ModeMPGame extends Mode {
	private ModeMPMenu mModeMenu = null;
	private SkinProgress mSkin = null;
	private MPWorld mWorld = null;
	private GameDrawer mGameDrawer = new GameDrawer();

	public ModeMPGame(ModeMPMenu menu, SkinProgress skin, MPWorld world) {
		mSkin = skin;
		mModeMenu = menu;
		mWorld = world;
	}
	
	@Override
	public void Setup(Context context) {
		super.Setup(context);
		
		//Setup autoscaling twice, for portrait and landscape orientations
		int required_width = mWorld.getWidth() * mGridSize;
		int required_height = mWorld.getHeight() * mGridSize;
		float scaleX = ((float)mScreenWidth - mLevelBorder * 2) / (float)required_width;
		float scaleY = ((float)(mScreenHeight - mBtnSize - mBtnBorder - mLevelBorder * 2)) / (float)required_height;
		float smaller = scaleX < scaleY ? scaleX : scaleY;
		if(smaller < 1)
			mGameDrawer.Setup(mContext, (int)(((float)mGridSize) * smaller), mSkin);
		else
			mGameDrawer.Setup(mContext, mGridSize, mSkin);

		mGameDrawer.CreateBackground(mWorld);
		mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * mGameDrawer.getGridSize() / 2), mLevelBorder);
		
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		mWidgetPage.Draw(canvas);
		super.Redraw(canvas);
	}
	
	@Override
	public boolean handleBack() {
		mPendMode = mModeMenu;
		return true;
	}
}
