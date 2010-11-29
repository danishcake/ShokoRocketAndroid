package uk.danishcake.shokorocket.moding;

import java.io.IOException;
import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.simulation.SPWorld;
import uk.danishcake.shokorocket.sound.SoundManager;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


public class ModeSPMenu extends Mode {
	private Widget mLevelName;
	private Widget mLevelPackName;
	private SPWorld mWorld = null;
	private GameDrawer mGameDrawer;
	private boolean mSetup = false;
	private boolean mEditorLoaded = false;
	private Progress mProgress;
	private boolean mDrawTick = false;
	private boolean mMakeTrainingOffer = false;
	private SkinProgress mSkin;
	
	@Override
	public void Setup(Context context) {
		mGameDrawer = new GameDrawer();
		if(mSetup)
		{
			mProgress.AssessUnlockable(mSkin);
			mGameDrawer.Setup(context, context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.preview_grid_size), mSkin);
			mDrawTick = mProgress.IsComplete(mWorld.getIdentifier());
			if(mEditorLoaded)
				LoadLevelList();
			mEditorLoaded = false;
			ChangeLevel();
			return;
		}
		super.Setup(context);
		mProgress = new Progress(context);
		mSkin = new SkinProgress(context);
		mProgress.AssessUnlockable(mSkin);
		mMakeTrainingOffer = Progress.IsFirstRun(context);
		
