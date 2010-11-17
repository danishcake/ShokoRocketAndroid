package uk.danishcake.shokorocket.moding;

import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

public class ModeUnlocks extends Mode {
	private ModeMenu mModeMenu;
	private WidgetPage mWidgetPage;
	
	public ModeUnlocks(ModeMenu menu) {
		mModeMenu = menu;
	}
	
	@Override
	public void Setup(Context context) {
		super.Setup(context);
		mWidgetPage = new WidgetPage();
		
		int np_border = context.getResources().getInteger(R.integer.np_border);
		NinePatchData btn_np = new NinePatchData(BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.blank_button)), np_border, np_border, np_border, np_border);
		
		int btnSize = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_size);
		int btnSep = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_sep);
		int btnBorder = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_border);
		
		Widget back = new Widget(btn_np, new Rect(btnBorder, mScreenHeight - btnBorder - btnSize, mScreenWidth - btnBorder, mScreenHeight - btnBorder));
		back.setText("Back");
		
		Widget tutorial = new Widget(btn_np, new Rect(btnBorder, btnBorder, mScreenWidth - btnBorder, btnBorder + btnSize));
		tutorial.setText("Tutorial");
		
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				mPendMode = mModeMenu;
			}
		});
		
		tutorial.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				mPendMode = new ModeTutorial(mModeMenu);
			}
		});
		
		mWidgetPage.setFontSize(context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_font_size));
		mWidgetPage.addWidget(back);
		mWidgetPage.addWidget(tutorial);
	}
	
	@Override
	public boolean handleBack() {
		mPendMode = mModeMenu;
		return true;
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
