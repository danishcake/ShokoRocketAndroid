package uk.danishcake.shokorocket.simulation;

import uk.danishcake.shokorocket.NL;
import uk.danishcake.shokorocket.simulation.Walker.WalkerState;
import uk.danishcake.shokorocket.simulation.Walker.WalkerType;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

public class SPWorld extends WorldBase {
	public enum WorldState
	{
		OK, Failed, Success
	}
	public class ArrowRecord
	{
		public int x;
		public int y;
		public Direction direction;
	}
		
	private ArrayList<Walker> mLiveMice = new ArrayList<Walker>();
	private ArrayList<Walker> mDeadMice = new ArrayList<Walker>();
	private ArrayList<Walker> mRescuedMice = new ArrayList<Walker>();
	private ArrayList<Walker> mLiveCats = new ArrayList<Walker>();
	private ArrayList<Walker> mDeadCats = new ArrayList<Walker>();
	
	private SquareType[] mSpecialSquares = new SquareType[mWidth*mHeight];
	private WorldState mWorldState = WorldState.OK;
	
	private ArrayList<ArrowRecord> mSolution = new ArrayList<ArrowRecord>();
	private ArrayList<Direction> mArrowStock = new ArrayList<Direction>();
	private boolean mUnlimitedArrows = false;
	
	private String mSplashMessage = null;
	
	private boolean mMouseRescued = false;
	private int mRotation = 0;
		
	/**
	 * Sets the splash message. This will be shown when a level is loaded.
	 * @param message the splash message
	 */
	public void setSplashMessage(String message) {
		mSplashMessage = message;
	}
	
	/**
	 * Gets the splash message. If not set or loaded then null
	 * @return The splash message to be shown when level loaded
	 */
	public String getSplashMessage() {
		return mSplashMessage;
	}
	
	/* getLiveMice
	 * @return the mice which are still alive and unrescued
	 */
	public ArrayList<Walker> getLiveMice() {
		return mLiveMice;
	}
	
	/* getDeadMice
	 * @return the cats which are dead
	 */
	public ArrayList<Walker> getDeadMice() {
		return mDeadMice;
	}
	
	/* getRescuedMice
	 * @return the mice which have reached the rocket
	 */
	public ArrayList<Walker> getRescuedMice() {
		return mRescuedMice;
	}
	
	/* getLiveCats
	 * @return the cats which are still alive
	 */
	public ArrayList<Walker> getLiveCats() {
		return mLiveCats;
	}
	
	/* getDeadCats
	 * @return the cats which are Dead
	 */
	public ArrayList<Walker> getDeadCats() {
		return mDeadCats;
	}

	/**
	 * getRotation
	 * @return the number of rotations to the right performed
	 */
	public int getRotation() {
		return mRotation;
	}

	/* addMouse
	 * @param walker the mouse
	 */
	public void addMouse(Walker walker) {
		mLiveMice.add(walker);
		walker.setWorld(this);
		walker.setSpeed(Walker.MouseSpeed);
		walker.setWalkerType(WalkerType.Mouse);
	}
	
	/* addCat
	 * @param walker the cat
	 */
	public void addCat(Walker walker) {
		mLiveCats.add(walker);
		walker.setWorld(this);
		walker.setSpeed(Walker.CatSpeed);
		walker.setWalkerType(WalkerType.Cat);
	}
	
	private void clearWalker(int x, int y) {
		Iterator<Walker> m_it = mLiveMice.iterator();
		while(m_it.hasNext())
		{
			Walker walker = m_it.next();
			Vector2i walker_position = walker.getStartingPosition();
			if(walker_position.x == x && walker_position.y == y)
			{
				m_it.remove();
			}
		}
		
		Iterator<Walker> c_it = mLiveCats.iterator();
		while(c_it.hasNext())
		{
			Walker walker = c_it.next();
			Vector2i walker_position = walker.getStartingPosition();
			if(walker_position.x == x && walker_position.y == y)
			{
				c_it.remove();
			}
		}
	}
	
