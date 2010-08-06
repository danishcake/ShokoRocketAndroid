package uk.danishcake.shokorocket.animation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.SquareType;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.simulation.Walker;
import uk.danishcake.shokorocket.simulation.World;
import uk.danishcake.shokorocket.simulation.Walker.WalkerState;

/**
 * Encapsulates methods to draw a world
 * @author Edward
 *
 */
public class GameDrawer {
	private Bitmap mWorldBitmap = null;
	private boolean mAnimationsLoaded = false;
	private EnumMap<Direction, Animation> mMouseAnimations = new EnumMap<Direction, Animation>(Direction.class);
	private Animation mMouseDeathAnimation = null;
	private Animation mMouseRescueAnimation = null;
	private EnumMap<Direction, Animation> mCatAnimations = new EnumMap<Direction, Animation>(Direction.class);
	private Animation mCatDeathAnimation = null;
	private Animation mRocketAnimation = null;
	private Animation mHoleAnimation = null;
	private Animation mRingAnimation = null;
	private EnumMap<Direction, Animation> mFullArrowAnimations = new EnumMap<Direction, Animation>(Direction.class);
	private EnumMap<Direction, Animation> mGestureArrowAnimations = new EnumMap<Direction, Animation>(Direction.class);
	private EnumMap<Direction, Animation> mHalfArrowAnimations = new EnumMap<Direction, Animation>(Direction.class);
	private Bitmap mTileA = null;
	private Bitmap mTileB = null;
	private Animation mNorthWall = null;
	private Animation mWestWall = null;
	private Animation mCursorAnimation = null;
	private Animation mTransCursorAnimation = null;
	private Animation mTickAnimation = null;
	
	private int mGridSize = 32;
	
	private int mDrawOffsetX = 0;
	private int mDrawOffsetY = 0;
	
	/**
	 * Changes the position that the game is drawn at. Units are pixels
	 * @param x
	 * @param y
	 */
	public void setDrawOffset(int x, int y) {
		mDrawOffsetX = x;
		mDrawOffsetY = y;
	}
	
	public Vector2i getDrawOffset() {
		return new Vector2i(mDrawOffsetX, mDrawOffsetY);
	}
	
	public int getGridSize() {
		return mGridSize;
	}
	
	public void DrawCursor(Canvas canvas, int x, int y, boolean running) 
	{
		if(running)
			mTransCursorAnimation.DrawCurrentFrame(canvas, x * mGridSize + mDrawOffsetX, y * mGridSize + mDrawOffsetY);
		else
			mCursorAnimation.DrawCurrentFrame(canvas, x * mGridSize + mDrawOffsetX, y * mGridSize + mDrawOffsetY);
	}
	
	public Animation GetTick() {
		return mTickAnimation;
	}
	
	public Animation GetArrow(Direction direction) {
		return mGestureArrowAnimations.get(direction);
	}
	
	/**
	 * Creates the background texture
	 * @param world The world to create the background texture from
	 */
	public void CreateBackground(World world)
	{
		//Create background image
		if(world != null)
		{
			mWorldBitmap = Bitmap.createBitmap(world.getWidth() * mGridSize, world.getHeight() * mGridSize, Bitmap.Config.ARGB_8888);
			Canvas world_canvas = new Canvas(mWorldBitmap);
			world_canvas.drawARGB(255, 255, 255, 255);
			
			for(int y = 0; y < world.getHeight(); y++)
			{
				boolean use_tile_a = (y % 2 == 0);
				for(int x = 0; x < world.getWidth(); x++)	
				{
					if(use_tile_a)
						world_canvas.drawBitmap(mTileA, x * mGridSize, y * mGridSize, null);
					else
						world_canvas.drawBitmap(mTileB, x * mGridSize, y * mGridSize, null);
					use_tile_a = !use_tile_a;
				}
			}
			for(int y = 0; y < world.getHeight(); y++)
			{
				for(int x = 0; x < world.getWidth(); x++)
				{
					if(world.getWest(x, y))
					{
						mWestWall.DrawCurrentFrame(world_canvas, x * mGridSize, y * mGridSize);
						if(x == 0)
							mWestWall.DrawCurrentFrame(world_canvas, world_canvas.getWidth() - mWestWall.getCurrentFrame().getWidth(), y * mGridSize);
					}
					if(world.getNorth(x, y))
					{
						mNorthWall.DrawCurrentFrame(world_canvas, x * mGridSize, y * mGridSize);
						if(y == 0)
							mNorthWall.DrawCurrentFrame(world_canvas, x * mGridSize, world_canvas.getHeight() - mNorthWall.getCurrentFrame().getHeight());
					}
				}
			}
			
		} else //Destroy background if passed null
		{
			mWorldBitmap = null;
		}		
	}
	
