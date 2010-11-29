package uk.danishcake.shokorocket.moding;

import android.content.Context;
import android.graphics.Canvas;

public class ModeMPGame extends Mode {
	private ModeMPMenu mModeMenu;
	private SkinProgress mSkin;

	public ModeMPGame(ModeMPMenu menu, SkinProgress skin) {
		mSkin = skin;
		mModeMenu = menu;
	}
	
	@Override
	public void Setup(Context context) {
		super.Setup(context);
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		mWidgetPage.Draw(canvas);
		super.Redraw(canvas);
	}
	
	@Override
	public boolean handleBack() {
		mPendMode = mModeMenu;
		return true;
	}
}
