package uk.danishcake.shokorocket.gui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.moding.Progress;


/** 
 * WorldSelector
 * Represents a swipable widget that flicks between levels 
 */
public class WorldSelector {
	private Progress mProgress;
	private GameDrawer mGameDrawer = new GameDrawer();
	private boolean mGestureInProgress = false;
	private int m_ltv_x;
	private int m_ltv_y;

	private float mRateX = 0;
	private float mRateY = 0;
	private int mRateFrames = 0;
	private float mFracX = 0;
	private float mFracY = 0;
	private float mLtvFracX = 0;
	private float mLtvFracY = 0;
	private float mLtvDt = 0.02f;
	
	private float mColumnWidth = 0;
	private float mRowHeight = 0;
	private WorldItem[][] mItems = new WorldItem[3][3];

	private final float MINIMUM_RATE = 0.2f; //0.2 screens/second
	private final float DECAY_RATE = 0.2f; //0.2 screens/second^2

	public WorldSelector(Progress progress, Context context)
	{
		mProgress = progress;
		mGameDrawer.Setup(context, context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.preview_grid_size));

		String[][] levels = mProgress.getLevelGrid();
		for(int x = 0; x < 3; x++)
		{
			for(int y = 0; y < 3; y++)
			{
				mItems[x][y] = new WorldItem(mGameDrawer, mProgress);
				mItems[x][y].load(levels[x][y]);
			}
		}
	}

	public void Draw(Canvas canvas, Rect clip)
	{
		canvas.save();
		canvas.clipRect(clip);
		
		/* Layout routine:
		 * a: Find widest and tallest in each column and row
		 */
		
		float width_max = 0;	//By column
		float height_max = 0;
		for(int x = 0; x < 3; x++)
		{
			for(int y = 0; y < 3; y++)
			{
				Bitmap bitmap = mItems[x][y].getBitmap();
				if(bitmap != null)
				{
				if(bitmap.getWidth() > width_max)
					width_max = bitmap.getWidth();
				if(bitmap.getHeight() > height_max)
					height_max = bitmap.getHeight();
				}
			}
		}
		if(mColumnWidth == 0) mColumnWidth = width_max;
		mColumnWidth = mColumnWidth * 0.8f + width_max * 0.2f;
		
		if(mRowHeight == 0) mRowHeight = height_max;
		mRowHeight = mRowHeight * 0.8f + height_max * 0.2f;
		
		
		int[] offsets_x = new int[3];
		int[] offsets_y = new int[3];
		offsets_x[0] = clip.centerX() - (int)mColumnWidth - 10;
		offsets_x[1] = clip.centerX();
		offsets_x[2] = clip.centerX() + (int)mColumnWidth + 10;
		
		offsets_y[0] = clip.centerY() - (int)mRowHeight - 10;
		offsets_y[1] = clip.centerY();
		offsets_y[2] = clip.centerY() + (int)mRowHeight + 10;
		
		/*
		 * Central item is centred in clip region
		 * eg for (1, 1) take centre, subtract half of bitmap size and use that
		 */
		
		for(int x = 0; x < 3; x++)
		{
			for(int y = 0; y < 3; y++)
			{
				Bitmap bitmap = mItems[x][y].getBitmap();
				
				if(bitmap != null)
				{
				canvas.drawBitmap(bitmap, offsets_x[x] - bitmap.getWidth() / 2 + mFracX * mColumnWidth, 
										  offsets_y[y] - bitmap.getHeight() / 2 + mFracY * mRowHeight, 
										  null);
				}
			}
		}
		canvas.restore();
	}

	/**
	 * Tick
	 * Advances time - allow kinetic scrolling to advance, and rate of swipes to be judged 
	 * The aim is for the kinetic scrolling to work to a certain degree, but after it slows
	 * past some threshold to drift to the next item in that direction at a constant speed
	 */
	public void Tick(int timespan)
	{
		float dt = ((float)timespan) / 1000.0f;
		mLtvDt = dt;
		if(mGestureInProgress)
		{
			
		} else
		{
			//TODO maximum rate limit
			if(mFracX > 0 && Math.abs(mRateX) < MINIMUM_RATE)
			{
				mRateX = -MINIMUM_RATE;
			}
			if(mFracX < 0 && Math.abs(mRateX) < MINIMUM_RATE)
			{
				mRateX = MINIMUM_RATE;
			}
			if(mFracY > 0 && Math.abs(mRateY) < MINIMUM_RATE)
			{
				mRateY = -MINIMUM_RATE;
			}
			if(mFracY < 0 && Math.abs(mRateY) < MINIMUM_RATE)
			{
				mRateY = MINIMUM_RATE;
			}
			
			/* Decay speed */
			if(mRateX > MINIMUM_RATE)
			{
				mRateX -= DECAY_RATE * dt;
			}
			if(mRateX < -MINIMUM_RATE)
			{
				mRateX += DECAY_RATE * dt;
			}
			if(mRateY > MINIMUM_RATE)
			{
				mRateY -= DECAY_RATE * dt;
			}
			if(mRateY < -MINIMUM_RATE)
			{
				mRateY += DECAY_RATE * dt;
			}
			mFracX += mRateX * dt;
			mFracY += mRateY * dt;
		}
		
		if(((mFracX >= 0 && mLtvFracX < 0) || (mFracX <= 0 && mLtvFracX > 0)) &&
		   Math.abs(mRateX) < MINIMUM_RATE * 1.1f)
		{
			mFracX = 0;
			mRateX = 0;
		}
		if(((mFracY >= 0 && mLtvFracY < 0) || (mFracY <= 0 && mLtvFracY > 0)) && 
		   Math.abs(mRateY) < MINIMUM_RATE * 1.1f)
		{
			mFracY = 0;
			mRateY = 0;
		}
		
		if(mFracX > 0.5) //Moving items to right
		{
			mFracX -= 1;

			mItems[2][0].set(mItems[1][0]);
			mItems[1][0].set(mItems[0][0]);
			mItems[2][1].set(mItems[1][1]);
			mItems[1][1].set(mItems[0][1]);
			mItems[2][2].set(mItems[1][2]);
			mItems[1][2].set(mItems[0][2]);
			
			mProgress.nextLevel();
			mProgress.nextLevelPack();
			mProgress.nextLevel();
			mProgress.prevLevelPack();
			mProgress.prevLevelPack();
			mProgress.nextLevel();
			mProgress.nextLevelPack();
			
			String[][] levels = mProgress.getLevelGrid();
			mItems[0][0].load(levels[0][0]);
			mItems[0][1].load(levels[0][1]);
			mItems[0][2].load(levels[0][2]);
		}
		if(mFracX < -0.5) //Moving items to left
		{
			mFracX += 1;
			mItems[0][0].set(mItems[1][0]);
			mItems[1][0].set(mItems[2][0]);
			mItems[0][1].set(mItems[1][1]);
			mItems[1][1].set(mItems[2][1]);
			mItems[0][2].set(mItems[1][2]);
			mItems[1][2].set(mItems[2][2]);
			
			mProgress.prevLevel();
			mProgress.nextLevelPack();
			mProgress.prevLevel();
			mProgress.prevLevelPack();
			mProgress.prevLevelPack();
			mProgress.prevLevel();
			mProgress.nextLevelPack();
			
			String[][] levels = mProgress.getLevelGrid();
			mItems[2][0].load(levels[2][0]);
			mItems[2][1].load(levels[2][1]);
			mItems[2][2].load(levels[2][2]);
		}
		if(mFracY > 0.5)
		{
			mFracY -= 1;
			mItems[0][2].set(mItems[0][1]);
			mItems[1][2].set(mItems[1][1]);
			mItems[2][2].set(mItems[2][1]);
			mItems[0][1].set(mItems[0][0]);
			mItems[1][1].set(mItems[1][0]);
			mItems[2][1].set(mItems[2][0]);
			
			mProgress.prevLevelPack();
			
			String[][] levels = mProgress.getLevelGrid();
			mItems[0][0].load(levels[0][0]);
			mItems[1][0].load(levels[1][0]);
			mItems[2][0].load(levels[2][0]);
		}
		if(mFracY < -0.5)
		{
			mFracY += 1;
			mItems[0][0].set(mItems[0][1]);
			mItems[1][0].set(mItems[1][1]);
			mItems[2][0].set(mItems[2][1]);
			mItems[0][1].set(mItems[0][2]);
			mItems[1][1].set(mItems[1][2]);
			mItems[2][1].set(mItems[2][2]);
			
			mProgress.nextLevelPack();
			
			String[][] levels = mProgress.getLevelGrid();
			mItems[0][0].load(levels[0][0]);
			mItems[1][0].load(levels[1][0]);
			mItems[2][0].load(levels[2][0]);
		}
		mLtvFracX = mFracX;
		mLtvFracY = mFracY;
	}

	/**
	 * updateGesture
	 * provides initial position and updates
	 */
	public void updateGesture(int x, int y)
	{
		if(!mGestureInProgress)
		{
			m_ltv_x = x;
			m_ltv_y = y;
			mRateFrames = 0;
		}
		mRateFrames++;
		if(mRateFrames > 3)
			mRateFrames = 3;
		mGestureInProgress = true;

		float instantaneous_rate_x = ((float)(x - m_ltv_x)) / (mColumnWidth * mLtvDt);
		float instantaneous_rate_y = ((float)(y - m_ltv_y)) / (mRowHeight * mLtvDt);
		float instantaneous_weight = 1.0f / (float)mRateFrames;

		mRateX = mRateX * (1.0f - instantaneous_weight) + instantaneous_rate_x * instantaneous_weight;
		mRateY = mRateY * (1.0f - instantaneous_weight) + instantaneous_rate_y * instantaneous_weight;
		
		mFracX += ((float)(x - m_ltv_x)) / 480.0f;
		mFracY += ((float)(y - m_ltv_y)) / 800.0f;
			
		m_ltv_x = x;
		m_ltv_y = y;
	}

	/**
	 * finishGesture
	 * Called at end of gesture
	 */
	public void finishGesture()
	{
		mGestureInProgress = false;
	}

	public String getSelectedWorld() {
		return mItems[1][1].getFilename();
	}
}