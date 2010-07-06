package uk.danishcake.shokorocket.moding;

import java.io.IOException;
import java.util.Map;

import uk.danishcake.shokorocket.animation.Animation;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.gui.OnClickListener;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Interpolator;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Interpolator.Result;


public class ModeMenu extends Mode {
	private WidgetPage mWidgetPage = new WidgetPage();
	Widget mLevelName;
	
	@Override
	public void Setup(Context context) {
		try
		{
			NinePatchData btn_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/Blank64x64.png")), 24, 24, 24, 24);
			mLevelName = new Widget(btn_np, new Rect(16, 16, mScreenWidth - 16, 64)); 
			mLevelName.setText("Level name");
			
			
			Widget scrollRight = new Widget(btn_np, new Rect(16, 68, 64, 68 + 64));
			scrollRight.setText(">");
			
			Widget scrollLeft= new Widget(btn_np, new Rect(mScreenWidth - 64, 68, mScreenWidth - 16, 68 + 64));
			scrollLeft.setText("<"); 
			
			
			mWidgetPage.addWidget(mLevelName);
			mWidgetPage.addWidget(scrollRight);
			mWidgetPage.addWidget(scrollLeft);
			
		} catch(IOException io_ex)
		{
			//TODO log
		}
	}
	
	@Override
	public Mode Teardown() {
		return super.Teardown();
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		mWidgetPage.Tick(timespan);
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		mWidgetPage.Draw(canvas);
	}
	
	@Override
	public void handleTap(int x, int y) {
		mWidgetPage.handleTap(x, y);
	}
}
