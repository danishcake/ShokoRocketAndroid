package uk.danishcake.shokorocket.moding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.moding.Mode;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.SquareType;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.simulation.SPWorld;
import uk.danishcake.shokorocket.simulation.SPWorld.WorldState;
import uk.danishcake.shokorocket.sound.SoundManager;

public class ModeEditor extends Mode {
	private enum EditMode
	{
		Walls, Mice, Cats, Arrows, Holes, Rockets
	}
	private EditMode mEditMode = EditMode.Walls;
	private SkinProgress mSkin;
	private Widget mEditModeWidget = null;
	private Widget mTryWidget = null;

	private Dialog mNewLevelDialog = null;
	private Runnable mNewLevelRunnable = new Runnable() {
		public void run() {
			mNewLevelDialog = new Dialog(mContext);
			mNewLevelDialog.setContentView(uk.danishcake.shokorocket.R.layout.editor_new_level);
			mNewLevelDialog.setTitle(R.string.editor_create_a_new_level);
			((Spinner)mNewLevelDialog.findViewById(R.id.LevelWidth)).setSelection(6);
			((Spinner)mNewLevelDialog.findViewById(R.id.LevelHeight)).setSelection(9);
			
			//Set the default author
			TextView level_author = (TextView) mNewLevelDialog.findViewById(R.id.LevelAuthor);
			SharedPreferences prefs =  mContext.getSharedPreferences("ShokoRocketPreferences", Context.MODE_PRIVATE);
			String default_author = prefs.getString("DefaultAuthor", mContext.getResources().getString(R.string.level_default_author));
			level_author.setText(default_author);
			
			Button createLevel = (Button) mNewLevelDialog.findViewById(R.id.CreateLevel);
			createLevel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try
					{
					if(mNewLevelDialog == null)
						return; //Have seen this crash due to null here - perhaps two clicks are called sometimes?
					mSemaphore.acquire();
					Spinner width_spinner = (Spinner) mNewLevelDialog.findViewById(R.id.LevelWidth);
					Spinner height_spinner = (Spinner) mNewLevelDialog.findViewById(R.id.LevelHeight);
					TextView level_author = (TextView) mNewLevelDialog.findViewById(R.id.LevelAuthor);
					TextView level_name = (TextView) mNewLevelDialog.findViewById(R.id.LevelName);
					
					mWorld = new SPWorld(Integer.parseInt((String)width_spinner.getSelectedItem()),
									   Integer.parseInt((String)height_spinner.getSelectedItem()));
					mWorld.setAuthor(level_author.getText().toString());
					mWorld.setLevelName(level_name.getText().toString());
					
					InitialiseDrawing();
					InitialiseWidgets();
					
					//Save the author name so can set default next time
					SharedPreferences prefs =  mContext.getSharedPreferences("ShokoRocketPreferences", Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("DefaultAuthor", level_author.getText().toString());
					editor.commit();
					
					mNewLevelDialog.dismiss();
					mNewLevelDialog = null;
					
					mSemaphore.release();
					} catch (InterruptedException int_ex)
					{
						Log.e("ModeEditor.newLevelRunnable.run", "Semaphore interupted");
					}
				}
			});
			mNewLevelDialog.show();
		}
	};
	
	private Dialog mPickFilenameDialog = null;
	private Dialog mPickPropertiesDialog = null;
	private Dialog mShareDialog = null;	
	private SPWorld mWorld = null;
	private GameDrawer mGameDrawer = null;
	private ModeSPMenu mModeMenu;
	private RunningMode mRunningMode = RunningMode.Stopped;
	
	private int mResetTimer = 0;
	private static final int ResetTime = 1500;
	
	boolean mSaveNeeded = false;
	boolean mValidated = false;
	
	private Vector2i mCursorPosition = new Vector2i(-1, -1);
	private Vector2i mGestureStart = new Vector2i(0, 0);
	private Vector2i mGestureEnd = new Vector2i(0, 0);
	private Direction mGestureDirection = Direction.Invalid;
	private boolean mGestureInProgress = false;
	
	private final int E_MENU_NEW = 1;
	private final int E_MENU_SAVE = 2;
	private final int E_MENU_SHARE = 3;
	private final int E_MENU_PROPERTIES = 4;
	private final int E_MENU_VERIFY = 5;
	
	private final int E_MENU_WALLMODE = 6;
	private final int E_MENU_MOUSEMODE = 7;
	private final int E_MENU_CATMODE = 8;
	private final int E_MENU_ROCKETMODE = 9;
	private final int E_MENU_HOLEMODE = 10;
	private final int E_MENU_ARROWMODE = 11;
	
	
	public ModeEditor(ModeSPMenu menu, SPWorld world, SkinProgress skin)
	{
		mWorld = world;
		if(world != null)
			world.setArrowStockUnlimited(true);
		mModeMenu = menu;
		mSkin = skin;
	}
	
	/**
	 * Sets up drawing after a new level is set
	 */
	private void InitialiseDrawing() {
		mGameDrawer = new GameDrawer();

		int required_width = mWorld.getWidth() * mGridSize ;
		int required_height = mWorld.getHeight() * mGridSize ;
		float scaleX = ((float)mScreenWidth - mLevelBorder  * 2) / (float)required_width;
		float scaleY = ((float)(mScreenHeight - mBtnSize - mBtnBorder - mLevelBorder * 2)) / (float)required_height;

		float smaller = scaleX < scaleY ? scaleX : scaleY;
		
		if(smaller < 1)
			mGameDrawer.Setup(mContext, (int)(((float)mGridSize) * smaller), mSkin);
		else
			mGameDrawer.Setup(mContext, mGridSize, mSkin);
		mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * mGameDrawer.getGridSize() / 2), mLevelBorder);
		
	}
	
	/**
	 * Sets up the widgets after a new level is set
	 */
	private void InitialiseWidgets() {
		mWidgetPage = new WidgetPage();
		mWidgetPage.setFontSize(mFontSize);
	
		mEditModeWidget = new Widget(mBtnNP, new Rect(mScreenWidth / 2  + mBtnBorder, 
												  mScreenHeight     - mBtnSize - mBtnBorder, 
												  mScreenWidth      - mBtnBorder, 
												  mScreenHeight     - mBtnBorder));
		mEditModeWidget.setText(mContext.getString(R.string.editor_mode_walls));
		mEditMode = EditMode.Walls;
		
		mTryWidget = new Widget(mBtnNP, new Rect(mBtnBorder, 
											 mScreenHeight     - mBtnSize - mBtnBorder, 
											 mScreenWidth / 2  - mBtnSep, 
											 mScreenHeight     - mBtnBorder));
		mTryWidget.setText(mContext.getString(R.string.editor_run_speed_try));
		
		
		mEditModeWidget.setOnClickListener(new uk.danishcake.shokorocket.gui.OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				switch(mEditMode)
				{
				case Walls:
					mEditMode = EditMode.Arrows;
					mEditModeWidget.setText(mContext.getString(R.string.editor_mode_arrows));
					break;
				case Arrows:
					mEditMode = EditMode.Mice;
					mEditModeWidget.setText(mContext.getString(R.string.editor_mode_mice));
					break;
				case Mice:
					mEditMode = EditMode.Cats;
					mEditModeWidget.setText(mContext.getString(R.string.editor_mode_cats));
					break;
				case Cats:
					mEditMode = EditMode.Holes;
					mEditModeWidget.setText(mContext.getString(R.string.editor_mode_holes));
					break;
				case Holes:
					mEditMode = EditMode.Rockets;
					mEditModeWidget.setText(mContext.getString(R.string.editor_mode_rockets));
					break;
				case Rockets:
					mEditMode = EditMode.Walls;
					mEditModeWidget.setText(mContext.getString(R.string.editor_mode_walls));
					break;
				}
			}
		});
		
		mTryWidget.setOnClickListener(new uk.danishcake.shokorocket.gui.OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				switch(mRunningMode)
				{
				case Stopped:
					mRunningMode = RunningMode.Running;
					mTryWidget.setText(mContext.getString(R.string.editor_run_speed_faster));
					break;
				case Running:
					mRunningMode = RunningMode.RunningFast;
					mTryWidget.setText(mContext.getString(R.string.editor_run_speed_reset));
					break;
				case RunningFast:
					mRunningMode = RunningMode.Stopped;
					mWorld.Reset();
					mTryWidget.setText(mContext.getString(R.string.editor_run_speed_try));
					break;
				}
			}
		});
		
		mWidgetPage.addWidget(mEditModeWidget);
		mWidgetPage.addWidget(mTryWidget);
	}

	@Override
	public void Setup(final Context context) {
		super.Setup(context);
		
		mBtnSize = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_size);
		mBtnSep = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_sep);
		mBtnBorder = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_border);
		mFontSize = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_font_size);
		
		if(mWorld == null)
		{
			Handler handler = new Handler(context.getMainLooper());
			handler.post(mNewLevelRunnable);
		} else
		{
			InitialiseDrawing();
			InitialiseWidgets();
			mWorld.LoadSolution();
		}
	}
	
	@Override
	public Mode Teardown() {
		return super.Teardown();
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		if(mWorld != null)
		{
			final int TimeRate = 5;
			int rate = 1;
			switch(mRunningMode)
			{
			case Stopped:
				break;
			case RunningFast:
				rate = 3; //Fall through
			case Running:
				mWorld.Tick(timespan * rate * TimeRate);
				if(mWorld.getMouseRescued())
					SoundManager.PlaySound(mMouseSound);
				WorldState state = mWorld.getWorldState();
				if(state == WorldState.Failed)
				{
					if(mResetTimer == 0)
					{
						SoundManager.PlaySound(mCatSound);
					}
				}
				if(state == WorldState.Success)
				{
					mValidated = true;
				}
				if(state == WorldState.Failed || state == WorldState.Success)
				{
					mResetTimer += timespan;
					if(mResetTimer > ResetTime)
					{
						mWorld.Reset();
						mRunningMode = RunningMode.Stopped;
						mTryWidget.setText(mContext.getString(R.string.editor_run_speed_try));
					}
				} else
					mResetTimer = 0;

				break;
			}
			mGameDrawer.Tick(timespan);
		}
		
		return super.Tick(timespan);
	}
	
	@Override
	public boolean handleBack() {
		if(mPendMode == null)
			mPendMode = mModeMenu;
		return true;
	}
	
	@Override
	public void handleTap(int x, int y) {
		if(mWorld == null || mGameDrawer == null || mWidgetPage == null)
			return;
		mWidgetPage.handleTap(x, y);
		Vector2i offset = mGameDrawer.getDrawOffset();
		int grid_x = (x - offset.x) / mGameDrawer.getGridSize();
		int grid_y = (y - offset.y) / mGameDrawer.getGridSize();
		if(grid_x >= 0 && grid_y >= 0 && grid_x < mWorld.getWidth() && grid_y < mWorld.getHeight())
		{
			mCursorPosition.x = grid_x;
			mCursorPosition.y = grid_y;
			SoundManager.PlaySound(mClickSound);
		}
	}
	
	@Override
	public void handleDPad(Direction direction) {
		if(mWorld == null || mGameDrawer == null || mWidgetPage == null)
			return;
		if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
		{
			switch(direction)
			{
			case North:
				mCursorPosition.y--;
				if (mCursorPosition.y < 0)
					mCursorPosition.y = 0;
				break;
			case South:
				mCursorPosition.y++;
				if (mCursorPosition.y >= mWorld.getHeight())
					mCursorPosition.y = mWorld.getHeight() - 1;
				break;
			case West:
				mCursorPosition.x--;
				if (mCursorPosition.x < 0)
					mCursorPosition.x = 0;
				break;
			case East:
				mCursorPosition.x++;
				if (mCursorPosition.x >= mWorld.getWidth())
					mCursorPosition.x = mWorld.getWidth() - 1;
				break;
			}
		}
	}
	
	@Override
	public void previewGesture(int x1, int y1, int x2, int y2,
			Direction direction) {
		mGestureInProgress = true;
		mGestureStart.x = x1;
		mGestureStart.y = y1;
		
		mGestureEnd.x = x2;
		mGestureEnd.y = y2;
		mGestureDirection = direction;
	}
	
	@Override
	public void clearPreviewGesture() {
		mGestureInProgress = false;
	}
	
	@Override
	public void handleGesture(Direction direction) {
		if(mWorld == null || mGameDrawer == null || mWidgetPage == null)
			return;
		if(mCursorPosition.x != -1 && mCursorPosition.y != -1 && mRunningMode == RunningMode.Stopped)
		{
			switch(mEditMode)
			{
			case Walls:
				mWorld.toggleDirection(mCursorPosition.x, mCursorPosition.y, direction);
				break;
			case Arrows:
				if(mWorld.getSpecialSquare(mCursorPosition.x, mCursorPosition.y).getArrowDirectionality() == Direction.Invalid)
					mWorld.setSpecialSquare(mCursorPosition.x, mCursorPosition.y, SquareType.Empty);
				mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, direction);
				break;
			case Mice:
				mWorld.toggleMouse(mCursorPosition.x, mCursorPosition.y, direction);
				break;
			case Cats:
				mWorld.toggleCat(mCursorPosition.x, mCursorPosition.y, direction);
				break;
			case Rockets:
				mWorld.toggleRocket(mCursorPosition.x, mCursorPosition.y);
				break;
			case Holes:
				mWorld.toggleHole(mCursorPosition.x, mCursorPosition.y);
				break;	
			}
		}
	}
	
	@Override
	public boolean getMenu(Menu menu, boolean clear) {
		menu.clear();

		menu.add(0, E_MENU_NEW, 0, R.string.editor_menu_new);
		menu.add(0, E_MENU_SAVE, 0, R.string.editor_menu_save);
		menu.add(0, E_MENU_SHARE, 0, R.string.editor_menu_share);
		
		menu.add(1, E_MENU_PROPERTIES, 0, R.string.editor_menu_properties);
		menu.add(1, E_MENU_VERIFY, 0, R.string.editor_menu_verify);
		
		SubMenu sm_edit = menu.addSubMenu(R.string.editor_menu_edit_mode);
		MenuItem mi_walls = sm_edit.add(2, E_MENU_WALLMODE, 0, R.string.editor_mode_walls);
		MenuItem mi_mice = sm_edit.add(2, E_MENU_MOUSEMODE, 0, R.string.editor_mode_mice);
		MenuItem mi_cats = sm_edit.add(2, E_MENU_CATMODE, 0, R.string.editor_mode_cats);
		MenuItem mi_arrows = sm_edit.add(2, E_MENU_ARROWMODE, 0, R.string.editor_mode_arrows);
		MenuItem mi_rocket = sm_edit.add(2, E_MENU_ROCKETMODE, 0, R.string.editor_mode_rockets);
		MenuItem mi_holes = sm_edit.add(2, E_MENU_HOLEMODE, 0, R.string.editor_mode_holes);
		sm_edit.setGroupCheckable(2, true, true);
		
		switch(mEditMode)
		{
		case Arrows:
			mi_arrows.setChecked(true);
			break;
		case Cats:
			mi_cats.setChecked(true);
			break;
		case Holes:
			mi_holes.setChecked(true);
			break;
		case Mice:
			mi_mice.setChecked(true);
			break;
		case Rockets:
			mi_rocket.setChecked(true);
			break;
		case Walls:
			mi_walls.setChecked(true);
			break;
		}

		return true;
	}
	
	private void SaveLevel() 
	{
		if(mWorld == null)
			return;
		try {
			File root = new File(new File(Environment.getExternalStorageDirectory(), "ShokoRocket"), "My Levels");
			File file = new File(root, mWorld.getFilename());
			
			root.mkdirs();
			
			FileOutputStream fos = new FileOutputStream(file);
			
			mWorld.Save(fos);
			Toast.makeText(mContext, mContext.getString(R.string.editor_saved_as) + file.getPath(), Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
			Log.e("ModeEditor.handleMenuSelection", e.getMessage());
			Toast.makeText(mContext, mContext.getString(R.string.editor_unable_to_save) + e.getMessage(), Toast.LENGTH_SHORT).show();
			mWorld.setLevelName("");
		}		
	}
	
	@Override
	public boolean handleMenuSelection(MenuItem item) {
		switch(item.getItemId())
		{
		case E_MENU_NEW:
			AlertDialog.Builder builder = new Builder(mContext);
			builder.setMessage(R.string.editor_new_level_confirmation);
			builder.setPositiveButton(R.string.editor_yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					try
					{
						mSemaphore.acquire();
						mWorld = null;
						Handler handler = new Handler(mContext.getMainLooper());
						handler.post(mNewLevelRunnable);
						mSemaphore.release();
					} catch(InterruptedException int_ex)
					{
						Log.e("ModeEditor.handleMenuSelection, E_MENU_NEW", "Semaphore interupted");
					}
				}
			});
			builder.setNegativeButton(R.string.editor_no, null);
			AlertDialog ad = builder.create();
			ad.show();
			break;
		case E_MENU_SAVE:
			if(mWorld == null)
			{
				Toast.makeText(mContext, R.string.editor_create_a_level_first, Toast.LENGTH_SHORT).show();
				return true;
			}
			if(Verify())
			{
				if(mWorld.getFilename().length() == 0)
				{
					mPickFilenameDialog = new Dialog(mContext);
					mPickFilenameDialog.setContentView(uk.danishcake.shokorocket.R.layout.editor_change_filename);
					mPickFilenameDialog.setTitle(R.string.editor_enter_filename);
					
					Button setFilename = (Button) mPickFilenameDialog.findViewById(R.id.SetFilename);
					setFilename.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							try
							{
								mSemaphore.acquire();
								TextView filename_widget = (TextView) mPickFilenameDialog.findViewById(R.id.LevelFilename);
								String filename = filename_widget.getText().toString();
								if(!filename.endsWith(".Level"))
								{
									if(filename.endsWith("."))
										filename = filename + "Level";
									else
										filename = filename + ".Level";
								}
								
								mWorld.setFilename(filename);
								mPickFilenameDialog.dismiss();
								
								File root = new File(new File(Environment.getExternalStorageDirectory(), "ShokoRocket"), "My Levels");
								File file = new File(root, mWorld.getFilename());
								
								if(file.exists())
								{
									AlertDialog.Builder builder = new Builder(mContext);
									builder.setMessage(R.string.editor_overwrite_confirmation);
									builder.setPositiveButton(R.string.editor_yes, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											try
											{
												mSemaphore.acquire();
												SaveLevel();
												mSemaphore.release();
											} catch(InterruptedException int_ex)
											{
												Log.e("ModeEditor.handleMenuSelection, E_MENU_SAVE", "Semaphore interupted");
											}
										}
									});
									
									builder.setNegativeButton(R.string.editor_no, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											try
											{
												mSemaphore.acquire();
												mWorld.setFilename("");
												mSemaphore.release();
											} catch(InterruptedException int_ex)
											{
												Log.e("ModeEditor.handleMenuSelection, E_MENU_SAVE", "Semaphore interupted");
											}
										}
									});

									builder.setOnCancelListener(new OnCancelListener() {
										public void onCancel(DialogInterface dialog) {
											try
											{
												mSemaphore.acquire();
												mWorld.setFilename("");
												mSemaphore.release();
											} catch(InterruptedException int_ex)
											{
												Log.e("ModeEditor.handleMenuSelection, E_MENU_SAVE", "Semaphore interupted");
											}	
										}
									});
									AlertDialog ad = builder.create();
									ad.show();
								} else
								{
									SaveLevel();
								}

								mSemaphore.release();
							} catch(InterruptedException int_ex)
							{
								//Todo log
							}
						}
						});
					mPickFilenameDialog.show();
					
				} else
					SaveLevel();
			}
			else
				Toast.makeText(mContext, R.string.editor_solution_invalid, Toast.LENGTH_SHORT).show();
			break;
		case E_MENU_SHARE:
			if(mWorld == null)
			{
				Toast.makeText(mContext, R.string.editor_create_a_level_first, Toast.LENGTH_SHORT).show();
				return true;
			}
			if(Verify())	
			{
				mShareDialog = new Dialog(mContext);
				mShareDialog.setTitle("Share your work");
				mShareDialog.setContentView(R.layout.editor_share_level);
				
				Button submit = (Button) mShareDialog.findViewById(R.id.LevelSubmit);
				submit.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						try
						{
							mSemaphore.acquire();
							FileOutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Share.Level");
							
							mWorld.Save(output);
							
							Intent intent = new Intent(Intent.ACTION_SEND);
							intent.putExtra(Intent.EXTRA_SUBJECT, "ShokoRocket level");
							intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/Share.Level"));
							intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mContext.getString(R.string.editor_submission_address)});
							intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.editor_share_message));
							intent.setType("text/csv");
							
							mContext.startActivity(intent);
							
							mShareDialog.dismiss();
							mSemaphore.release();
							mSkin.unlockSkin("Animations/Contributor.xml");
						} catch(InterruptedException int_ex)
						{
							Log.e("ModeEditor.HandleMenuSelection", "Semphore interupted");
						} catch(IOException io_ex)
						{
							Toast.makeText(mContext, R.string.editor_temp_fail, Toast.LENGTH_SHORT).show();
						}						
					}
				});
				
				mShareDialog.show();
			}
			else
				Toast.makeText(mContext, R.string.editor_solution_invalid, Toast.LENGTH_SHORT).show();
			break;
		case E_MENU_VERIFY:
			if(mWorld == null)
			{
				Toast.makeText(mContext, R.string.editor_create_a_level_first, Toast.LENGTH_SHORT).show();
				return true;
			}
			if(Verify())
				Toast.makeText(mContext, R.string.editor_solution_valid, Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(mContext, R.string.editor_solution_invalid, Toast.LENGTH_SHORT).show();
			break;
		case E_MENU_PROPERTIES:
			if(mWorld == null)
			{
				Toast.makeText(mContext, R.string.editor_create_a_level_first, Toast.LENGTH_SHORT).show();
				return true;
			}
			mPickPropertiesDialog = new Dialog(mContext);
			mPickPropertiesDialog.setContentView(uk.danishcake.shokorocket.R.layout.editor_properties);
			mPickPropertiesDialog.setTitle(R.string.editor_menu_properties);
			
			EditText level_author = (EditText) mPickPropertiesDialog.findViewById(R.id.LevelAuthorProperties);
			EditText level_name = (EditText) mPickPropertiesDialog.findViewById(R.id.LevelNameProperties);
			
			level_author.setText(mWorld.getAuthor());
			level_name.setText(mWorld.getLevelName());
			
			Button accept = (Button) mPickPropertiesDialog.findViewById(R.id.AcceptChanges);
			accept.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try
					{
					mSemaphore.acquire();
					EditText level_author = (EditText) mPickPropertiesDialog.findViewById(R.id.LevelAuthorProperties);
					EditText level_name = (EditText) mPickPropertiesDialog.findViewById(R.id.LevelNameProperties);
					
					mWorld.setAuthor(level_author.getText().toString());
					mWorld.setLevelName(level_name.getText().toString());
					mPickPropertiesDialog.dismiss();
					mSemaphore.release();
					} catch(InterruptedException int_ex)
					{
						Log.e("ModeEditor.handleMenuSelection", "Semaphore interupted");
					}
				}
			});
			
			mPickPropertiesDialog.show();
			break;
		case E_MENU_WALLMODE:
			mEditMode = EditMode.Walls;
			mEditModeWidget.setText(mContext.getString(R.string.editor_mode_walls));
			break;
		case E_MENU_MOUSEMODE:
			mEditMode = EditMode.Mice;
			mEditModeWidget.setText(mContext.getString(R.string.editor_mode_mice));
			break;
		case E_MENU_CATMODE:
			mEditMode = EditMode.Cats;
			mEditModeWidget.setText(mContext.getString(R.string.editor_mode_cats));
			break;
		case E_MENU_ARROWMODE:
			mEditMode = EditMode.Arrows;
			mEditModeWidget.setText(mContext.getString(R.string.editor_mode_arrows));
			break;
		case E_MENU_HOLEMODE:
			mEditMode = EditMode.Holes;
			mEditModeWidget.setText(mContext.getString(R.string.editor_mode_holes));
			break;
		case E_MENU_ROCKETMODE:
			mEditMode = EditMode.Rockets;
			mEditModeWidget.setText(mContext.getString(R.string.editor_mode_rockets));
			break;
		default:
			return false;
		}
		return true;
	}
	
	/**
	 * Checks that the level can be completed within 240s with the current arrows
	 * @return
	 */
	private boolean Verify()
	{
		mRunningMode = RunningMode.Stopped;
		mTryWidget.setText(mContext.getString(R.string.editor_run_speed_try));
		mWorld.Reset();
		for(int ms = 0; ms < 1000 * 240 && mWorld.getWorldState() == WorldState.OK; ms += 50) //Maximum 240s runtime
		{
			mWorld.Tick(50);
		}
		boolean success = mWorld.getWorldState() == WorldState.Success;
		mWorld.Reset();
		return success;
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		if(mNewLevelDialog != null) {
			
		} else if(mWorld != null){
			//SPWorld has been created, so at next stage
			mGameDrawer.DrawTilesAndWalls(canvas, mWorld);
			mGameDrawer.Draw(canvas, mWorld);
			if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
			{
				mGameDrawer.DrawCursor(canvas, mCursorPosition.x, mCursorPosition.y, mRunningMode != RunningMode.Stopped);
			}
			if(mGestureInProgress)
			{
				Paint linePaint = new Paint();
				linePaint.setARGB(255, 255, 0, 0);
				linePaint.setStrokeWidth(6);

				canvas.drawLine(mGestureStart.x, mGestureStart.y, mGestureEnd.x, mGestureEnd.y, linePaint);
				Bitmap frame = mGameDrawer.GetArrow(mGestureDirection).getCurrentFrame();
				if(mGestureDirection != Direction.Invalid)
					canvas.drawBitmap(frame, (mGestureStart.x + mGestureEnd.x) / 2 - frame.getWidth() / 2, (mGestureStart.y + mGestureEnd.y) / 2 - frame.getHeight() / 2, null);
			}
			mWidgetPage.Draw(canvas);
		}

		super.Redraw(canvas);
	}
}
