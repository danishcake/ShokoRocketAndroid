package uk.danishcake.shokorocket.moding;

import android.content.Context;
import android.graphics.Canvas;
import uk.danishcake.shokorocket.gui.WidgetPage;

public class ModeMPMenu extends Mode {
	private WidgetPage mWidgetPage = new WidgetPage();
	private SkinProgress mSkin;
	private ModeMenu mMenu;
	
	public ModeMPMenu(ModeMenu menu, SkinProgress skin) {
		mSkin = skin;
		mMenu = menu;
	}
	
	@Override
	public void Setup(Context context) {
		super.Setup(context);	
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		mWidgetPage.Tick(timespan);
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		mWidgetPage.Draw(canvas);
		super.Redraw(canvas);
	}
	
	@Override
	public void handleTap(int x, int y) {
		mWidgetPage.handleTap(x, y);
	}
}
