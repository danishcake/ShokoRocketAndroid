package uk.danishcake.shokorocket;

import java.security.InvalidParameterException;

import uk.danishcake.shokorocket.Vector2i;
import uk.danishcake.shokorocket.Direction;
import uk.danishcake.shokorocket.World;

public class Walker {
	private int mX = 0;
	private int mY = 0;
	private int mFractional = 0; //Resets at 1,000,000 - 1000ms at speed 1000 
	private final int FractionReset = 1000000;
	private int mSpeed = 1000;
	private Direction mDirection = Direction.North;
	private World mWorld = null;
	
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
		this.mX = position.x;
		this.mY = position.y;
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
		this.mDirection = direction;
	}
	
	/* Gets the fraction between 0 and 1,000,000 of the walkers progress to the next square
	 * @return 0-1,000,000 integer representing progress to next square
	 */
	public int getFraction() {
		return mFractional;
	}
	/* Sets the speed of the walker. Defaults to 1,000
	 * @param speed the number of units to move in units per millisecond. Fraction will be incremented by timespan(ms) * speed
	 */	
	public void setSpeed(int speed) {
		mSpeed = speed;
	}
	/* Gets the speed in units per millisecond. Once unit is 1/1,000,000th of a grid square
	 * @return the speed of the walker
	 */	
	public int getSpeed() {
		return mSpeed;
	}
	
	/* Sets the world in which the walker will turn and interact
	 * @param world the world the walker will be active in
	 */
	public void setWorld(World world) {
		if(world.getWidth() <= mX || world.getHeight() <= mY)
			throw new InvalidParameterException("Unable to set world as walker outside range. Walker at (" + Integer.toString(mX) + "," + Integer.toString(mY) + "), world size is (" + Integer.toString(world.getWidth()) + "," + Integer.toString(world.getHeight()) + ")");
		mWorld = world;
		reachNewGridSquare();
	}
	/* Gets the world the walker is moving in
	 * @return the world the walker is moving through
	 */
	public World getWorld() {
		return mWorld;
	}
	
	/* Advances time for the walker.
	 * @param timespan the timespan to advance for in milliseconds
	 */
	public void Advance(int timespan) {
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
			reachNewGridSquare();
		}
	}
	
	/* Performs actions associated with reaching a new grid square.
	 * This may include turns, falling into holes, reaching rockets etc
	 */
	private void reachNewGridSquare() {
		if(mWorld != null)
		{
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
