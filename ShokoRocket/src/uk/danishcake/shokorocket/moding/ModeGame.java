package uk.danishcake.shokorocket.moding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;

import uk.danishcake.shokorocket.R;
import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.simulation.World;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.World.WorldState;
import uk.danishcake.shokorocket.sound.SoundManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;

public class ModeGame extends Mode {
	private Context mContext;
	private WidgetPage mWidgetPage = new WidgetPage();
	private GameDrawer mGameDrawer = new GameDrawer();
	private World mWorld;
	private ModeMenu mModeMenu;
	private int mResetTimer = 0;
	private static final int ResetTime = 1500;
	private Vector2i mCursorPosition = new Vector2i(-1, -1);
	private EnumMap<Direction, Widget> mArrowWidgets = new EnumMap<Direction, Widget>(Direction.class);
	boolean mCompleted = false;
	int mCompleteAge = 0;
	private Progress mProgress;
	
	private int mBtnSize = 48;
	private int mBtnSize2 = 64;
	private int mBtnSep = 8;
	private int mBtnBorder = 16;
	private int mFontSize = 16;
	
	private int mCatSound = -1;
	private int mClickSound = -1;
	private int mMouseSound = -1; 
	
	
	private RunningMode mRunningMode = RunningMode.Stopped;
	
	private Vector2i mGestureStart = new Vector2i(0, 0);
	private Vector2i mGestureEnd = new Vector2i(0, 0);
	private Direction mGestureDirection = Direction.Invalid;
	private boolean mGestureInProgress = false;
	
	public ModeGame(World world, ModeMenu menu, Progress progress)
	{
		mWorld = world;
		mModeMenu = menu;
		mProgress = progress;
		
		try
		{
			mCatSound = SoundManager.LoadSound("Sounds/Cat.ogg");
			mClickSound = SoundManager.LoadSound("Sounds/Click.ogg");
			mMouseSound = SoundManager.LoadSound("Sounds/Mouse.ogg");
		} catch(IOException io_ex)
		{
			//TODO log
		}
	}
	
	@Override
	public void Setup(Context context) {
		mContext = context;
		//Setup autoscaling
		int grid_size = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.grid_size);
		int required_width = mWorld.getWidth() * grid_size ;
		int required_height = mWorld.getHeight() * grid_size ;
		float scaleX = ((float)mScreenWidth - 16) / (float)required_width;
		float scaleY = ((float)(mScreenHeight - 156 - 8)) / (float)required_height;
		float smaller = scaleX < scaleY ? scaleX : scaleY;
		