	/**
	 * toggleMouse
	 * @param x X position
	 * @param y Y position
	 * @param direction Direction to face
	 */
	public void toggleMouse(int x, int y, Direction direction) {
		boolean found = false;
		Iterator<Walker> m_it = mLiveMice.iterator();
		while(m_it.hasNext())
		{
			Walker walker = m_it.next();
			Vector2i walker_position = walker.getStartingPosition();
			if(walker_position.x == x && walker_position.y == y)
			{
				if(walker.getDirection() == direction)
					found = true;
				m_it.remove();
			}
		}
		
		Iterator<Walker> c_it = mLiveCats.iterator();
		while(c_it.hasNext())
		{
			Walker walker = c_it.next();
			Vector2i walker_position = walker.getStartingPosition();
			if(walker_position.x == x && walker_position.y == y)
			{
				c_it.remove();
			}
		}
		if(getSpecialSquare(x, y) == SquareType.Hole || getSpecialSquare(x, y) == SquareType.Rocket)
			setSpecialSquare(x, y, SquareType.Empty);
		
		if(!found)
		{
			Walker walker = new Walker();
			walker.setDirection(direction);
			walker.setPosition(new Vector2i(x, y));
			addMouse(walker);
		}
	}
	
	/**
	 * toggleCat
	 * @param x X position
	 * @param y Y position
	 * @param direction Direction to face
	 */
	public void toggleCat(int x, int y, Direction direction) {
		boolean found = false;
		Iterator<Walker> m_it = mLiveMice.iterator();
		while(m_it.hasNext())
		{
			Walker walker = m_it.next();
			Vector2i walker_position = walker.getStartingPosition();
			if(walker_position.x == x && walker_position.y == y)
			{
				m_it.remove();
			}
		}
		
		Iterator<Walker> c_it = mLiveCats.iterator();
		while(c_it.hasNext())
		{
			Walker walker = c_it.next();
			Vector2i walker_position = walker.getStartingPosition();
			if(walker_position.x == x && walker_position.y == y)
			{
				if(walker.getDirection() == direction)
					found = true;
				c_it.remove();
			}
		}
		if(getSpecialSquare(x, y) == SquareType.Hole || getSpecialSquare(x, y) == SquareType.Rocket)
			setSpecialSquare(x, y, SquareType.Empty);
		
		if(!found)
		{
			Walker walker = new Walker();
			walker.setDirection(direction);
			walker.setPosition(new Vector2i(x, y));
			addCat(walker);
		}
	}
	
	
	
