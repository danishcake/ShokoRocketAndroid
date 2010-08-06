package uk.danishcake.shokorocket.moding;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.moding.Mode;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.SquareType;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.simulation.World;
import uk.danishcake.shokorocket.simulation.World.WorldState;
import uk.danishcake.shokorocket.sound.SoundManager;

public class ModeEditor extends Mode {
	private enum EditMode
	{
		Walls, Mice, Cats, Arrows, Holes, Rockets
	}
	private EditMode mEditMode = EditMode.Walls;
	private Widget mEditModeWidget;

	private Dialog mNewLevelDialog = null;
	private Runnable mNewLevelRunnable = new Runnable() {
		public void run() {
			mNewLevelDialog = new Dialog(mContext);
			mNewLevelDialog.setContentView(uk.danishcake.shokorocket.R.layout.editor_new_level);
			mNewLevelDialog.setTitle("Create a new level");
			((Spinner)mNewLevelDialog.findViewById(R.id.LevelWidth)).setSelection(9);
			((Spinner)mNewLevelDialog.findViewById(R.id.LevelHeight)).setSelection(6);
			
			Button createLevel = (Button) mNewLevelDialog.findViewById(R.id.CreateLevel);
			createLevel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try
					{
					mSemaphore.acquire();
					Spinner width_spinner = (Spinner) mNewLevelDialog.findViewById(R.id.LevelWidth);
					Spinner height_spinner = (Spinner) mNewLevelDialog.findViewById(R.id.LevelHeight);
					TextView level_author = (TextView) mNewLevelDialog.findViewById(R.id.LevelAuthor);
					TextView level_name = (TextView) mNewLevelDialog.findViewById(R.id.LevelName);
					
					World world = new World(Integer.parseInt((String)width_spinner.getSelectedItem()),
											Integer.parseInt((String)height_spinner.getSelectedItem()));
					world.setAuthor(level_author.getText().toString());
					world.setLevelName(level_name.getText().toString());
					
					mGameDrawer = new GameDrawer();

					int grid_size = mContext.getResources().getInteger(R.integer.grid_size);
					int required_width = world.getWidth() * grid_size ;
					int required_height = world.getHeight() * grid_size ;
					float scaleX = ((float)mScreenWidth - 16) / (float)required_width;
					float scaleY = ((float)(mScreenHeight - 156 - 8)) / (float)required_height;
					float smaller = scaleX < scaleY ? scaleX : scaleY;
					
					if(smaller < 1)
						mGameDrawer.Setup(mContext, (int)(((float)grid_size) * smaller));
					else
						mGameDrawer.Setup(mContext, grid_size );
					//mGameDrawer.CreateBackground(world);
					mGameDrawer.setDrawOffset(mScreenWidth / 2 - (world.getWidth() * mGameDrawer.getGridSize() / 2), 16);
					
					mWorld = world;
					mWidgetPage = new WidgetPage();
					mWidgetPage.setFontSize(mFontSize);
					InitialiseWidgets();
					
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
	
	private Context mContext;
	private World mWorld = null;
	private GameDrawer mGameDrawer = null;
	private ModeMenu mModeMenu;
	private RunningMode mRunningMode = RunningMode.Stopped;
	
	private int mResetTimer = 0;
	private static final int ResetTime = 1500;

	
	private int mCatSound = -1;
	private int mClickSound = -1;
	private int mMouseSound = -1;
	
	boolean mSaveNeeded = false;
	boolean mValidated = false;
	
	private WidgetPage mWidgetPage = null;
	
	private Vector2i mCursorPosition = new Vector2i(-1, -1);
	private Vector2i mGestureStart = new Vector2i(0, 0);
	private Vector2i mGestureEnd = new Vector2i(0, 0);
	private Direction mGestureDirection = Direction.Invalid;
	private boolean mGestureInProgress = false;
	
	private int mBtnSize = 48;
	private int mBtnSize2 = 64;
	private int mBtnSep = 8;
	private int mBtnBorder = 16;
	private int mFontSize = 16;
	
	private Menu mMenu = null;
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
	
	
	public ModeEditor(ModeMenu menu)
	{
		mModeMenu = menu;
	}
	
	private void InitialiseWidgets() {
		try
		{
			NinePatchData np = new NinePatchData(BitmapFactory.decodeStream(mContext.getAssets().open("Bitmaps/GUI/Blank64x64.png")), 24, 24, 24, 24);
			mEditModeWidget = new Widget(np, new Rect(mScreenWidth  - mBtnSize2 * 2 - mBtnBorder, 
													  mScreenHeight - mBtnSize - mBtnBorder, 
													  mScreenWidth  - mBtnBorder, 
													  mScreenHeight - mBtnBorder));
			mEditModeWidget.setText("Wall");
			mEditModeWidget.setOnClickListener(new uk.danishcake.shokorocket.gui.OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					switch(mEditMode)
					{
					case Walls:
						mEditMode = EditMode.Arrows;
						mEditModeWidget.setText("Arrows");
						break;
					case Arrows:
						mEditMode = EditMode.Mice;
						mEditModeWidget.setText("Mice");
						break;
					case Mice:
						mEditMode = EditMode.Cats;
						mEditModeWidget.setText("Cats");
						break;
					case Cats:
						mEditMode = EditMode.Holes;
						mEditModeWidget.setText("Holes");
						break;
					case Holes:
						mEditMode = EditMode.Rockets;
						mEditModeWidget.setText("Rockets");
						break;
					case Rockets:
						mEditMode = EditMode.Walls;
						mEditModeWidget.setText("Walls");
						break;
					}
				}
			});
			
			mWidgetPage.addWidget(mEditModeWidget);
		} catch(IOException io_ex)
		{
			Log.e("ModeEditor.InitialiseWidget", "Unable to open Blank64x64.png");
		}
	}

	@Override
	public void Setup(final Context context) {
		mContext = context;
		
		mBtnSize = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_size);
		mBtnSize2 = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_wide_size);
		mBtnSep = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_sep);
		mBtnBorder = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_border);
		mFontSize = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_font_size);
		
		Handler handler = new Handler(context.getMainLooper());
		handler.post(mNewLevelRunnable);
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
					}
				} else
					mResetTimer = 0;

				break;
			}
			mGameDrawer.Tick(timespan);
			mWidgetPage.Tick(timespan);
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
		if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
		{
			switch(mEditMode)
			{
			case Walls:
				mWorld.toggleDirection(mCursorPosition.x, mCursorPosition.y, direction);
				break;
			case Arrows:
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
	public boolean getMenu(Menu menu) {
		menu.clear();
	//	SubMenu sm = menu.addSubMenu("File");
		menu.add(0, E_MENU_NEW, 0, "New");
		menu.add(0, E_MENU_SAVE, 0, "Save");
		menu.add(0, E_MENU_SHARE, 0, "Share");
		
		
	//	SubMenu sm2 = menu.addSubMenu("Level");
		menu.add(1, E_MENU_PROPERTIES, 0, "Properties");
		menu.add(1, E_MENU_VERIFY, 0, "Verify");
		
		SubMenu sm_edit = menu.addSubMenu("Edit mode");
		MenuItem mi_walls = sm_edit.add(2, E_MENU_WALLMODE, 0, "Walls");
		MenuItem mi_mice = sm_edit.add(2, E_MENU_MOUSEMODE, 0, "Mice");
		MenuItem mi_cats = sm_edit.add(2, E_MENU_CATMODE, 0, "Cats");
		MenuItem mi_arrows = sm_edit.add(2, E_MENU_ARROWMODE, 0, "Arrows");
		MenuItem mi_rocket = sm_edit.add(2, E_MENU_ROCKETMODE, 0, "Rocket");
		MenuItem mi_holes = sm_edit.add(2, E_MENU_HOLEMODE, 0, "Hole");
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
	
	@Override
	public boolean handleMenuSelection(MenuItem item) {
		switch(item.getItemId())
		{
		case E_MENU_NEW:
			AlertDialog.Builder builder = new Builder(mContext);
			builder.setMessage("Are you sure you want to create a new level?");
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
			builder.setNegativeButton("No", null);
			AlertDialog ad = builder.create();
			ad.show();
			break;
		case E_MENU_SAVE:
			break;
		case E_MENU_SHARE:
			break;
		case E_MENU_VERIFY:
			break;
		case E_MENU_PROPERTIES:
			break;
		case E_MENU_WALLMODE:
			mEditMode = EditMode.Walls;
			mEditModeWidget.setText("Walls");
			break;
		case E_MENU_MOUSEMODE:
			mEditMode = EditMode.Mice;
			mEditModeWidget.setText("Mice");
			break;
		case E_MENU_CATMODE:
			mEditMode = EditMode.Cats;
			mEditModeWidget.setText("Cats");
			break;
		case E_MENU_ARROWMODE:
			mEditMode = EditMode.Arrows;
			mEditModeWidget.setText("Arrows");
			break;
		case E_MENU_HOLEMODE:
			mEditMode = EditMode.Holes;
			mEditModeWidget.setText("Holes");
			break;
		case E_MENU_ROCKETMODE:
			mEditMode = EditMode.Rockets;
			mEditModeWidget.setText("Rockets");
			break;
		default:
			return false;
		}
		return true;
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		if(mNewLevelDialog != null) {
			
		} else if(mWorld != null){
			//World has been created, so at next stage
			mGameDrawer.DrawBackground(canvas, mWorld);
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
