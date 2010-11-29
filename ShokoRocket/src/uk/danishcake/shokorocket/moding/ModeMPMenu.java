package uk.danishcake.shokorocket.moding;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.Widget;

public class ModeMPMenu extends Mode {
	private SkinProgress mSkin;
	private ModeSPMenu mModeMenu;
	private boolean mSetup = false;
	
	public ModeMPMenu(ModeSPMenu menu, SkinProgress skin) {
		mSkin = skin;
		mModeMenu = menu;
	}
	
	@Override
	public void Setup(Context context) {
		if(mSetup)
		{
			return;
		}
		super.Setup(context);
		
		Widget toggleSP = new Widget(mBtnNP, new Rect(mBtnBorder, mScreenHeight - (mBtnSize + mBtnBorder), mBtnSize + mBtnBorder, mScreenHeight - mBtnBorder));
		toggleSP.setText("SP");
		toggleSP.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(mPendMode == null)
					mPendMode = mModeMenu;
			}
		});
		
		Widget testAI = new Widget(mBtnNP, new Rect(mBtnBorder, mBtnBorder, mScreenWidth - mBtnBorder, mBtnBorder + mBtnSize));
		testAI.setText("Test AI");
		testAI.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(mPendMode == null)
					mPendMode = new ModeMPGame(ModeMPMenu.this, mSkin);
			}
		});
		
		
		mWidgetPage.setFontSize(mFontSize);
		mWidgetPage.addWidget(toggleSP);
		mWidgetPage.addWidget(testAI);
		
		mSetup = true;
	}
	
	@Override
	public Mode Teardown() {
		Mode next_mode = super.Teardown();
		mPendMode = null;
		mPendTimer = 0;
		mAge = 0;
		return next_mode;
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
