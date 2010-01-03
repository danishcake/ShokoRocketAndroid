package uk.danishcake.shokorocket.test;

import junit.framework.TestCase;
import uk.danishcake.shokorocket.Walker;
import uk.danishcake.shokorocket.Direction;
import uk.danishcake.shokorocket.Vector2i;

public class WalkerTests extends TestCase {

	public void testWalkerPosition()
	{
		/*
		//Check default position
		Walker walker = new Walker();
		assertEquals(walker.getPosition().GetX_i(), 0);
		assertEquals(walker.getPosition().GetY_i(), 0);
		
		//Check setter works
		walker.setPosition(new Vector2FP(5.0f, 5.0f));
		assertEquals(walker.getPosition().GetX_i(), 5);
		assertEquals(walker.getPosition().GetY_i(), 5);
		*/
		
		Walker walker = new Walker();
		assertEquals(walker.getPosition().x, 0);
		assertEquals(walker.getPosition().y, 0);
		
		//Check setter works
		walker.setPosition(new Vector2i(5,3));
		assertEquals(walker.getPosition().x, 5);
		assertEquals(walker.getPosition().y, 3);		
		
	}

	public void testWalkerDirection()
	{
		//Check defaults
		Walker walker = new Walker();
		assertEquals(walker.getDirection(), Direction.North);
		
		//Check setter works
		walker.setDirection(Direction.East);
		assertEquals(walker.getDirection(), Direction.East);
	}
	
	public void testWalkerAdvance()
	{
		//Check the fractional component is correct
		Walker walker = new Walker();
		walker.setPosition(new Vector2i(1,1));
		walker.setSpeed(500);
		walker.Advance(1000);
		assertEquals(500 * 1000, walker.getFraction());
		
		walker.Advance(1000);
		assertEquals(0, walker.getFraction());
		
		walker.Advance(3000);
		assertEquals(500 * 1000, walker.getFraction());
		
		//Check the coordinate changes sensibly
		walker.setPosition(new Vector2i(1,1));
		walker.setDirection(Direction.East);
		
		walker.Advance(2000);
		assertEquals(2, walker.getPosition().x);
		assertEquals(1, walker.getPosition().y);
		
		walker.setDirection(Direction.South);
		walker.Advance(2000);
		assertEquals(2, walker.getPosition().x);
		assertEquals(2, walker.getPosition().y);
		
		walker.setDirection(Direction.West);
		walker.Advance(2000);
		assertEquals(1, walker.getPosition().x);
		assertEquals(2, walker.getPosition().y);
		
		walker.setDirection(Direction.North);
		walker.Advance(2000);
		assertEquals(1, walker.getPosition().x);
		assertEquals(1, walker.getPosition().y);
	}
	
	void testWalkerWraps()
	{
		//Wrapping requires a world
		
	}
}
