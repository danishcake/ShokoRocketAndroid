package uk.danishcake.shokorocket.moding;

import java.io.IOException;

import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.MPWorld;
import uk.danishcake.shokorocket.simulation.SPWorld;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.sound.MusicManager;
import uk.danishcake.shokorocket.sound.SoundManager;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


public class ModeMenu extends Mode {
	
	//SP related stuff
	private Widget mLevelName;
	private Widget mLevelPackName;
	private Widget mPlayButton;
	private Point mPlayPosition;
	private SPWorld mWorld = null;
	private GameDrawer mGameDrawer;
	private GameDrawer mGameDrawerNorm;
	private GameDrawer mGameDrawerRot;
	private boolean mSetup = false;
	private boolean mEditorLoaded = false;
	private Progress mProgress;
	private boolean mDrawTick = false;
	private boolean mMakeTrainingOffer = false;
	private SkinProgress mSkin;
	private SPWorld mPendWorld = null;
	private int mWorldPendTimer = 0;
	private final int WORLD_TRANSITION_TIME = 250;
	private Vector2i mWorldOffset = new Vector2i(0, 0);
	private Vector2i mPendWorldOffset = new Vector2i(0, 0);
	private Direction mWorldDirection = Direction.West; //Transition from this direction

	/* AI widgets */
	Widget mAI1;
	Widget mAI2;
	Widget mAI3;
	Widget mLaunchAI;
	
	//Widget pages and transition control
	private WidgetPage mAIPage = new WidgetPage();
	private WidgetPage mIPPage = new WidgetPage();
	private WidgetPage mPuzzlePage = mWidgetPage;
	private WidgetPage mPendPage = null; 
	private int mPagePendTimer = 0;
	private static int PAGE_TRANSITION_TIME = 250;

	public ModeMenu(Progress progress)
	{
		mProgress = progress;
	}
	
	@Override
	public void Setup(Context context) {
		MusicManager.PlayMenuMusic();
		if(mSetup)
		{
			mProgress.AssessUnlockable(mSkin);
			mDrawTick = mProgress.IsComplete(mWorld.getIdentifier());
			if(mEditorLoaded)
				LoadLevelList();
			mEditorLoaded = false;
			ChangeLevel();
			return;
		}

		super.Setup(context);
		mSkin = new SkinProgress(context);
		mProgress.AssessUnlockable(mSkin);
		mMakeTrainingOffer = Progress.IsFirstRun(context);

		mGameDrawer = new GameDrawer();
		mGameDrawerNorm = new GameDrawer();
		mGameDrawerRot = new GameDrawer();
		mGameDrawer.Setup(context, context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.preview_grid_size), mSkin, false);

