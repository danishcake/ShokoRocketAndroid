package uk.danishcake.shokorocket.moding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import uk.danishcake.shokorocket.animation.Animation;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.simulation.World;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Interpolator;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Interpolator.Result;


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
	
	@Override
	public void Setup(Context context) {
		mContext = context;
		
		try
		{
			NinePatchData btn_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/Blank64x64.png")), 24, 24, 24, 24);
		
			mLevelPackName = new Widget(btn_np, new Rect(68, 16, mScreenWidth - 68, 64));
			Widget levelPackLeft = new Widget(btn_np, new Rect(16, 16, 64, 64));
			levelPackLeft.setText("<");
			Widget levelPackRight = new Widget(btn_np, new Rect(mScreenWidth - 64, 16, mScreenWidth - 16, 64));
			levelPackRight.setText(">");
			

			mLevelName = new Widget(btn_np, new Rect(16, 68, mScreenWidth - 16, 68 + 48)); 
			mLevelName.setText("Level name");
			
			Widget scrollLeft = new Widget(btn_np, new Rect(16, mScreenHeight - 64, 64, mScreenHeight - 16));
			scrollLeft.setText("<");
			
			Widget scrollRight = new Widget(btn_np, new Rect(mScreenWidth - 64, mScreenHeight - 64, mScreenWidth - 16, mScreenHeight - 16));
			scrollRight.setText(">");
			
			Widget playMap = new Widget(btn_np, new Rect(68, mScreenHeight - 64, mScreenWidth - 68, mScreenHeight - 16));
			playMap.setText("Play");
			
			
			
			mWidgetPage.addWidget(levelPackLeft);
			mWidgetPage.addWidget(levelPackRight);
			mWidgetPage.addWidget(mLevelPackName);
			mWidgetPage.addWidget(mLevelName);
			mWidgetPage.addWidget(scrollRight);
			mWidgetPage.addWidget(scrollLeft);
			mWidgetPage.addWidget(playMap);
			
			
			scrollRight.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					mLevelIndex++;
					mLevelIndex %= mLevels[mLevelPackIndex].length;
					ChangeLevel();

				}
			});
			scrollLeft.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					mLevelIndex--;
					if(mLevelIndex < 0)
						mLevelIndex += mLevels[mLevelPackIndex].length;
					ChangeLevel();
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
				}
			});
			levelPackRight.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					mLevelPackIndex++;
					mLevelPackIndex %= mLevels.length;
					mLevelIndex = 0;
					ChangeLevel();
				}
			});
			
		} catch(IOException io_ex)
		{
			//TODO log
		}
		
		try
		{
			ArrayList<String> level_list = new ArrayList<String>();
			String[] level_packs = context.getAssets().list("Levels");
			int level_pack_id = 0;
			
			mLevels = new String[level_packs.length][];
			mLevelPacks = new String[level_packs.length];
			
			for (String level_pack : level_packs) {
				String[] levels = context.getAssets().list("Levels/" + level_pack);
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
		
		mGameDrawer.Setup(context, 16);
		ChangeLevel();		
	}
	
	/**
	 * Updates the gui with a new level
	 */
	private void ChangeLevel()
	{
		try
		{
			mWorld = new World(mContext.getAssets().open(mLevels[mLevelPackIndex][mLevelIndex]));
			mLevelPackName.setText(mLevelPacks[mLevelPackIndex]);
			mLevelName.setText(Integer.toString(mLevelIndex+1)+ "/" + Integer.toString(mLevels[mLevelPackIndex].length) + ": " + mWorld.getLevelName());
			mGameDrawer.CreateBackground(mWorld);
			mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * 16 / 2), 68 + 48 + 4);
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
		mGameDrawer.Tick(timespan);
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		mGameDrawer.Draw(canvas, mWorld);
		mWidgetPage.Draw(canvas);
	}
	
	@Override
	public void handleTap(int x, int y) {
		mWidgetPage.handleTap(x, y);
	}
}
