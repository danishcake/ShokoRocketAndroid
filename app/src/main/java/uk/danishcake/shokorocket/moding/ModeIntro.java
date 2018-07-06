package uk.danishcake.shokorocket.moding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.animation.Animation;
import uk.danishcake.shokorocket.simulation.SPWorld;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.sound.MusicManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Interpolator.Result;
import android.graphics.Paint.Align;
import android.text.TextPaint;

public class ModeIntro extends Mode implements Runnable {
	private class Rocket
	{
		public Vector2i position;
		public int age;
		public String text;
	}
	private class Smoke
	{
		public Vector2i position;
		public Vector2i velocity;
		public int age;
	}
	
	Animation mLogoLeft;
	Animation mLogoRight;
	Animation mCreditRocket;
	Animation mSmokeAnimation;
	Animation mForkMe;
	Interpolator mLogoInterpolator = null;
	Matrix mMatrix = new Matrix();
	boolean mFirstRun = false;
	private List<Rocket> mRockets = new ArrayList<Rocket>();
	private List<Smoke> mPlumes = new ArrayList<Smoke>();
	private int mSpawnTimer = 0;
	private Random mRandom = new Random();
	private Progress mProgress = null;
	private ArrayList<String> mUserLevels = null;
	private int mUserLevelIndex = 0;
	private TextPaint mTextPaint;
	private float mLoadFraction = 0;
	private String mLastLoaded;
	private Thread mLoadingThread = null;

	@Override
	public void Setup(Context context) {
		super.Setup(context);
		
		MusicManager.PlayMenuMusic();

		mTextPaint = new TextPaint();
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mTextPaint.setTextSize(mFontSize);
		mTextPaint.setColor(Color.BLACK);

		mProgress = new Progress(context);
		mUserLevels = new ArrayList<String>();
		mUserLevels.add(context.getString(R.string.intro_credits));
		mUserLevels.add("by Edward Woolhouse");
		mUserLevels.add("");
		mUserLevels.add(context.getString(R.string.intro_contributors));
		mUserLevels.add("");
		
		mLoadingThread = new Thread(this);
		mLoadingThread.start();

		mFirstRun = Progress.IsFirstRun(context);
		try
		{
			Map<String, Animation> animations = Animation.GetAnimations(context, "Animations/Intro/Splash.animation"); 
			mLogoLeft = animations.get("Left");
			mLogoRight = animations.get("Right");
			mCreditRocket = animations.get("CreditRocket");
			mSmokeAnimation = animations.get("Smoke");
			mForkMe = animations.get("ForkMe");
		} catch(IOException io_ex)
		{
			//TODO log it!
		}
	}
	
	@Override
	public Mode Teardown() {
		if(mLoadingThread != null)
		{
			mLoadingThread.interrupt();
			mLoadingThread = null;
		}
		return super.Teardown();
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		// The following code was bodged and has no particular coordinate system apart from
		// 1000:1 pixel
		// It was however designed for a 64 wide, so this bodge makes stuff work better
		// for offsets
		float scale = 2.0f;

		if(mLoadFraction < 1.0)
		{
			//Still waiting on list of levels to load
			mAge = 0;
		} else
		{
			int rocket_speed = 300 * 800 / mScreenHeight; //300px/s
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
						rocket.position = new Vector2i((mRandom.nextInt(mScreenWidth-128)+64),
								((mScreenHeight + 250 + mRandom.nextInt(50)) * 1000));
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
				int ltv_age= rocket.age;
				rocket.age += timespan;
				rocket.position.y -= rocket_speed * timespan;
				if(rocket.position.y < -1000000)
					v_it.remove();
				final int smoke_offset = (int)(100000 * scale);
				if(rocket.age / 80 > ltv_age / 80)
				{
					{
						Smoke s = new Smoke();
						s.position = new Vector2i(rocket.position.x * 1000 + (int)(10000 * scale), rocket.position.y + smoke_offset);
						s.velocity = new Vector2i(-7, -rocket_speed);
						mPlumes.add(s);
					}
					{
						Smoke s = new Smoke();
						s.position = new Vector2i(rocket.position.x * 1000, rocket.position.y + smoke_offset);
						s.velocity = new Vector2i(0, -rocket_speed);
						mPlumes.add(s);
					}
					{
						Smoke s = new Smoke();
						s.position = new Vector2i(rocket.position.x * 1000 - (int)(10000 * scale), rocket.position.y + smoke_offset);
						s.velocity = new Vector2i(7, -rocket_speed);
						mPlumes.add(s);
					}
				}
			}
			
			Iterator<Smoke> s_it = mPlumes.iterator();
			while(s_it.hasNext()){
				Smoke smoke = s_it.next();
				smoke.position.y += timespan * smoke.velocity.y;
				smoke.position.x += timespan * smoke.velocity.x;
				
				smoke.age += timespan;
				if(smoke.age >= 1000)
					s_it.remove();
				if(smoke.age > 125 && smoke.velocity.y < 0)
					smoke.velocity.y = 0;
			}

		}
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		// As described above, this was designed for 64 pixel grids
		float scale = 2.0f;

