package uk.danishcake.shokorocket.moding;

import java.util.ArrayList;
import java.util.List;

import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.RadioWidget;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.Toast;

public class ModeUnlocks extends Mode {
	private ModeMenu mModeMenu;
	private WidgetPage mWidgetPage;
	private SkinProgress mSkin;
	
	public ModeUnlocks(ModeMenu menu, SkinProgress skin) {
		mModeMenu = menu;
		mSkin = skin;
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
		
		List<RadioWidget> skins = new ArrayList<RadioWidget>();
		NinePatchData set_np = new NinePatchData(BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.blank_radio_set)), np_border, np_border, np_border, np_border);
		NinePatchData unset_np = new NinePatchData(BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.blank_radio_unset)), np_border, np_border, np_border, np_border);
		
		RadioWidget skin_pink = new RadioWidget(set_np, unset_np, new Rect(btnBorder, btnBorder + 1 * (btnSize + btnSep), mScreenWidth - btnBorder, btnBorder + 1 * (btnSize + btnSep) + btnSize), skins);
		skin_pink.setText("Pink mice");
		skin_pink.setEnabled(mSkin.getSkinUnlocked("Animations/PinkMice.xml"));
		
		RadioWidget skin_cont = new RadioWidget(set_np, unset_np, new Rect(btnBorder, btnBorder + 2 * (btnSize + btnSep), mScreenWidth - btnBorder, btnBorder + 2 * (btnSize + btnSep) + btnSize), skins);
		skin_cont.setText("Elite mice");
		skin_cont.setEnabled(mSkin.getSkinUnlocked("Animations/Contributor.xml"));
		
		RadioWidget skin_xmas = new RadioWidget(set_np, unset_np, new Rect(btnBorder, btnBorder + 3 * (btnSize + btnSep), mScreenWidth - btnBorder, btnBorder + 3 * (btnSize + btnSep) + btnSize), skins);
		skin_xmas.setText("Christmas");
		skin_xmas.setEnabled(mSkin.getSkinUnlocked("Animations/Christmas.xml"));
		
		//Read current set state
		if(mSkin.getSkin().equals("Animations/PinkMice.xml"))
			skin_pink.setValue(true);
		if(mSkin.getSkin().equals("Animations/Contributor.xml"))
			skin_cont.setValue(true);
		if(mSkin.getSkin().equals("Animations/Christmas.xml"))
			skin_xmas.setValue(true);

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
		
		skin_pink.setOnDisabledClickListener(new OnClickListener() {
			public void OnClick(Widget widget) {
				Toast.makeText(mContext, "Beat 5 levels to unlock", Toast.LENGTH_SHORT).show();
			}});
		
		skin_cont.setOnDisabledClickListener(new OnClickListener() {
			public void OnClick(Widget widget) {
				Toast.makeText(mContext, "Submit a level with the editor to unlock", Toast.LENGTH_SHORT).show();
			}});

		skin_xmas.setOnDisabledClickListener(new OnClickListener() {
			public void OnClick(Widget widget) {
				Toast.makeText(mContext, "Play during December to unlock", Toast.LENGTH_SHORT).show();
			}});
		
		mWidgetPage.setFontSize(context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_font_size));
		mWidgetPage.addWidget(back);
		mWidgetPage.addWidget(tutorial);
		mWidgetPage.addWidget(skin_pink);
		mWidgetPage.addWidget(skin_cont);
		mWidgetPage.addWidget(skin_xmas);
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		mWidgetPage.Tick(timespan);
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
	
	@Override
	public void handleTap(int x, int y) {
		mWidgetPage.handleTap(x, y);
	}
}
