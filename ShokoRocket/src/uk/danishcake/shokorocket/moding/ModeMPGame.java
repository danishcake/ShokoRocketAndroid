package uk.danishcake.shokorocket.moding;

import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.MPWorld;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.sound.SoundManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ModeMPGame extends Mode {
	private ModeMPMenu mModeMenu = null;
	private SkinProgress mSkin = null;
	private MPWorld mWorld = null;
	private GameDrawer mGameDrawer = new GameDrawer();
	Widget[] mScoreWidgets = new Widget[4];
	
	private Vector2i mCursorPosition = new Vector2i(-1, -1);
	private Vector2i mGestureStart = new Vector2i(0, 0);
	private Vector2i mGestureEnd = new Vector2i(0, 0);
	private Direction mGestureDirection = Direction.Invalid;
	private boolean mGestureInProgress = false;
	private int mPlayerID = -1;

	public ModeMPGame(ModeMPMenu menu, SkinProgress skin, MPWorld world) {
		mSkin = skin;
		mModeMenu = menu;
		mWorld = world;
	}
	
	@Override
	public void Setup(Context context) {
		super.Setup(context);
		
		NinePatchData np0 = new NinePatchData(BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.blank_mpbutton0)), mNPBorder, mNPBorder, mNPBorder, mNPBorder);
		NinePatchData np1 = new NinePatchData(BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.blank_mpbutton1)), mNPBorder, mNPBorder, mNPBorder, mNPBorder);
		NinePatchData np2 = new NinePatchData(BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.blank_mpbutton2)), mNPBorder, mNPBorder, mNPBorder, mNPBorder);
		NinePatchData np3 = new NinePatchData(BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.blank_mpbutton3)), mNPBorder, mNPBorder, mNPBorder, mNPBorder);
		
		mScoreWidgets[0] = new Widget(np0, new Rect(mBtnBorder, mScreenHeight - mBtnSize - mBtnBorder, 
													mScreenWidth / 4 - mBtnSep / 2, mScreenHeight - mBtnBorder));
		mScoreWidgets[1] = new Widget(np1, new Rect(mScreenWidth /4 + mBtnSep / 2, mScreenHeight - mBtnSize - mBtnBorder, 
													mScreenWidth / 2 - mBtnSep / 2, mScreenHeight - mBtnBorder));
		mScoreWidgets[2] = new Widget(np2, new Rect(mScreenWidth / 2 + mBtnSep / 2, mScreenHeight - mBtnSize - mBtnBorder, 
													mScreenWidth * 3 / 4 - mBtnSep / 2, mScreenHeight - mBtnBorder));
		mScoreWidgets[3] = new Widget(np3, new Rect(mScreenWidth * 3 / 4 + mBtnSep / 2, mScreenHeight - mBtnSize - mBtnBorder, 
													mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder));
		mWidgetPage.setFontSize(mFontSize);
		mWidgetPage.addWidget(mScoreWidgets[0]);
		mWidgetPage.addWidget(mScoreWidgets[1]);
		mWidgetPage.addWidget(mScoreWidgets[2]);
		mWidgetPage.addWidget(mScoreWidgets[3]);
		
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
		mWorld.Tick(timespan);
		updateScores();
		if(mPlayerID == -1) mPlayerID = mWorld.getPlayerID();
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		//TODO DrawMP
		mGameDrawer.DrawMP(canvas, mWorld);
		Vector2i[] cursors = mWorld.getCursorPositions();
		for(int i = 0; i < 4; i++)
		{
			if(i != mPlayerID)
			{
				if(cursors[i].x != -1 && cursors[i].y != -1)
					mGameDrawer.drawMPCursor(canvas, cursors[i].x, cursors[i].y, i);
			}
		}
		//Draw player cursor last so on top. Use local value for responsiveness
		if(mPlayerID != -1 && cursors[mPlayerID].x != -1 && cursors[mPlayerID].y != -1)
			mGameDrawer.drawMPCursor(canvas, mCursorPosition.x, mCursorPosition.y, mPlayerID);
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
	public void handleTap(int x, int y) {
		mWidgetPage.handleTap(x, y);
		Vector2i offset = mGameDrawer.getDrawOffset();
		int grid_x = (x - offset.x) / mGameDrawer.getGridSize();
		int grid_y = (y - offset.y) / mGameDrawer.getGridSize();
		if(grid_x >= 0 && grid_y >= 0 && grid_x < mWorld.getWidth() && grid_y < mWorld.getHeight())
		{
			mCursorPosition.x = grid_x;
			mCursorPosition.y = grid_y;
			SoundManager.PlaySound(mClickSound);
			mWorld.cursorPlacement(mCursorPosition.x, mCursorPosition.y);
		}
	}
	
	@Override
	public void handleDPad(Direction direction) {
		if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
		{
			switch(direction)
			{
			case North:
				mCursorPosition.y--;
				if (mCursorPosition.y < 0)
					mCursorPosition.y = 0;
				break;
			case South:
				mCursorPosition.y++;
				if (mCursorPosition.y >= mWorld.getHeight())
					mCursorPosition.y = mWorld.getHeight() - 1;
				break;
			case West:
				mCursorPosition.x--;
				if (mCursorPosition.x < 0)
					mCursorPosition.x = 0;
				break;
			case East:
				mCursorPosition.x++;
				if (mCursorPosition.x >= mWorld.getWidth())
					mCursorPosition.x = mWorld.getWidth() - 1;
				break;
			}
			mWorld.cursorPlacement(mCursorPosition.x, mCursorPosition.y);
		}
	}
	
	@Override
	public void handleGesture(Direction direction) {
		if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
		{
			switch(direction)
			{
			case North:
			case South:
			case West:
			case East:
				SoundManager.PlaySound(mClickSound);
				mWorld.arrowPlacement(mCursorPosition.x, mCursorPosition.y, direction);
				break;
			}
		}
	}
	
	@Override
	public void previewGesture(int x1, int y1, int x2, int y2,
			Direction direction) {
		mGestureInProgress = true;
		mGestureStart.x = x1;
		mGestureStart.y = y1;
		
		mGestureEnd.x = x2;
		mGestureEnd.y = y2;
		mGestureDirection = direction;
	}
	
	@Override
	public void clearPreviewGesture() {
		mGestureInProgress = false;
	}
	
	@Override
	public boolean handleBack() {
		mPendMode = mModeMenu;
		return true;
	}

	private void updateScores() {
		//TODO maybe cache ltv and only update on change?
		int[] scores = mWorld.getPlayerScores();
		for(int i = 0; i < 4; i++)
		{
			mScoreWidgets[i].setText(Integer.toString(scores[i]));
		}
	}
}
