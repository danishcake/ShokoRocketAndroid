package uk.danishcake.shokorocket.moding;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.simulation.World;
import uk.danishcake.shokorocket.sound.SoundManager;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;


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
	private boolean mEditorLoaded = false;
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
			if(mEditorLoaded)
				LoadLevelList();
			mEditorLoaded = false;
			ChangeLevel();
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
					if(mPendMode == null)
						mPendMode = new ModeGame(mWorld, ModeMenu.this, mProgress);
					SoundManager.PlaySound(mClickSound);
				}
			});
			
			showTutorial.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mPendMode == null)
						mPendMode = new ModeTutorial();
					SoundManager.PlaySound(mClickSound);
				}
			});
			
			loadEditor.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mPendMode == null)
					{
						if(mLevelPacks[mLevelPackIndex].equals("My Levels"))
						{
							AlertDialog.Builder builder = new Builder(mContext);
							builder.setMessage("Edit this level?");
							builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									try
									{
										mSemaphore.acquire();
										mPendMode = new ModeEditor(ModeMenu.this, mWorld);
										mSemaphore.release();
									} catch(InterruptedException int_ex)
									{
										Log.e("ModeMenu.Setup", "Semaphore interupted");
									}
								}
							});
							builder.setNeutralButton("New", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									try
									{
										mSemaphore.acquire();
										mPendMode = new ModeEditor(ModeMenu.this, null);
										mEditorLoaded = true;
										mSemaphore.release();
									} catch(InterruptedException int_ex)
									{
										Log.e("ModeMenu.Setup", "Semaphore interupted");
									}
								}
							});
							builder.create().show();
						} else
						{
							mPendMode = new ModeEditor(ModeMenu.this, null);
							mEditorLoaded = true;
						}
	
						SoundManager.PlaySound(mClickSound);
					}
				}
			});
			
		} catch(IOException io_ex)
		{
			//TODO log
		}
		
		LoadLevelList();
		
		mGameDrawer.Setup(context, context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.preview_grid_size));
		ChangeLevel();
		mSetup = true;
	}
	
	/**
	 * Loads a list of levels from external storage and assets
	 */
	private void LoadLevelList()
	{
		try
		{
			String[] level_packs = mContext.getAssets().list("Levels");
			int level_pack_id = 0;
			int total_length = level_packs.length;
			
			//If external storage mounted then search it for levels
			File[] user_packs = new File[]{};
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				File root = new File(Environment.getExternalStorageDirectory(), "ShokoRocket");
				user_packs = root.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isDirectory();
					}
				});
				total_length += user_packs.length;
			}
			
			
			
			mLevels = new String[total_length][];
			mLevelPacks = new String[total_length];
			
			//First list levels in assets
			for (String level_pack : level_packs) {
				String[] levels = mContext.getAssets().list("Levels/" + level_pack);
				ArrayList<String> level_list = new ArrayList<String>();
				for (String level : levels) {
					level_list.add("assets://Levels/" + level_pack + "/" + level);
				}
				
				mLevels[level_pack_id] = new String[levels.length];
				level_list.toArray(mLevels[level_pack_id]);	
				mLevelPacks[level_pack_id] = level_pack;
				
				level_pack_id++;
			}
			
			//Now list levels on external storage
			for (File user_pack : user_packs)
			{
				File[] levels = user_pack.listFiles();
				ArrayList<String> level_list = new ArrayList<String>();
				for(File level : levels)
				{
					String path = level.getPath();
					if(path.endsWith(".Level"))
					{
						level_list.add(level.getAbsolutePath());
					}
				}

				mLevels[level_pack_id] = new String[level_list.size()];
				level_list.toArray(mLevels[level_pack_id]);
				mLevelPacks[level_pack_id] = user_pack.getName();		
			
				level_pack_id++;
			}
		} catch(IOException io_ex)
		{
			//TODO log
		}
	}
	
	/**
	 * Updates the gui with a new level
	 */
	private void ChangeLevel()
	{
		try
		{
			String level_name = mLevels[mLevelPackIndex][mLevelIndex];
			mLevelPackName.setText(mLevelPacks[mLevelPackIndex]);
			
			if(level_name.startsWith("assets://"))
			{
				mWorld = new World(mContext.getAssets().open(level_name.substring(9)));
			} else
			{
				mWorld = new World(new FileInputStream(level_name));
				File level_file = new File(level_name);
				mWorld.setFilename(level_file.getName());
			}
			mWorld.setIdentifier(mLevels[mLevelPackIndex][mLevelIndex]);
			mLevelName.setText(Integer.toString(mLevelIndex+1)+ "/" + Integer.toString(mLevels[mLevelPackIndex].length) + ": " + mWorld.getLevelName());
			mGameDrawer.CreateBackground(mWorld);
			mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * mGameDrawer.getGridSize() / 2), mBtnBorder + mBtnSize + mBtnSep + mBtnSize + 4);
			mDrawTick = mProgress.IsComplete(mWorld.getIdentifier());
		} catch(IOException io_ex)
		{
			Log.e("ModeMenu.ChangeLevel", "Error changing level");
			mDrawTick = false;
			mLevelName.setText("Error loading");
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
