package uk.danishcake.shokorocket;

public enum SquareType {
	Empty, Rocket, Hole, NorthArrow, SouthArrow, EastArrow, WestArrow;
	
	public Direction ToDirection() {
		switch(this)
		{
		case NorthArrow:
			return Direction.North;
		case SouthArrow:
			return Direction.South;
		case EastArrow:
			return Direction.East;
		case WestArrow:
			return Direction.West;
		default:
			return Direction.Invalid;
		}
		
	}
}
