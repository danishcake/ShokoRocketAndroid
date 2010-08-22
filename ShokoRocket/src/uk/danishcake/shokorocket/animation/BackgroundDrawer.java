package uk.danishcake.shokorocket.animation;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class BackgroundDrawer {
	private Context mContext = null;
	private Bitmap mBackgroundGradient = null;
	private Bitmap mCloudTile = null;
	private Bitmap mCloudA = null;
	private Bitmap mCloudB = null;
	private Bitmap mCloudC = null;
	private Bitmap mRocket = null;
	
	private int mWidth = 480;
	private int mHeight = 800;
	private int mGradientHeight = 800;
	private Rect mInputRect = new Rect();
	private Rect mOutputRect = new Rect();
	private Paint mClearPaint = new Paint();
	
	private int mAge = 0;
	private int mCloudTileX = 0;
	private int mCloudAX = 0;
	private int mCloudBX = 0;
	private int mCloudCX = 0;
	
	public BackgroundDrawer(Context context, int width, int height)
	{
		mContext = context;
		mWidth = width;
		mHeight = height;
		mClearPaint.setARGB(255, 255, 255, 255);
		
		try {
			Bitmap background_gradient = BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/Game/BackgroundGradient.png"));
			mCloudTile = BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/Game/CloudTile.png"));
			mCloudA = BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/Game/CloudA.png"));
			mCloudB = BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/Game/CloudB.png"));
			mCloudC = BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/Game/CloudC.png"));
			mRocket = BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/Game/RocketLaunch.png"));
			
			//Scale up gradient
			//Create scaled background image while preserving 800x480 aspect
			mGradientHeight = (int)(800.0f * (float)mWidth / 480.0f);
			mBackgroundGradient = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(mBackgroundGradient);
			canvas.drawRGB(255, 255, 255);
			Paint p = new Paint();
			p.setDither(true);
			p.setFilterBitmap(true);
			canvas.drawBitmap(background_gradient, new Rect(0, 0, 800, 480), new Rect(0, 0, mWidth, mGradientHeight), p);
			
			mCloudAX = mWidth * 1000 * 2 / 10;
			mCloudBX = mWidth * 1000 * 3 / 10;
			mCloudCX = mWidth * 1000 * 6 / 10;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void draw(Canvas canvas)
	{
		canvas.drawBitmap(mBackgroundGradient, 0, 0, null);
		//Now add clouds and rocket
		canvas.drawBitmap(mCloudA, mCloudAX/1000, 50 * mHeight / 800, null);
		canvas.drawBitmap(mCloudB, mCloudBX/1000, 300 * mHeight / 800, null);
		canvas.drawBitmap(mCloudC, mCloudCX/1000, 500 * mHeight / 800, null);
		
		canvas.drawBitmap(mRocket, mWidth - mRocket.getWidth() - (mWidth / 10), mHeight - mRocket.getHeight(), null);
		
		
		//Bottom of cloudscape should be at 800 pixels
		//canvas.drawBitmap(mCloudTile, 0, 800 - mCloudTile.getHeight(), null);
		
		mInputRect.left = mCloudTileX;
		mInputRect.right = mCloudTileX + mWidth;
		mInputRect.top = 0;
		mInputRect.bottom = mCloudTile.getHeight();
		mOutputRect.left = 0;
		mOutputRect.right = mCloudTile.getWidth() - mCloudTileX;
		mOutputRect.top = mGradientHeight - mCloudTile.getHeight();
		mOutputRect.bottom = mGradientHeight;
		
		if(mInputRect.right > mCloudTile.getWidth())
			mInputRect.right = mCloudTile.getWidth();
		if(mOutputRect.right > mWidth)
			mOutputRect.right = mWidth;
		
		canvas.drawBitmap(mCloudTile, mInputRect, mOutputRect, null);
		
		mOutputRect.left = mOutputRect.right;
		mOutputRect.right = mWidth;
		mInputRect.left = 0;
		mInputRect.right = mOutputRect.right - mOutputRect.left;
		if(mOutputRect.width() > 0)
			canvas.drawBitmap(mCloudTile, mInputRect, mOutputRect, null);
		
		//Clear bottom white
		canvas.drawRect(0, mGradientHeight, mWidth, mHeight, mClearPaint);
	}
	
	public void tick(int timespan)
	{
		mAge += timespan;
		mCloudTileX = (mAge / 75) % mCloudTile.getWidth();
		
		
		//Store is 1000ths of pixels
		//So to get position just divide by 1000
		//Multiplier of 1 would move 1 pixel per second
		mCloudAX += timespan * 10;
		if(mCloudAX > mWidth * 1000)
			mCloudAX = -mCloudA.getWidth() * 1000 - 3000;
		
		mCloudBX -= timespan * 12;
		if(mCloudBX < -mCloudB.getWidth() * 1000)
			mCloudBX = mWidth * 1000 + 5000;
		
		mCloudCX += timespan * 15;
		if(mCloudCX > mWidth * 1000)
			mCloudCX = -mCloudC.getWidth() * 1000 - 7000;
		
	}
}