	public void DrawBackground(Canvas canvas, World world)
	{
		int wall_offset = -mWestWall.getFrameByIndex(0).getWidth() / 2;
		for(int y = 0; y < world.getHeight(); y++)
		{
			boolean use_tile_a = (y % 2 == 0);
			for(int x = 0; x < world.getWidth(); x++)	
			{
				if(use_tile_a)
					canvas.drawBitmap(mTileA, x * mGridSize + mDrawOffsetX, y * mGridSize + mDrawOffsetY, null);
				else
					canvas.drawBitmap(mTileB, x * mGridSize + mDrawOffsetX, y * mGridSize + mDrawOffsetY, null);
				use_tile_a = !use_tile_a;
			}
		}
		for(int y = 0; y < world.getHeight(); y++)
		{
			for(int x = 0; x < world.getWidth(); x++)
			{
				if(world.getWest(x, y))
				{
					mWestWall.DrawCurrentFrame(canvas, x * mGridSize + mDrawOffsetX + wall_offset, y * mGridSize + mDrawOffsetY);
					if(x == 0)
						mWestWall.DrawCurrentFrame(canvas, world.getWidth() * mGridSize - mWestWall.getCurrentFrame().getWidth() + mDrawOffsetX, y * mGridSize + mDrawOffsetY);
				}
				if(world.getNorth(x, y))
				{
					mNorthWall.DrawCurrentFrame(canvas, x * mGridSize + mDrawOffsetX, y * mGridSize + mDrawOffsetY + wall_offset);
					if(y == 0)
						mNorthWall.DrawCurrentFrame(canvas, x * mGridSize + mDrawOffsetX, world.getHeight() * mGridSize - mNorthWall.getCurrentFrame().getHeight() + mDrawOffsetY);
				}
			}
		}
	}
	
	/**
	 * Loads any unloaded textures
	 * @param context The context to obtain animations from
	 */
	public void Setup(Context context, int gridSize)
	{
		mGridSize = gridSize;
		float referenceGridSize = (float)context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.grid_size);
		float scale = ((float)mGridSize) / referenceGridSize; 

