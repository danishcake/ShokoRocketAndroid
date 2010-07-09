package uk.danishcake.shokorocket.moding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;

import uk.danishcake.shokorocket.animation.GameDrawer;
import uk.danishcake.shokorocket.gui.NinePatchData;
import uk.danishcake.shokorocket.gui.OnClickListener;
import uk.danishcake.shokorocket.gui.Widget;
import uk.danishcake.shokorocket.gui.WidgetPage;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.simulation.World;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.World.WorldState;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

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
	
	enum RunningMode { Stopped, Running, RunningFast }
	private RunningMode mRunningMode = RunningMode.Stopped;
	
	public ModeGame(World world, ModeMenu menu)
	{
		mWorld = world;
		mModeMenu = menu;
	}
	
	@Override
	public void Setup(Context context) {
		mContext = context;
		//Setup autoscaling?
		int required_width = mWorld.getWidth() * 32;
		float scale = mScreenWidth / (float)required_width;
		if(required_width > mScreenWidth)
			mGameDrawer.Setup(mContext, (int)(32 * scale));
		else
			mGameDrawer.Setup(mContext, 32);
		mGameDrawer.CreateBackground(mWorld);
		mGameDrawer.setDrawOffset(mScreenWidth / 2 - (mWorld.getWidth() * mGameDrawer.getGridSize() / 2), 16);
		
		//Setup widgets
		NinePatchData btn_np;
		try {
			btn_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/Blank64x64.png")), 24, 24, 24, 24);
			NinePatchData west_arrow_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/WestArrowBox.png")), 8, 8, 22, 42);
			NinePatchData north_arrow_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/NorthArrowBox.png")), 8, 8, 22, 42);
			NinePatchData south_arrow_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/SouthArrowBox.png")), 8, 8, 22, 42);
			NinePatchData east_arrow_np = new NinePatchData(BitmapFactory.decodeStream(context.getAssets().open("Bitmaps/GUI/EastArrowBox.png")), 8, 8, 22, 42);
			
			
			Widget reset = new Widget(btn_np, new Rect(mScreenWidth - 64 - 16, mScreenHeight - 64, mScreenWidth - 16, mScreenHeight - 16));
			reset.setText("Reset");
			reset.setOnClickListener(new OnClickListener() {				
				@Override
				public void OnClick(Widget widget) {
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
			
			Widget go = new Widget(btn_np, new Rect(mScreenWidth - 64 - 64 - 4 - 16, mScreenHeight - 64, mScreenWidth - 64 - 4 - 16, mScreenHeight - 16));
			go.setText("Go");
			go.setOnClickListener(new OnClickListener() {				
				@Override
				public void OnClick(Widget widget) {
					if(mRunningMode == RunningMode.Stopped)
						mRunningMode = RunningMode.Running;
					else if(mRunningMode == RunningMode.Running)
						mRunningMode = RunningMode.RunningFast;
				}
			});
			
			
			Widget back = new Widget(btn_np, new Rect(16, mScreenHeight - 64, 16 + 64, mScreenHeight - 16));
			back.setText("Back");
			back.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					mPendMode = mModeMenu;
				}
			});
			
			Widget west_arrows = new Widget(west_arrow_np, new Rect(16, 256, 16+48, 256 + 72));
			west_arrows.setText("0");
			west_arrows.setVerticalAlignment(Widget.VerticalAlignment.Top);
			west_arrows.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
						mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.West);
					updateArrowStock();
				}
			});
			
			//Given width 480 take 16 from each side and width -> 400 ->100 spacing
			Widget north_arrows = new Widget(north_arrow_np, new Rect(16 + (mScreenWidth - 32 - 48) / 3, 256, 16 + (mScreenWidth - 32 - 48) / 3 + 48, 256 + 72));
			north_arrows.setText("0");
			north_arrows.setVerticalAlignment(Widget.VerticalAlignment.Top);
			north_arrows.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
						mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.North);
					updateArrowStock();
				}
			});
			
			Widget south_arrows = new Widget(south_arrow_np, new Rect(16 + 2 * + (mScreenWidth - 32 - 48) / 3, 256, 16 + 2 * (mScreenWidth - 32 - 48) / 3 + 48, 256 + 72));
			south_arrows.setText("0");
			south_arrows.setVerticalAlignment(Widget.VerticalAlignment.Top);
			south_arrows.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
						mWorld.toggleArrow(mCursorPosition.x, mCursorPosition.y, Direction.South);
					updateArrowStock();
				}
			});
		
			Widget east_arrows = new Widget(east_arrow_np, new Rect(mScreenWidth - 16 - 48, 256, mScreenWidth - 16, 256 + 72));
			east_arrows.setText("0");
			east_arrows.setVerticalAlignment(Widget.VerticalAlignment.Top);
			east_arrows.setOnClickListener(new OnClickListener() {
				@Override
				public void OnClick(Widget widget) {
					if(mCursorPosition.x != -1 && mCursorPosition.y != -1)
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
			WorldState state = mWorld.getWorldState();
			if(state == WorldState.Failed)
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
		
		mWidgetPage.Tick(timespan);
		mGameDrawer.Tick(timespan);
		return super.Tick(timespan);
	}
	
	@Override
	public void Redraw(Canvas canvas) {
		mGameDrawer.Draw(canvas, mWorld);
		if(mCursorPosition.x != -1 && mCursorPosition.y != -1 && mRunningMode == RunningMode.Stopped)
		{
			mGameDrawer.DrawCursor(canvas, mCursorPosition.x, mCursorPosition.y);
		}
		
		mWidgetPage.Draw(canvas);
		super.Redraw(canvas);
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
		}
	}
	
	@Override
	public boolean handleBack() {
		mPendMode = mModeMenu;
		return true;
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