		if(smaller < 1)
			mGameDrawer.Setup(mContext, (int)(((float)grid_size) * smaller));
		else
			mGameDrawer.Setup(mContext, grid_size );
		mGameDrawer.CreateBackground(mWorld);
		mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * mGameDrawer.getGridSize() / 2), 16);
		
		mBtnSize = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_size);
		mBtnSize2 = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_wide_size);
		mBtnSep = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_sep);
		mBtnBorder = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_border);
		mFontSize = context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.btn_font_size);

		NinePatchData btn_np;
		try {
			btn_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/Blank64x64.png")), 24, 24, 24, 24);
			NinePatchData west_arrow_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/WestArrowBox.png")), 8, 8, 22, 42);
			NinePatchData north_arrow_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/NorthArrowBox.png")), 8, 8, 22, 42);
			NinePatchData south_arrow_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/SouthArrowBox.png")), 8, 8, 22, 42);
			NinePatchData east_arrow_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/EastArrowBox.png")), 8, 8, 22, 42);
			
			
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
			
			
			Widget back = new Widget(btn_np, new Rect(mBtnBorder, mScreenHeight - mBtnSize - mBtnBorder, mBtnBorder + mBtnSize2, mScreenHeight - mBtnBorder));
			back.setText(context.getString(R.string.game_back));
			back.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					SoundManager.PlaySound(mClickSound);
					mPendMode = mModeMenu;
				}
			});
			
			reset.setFontSize(mFontSize);
			go.setFontSize(mFontSize);
			back.setFontSize(mFontSize);
			
			Widget west_arrows = new Widget(west_arrow_np, new Rect(16, mScreenHeight - 72 - mBtnBorder - mBtnSize - mBtnSep, mBtnBorder + 48, mScreenHeight - mBtnBorder - mBtnSize - mBtnSep));
			west_arrows.setText("0");
			west_arrows.setVerticalAlignment(Widget.VerticalAlignment.Top);
			west_arrows.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					SoundManager.PlaySound(mClickSound);
					if(mCursorPosition.x != -1 && mCursorPosition.y != -1 && mRunningMode == RunningMode.Stopped)
						mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.West);
					updateArrowStock();
				}
			});			
			
			//Given width 480 take 16 from each side and width -> 400 ->100 spacing
			Widget north_arrows = new Widget(north_arrow_np, new Rect(mBtnBorder + (mScreenWidth - mBtnBorder * 2 - 48) / 3,
					          										  mScreenHeight - 72 - mBtnBorder - mBtnSize - mBtnSep, 
					          										  mBtnBorder + (mScreenWidth - mBtnBorder * 2 - 48) / 3 + 48, 
					          										  mScreenHeight - mBtnBorder - mBtnSize - mBtnSep));
			north_arrows.setText("0");
			north_arrows.setVerticalAlignment(Widget.VerticalAlignment.Top);
			north_arrows.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					SoundManager.PlaySound(mClickSound);
					if(mCursorPosition.x != -1 && mCursorPosition.y != -1 && mRunningMode == RunningMode.Stopped)
						mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.North);
					updateArrowStock();
				}
			});
			
			Widget south_arrows = new Widget(south_arrow_np, new Rect(mBtnBorder + 2 * (mScreenWidth - mBtnBorder * 2 - 48) / 3,
					  												  mScreenHeight - 72 - mBtnBorder - mBtnSize - mBtnSep, 
					  												  mBtnBorder + 2 * (mScreenWidth - mBtnBorder * 2 - 48) / 3 + 48, 
					  												  mScreenHeight - mBtnBorder - mBtnSize - mBtnSep));
			south_arrows.setText("0");
			south_arrows.setVerticalAlignment(Widget.VerticalAlignment.Top);
			south_arrows.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					SoundManager.PlaySound(mClickSound);
					if(mCursorPosition.x != -1 && mCursorPosition.y != -1 && mRunningMode == RunningMode.Stopped)
						mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.South);
					updateArrowStock();
				}
			});
		
			Widget east_arrows = new Widget(east_arrow_np, new Rect(mScreenWidth - mBtnBorder - 48,
																  	mScreenHeight - 72 - mBtnBorder - mBtnSize - mBtnSep,
																  	mScreenWidth - mBtnBorder,
																  	mScreenHeight - mBtnBorder - mBtnSize - mBtnSep));
			east_arrows.setText("0");
			east_arrows.setVerticalAlignment(Widget.VerticalAlignment.Top);
			east_arrows.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					SoundManager.PlaySound(mClickSound);
					if(mCursorPosition.x != -1 && mCursorPosition.y != -1 && mRunningMode == RunningMode.Stopped)
						mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.East);
					updateArrowStock();
				}
			});
			
			mArrowWidgets.put(Direction.West, west_arrows);
			mArrowWidgets.put(Direction.North, north_arrows);
			mArrowWidgets.put(Direction.South, south_arrows);
			mArrowWidgets.put(Direction.East, east_arrows);
		
			
			mWidgetPage.addWidget(reset);
			mWidgetPage.addWidget(go);
			mWidgetPage.addWidget(back);
			mWidgetPage.addWidget(west_arrows);
			mWidgetPage.addWidget(north_arrows);
			mWidgetPage.addWidget(south_arrows);
			mWidgetPage.addWidget(east_arrows);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		updateArrowStock();
	}
	
	@Override
	public Mode Teardown() {
		mWorld.Reset();
		mRunningMode = RunningMode.Stopped;
		mWorld.ClearArrows();
		if(mCompleted)
			mProgress.MarkComplete(mWorld.getIdentifier());
		return super.Teardown();
	}
	
	@Override
	public ModeAction Tick(int timespan) {
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
				mResetTimer += timespan;
				if(mResetTimer > ResetTime)
				{
					mWorld.Reset();
					mRunningMode = RunningMode.Stopped;
				}
			} else
				mResetTimer = 0;
			if(state == WorldState.Success && !mCompleted)
			{
				mCompleted = true;
				mCompleteAge = mAge;
			}
			break;
		}
		
		mWidgetPage.Tick(timespan);
		mGameDrawer.Tick(timespan);
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		mGameDrawer.Draw(canvas, mWorld);
		if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
		{
			mGameDrawer.DrawCursor(canvas, mCursorPosition.x, mCursorPosition.y, mRunningMode != RunningMode.Stopped);
		}
		
		mWidgetPage.Draw(canvas);
		
		if(mCompleted)
		{
			float scale = ((float)mAge - (float)mCompleteAge) / 1500.0f;
			if(scale > 1.0f)
				scale = 1.0f;
			scale = (float) Math.pow(scale, 0.5d);
			
			Paint text_paint = new Paint();
			text_paint.setAntiAlias(true);
			text_paint.setTypeface(Typeface.MONOSPACE);
			text_paint.setTextAlign(Align.CENTER);
			text_paint.setTextSize(50);
			text_paint.setFakeBoldText(true);
			text_paint.setARGB(255, 255, 255, 255);
			int y = (int)(-(text_paint.descent() - text_paint.ascent()) + (float)mScreenHeight * scale);
			
			
			canvas.drawText("Success", mScreenWidth/2, y, text_paint);
			text_paint.setFakeBoldText(false);
			text_paint.setARGB(255, 0, 166, 0);
			canvas.drawText("Success", mScreenWidth/2, y, text_paint);
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
		} else
		{
			mPendMode = mModeMenu;
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
		if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
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
		ArrayList<Direction> arrows =  mWorld.getArrowStock();
		EnumMap<Direction, Integer> arrow_count = new EnumMap<Direction, Integer>(Direction.class);
		arrow_count.put(Direction.East, 0);
		arrow_count.put(Direction.South, 0);
		arrow_count.put(Direction.North, 0);
		arrow_count.put(Direction.West, 0);
		for (Direction arrow : arrows) {
			arrow_count.put(arrow, arrow_count.get(arrow) + 1); 
		}
		for(Direction direction : Direction.values())
		{
			Widget widget = mArrowWidgets.get(direction);
			if(widget != null)
			{
				widget.setText(arrow_count.get(direction).toString());
			}
		}
	}
}
