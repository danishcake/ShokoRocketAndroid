package uk.danishcake.shokorocket.test;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import android.test.AndroidTestCase;

import uk.danishcake.shokorocket.Direction;
import uk.danishcake.shokorocket.SquareType;
import uk.danishcake.shokorocket.Walker;
import uk.danishcake.shokorocket.World;

public class WorldTests extends AndroidTestCase {
	public void testWorldDefaults()
	{
		World world = new World();
		assertEquals(12, world.getWidth());
		assertEquals(9, world.getHeight());
		assertEquals(true, world.getArrowStockUnlimited());
	}
	
	public void testWorldWallBounds()
	{
		World world = new World();
		boolean negative_x_throws = false;
		boolean negative_y_throws = false;
		boolean excessive_x_throws = false;
		boolean excessive_y_throws = false;
		
		try
		{	
			world.getSouth(-1, 0);
		} catch(InvalidParameterException exception)
		{
			negative_x_throws = true;
		} catch(Exception other_exception)
		{
			assertFalse("Invalid exception", true);
		}
		assertTrue(negative_x_throws);
		
		try
		{	
			world.getSouth(12, 0);
		} catch(InvalidParameterException exception)
		{
			excessive_x_throws = true;
		} catch(Exception other_exception)
		{
			assertFalse("Invalid exception", true);
		}
		assertTrue(excessive_x_throws);
		
		try
		{	
			world.getSouth(0, -1);
		} catch(InvalidParameterException exception)
		{
			negative_y_throws = true;
		} catch(Exception other_exception)
		{
			assertFalse("Invalid exception", true);
		}
		assertTrue(negative_y_throws);
		
		try
		{	
			world.getSouth(0, 10);
		} catch(InvalidParameterException exception)
		{
			excessive_y_throws = true;
		} catch(Exception other_exception)
		{
			assertFalse("Invalid exception", true);
		}
		assertTrue(excessive_y_throws);
	}
	
	public void testWorldWallDefaults()
	{
		World world = new World();
		for(int x = 0; x < 12; x++)
		{
			assertTrue(world.getNorth(x, 0));
			assertTrue(world.getSouth(x, 8));
		}
		for(int y = 0; y < 9; y++)
		{
			assertTrue(world.getWest(0, y));
			assertTrue(world.getEast(11, y));
		}
	}
	
	public void testWorldAssetsListed()
	{
		try
		{
			String[] files = getContext().getAssets().list("");
			for (String file : files) 
			{
				assertTrue(file.length() > 0);
			}
		}
		catch(IOException io_ex)
		{
			assertTrue("Some sort of io error - " + io_ex.getMessage(), false);	
		}
		
	}
	
	public void testWorldLoadsNames()
	{
		try
		{
			InputStream world_stream = getContext().getAssets().open("Levels/Original Easy/Level 01.Level");			
			World world = new World(world_stream);
			
			assertEquals("Edward Woolhouse", world.getAuthor());
			assertEquals("Seems familiar", world.getLevelName());
		}
		catch(IOException io_ex)
		{
			assertTrue("Error opening stream - " + io_ex.getMessage(), false);
		}
	}
	
	public void testWorldLoadsSize()
	{
		try
		{
			InputStream world_stream = getContext().getAssets().open("Levels/Original Easy/Level 01.Level");			
			World world = new World(world_stream);

			assertEquals(12, world.getWidth());
			assertEquals(9, world.getHeight());
		}
		catch(IOException io_ex)
		{
			assertTrue("Error opening stream - " + io_ex.getMessage(), false);
		}
	}
	
	public void testWorldLoadsWalls()
	{
		try
		{
			InputStream world_stream = getContext().getAssets().open("Levels/Original Easy/Level 01.Level");			
			World world = new World(world_stream);

			assertTrue(world.getWest(2, 0));
			assertFalse(world.getWest(2, 1));
			assertTrue(world.getWest(2, 2));
		}
		catch(IOException io_ex)
		{
			assertTrue("Error opening stream - " + io_ex.getMessage(), false);
		}
	}
	
