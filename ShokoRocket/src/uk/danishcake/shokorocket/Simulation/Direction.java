package uk.danishcake.shokorocket.simulation;



public enum Direction {
	Invalid,
	North,
	South,
	East,
	West;
	
	public SquareType toArrow() {
		switch(this)
		{
		case North:
			return SquareType.NorthArrow;
		case South:
			return SquareType.SouthArrow;
		case West:
			return SquareType.WestArrow;
		case East:
			return SquareType.EastArrow;
		default:
			return SquareType.Empty;		
		}
	}
	
	public SquareType toSpawner() {
		switch(this)
		{
		case North:
			return SquareType.NorthSpawner;
		case South:
			return SquareType.SouthSpawner;
		case West:
			return SquareType.WestSpawner;
		case East:
			return SquareType.EastArrow;
		default:
			return SquareType.Empty;		
		}
	}
	
	public Direction RotateLeft() {
		switch(this)
		{
		case North:
			return West;
		case South:
			return East;
		case West:
			return South;
		case East:
			return North;
		default:
			return Invalid;
		}
	}
	
	public Direction RotateRight() {
		switch(this)
		{
		case North:
			return East;
		case South:
			return West;
		case West:
			return North;
		case East:
			return South;
		default:
			return Invalid;
		}
	}
}
