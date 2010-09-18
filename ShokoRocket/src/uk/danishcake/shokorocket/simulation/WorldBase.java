package uk.danishcake.shokorocket.simulation;

import java.security.InvalidParameterException;

public class WorldBase {
	protected int mWidth = 12;
	protected int mHeight = 9;
	protected String mLevelName = "Default";
	protected String mLevelAuthor = "Unknown";
	
	protected final int eWestWall = 1;
	protected final int eNorthWall = 2;
	protected int[] mWalls = new int[mWidth*mHeight];
	
	protected String mIdentifier = "";
	protected String mFilename = "";
	
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
	
	/**
	 * Sets the level author
	 * @param author The name of the author
	 */
	public void setAuthor(String author) {
		mLevelAuthor = author;
	}
	
	/* getLevelName
	 * @return the name of the level
	 */	
	public String getLevelName() {
		return mLevelName;
	}
	
	/**
	 * Sets the level name
	 * @param name The name of the level
	 */
	public void setLevelName(String name) {
		mLevelName = name;
	}
	
	/**
	 * Sets the filename. This is used to determine if a file has already been saved
	 * @param name The filename to store. 
	 */
	public void setFilename(String name) {
		mFilename = name;
	}
	
	/**
	 * Gets the filename. If it has not been set then it returns ""
	 * @return The filename set by setFilename
	 */
	public String getFilename() {
		return mFilename;
	}

	/**
	 * Gets an identifier for this level 
	 * @return
	 */
	public String getIdentifier() {
		return mIdentifier;
	}
	
	/**
	 * Sets the identifier for this level. Planned use is the filename
	 * @param id
	 */
	public void setIdentifier(String id) {
		mIdentifier = id;
	}
	
	
	public WorldBase(int width, int height)
	{
		mWidth = width;
		mHeight = height;
		mWalls = new int[mWidth * mHeight];
		defaultWalls();
	}
	
	public WorldBase()
	{
		defaultWalls();
	}
	
	/* wallIndex
	 * @param x x coordinate - must be between 0 and width-1
	 * @param y y coordinate - must be between 0 and height-1
	 * @return index into wall array corresponding to (x,y)
	 */
	protected int wallIndex(int x, int y)	{
		return y * mWidth + x;
	}
	
	/* defaultWalls()
	 * Sets the default walls around the edge
	 */
	protected void defaultWalls()	{
		for(int x = 0; x < mWidth; x++)
		{
			setNorth(x, 0, true);
		}
		for(int y = 0; y < mHeight; y++)
		{
			setWest(0, y, true);
		}
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
	
	public void toggleDirection(int x, int y, Direction direction) {
		switch(direction)
		{
		case North:
			setNorth(x, y, !getDirection(x, y, direction));
			break;
		case South:
			setSouth(x, y, !getDirection(x, y, direction));
			break;
		case East:
			setEast(x, y, !getDirection(x, y, direction));
			break;
		case West:
			setWest(x, y, !getDirection(x, y, direction));
			break;
		}
	}

}
