package uk.danishcake.shokorocket.simulation;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.danishcake.shokorocket.networking.GameSync;
import uk.danishcake.shokorocket.networking.LocalSync;
import uk.danishcake.shokorocket.networking.messages.ArrowPlacementMessage;
import uk.danishcake.shokorocket.networking.messages.Message;
import uk.danishcake.shokorocket.simulation.Walker.WalkerState;
import uk.danishcake.shokorocket.simulation.Walker.WalkerType;

/* MPWorld
 * Represents a world designed to run in lockstep with other instances
 * Walkers are not preserved past death, but rather thrown away (or recycled?)
 */
public class MPWorld extends WorldBase {
	private GameSync mSync = null;
	private ArrayList<Walker> mLiveMice = new ArrayList<Walker>();
	private ArrayList<Walker> mLiveCats = new ArrayList<Walker>();
	private ArrayList<Walker> mDeadMice = new ArrayList<Walker>();
	private ArrayList<Walker> mRescuedMice = new ArrayList<Walker>();
	private ArrayList<Walker> mDeadCats = new ArrayList<Walker>();
	private int mFixedTimestep = 20;
	private int mCommunicationTimestep = 100;
	private int mCommunicationFrameTime = 0;
	private int mSubFrame = 0;
	private List<Message> mMessages;
	private MPSquareType[] mSpecialSquares = new MPSquareType[mWidth*mHeight];
	
	/* MPWorld(input)
	 * Loads a world from specified XML file
	 * @param input an InputStream representing the level  
	 */
	public MPWorld(InputStream input) throws IOException {
		LoadFromXML(input);
		//loadSpecific is then called with root element to parse
	}
	
	public MPWorld() {
		super();
		mSpecialSquares = new MPSquareType[mWidth * mHeight];
		for(int i = 0; i < mWidth * mHeight; i++){
			mSpecialSquares[i] = new MPSquareType();
			mSpecialSquares[i].square_type = SquareType.Empty;
			mSpecialSquares[i].player_id = 0;
		}
	}
	
	public ArrayList<Walker> getMice() {
		return mLiveMice;
	}
	
	public ArrayList<Walker> getCats() {
		return mLiveCats;
	}
	
	/* addWalker
	 * Creates a walker at the given position, with the given direction 
	 */
	
	public void addWalker(int x, int y, Direction d, WalkerType walker_type)
	{
		Walker w = new Walker();
		w.setWalkerType(walker_type);
		w.setPosition(new Vector2i(x, y));
		w.setDirection(d);
		switch (walker_type) {
		case Mouse:
		case MouseGold:
		case MouseSpecial:
			mLiveMice.add(w);
			break;
		default:
			mLiveCats.add(w);
			break;
		}
	}
	
	/**
	 * Loads XML specific to the multiplayer modes - eg player locations, spawners etc
	 */
	@Override
	protected void loadSpecific(Element root) {
		mSpecialSquares = new MPSquareType[mWidth * mHeight];
		for(int i = 0; i < mWidth * mHeight; i++){
			mSpecialSquares[i] = new MPSquareType();
			mSpecialSquares[i].square_type = SquareType.Empty;
			mSpecialSquares[i].player_id = 0;
		}
		loadEntities(root);
	}
	
	/**
	 * Loads entities from XML
	 * @param root the document element of the XML
	 */
	private void loadEntities(Element root) {
		NodeList player_list = root.getElementsByTagName("PlayerRocket");
		for(int i = 0; i < player_list.getLength(); i++)
		{
			Node player_rocket = player_list.item(i);
			NamedNodeMap rocket_attr = player_rocket.getAttributes();
			Node pos_x = rocket_attr.getNamedItem("x");
			Node pos_y = rocket_attr.getNamedItem("y");
			Node player_id = rocket_attr.getNamedItem("id");
			if(pos_x == null || pos_y == null || player_id == null)
			{
				throw new InvalidParameterException("Both x, y & id must be specified in PlayerRocket");
			} else
			{
				try
				{
					int x = Integer.parseInt(pos_x.getNodeValue());
					int y = Integer.parseInt(pos_y.getNodeValue());
					int id = Integer.parseInt(player_id.getNodeValue());
					
					setRocket(x, y, id);
				} catch(NullPointerException nfe)
				{
					throw new InvalidParameterException("Both x, y and id must be specified in PlayerRocket");
				}			
			}
		}
	}
	
