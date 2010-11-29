package uk.danishcake.shokorocket.moding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;

import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.ToastRunnable;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.simulation.SPWorld;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.SPWorld.WorldState;
import uk.danishcake.shokorocket.sound.SoundManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ModeGame extends Mode {
	private WidgetPage mWidgetPage = new WidgetPage();
	private Widget mLeftArrowCount, mRightArrowCount, mUpArrowCount, mDownArrowCount;
	private GameDrawer mGameDrawer;
	private GameDrawer mGameDrawerRot = new GameDrawer();
	private GameDrawer mGameDrawerNorm = new GameDrawer();
	private SPWorld mWorld;
	private ModeMenu mModeMenu;
	private int mResetTimer = 0;
	private static final int ResetTime = 1500;
	private Vector2i mCursorPosition = new Vector2i(-1, -1);
	boolean mCompleted = false;
	boolean mCompleteDialogShown = false;
	int mCompleteAge = 0;
	private Progress mProgress;
	private SkinProgress mSkin;	
	
	private RunningMode mRunningMode = RunningMode.Stopped;
	private int mRotateTimer = 0;
	private final static int RotateTime = 400;
	private boolean mRotatePerformed = false;
	private float mRotateAngle = 0.0f;
	private float mRotateScale = 1.0f;
	
	private Vector2i mGestureStart = new Vector2i(0, 0);
	private Vector2i mGestureEnd = new Vector2i(0, 0);
	private Direction mGestureDirection = Direction.Invalid;
	private boolean mGestureInProgress = false;
	
	private Dialog mCompleteDialog = null;
	private Dialog mSplashDialog = null;
	
	private final int E_MENU_ROTATE = 1;
	private final int E_MENU_BACK = 2;
	
	public ModeGame(SPWorld world, ModeMenu menu, Progress progress, SkinProgress skin)
	{
		mWorld = world;
		mModeMenu = menu;
		mProgress = progress;
		mSkin = skin;
	}
	
	@Override
	public void Setup(Context context) {
		super.Setup(context);
		
		//Setup autoscaling twice, for portrait and landscape orientations
		int required_width = mWorld.getWidth() * mGridSize;
		int required_height = mWorld.getHeight() * mGridSize;
		float scaleX = ((float)mScreenWidth - mLevelBorder * 2) / (float)required_width;
		float scaleY = ((float)(mScreenHeight - mBtnSize - mBtnBorder - mLevelBorder * 2)) / (float)required_height;
		float scaleX_rot = ((float)mScreenWidth - mLevelBorder * 2) / (float)required_height;
		float scaleY_rot = ((float)(mScreenHeight - mBtnSize - mBtnBorder - mLevelBorder * 2)) / (float)required_width;
		float smaller = scaleX < scaleY ? scaleX : scaleY;
		float smaller_rot = scaleX_rot < scaleY_rot ? scaleX_rot : scaleY_rot;
		
		

		mGameDrawer = mGameDrawerNorm;
		if(smaller < 1)
			mGameDrawer.Setup(mContext, (int)(((float)mGridSize) * smaller), mSkin);
		else
			mGameDrawer.Setup(mContext, mGridSize, mSkin);
		if(smaller_rot < 1)
			mGameDrawerRot.Setup(mContext, (int)(((float)mGridSize) * smaller_rot), mSkin);
		else
			mGameDrawerRot.Setup(mContext, mGridSize, mSkin);

		mGameDrawer.CreateBackground(mWorld);
		mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * mGameDrawer.getGridSize() / 2), mLevelBorder);

		mWorld.RotateRight();
		mGameDrawerRot.CreateBackground(mWorld);
		mGameDrawerRot.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * mGameDrawerRot.getGridSize() / 2), mLevelBorder);
		mWorld.RotateLeft();
		
		SharedPreferences sp = mContext.getSharedPreferences("Settings", Context.MODE_PRIVATE);
		boolean auto_rotate = sp.getBoolean("auto_rotate", true);
		if(smaller_rot > smaller && auto_rotate)
		{
			mRunningMode = RunningMode.AutoRotating;
			mRotateTimer = 0;
			mGameDrawer.CreateCacheBitmap(mWorld);
		}
		
		Handler handler = new Handler(mContext.getMainLooper());
		if(mWorld.getSplashMessage() == null)
		{
			handler.post(new ToastRunnable(mContext, mWorld.getLevelName() + "\n\nBy " + mWorld.getAuthor(), Toast.LENGTH_SHORT));
		}
		else
			handler.post(mShowSplashRunnable);
		
		int np_border = context.getResources().getInteger(R.integer.np_border);
		NinePatchData btn_np = new NinePatchData(BitmapFactory.decodeStream(context.getResources().openRawResource(R.raw.blank_button)), np_border, np_border, np_border, np_border);			
		
		Widget reset = new Widget(btn_np, new Rect(mScreenWidth - (mBtnSize2 + mBtnBorder), mScreenHeight - mBtnSize - mBtnBorder, mScreenWidth - mBtnBorder, mScreenHeight - mBtnBorder));
		reset.setText(context.getString(R.string.game_reset));
		reset.setOnClickListener(new OnClickListener() {				
			@Override
			public void OnClick(Widget widget) {
				SoundManager.PlaySound(mClickSound);
				if(mRunningMode == RunningMode.Stopped)
				{
					mWorld.Reset();
					mWorld.ClearArrows();
					updateArrowStock();
				}
				else if(mRunningMode == RunningMode.Running || mRunningMode == RunningMode.RunningFast)
				{
					mWorld.Reset();
					mRunningMode = RunningMode.Stopped;
				}
			}
		});
		
		Widget go = new Widget(btn_np, new Rect(mScreenWidth - mBtnSize2 - mBtnSize2 - 4 - mBtnBorder, mScreenHeight - mBtnSize - mBtnBorder, mScreenWidth - mBtnSize2 - 4 - mBtnBorder, mScreenHeight - mBtnBorder));
		go.setText(context.getString(R.string.game_go));
		go.setOnClickListener(new OnClickListener() {				
			@Override
			public void OnClick(Widget widget) {
				SoundManager.PlaySound(mClickSound);
				if(mRunningMode == RunningMode.Stopped)
					mRunningMode = RunningMode.Running;
				else if(mRunningMode == RunningMode.Running)
					mRunningMode = RunningMode.RunningFast;
			}
		});
		
		//Load arrow buttons
		
		NinePatchData np_left_arrows = new NinePatchData(BitmapFactory.decodeStream(mContext.getResources().openRawResource(R.raw.arrow_button_left)),
														 mContext.getResources().getInteger(R.integer.arrow_btn_left),
														 mContext.getResources().getInteger(R.integer.arrow_btn_right),
														 mContext.getResources().getInteger(R.integer.arrow_btn_top),
														 mContext.getResources().getInteger(R.integer.arrow_btn_bottom));
		NinePatchData np_right_arrows = new NinePatchData(BitmapFactory.decodeStream(mContext.getResources().openRawResource(R.raw.arrow_button_right)),
												 		  mContext.getResources().getInteger(R.integer.arrow_btn_left),
														  mContext.getResources().getInteger(R.integer.arrow_btn_right),
														  mContext.getResources().getInteger(R.integer.arrow_btn_top),
														  mContext.getResources().getInteger(R.integer.arrow_btn_bottom));
		NinePatchData np_top_arrows = new NinePatchData(BitmapFactory.decodeStream(mContext.getResources().openRawResource(R.raw.arrow_button_up)),
														mContext.getResources().getInteger(R.integer.arrow_btn_left),
														mContext.getResources().getInteger(R.integer.arrow_btn_right),
														mContext.getResources().getInteger(R.integer.arrow_btn_top),
														mContext.getResources().getInteger(R.integer.arrow_btn_bottom));
		NinePatchData np_bottom_arrows = new NinePatchData(BitmapFactory.decodeStream(mContext.getResources().openRawResource(R.raw.arrow_button_down)),
													   	   mContext.getResources().getInteger(R.integer.arrow_btn_left),
														   mContext.getResources().getInteger(R.integer.arrow_btn_right),
														   mContext.getResources().getInteger(R.integer.arrow_btn_top),
														   mContext.getResources().getInteger(R.integer.arrow_btn_bottom));
		
		int arrow_btn_height = np_left_arrows.bottomBorder + np_left_arrows.topBorder;
		int arrow_btn_width  = np_left_arrows.leftBorder + np_left_arrows.rightBorder;
		mLeftArrowCount = new Widget(np_left_arrows,   new Rect(mBtnBorder + 0 * (mBtnSep + arrow_btn_width), 
																mScreenHeight - arrow_btn_height, 
																mBtnBorder + 0 * (mBtnSep + arrow_btn_width) + arrow_btn_width, 
																mScreenHeight));
		mRightArrowCount = new Widget(np_right_arrows, new Rect(mBtnBorder + 1 * (mBtnSep + arrow_btn_width), 
																mScreenHeight - arrow_btn_height, 
																mBtnBorder + 1 * (mBtnSep + arrow_btn_width) + arrow_btn_width, 
																mScreenHeight));
		mUpArrowCount = new Widget(np_top_arrows, 	   new Rect(mBtnBorder + 2 * (mBtnSep + arrow_btn_width), 
																mScreenHeight - arrow_btn_height, 
																mBtnBorder + 2 * (mBtnSep + arrow_btn_width) + arrow_btn_width, 
																mScreenHeight));
		mDownArrowCount = new Widget(np_bottom_arrows, new Rect(mBtnBorder + 3 * (mBtnSep + arrow_btn_width), 
																mScreenHeight - arrow_btn_height, 
																mBtnBorder + 3 * (mBtnSep + arrow_btn_width) + arrow_btn_width, 
																mScreenHeight));
		
		reset.setFontSize(mFontSize);
		go.setFontSize(mFontSize);
		mLeftArrowCount.setFontSize(mFontSize);
		mRightArrowCount.setFontSize(mFontSize);
		mUpArrowCount.setFontSize(mFontSize);
		mDownArrowCount.setFontSize(mFontSize);
		
		mLeftArrowCount.setText("0");
		mRightArrowCount.setText("0");
		mUpArrowCount.setText("0");
		mDownArrowCount.setText("0");
					
		mWidgetPage.addWidget(reset);
		mWidgetPage.addWidget(go);
		mWidgetPage.addWidget(mLeftArrowCount);
		mWidgetPage.addWidget(mRightArrowCount);
		mWidgetPage.addWidget(mUpArrowCount);
		mWidgetPage.addWidget(mDownArrowCount);
		
		updateArrowStock();
	}
	
	@Override
	public Mode Teardown() {
		mWorld.Reset();
		mRunningMode = RunningMode.Stopped;
		mWorld.ClearArrows();
		return super.Teardown();
	}
	
	@Override
	public ModeAction Tick(int timespan) {
		final int TimeRate = 5;
		int rate = 1;
		
		switch(mRunningMode)
		{
		case AutoRotating:
			mRotateTimer += timespan;
			if(mRotateTimer > 250)
			{
				mWorld.Reset();
				mRotateTimer = 0;
				mRunningMode = RunningMode.RotatingCW;
			}
			break;
		case RotatingCW:
			mRotateTimer += timespan;
			if(mRotateTimer <= RotateTime / 2)
			{
				mRotateAngle = 90.0f * ((float)mRotateTimer) / ((float)RotateTime);
				mRotateScale = 1.0f - 2.0f * ((float)mRotateTimer) / ((float)RotateTime);
			}
			if(mRotateTimer > RotateTime / 2)
			{
				if(!mRotatePerformed)
				{
					changeRotation();
					mRotatePerformed = true;
				}
				mRotateAngle = -90.0f + 90.0f * ((float)mRotateTimer) / ((float)RotateTime);
				mRotateScale = -1.0f + 2.0f * ((float)mRotateTimer) / ((float)RotateTime);
			}
			if(mRotateTimer > RotateTime)
			{
				mRunningMode = RunningMode.Stopped;
				mRotateTimer = 0;
				mRotatePerformed = false;
			}
			break;
		case RotatingCCW:
			mRotateTimer += timespan;
			if(mRotateTimer <= RotateTime / 2)
			{
				mRotateAngle = -90.0f * ((float)mRotateTimer) / ((float)RotateTime);
				mRotateScale = 1.0f - 2.0f * ((float)mRotateTimer) / ((float)RotateTime);
			}
			if(mRotateTimer > RotateTime / 2)
			{
				if(!mRotatePerformed)
				{
					changeRotation();
					mRotatePerformed = true;
				}
				mRotateAngle = 90.0f - 90.0f * ((float)mRotateTimer) / ((float)RotateTime);
				mRotateScale = -1.0f + 2.0f * ((float)mRotateTimer) / ((float)RotateTime);
			}
			if(mRotateTimer > RotateTime)
			{
				mRunningMode = RunningMode.Stopped;
				mRotateTimer = 0;
				mRotatePerformed = false;
			}
			break;
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
				mResetTimer += timespan;
				if(mResetTimer > ResetTime)
				{
					mWorld.Reset();
					mRunningMode = RunningMode.Stopped;
				}
			} else
				mResetTimer = 0;
			if(state == WorldState.Success)
			{
				if(!mCompleted)
				{
					mCompleted = true;
					mProgress.MarkComplete(mWorld.getIdentifier());
					mCompleteAge = mAge;
				}
				if(mAge > mCompleteAge + 750 && !mCompleteDialogShown)
				{
					Handler handler = new Handler(mContext.getMainLooper());
					handler.post(mLevelCompleteRunnable);
					mCompleteDialogShown = true;
				}
			}
			break;
		}
		
		mWidgetPage.Tick(timespan);
		mGameDrawer.Tick(timespan);
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		if(mRunningMode == RunningMode.RotatingCCW || mRunningMode == RunningMode.RotatingCW)
		{
			mGameDrawer.DrawCacheBitmap(canvas, mRotateAngle, mRotateScale);
		} else
		{
			mGameDrawer.Draw(canvas, mWorld);
			if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
			{
				mGameDrawer.DrawCursor(canvas, mCursorPosition.x, mCursorPosition.y, mRunningMode != RunningMode.Stopped);
			}
		}
	
		mWidgetPage.Draw(canvas);
		
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
			
		super.Redraw(canvas);
	}
	
	@Override
	public void handleTap(int x, int y) {
		if(!mCompleted)
		{
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
	}
	
	@Override
	public boolean handleBack() {
		mPendMode = mModeMenu;
		return true;
	}
	
	@Override
	public void handleDPad(Direction direction) {
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
	public void handleGesture(Direction direction) {
		if(mCursorPosition.x != -1 && mCursorPosition.y != -1 && mRunningMode == RunningMode.Stopped)
		{
			switch(direction)
			{
			case North:
				mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.North);
				SoundManager.PlaySound(mClickSound);
				break;
			case South:
				mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.South);
				SoundManager.PlaySound(mClickSound);
				break;
			case West:
				mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.West);
				SoundManager.PlaySound(mClickSound);
				break;
			case East:
				mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.East);
				SoundManager.PlaySound(mClickSound);
				break;
			}
			updateArrowStock();
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
	
	private void updateArrowStock() {
		ArrayList<Direction> arrows = mWorld.getArrowStock();
		EnumMap<Direction, Integer> arrow_count = new EnumMap<Direction, Integer>(Direction.class);
		arrow_count.put(Direction.East, 0);
		arrow_count.put(Direction.South, 0);
		arrow_count.put(Direction.North, 0);
		arrow_count.put(Direction.West, 0);
		for (Direction arrow : arrows) {
			arrow_count.put(arrow, arrow_count.get(arrow) + 1); 
		}

		mLeftArrowCount.setText(Integer.toString(arrow_count.get(Direction.West)));
		mRightArrowCount.setText(Integer.toString(arrow_count.get(Direction.East)));
		mUpArrowCount.setText(Integer.toString(arrow_count.get(Direction.North)));
		mDownArrowCount.setText(Integer.toString(arrow_count.get(Direction.South)));
	}
	
	@Override
	public boolean getMenu(Menu menu, boolean clear) {
		super.getMenu(menu, clear);
		menu.add(0, E_MENU_ROTATE, 0, R.string.game_rotate).setEnabled(mRunningMode != RunningMode.RotatingCW && mRunningMode != RunningMode.RotatingCCW );
		menu.add(0, E_MENU_BACK, 0, R.string.game_back);
		return true;
	}

	private void changeRotation() {
		switch(mRunningMode)
		{
		case RotatingCW:
			mWorld.RotateRight();
			mGameDrawer = mGameDrawerRot;
			mGameDrawer.CreateCacheBitmap(mWorld);
			updateArrowStock();
			break;
		case RotatingCCW:
			mWorld.RotateLeft();
			mGameDrawer = mGameDrawerNorm;
			mGameDrawer.CreateCacheBitmap(mWorld);
			updateArrowStock();
			break;
		}
		mCursorPosition.x = -1;
		mCursorPosition.y = -1;
	}

	@Override
	public boolean handleMenuSelection(MenuItem item) {
		switch(item.getItemId())
		{
		case E_MENU_ROTATE:
			if(mWorld.getRotation() == 0)
			{
				mWorld.Reset();
				mRotateTimer = 0;
				mRunningMode = RunningMode.RotatingCW;
				mGameDrawer.CreateCacheBitmap(mWorld);
			}
			else
			{
				mWorld.Reset();
				mRotateTimer = 0;
				mRunningMode = RunningMode.RotatingCCW;
				mGameDrawer.CreateCacheBitmap(mWorld);
			}
			return true;
		case E_MENU_BACK:
			mPendMode = mModeMenu;
		default:
			return super.handleMenuSelection(item);
		}
		
	}
	
	private Runnable mLevelCompleteRunnable = new Runnable() {
		public void run() {
			mCompleteDialog = new Dialog(mContext);
			mCompleteDialog.setContentView(R.layout.game_complete);
			
			Button next = (Button)mCompleteDialog.findViewById(R.id.game_complete_next);
			Button menu = (Button)mCompleteDialog.findViewById(R.id.game_complete_menu);
			TextView progress_text = (TextView)mCompleteDialog.findViewById(R.id.game_complete_progress);
			
			if(mProgress.getCompletedCount() < mProgress.getLevelPackSize())
			{
				progress_text.setText(Integer.toString(mProgress.getCompletedCount()) + "/" + Integer.toString(mProgress.getLevelPackSize()));
				next.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						try
						{
							mSemaphore.acquire();

							mProgress.nextUnbeaten();
							SPWorld world = mProgress.getWorld();
							mPendMode = new ModeGame(world, mModeMenu, mProgress, mSkin);
							
							mSemaphore.release();
						} catch(InterruptedException int_ex)
						{
							//Won't happen, has to be handled
						} catch(Exception other_ex)
						{
							mPendMode = mModeMenu;
							mSemaphore.release();
						} finally
						{
							mCompleteDialog.dismiss();
						}
					}
				});	
			} else
			{
				next.setEnabled(false);
				progress_text.setText(R.string.game_complete_100_complete);
			}
			
			menu.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try
					{
						mSemaphore.acquire();
						mPendMode = mModeMenu;
						mCompleteDialog.dismiss();
						mSemaphore.release();
					} catch(InterruptedException int_ex)
					{
						//Won't happen, has to be handled
					}
				}
			});
			
			mCompleteDialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					try
					{
						mSemaphore.acquire();
						mPendMode = mModeMenu;
						mCompleteDialog.dismiss();
						mSemaphore.release();
					} catch(InterruptedException int_ex)
					{
						//Won't happen, has to be handled
					}	
				}
			});
			
			mCompleteDialog.show();
		}
	};
   
   private Runnable mShowSplashRunnable = new Runnable() {
		public void run() {
			mSplashDialog = new Dialog(mContext);
			mSplashDialog.setTitle(mWorld.getLevelName());
			mSplashDialog.setContentView(R.layout.game_splash);
			
			TextView splash_message = (TextView)mSplashDialog.findViewById(R.id.game_splash_message);
			Button splash_dismiss_btn = (Button)mSplashDialog.findViewById(R.id.game_splash_button);
			
			splash_message.setText(mWorld.getSplashMessage());
			splash_dismiss_btn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try
					{
						mSemaphore.acquire();
						mSplashDialog.dismiss();
						mSemaphore.release();
					} catch(InterruptedException int_ex)
					{
						//Won't happen, has to be handled
					}
				}
			});
			mSplashDialog.show();
		}
	};
}
