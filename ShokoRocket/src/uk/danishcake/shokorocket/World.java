package uk.danishcake.shokorocket;

import uk.danishcake.shokorocket.Direction;
import uk.danishcake.shokorocket.SquareType;
import uk.danishcake.shokorocket.Walker;
import uk.danishcake.shokorocket.Walker.WalkerState;
import uk.danishcake.shokorocket.Walker.WalkerType;

import java.io.InputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;


public class World {
	private int mWidth = 12;
	private int mHeight = 9;
	private String mLevelName = "Default";
	private String mLevelAuthor = "Unknown";
	
	private final int eWestWall = 1;
	private final int eNorthWall = 2;
	private int[] mWalls = new int[12*9];
	
	private ArrayList<Walker> mLiveMice = new ArrayList<Walker>();
	private ArrayList<Walker> mDeadMice = new ArrayList<Walker>();
	private ArrayList<Walker> mRescuedMice = new ArrayList<Walker>();
	private ArrayList<Walker> mLiveCats = new ArrayList<Walker>();
	private ArrayList<Walker> mDeadCats = new ArrayList<Walker>();
	
	private SquareType[] mSpecialSquares = new SquareType[12*9];
	
	/* getWidth
	 * @return width of the level - defaults to 12
	 */
	public int getWidth() {
		return mWidth;
	}
	/* getHeight
	 * @return height of the level - defaults to 9
	 */	
	public int getHeight() {
		return mHeight;
	}
	
	/* getAuthor
	 * @return the author of the map
	 */
	public String getAuthor() {
		return mLevelAuthor;
	}
	
