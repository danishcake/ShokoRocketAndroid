package uk.danishcake.shokorocket.moding;

import java.io.IOException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.simulation.MPWorld;

public class ModeMPMenu extends Mode {
	private SkinProgress mSkin;
	private ModeSPMenu mModeMenu;
	private boolean mSetup = false;
	
	//Widget pages and transition control
	private WidgetPage mAiPage = new WidgetPage();
	private WidgetPage mIPPage = mWidgetPage;
	private WidgetPage mPendPage = null; 
	private int mPagePendTimer = 0;
	private static int PAGE_TRANSITION_TIME = 250;
	
	
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
		
		
		//Setup AI page
		Widget toggleSP = new Widget(mBtnNP, new Rect(mBtnBorder, mScreenHeight - (mBtnSize + mBtnBorder), mBtnSize + mBtnBorder, mScreenHeight - mBtnBorder));
		toggleSP.setText("SP");
		toggleSP.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(mPendMode == null)
					mPendMode = mModeMenu;
			}
		});
		
		Widget toggleAI = new Widget(mBtnNP, new Rect(mBtnBorder + mBtnSize + mBtnSep, mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder));
		toggleAI.setText("Play online");
		toggleAI.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				gotoPage(mIPPage);
			}
		});
		
		Widget testAI = new Widget(mBtnNP, new Rect(mBtnBorder, mBtnBorder, mScreenWidth - mBtnBorder, mBtnBorder + mBtnSize));
		testAI.setText("Test AI");
		testAI.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(mPendMode == null)
				{
					try {
						MPWorld world = new MPWorld(mContext.getAssets().open("MultiplayerLevels/MP001.Level"));
						mPendMode = new ModeMPGame(ModeMPMenu.this, mSkin, world);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		mAiPage.setFontSize(mFontSize);
		mAiPage.addWidget(toggleSP);
		mAiPage.addWidget(testAI);
		mAiPage.addWidget(toggleAI);
		
		//Setup online page
		Widget toggleIP = new Widget(mBtnNP, new Rect(mBtnBorder + mBtnSize + mBtnSep, mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder));
		toggleIP.setText("Play vs AI");
		toggleIP.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				gotoPage(mAiPage);
			}
		});		
		
		mIPPage.setFontSize(mFontSize);
		mIPPage.addWidget(toggleIP);
		mIPPage.addWidget(toggleSP); //Reuse as in same place
		
		mSetup = true;
	}
	
	/**
	 * Starts a transition to the specified page. Ignored if transition in progress.
	 */
	private void gotoPage(WidgetPage page) {
		if(mPendPage == null && page != mWidgetPage) {
			mWidgetPage.setEnabled(false);
			mPendPage = page;
			mPagePendTimer = 0;
		}
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		controlWidgetPages(timespan);
		return super.Tick(timespan);
	}
	
	/**
	 * Controls transitioning and positioning of widget pages
	 */
	private void controlWidgetPages(int timespan) {
		//Move page across off screen, bring in the next page. 20% gap
		if(mPendPage != null) {
			mPagePendTimer += timespan;
			if(mPagePendTimer > PAGE_TRANSITION_TIME) mPagePendTimer = PAGE_TRANSITION_TIME;
			int x_offset      = (mPagePendTimer * mScreenWidth / PAGE_TRANSITION_TIME);
			int x_offset_pend = (mPagePendTimer * mScreenWidth / PAGE_TRANSITION_TIME) -
								mScreenWidth;
			mWidgetPage.setOffset(x_offset, 0);
			mPendPage.setOffset(x_offset_pend, 0);
			
			if(mPagePendTimer >= PAGE_TRANSITION_TIME) {
				mWidgetPage = mPendPage;
				mWidgetPage.setEnabled(true);
				mWidgetPage.setOffset(0, 0);
				mPendPage = null;
			}
		}
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
		if(mPendPage != null)
			mPendPage.Draw(canvas);
		super.Redraw(canvas);
	}
	
	@Override
	public boolean handleBack() {
		mPendMode = mModeMenu;
		return true;
	}
}
