package uk.danishcake.shokorocket.gui;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.moding.Progress;
import uk.danishcake.shokorocket.simulation.World;


/*
 * WorldItem
 * This is an item in the WorldSelector grid. It contains the name of the level 
 * it represents, and a bitmap, and is responsible for rendering that bitmap
 * in a separate thread. While rendering it will return a loading bitmap.
 */
public class WorldItem {
	private GameDrawer mGameDrawer = null;
	private Bitmap mWorldBitmap = null;
	private Bitmap mLoadingBitmap;
	private String mWorldName = null;
	private Progress mProgress = null;
	private Thread mRenderThread = null;
	private Semaphore mSemaphore = null;
	private int mFrameCount = 0;

	public WorldItem(GameDrawer game_drawer, Progress progress, Bitmap loading_bitmap)
	{
		mGameDrawer = game_drawer;
		mSemaphore = new Semaphore(1);
		mProgress = progress;
		mLoadingBitmap = loading_bitmap;
		if(mLoadingBitmap == null)
		{
			//TODO load it!
		}
	}

	/**
	 * getBitmap
	 * returns the current bitmap, if loaded, or a default bitmap if not
	 */
	public Bitmap getBitmap()
	{
		Bitmap world_bitmap = null;
		//If not actively loading then grab a semaphore
		try
		{
			if(mSemaphore.tryAcquire(0, TimeUnit.SECONDS))
			{
				world_bitmap = mWorldBitmap;
				mSemaphore.release();
			}
		} catch(InterruptedException int_ex)
		{
			//TODO log
		}
		if(world_bitmap == null)
			return mLoadingBitmap;
		else
			return world_bitmap;
	}

	/**
	 * set
	 * Interrupts any loads in progress
	 */
	public void set(WorldItem item)
	{
		try
		{
			mSemaphore.acquire();
			if(mRenderThread != null)
				mRenderThread.interrupt();
			//Causes a InteruptedException to be thrown on the thread, should just handle it at root level of runnable
			mWorldName = item.mWorldName;
			mWorldBitmap = item.mWorldBitmap;
			mSemaphore.release();
		} catch(InterruptedException int_ex)
		{
			//Shouldn't happen
		}
		if(mWorldBitmap == null)
			load(mWorldName);
	}

	/**
	 * load
	 * Clears the current bitmap, and fires off a thread to load the next
	 */
	public void load(String world_name)
	{
		try
		{
			mSemaphore.acquire();
			if(mRenderThread != null)
				mRenderThread.interrupt();
			mWorldName = world_name;

			mRenderThread = new Thread(new Runnable() {
				@Override
				public void run()
				{
					try
					{
						World world = mProgress.getWorld(mWorldName);

						//Only acquire semaphore for last writing activity
						mSemaphore.acquire();
						Bitmap bitmap = Bitmap.createBitmap(mGameDrawer.getGridSize() * world.getWidth(), 
								mGameDrawer.getGridSize() * world.getHeight(), 
								Bitmap.Config.RGB_565);

						Canvas canvas = new Canvas(bitmap);
						mGameDrawer.DrawTilesAndWalls(canvas, world);
						mGameDrawer.Draw(canvas, world);
						mWorldBitmap = bitmap;
						mSemaphore.release();
					} catch(InterruptedException int_ex)
					{
						//Quite likely to be interupted, but only before semaphore acquired
					} catch(Exception loading_exception)
					{
						//TODO log, set bitmap to error bitmap
					}
				}
			});
			mRenderThread.setPriority(android.os.Process.THREAD_PRIORITY_LESS_FAVORABLE);
			mWorldBitmap = null;
			mRenderThread.start();
			mSemaphore.release();
		} catch(InterruptedException int_ex)
		{
			//Shouldn't happen
		}
	}
	
	public String getFilename() {
		return mWorldName;
	}
}