	/* getLevelName
	 * @return the name of the level
	 */	
	public String getLevelName() {
		return mLevelName;
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
	
	/* getHole
	 * @return if there is a hole at x/y
	 */
	public Boolean getHole(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)] == SquareType.Hole;
	}
	
	/* setHole
	 * Sets the hole at x/y to hole. Will not clear a rocket
	 */
	public void setHole(int x, int y, Boolean hole) {
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
		if(mSpecialSquares[wallIndex(x, y)] == SquareType.Hole)
			mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
		else
			mSpecialSquares[wallIndex(x, y)] = SquareType.Hole;
	}

	/* getRocket
	 * @return true if there is a rocket at x/y
	 */
	public Boolean getRocket(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)] == SquareType.Rocket;
	}
	
	/* setRocket
	 * Sets the rocket at x/y to rocket. Will not clear a hole
	 */
	public void setRocket(int x, int y, Boolean rocket) {
		if(rocket)
			mSpecialSquares[wallIndex(x, y)] = SquareType.Rocket;
		else if(mSpecialSquares[wallIndex(x, y)] == SquareType.Rocket)
				mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
	}
	
	/* toggleRocket
	 * Toggles the rocket at x/y. If a hole is present it changes it to a rocket 
	 */
	public void toggleRocket(int x, int y) {
		if(mSpecialSquares[wallIndex(x, y)] == SquareType.Rocket)
			mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
		else
			mSpecialSquares[wallIndex(x, y)] = SquareType.Rocket;
	}
	
	/* setArrow
	 * Sets the grid square to a certain direction. Clears if invalid passed in
	 */
	public void setArrow(int x, int y, Direction direction) {
		switch(direction)
		{
		case North:
			mSpecialSquares[wallIndex(x, y)] = SquareType.NorthArrow;
			break;
		case West:
			mSpecialSquares[wallIndex(x, y)] = SquareType.WestArrow;
			break;
		case South:
			mSpecialSquares[wallIndex(x, y)] = SquareType.SouthArrow;
			break;
		case East:
			mSpecialSquares[wallIndex(x, y)] = SquareType.EastArrow;
			break;
		case Invalid:
			switch(mSpecialSquares[wallIndex(x, y)])
			{
			case NorthArrow:
			case WestArrow:
			case SouthArrow:
			case EastArrow:
				mSpecialSquares[wallIndex(x, y)] = SquareType.Empty;
				break;
			}
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
	
	/* World()
	 * Creates a empty world 12x9 with walls around the edge
	 */
	public World() 
	{
		defaultWalls();
		defaultSpecialSquares();
	}
	
	/* World(input)
	 * Loads a world from specified XML file
	 * @param input an InputStream representing the level  
	 */
	public World(InputStream input) throws IOException
	{
		//FileInputStream level_in =  (FileInputStream)input; 		
		
		try
		{
			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setCoalescing(false);
			dbf.setExpandEntityReferences(false);
			javax.xml.parsers.DocumentBuilder dbuilder = dbf.newDocumentBuilder();
			Document document = dbuilder.parse(input);
			Element root = document.getDocumentElement();
			
			
			loadProperties(root);
			defaultSpecialSquares();
			loadWalls(root);
			loadEntities(root);
			loadSolution(root);
		}
		catch(ParserConfigurationException parse_config_error)
		{
			throw new IOException("Unable to create parser to read XML: " + parse_config_error.getMessage());
		}
		catch(SAXException sax_error)
		{
			throw new IOException("Unable to load level due to SAX exception: " + sax_error.getMessage());
		}
		catch(InvalidParameterException xml_error)
		{
			throw new IOException("Unable to load level due to XML parameter error : " + xml_error.getMessage());
		}
	}
	
	/* Loads size, author and name etc from XML
	 * @param root the document element in the XML level
	 */
	private void loadProperties(Element root) {
		NodeList author_nodes = root.getElementsByTagName("Author");
		if(author_nodes.getLength() >= 1)
		{
			String author = author_nodes.item(0).getFirstChild().getNodeValue();
			if(author != null)
				mLevelAuthor = author;
		}
		NodeList levelname_nodes = root.getElementsByTagName("Name");
		if(levelname_nodes.getLength() >= 1)
		{
			String level_name = levelname_nodes.item(0).getFirstChild().getNodeValue();
			if(level_name != null)
				mLevelName = level_name; 
		}
		NodeList size_nodes = root.getElementsByTagName("Size");
		if(size_nodes.getLength() >= 1)
		{
			Node size_node = size_nodes.item(0);
			NamedNodeMap sizes = size_node.getAttributes();
			Node size_x = sizes.getNamedItem("x");
			Node size_y = sizes.getNamedItem("y");
			if(size_x == null || size_y == null)
			{
				throw new InvalidParameterException("Both x and y must be specified in size");			
			} else
			{
				try
				{
					mWidth = Integer.parseInt(size_x.getNodeValue());
					mHeight = Integer.parseInt(size_y.getNodeValue());
					mWalls = new int[mWidth*mHeight];
				} catch(NumberFormatException nfe)
				{
					throw new InvalidParameterException("Unable to parse x or y in size");
				}
			}
		}
	}
	
	/* Loads walls from XML
	 * @param root the document element in the XML level
	 */	
	private void loadWalls(Element root) {
		NodeList h_list = root.getElementsByTagName("H");
		for(int i = 0; i < h_list.getLength(); i++)
		{			
			Node h_node = h_list.item(i);
			NamedNodeMap wall_position_attr = h_node.getAttributes();
			
			Node pos_x = wall_position_attr.getNamedItem("x");
			Node pos_y = wall_position_attr.getNamedItem("y");
			if(pos_x == null || pos_y == null)
			{
				throw new InvalidParameterException("Both x and y must be specified in wall");			
			} else
			{
				try
				{
					int x = Integer.parseInt(pos_x.getNodeValue());
					int y = Integer.parseInt(pos_y.getNodeValue());
					setNorth(x, y, true);
				} catch(NumberFormatException nfe)
				{
					throw new InvalidParameterException("Unable to parse x or y in wall");
				}
			}
		}
		
		NodeList v_list = root.getElementsByTagName("V");
		for(int i = 0; i < v_list.getLength(); i++)
		{			
			Node v_node = v_list.item(i);
			NamedNodeMap wall_position_attr = v_node.getAttributes();
			
			Node pos_x = wall_position_attr.getNamedItem("x");
			Node pos_y = wall_position_attr.getNamedItem("y");
			if(pos_x == null || pos_y == null)
			{
				throw new InvalidParameterException("Both x and y must be specified in wall");			
			} else
			{
				try
				{
					int x = Integer.parseInt(pos_x.getNodeValue());
					int y = Integer.parseInt(pos_y.getNodeValue());
					setWest(x, y, true);
				} catch(NumberFormatException nfe)
				{
					throw new InvalidParameterException("Unable to parse x or y in wall");
				}
			}
		}
	}
	
	/* Loads entities from XML
	 * @param root the document element in the XML level
	 */
	private void loadEntities(Element root) {
		//Load mice
		NodeList mouse_list = root.getElementsByTagName("Mouse");
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
		NodeList cat_list = root.getElementsByTagName("Cat");
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
		NodeList rocket_list = root.getElementsByTagName("Rocket");
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
		NodeList hole_list = root.getElementsByTagName("Hole");
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
		NodeList arrow_list = root.getElementsByTagName("Arrow");
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
					setArrow(x, y, Direction.valueOf(dir.getNodeValue()));
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
	
	/* defaultWalls()
	 * Sets the default walls around the edge
	 */
	private void defaultWalls()	{
		for(int x = 0; x < mWidth; x++)
		{
			setNorth(x, 0, true);
		}
		for(int y = 0; y < mHeight; y++)
		{
			setWest(0, y, true);
		}
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
	
	/* wallIndex
	 * @param x x coordinate - must be between 0 and width-1
	 * @param y y coordinate - must be between 0 and height-1
	 * @return index into wall array corresponding to (x,y)
	 */
	private int wallIndex(int x, int y)	{
		return y * mWidth + x;
	}

	/* getNorth
	 * Gets the north wall state
	 */
	public boolean getNorth(int x, int y) {
		if(x < 0 || x >= mWidth || y < 0 || y >= mHeight)
			throw new InvalidParameterException("x/y outside valid world area");
		return (mWalls[wallIndex(x, y)] & eNorthWall) != 0;
	}
	/* getWest
	 * Gets the west wall state
	 */
	public boolean getWest(int x, int y) {
		if(x < 0 || x >= mWidth || y < 0 || y >= mHeight)
			throw new InvalidParameterException("x/y outside valid world area");
		return (mWalls[wallIndex(x, y)] & eWestWall) != 0;
	}
	/* getEast
	 * Gets the east wall state
	 */
	public boolean getEast(int x, int y) {
		if(x < 0 || x >= mWidth || y < 0 || y >= mHeight)
			throw new InvalidParameterException("x/y outside valid world area");
		return (mWalls[wallIndex((x + 1) % mWidth, y)] & eWestWall) != 0;
	}
	/* getSouth
	 * Gets the south wall state
	 */
	public boolean getSouth(int x, int y) {
		if(x < 0 || x >= mWidth || y < 0 || y >= mHeight)
			throw new InvalidParameterException("x/y outside valid world area");
		return (mWalls[wallIndex(x, (y + 1) % mHeight)] & eNorthWall) != 0;
	}
	/* getDirection
	 * Gets the wall state for a particular direction
	 */
	public boolean getDirection(int x, int y, Direction direction) {
		if(x < 0 || x >= mWidth || y < 0 || y >= mHeight)
			throw new InvalidParameterException("x/y outside valid world area");
		switch(direction)
		{
		case North:
			return getNorth(x,y);
		case West:
			return getWest(x,y);
		case East:
			return getEast(x,y);
		case South:
			return getSouth(x,y);
		}
		return false;
	}
	
	
	/* setNorth
	 * Sets the north wall state. Achieves this by bit twiddling.
	 */
	public void setNorth(int x, int y, boolean set) {
		if(x < 0 || x >= mWidth || y < 0 || y >= mHeight)
			throw new InvalidParameterException("x/y outside valid world area");
		int wi = wallIndex(x, y);
		mWalls[wi] = (mWalls[wi] & ~eNorthWall) | (set ? eNorthWall : 0);  
	}
	/* setWest
	 * Sets the west wall state. Achieves this by bit twiddling.
	 */
	public void setWest(int x, int y, boolean set) {
		if(x < 0 || x >= mWidth || y < 0 || y >= mHeight)
			throw new InvalidParameterException("x/y outside valid world area");
		int wi = wallIndex(x, y);
		mWalls[wi] = (mWalls[wi] & ~eWestWall) | (set ? eWestWall : 0);	
	}
	/* setEast
	 * Sets the east wall state. Achieves this by bit twiddling.
	 */
	public void setEast(int x, int y, boolean set) {
		if(x < 0 || x >= mWidth || y < 0 || y >= mHeight)
			throw new InvalidParameterException("x/y outside valid world area");
		int wi = wallIndex((x+1) % mWidth, y);
		mWalls[wi] = (mWalls[wi] & ~eWestWall) | (set ? eWestWall : 0);		
	}
	/* setSouth
	 * Sets the south wall state. Achieves this by bit twiddling
	 */ 
	public void setSouth(int x, int y, boolean set) {
		if(x < 0 || x >= mWidth || y < 0 || y >= mHeight)
			throw new InvalidParameterException("x/y outside valid world area");
		int wi = wallIndex(x, (y + 1) % mHeight);
		mWalls[wi] = (mWalls[wi] & ~eNorthWall) | (set ? eNorthWall : 0);		
	}
	
	/* tick
	 * Advances cats, mice & performs collisions
	 * @param timespan the number of milliseconds to advance for
	 */
	public void Tick(int timespan) {
		//Whether or not justDeadX is a good pattern to use I don't know, but it works in C++
		//and works here, so will use
		ArrayList<Walker> justDeadMice = new ArrayList<Walker>();
		ArrayList<Walker> justRescuedMice = new ArrayList<Walker>();
		ArrayList<Walker> justDeadCats = new ArrayList<Walker>();
		
		for (Walker mouse : mLiveMice) {
			mouse.Advance(timespan);
			if(mouse.getWalkerState() == WalkerState.Dead)
				justDeadMice.add(mouse);
			if(mouse.getWalkerState() == WalkerState.Rescued)
				justRescuedMice.add(mouse);
		}
		for (Walker cat : mLiveCats) {
			cat.Advance(timespan);
			if(cat.getWalkerState() == WalkerState.Dead)
				justDeadCats.add(cat);
		}
		mLiveMice.removeAll(justDeadMice);
		mDeadMice.addAll(justDeadMice);
		mLiveMice.removeAll(justRescuedMice);
		mRescuedMice.addAll(justRescuedMice);
		mLiveCats.removeAll(justDeadCats);
		mDeadCats.addAll(justDeadCats);
		
	}
}
