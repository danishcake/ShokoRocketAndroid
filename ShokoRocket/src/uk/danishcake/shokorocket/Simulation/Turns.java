package uk.danishcake.shokorocket.Simulation;



/* 
 * Collection of methods for turning
 */
public abstract class Turns {
	/* Rotates a direction left
	 * @param direction initial direction
	 * @return direction turned direction
	 */
	public static Direction TurnLeft(Direction direction) {
		switch(direction)
		{
		case North:
			return Direction.West;
		case West:
			return Direction.South;
		case South:
			return Direction.East;
		case East:
			return Direction.North;
		}
		return Direction.Invalid;
	}
	
	/* Rotates a direction right
	 * @param direction initial direction
	 * @return direction turned direction
	 */
	public static Direction TurnRight(Direction direction) {
		switch(direction)
		{
		case North:
			return Direction.East;
		case West:
			return Direction.North;
		case South:
			return Direction.West;
		case East:
			return Direction.South;
		}
		return Direction.Invalid;
	}
	
	/* Rotates a direction 180 degrees
	 * @param direction initial direction
	 * @return direction turned direction
	 */
	public static Direction TurnAround(Direction direction) {
		switch(direction)
		{
		case North:
			return Direction.South;
		case West:
			return Direction.East;
		case South:
			return Direction.North;
		case East:
			return Direction.West;
		}
		return Direction.Invalid;
	}	
}
