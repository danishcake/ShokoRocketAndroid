package uk.danishcake.shokorocket.test;

import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.MPWorld;
import uk.danishcake.shokorocket.simulation.Walker.WalkerType;
import android.test.AndroidTestCase;

public class MPWorldTests extends AndroidTestCase {
	public void testMPWorldBasics()
	{
		//Really just checks MPWorld class exists
		MPWorld world = new MPWorld();
		assertEquals(12, world.getWidth());
		assertEquals(9, world.getHeight());
	}
	
	public void testMPWalkers()
	{
		MPWorld world = new MPWorld();
		assertEquals(0, world.getMice().size());
		
		world.addWalker(0, 0, Direction.East, WalkerType.Mouse);
		assertEquals(1, world.getMice().size());
		assertEquals(0, world.getCats().size());
		
		world.addWalker(0, 0, Direction.East, WalkerType.MouseGold);
		assertEquals(2, world.getMice().size());
		assertEquals(0, world.getCats().size());
		
		world.addWalker(0, 0, Direction.East, WalkerType.MouseSpecial);
		assertEquals(3, world.getMice().size());
		assertEquals(0, world.getCats().size());
		
		world.addWalker(0, 0, Direction.East, WalkerType.Cat);
		assertEquals(3, world.getMice().size());
		assertEquals(1, world.getCats().size());		
	}
	
	public void testMPWalkerCollisions()
	{
		MPWorld world  = new MPWorld();
		world.addWalker(0, 0, Direction.East, WalkerType.Mouse);
		world.addWalker(10, 0, Direction.West, WalkerType.Cat);
		assertEquals(1, world.getMice().size());
		assertEquals(1, world.getCats().size());
		world.Tick(10000);
		assertEquals(0, world.getMice().size());
		assertEquals(1, world.getCats().size());
	}
}
