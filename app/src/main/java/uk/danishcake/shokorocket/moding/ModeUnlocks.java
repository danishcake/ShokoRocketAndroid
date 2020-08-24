package uk.danishcake.shokorocket.moding;

import java.util.ArrayList;
import java.util.List;

import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.RadioWidget;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.sound.MusicManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.Toast;

public class ModeUnlocks extends Mode {
	private ModeMenu mModeMenu;
	private SkinProgress mSkin;
	private Progress mProgress;
	
	public ModeUnlocks(ModeMenu menu, SkinProgress skin, Progress progress) {
		mModeMenu = menu;
		mSkin = skin;
		mProgress = progress;
	}
	
	@Override
	public void Setup(Context context) {
		super.Setup(context);
		
		MusicManager.PlayMenuMusic();
		
		mWidgetPage = new WidgetPage();
				
		Widget back = new Widget(mBtnNP, new Rect(mBtnBorder, mScreenHeight - mBtnBorder - mBtnSize, mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder));
		back.setText(context.getString(R.string.unlocks_back));
		
		Widget complete_count = new Widget(mBtnNP, new Rect(mBtnBorder, mScreenHeight - mBtnBorder - 2 * mBtnSize - mBtnSep, mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder - 1 * mBtnSize - mBtnSep));
		complete_count.setText(context.getString(R.string.unlocks_complete_prefix) + (new Integer(mProgress.getBeatenLevelCount())).toString() + "/" + (new Integer(mProgress.getTotalLevelCount())).toString());
		
		Widget tutorial = new Widget(mBtnNP, new Rect(mBtnBorder, mBtnBorder, mScreenWidth - mBtnBorder, mBtnBorder + mBtnSize));
		tutorial.setText(context.getString(R.string.menu_tutorial));
		
		List<RadioWidget> skins = new ArrayList<RadioWidget>();
		NinePatchData set_np = new NinePatchData(BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.blank_radio_set)), mNPBorder, mNPBorder, mNPBorder, mNPBorder);
		NinePatchData unset_np = new NinePatchData(BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.blank_radio_unset)), mNPBorder, mNPBorder, mNPBorder, mNPBorder);
		
		RadioWidget skin_pink = new RadioWidget(set_np, unset_np, new Rect(mBtnBorder, mBtnBorder + 1 * (mBtnSize + mBtnSep), mScreenWidth - mBtnBorder, mBtnBorder + 1 * (mBtnSize + mBtnSep) + mBtnSize), skins);
		skin_pink.setText(context.getString(R.string.unlocks_pinkmouse_text));
		skin_pink.setEnabled(mSkin.getSkinUnlocked("Animations/PinkMice.xml"));
		
		RadioWidget skin_cont = new RadioWidget(set_np, unset_np, new Rect(mBtnBorder, mBtnBorder + 2 * (mBtnSize + mBtnSep), mScreenWidth - mBtnBorder, mBtnBorder + 2 * (mBtnSize + mBtnSep) + mBtnSize), skins);
		skin_cont.setText(context.getString(R.string.unlocks_contributor_text));
		skin_cont.setEnabled(mSkin.getSkinUnlocked("Animations/Contributor.xml"));
		
		RadioWidget skin_xmas = new RadioWidget(set_np, unset_np, new Rect(mBtnBorder, mBtnBorder + 3 * (mBtnSize + mBtnSep), mScreenWidth - mBtnBorder, mBtnBorder + 3 * (mBtnSize + mBtnSep) + mBtnSize), skins);
		skin_xmas.setText(context.getString(R.string.unlocks_xmas_text));
		skin_xmas.setEnabled(mSkin.getSkinUnlocked("Animations/Christmas.xml"));
		
		RadioWidget skin_obs = new RadioWidget(set_np, unset_np, new Rect(mBtnBorder, mBtnBorder + 4 * (mBtnSize + mBtnSep), mScreenWidth - mBtnBorder, mBtnBorder + 4 * (mBtnSize + mBtnSep) + mBtnSize), skins);
		skin_obs.setText(context.getString(R.string.unlocks_obsidian_text));
		skin_obs.setEnabled(mSkin.getSkinUnlocked("Animations/ObsidianMice.xml"));
		
		RadioWidget skin_ghost = new RadioWidget(set_np, unset_np, new Rect(mBtnBorder, mBtnBorder + 5 * (mBtnSize + mBtnSep), mScreenWidth - mBtnBorder, mBtnBorder + 5 * (mBtnSize + mBtnSep) + mBtnSize), skins);
		skin_ghost.setText(context.getString(R.string.unlocks_ghostmice_text));
		skin_ghost.setEnabled(mSkin.getSkinUnlocked("Animations/GhostMice.xml"));
		
		RadioWidget skin_line = new RadioWidget(set_np, unset_np, new Rect(mBtnBorder, mBtnBorder + 6 * (mBtnSize + mBtnSep), mScreenWidth - mBtnBorder, mBtnBorder + 6 * (mBtnSize + mBtnSep) + mBtnSize), skins);
		skin_line.setText(context.getString(R.string.unlocks_lineart_text));
		skin_line.setEnabled(mSkin.getSkinUnlocked("Animations/Line.xml"));
		
		//Read current set state
		if(mSkin.getSkin().equals("Animations/PinkMice.xml"))
			skin_pink.setValue(true);
		if(mSkin.getSkin().equals("Animations/Contributor.xml"))
			skin_cont.setValue(true);
		if(mSkin.getSkin().equals("Animations/Christmas.xml"))
			skin_xmas.setValue(true);
		if(mSkin.getSkin().equals("Animations/ObsidianMice.xml"))
			skin_obs.setValue(true);
		if(mSkin.getSkin().equals("Animations/GhostMice.xml"))
			skin_ghost.setValue(true);
		if(mSkin.getSkin().equals("Animations/Line.xml"))
			skin_line.setValue(true);
		

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
		
		skin_pink.setOnValueChangedCallback(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(((RadioWidget)widget).getValue())
					mSkin.setSkin("");
				else
					mSkin.setSkin("Animations/PinkMice.xml");
			}
		});
		
		skin_cont.setOnValueChangedCallback(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(((RadioWidget)widget).getValue())
					mSkin.setSkin("");
				else
					mSkin.setSkin("Animations/Contributor.xml");
			}
		});
		
		skin_xmas.setOnValueChangedCallback(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(((RadioWidget)widget).getValue())
					mSkin.setSkin("");
				else
					mSkin.setSkin("Animations/Christmas.xml");
			}
		});
		
		skin_obs.setOnValueChangedCallback(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(((RadioWidget)widget).getValue())
					mSkin.setSkin("");
				else
					mSkin.setSkin("Animations/ObsidianMice.xml");
			}
		});
		
		skin_ghost.setOnValueChangedCallback(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(((RadioWidget)widget).getValue())
					mSkin.setSkin("");
				else
					mSkin.setSkin("Animations/GhostMice.xml");
			}
		});
		
		skin_line.setOnValueChangedCallback(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(((RadioWidget)widget).getValue())
					mSkin.setSkin("");
				else
					mSkin.setSkin("Animations/Line.xml");
			}
		});
		
		skin_pink.setOnDisabledClickListener(new OnClickListener() {
			public void OnClick(Widget widget) {
				Toast.makeText(mContext, R.string.unlocks_pink_mice_hint, Toast.LENGTH_SHORT).show();
			}});
		
		skin_cont.setOnDisabledClickListener(new OnClickListener() {
			public void OnClick(Widget widget) {
				Toast.makeText(mContext, R.string.unlocks_contributor_hint, Toast.LENGTH_SHORT).show();
			}});

		skin_xmas.setOnDisabledClickListener(new OnClickListener() {
			public void OnClick(Widget widget) {
				Toast.makeText(mContext, R.string.unlocks_christmas_hint, Toast.LENGTH_SHORT).show();
			}});
		
		skin_obs.setOnDisabledClickListener(new OnClickListener() {
			public void OnClick(Widget widget) {
				Toast.makeText(mContext, R.string.unlocks_obsidian_hint, Toast.LENGTH_SHORT).show();
			}});
		
		skin_ghost.setOnDisabledClickListener(new OnClickListener() {
			public void OnClick(Widget widget) {
				Toast.makeText(mContext, R.string.unlocks_ghost_hint, Toast.LENGTH_SHORT).show();
			}});
		
		skin_line.setOnDisabledClickListener(new OnClickListener() {
			public void OnClick(Widget widget) {
				Toast.makeText(mContext, R.string.unlocks_lineart_hint, Toast.LENGTH_SHORT).show();
			}});
		
		mWidgetPage.setFontSize(mFontSize);
		mWidgetPage.addWidget(back);
		mWidgetPage.addWidget(complete_count);
		mWidgetPage.addWidget(tutorial);
		mWidgetPage.addWidget(skin_pink);
		mWidgetPage.addWidget(skin_cont);
		mWidgetPage.addWidget(skin_xmas);
		mWidgetPage.addWidget(skin_obs);
		mWidgetPage.addWidget(skin_ghost);
		mWidgetPage.addWidget(skin_line);
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		return super.Tick(timespan);
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
}
