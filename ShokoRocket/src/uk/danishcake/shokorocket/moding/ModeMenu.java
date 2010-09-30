package uk.danishcake.shokorocket.moding;

import java.io.IOException;

import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.WorldSelector;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.sound.SoundManager;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


public class ModeMenu extends Mode {
	private WidgetPage mWidgetPage = new WidgetPage();
	private Widget mLevelName;
	private Widget mLevelPackName;
	private Context mContext;
	private boolean mSetup = false;
	private boolean mEditorLoaded = false;
	private Progress mProgress;
	private boolean mDrawTick = false;
	private boolean mMakeTrainingOffer = false;
	private WorldSelector mSelector = null;
	
	private int mBtnSize = 48;
	private int mBtnSep = 8;
	private int mBtnBorder = 16;
	private int mFontSize = 16;
	
	private int mClickSound = -1;
	
	@Override
	public void Setup(Context context) {
		if(mSetup)
		{
			//mSelector. //TODO restore tick
			//mDrawTick = mProgress.IsComplete(mWorld.getIdentifier());
			if(mEditorLoaded)
				LoadLevelList();
			mEditorLoaded = false;
			ChangeLevel();
			return;
		}
		mContext = context;
		mProgress = new Progress(context);
		mMakeTrainingOffer = Progress.IsFirstRun(context);
		
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
			mSelector = new WorldSelector(mProgress, mContext);
			
			NinePatchData btn_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/Blank64x64.png")), 24, 24, 24, 24);
		
			mLevelPackName = new Widget(btn_np, new Rect(mBtnSize + mBtnSep + mBtnBorder, mBtnBorder, mScreenWidth - (mBtnSize + mBtnBorder + mBtnSep), mBtnBorder + mBtnSize));
						
			mLevelName = new Widget(btn_np, new Rect(mBtnBorder, mBtnSize + mBtnSep + mBtnBorder, mScreenWidth - mBtnBorder, mBtnSize + mBtnSep + mBtnBorder + mBtnSize)); 
			mLevelName.setText(context.getString(R.string.menu_level_name));
			
			Widget playMap = new Widget(btn_np, new Rect(mBtnSize + mBtnBorder + mBtnSep, mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth - (mBtnSize + mBtnBorder + mBtnSep), mScreenHeight - mBtnBorder));
			playMap.setText(context.getString(R.string.menu_play));
			
			Widget showTutorial = new Widget(btn_np, new Rect(mScreenWidth / 2 + mBtnSep, mScreenHeight - (mBtnSize * 2 + mBtnBorder) - mBtnSep, mScreenWidth - mBtnBorder, mScreenHeight - (mBtnSize * 1 + mBtnBorder) - mBtnSep));
			showTutorial.setText(context.getString(R.string.menu_tutorial));
			
			Widget loadEditor = new Widget(btn_np, new Rect(mBtnBorder , mScreenHeight - (mBtnSize * 2 + mBtnBorder) - mBtnSep, mScreenWidth / 2 - mBtnSep, mScreenHeight - (mBtnSize * 1 + mBtnBorder) - mBtnSep));
			loadEditor.setText(context.getString(R.string.menu_editor));			
			
			
			mWidgetPage.setFontSize(mFontSize);
			mWidgetPage.addWidget(mLevelPackName);
			mWidgetPage.addWidget(mLevelName);
			mWidgetPage.addWidget(playMap);
			mWidgetPage.addWidget(showTutorial);
			mWidgetPage.addWidget(loadEditor);
					
			playMap.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					try
					{
						if(mPendMode == null)
							mPendMode = new ModeGame(mProgress.getWorld(), ModeMenu.this, mProgress);
					} catch(IOException io_ex)
					{
						Log.e("playMap.onClick", "File not found, not loading level");
					}
					SoundManager.PlaySound(mClickSound);
				}
			});
			
			showTutorial.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mPendMode == null)
						mPendMode = new ModeTutorial(ModeMenu.this);
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
										mPendMode = new ModeEditor(ModeMenu.this, mProgress.getWorld(mSelector.getSelectedWorld()));
										mSemaphore.release();
									} catch(InterruptedException int_ex)
									{
										Log.e("ModeMenu.Setup", "Semaphore interupted");
									} catch(IOException io_ex)
									{
										Log.e("ModeMenu.Setup", "IOException, not loading level");
									}
								}
							});
							builder.setNeutralButton(R.string.menu_new, new DialogInterface.OnClickListener() {
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

//			mLevelName.setText(Integer.toString(mProgress.getLevelIndex() + 1) + "/" + Integer.toString(mProgress.getLevelPackSize()) + ": " + mWorld.getLevelName());
			//TODO restore mLevelName
			//mDrawTick = mProgress.IsComplete(mWorld.getIdentifier());
			//TODO restore drawTick
		} finally 
		{
			
		}
		/*catch(IOException io_ex)
		{
		{
			Log.e("ModeMenu.ChangeLevel", "Error changing level");
			mDrawTick = false;
			if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				mLevelName.setText(mContext.getString(R.string.menu_error_loading));
			else
				mLevelName.setText(mContext.getString(R.string.menu_external_storage_unmounted));
		}*/	
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
		mSelector.Tick(timespan);
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
		mSelector.Draw(canvas, new Rect(0, 0, 480, 800));
		mWidgetPage.Draw(canvas);
		if(mDrawTick)
		{
			//TODO restore tick
			//mGameDrawer.GetTick().DrawCurrentFrame(canvas, mScreenWidth - mBtnBorder - mBtnSize + mBtnBorder - mBtnSep,  mBtnBorder + mBtnSep + mBtnSize + mBtnSep);
		}
		
		super.Redraw(canvas);
	}
	
	@Override
	public void handleTap(int x, int y) {
		mWidgetPage.handleTap(x, y);
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
							Log.e("ModeMenu.mTrainingOffer.negative", "Semaphore interupted");
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
							mPendMode = new ModeGame(mProgress.getWorld(), ModeMenu.this, mProgress);
							mSemaphore.release();
						} catch(InterruptedException int_ex)
						{
							Log.e("ModeMenu.mTrainingOffer.negative", "Semaphore interupted");
						} catch(IOException io_ex)
						{
							Log.e("ModeMenu.mTrainingOffer.negative", "IOException, not loading level");
						}
					}
				});
				AlertDialog ad = ad_builder.create();
				ad.show();
				
				mSemaphore.release();
			} catch(InterruptedException int_ex)
			{
				Log.e("ModeMenu.mTrainingOffer", "Semaphore interupted");
			}
		}
	};
	
	@Override
	public void previewGesture(int x1, int y1, int x2, int y2,
			Direction direction) {
		mSelector.updateGesture(x2, y2);
	}
	
	@Override
	public void clearPreviewGesture() {
		mSelector.finishGesture();
	}
}
