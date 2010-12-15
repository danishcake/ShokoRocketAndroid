package uk.danishcake.shokorocket.moding;

import java.io.IOException;

import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.simulation.SPWorld;
import uk.danishcake.shokorocket.sound.SoundManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ModeTutorial extends Mode {
	private Widget mNextButton = null;
	private Widget mExplanation = null;
	
	private ModeMenu mModeMenu = null;
	private SPWorld mWorld = null;
	private GameDrawer mGameDrawer = new GameDrawer();
	
	private Vector2i mCursorPosition = new Vector2i(-1, -1);
	private int mSwipeStart = 0;
	private static final int SwipeTime = 750;
	private boolean mRunning = false;
	private int mRotateStart = 0;
	private float mRotateAngle = 0;
	private float mRotateScale = 0;
	private static final float RotateTime = 400.0f;
	
	private int mTutorialStage = 0;
	private OnClickListener[] mTutorialStages = {new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText(mContext.getString(R.string.tutorial_1));
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText(mContext.getString(R.string.tutorial_2));
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText(mContext.getString(R.string.tutorial_3));
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText(mContext.getString(R.string.tutorial_4));
			mCursorPosition.x = 1;
			mCursorPosition.y = 4;
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText(mContext.getString(R.string.tutorial_5));
			
			mSwipeStart = mAge;
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText(mContext.getString(R.string.tutorial_6));
			if(mAge < mSwipeStart + SwipeTime)
			{
				mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.East);
			}
			mSwipeStart = 0;
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText(mContext.getString(R.string.tutorial_7));
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText(mContext.getString(R.string.tutorial_8));
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			mRunning = true;
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			try {
				mWorld = new SPWorld(mContext.getAssets().open("TutorialLevels/Tut2.Level"));
				mGameDrawer.CreateBackground(mWorld);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mCursorPosition.x = -1;
			mExplanation.setText(mContext.getString(R.string.tutorial_9));
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText(mContext.getString(R.string.tutorial_10));
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			try {
				mWorld = new SPWorld(mContext.getAssets().open("TutorialLevels/Tut3.Level"));
				mGameDrawer.CreateBackground(mWorld);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mCursorPosition.x = -1;
			mExplanation.setText(mContext.getString(R.string.tutorial_11));
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			try {
				mWorld = new SPWorld(mContext.getAssets().open("TutorialLevels/Tut4.Level"));
				mWorld.LoadSolution();
				mGameDrawer.CreateBackground(mWorld);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mExplanation.setText(mContext.getString(R.string.tutorial_12));
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			if(mPendMode == null)
			{
				mExplanation.setText(mContext.getString(R.string.tutorial_13));
				mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
				mRotateStart = mAge;
				mWorld.Reset();
				mRunning = false;
				mGameDrawer.CreateCacheBitmap(mWorld);
				SoundManager.PlaySound(mClickSound);
			}
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			if(mPendMode == null)
			{
				mExplanation.setText(mContext.getString(R.string.tutorial_14));
				mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
				SoundManager.PlaySound(mClickSound);
			}
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText(mContext.getString(R.string.tutorial_15));
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			mNextButton.setText("Finish");
			SoundManager.PlaySound(mClickSound);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			if(mPendMode == null)
			{
				mPendMode = mModeMenu;
				SoundManager.PlaySound(mClickSound);
			}
		}
	},
	};
	
	public ModeTutorial(ModeMenu menu) {
		mModeMenu = menu;
	}
	
	public void Setup(Context context)
	{
		super.Setup(context);

		try {
			mNextButton = new Widget(mBtnNP, new Rect(mBtnBorder, 
													  mScreenHeight - mBtnBorder - mBtnSize,
													  mScreenWidth - mBtnBorder, 
													  mScreenHeight - mBtnBorder));
			mNextButton.setText("Next");
			mNextButton.setOnClickListener(mTutorialStages[0]);
			
			mExplanation = new Widget(mBtnNP, new Rect(mBtnBorder,
													   mBtnBorder,
													   mScreenWidth - mBtnBorder,
													   mBtnBorder + mBtnSize * 2 + mBtnSep)); 
			mExplanation.setText("How to play ShokoRocket!");
			
			mWidgetPage.setFontSize(mFontSize);
			mWidgetPage.addWidget(mNextButton);
			mWidgetPage.addWidget(mExplanation);
			
			mGameDrawer.Setup(mContext, mGridSize, new SkinProgress(mContext), false);
			mWorld = new SPWorld(mContext.getAssets().open("TutorialLevels/Tut1.Level"));
			mGameDrawer.setDrawOffset((mScreenWidth - mWorld.getWidth() * mGameDrawer.getGridSize()) / 2, mBtnBorder + mBtnSize * 2 + mBtnSep * 2);
			mGameDrawer.CreateBackground(mWorld);
		} catch(IOException io_ex) {
			//TODO log
		}
		
	}

	public Mode Teardown()
	{
		return mPendMode;
	}

	public ModeAction Tick(int timespan)
	{
		mGameDrawer.Tick(timespan);
		if(mRunning)
		{
			mWorld.Tick(timespan * 5);
		}
		
		if(mRotateStart > 0)
		{
			if(mAge > mRotateStart + RotateTime / 2 && mWorld.getRotation() == 0)
			{
				mWorld.RotateRight();
				mGameDrawer.CreateCacheBitmap(mWorld);
				mGameDrawer.CreateBackground(mWorld);
			}
			if(mAge > mRotateStart + RotateTime)
			{
				mRotateStart = 0;
				mRunning = true;
			}
			
			if(mAge <= mRotateStart + RotateTime / 2)
			{
				mRotateAngle = 90 * ((float)(mAge - mRotateStart)) / RotateTime;
				mRotateScale = 1.0f - ((float)(mAge - mRotateStart)) / RotateTime;
			}
			if(mAge > mRotateStart + RotateTime / 2)
			{
				mRotateAngle = -90 + 90 * ((float)(mAge - mRotateStart)) / RotateTime;
				mRotateScale = ((float)(mAge - mRotateStart)) / RotateTime;
			}
		}
		

		
		return super.Tick(timespan);
	}
	
	public void Redraw(Canvas canvas)
	{
		if(mRotateStart > 0)
		{
			mGameDrawer.DrawCacheBitmap(canvas, mRotateAngle, mRotateScale);
		} else
		{
			mGameDrawer.DrawSP(canvas, mWorld);
		}
		mWidgetPage.Draw(canvas);
		if(mCursorPosition.x != -1)
		{
			mGameDrawer.DrawCursor(canvas, mCursorPosition.x, mCursorPosition.y, mRunning);
		}
		if(mAge > mSwipeStart + SwipeTime && mSwipeStart > 0)
		{
			mSwipeStart = 0;
			mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.East);
		}
		if(mSwipeStart > 0)
		{
			Paint linePaint = new Paint();
			linePaint.setARGB(255, 255, 0, 0);
			linePaint.setStrokeWidth(6);
			
			float end_x = (float)mBtnBorder + (float)(mScreenWidth - mBtnBorder * 2) * (float)(mAge - mSwipeStart) / (float)SwipeTime;  
			canvas.drawLine(mBtnBorder, mScreenHeight / 2, end_x, mScreenHeight / 2, linePaint);
			
			float mid_x = (float)mBtnBorder + (float)(mScreenWidth - mBtnBorder * 2) * (float)(mAge - mSwipeStart) / (2.0f * (float)SwipeTime);
			Bitmap arrow = mGameDrawer.GetArrow(Direction.East).getCurrentFrame();
			canvas.drawBitmap(arrow, mid_x - arrow.getWidth() / 2, mScreenHeight / 2 - arrow.getHeight() / 2, null);
		}

		super.Redraw(canvas);
	}
}