		//Load unloaded animations
		if(context != null && !mAnimationsLoaded)
		{
			try
			{
//				Map<String, Animation> mouse_animations = Animation.GetAnimations(context, "Animations/Game/Mouse_ShokoWhite.animation", scale);
				
				Map<String, Animation> mouse_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.mouse_shokowhite, scale);
				mMouseAnimations.put(Direction.North, mouse_animations.get("North"));
				mMouseAnimations.put(Direction.South, mouse_animations.get("South"));
				mMouseAnimations.put(Direction.East, mouse_animations.get("East"));
				mMouseAnimations.put(Direction.West, mouse_animations.get("West"));
				mMouseAnimations.put(Direction.Invalid, mouse_animations.get("Stopped"));
				mMouseDeathAnimation = mouse_animations.get("Death");
				mMouseRescueAnimation = mouse_animations.get("Rescue");

				Map<String, Animation> cat_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.kapukapu, scale);
				mCatAnimations.put(Direction.North, cat_animations.get("North"));
				mCatAnimations.put(Direction.South, cat_animations.get("South"));
				mCatAnimations.put(Direction.East, cat_animations.get("East"));
				mCatAnimations.put(Direction.West, cat_animations.get("West"));
				mCatAnimations.put(Direction.Invalid, cat_animations.get("Stopped"));
				mCatDeathAnimation = cat_animations.get("Death");

				
				Map<String, Animation> arrow_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.arrows, scale);
				mFullArrowAnimations.put(Direction.North, arrow_animations.get("North"));
				mFullArrowAnimations.put(Direction.South, arrow_animations.get("South"));
				mFullArrowAnimations.put(Direction.East, arrow_animations.get("East"));
				mFullArrowAnimations.put(Direction.West, arrow_animations.get("West"));
				mFullArrowAnimations.put(Direction.Invalid, arrow_animations.get("Stopped"));
				
				Map<String, Animation> g_arrow_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.gesture_arrows);
				mGestureArrowAnimations.put(Direction.North, g_arrow_animations.get("North"));
				mGestureArrowAnimations.put(Direction.South, g_arrow_animations.get("South"));
				mGestureArrowAnimations.put(Direction.East, g_arrow_animations.get("East"));
				mGestureArrowAnimations.put(Direction.West, g_arrow_animations.get("West"));
				mGestureArrowAnimations.put(Direction.Invalid, g_arrow_animations.get("Stopped"));
				
				
				Map<String, Animation> half_arrow_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.halfarrows, scale);
				mHalfArrowAnimations.put(Direction.North, half_arrow_animations.get("North"));
				mHalfArrowAnimations.put(Direction.South, half_arrow_animations.get("South"));
				mHalfArrowAnimations.put(Direction.East, half_arrow_animations.get("East"));
				mHalfArrowAnimations.put(Direction.West, half_arrow_animations.get("West"));
				mHalfArrowAnimations.put(Direction.Invalid, half_arrow_animations.get("Stopped"));
				
				Map<String, Animation> rocket_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.rocket, scale);
				mRocketAnimation = rocket_animations.get("Normal");
				
				Map<String, Animation> hole_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.hole, scale);
				mHoleAnimation = hole_animations.get("All");				
				
				Map<String, Animation> ring_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.ring, scale);
				mRingAnimation = ring_animations.get("All");
				
				Map<String, Animation> wall_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.walls, scale);
				mNorthWall = wall_animations.get("Horizontal");
				mWestWall = wall_animations.get("Vertical");
				
				Map<String, Animation> tile_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.tiles, scale);
				
				mTileA = tile_animations.get("TileA").getCurrentFrame();
				mTileB = tile_animations.get("TileB").getCurrentFrame();
				
				Map<String, Animation> cursor_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.cursor, scale); 
				mCursorAnimation = cursor_animations.get("Normal");
				mTransCursorAnimation = cursor_animations.get("Faded");

				Map<String, Animation> tick_animations = Animation.GetAnimations(context, uk.danishcake.shokorocket.R.raw.tick); 
				mTickAnimation = tick_animations.get("All");
				
				
				mAnimationsLoaded = true;
			} catch(IOException ex)
			{
				//TODO log or something
			}
		}
	}
	/**
	 * Draws the world to the canvas
	 * @param canvas
	 * @param world
	 */
	public void Draw(Canvas canvas, World world)
	{
		if(mWorldBitmap != null)
			canvas.drawBitmap(mWorldBitmap, mDrawOffsetX, mDrawOffsetY, null);
		//Draw rockets, arrows & holes		
		for(int x = 0; x < world.getWidth(); x++)
		{
			for(int y = 0; y < world.getHeight(); y++)
			{
				SquareType square = world.getSpecialSquare(x, y);
				int drawX = x * mGridSize + mDrawOffsetX;
				int drawY = y * mGridSize + mDrawOffsetY;
				switch(square)
				{
				case Hole:
					mHoleAnimation.DrawCurrentFrame(canvas, drawX, drawY);
					break;
				case Rocket:
					mRocketAnimation.DrawCurrentFrame(canvas, drawX, drawY);
					break;
				case NorthArrow:
				case SouthArrow:
				case EastArrow:
				case WestArrow:
					mFullArrowAnimations.get(square.GetDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
					break;
				case NorthHalfArrow:
				case SouthHalfArrow:
				case EastHalfArrow:
				case WestHalfArrow:
					mHalfArrowAnimations.get(square.GetDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
					break;
				}
			}
		}
		
		
		//Draw cats & mice
		ArrayList<Walker> mice = world.getLiveMice();
		ArrayList<Walker> cats = world.getLiveCats();
		for (Walker walker : mice) {
			Vector2i position = walker.getPosition();
			int x = position.x * mGridSize + mDrawOffsetX;
			int y = position.y * mGridSize + mDrawOffsetY;
			switch(walker.getDirection())
			{
			case North:
				y -= (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case South:
				y += (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case East:
				x += (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case West:
				x -= (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			}
			Animation animation = mMouseAnimations.get(walker.getDirection());
			if(animation != null) 
				animation.DrawCurrentFrame(canvas, x, y);
		}
		for (Walker walker : cats) {
			Vector2i position = walker.getPosition();
			int x = position.x * mGridSize + mDrawOffsetX;
			int y = position.y * mGridSize + mDrawOffsetY;
			switch(walker.getDirection())
			{
			case North:
				y -= (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case South:
				y += (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case East:
				x += (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case West:
				x -= (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			}
			Animation animation = mCatAnimations.get(walker.getDirection());
			if(animation != null) 
				animation.DrawCurrentFrame(canvas, x, y);		
		}
		for(Walker walker : world.getDeadMice())
		{
			Vector2i position = walker.getPosition();
			int x = position.x * mGridSize + mDrawOffsetX;
			int y = position.y * mGridSize + mDrawOffsetY;
			switch(walker.getDirection())
			{
			case North:
				y -= (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case South:
				y += (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case East:
				x += (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case West:
				x -= (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			}
			
			mRingAnimation.DrawCurrentFrame(canvas, x, y);
			
			int death_time = walker.getDeathTime();
			if(death_time < 5000)
			{
				y -= walker.getDeathTime() * 20 / 5000;
				mMouseDeathAnimation.DrawFrameAtTime(canvas, x, y, death_time / 5);
			}
		}
		for(Walker walker : world.getRescuedMice())
		{
			Vector2i position = walker.getPosition();
			int x = position.x * mGridSize + mDrawOffsetX;
			int y = position.y * mGridSize + mDrawOffsetY;
			switch(walker.getDirection())
			{
			case North:
				y -= (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case South:
				y += (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case East:
				x += (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			case West:
				x -= (mGridSize * walker.getFraction() / Walker.FractionReset);
				break;
			}
			
			int death_time = walker.getDeathTime();
			if(death_time < 5000)
			{
				mMouseRescueAnimation.DrawFrameAtTime(canvas, x, y, death_time / 5);
			}
		}
		for(Walker walker : world.getDeadCats())
		{
			if(walker.getWalkerState() == WalkerState.Rescued)
			{
				Vector2i position = walker.getPosition();
				int x = position.x * mGridSize + mDrawOffsetX;
				int y = position.y * mGridSize + mDrawOffsetY;
				switch(walker.getDirection())
				{
				case North:
					y -= (mGridSize * walker.getFraction() / Walker.FractionReset);
					break;
				case South:
					y += (mGridSize * walker.getFraction() / Walker.FractionReset);
					break;
				case East:
					x += (mGridSize * walker.getFraction() / Walker.FractionReset);
					break;
				case West:
					x -= (mGridSize * walker.getFraction() / Walker.FractionReset);
					break;
				}
				mRingAnimation.DrawCurrentFrame(canvas, x, y);
				
				int death_time = walker.getDeathTime();
				if(death_time < 5000)
				{
					mCatDeathAnimation.DrawFrameAtTime(canvas, x, y, death_time / 5);
				}
			}
		}
		
	}
	
	/**
	 * Update animation
	 * @param timespan The timespan in milliseconds
	 */
	public void Tick(int timespan)
	{
		for (Animation animation : mMouseAnimations.values()) {
			animation.Tick(timespan);
		}
		for (Animation animation : mCatAnimations.values()) {
			animation.Tick(timespan);
		}
		mMouseDeathAnimation.Tick(timespan);
	}
}
