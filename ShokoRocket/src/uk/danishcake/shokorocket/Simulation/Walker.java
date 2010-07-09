package uk.danishcake.shokorocket.simulation;

import java.security.InvalidParameterException;


public class Walker {
	public enum WalkerType
	{
		Unknown, Cat, Mouse
	}
	public enum WalkerState
	{
		Alive, Dead, Rescued
	}
	
	private int mX = 0;
	private int mY = 0;
	private int mStartingX = 0;
	private int mStartingY = 0;
	private int mFractional = 0; //Resets at FractionReset - 1000ms at speed MouseSpeed
	private int mDeathTime = 0;
	public static final int FractionReset = 3000000;
	public static final int MouseSpeed = 3000;
	public static final int CatSpeed = 2000;
	private int mSpeed = MouseSpeed;
	private Direction mDirection = Direction.North;
	private Direction mStartingDirection = Direction.North;
	private World mWorld = null;
	private WalkerType mWalkerType = WalkerType.Unknown;
	private WalkerState mWalkerState = WalkerState.Alive;
	private boolean mFirstAdvance = true;
	
	/* Gets the position of the walker
	 * @return The position of the walker
	 */
	public Vector2i getPosition() {
		return new Vector2i(mX, mY);
	}
	/* Sets the position of the walker
	 * @param position The position
	 */
	public void setPosition(Vector2i position) {
		mX = position.x;
		mY = position.y;
		mStartingX = mX;
		mStartingY = mY;
	}
	
	/* Gets the direction faced by the walker
	 * @return direction faced by the walker
	 */
	public Direction getDirection() {
		return mDirection;
	}
	/* Sets the direction faced by the walker
	 * @param direction direction to be faced by the walker
	 */
	public void setDirection(Direction direction) {
		mDirection = direction;
		mStartingDirection = mDirection;
	}
	
	/* Gets the fraction between 0 and FractionReset of the walkers progress to the next square
	 * @return 0-FractionReset-1 integer representing progress to next square
	 */
	public int getFraction() {
		return mFractional;
	}
	/* Sets the speed of the walker. Defaults to 3,000
	 * @param speed the number of units to move in units per millisecond. Fraction will be incremented by timespan(ms) * speed
	 */	
	public void setSpeed(int speed) {
		mSpeed = speed;
	}
	/* Gets the speed in units per millisecond. Once unit is 1/3,000,000th of a grid square
	 * @return the speed of the walker
	 */	
	public int getSpeed() {
		return mSpeed;
	}
	
	/* setWalkerType
	 * Sets the type of walker, called by World from addCat and addMouse
	 */
	public void setWalkerType(WalkerType walker_type) {
		mWalkerType = walker_type;
	}
	
	/* getWalkerState
	 * Gets the state of the walker - whether alive, dead or rescued
	 */
	public WalkerState getWalkerState() {
		return mWalkerState;
	}
	
	/* Sets the world in which the walker will turn and interact
	 * @param world the world the walker will be active in
	 */
	public void setWorld(World world) {
		if(world.getWidth() <= mX || world.getHeight() <= mY)
			throw new InvalidParameterException("Unable to set world as walker outside range. Walker at (" + Integer.toString(mX) + "," + Integer.toString(mY) + "), world size is (" + Integer.toString(world.getWidth()) + "," + Integer.toString(world.getHeight()) + ")");
		mWorld = world;
	}
	/* Gets the world the walker is moving in
	 * @return the world the walker is moving through
	 */
	public World getWorld() {
		return mWorld;
	}
	/**
	 * Gets the time spent dead
	 * @return milliseconds spent dead
	 */
	public int getDeathTime() {
		return mDeathTime;
	}
	
	/* Advances time for the walker.
	 * @param timespan the timespan to advance for in milliseconds
	 */
	public void Advance(int timespan) {
		if(mFirstAdvance)
		{
			reachNewGridSquare();
			mFirstAdvance = false;
		}
		mFractional += mSpeed * timespan;
		while(mFractional >= FractionReset)
		{
			mFractional -= FractionReset;
			switch(mDirection)
			{
			case North:
				mY--;
				break;
			case South:
				mY++;
				break;
			case East:
				mX++;
				break;
			case West:
				mX--;
				break;
			}
			wrapAround();
			reachNewGridSquare();
		}
	}
	
	public void DeathTick(int timespan)
	{
		mDeathTime += timespan;
	}
	
	/* Reset
	 * Restores starting position and revives the dead or rescued
	 */
	public void Reset()	{
		mFractional = 0;
		mX = mStartingX;
		mY = mStartingY;
		mDirection = mStartingDirection;
		mWalkerState = WalkerState.Alive;
		mFirstAdvance = true;
		mDeathTime = 0;
	}
	
	/* wrapAround
	 * If the walker has walked off the edge of a world then reposition at the other side.
	 */
	private void wrapAround() {
		if(mWorld != null)
		{
			if(mY == -1)
				mY += mWorld.getHeight();
			if(mX == -1)
				mX += mWorld.getWidth();
			if(mY == mWorld.getHeight())
				mY = 0;
			if(mX == mWorld.getWidth())
				mX = 0;
		}
	}
	
	/* Performs actions associated with reaching a new grid square.
	 * This may include turns, falling into holes, reaching rockets etc
	 */
	private void reachNewGridSquare() {
		if(mWorld != null)
		{
			//First interact with special squares (arrow, holes & rockets)
			SquareType square = mWorld.getSpecialSquare(mX, mY);
			//Holes
			if(square == SquareType.Hole)
			{
				mWalkerState = WalkerState.Dead;
			}
			if(square == SquareType.Rocket)
			{
				mWalkerState = WalkerState.Rescued;
			}
			//Arrows
			Direction arrow_direction = square.ToDirection(); 
			if(arrow_direction != Direction.Invalid)
			{
				if(arrow_direction == Turns.TurnAround(mDirection) && mWalkerType == WalkerType.Cat)
				{
					SquareType reduced = square.Diminish();
					mWorld.setSpecialSquare(mX, mY, reduced);
				}
				mDirection = arrow_direction;
			}
			//Now interact with walls
			if(!mWorld.getDirection(mX, mY, mDirection))
			{
				//mDirection = mDirection; //Carry straight on!
			} 
			else if(mWorld.getDirection(mX, mY, mDirection) && 
					!mWorld.getDirection(mX, mY, Turns.TurnRight(mDirection)))
				mDirection = Turns.TurnRight(mDirection);
			else if(mWorld.getDirection(mX, mY, mDirection) &&
					mWorld.getDirection(mX, mY, Turns.TurnRight(mDirection)) &&
					!mWorld.getDirection(mX, mY, Turns.TurnLeft(mDirection)))
				mDirection = Turns.TurnLeft(mDirection);
			else if(!mWorld.getDirection(mX, mY, Turns.TurnAround(mDirection)))
				mDirection = Turns.TurnAround(mDirection);
			else
				mDirection = Direction.Invalid;		
		}
	}
}
