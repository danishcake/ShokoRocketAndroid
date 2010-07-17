package uk.danishcake.shokorocket.moding;

import java.io.IOException;
import java.util.ArrayList;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.simulation.World;
import uk.danishcake.shokorocket.sound.SoundManager;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.Toast;


public class ModeMenu extends Mode {
	private WidgetPage mWidgetPage = new WidgetPage();
	private Widget mLevelName;
	private Widget mLevelPackName;
	private World mWorld = null;
	private GameDrawer mGameDrawer = new GameDrawer();
	private String[][] mLevels;
	private String[] mLevelPacks;
	private int mLevelIndex = 0;
	private int mLevelPackIndex = 0;
	private Context mContext;
	private boolean mSetup = false;
	private Progress mProgress;
	private boolean mDrawTick = false;
	
	private int mBtnSize = 48;
	private int mBtnSep = 8;
	private int mBtnBorder = 16;
	private int mFontSize = 16;
	
	private int mClickSound = -1;
	
	@Override
	public void Setup(Context context) {
		if(mSetup)
		{
			mDrawTick = mProgress.IsComplete(mWorld.getIdentifier());
			return;
		}
		mContext = context;
		mProgress = new Progress(context);
		
		mBtnSize = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_size);
		mBtnSep = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_sep);
		mBtnBorder = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_border);
		mFontSize = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_font_size);
	
		try
		{
			mClickSound = SoundManager.LoadSound("Sounds/Click.ogg");
		} catch(IOException io_ex)
		{
			//TODO log
		}
		
		try
		{
			NinePatchData btn_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/Blank64x64.png")), 24, 24, 24, 24);
		
			mLevelPackName = new Widget(btn_np, new Rect(mBtnSize + mBtnSep + mBtnBorder, mBtnBorder, mScreenWidth - (mBtnSize + mBtnBorder + mBtnSep), mBtnBorder + mBtnSize));
			
			Widget levelPackLeft = new Widget(btn_np, new Rect(mBtnBorder, mBtnBorder, mBtnBorder + mBtnSize, mBtnBorder + mBtnSize));
			levelPackLeft.setText("<");

			Widget levelPackRight = new Widget(btn_np, new Rect(mScreenWidth - (mBtnBorder + mBtnSize), mBtnBorder, mScreenWidth - mBtnBorder, mBtnBorder + mBtnSize));
			levelPackRight.setText(">");
			

			mLevelName = new Widget(btn_np, new Rect(mBtnBorder, mBtnSize + mBtnSep + mBtnBorder, mScreenWidth - mBtnBorder, mBtnSize + mBtnSep + mBtnBorder + mBtnSize)); 
			mLevelName.setText("Level name");
			
			Widget scrollLeft = new Widget(btn_np, new Rect(mBtnBorder, mScreenHeight - (mBtnSize + mBtnBorder), mBtnSize + mBtnBorder, mScreenHeight - mBtnBorder));
			scrollLeft.setText("<");
			
			Widget scrollRight = new Widget(btn_np, new Rect(mScreenWidth - (mBtnSize + mBtnBorder), mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder));
			scrollRight.setText(">");
			
			Widget playMap = new Widget(btn_np, new Rect(mBtnSize + mBtnBorder + mBtnSep, mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth - (mBtnSize + mBtnBorder + mBtnSep), mScreenHeight - mBtnBorder));
			playMap.setText("Play");
			
			Widget showTutorial = new Widget(btn_np, new Rect(mScreenWidth / 2 + mBtnSep, mScreenHeight - (mBtnSize * 2 + mBtnBorder) - mBtnSep, mScreenWidth - mBtnBorder, mScreenHeight - (mBtnSize * 1 + mBtnBorder) - mBtnSep));
			showTutorial.setText("Tutorial");
			
			Widget loadEditor = new Widget(btn_np, new Rect(mBtnBorder , mScreenHeight - (mBtnSize * 2 + mBtnBorder) - mBtnSep, mScreenWidth / 2 - mBtnSep, mScreenHeight - (mBtnSize * 1 + mBtnBorder) - mBtnSep));
			loadEditor.setText("Editor");			
			
			
			mWidgetPage.setFontSize(mFontSize);
			mWidgetPage.addWidget(levelPackLeft);
			mWidgetPage.addWidget(levelPackRight);
			mWidgetPage.addWidget(mLevelPackName);
			mWidgetPage.addWidget(mLevelName);
			mWidgetPage.addWidget(scrollRight);
			mWidgetPage.addWidget(scrollLeft);
			mWidgetPage.addWidget(playMap);
			mWidgetPage.addWidget(showTutorial);
			mWidgetPage.addWidget(loadEditor);
			
		
			scrollRight.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					mLevelIndex++;
					mLevelIndex %= mLevels[mLevelPackIndex].length;
					ChangeLevel();
					SoundManager.PlaySound(mClickSound);
				}
			});
			scrollLeft.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					mLevelIndex--;
					if(mLevelIndex < 0)
						mLevelIndex += mLevels[mLevelPackIndex].length;
					ChangeLevel();
					SoundManager.PlaySound(mClickSound);
				}
			});
			levelPackLeft.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					mLevelPackIndex--;
					if(mLevelPackIndex < 0)
						mLevelPackIndex += mLevels.length;
					mLevelIndex = 0;
					ChangeLevel();
					SoundManager.PlaySound(mClickSound);
				}
			});
			levelPackRight.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					mLevelPackIndex++;
					mLevelPackIndex %= mLevels.length;
					mLevelIndex = 0;
					ChangeLevel();
					SoundManager.PlaySound(mClickSound);
				}
			});
			
			playMap.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					mPendMode = new ModeGame(mWorld, ModeMenu.this, mProgress);
					SoundManager.PlaySound(mClickSound);
				}
			});
			
			showTutorial.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					mPendMode = new ModeTutorial(false);
				}
			});
			
			loadEditor.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					Toast t = Toast.makeText(mContext, "Coming soon!", Toast.LENGTH_SHORT);
					t.show();
				}
			});
			
		} catch(IOException io_ex)
		{
			//TODO log
		}
		
		try
		{
			String[] level_packs = context.getAssets().list("Levels");
			int level_pack_id = 0;
			
			mLevels = new String[level_packs.length][];
			mLevelPacks = new String[level_packs.length];
			
			for (String level_pack : level_packs) {
				String[] levels = context.getAssets().list("Levels/" + level_pack);
				ArrayList<String> level_list = new ArrayList<String>();
				for (String level : levels) {
					level_list.add("Levels/" + level_pack + "/" + level);
				}
				
				mLevels[level_pack_id] = new String[levels.length];
				level_list.toArray(mLevels[level_pack_id]);
				
				mLevelPacks[level_pack_id] = level_pack;
				
				level_pack_id++;
			}
			
			
			
		} catch(IOException io_ex)
		{
			//TODO log
		}
		
		mGameDrawer.Setup(context, context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.preview_grid_size));
		ChangeLevel();
		mSetup = true;
	}
	
	/**
	 * Updates the gui with a new level
	 */
	private void ChangeLevel()
	{
		try
		{
			mWorld = new World(mContext.getAssets().open(mLevels[mLevelPackIndex][mLevelIndex]));
			mWorld.setIdentifier(mLevels[mLevelPackIndex][mLevelIndex]);
			mLevelPackName.setText(mLevelPacks[mLevelPackIndex]);
			mLevelName.setText(Integer.toString(mLevelIndex+1)+ "/" + Integer.toString(mLevels[mLevelPackIndex].length) + ": " + mWorld.getLevelName());
			mGameDrawer.CreateBackground(mWorld);
			mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * mGameDrawer.getGridSize() / 2), mBtnBorder + mBtnSize + mBtnSep + mBtnSize + 4);
			mDrawTick = mProgress.IsComplete(mWorld.getIdentifier());
		} catch(IOException io_ex)
		{
			//TODO log
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
	public ModeAction Tick(int timespan) {
		mWidgetPage.Tick(timespan);
		mGameDrawer.Tick(timespan);
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		mGameDrawer.Draw(canvas, mWorld);
		mWidgetPage.Draw(canvas);
		if(mDrawTick)
		{
			mGameDrawer.GetTick().DrawCurrentFrame(canvas, mScreenWidth - mBtnBorder - mBtnSize + mBtnBorder - mBtnSep,  mBtnBorder + mBtnSep + mBtnSize + mBtnSep);
		}
		
		super.Redraw(canvas);
	}
	
	@Override
	public void handleTap(int x, int y) {
		mWidgetPage.handleTap(x, y);
	}
}
