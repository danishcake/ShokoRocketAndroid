package uk.danishcake.shokorocket.simulation;



public enum Direction {
	Invalid,
	North,
	South,
	East,
	West;
	
	public SquareType ToArrow() {
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
}
