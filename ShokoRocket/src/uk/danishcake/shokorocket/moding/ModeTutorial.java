package uk.danishcake.shokorocket.moding;

import java.io.IOException;
import java.io.InputStream;

import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.moding.ModeGame.RunningMode;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.simulation.World;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ModeTutorial extends Mode {
	private boolean mFirstRun = false;
	
	private int mBtnSize = 48;
	private int mBtnSizeWide = 64;
	private int mBtnSep = 8;
	private int mBtnBorder = 16;
	private int mFontSize = 16;
	
	private WidgetPage mWidgetPage = new WidgetPage();
	private Widget mNextButton = null;
	private Widget mExplanation = null;
	
	private Context mContext = null;
	private World mWorld = null;
	private GameDrawer mGameDrawer = new GameDrawer();
	
	private Vector2i mCursorPosition = new Vector2i(-1, -1);
	private int mSwipeStart = 0;
	private static final int SwipeTime = 750;
	private boolean mRunning = false;
	
	private int mTutorialStage = 0;
	private OnClickListener[] mTutorialStages = {new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText("You must save the mice from the evil space cats");
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText("The mice are not very clever, so you need to direct them to the rockets");
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText("Do this by placing arrows for them to follow");
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText("Place the cursor by tapping the spot where the arrow should go...");
			mCursorPosition.x = 1;
			mCursorPosition.y = 4;
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText("Then either tap one of the arrow buttons, or swipe across the screen in the direction you want");
			
			mSwipeStart = mAge;
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText("You only have a limited stock of arrows, so think carefully before placing them");
			if(mAge < mSwipeStart + SwipeTime)
			{
				mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.East);
			}
			mSwipeStart = 0;
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText("You can always remove them the same way they were placed, or use the reset button to clear all the arrows");
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText("Tap start once the arrows are in place. If you don't have the answer then hit reset and try again");
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			mRunning = true;
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			try {
				mWorld = new World(mContext.getAssets().open("TutorialLevels/Tut2.Level"));
				mGameDrawer.CreateBackground(mWorld);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mCursorPosition.x = -1;
			mExplanation.setText("You need to prevent mice from falling into black holes, and stop cats reaching the rocket");
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			mExplanation.setText("Note that problem points are highlighted with a circle. Just hit reset and try again!");
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			try {
				mWorld = new World(mContext.getAssets().open("TutorialLevels/Tut3.Level"));
				mWorld.LoadSolution();
				mGameDrawer.CreateBackground(mWorld);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mExplanation.setText("Cats will destroy arrows if they hit them head on, but not if they hit them from the side");
			mNextButton.setOnClickListener(mTutorialStages[++mTutorialStage]);
			mNextButton.setText("Finish");
		}
	},
	new OnClickListener() {
		@Override
		public void OnClick(Widget widget) {
			ModeTutorial.this.mPendMode = new ModeMenu(); 
		}
	},
	};
	
	public ModeTutorial(boolean firstRun) {
		mFirstRun = firstRun;
	}
	
	public void Setup(Context context)
	{
		mContext = context;
		
		mBtnSize = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_size);
		mBtnSizeWide = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_wide_size);
		mBtnSep = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_sep);
		mBtnBorder = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_border);
		mFontSize = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_font_size);
		
		try {
			NinePatchData btn_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/Blank64x64.png")), 24, 24, 24, 24);
			
			mNextButton = new Widget(btn_np, new Rect(mBtnBorder, 
													  mScreenHeight - mBtnBorder - mBtnSize,
													  mScreenWidth - mBtnBorder, 
													  mScreenHeight - mBtnBorder));
			mNextButton.setText("Next");
			mNextButton.setOnClickListener(mTutorialStages[0]);
			
			mExplanation = new Widget(btn_np, new Rect(mBtnBorder,
													   mBtnBorder,
													   mScreenWidth - mBtnBorder,
													   mBtnBorder + mBtnSize * 2)); 
			mExplanation.setText("How to play ShokoRocket!");
			
			mWidgetPage.setFontSize(mFontSize);
			mWidgetPage.addWidget(mNextButton);
			mWidgetPage.addWidget(mExplanation);
			
			mWorld = new World(mContext.getAssets().open("TutorialLevels/Tut1.Level"));
			mGameDrawer.Setup(mContext, mContext.getResources().getInteger(uk.danishcake.shokorocket.R.integer.grid_size));
			mGameDrawer.setDrawOffset((mScreenWidth - mWorld.getWidth() * mGameDrawer.getGridSize()) / 2, mBtnBorder + mBtnSize * 2 + mBtnSep);
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
		mWidgetPage.Tick(timespan);
		if(mRunning)
		{
			mWorld.Tick(timespan * 5);
		}
		
		return super.Tick(timespan);
	}
	
	public void Redraw(Canvas canvas)
	{
		mGameDrawer.Draw(canvas, mWorld);
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
		}

		super.Redraw(canvas);
	}
	
	@Override
	public void handleTap(int x, int y) {
		mWidgetPage.handleTap(x, y);
	}
}
