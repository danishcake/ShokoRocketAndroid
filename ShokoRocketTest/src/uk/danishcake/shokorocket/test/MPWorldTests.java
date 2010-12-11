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
		assertEquals(0, world.getLiveMice().size());
		
		world.addWalker(0, 0, Direction.East, WalkerType.Mouse);
		assertEquals(1, world.getLiveMice().size());
		assertEquals(0, world.getLiveCats().size());
		
		world.addWalker(0, 0, Direction.East, WalkerType.MouseGold);
		assertEquals(2, world.getLiveMice().size());
		assertEquals(0, world.getLiveCats().size());
		
		world.addWalker(0, 0, Direction.East, WalkerType.MouseSpecial);
		assertEquals(3, world.getLiveMice().size());
		assertEquals(0, world.getLiveCats().size());
		
		world.addWalker(0, 0, Direction.East, WalkerType.Cat);
		assertEquals(3, world.getLiveMice().size());
		assertEquals(1, world.getLiveCats().size());		
	}
}