		mGameDrawer.Setup(context, context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.preview_grid_size), mSkin);
				
		mLevelPackName = new Widget(mBtnNP, new Rect(mBtnSize + mBtnSep + mBtnBorder, mBtnBorder, mScreenWidth - (mBtnSize + mBtnBorder + mBtnSep), mBtnBorder + mBtnSize));
		
		Widget levelPackLeft = new Widget(mBtnNP, new Rect(mBtnBorder, mBtnBorder, mBtnBorder + mBtnSize, mBtnBorder + mBtnSize));
		levelPackLeft.setText("<");

		Widget levelPackRight = new Widget(mBtnNP, new Rect(mScreenWidth - (mBtnBorder + mBtnSize), mBtnBorder, mScreenWidth - mBtnBorder, mBtnBorder + mBtnSize));
		levelPackRight.setText(">");
		

		mLevelName = new Widget(mBtnNP, new Rect(mBtnBorder, mBtnSize + mBtnSep + mBtnBorder, mScreenWidth - mBtnBorder, mBtnSize + mBtnSep + mBtnBorder + mBtnSize)); 
		mLevelName.setText(context.getString(R.string.menu_level_name));
		
		Widget scrollLeft = new Widget(mBtnNP, new Rect(mBtnSize + mBtnBorder + mBtnSep, mScreenHeight - (mBtnSize + mBtnBorder), mBtnSize * 2 + mBtnBorder + mBtnSep, mScreenHeight - mBtnBorder));
		scrollLeft.setText("<");
		
		Widget scrollRight = new Widget(mBtnNP, new Rect(mScreenWidth - (mBtnSize + mBtnBorder), mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder));
		scrollRight.setText(">");
		
		Widget playMap = new Widget(mBtnNP, new Rect(mBtnBorder + (mBtnSize+ mBtnSep) * 2, mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth - (mBtnSize + mBtnBorder + mBtnSep), mScreenHeight - mBtnBorder));
		playMap.setText(context.getString(R.string.menu_play));
		
		Widget toggleMP = new Widget(mBtnNP, new Rect(mBtnBorder, mScreenHeight - (mBtnSize + mBtnBorder), mBtnSize + mBtnBorder, mScreenHeight - mBtnBorder));
		toggleMP.setText("MP");
		
		Widget unlocks = new Widget(mBtnNP, new Rect(mScreenWidth / 2 + mBtnSep, mScreenHeight - (mBtnSize * 2 + mBtnBorder) - mBtnSep, mScreenWidth - mBtnBorder, mScreenHeight - (mBtnSize * 1 + mBtnBorder) - mBtnSep));
		unlocks.setText(context.getString(R.string.menu_unlocks));
		
		Widget loadEditor = new Widget(mBtnNP, new Rect(mBtnBorder , mScreenHeight - (mBtnSize * 2 + mBtnBorder) - mBtnSep, mScreenWidth / 2 - mBtnSep, mScreenHeight - (mBtnSize * 1 + mBtnBorder) - mBtnSep));
		loadEditor.setText(context.getString(R.string.menu_editor));		
		
		mWidgetPage.setFontSize(mFontSize);
		mWidgetPage.addWidget(levelPackLeft);
		mWidgetPage.addWidget(levelPackRight);
		mWidgetPage.addWidget(mLevelPackName);
		mWidgetPage.addWidget(mLevelName);
		mWidgetPage.addWidget(scrollRight);
		mWidgetPage.addWidget(scrollLeft);
		mWidgetPage.addWidget(playMap);
		mWidgetPage.addWidget(unlocks);
		mWidgetPage.addWidget(loadEditor);
		mWidgetPage.addWidget(toggleMP);
		
	
		scrollRight.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				mProgress.nextLevel();
				ChangeLevel();
				SoundManager.PlaySound(mClickSound);
			}
		});
		scrollLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				mProgress.prevLevel();
				ChangeLevel();
				SoundManager.PlaySound(mClickSound);
			}
		});
		levelPackLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				mProgress.prevLevelPack();
				ChangeLevel();
				SoundManager.PlaySound(mClickSound);
			}
		});
		levelPackRight.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				mProgress.nextLevelPack();
				ChangeLevel();
				SoundManager.PlaySound(mClickSound);
			}
		});
		
		playMap.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(mPendMode == null)
					mPendMode = new ModeSPGame(mWorld, ModeSPMenu.this, mProgress, mSkin);
				SoundManager.PlaySound(mClickSound);
			}
		});
		
		unlocks.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(mPendMode == null)
					mPendMode = new ModeUnlocks(ModeSPMenu.this, mSkin, mProgress);
				SoundManager.PlaySound(mClickSound);
			}
		});
		
		loadEditor.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(mPendMode == null)
				{
					if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
					{
						Toast.makeText(mContext, R.string.menu_editor_requies_storage, Toast.LENGTH_LONG).show();
						return;
					}
					if(mProgress.getLevelPack().equals("My Levels"))
					{
						AlertDialog.Builder builder = new Builder(mContext);
						builder.setMessage(R.string.menu_edit_this_level_prompt);
						builder.setPositiveButton(R.string.menu_edit, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								try
								{
									mSemaphore.acquire();
									mPendMode = new ModeEditor(ModeSPMenu.this, mWorld, mSkin);
									mSemaphore.release();
								} catch(InterruptedException int_ex)
								{
									Log.e("ModeSPMenu.Setup", "Semaphore interupted");
								}
							}
						});
						builder.setNeutralButton(R.string.menu_new, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								try
								{
									mSemaphore.acquire();
									mPendMode = new ModeEditor(ModeSPMenu.this, null, mSkin);
									mEditorLoaded = true;
									mSemaphore.release();
								} catch(InterruptedException int_ex)
								{
									Log.e("ModeSPMenu.Setup", "Semaphore interupted");
								}
							}
						});
						builder.create().show();
					} else
					{
						mPendMode = new ModeEditor(ModeSPMenu.this, null, mSkin);
						mEditorLoaded = true;
					}

					SoundManager.PlaySound(mClickSound);
				}
			}
		});
		
		toggleMP.setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				if(mPendMode == null)
				{
					mPendMode = new ModeMPMenu(ModeSPMenu.this, mSkin); 
				}
			}
		});
		
		LoadLevelList();

		ChangeLevel();
		mSetup = true;
	}
	
	/**
	 * Loads a list of levels from external storage and assets
	 */
	private void LoadLevelList()
	{
		mProgress.Reload();
	}
	
	/**
	 * Updates the gui with a new level
	 */
	private void ChangeLevel()
	{
		try
		{
			mLevelPackName.setText(mProgress.getLevelPack());
			mWorld = mProgress.getWorld();

			mLevelName.setText(Integer.toString(mProgress.getLevelIndex() + 1) + "/" + Integer.toString(mProgress.getLevelPackSize()) + ": " + mWorld.getLevelName()); 
			mGameDrawer.CreateBackground(mWorld);
			mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * mGameDrawer.getGridSize() / 2), mBtnBorder + mBtnSize + mBtnSep + mBtnSize + 4);
			mDrawTick = mProgress.IsComplete(mWorld.getIdentifier());
		} catch(IOException io_ex)
		{
			Log.e("ModeSPMenu.ChangeLevel", "Error changing level");
			mDrawTick = false;
			if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				mLevelName.setText(mContext.getString(R.string.menu_error_loading));
			else
				mLevelName.setText(mContext.getString(R.string.menu_external_storage_unmounted));
			mWorld = null;
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
		mGameDrawer.Tick(timespan);
		if(mMakeTrainingOffer)
		{
			Handler handler = new Handler(mContext.getMainLooper());
			handler.post(mTrainingOffer);
			mMakeTrainingOffer = false;
		}
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		if(mWorld != null)
			mGameDrawer.Draw(canvas, mWorld);
		else
		{
			//TODO draw a SD card symbol
		}
		mWidgetPage.Draw(canvas);
		if(mDrawTick)
		{
			mGameDrawer.GetTick().DrawCurrentFrame(canvas, mScreenWidth - mBtnBorder - mBtnSize + mBtnBorder - mBtnSep,  mBtnBorder + mBtnSep + mBtnSize + mBtnSep);
		}
		
		super.Redraw(canvas);
	}
	
	private Runnable mTrainingOffer = new Runnable() {
		@Override
		public void run() {
			try
			{
				mSemaphore.acquire();
				AlertDialog.Builder ad_builder = new Builder(mContext);
				ad_builder.setMessage(R.string.menu_run_training_detail);
				ad_builder.setTitle(R.string.menu_run_training);
				ad_builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try
						{
							mSemaphore.acquire();
							
							mSemaphore.release();
						} catch(InterruptedException int_ex)
						{
							Log.e("ModeSPMenu.mTrainingOffer.negative", "Semaphore interupted");
						}
					}
				});
				ad_builder.setPositiveButton(R.string.menu_train_me, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try
						{
							mSemaphore.acquire();
							mProgress.gotoTrainingPack();
							ChangeLevel();
							mPendMode = new ModeSPGame(mWorld, ModeSPMenu.this, mProgress, mSkin);
							mSemaphore.release();
						} catch(InterruptedException int_ex)
						{
							Log.e("ModeSPMenu.mTrainingOffer.negative", "Semaphore interupted");
						}
					}
				});
				AlertDialog ad = ad_builder.create();
				ad.show();
				
				mSemaphore.release();
			} catch(InterruptedException int_ex)
			{
				Log.e("ModeSPMenu.mTrainingOffer", "Semaphore interupted");
			}
		}
	};
}