	public void testWorldLoadsCatsAndMice()
	{
		try
		{
			InputStream world_stream = getContext().getAssets().open("Levels/Original Easy/Level 23.Level");			
			World world = new World(world_stream);
			assertEquals(10, world.getLiveMice().size());
			assertEquals(7, world.getLiveCats().size());
			//Test mouse position TODO
			
		}
		catch(IOException io_ex)
		{
			assertTrue("Error opening stream - " + io_ex.getMessage(), false);
		}	
	}
	
	public void testWorldLoadsCatsAndMicePositionsAndDirections()
	{
		try
		{
			InputStream world_stream = getContext().getAssets().open("Levels/Original Easy/Level 06.Level");			
			World world = new World(world_stream);
			assertEquals(1, world.getLiveMice().size());
			Walker mouse = world.getLiveMice().get(0);
			assertEquals(4, mouse.getPosition().x);
			assertEquals(8, mouse.getPosition().y);
			assertEquals(Direction.East, mouse.getDirection());
			
			Walker first_cat = world.getLiveCats().get(0);
			assertEquals(11, first_cat.getPosition().x);
			assertEquals(0, first_cat.getPosition().y);
			assertEquals(Direction.West, first_cat.getDirection());
		}
		catch(IOException io_ex)
		{
			assertTrue("Error opening stream - " + io_ex.getMessage(), false);
		}	
	}	
	public void testWorldManualHolesAndRockets()
	{
		//Holes
		World world = new World();
		
		assertFalse(world.getHole(0,0));
		
		world.setHole(0, 0, true);
		assertTrue(world.getHole(0,0));
		
		world.setHole(0, 0, false);
		assertFalse(world.getHole(0,0));
		
		world.toggleHole(0, 0);
		assertTrue(world.getHole(0,0));
		
		world.toggleHole(0, 0);
		assertFalse(world.getHole(0,0));
		
		
		
		//Rockets
		assertFalse(world.getHole(5, 5));
		
		world.setRocket(5, 5, true);
		assertTrue(world.getRocket(5, 5));
		
		world.setRocket(5, 5, false);
		assertFalse(world.getRocket(5, 5));
		
		world.toggleRocket(5, 5);
		assertTrue(world.getRocket(5,5));
		
		world.toggleRocket(5, 5);
		assertFalse(world.getRocket(5,5));
		
		//Clear
		
		world.setRocket(6,6, true);
		assertTrue(world.getRocket(6, 6));
		world.setSpecialSquare(6, 6, SquareType.Empty);
		assertEquals(SquareType.Empty, world.getSpecialSquare(6, 6));
	}
	
	public void testWorldHolesAndRocketsExlusivity()
	{
		//Holes
		World world = new World();
		
		world.setHole(0, 0, true);
		assertTrue(world.getHole(0,0));
		
		world.setRocket(0, 0, true);
		assertFalse(world.getHole(0,0));
		assertTrue(world.getRocket(0,0));
	}
	
	public void testWorldLoadsHolesAndRockets()
	{
		try
		{
			InputStream world_stream = getContext().getAssets().open("Levels/Original Easy/Level 23.Level");			
			World world = new World(world_stream);
			
			assertFalse(world.getHole(0, 0));
			assertTrue(world.getHole(4, 8));
			assertFalse(world.getRocket(0, 0));
			assertTrue(world.getRocket(5, 8));
		}
		catch(IOException io_ex)
		{
			assertTrue("Error opening stream - " + io_ex.getMessage(), false);
		}		
	}
	