		//Setup puzzle page widgets
		{
			mLevelPackName = new Widget(mBtnNP, new Rect(mBtnSize + mBtnSep + mBtnBorder, mBtnBorder, mScreenWidth - (mBtnSize + mBtnBorder + mBtnSep), mBtnBorder + mBtnSize));

			Widget levelPackLeft = new Widget(mBtnNP, new Rect(mBtnBorder, mBtnBorder, mBtnBorder + mBtnSize, mBtnBorder + mBtnSize));
			levelPackLeft.setText("<");

			Widget levelPackRight = new Widget(mBtnNP, new Rect(mScreenWidth - (mBtnBorder + mBtnSize), mBtnBorder, mScreenWidth - mBtnBorder, mBtnBorder + mBtnSize));
			levelPackRight.setText(">");

			mLevelName = new Widget(mBtnNP, new Rect(mBtnBorder, mBtnSize + mBtnSep + mBtnBorder, mScreenWidth - mBtnBorder, mBtnSize + mBtnSep + mBtnBorder + mBtnSize)); 
			mLevelName.setText(context.getString(R.string.menu_level_name));

			Widget scrollLeft = new Widget(mBtnNP, new Rect(-mBtnBorder, mScreenHeight / 2 - mBtnSize / 2, mBtnSize, mScreenHeight / 2 + mBtnSize / 2));
			scrollLeft.setText("<");

			Widget scrollRight = new Widget(mBtnNP, new Rect(mScreenWidth - mBtnSize, mScreenHeight / 2 - mBtnSize / 2, mScreenWidth + mBtnBorder, mScreenHeight / 2 + mBtnSize / 2));
			scrollRight.setText(">");

			Widget toggleMP = new Widget(mBtnNP, new Rect(mBtnBorder, mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth / 2 - mBtnSep / 2, mScreenHeight - mBtnBorder));
			toggleMP.setText("Online");

			Widget toggleAI = new Widget(mBtnNP, new Rect(mScreenWidth / 2 + mBtnSep / 2, mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder));
			toggleAI.setText("AI");

			Widget unlocks = new Widget(mBtnNP, new Rect(mScreenWidth / 2 + mBtnSep / 2, mScreenHeight - (mBtnSize * 2 + mBtnBorder) - mBtnSep, mScreenWidth - mBtnBorder, mScreenHeight - (mBtnSize * 1 + mBtnBorder) - mBtnSep));
			unlocks.setText(context.getString(R.string.menu_unlocks));

			Widget loadEditor = new Widget(mBtnNP, new Rect(mBtnBorder , mScreenHeight - (mBtnSize * 2 + mBtnBorder) - mBtnSep, mScreenWidth / 2 - mBtnSep / 2, mScreenHeight - (mBtnSize * 1 + mBtnBorder) - mBtnSep));
			loadEditor.setText(context.getString(R.string.menu_editor));

			Bitmap play_bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.play);
			mPlayPosition = new Point(mScreenWidth / 2 - play_bm.getWidth() / 2, mScreenHeight / 2 - play_bm.getHeight() / 2);
			mPlayButton = new Widget(play_bm, mPlayPosition);

