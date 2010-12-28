package uk.danishcake.shokorocket.moding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.danishcake.shokorocket.animation.Animation;
import uk.danishcake.shokorocket.simulation.SPWorld;
import uk.danishcake.shokorocket.simulation.Vector2i;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Interpolator.Result;
import android.graphics.Paint.Align;
import android.text.TextPaint;

public class ModeIntro extends Mode {
	private class Rocket
	{
		public Vector2i position;
		public int age;
		public String text;
	}
	
	Animation mLogoLeft;
	Animation mLogoRight;
	Animation mCreditRocket;
	Interpolator mLogoInterpolator = null;
	Matrix mMatrix = new Matrix();
	boolean mFirstRun = false;
	private List<Rocket> mRockets = new ArrayList<Rocket>();
	private int mSpawnTimer = 0;
	private Random mRandom = new Random();
	private Progress mProgress = null;
	private ArrayList<String> mUserLevels = null;
	private int mUserLevelIndex = 0;
	private TextPaint mTextPaint;

	@Override
	public void Setup(Context context) {
		super.Setup(context);

		mTextPaint = new TextPaint();
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mTextPaint.setTextSize(mFontSize);
		mTextPaint.setColor(Color.BLACK);

		mProgress = new Progress(context);
		ArrayList<String> cont_levels = mProgress.getUserLevels();
		mUserLevels = new ArrayList<String>();
		mUserLevels.add("Credits");
		mUserLevels.add("by Edward Woolhouse");
		mUserLevels.add("");
		mUserLevels.add("Contributers");
		mUserLevels.add("");
		
		for (String level: cont_levels) {
			try {
				SPWorld temp_world = mProgress.getWorld(level);
				if(!mUserLevels.contains(temp_world.getAuthor()))
					mUserLevels.add(temp_world.getAuthor());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mUserLevels.add("Thanks guys!");

		mFirstRun = Progress.IsFirstRun(context);
		try
		{
			Map<String, Animation> animations = Animation.GetAnimations(context, "Animations/Intro/Splash.animation"); 
			mLogoLeft = animations.get("Left");
			mLogoRight = animations.get("Right");
			mCreditRocket = animations.get("CreditRocket");
		} catch(IOException io_ex)
		{
			//TODO log it!
		}
	}
	
	@Override
	public Mode Teardown() {
		return super.Teardown();
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		int rocket_speed = mScreenHeight * timespan / 2400; 
		mLogoLeft.Tick(timespan);
		mLogoRight.Tick(timespan);
		mCreditRocket.Tick(timespan);
		
		if(mAge > 4000)
		{
			mSpawnTimer += timespan;
			if(mSpawnTimer > 1000 && mUserLevelIndex < mUserLevels.size())
			{
				if(!mUserLevels.get(mUserLevelIndex).equals(""))
				{
					Rocket rocket = new Rocket();
					rocket.position = new Vector2i(mRandom.nextInt(mScreenWidth-128)+32, mScreenHeight + 150 + mRandom.nextInt(50));
					rocket.age = 0;
					rocket.text = mUserLevels.get(mUserLevelIndex);
					mRockets.add(rocket);
				}
				mUserLevelIndex++;
				mSpawnTimer = 0;
			}
		}
		Iterator<Rocket> v_it = mRockets.iterator();
		while(v_it.hasNext()){
			Rocket rocket = v_it.next();
			rocket.position.y -= rocket_speed;
			if(rocket.position.y < -1000)
				v_it.remove();
		}

		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		if(mLogoInterpolator == null)
		{
			mLogoInterpolator = new Interpolator(3, 5);
			mLogoInterpolator.setKeyFrame(0, 0, new float[] {-250, 100, 0});
			mLogoInterpolator.setKeyFrame(1, 500, new float[]{-250, 100, 0});
			mLogoInterpolator.setKeyFrame(2, 1500, new float[]{canvas.getWidth() + 1, canvas.getHeight() + 1, 45});
			mLogoInterpolator.setKeyFrame(3, 2500, new float[]{canvas.getWidth() + 1, canvas.getHeight() + 1, 45}, new float[]{1.0f, 0.0f, 0.0f, 0.0f});
			mLogoInterpolator.setKeyFrame(4, 3500, new float[]{canvas.getWidth() / 2 - 100, canvas.getHeight() / 2 - 50, 0});
		}
		
		Paint fill_paint = new Paint();
		fill_paint.setARGB(255, 0, 255, 255);
		canvas.drawPaint(fill_paint);
		//canvas.drawARGB(255,0,255,255);
		float[] interpolated = new float[3];
		Result interp_result = mLogoInterpolator.timeToValues(mAge, interpolated);
		float x = interpolated[0];
		float y = interpolated[1];
		float angle = interpolated[2];
		
		mMatrix.reset();
		if(interp_result != Result.FREEZE_END)
			mMatrix.setRotate(angle, 100, 50);
		else
			mMatrix.setRotate(-(mAge - 3500) / 30, 100, 50);

		mMatrix.postTranslate(x, y);

		for (Rocket r : mRockets) {
			canvas.drawBitmap(mCreditRocket.getCurrentFrame(), r.position.x, r.position.y, null);
			canvas.drawText(r.text, r.position.x+32, r.position.y, mTextPaint);
		}
		
		if(mAge < 2500)
		{
			canvas.drawBitmap(mLogoRight.getCurrentFrame(), mMatrix, null);
		} else
		{
			canvas.drawBitmap(mLogoLeft.getCurrentFrame(), mMatrix, null);
		}

		super.Redraw(canvas);
	}
	
	@Override
	public void handleTap(int x, int y) {
		if(mFirstRun)
			mPendMode = new ModeTutorial(new ModeMenu(mProgress));
		else
			mPendMode = new ModeMenu(mProgress);
	}
	
	@Override
	public boolean getBackgroundDrawn() {
		return false;
	}
}