	/* getHole
	 * @return if there is a hole at x/y
	 */
	public boolean getHole(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)] == SquareType.Hole;
	}
	
	/* setHole
	 * Sets the hole at x/y to hole. Will not clear a rocket
	 */
	public void setHole(int x, int y, boolean hole) {
		clearWalker(x, y);
		if(hole)
			mSpecialSquares[wallIndex(x, y)] = SquareType.Hole;
		else if(mSpecialSquares[wallIndex(x, y)] == SquareType.Hole)
				mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
	}
	
	/* toggleHole
	 * Toggles the hole at x/y. If a rocket is present it changes it to a hole 
	 */
	public void toggleHole(int x, int y)
	{
		clearWalker(x, y);
		if(mSpecialSquares[wallIndex(x, y)] == SquareType.Hole)
			mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
		else
			mSpecialSquares[wallIndex(x, y)] = SquareType.Hole;
	}

	/* getRocket
	 * @return true if there is a rocket at x/y
	 */
	public boolean getRocket(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)] == SquareType.Rocket;
	}
	
	/* setRocket
	 * Sets the rocket at x/y to rocket. Will not clear a hole
	 */
	public void setRocket(int x, int y, boolean rocket) {
		clearWalker(x, y);
		if(rocket)
			mSpecialSquares[wallIndex(x, y)] = SquareType.Rocket;
		else if(mSpecialSquares[wallIndex(x, y)] == SquareType.Rocket)
				mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
	}
	
	/* toggleRocket
	 * Toggles the rocket at x/y. If a hole is present it changes it to a rocket 
	 */
	public void toggleRocket(int x, int y) {
		clearWalker(x, y);
		if(mSpecialSquares[wallIndex(x, y)] == SquareType.Rocket)
			mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
		else
			mSpecialSquares[wallIndex(x, y)] = SquareType.Rocket;
	}
	
	private boolean stockHasArrow(Direction direction) {
		for (Direction dir : mArrowStock) {
			if(dir == direction)
			{
				return true;
			}
		}
		return false;
	}
	
	private void removeArrowFromStock(Direction direction) {
		for (Direction dir : mArrowStock) {
			if(dir == direction)
			{
				mArrowStock.remove(dir);
				break;
			}
		}
	}
	
	/* setArrow
	 * Sets the grid square to a certain direction. Clears if invalid passed in
	 */
	public void setArrow(int x, int y, Direction direction) {
		SquareType square_type = mSpecialSquares[wallIndex(x, y)];
		Direction square_dir = square_type.getArrowDirectionality();
		//Can't set arrow in rocket or hole
		if(square_type == SquareType.Hole || square_type == SquareType.Rocket)
			return;
		
		switch(direction)
		{
		case North:
		case South:
		case West:
		case East:
			if(mUnlimitedArrows)
				mSpecialSquares[wallIndex(x, y)] = direction.toArrow();
			else if(stockHasArrow(direction))
			{
				if(square_dir == Direction.Invalid)
				{
					mSpecialSquares[wallIndex(x, y)] = direction.toArrow();
					removeArrowFromStock(direction);
				} else
				{
					mArrowStock.add(square_dir);
					mSpecialSquares[wallIndex(x, y)] = direction.toArrow();
					removeArrowFromStock(direction);	
				}
			}
			break;
		case Invalid:
			if(!mUnlimitedArrows && square_dir != Direction.Invalid)
			{
				mArrowStock.add(square_dir);
			}
			mSpecialSquares[wallIndex(x,y)] = SquareType.Empty;
			break;
		}		
	}
	
	/* toggleArrow
	 * Toggles the arrow in a particular direction
	 */
	public void toggleArrow(int x, int y, Direction direction) {
		switch(mSpecialSquares[wallIndex(x, y)])
		{
		case NorthArrow:
			if(direction == Direction.North)
				setArrow(x, y, Direction.Invalid);
			else
				setArrow(x, y, direction);
			break;
		case WestArrow:
			if(direction == Direction.West)
				setArrow(x, y, Direction.Invalid);
			else
				setArrow(x, y, direction);
			break;
		case SouthArrow:
			if(direction == Direction.South)
				setArrow(x, y, Direction.Invalid);
			else
				setArrow(x, y, direction);
			break;
		case EastArrow:
			if(direction == Direction.East)
				setArrow(x, y, Direction.Invalid);
			else
				setArrow(x, y, direction);
			break;
		default:
			setArrow(x, y, direction);
			break;		
		}
	}
	
	/* getSpecialSquare
	 * Gets the square type at x/y
	 * @return the square type at x/y
	 */
	public SquareType getSpecialSquare(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)];
	}
	
	/* setSpecialSquare
	 * Directly sets the square type at x/y
	 */
	public void setSpecialSquare(int x, int y, SquareType square_type) {
		mSpecialSquares[wallIndex(x,y)] = square_type;
	}
	
	/* getWorldState
	 * @return the worlds state
	 */
	public WorldState getWorldState() {
		return mWorldState;
	}
	
	/* setWorldState
	 * Sets the world state
	 */
	public void setWorldState(WorldState world_state) {
		mWorldState = world_state;
	}
	
	/* getArrowStockUnlimited
	 * @return the arrow stock unlimited state
	 */
	public boolean getArrowStockUnlimited() {
		return mUnlimitedArrows;
	}
	
	/* setArrowStockUnlimited
	 * @param unlimited_arrows whether the arrow stocks are unlimited
	 * Sets the arrow stock to unlimited or not.
	 */
	public void setArrowStockUnlimited(boolean unlimited_arrows) {
		mUnlimitedArrows = unlimited_arrows;
	}
	
	/* getArrowStock
	 * @return the stock or remaining arrows
	 */
	public ArrayList<Direction> getArrowStock() {
		return mArrowStock;
	}
	
	public boolean getMouseRescued() {
		boolean mouse_rescued = mMouseRescued;
		mMouseRescued = false;
		return mouse_rescued;
	}
	
	public SPWorld(int width, int height)
	{
		super(width, height);
		mSpecialSquares = new SquareType[mWidth*mHeight];
		defaultSpecialSquares();
		mUnlimitedArrows = true;
	}
	
	/* SPWorld()
	 * Creates a empty world 12x9 with walls around the edge
	 */
	public SPWorld() 
	{
		super();
		defaultSpecialSquares();
		mUnlimitedArrows = true;
	}
	
	/* SPWorld(input)
	 * Loads a world from specified XML file
	 * @param input an InputStream representing the level  
	 */
	public SPWorld(InputStream input) throws IOException
	{
		LoadFromXML(input);
		//loadSpecific is then called with root element to parse
	}
	
	@Override
	protected void loadSpecific(Element root) {
		mUnlimitedArrows = false;
		mSpecialSquares = new SquareType[mWidth*mHeight];
		defaultSpecialSquares();
		NodeList splash_nodes = NL.ElementsByTag(root, "Splash");
		if(splash_nodes.getLength() >= 1)
		{
			String splash_text = splash_nodes.item(0).getFirstChild().getNodeValue();
			if(splash_text != null)
				mSplashMessage = splash_text; 
		}
		loadEntities(root);
		loadSolution(root);
	}
	
	/* Loads entities from XML
	 * @param root the document element in the XML level
	 */
	private void loadEntities(Element root) {
		//Load mice
		NodeList mouse_list = NL.ElementsByTag(root, "Mouse");
		for(int i = 0; i < mouse_list.getLength(); i++)
		{			
			Node mouse_node = mouse_list.item(i);
			NamedNodeMap mouse_position_attr = mouse_node.getAttributes();
			
			Node pos_x = mouse_position_attr.getNamedItem("x");
			Node pos_y = mouse_position_attr.getNamedItem("y");
			Node dir = mouse_position_attr.getNamedItem("d");
			if(pos_x == null || pos_y == null || dir == null)
			{
				throw new InvalidParameterException("Both x and y must be specified in mouse");			
			} else
			{
				try
				{
					int x = Integer.parseInt(pos_x.getNodeValue());
					int y = Integer.parseInt(pos_y.getNodeValue());
					Walker walker = new Walker();
					walker.setPosition(new Vector2i(x,y));
					walker.setDirection(Direction.valueOf(dir.getNodeValue()));
					addMouse(walker);
				} catch(NumberFormatException nfe)
				{
					throw new InvalidParameterException("Both x, y and d must be specified in mouse");
				} catch(IllegalArgumentException iae)
				{
					throw new InvalidParameterException("Unable to parse direction to enum in mouse");
				}
			}
		}		
		//Load cats
		NodeList cat_list = NL.ElementsByTag(root, "Cat");
		for(int i = 0; i < cat_list.getLength(); i++)
		{			
			Node cat_node = cat_list.item(i);
			NamedNodeMap cat_position_attr = cat_node.getAttributes();
			
			Node pos_x = cat_position_attr.getNamedItem("x");
			Node pos_y = cat_position_attr.getNamedItem("y");
			Node dir = cat_position_attr.getNamedItem("d");
			if(pos_x == null || pos_y == null || dir == null)
			{
				throw new InvalidParameterException("Both x, y and d must be specified in cat");			
			} else
			{
				try
				{
					int x = Integer.parseInt(pos_x.getNodeValue());
					int y = Integer.parseInt(pos_y.getNodeValue());
					Walker walker = new Walker();
					walker.setPosition(new Vector2i(x,y));
					walker.setDirection(Direction.valueOf(dir.getNodeValue()));
					addCat(walker);
				} catch(NumberFormatException nfe)
				{
					throw new InvalidParameterException("Unable to parse x or y in cat");
				} catch(IllegalArgumentException iae)
				{
					throw new InvalidParameterException("Unable to parse direction to enum in cat");
				}
			}
		}
		//Load rockets
		NodeList rocket_list = NL.ElementsByTag(root, "Rocket");
		for(int i = 0; i < rocket_list.getLength(); i++)
		{			
			Node rocket_node = rocket_list.item(i);
			NamedNodeMap rocket_position_attr = rocket_node.getAttributes();
			
			Node pos_x = rocket_position_attr.getNamedItem("x");
			Node pos_y = rocket_position_attr.getNamedItem("y");
			if(pos_x == null || pos_y == null)
			{
				throw new InvalidParameterException("Both x and y must be specified in rocket");			
			} else
			{
				try
				{
					int x = Integer.parseInt(pos_x.getNodeValue());
					int y = Integer.parseInt(pos_y.getNodeValue());
					setRocket(x, y, true);
				} catch(NumberFormatException nfe)
				{
					throw new InvalidParameterException("Unable to parse x or y in rocket");
				}
			}
		}
		//Load holes
		NodeList hole_list = NL.ElementsByTag(root, "Hole");
		for(int i = 0; i < hole_list.getLength(); i++)
		{			
			Node hole_node = hole_list.item(i);
			NamedNodeMap hole_position_attr = hole_node.getAttributes();
			
			Node pos_x = hole_position_attr.getNamedItem("x");
			Node pos_y = hole_position_attr.getNamedItem("y");
			if(pos_x == null || pos_y == null)
			{
				throw new InvalidParameterException("Both x and y must be specified in hole");			
			} else
			{
				try
				{
					int x = Integer.parseInt(pos_x.getNodeValue());
					int y = Integer.parseInt(pos_y.getNodeValue());
					setHole(x, y, true);
				} catch(NumberFormatException nfe)
				{
					throw new InvalidParameterException("Unable to parse x or y in hole");
				}
			}
		}
	}
	
	private void loadSolution(Element root) {
		//Load arrows
		NodeList arrow_list = NL.ElementsByTag(root, "Arrow");
		for(int i = 0; i < arrow_list.getLength(); i++)
		{			
			Node arrow_node = arrow_list.item(i);
			NamedNodeMap arrow_position_attr = arrow_node.getAttributes();
			
			Node pos_x = arrow_position_attr.getNamedItem("x");
			Node pos_y = arrow_position_attr.getNamedItem("y");
			Node dir = arrow_position_attr.getNamedItem("d");
			if(pos_x == null || pos_y == null || dir == null)
			{
				throw new InvalidParameterException("Both x, y and d must be specified in arrow");			
			} else
			{
				try
				{
					int x = Integer.parseInt(pos_x.getNodeValue());
					int y = Integer.parseInt(pos_y.getNodeValue());
					//setArrow(x, y, Direction.valueOf(dir.getNodeValue()));
					mArrowStock.add(Direction.valueOf(dir.getNodeValue()));
					ArrowRecord ar = new ArrowRecord();
					ar.x = x;
					ar.y = y;
					ar.direction = Direction.valueOf(dir.getNodeValue());
					mSolution.add(ar);
				} catch(NumberFormatException nfe)
				{
					throw new InvalidParameterException("Unable to parse x or y in arrow");
				} catch(IllegalArgumentException iae)
				{
					throw new InvalidParameterException("Unable to parse direction to enum in arrow");
				}
			} 
		}
	}
	
	public void Save(OutputStream output)
	{
		//Unfortunately there is not built in XML writing in android until API 8, so will roll my own
		
		PrintStream out = new PrintStream(output);
		//XML preamble
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		out.println("<Level>");
		//Save properties
		out.println("<Name>" + mLevelName + "</Name>");
		out.println("<Author>" + mLevelAuthor + "</Author>");
		if(mSplashMessage != null)
		{
			out.println("<Splash>" + mSplashMessage + "</Splash>");
		}
		out.println("<Size x=\"" + Integer.toString(mWidth) + "\" y=\"" + Integer.toString(mHeight) + "\"/>");
		//Save walls
		for(int y = 0; y < mHeight; y++)
		{
			for(int x = 0; x <mWidth; x++)
			{
				if(getWest(x, y))
					out.println("<V x=\"" + Integer.toString(x) + "\" y=\"" + Integer.toString(y) + "\" />");
				if(getNorth(x, y))
					out.println("<H x=\"" + Integer.toString(x) + "\" y=\"" + Integer.toString(y) + "\" />");
			}
		}
		//Save mice
		for (Walker mouse : mLiveMice) {
			out.println("<Mouse x=\"" + Integer.toString(mouse.getStartingPosition().x) + "\" y=\"" + Integer.toString(mouse.getStartingPosition().y) + "\" d=\"" + mouse.getStartingDirection().toString() + "\" />");
		}
		//Save cats
		for (Walker cat : mLiveCats) {
			out.println("<Cat x=\"" + Integer.toString(cat.getStartingPosition().x) + "\" y=\"" + Integer.toString(cat.getStartingPosition().y) + "\" d=\"" + cat.getStartingDirection().toString() + "\" />");
		}
		//Save rockets
		//Save holes
		//Save arrows
		for(int y = 0; y < mHeight; y++)
		{
			for(int x = 0; x <mWidth; x++)
			{
				SquareType square = getSpecialSquare(x, y); 
				switch(square)
				{
				case Rocket:
					out.println("<Rocket x=\"" + Integer.toString(x) + "\" y=\"" + Integer.toString(y) + "\" />");
					break;
				case Hole:
					out.println("<Hole x=\"" + Integer.toString(x) + "\" y=\"" + Integer.toString(y) + "\" />");
					break;
				case EastArrow:
				case EastHalfArrow:
				case EastDestroyedArrow:
				case WestArrow:
				case WestHalfArrow:
				case WestDestroyedArrow:
				case NorthArrow:
				case NorthHalfArrow:
				case NorthDestroyedArrow:
				case SouthArrow:
				case SouthHalfArrow:
				case SouthDestroyedArrow:
					Direction d = square.getArrowDirectionality();
					out.println("<Arrow x=\"" + Integer.toString(x) + "\" y=\"" + Integer.toString(y) + "\" d=\"" + d.toString() + "\"/>");
					break;
				}
			}
		}		
		out.println("</Level>");
		out.flush();
		out.close();
	}
	
	/* defaultSpecialSquares()
	 * Sets the default special squares
	 */
	private void defaultSpecialSquares() {
		for(int x = 0; x < mWidth; x++)
		{
			for(int y = 0; y < mHeight; y++)
			{
				mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
			}
		}
	}

	/* tick
	 * Advances cats, mice & performs collisions
	 * @param timespan the number of milliseconds to advance for
	 */
	public void Tick(int timespan) {
		final int max_timespan = 100; //No more than 100ms movement, longer periods should be broken up
		while(timespan > 0)
		{
			int sub_timespan = timespan;
			if(sub_timespan > max_timespan)
				sub_timespan = max_timespan;
			timespan -= sub_timespan;
			if(mWorldState == WorldState.OK)
			{
				//Whether or not justDeadX is a good pattern to use I don't know, but it works in C++
				//and works here, so will use
				ArrayList<Walker> justDeadMice = new ArrayList<Walker>();
				ArrayList<Walker> justRescuedMice = new ArrayList<Walker>();
				ArrayList<Walker> justDeadCats = new ArrayList<Walker>();
				
				for (Walker mouse : mLiveMice) {
					mouse.Advance(sub_timespan);
					if(mouse.getWalkerState() == WalkerState.Dead && !justDeadMice.contains(mouse))
					{
						justDeadMice.add(mouse);
						mWorldState = WorldState.Failed;
					}
					if(mouse.getWalkerState() == WalkerState.Rescued && !justRescuedMice.contains(mouse))
					{
						justRescuedMice.add(mouse);
						mMouseRescued = true;
					}
				}
				for (Walker cat : mLiveCats) {
					cat.Advance(sub_timespan);
					if(cat.getWalkerState() == WalkerState.Dead && !justDeadCats.contains(cat))
						justDeadCats.add(cat);
					if(cat.getWalkerState() == WalkerState.Rescued && !justDeadCats.contains(cat))
					{
						justDeadCats.add(cat);
						mWorldState = WorldState.Failed;
					}
				}
				
				for(Walker cat : mLiveCats) {
					for(Walker mouse : mLiveMice) {
						//Calculate distance
						if(checkCollision(cat, mouse) && !justDeadMice.contains(mouse))
						{
							justDeadMice.add(mouse);
							mWorldState = WorldState.Failed;
						}
					}
				}
				mLiveMice.removeAll(justDeadMice);
				mDeadMice.addAll(justDeadMice);
				mLiveMice.removeAll(justRescuedMice);
				mRescuedMice.addAll(justRescuedMice);
				mLiveCats.removeAll(justDeadCats);
				mDeadCats.addAll(justDeadCats);
			}
			
			for(Walker mouse : mDeadMice)
			{
				mouse.DeathTick(sub_timespan);
			}
			for(Walker mouse : mRescuedMice)
			{
				mouse.DeathTick(sub_timespan);
			}
			for(Walker cat : mDeadCats)
			{
				cat.DeathTick(sub_timespan);
			}
			
			if(mDeadMice.size() == 0 && mLiveMice.size() == 0 && mRescuedMice.size() > 0)
			{
				mWorldState = WorldState.Success;
			}
		}
	}
	
	@Override
	public void walkerReachNewSquare(Walker walker, int x, int y, Direction d) {
		//First interact with special squares (arrow, holes & rockets)
		SquareType square = getSpecialSquare(x, y);
		//Holes
		if(square == SquareType.Hole)
		{
			walker.setWalkerState(WalkerState.Dead);
		}
		if(square == SquareType.Rocket)
		{
			walker.setWalkerState(WalkerState.Rescued);
		}
		//Arrows
		Direction arrow_direction = square.toArrowDirection(); 
		if(arrow_direction != Direction.Invalid)
		{
			if(arrow_direction == Turns.TurnAround(d) && walker.getWalkerType() == WalkerType.Cat)
			{
				SquareType reduced = square.Diminish();
				setSpecialSquare(x, y, reduced);
			}
			walker.setDirection2(arrow_direction);
		}
		/* Now interact with walls */
		super.walkerReachNewSquare(walker, x, y, walker.getDirection());
	}
	
	/* Reset
	 * Resets the world to it's starting state
	 */
	public void Reset() {
		mLiveMice.addAll(mDeadMice);
		mLiveMice.addAll(mRescuedMice);
		mLiveCats.addAll(mDeadCats);
		mDeadMice.clear();
		mRescuedMice.clear();
		mDeadCats.clear();
		
		for (Walker mouse : mLiveMice) {
			mouse.Reset();
		}
		for (Walker cat : mLiveCats) {
			cat.Reset();
		}
		
		for(int x = 0; x < mWidth; x++)
		{
			for(int y = 0; y < mHeight; y++)
			{
				mSpecialSquares[wallIndex(x,y)] = mSpecialSquares[wallIndex(x,y)].Restore();
			}
		}
		
		mWorldState = WorldState.OK;
	}
	
	public void LoadSolution() {
		ClearArrows();
		for (ArrowRecord ar : mSolution) {
			setArrow(ar.x, ar.y, ar.direction);
		}
	}
	
	/* ClearArrows
	 * Clears the arrows
	 */
	public void ClearArrows() {
		for(int x = 0; x < mWidth; x++)
		{
			for(int y = 0; y < mHeight; y++)
			{
				switch(mSpecialSquares[wallIndex(x, y)])
				{
				case EastArrow:
				case EastDestroyedArrow:
				case EastHalfArrow:
					mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
					if(!mUnlimitedArrows) mArrowStock.add(Direction.East);
					break;
				case NorthArrow:
				case NorthDestroyedArrow:
				case NorthHalfArrow:
					mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
					if(!mUnlimitedArrows) mArrowStock.add(Direction.North);
					break;
				case WestArrow:
				case WestDestroyedArrow:
				case WestHalfArrow:
					mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
					if(!mUnlimitedArrows) mArrowStock.add(Direction.West);
					break;
				case SouthArrow:
				case SouthDestroyedArrow:
				case SouthHalfArrow:
					mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
					if(!mUnlimitedArrows) mArrowStock.add(Direction.South);
					break;
				}
			}
		}
	}

	/**
	 * RotateRight()
	 * Changes the level orientation by rotating all members right
	 */
	public void RotateRight() {
		Reset();
		mRotation = (mRotation + 1) % 4;
		//Rotate size
		int temp = mWidth;
		mWidth = mHeight;
		mHeight = temp;
		//Rotate walkers
		for(Walker mouse : mLiveMice)
		{
			Vector2i position = mouse.getPosition();
			mouse.setPosition(new Vector2i(mWidth - 1 - position.y, position.x));
			mouse.setDirection(mouse.getDirection().RotateRight());
		}
		for(Walker cat : mLiveCats)
		{
			Vector2i position = cat.getPosition();
			cat.setPosition(new Vector2i(mWidth - 1 - position.y, position.x));
			cat.setDirection(cat.getDirection().RotateRight());
		}
		//Rotate walls
		//Rotate special squares
		int[] walls = mWalls;
		mWalls = new int[mWidth*mHeight];
		SquareType[] special_squares = mSpecialSquares;
		mSpecialSquares = new SquareType[mWidth*mHeight];
		for(int x = 0; x < mWidth; x++)
		{
			for(int y = 0; y < mHeight; y++)
			{
				/*
				 * 2x3
				 * 01
				 * 23
				 * 45
				 * 
				 * to
				 * 
				 * 3x2
				 * 420
				 * 531
				 * 
				 * 
				 * West from south, north from west
				 * Only east to read West and north, so set north and east
				*/
				int old_index = (mWidth-1) * mHeight + y - x * mHeight; 
				
				mSpecialSquares[wallIndex(x, y)] = special_squares[old_index].RotateRight();
				
				setNorth(x, y, (walls[old_index] & eWestWall) != 0);
				setEast(x, y, (walls[old_index] & eNorthWall) != 0);
				  
			}
		}
		
		//Rotate saved solution
		for(ArrowRecord ar : mSolution)
		{
			ar.direction = ar.direction.RotateRight();
		}
		//Rotate arrow stock
		for(int i = 0; i < mArrowStock.size(); i++)
		{
			mArrowStock.set(i, mArrowStock.get(i).RotateRight());
		}
	}

	/**
	 * RotateLeft()
	 * Changes the level orientation by rotating all members left
	 */
	public void RotateLeft()
	{
		RotateRight();
		RotateRight();
		RotateRight();
	}

	/**
	 * RotateToOriginal()
	 * Rotates the level to it's original orientation
	 */
	public void RotateToOriginal()
	{
		for(int i = 0; i < mRotation; i++)
		{
			RotateLeft();
		}
	}
}