		if(mLoadFraction < 1.0)
		{
			canvas.drawARGB(255, mFadeR, mFadeG, mFadeB);
			RectF load_bar_full = new RectF(10, canvas.getHeight() / 2 - 24, canvas.getWidth() - 10, canvas.getHeight() / 2 + 24);
			Paint back_paint = new Paint();
			back_paint.setColor(Color.rgb(64, 0, 0));
			canvas.drawRoundRect(load_bar_full, 5, 5, back_paint);
			
			load_bar_full.left = 14;
			load_bar_full.right = 14 + (canvas.getWidth() - 28) * mLoadFraction;
			load_bar_full.top = canvas.getHeight() / 2 - 20;
			load_bar_full.bottom = canvas.getHeight() / 2 + 20;
			back_paint.setColor(Color.rgb(112, 146, 190));
			canvas.drawRoundRect(load_bar_full, 5, 5, back_paint);
			
			String last_loaded = mLastLoaded;
			float text_width = back_paint.measureText(last_loaded);
			back_paint.setColor(Color.rgb(0, 0, 0));
			canvas.drawText(last_loaded, canvas.getWidth() / 2 - text_width / 2, canvas.getHeight() / 2 + 40, back_paint);
		} else
		{
			float logoWidth = mLogoRight.getCurrentFrame().getWidth();
			float logoHeight = mLogoRight.getCurrentFrame().getHeight();

			if(mLogoInterpolator == null) {
				mLogoInterpolator = new Interpolator(3, 5);
				mLogoInterpolator.setKeyFrame(0, 0, new float[]{-logoWidth * 1.5f, 100, 0});
				mLogoInterpolator.setKeyFrame(1, 500, new float[]{-logoWidth * 1.5f, 100, 0});
				mLogoInterpolator.setKeyFrame(2, 1500, new float[]{canvas.getWidth() + 1, canvas.getHeight() + 1, 45});
				mLogoInterpolator.setKeyFrame(3, 2500, new float[]{canvas.getWidth() + 1, canvas.getHeight() + 1, 45}, new float[]{1.0f, 0.0f, 0.0f, 0.0f});
				mLogoInterpolator.setKeyFrame(4, 3500, new float[]{
						(canvas.getWidth() / 2) - (logoWidth  / 2),
						(canvas.getHeight() / 2) - (logoHeight / 2), 0});
			}
			
			Paint fill_paint = new Paint();
			fill_paint.setARGB(255, 0, 255, 255);
			canvas.drawPaint(fill_paint);

			float[] interpolated = new float[3];
			Result interp_result = mLogoInterpolator.timeToValues(mAge, interpolated);
			float x = interpolated[0];
			float y = interpolated[1];
			float angle = interpolated[2];

			mMatrix.reset();
			if(interp_result != Result.FREEZE_END) {
				mMatrix.setRotate(angle, logoWidth / 2, logoHeight / 2);
			} else {
				// Potentially wrong size used, but it doesn't matter as they're the same size
				mMatrix.setRotate(-(mAge - 3500) / 30, logoWidth / 2, logoHeight / 2);
			}

			mMatrix.postTranslate(x, y);

			for (Rocket r : mRockets) {
				canvas.drawBitmap(mCreditRocket.getCurrentFrame(), r.position.x, r.position.y / 1000, null);
				canvas.drawText(r.text, r.position.x + 32 * scale, r.position.y / 1000, mTextPaint);
			}

			for (Smoke s : mPlumes) {
				canvas.drawBitmap(mSmokeAnimation.getFrameByTime(s.age), s.position.x/1000, s.position.y/1000, null);
			}

			if(mAge < 2500)
			{
				canvas.drawBitmap(mLogoRight.getCurrentFrame(), mMatrix, null);
			} else
			{
				canvas.drawBitmap(mLogoLeft.getCurrentFrame(), mMatrix, null);
			}

			// Draw 'Fork me on github logo
			mForkMe.DrawCurrentFrame(canvas, canvas.getWidth() - mForkMe.getFrameByIndex(0).getWidth(), 0);
			super.Redraw(canvas);
		}
	}
	
	@Override
	public void handleTap(int x, int y) {
		if(mLoadFraction >= 1.0)
		{
			if(mFirstRun)
				mPendMode = new ModeTutorial(new ModeMenu(mProgress));
			else
				mPendMode = new ModeMenu(mProgress);
		}
	}
	
	@Override
	public boolean getBackgroundDrawn() {
		return false;
	}

	/**
	 * Loads the list of levels, setting mLoadFraction to 1 when done
	 */
	@Override
	public void run() {
		ArrayList<String> cont_levels = mProgress.getUserLevels();
		for (String level: cont_levels) {
			try {
				SPWorld temp_world = mProgress.getWorld(level);
				if(!mUserLevels.contains(temp_world.getAuthor()))
					mUserLevels.add(temp_world.getAuthor());
				mLastLoaded = "Loaded: " + temp_world.getLevelName();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mLoadFraction += 1.0f / (float)cont_levels.size();
		}
		mUserLevels.add(mContext.getString(R.string.intro_thanks_guys));
		
		mLoadFraction = 1.01f;
		mLoadingThread = null;
	}
}

