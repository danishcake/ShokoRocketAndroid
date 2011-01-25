package uk.danishcake.shokorocket.animation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import uk.danishcake.shokorocket.moding.SkinProgress;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.MPWorld;
import uk.danishcake.shokorocket.simulation.SPWorld;
import uk.danishcake.shokorocket.simulation.SquareType;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.simulation.Walker;
import uk.danishcake.shokorocket.simulation.WorldBase;
import uk.danishcake.shokorocket.simulation.Walker.WalkerState;

/**
 * Encapsulates methods to draw a world
 * @author Edward Woolhouse
 *
 */
public class GameDrawer {
	private class RenderItem implements Comparable<RenderItem>{
		public int x;
		public int y;
		Animation animation;
		@Override
		public int compareTo(RenderItem another) {
			// return negative if this < another
			if(animation == null && another.animation != null)
				return 1;
			if(animation != null && another.animation == null)
				return -1;
			return y - another.y;
		}
	}
	
	private Bitmap mWorldBitmap = null;
	private EnumMap<Direction, Animation> mMouseAnimations = new EnumMap<Direction, Animation>(Direction.class);
	private Animation mMouseDeathAnimation = null;
	private Animation mMouseRescueAnimation = null;
	//MP only Mouse
	private EnumMap<Direction, Animation> mSpecialMouseAnimations = new EnumMap<Direction, Animation>(Direction.class);
	private Animation mSpecialMouseDeathAnimation = null;
	private Animation mSpecialMouseRescueAnimation = null;
	//MP only Mouse
	private EnumMap<Direction, Animation> mGoldMouseAnimations = new EnumMap<Direction, Animation>(Direction.class);
	private Animation mGoldMouseDeathAnimation = null;
	private Animation mGoldMouseRescueAnimation = null;
	
	private EnumMap<Direction, Animation> mCatAnimations = new EnumMap<Direction, Animation>(Direction.class);
	private Animation mCatDeathAnimation = null;
	private Animation mRocketAnimation = null;
	private Animation[] mMPRocketAnimation = new Animation[4];
	private Animation mHoleAnimation = null;
	private Animation mRingAnimation = null;
	private EnumMap<Direction, Animation> mFullArrowAnimations = new EnumMap<Direction, Animation>(Direction.class);
	private EnumMap<Direction, Animation> mGestureArrowAnimations = new EnumMap<Direction, Animation>(Direction.class);
	private EnumMap<Direction, Animation> mHalfArrowAnimations = new EnumMap<Direction, Animation>(Direction.class);
	
	private EnumMap<Direction, Animation> mMPFullArrowAnimations0 = new EnumMap<Direction, Animation>(Direction.class);
	private EnumMap<Direction, Animation> mMPHalfArrowAnimations0 = new EnumMap<Direction, Animation>(Direction.class);
	private EnumMap<Direction, Animation> mMPFullArrowAnimations1 = mFullArrowAnimations;
	private EnumMap<Direction, Animation> mMPHalfArrowAnimations1 = mHalfArrowAnimations;
	private EnumMap<Direction, Animation> mMPFullArrowAnimations2 = new EnumMap<Direction, Animation>(Direction.class);
	private EnumMap<Direction, Animation> mMPHalfArrowAnimations2 = new EnumMap<Direction, Animation>(Direction.class);
	private EnumMap<Direction, Animation> mMPFullArrowAnimations3 = new EnumMap<Direction, Animation>(Direction.class);
	private EnumMap<Direction, Animation> mMPHalfArrowAnimations3 = new EnumMap<Direction, Animation>(Direction.class);
	private Animation mSpawnerAnimation = null;

	private Bitmap mTileA = null;
	private Bitmap mTileB = null;
	private Animation mNorthWall = null;
	private Animation mWestWall = null;
	private Animation mCursorAnimation = null;
	private Animation[] mMPCursorAnimations = new Animation[4];
	private Animation mTransCursorAnimation = null;
	private Animation mTickAnimation = null;
	private Bitmap mCacheBitmap = null;
	private List<RenderItem> mRenderItems = new ArrayList<RenderItem>();
	
	private int mGridSize = 32;
	private int mDrawOffsetX = 0;
	private int mDrawOffsetY = 0;
	private float mScale = -1;
	private boolean mMPLoaded = false;
	
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
	
