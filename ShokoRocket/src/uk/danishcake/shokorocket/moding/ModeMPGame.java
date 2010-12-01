package uk.danishcake.shokorocket.moding;

import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.MPWorld;
import uk.danishcake.shokorocket.simulation.Vector2i;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class ModeMPGame extends Mode {
	private ModeMPMenu mModeMenu = null;
	private SkinProgress mSkin = null;
	private MPWorld mWorld = null;
	private GameDrawer mGameDrawer = new GameDrawer();
	
	private Vector2i mCursorPosition = new Vector2i(-1, -1);
	private Vector2i mGestureStart = new Vector2i(0, 0);
	private Vector2i mGestureEnd = new Vector2i(0, 0);
	private Direction mGestureDirection = Direction.Invalid;
	private boolean mGestureInProgress = false;

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
		//TODO DrawMP
		mGameDrawer.DrawMP(canvas, mWorld);
		if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
		{
			mGameDrawer.DrawCursor(canvas, mCursorPosition.x, mCursorPosition.y, true);
		}
		mWidgetPage.Draw(canvas);
		if(mGestureInProgress)
		{
			Paint linePaint = new Paint();
			linePaint.setARGB(255, 255, 0, 0);
			linePaint.setStrokeWidth(6);

			canvas.drawLine(mGestureStart.x, mGestureStart.y, mGestureEnd.x, mGestureEnd.y, linePaint);
			Bitmap frame = mGameDrawer.GetArrow(mGestureDirection).getCurrentFrame();
			if(mGestureDirection != Direction.Invalid)
				canvas.drawBitmap(frame, (mGestureStart.x + mGestureEnd.x) / 2 - frame.getWidth() / 2, (mGestureStart.y + mGestureEnd.y) / 2 - frame.getHeight() / 2, null);
		}
		super.Redraw(canvas);
	}
	
	@Override
	public boolean handleBack() {
		mPendMode = mModeMenu;
		return true;
	}
}
