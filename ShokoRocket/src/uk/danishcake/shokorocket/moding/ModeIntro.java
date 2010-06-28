package uk.danishcake.shokorocket.moding;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class ModeIntro extends Mode {

	private Bitmap mIntroSplash;
	
	public ModeIntro() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void Setup() {
	}
	
	@Override
	public Mode Teardown() {
		return super.Teardown();
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		canvas.drawARGB(255,0,255,255);
	}
}