	public void drawMPCursor(Canvas canvas, int x, int y, int id) {
		mMPCursorAnimations[id].DrawCurrentFrame(canvas, x * mGridSize + mDrawOffsetX, y * mGridSize + mDrawOffsetY);
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
	public void CreateBackground(WorldBase world)
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
	
	public void DrawTilesAndWalls(Canvas canvas, WorldBase world)
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
	 * Attempts to free all bitmap memory
	 */
	private void Teardown()
	{
		mScale = -1;
		if(mCacheBitmap != null) mCacheBitmap.recycle();
		mCacheBitmap = null;
		if(mWorldBitmap != null) mWorldBitmap.recycle();
		mWorldBitmap = null;
		if(mTileA != null) mTileA.recycle();
		mTileA = null;
		if(mTileB != null) mTileB.recycle();
		mTileB = null;

		for (Animation animation : mCatAnimations.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mCatAnimations.clear();

		for (Animation animation : mFullArrowAnimations.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mFullArrowAnimations.clear();

		for (Animation animation : mGestureArrowAnimations.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mGestureArrowAnimations.clear();

		for (Animation animation : mGoldMouseAnimations.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mGoldMouseAnimations.clear();

		for (Animation animation : mHalfArrowAnimations.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mHalfArrowAnimations.clear();

		for (Animation animation : mMouseAnimations.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mMouseAnimations.clear();

		for (Animation animation : mMPFullArrowAnimations0.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mMPFullArrowAnimations0.clear();

		for (Animation animation : mMPFullArrowAnimations1.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mMPFullArrowAnimations1.clear();

		for (Animation animation : mMPFullArrowAnimations2.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mMPFullArrowAnimations2.clear();

		for (Animation animation : mMPFullArrowAnimations3.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mMPFullArrowAnimations3.clear();

		for (Animation animation : mMPHalfArrowAnimations0.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mMPHalfArrowAnimations0.clear();

		for (Animation animation : mMPHalfArrowAnimations1.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mMPHalfArrowAnimations1.clear();

		for (Animation animation : mMPHalfArrowAnimations2.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mMPHalfArrowAnimations2.clear();

		for (Animation animation : mMPHalfArrowAnimations3.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mMPHalfArrowAnimations3.clear();

		for (Animation animation : mSpecialMouseAnimations.values()) {
			if(animation != null)
				animation.Teardown();
		}
		mSpecialMouseAnimations.clear();

		for (Animation animation : mMPCursorAnimations) {
			if(animation != null)
				animation.Teardown();
		}
		mMPCursorAnimations = new Animation[mMPCursorAnimations.length];

		for (Animation animation : mMPRocketAnimation) {
			if(animation != null)
				animation.Teardown();
		}
		mMPRocketAnimation = new Animation[mMPRocketAnimation.length];

		if(mCatDeathAnimation != null) mCatDeathAnimation.Teardown();
		mCatDeathAnimation = null;

		if(mCursorAnimation != null) mCursorAnimation.Teardown();
		mCursorAnimation = null;

		if(mGoldMouseDeathAnimation != null) mGoldMouseDeathAnimation.Teardown();
		mGoldMouseDeathAnimation = null;

		if(mGoldMouseRescueAnimation != null) mGoldMouseRescueAnimation.Teardown();
		mGoldMouseRescueAnimation = null;

		if(mHoleAnimation != null) mHoleAnimation.Teardown();
		mHoleAnimation = null;

		if(mMouseDeathAnimation != null) mMouseDeathAnimation.Teardown();
		mMouseDeathAnimation = null;

		if(mMouseRescueAnimation != null) mMouseRescueAnimation.Teardown();
		mMouseRescueAnimation = null;

		if(mNorthWall != null) mNorthWall.Teardown();
		mNorthWall = null;

		if(mWestWall!= null) mWestWall.Teardown();
		mWestWall = null;

		if(mRingAnimation != null) mRingAnimation.Teardown();
		mRingAnimation = null;

		if(mRocketAnimation != null) mRocketAnimation.Teardown();
		mRocketAnimation = null;

		if(mSpawnerAnimation != null) mSpawnerAnimation.Teardown();
		mSpawnerAnimation = null;

		if(mSpecialMouseDeathAnimation != null) mSpecialMouseDeathAnimation.Teardown();
		mSpecialMouseDeathAnimation = null;

		if(mSpecialMouseRescueAnimation != null) mSpecialMouseRescueAnimation.Teardown();
		mSpecialMouseRescueAnimation = null;

		if(mTickAnimation != null) mTickAnimation.Teardown();
		mTickAnimation= null;
	}
	
	/**
	 * Loads any unloaded textures
	 * @param context The context to obtain animations from
	 */
	public void Setup(Context context, int gridSize, SkinProgress skin, boolean loadMP)
	{
		mGridSize = gridSize;
		float referenceGridSize = (float)context.getResources().getInteger(uk.danishcake.shokorocket.R.integer.grid_size);
		float scale = ((float)mGridSize) / referenceGridSize;

		//Load unloaded animations
		if(context != null && (scale != mScale || mMPLoaded != loadMP))
		{
			Log.d("GameDrawer.Setup", "Reloading due to scale change/mp change");
			Log.d("GameDrawer.Setup", "Scale from " + Float.toString(mScale) + " to " + Float.toString(scale));
			Teardown();
			try
			{
				if(!loadMP)
				{
					Map<String, Animation> mouse_animations = Animation.GetAnimations(context, skin.getAnimation("mouse"), scale);
					mMouseAnimations.put(Direction.North, mouse_animations.get("North"));
					mMouseAnimations.put(Direction.South, mouse_animations.get("South"));
					mMouseAnimations.put(Direction.East, mouse_animations.get("East"));
					mMouseAnimations.put(Direction.West, mouse_animations.get("West"));
					mMouseAnimations.put(Direction.Invalid, mouse_animations.get("Stopped"));
					mMouseDeathAnimation = mouse_animations.get("Death");
					mMouseRescueAnimation = mouse_animations.get("Rescue");
				}
				Map<String, Animation> cat_animations = Animation.GetAnimations(context, skin.getAnimation("kapukapu"), scale);
				mCatAnimations.put(Direction.North, cat_animations.get("North"));
				mCatAnimations.put(Direction.South, cat_animations.get("South"));
				mCatAnimations.put(Direction.East, cat_animations.get("East"));
				mCatAnimations.put(Direction.West, cat_animations.get("West"));
				mCatAnimations.put(Direction.Invalid, cat_animations.get("Stopped"));
				mCatDeathAnimation = cat_animations.get("Death");

				
				Map<String, Animation> arrow_animations = Animation.GetAnimations(context, skin.getAnimation("arrows"), scale);
				mFullArrowAnimations.put(Direction.North, arrow_animations.get("North"));
				mFullArrowAnimations.put(Direction.South, arrow_animations.get("South"));
				mFullArrowAnimations.put(Direction.East, arrow_animations.get("East"));
				mFullArrowAnimations.put(Direction.West, arrow_animations.get("West"));
				mFullArrowAnimations.put(Direction.Invalid, arrow_animations.get("Stopped"));
				
				Map<String, Animation> g_arrow_animations = Animation.GetAnimations(context, skin.getAnimation("gesture_arrows"), scale);
				mGestureArrowAnimations.put(Direction.North, g_arrow_animations.get("North"));
				mGestureArrowAnimations.put(Direction.South, g_arrow_animations.get("South"));
				mGestureArrowAnimations.put(Direction.East, g_arrow_animations.get("East"));
				mGestureArrowAnimations.put(Direction.West, g_arrow_animations.get("West"));
				mGestureArrowAnimations.put(Direction.Invalid, g_arrow_animations.get("Stopped"));
				
				Map<String, Animation> half_arrow_animations = Animation.GetAnimations(context, skin.getAnimation("halfarrows"), scale);
				mHalfArrowAnimations.put(Direction.North, half_arrow_animations.get("North"));
				mHalfArrowAnimations.put(Direction.South, half_arrow_animations.get("South"));
				mHalfArrowAnimations.put(Direction.East, half_arrow_animations.get("East"));
				mHalfArrowAnimations.put(Direction.West, half_arrow_animations.get("West"));
				mHalfArrowAnimations.put(Direction.Invalid, half_arrow_animations.get("Stopped"));
				
				Map<String, Animation> rocket_animations = Animation.GetAnimations(context, skin.getAnimation("rocket"), scale);
				mRocketAnimation = rocket_animations.get("Normal");
				
				Map<String, Animation> hole_animations = Animation.GetAnimations(context, skin.getAnimation("hole"), scale);
				mHoleAnimation = hole_animations.get("All");				
				
				Map<String, Animation> ring_animations = Animation.GetAnimations(context, skin.getAnimation("ring"), scale);
				mRingAnimation = ring_animations.get("All");
				
				Map<String, Animation> wall_animations = Animation.GetAnimations(context, skin.getAnimation("walls"), scale);
				mNorthWall = wall_animations.get("Horizontal");
				mWestWall = wall_animations.get("Vertical");
				
				Map<String, Animation> tile_animations = Animation.GetAnimations(context, skin.getAnimation("tiles"), scale);
				mTileA = tile_animations.get("TileA").getCurrentFrame();
				mTileB = tile_animations.get("TileB").getCurrentFrame();
				
				Map<String, Animation> cursor_animations = Animation.GetAnimations(context, skin.getAnimation("cursor"), scale); 
				mCursorAnimation = cursor_animations.get("Normal");
				mTransCursorAnimation = cursor_animations.get("Faded");

				Map<String, Animation> tick_animations = Animation.GetAnimations(context, skin.getAnimation("tick"), scale); 
				mTickAnimation = tick_animations.get("All");
				
				if(loadMP)
				{
					//Multiplayer 
					Map<String, Animation> mp_rocket_animations = Animation.GetAnimations(context, skin.getAnimation("mp_rockets"), scale);
					mMPRocketAnimation[0] = mp_rocket_animations.get("Normal0");
					mMPRocketAnimation[1] = mp_rocket_animations.get("Normal1");
					mMPRocketAnimation[2] = mp_rocket_animations.get("Normal2");
					mMPRocketAnimation[3] = mp_rocket_animations.get("Normal3");
					
					Map<String, Animation> mp_arrow_animations = Animation.GetAnimations(context, skin.getAnimation("mp_arrows"), scale);
					mMPFullArrowAnimations0.put(Direction.North, mp_arrow_animations.get("North0"));
					mMPFullArrowAnimations0.put(Direction.South, mp_arrow_animations.get("South0"));
					mMPFullArrowAnimations0.put(Direction.East, mp_arrow_animations.get("East0"));
					mMPFullArrowAnimations0.put(Direction.West, mp_arrow_animations.get("West0"));
					mMPFullArrowAnimations0.put(Direction.Invalid, mp_arrow_animations.get("Stopped"));
					
					mMPFullArrowAnimations2.put(Direction.North, mp_arrow_animations.get("North2"));
					mMPFullArrowAnimations2.put(Direction.South, mp_arrow_animations.get("South2"));
					mMPFullArrowAnimations2.put(Direction.East, mp_arrow_animations.get("East2"));
					mMPFullArrowAnimations2.put(Direction.West, mp_arrow_animations.get("West2"));
					mMPFullArrowAnimations2.put(Direction.Invalid, mp_arrow_animations.get("Stopped"));
					
					mMPFullArrowAnimations3.put(Direction.North, mp_arrow_animations.get("North3"));
					mMPFullArrowAnimations3.put(Direction.South, mp_arrow_animations.get("South3"));
					mMPFullArrowAnimations3.put(Direction.East, mp_arrow_animations.get("East3"));
					mMPFullArrowAnimations3.put(Direction.West, mp_arrow_animations.get("West3"));
					mMPFullArrowAnimations3.put(Direction.Invalid, mp_arrow_animations.get("Stopped"));
					
					mMPHalfArrowAnimations0.put(Direction.North, mp_arrow_animations.get("HalfNorth0"));
					mMPHalfArrowAnimations0.put(Direction.South, mp_arrow_animations.get("HalfSouth0"));
					mMPHalfArrowAnimations0.put(Direction.East, mp_arrow_animations.get("HalfEast0"));
					mMPHalfArrowAnimations0.put(Direction.West, mp_arrow_animations.get("HalfWest0"));
					mMPHalfArrowAnimations0.put(Direction.Invalid, mp_arrow_animations.get("Stopped"));
					
					mMPHalfArrowAnimations2.put(Direction.North, mp_arrow_animations.get("HalfNorth2"));
					mMPHalfArrowAnimations2.put(Direction.South, mp_arrow_animations.get("HalfSouth2"));
					mMPHalfArrowAnimations2.put(Direction.East, mp_arrow_animations.get("HalfEast2"));
					mMPHalfArrowAnimations2.put(Direction.West, mp_arrow_animations.get("HalfWest2"));
					mMPHalfArrowAnimations2.put(Direction.Invalid, mp_arrow_animations.get("Stopped"));
					
					mMPHalfArrowAnimations3.put(Direction.North, mp_arrow_animations.get("HalfNorth3"));
					mMPHalfArrowAnimations3.put(Direction.South, mp_arrow_animations.get("HalfSouth3"));
					mMPHalfArrowAnimations3.put(Direction.East, mp_arrow_animations.get("HalfEast3"));
					mMPHalfArrowAnimations3.put(Direction.West, mp_arrow_animations.get("HalfWest3"));
					mMPHalfArrowAnimations3.put(Direction.Invalid, mp_arrow_animations.get("Stopped"));
					
					Map<String, Animation> mp_cursor_animations = Animation.GetAnimations(context, skin.getAnimation("mp_cursors"), scale);
					mMPCursorAnimations[0] = mp_cursor_animations.get("Normal0");
					mMPCursorAnimations[1] = mp_cursor_animations.get("Normal1");
					mMPCursorAnimations[2] = mp_cursor_animations.get("Normal2");
					mMPCursorAnimations[3] = mp_cursor_animations.get("Normal3");
					
					Map<String, Animation> spawner_animations = Animation.GetAnimations(context, skin.getAnimation("spawner"), scale);
					mSpawnerAnimation = spawner_animations.get("Normal");
					
					Map<String, Animation> mouse_animations = Animation.GetAnimations(context, skin.getAnimation("plain_mouse"), scale);
					mMouseAnimations.put(Direction.North, mouse_animations.get("North"));
					mMouseAnimations.put(Direction.South, mouse_animations.get("South"));
					mMouseAnimations.put(Direction.East, mouse_animations.get("East"));
					mMouseAnimations.put(Direction.West, mouse_animations.get("West"));
					mMouseAnimations.put(Direction.Invalid, mouse_animations.get("Stopped"));
					mMouseDeathAnimation = mouse_animations.get("Death");
					mMouseRescueAnimation = mouse_animations.get("Rescue");

					Map<String, Animation> special_mouse_animations = Animation.GetAnimations(context, skin.getAnimation("special_mouse"), scale);
					mSpecialMouseAnimations.put(Direction.North, special_mouse_animations.get("North"));
					mSpecialMouseAnimations.put(Direction.South, special_mouse_animations.get("South"));
					mSpecialMouseAnimations.put(Direction.East, special_mouse_animations.get("East"));
					mSpecialMouseAnimations.put(Direction.West, special_mouse_animations.get("West"));
					mSpecialMouseAnimations.put(Direction.Invalid, special_mouse_animations.get("Stopped"));
					mSpecialMouseDeathAnimation = special_mouse_animations.get("Death");
					mSpecialMouseRescueAnimation = special_mouse_animations.get("Rescue");

					Map<String, Animation> gold_mouse_animations = Animation.GetAnimations(context, skin.getAnimation("gold_mouse"), scale);
					mGoldMouseAnimations.put(Direction.North, gold_mouse_animations.get("North"));
					mGoldMouseAnimations.put(Direction.South, gold_mouse_animations.get("South"));
					mGoldMouseAnimations.put(Direction.East, gold_mouse_animations.get("East"));
					mGoldMouseAnimations.put(Direction.West, gold_mouse_animations.get("West"));
					mGoldMouseAnimations.put(Direction.Invalid, gold_mouse_animations.get("Stopped"));
					mGoldMouseDeathAnimation = gold_mouse_animations.get("Death");
					mGoldMouseRescueAnimation = gold_mouse_animations.get("Rescue");
				}

				mScale = scale;
				mMPLoaded = loadMP;
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
	public void DrawMP(Canvas canvas, MPWorld world)
	{
		int sprite_count = 0;
		for(int i = 0; i < mRenderItems.size(); i++)
		{
			mRenderItems.get(i).animation = null;
		}
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
				{
					int player_id = world.getPlayer(x, y); // Limit to 0-3
					if(player_id < 0 || player_id > 3) player_id = 0;
					mMPRocketAnimation[player_id].DrawCurrentFrame(canvas, drawX, drawY);
				}
					break;
				case NorthSpawner:
				case SouthSpawner:
				case EastSpawner:
				case WestSpawner:
					mSpawnerAnimation.DrawCurrentFrame(canvas, drawX, drawY);
					break;
				case NorthArrow:
				case SouthArrow:
				case EastArrow:
				case WestArrow:
				{
					int player_id = world.getPlayer(x, y); // Limit to 0-3
					switch(player_id)
					{
					case 0:
						mMPFullArrowAnimations0.get(square.getArrowDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
						break;
					case 1:
						mMPFullArrowAnimations1.get(square.getArrowDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
						break;
					case 2:
						mMPFullArrowAnimations2.get(square.getArrowDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
						break;
					case 3:
					default:
						mMPFullArrowAnimations3.get(square.getArrowDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
						break;
					}
				}
					break;
				case NorthHalfArrow:
				case SouthHalfArrow:
				case EastHalfArrow:
				case WestHalfArrow:
				{
					int player_id = world.getPlayer(x, y); // Limit to 0-3
					switch(player_id)
					{
					case 0:
						mMPHalfArrowAnimations0.get(square.getArrowDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
						break;
					case 1:
						mMPHalfArrowAnimations1.get(square.getArrowDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
						break;
					case 2:
						mMPHalfArrowAnimations2.get(square.getArrowDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
						break;
					case 3:
					default:
						mMPHalfArrowAnimations3.get(square.getArrowDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
						break;
					}
				}
					break;
				}
			}
		}
		
		
		//Draw cats & mice
		ArrayList<Walker> mice = world.getLiveMice();
		ArrayList<Walker> cats = world.getLiveCats();
		for (Walker walker : mice) {
			EnumMap<Direction, Animation> mouse_type;
			switch(walker.getWalkerType()){
				case Mouse:
				default:
					mouse_type = mMouseAnimations;
					break;
				case MouseGold:
					mouse_type = mGoldMouseAnimations;
					break;
				case MouseSpecial:
					mouse_type = mSpecialMouseAnimations;
					break;
			}
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
			Animation animation = mouse_type.get(walker.getDirection());
			if(animation != null)
			{
				if(++sprite_count > mRenderItems.size())
				{
					mRenderItems.add(new RenderItem());
				}
				RenderItem ri = mRenderItems.get(sprite_count-1);
				ri.x = x;
				ri.y = y;
				ri.animation = animation;
			}
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
			{
				if(++sprite_count > mRenderItems.size())
				{
					mRenderItems.add(new RenderItem());
				}
				RenderItem ri = mRenderItems.get(sprite_count-1);
				ri.x = x;
				ri.y = y;
				ri.animation = animation;
			}
		}
		//Sort cats & mice by depth
		java.util.Collections.sort(mRenderItems);
		for(int i = 0; i < sprite_count; i++)
		{
			RenderItem ri = mRenderItems.get(i);
			ri.animation.DrawCurrentFrame(canvas, ri.x, ri.y);
		}
		for(Walker walker : world.getDeadMice())
		{
			Animation mouse_anim;
			switch(walker.getWalkerType())
			{
			case Mouse:
			default:
				mouse_anim = mMouseDeathAnimation;
				break;
			case MouseGold:
				mouse_anim = mGoldMouseDeathAnimation;
				break;
			case MouseSpecial:
				mouse_anim = mSpecialMouseDeathAnimation;
				break;
			}
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
				mouse_anim.DrawFrameAtTime(canvas, x, y, death_time / 5);
			}
		}
		for(Walker walker : world.getRescuedMice())
		{
			Animation mouse_anim;
			switch(walker.getWalkerType())
			{
			case Mouse:
			default:
				mouse_anim = mMouseRescueAnimation;
				break;
			case MouseGold:
				mouse_anim = mGoldMouseRescueAnimation;
				break;
			case MouseSpecial:
				mouse_anim = mSpecialMouseRescueAnimation;
				break;
			}

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
				mouse_anim.DrawFrameAtTime(canvas, x, y, death_time / 5);
			}
		}
		for(Walker walker : world.getDeadCats())
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
			if(walker.getWalkerState() == WalkerState.Rescued)
			{
				mRingAnimation.DrawCurrentFrame(canvas, x, y);
			}
			
			int death_time = walker.getDeathTime();
			if(death_time < 5000)
			{
				mCatDeathAnimation.DrawFrameAtTime(canvas, x, y, death_time / 5);
			}
		}
	}
	
	public void DrawSP(Canvas canvas, SPWorld world)
	{
		int sprite_count = 0;
		for(int i = 0; i < mRenderItems.size(); i++)
		{
			mRenderItems.get(i).animation = null;
		}
		if(mWorldBitmap != null)
			canvas.drawBitmap(mWorldBitmap, mDrawOffsetX, mDrawOffsetY, null);
		else
			DrawTilesAndWalls(canvas, world);
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
					mFullArrowAnimations.get(square.getArrowDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
					break;
				case NorthHalfArrow:
				case SouthHalfArrow:
				case EastHalfArrow:
				case WestHalfArrow:
					mHalfArrowAnimations.get(square.getArrowDirectionality()).DrawCurrentFrame(canvas, drawX, drawY);
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
			{
				if(++sprite_count > mRenderItems.size())
				{
					mRenderItems.add(new RenderItem());
				}
				RenderItem ri = mRenderItems.get(sprite_count-1);
				ri.x = x;
				ri.y = y;
				ri.animation = animation;
			}
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
			{
				if(++sprite_count > mRenderItems.size())
				{
					mRenderItems.add(new RenderItem());
				}
				RenderItem ri = mRenderItems.get(sprite_count-1);
				ri.x = x;
				ri.y = y;
				ri.animation = animation;
			}
		}
		//Sort cats & mice by depth
		java.util.Collections.sort(mRenderItems);
		for(int i = 0; i < sprite_count; i++)
		{
			RenderItem ri = mRenderItems.get(i);
			ri.animation.DrawCurrentFrame(canvas, ri.x, ri.y);
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
			if(walker.getWalkerState() == WalkerState.Rescued)
			{
				mRingAnimation.DrawCurrentFrame(canvas, x, y);
			}
			
			int death_time = walker.getDeathTime();
			if(death_time < 5000)
			{
				mCatDeathAnimation.DrawFrameAtTime(canvas, x, y, death_time / 5);
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
		for (Animation animation : mGoldMouseAnimations.values()) {
			animation.Tick(timespan);
		}
		for (Animation animation : mSpecialMouseAnimations.values()) {
			animation.Tick(timespan);
		}
		for (Animation animation : mCatAnimations.values()) {
			animation.Tick(timespan);
		}
	}
	
	public void CreateCacheBitmap(SPWorld world)
	{
		int old_drawoffset_x = mDrawOffsetX;
		int old_drawoffset_y = mDrawOffsetY;
		mDrawOffsetX = 0;
		mDrawOffsetY = 0;

		if(mCacheBitmap != null)
			mCacheBitmap.recycle();
		mCacheBitmap = Bitmap.createBitmap(world.getWidth() * mGridSize, world.getHeight() * mGridSize, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(mCacheBitmap);
		DrawSP(canvas, world);

		mDrawOffsetX = old_drawoffset_x;
		mDrawOffsetY = old_drawoffset_y;
	}
   
	public void DrawCacheBitmap(Canvas canvas, float angle, float scale)
	{
		/* Rotate the cached bitmap by the specified angle and draw */
		Matrix trans = new Matrix();

		trans.postRotate(angle, mCacheBitmap.getWidth()/2, mCacheBitmap.getHeight()/2);
		trans.postScale(scale, scale, mCacheBitmap.getWidth()/2, mCacheBitmap.getHeight()/2);
		trans.postTranslate(mDrawOffsetX, mDrawOffsetY);
		canvas.drawBitmap(mCacheBitmap, trans, null);
	}
}
