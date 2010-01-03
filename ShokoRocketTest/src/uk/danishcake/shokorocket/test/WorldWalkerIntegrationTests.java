package uk.danishcake.shokorocket.test;

import java.security.InvalidParameterException;

import junit.framework.TestCase;
import uk.danishcake.shokorocket.Direction;
import uk.danishcake.shokorocket.World;
import uk.danishcake.shokorocket.Walker;
import uk.danishcake.shokorocket.Vector2i;

public class WorldWalkerIntegrationTests extends TestCase {
	
	public void testWalkerWorldBasics()
	{
		{//Check getter and setter of world work
			World world = new World();
			Walker walker = new Walker();
			walker.setWorld(world);
			
			assertEquals(world, walker.getWorld());
		}
		
		{//Check it can't be added to a world outside of bounds - should throw in this case
			World world = new World();
			Walker walker = new Walker();
			walker.setPosition(new Vector2i(15, 0));
			
			boolean throws_exception = false;
			try
			{
			walker.setWorld(world);
			} catch(InvalidParameterException exception)
			{
				throws_exception = true;
			}
			
			assertTrue(throws_exception);
		}
	}
	
	public void testWalkerTurns() 
	{
		Walker walker = new Walker();
		World world = new World();
		walker.setWorld(world);
		
		/* Test corner turn
		 * ========
		 * |    <--
		 * ||
		 * |v
		 */
		walker.setPosition(new Vector2i(2, 0));
		walker.setDirection(Direction.West);
		
		walker.Advance(2000);
		assertEquals(Direction.South, walker.getDirection());
		assertEquals(0, walker.getPosition().x);
		assertEquals(0, walker.getPosition().y);
		
		/* Test corner turn
		 * ========
		 * |    -->
		 * |^
		 * ||
		 */
		walker.setPosition(new Vector2i(0, 2));
		walker.setDirection(Direction.North);
		
		walker.Advance(2000);
		assertEquals(Direction.East, walker.getDirection());
		assertEquals(0, walker.getPosition().x);
		assertEquals(0, walker.getPosition().y);
		
		/* Test corner turn
		 * ||
		 * |v
		 * |   -->
		 * =======
		 */
		walker.setPosition(new Vector2i(0, 6));
		walker.setDirection(Direction.South);
		
		walker.Advance(2000);
		assertEquals(Direction.East, walker.getDirection());
		assertEquals(0, walker.getPosition().x);
		assertEquals(8, walker.getPosition().y);
		
		/* Test corner turn
		 * |^
		 * ||
		 * |   <--
		 * =======
		 */
		walker.setPosition(new Vector2i(2, 8));
		walker.setDirection(Direction.West);
		
		walker.Advance(2000);
		assertEquals(Direction.North, walker.getDirection());
		assertEquals(0, walker.getPosition().x);
		assertEquals(8, walker.getPosition().y);
		
		/* Test corner turn
		 * ========
		 * -->    |
		 *       ||
		 *       v|
		 */
		walker.setPosition(new Vector2i(9, 0));
		walker.setDirection(Direction.East);
		
		walker.Advance(2000);
		assertEquals(Direction.South, walker.getDirection());
		assertEquals(11, walker.getPosition().x);
		assertEquals(0, walker.getPosition().y);
		
		/* Test corner turn
		 * ========
		 * <--    |
		 *       ^|
		 *       ||
		 */
		walker.setPosition(new Vector2i(11, 2));
		walker.setDirection(Direction.North);
		
		walker.Advance(2000);
		assertEquals(Direction.West, walker.getDirection());
		assertEquals(11, walker.getPosition().x);
		assertEquals(0, walker.getPosition().y);
		
		/* Test corner turn
		 *      ||
		 *      v|
		 * <--   |
		 * =======
		 */
		walker.setPosition(new Vector2i(11, 6));
		walker.setDirection(Direction.South);
		
		walker.Advance(2000);
		assertEquals(Direction.West, walker.getDirection());
		assertEquals(11, walker.getPosition().x);
		assertEquals(8, walker.getPosition().y);
		
		/* Test corner turn
		 *      ^|
		 *      ||
		 * -->   |
		 * =======
		 */
		walker.setPosition(new Vector2i(9, 8));
		walker.setDirection(Direction.East);
		
		walker.Advance(2000);
		assertEquals(Direction.North, walker.getDirection());
		assertEquals(11, walker.getPosition().x);
		assertEquals(8, walker.getPosition().y);
		
		/* Test dead end turn
		 * || ^|
		 * |v ||
		 * |---|
		 * =====
		 */
		
		world.setEast(0, 8, true);
		walker.setPosition(new Vector2i(0, 6));
		walker.setDirection(Direction.South);
		
		walker.Advance(2000);
		assertEquals(Direction.North, walker.getDirection());
		assertEquals(0, walker.getPosition().x);
		assertEquals(8, walker.getPosition().y);		
		
		/* Test dead end turn
		 * =====
		 * |---|
		 * |^ ||
		 * || v|
		 */

		world.setEast(0, 0, true);
		walker.setPosition(new Vector2i(0, 2));
		walker.setDirection(Direction.North);
		
		walker.Advance(2000);
		assertEquals(Direction.South, walker.getDirection());
		assertEquals(0, walker.getPosition().x);
		assertEquals(0, walker.getPosition().y);
		
		/* Test dead end turn
		 * =====
		 * | <--
		 * | -->
		 * =====
		 */
		
		world.setSouth(1, 0, true);
		walker.setPosition(new Vector2i(3, 0));
		walker.setDirection(Direction.West);
		
		walker.Advance(2000);
		assertEquals(Direction.East, walker.getDirection());
		assertEquals(1, walker.getPosition().x);
		assertEquals(0, walker.getPosition().y);
		
		/* Test dead end turn
		 * =====
		 * <-- |
		 * --> |
		 * =====
		 */
		world.setSouth(11, 0, true);
		walker.setPosition(new Vector2i(9, 0));
		walker.setDirection(Direction.East);
		
		walker.Advance(2000);
		assertEquals(Direction.West, walker.getDirection());
		assertEquals(11, walker.getPosition().x);
		assertEquals(0, walker.getPosition().y);
	}
	
	void testWalkersTurnWhenAdded()
	{
		/* Tests that a walker that is totally surrounded will stop */
		World world = new World();
		world.setEast(0, 0, true);
		world.setNorth(0, 0, true);
		world.setWest(0, 0, true);
		Walker walker = new Walker();
		walker.setPosition(new Vector2i(0,0));
		walker.setDirection(Direction.East);
		walker.setWorld(world);
		
		assertEquals(Direction.South, walker.getDirection());
	}
	
	void testTrappedWalker()
	{
		/* Tests that a walker that is totally surrounded will stop */
		World world = new World();
		world.setEast(0, 0, true);
		world.setSouth(0, 0, true);
		world.setNorth(0, 0, true);
		world.setWest(0, 0, true);
		Walker walker = new Walker();
		walker.setPosition(new Vector2i(0,0));
		walker.setWorld(world);
		
		
		walker.Advance(500);
	}
	
	
}
