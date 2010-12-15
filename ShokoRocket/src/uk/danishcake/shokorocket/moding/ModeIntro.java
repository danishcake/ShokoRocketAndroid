package uk.danishcake.shokorocket.moding;

import java.io.IOException;
import java.util.Map;

import uk.danishcake.shokorocket.animation.Animation;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Interpolator;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Interpolator.Result;

public class ModeIntro extends Mode {
	Animation mLogoLeft;
	Animation mLogoRight;
	Interpolator mLogoInterpolator = null;
	Matrix mMatrix = new Matrix();
	boolean mFirstRun = false;
	
	public ModeIntro() {
	}
	
	@Override
	public void Setup(Context context) {
		super.Setup(context);
		mFirstRun = Progress.IsFirstRun(context);
		try
		{
			Map<String, Animation> animations = Animation.GetAnimations(context, "Animations/Intro/Splash.animation"); 
			mLogoLeft = animations.get("Left");
			mLogoRight = animations.get("Right");
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
		mLogoLeft.Tick(timespan);
		mLogoRight.Tick(timespan);
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
			mPendMode = new ModeTutorial(new ModeMenu());
		else
			mPendMode = new ModeMenu();
	}
	
	@Override
	public boolean getBackgroundDrawn() {
		return false;
	}
}