	public void Tick(int timespan) {
		//Initialise as a temporary measure
		if(mSync == null){
			mSync = new LocalSync(this);
			mSync.Connect("3");
		}
		

		
		//Ignore timespan, all frames are mFixedTimestep long
		timespan = mFixedTimestep;
		//Freeze until
		mCommunicationFrameTime += timespan;
		if(mCommunicationFrameTime >= mCommunicationTimestep)
		{
			if(mCommunicationFrameTime == mCommunicationTimestep)
			{
				mSync.SendFrameEnd();
				
				//Allow to advance once sync frame is within 2 of sent frame
				if(mSync.getReadyFrame() >= mSync.getSentFrame() - 2)
				{
					mCommunicationFrameTime = 0;
					mSubFrame = 0;
					mMessages = mSync.popMessages();
				}
			}	
		}
		
		//If mCommunicationFrameTime has been reset then simulation is synced and can continue
		if(mCommunicationFrameTime < mCommunicationTimestep)
		{
			//Action any messages this frame
			if(mMessages.size() > 0)
			{
				Message next = mMessages.get(0); 
				while(next != null && next.sub_frame_id <= mSubFrame)
				{
					//Action message
					switch(next.message_type)
					{
					case Message.MESSAGE_CURSOR_POSITION:
						{
							
						}
						break;
					case Message.MESSAGE_ARROW_PLACEMENT:
						{
							ArrowPlacementMessage message = (ArrowPlacementMessage)next;
							toggleArrow(message.x, message.y, message.direction, message.user_id);
						}
						break;
					}
					
					
					mMessages.remove(0);
					if(mMessages.size() == 0)
						next = null;
					else
						next = mMessages.get(0);
				}
			}
			
			ArrayList<Walker> justDeadMice = new ArrayList<Walker>();
			ArrayList<Walker> justRescuedMice = new ArrayList<Walker>();
			ArrayList<Walker> justDeadCats = new ArrayList<Walker>();
			
			for(Walker mouse : mLiveMice)
			{
				mouse.Advance(timespan);
				if(mouse.getWalkerState() == WalkerState.Dead && !justDeadMice.contains(mouse))
				{
					justDeadMice.add(mouse);
				}
				if(mouse.getWalkerState() == WalkerState.Rescued && !justRescuedMice.contains(mouse))
				{
					justRescuedMice.add(mouse);
				}
			}
			for (Walker cat : mLiveCats) {
				cat.Advance(timespan);
				if((cat.getWalkerState() == WalkerState.Dead || 
					cat.getWalkerState() == WalkerState.Rescued) && !justDeadCats.contains(cat))
				{
					justDeadCats.add(cat);
				}
			}
			
			for(Walker cat : mLiveCats) {
				for(Walker mouse : mLiveMice) {
					//Calculate distance
					if(checkCollision(cat, mouse) && !justDeadMice.contains(mouse))
					{
						justDeadMice.add(mouse);
					}
				}
			}
			
			mLiveMice.removeAll(justDeadMice);
			mDeadMice.addAll(justDeadMice);
			mLiveMice.removeAll(justRescuedMice);
			mRescuedMice.addAll(justRescuedMice);
			mLiveCats.removeAll(justDeadCats);
			mDeadCats.addAll(justDeadCats);
			
			for(Walker mouse : mDeadMice)
			{
				mouse.DeathTick(timespan);
			}
			for(Walker mouse : mRescuedMice)
			{
				mouse.DeathTick(timespan);
			}
			for(Walker cat : mDeadCats)
			{
				cat.DeathTick(timespan);
			}
			mSubFrame++;
		}
	}
	
	//TODO provide level loading
	//TODO override walkerReachNewGridSquare
	//TODO send input messages
	
	/**
	 * Toggles the hole at (x,y)
	 */
	void toggleHole(int x, int y) {
		mSpecialSquares[wallIndex(x, y)].square_type = getHole(x, y) ? SquareType.Hole : SquareType.Empty;
	}
	
	/**
	 * Sets the hole at (x,y)
	 */
	void setHole(int x, int y, boolean hole) {
		mSpecialSquares[wallIndex(x, y)].square_type = hole ? SquareType.Hole : SquareType.Empty; 
	}
	
	/**
	 * @return true if a hole is at (x,y)
	 */
	boolean getHole(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)].square_type == SquareType.Hole;
	}
	
	/**
	 * Toggles spawner at (x,y). If already one present with same direction then clears.
	 */
	void toggleSpawner(int x, int y, Direction direction) {
		mSpecialSquares[wallIndex(x, y)].square_type = getSpawner(x, y) != direction ? direction.toSpawner() : SquareType.Empty;
	}
	
	/**
	 * Sets the spawner at (x,y)
	 */
	void setSpawner(int x, int y, Direction direction) {
		mSpecialSquares[wallIndex(x, y)].square_type = direction.toSpawner(); 
	}
	
	/**
	 * @return Direction of spawner at (x,y). Direction.Invalid if empty
	 */
	Direction getSpawner(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)].square_type.toSpawnerDirection();
	}
	
	/**
	 * Toggles arrow at (x,y) for player p. If already one present with same direction  
	 * and player then clears. Different players do not overwrite existing arrows
	 */
	void toggleArrow(int x, int y, Direction direction, int player) {
		if(getArrow(x, y) == Direction.Invalid || getPlayer(x, y) == player)
			setArrow(x, y, direction, player);
	}
	
	/**
	 * Sets the arrow at (x,y) for player p. Different players do not overwrite existing
	 * arrows
	 */
	void setArrow(int x, int y, Direction direction, int player) {
		mSpecialSquares[wallIndex(x, y)].square_type = direction.toArrow();
		mSpecialSquares[wallIndex(x, y)].player_id = player;
	}
	
	/**
	 * @return Direction of arrow at (x,y). Direction.Invalid if empty
	 */
	Direction getArrow(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)].square_type.toArrowDirection();
	}
	
	/**
	 * @return ID of player square at (x,y). If not a spawner or arrow square
	 * behaviour undefined
	 */
	int getPlayer(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)].player_id;
	}
	
	
	/**
	 * Sets a rocket at (x,y)
	 */
	void setRocket(int x, int y, int player) {
		mSpecialSquares[wallIndex(x, y)].square_type = SquareType.Rocket;
		mSpecialSquares[wallIndex(x, y)].player_id = player;
	}
	
	/**
	 * @return true if a rocket is at (x,y)
	 */
	boolean getRocket(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)].square_type == SquareType.Rocket;
	}
	
	
}
