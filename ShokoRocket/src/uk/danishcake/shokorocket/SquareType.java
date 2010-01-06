package uk.danishcake.shokorocket;

public enum SquareType {
	Empty, Rocket, Hole, NorthArrow, SouthArrow, EastArrow, WestArrow, NorthHalfArrow, SouthHalfArrow, WestHalfArrow, EastHalfArrow;
	
	public Direction ToDirection() {
		switch(this)
		{
		case NorthArrow:
		case NorthHalfArrow:
			return Direction.North;
		case SouthArrow:
		case SouthHalfArrow:
			return Direction.South;
		case EastArrow:
		case EastHalfArrow:
			return Direction.East;
		case WestArrow:
		case WestHalfArrow:
			return Direction.West;
		default:
			return Direction.Invalid;
		}
	}
	
	public SquareType Diminish()
	{
		switch(this)
		{
		case NorthArrow:
			return SquareType.NorthHalfArrow;
		case SouthArrow:
			return SquareType.SouthHalfArrow;
		case EastArrow:
			return SquareType.EastHalfArrow;
		case WestArrow:
			return SquareType.WestHalfArrow;
		case NorthHalfArrow:
			return SquareType.Empty;
		case SouthHalfArrow:
			return SquareType.Empty;
		case EastHalfArrow:
			return SquareType.Empty;
		case WestHalfArrow:
			return SquareType.Empty;
		default:
			return this;
		}		
	}
}
