package uk.danishcake.shokorocket.simulation;

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
}
