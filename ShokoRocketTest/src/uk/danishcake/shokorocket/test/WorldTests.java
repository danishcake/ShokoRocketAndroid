package uk.danishcake.shokorocket.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;

import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Log;

import junit.framework.TestCase;
import uk.danishcake.shokorocket.World;

public class WorldTests extends AndroidTestCase {
	public void testWorldDefaults()
	{
		World world = new World();
		assertEquals(12, world.getWidth());
		assertEquals(9, world.getHeight());
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
	
	public void testWorldAssetsLoadable()
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
			InputStream world_stream = getContext().getAssets().open("Levels/Level 01.Level");			
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
			InputStream world_stream = getContext().getAssets().open("Levels/Level 01.Level");			
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
			InputStream world_stream = getContext().getAssets().open("Levels/Level 01.Level");			
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
		
	}
	
	public void testWorldLoadsHolesAndRockets()
	{
		
	}
}
