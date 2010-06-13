package uk.danishcake.shokorocket.test;

import junit.framework.TestCase;
import uk.danishcake.shokorocket.Simulation.Direction;
import uk.danishcake.shokorocket.Simulation.Vector2i;
import uk.danishcake.shokorocket.Simulation.Walker;
import uk.danishcake.shokorocket.Simulation.World;
import uk.danishcake.shokorocket.Simulation.Walker.WalkerState;

public class WalkerTests extends TestCase {

	public void testWalkerPosition()
	{
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
		walker.setSpeed(1500);
		walker.Advance(1000);
		assertEquals(1500 * 1000, walker.getFraction());
		
		walker.Advance(1000);
		assertEquals(0, walker.getFraction());
		
		walker.Advance(3000);
		assertEquals(1500 * 1000, walker.getFraction());
		
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
	
	public void testWalkerWraps()
	{
		//Wrapping requires a world
		World world = new World();
		Walker walker = new Walker();
		walker.setPosition(new Vector2i(3,1));
		world.setNorth(3, 0, false);
		
		world.addMouse(walker);
		
		world.Tick(3000);
		
		assertEquals(3, walker.getPosition().x);
		assertEquals(7, walker.getPosition().y);
	}
	
	public void testWalkerReset()
	{
		World world = new World();
		Walker walker = new Walker();
		walker.setPosition(new Vector2i(0,5));
		walker.setDirection(Direction.South);
		world.addMouse(walker);
	
		world.Tick(4000);
		assertEquals(1, walker.getPosition().x);
		assertEquals(8, walker.getPosition().y);
		assertEquals(Direction.East, walker.getDirection());
		
		walker.Reset();
		assertEquals(0, walker.getPosition().x);
		assertEquals(5, walker.getPosition().y);
		assertEquals(Direction.South, walker.getDirection());
		
		world.setHole(2, 8, true);
		world.Tick(5000);
		
		assertEquals(WalkerState.Dead, walker.getWalkerState());
		
		walker.Reset();
		assertEquals(WalkerState.Alive, walker.getWalkerState());
		
		
	}
}