	public void testWorldSolution()
	{
		World world = new World();
		world.setArrow(1, 1, Direction.East);
		world.setArrow(2, 2, Direction.West);
		assertEquals(SquareType.EastArrow, world.getSpecialSquare(1, 1));
		assertEquals(SquareType.WestArrow, world.getSpecialSquare(2, 2));
		
		world.toggleArrow(3, 3, Direction.East);
		assertEquals(SquareType.EastArrow, world.getSpecialSquare(3, 3));
		world.toggleArrow(3, 3, Direction.East);
		assertEquals(SquareType.Empty, world.getSpecialSquare(3, 3));
		
		world.toggleArrow(3, 3, Direction.North);
		assertEquals(SquareType.NorthArrow, world.getSpecialSquare(3, 3));
		world.toggleArrow(3, 3, Direction.North);
		assertEquals(SquareType.Empty, world.getSpecialSquare(3, 3));
		
		world.toggleArrow(3, 3, Direction.West);
		assertEquals(SquareType.WestArrow, world.getSpecialSquare(3, 3));
		world.toggleArrow(3, 3, Direction.West);
		assertEquals(SquareType.Empty, world.getSpecialSquare(3, 3));
		
		world.toggleArrow(3, 3, Direction.South);
		assertEquals(SquareType.SouthArrow, world.getSpecialSquare(3, 3));
		world.toggleArrow(3, 3, Direction.South);
		assertEquals(SquareType.Empty, world.getSpecialSquare(3, 3));
	}
	
	public void testWorldLoadsSolution()
	{
		try
		{
			InputStream world_stream = getContext().getAssets().open("Levels/Original Easy/Level 23.Level");			
			World world = new World(world_stream);
			
			assertEquals(SquareType.Empty, world.getSpecialSquare(1, 1));
			assertEquals(SquareType.Empty, world.getSpecialSquare(5, 0));
			assertEquals(SquareType.Empty, world.getSpecialSquare(5, 2));
			assertEquals(SquareType.Empty, world.getSpecialSquare(11, 2));
			
			assertEquals(4, world.getArrowStock().size());
			
			world.LoadSolution();
			assertEquals(false, world.getArrowStockUnlimited());
			
			assertEquals(SquareType.EastArrow, world.getSpecialSquare(1, 1));
			assertEquals(SquareType.SouthArrow, world.getSpecialSquare(5, 0));
			assertEquals(SquareType.SouthArrow, world.getSpecialSquare(5, 2));
			assertEquals(SquareType.WestArrow, world.getSpecialSquare(11, 2));
			
			assertEquals(0, world.getArrowStock().size());
		}
		catch(IOException io_ex)
		{
			assertTrue("Error opening stream - " + io_ex.getMessage(), false);
		}
	}
	
	public void testArrowStockLimitsArrows()
	{
		World world = new World();
		world.setArrowStockUnlimited(true);

		world.setArrow(0, 0, Direction.East);
		world.setArrow(1, 1, Direction.East);
		
		assertEquals(Direction.East, world.getSpecialSquare(0, 0).ToDirection());
		assertEquals(Direction.East, world.getSpecialSquare(1, 1).ToDirection());
		
		World world2 = new World();
		world2.setArrowStockUnlimited(false);
		
		world2.setArrow(0, 0, Direction.East);
		world2.setArrow(1, 1, Direction.East);
		assertEquals(Direction.Invalid, world2.getSpecialSquare(0, 0).ToDirection());
		assertEquals(Direction.Invalid, world2.getSpecialSquare(1, 1).ToDirection());
	}
	
	public void testResetArrows()
	{
		try
		{
			InputStream world_stream = getContext().getAssets().open("Levels/Original Easy/Level 23.Level");			
			World world = new World(world_stream);
			world.LoadSolution();
			
			assertEquals(SquareType.EastArrow, world.getSpecialSquare(1, 1));
			assertEquals(SquareType.SouthArrow, world.getSpecialSquare(5, 0));
			assertEquals(SquareType.SouthArrow, world.getSpecialSquare(5, 2));
			assertEquals(SquareType.WestArrow, world.getSpecialSquare(11, 2));
			assertEquals(0, world.getArrowStock().size());
			
			world.ClearArrows();
			assertEquals(SquareType.Empty, world.getSpecialSquare(1, 1));
			assertEquals(SquareType.Empty, world.getSpecialSquare(5, 0));
			assertEquals(SquareType.Empty, world.getSpecialSquare(5, 2));
			assertEquals(SquareType.Empty, world.getSpecialSquare(11, 2));
			assertEquals(4, world.getArrowStock().size());			
			
		}
		catch(IOException io_ex)
		{
			assertTrue("Error opening stream - " + io_ex.getMessage(), false);
		}

	}
}	