			scrollRight.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mPendWorld == null)
					{
						mProgress.nextLevel();
						mWorldDirection = Direction.East;
						startTransition();
						SoundManager.PlaySound(mClickSound);
					}
				}
			});

			scrollLeft.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mPendWorld == null)
					{
						mProgress.prevLevel();
						mWorldDirection = Direction.West;
						startTransition();
						SoundManager.PlaySound(mClickSound);
					}
				}
			});

			levelPackLeft.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mPendWorld == null)
					{
						mProgress.prevLevelPack();
						mWorldDirection = Direction.North;
						startTransition();
						SoundManager.PlaySound(mClickSound);
					}
				}
			});

			levelPackRight.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mPendWorld == null)
					{
						mProgress.nextLevelPack();
						mWorldDirection = Direction.South;
						startTransition();
						SoundManager.PlaySound(mClickSound);
					}
				}
			});

			mPlayButton.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mPendMode == null)
					{
						try
						{
							SPWorld world = mProgress.getWorld();
							mPendMode = new ModeSPGame(world, ModeMenu.this, mProgress, mSkin, mGameDrawerNorm, mGameDrawerRot);
						} catch(Exception ex)
						{
							Log.e("ModeMenu.PlayButton.onClick", "Could not load level");
						}
					}
					SoundManager.PlaySound(mClickSound);
				}
			});

			unlocks.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mPendMode == null)
					{
						mGameDrawerNorm.Teardown();
						mGameDrawerRot.Teardown();
						mPendMode = new ModeUnlocks(ModeMenu.this, mSkin, mProgress);
					}
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
										mGameDrawerNorm.Teardown();
										mGameDrawerRot.Teardown();
										mPendMode = new ModeEditor(ModeMenu.this, mWorld, mSkin);
										mSemaphore.release();
									} catch(InterruptedException int_ex)
									{
										Log.e("ModeMenu.Setup", "Semaphore interupted");
									}
								}
							});
							builder.setNeutralButton(R.string.menu_new, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									try
									{
										mSemaphore.acquire();
										mGameDrawerNorm.Teardown();
										mGameDrawerRot.Teardown();
										mPendMode = new ModeEditor(ModeMenu.this, null, mSkin);
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
							mGameDrawerNorm.Teardown();
							mGameDrawerRot.Teardown();
							mPendMode = new ModeEditor(ModeMenu.this, null, mSkin);
							mEditorLoaded = true;
						}
	
						SoundManager.PlaySound(mClickSound);
					}
				}
			});

			toggleMP.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					gotoPage(mIPPage);
				}
			});

			toggleAI.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					gotoPage(mAIPage);
				}
			});


			mPuzzlePage.setFontSize(mFontSize);
			mPuzzlePage.addWidget(levelPackLeft);
			mPuzzlePage.addWidget(levelPackRight);
			mPuzzlePage.addWidget(mLevelPackName);
			mPuzzlePage.addWidget(mLevelName);
			mPuzzlePage.addWidget(scrollRight);
			mPuzzlePage.addWidget(scrollLeft);
			mPuzzlePage.addWidget(unlocks);
			mPuzzlePage.addWidget(loadEditor);
			mPuzzlePage.addWidget(toggleMP);
			mPuzzlePage.addWidget(toggleAI);
			mPuzzlePage.addWidget(mPlayButton);
		}
		
		//Setup AI page widgets
		{
			Widget toggleMP = new Widget(mBtnNP, new Rect(mBtnBorder, mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth / 2 - mBtnSep / 2, mScreenHeight - mBtnBorder));
			toggleMP.setText("Online");

			Widget toggleSP = new Widget(mBtnNP, new Rect(mScreenWidth / 2 + mBtnSep / 2, mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder));
			toggleSP.setText("Puzzle");

			Widget human = new Widget(mBtnNP, new Rect(mBtnBorder, mBtnBorder, mScreenWidth / 2 - mBtnSep / 2, mBtnBorder + mBtnSize));
			human.setText("P1");
			human.setEnabled(false);
			mAI1 = new Widget(mBtnNP, new Rect(mScreenWidth / 2 + mBtnSep / 2, mBtnBorder, mScreenWidth - mBtnBorder, mBtnBorder + mBtnSize));
			mAI1.setText("Easy AI");
			mAI2 = new Widget(mBtnNP, new Rect(mBtnBorder, mBtnBorder + mBtnSep * 1 + mBtnSize * 1, mScreenWidth / 2 - mBtnSep / 2, mBtnBorder + mBtnSep * 1 + mBtnSize * 2));
			mAI2.setText("Easy AI");
			mAI3 = new Widget(mBtnNP, new Rect(mScreenWidth / 2 + mBtnSep / 2, mBtnBorder + mBtnSep * 1 + mBtnSize * 1, mScreenWidth - mBtnBorder, mBtnBorder + mBtnSep * 1 + mBtnSize * 2));
			mAI3.setText("Easy AI");

			mLaunchAI = new Widget(mBtnNP, new Rect(mBtnBorder, mScreenHeight - mBtnBorder - mBtnSep - mBtnSize * 2, mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder - mBtnSep - mBtnSize * 1));
			mLaunchAI.setText("Launch");

			OnClickListener ai_change = new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(widget.getText().equals("Easy AI"))
					{
						widget.setText("Medium AI");
					} else if(widget.getText().equals("Medium AI"))
					{
						widget.setText("Hard AI");
					} else if(widget.getText().equals("Hard AI"))
					{
						widget.setText("None");
					} else
					{
						widget.setText("Easy AI");
					}
					mLaunchAI.setEnabled(!(mAI1.getText().equals("None") && 
										   mAI2.getText().equals("None") &&
										   mAI3.getText().equals("None")));
				}
			};

			mAI1.setOnClickListener(ai_change);
			mAI2.setOnClickListener(ai_change);
			mAI3.setOnClickListener(ai_change);

			toggleMP.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					gotoPage(mIPPage);
				}
			});

			toggleSP.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					gotoPage(mPuzzlePage);
				}
			});

			mLaunchAI.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mPendMode == null)
					{
						try {
							String ai_string = "";
							Widget[] ai_widgets = {mAI1, mAI2, mAI3};
							for (Widget ai_widget : ai_widgets) {
								if(ai_widget.getText().equals("Easy AI"))
									ai_string = ai_string + "E";
								else if(ai_widget.getText().equals("Medium AI"))
									ai_string = ai_string + "M";
								else if(ai_widget.getText().equals("Hard AI"))
									ai_string = ai_string + "H";
								else
									ai_string = ai_string + " ";
							}

							MPWorld world = new MPWorld(mContext.getAssets().open("MultiplayerLevels/MP001.Level"), ai_string);
							mGameDrawerRot.Teardown();
							mPendMode = new ModeMPGame(ModeMenu.this, mSkin, world, mGameDrawerNorm);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});

			mAIPage.setFontSize(mFontSize);
			mAIPage.addWidget(toggleSP);
			mAIPage.addWidget(mLaunchAI);
			mAIPage.addWidget(toggleMP);
			mAIPage.addWidget(human);
			mAIPage.addWidget(mAI1);
			mAIPage.addWidget(mAI2);
			mAIPage.addWidget(mAI3);
		}
		//Setup IP page widgets
		{
			
			Widget toggleAI = new Widget(mBtnNP, new Rect(mScreenWidth / 2 + mBtnSep / 2, mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder));
			toggleAI.setText("AI");

			Widget toggleSP = new Widget(mBtnNP, new Rect(mBtnBorder, mScreenHeight - (mBtnSize + mBtnBorder), mScreenWidth / 2 - mBtnSep / 2, mScreenHeight - mBtnBorder));
			toggleSP.setText("Puzzle");
			
			Widget lameExcuses = new Widget(mBtnNP, new Rect(mBtnBorder, mBtnBorder + mBtnSize + mBtnSep, mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder - mBtnSep - mBtnSize));
			lameExcuses.setText(context.getString(R.string.mp_beta_warning));

			toggleAI.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					gotoPage(mAIPage);
				}
			});

			toggleSP.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					gotoPage(mPuzzlePage);
				}
			});

			mIPPage.setFontSize(mFontSize);
			mIPPage.addWidget(toggleSP);
			mIPPage.addWidget(toggleAI);
			mIPPage.addWidget(lameExcuses);
		}
		
		LoadLevelList();
		startTransition();

		ChangeLevel();
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

	/**
	 * Loads a list of levels from external storage and assets
	 */
	private void LoadLevelList()
	{
		mProgress.Reload();
	}
	
	private void control_level_transition(int timespan) {
		if(mPendWorld != null) {
			mWorldPendTimer += timespan;
			if(mWorldPendTimer > WORLD_TRANSITION_TIME) mWorldPendTimer = WORLD_TRANSITION_TIME;
			switch(mWorldDirection) {
			case East:
				mWorldOffset.x = -mWorldPendTimer * mScreenWidth / WORLD_TRANSITION_TIME;
				mPendWorldOffset.x = mScreenWidth + mWorldOffset.x;
				mWorldOffset.y = 0;
				mPendWorldOffset.y = 0;
				break;
			case West:
				mWorldOffset.x = mWorldPendTimer * mScreenWidth / WORLD_TRANSITION_TIME;
				mPendWorldOffset.x = mWorldOffset.x - mScreenWidth;
				mWorldOffset.y = 0;
				mPendWorldOffset.y = 0;
				break;
			case North:
				mWorldOffset.x = 0;
				mPendWorldOffset.x = 0;
				mWorldOffset.y = mWorldPendTimer * mScreenHeight / WORLD_TRANSITION_TIME;
				mPendWorldOffset.y = mWorldOffset.y - mScreenHeight;
				break;
			case South:
				mWorldOffset.x = 0;
				mPendWorldOffset.x = 0;
				mWorldOffset.y = -mWorldPendTimer * mScreenHeight / WORLD_TRANSITION_TIME;
				mPendWorldOffset.y = mScreenHeight + mWorldOffset.y;
			default:
				break;
			}
			if(mWorldPendTimer >= WORLD_TRANSITION_TIME) {
				mWorld = mPendWorld;
				ChangeLevel();
				mPendWorld = null;
				mWorldPendTimer = 0;
				mWorldOffset.x = 0;
				mWorldOffset.y = 0;
				mPlayButton.setPosition(mPlayPosition);
			}
			
		}
	}
	
	private void startTransition() {
		if(mPendWorld == null)
		{
			try
			{
				mWorldPendTimer = 0;
				mPendWorld = mProgress.getWorld();
				mPlayButton.setPosition(new Point(0, -1000));
			} catch(Exception ex)
			{
				mPendWorld = null;
			}
		}
	}
	
	/**
	 * Updates the gui with a new level
	 */
	private void ChangeLevel()
	{
		mLevelPackName.setText(mProgress.getLevelPack());
		if(mWorld != null)
		{
			mLevelName.setText(Integer.toString(mProgress.getLevelIndex() + 1) + "/" + Integer.toString(mProgress.getLevelPackSize()) + ": " + mWorld.getLevelName()); 
			mDrawTick = mProgress.IsComplete(mWorld.getIdentifier());
		} else 
		{
			mLevelName.setText(mContext.getString(R.string.error_loading_level));
			mDrawTick = false;
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
		controlWidgetPages(timespan);
		if(mWidgetPage == mPuzzlePage && mPendPage == null)
		{
			control_level_transition(timespan);
			mGameDrawer.Tick(timespan);
		}
		if(mMakeTrainingOffer)
		{
			Handler handler = new Handler(mContext.getMainLooper());
			handler.post(mTrainingOffer);
			mMakeTrainingOffer = false;
		}
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
	public void Redraw(Canvas canvas) {
		if(mWidgetPage == mPuzzlePage)
		{
			if(mWorld != null)
			{
				mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * mGameDrawer.getGridSize() / 2) + mWorldOffset.x, 
										  mScreenHeight / 2 - mWorld.getHeight() * (mGameDrawer.getGridSize() / 2) + mWorldOffset.y);
				mGameDrawer.DrawTilesAndWalls(canvas, mWorld);
				mGameDrawer.DrawSP(canvas, mWorld);
			}
			else
			{
				//TODO draw a SD card symbol
			}
			if(mPendWorld != null)
			{
				mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mPendWorld.getWidth() * mGameDrawer.getGridSize() / 2) + mPendWorldOffset.x, 
										  mScreenHeight / 2 - mPendWorld.getHeight() * (mGameDrawer.getGridSize() / 2) + mPendWorldOffset.y);
				mGameDrawer.DrawTilesAndWalls(canvas, mPendWorld);
				mGameDrawer.DrawSP(canvas, mPendWorld);
			}
			else
			{
				//TODO draw a SD card symbol
			}
		}

		mWidgetPage.Draw(canvas);
		if(mPendPage != null)
		{
			mPendPage.Draw(canvas);
		}
		if(mWidgetPage == mPuzzlePage && mDrawTick)
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
							try
							{
								SPWorld world = mProgress.getWorld();
								mPendMode = new ModeSPGame(world, ModeMenu.this, mProgress, mSkin, mGameDrawerNorm, mGameDrawerRot);
							} catch(Exception ex)
							{
								Log.e("ModeMenu.PlayButton.onClick", "Could not load level");
							}
							
							mSemaphore.release();
						} catch(InterruptedException int_ex)
						{
							Log.e("ModeMenu.mTrainingOffer.negative", "Semaphore interupted");
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
	public void handleGesture(Direction direction) {
		if(mPendWorld == null && mWidgetPage == mPuzzlePage)
		{
			switch(direction) {
			case West:
				mProgress.nextLevel();
				mWorldDirection = Direction.East;
				startTransition();
				SoundManager.PlaySound(mClickSound);
				break;
			case East:
				mProgress.prevLevel();
				mWorldDirection = Direction.West;
				startTransition();
				SoundManager.PlaySound(mClickSound);
				break;
			case North:
				mProgress.nextLevelPack();
				mWorldDirection = Direction.South;
				startTransition();
				SoundManager.PlaySound(mClickSound);
				break;
			case South:
				mProgress.prevLevelPack();
				mWorldDirection = Direction.North;
				startTransition();
				SoundManager.PlaySound(mClickSound);
				break;
			}
		}
	}
	
	@Override
	public boolean handleBack() {
		if(mWidgetPage == mPuzzlePage)
		{
			return false;
		} else
		{
			gotoPage(mPuzzlePage);
			return true;
		}
	}
}
