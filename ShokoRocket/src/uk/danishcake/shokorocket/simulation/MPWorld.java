package uk.danishcake.shokorocket.simulation;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.danishcake.shokorocket.networking.GameSync;
import uk.danishcake.shokorocket.networking.LocalSync;
import uk.danishcake.shokorocket.networking.messages.ArrowClearMessage;
import uk.danishcake.shokorocket.networking.messages.ArrowPlacementMessage;
import uk.danishcake.shokorocket.networking.messages.CursorPositioningMessage;
import uk.danishcake.shokorocket.networking.messages.Message;
import uk.danishcake.shokorocket.simulation.Walker.WalkerState;
import uk.danishcake.shokorocket.simulation.Walker.WalkerType;

/* MPWorld
 * Represents a world designed to run in lockstep with other instances
 * Walkers are not preserved past death, but rather thrown away (or recycled?)
 */
public class MPWorld extends WorldBase {
	private enum MPGameState
	{
		Countdown, InPlay, GoldSelect, MouseMania, CatMania, SpeedUp, SlowDown, Finished 
	}

	private GameSync mSync = null;
	private ArrayList<Walker> mLiveMice = new ArrayList<Walker>();
	private ArrayList<Walker> mLiveCats = new ArrayList<Walker>();
	private ArrayList<Walker> mDeadMice = new ArrayList<Walker>();
	private ArrayList<Walker> mRescuedMice = new ArrayList<Walker>();
	private ArrayList<Walker> mDeadCats = new ArrayList<Walker>();
	private static final int REAL_FIXED_TIMESTEP = 20;
	private static final int FIXED_TIMESTEP = 100;
	private static final int COMM_RATIO = 5;
	private int mCommunicationFrameTime = 0;
	private int mSubFrame = 0;
	private List<Message> mMessages = new ArrayList<Message>();
	private MPSquareType[] mSpecialSquares = new MPSquareType[mWidth*mHeight];
	private Vector2i[] mCursorPositions = new Vector2i[4];
	private int[] mScores = new int[4];
	private int mPlayerID = 0;

	private int mSpawnMax = SPAWN_DEFAULT;
	private int mSpawnInterval = 400;
	private int mSpawnTimer = 1500;
	private Random mRandom;
	private int mTimer = 0;
	private static final int SPAWN_DEFAULT = 20;

	private MPGameState mGameState = MPGameState.Countdown;
	private int mStateTimer = COUNTDOWN_TIME;
	private int mStateTimerLTV = mStateTimer;
	private static final int COUNTDOWN_TIME = 3000;
	private static final int GOLDSELECT_TIME = 1500;
	private static final int MOUSEMANIA_TIME = 10000;
	private static final int CATMANIA_TIME = 10000;
	private static final int SPEEDUP_TIME = 10000;
	private static final int SLOWDOWN_TIME = 10000;
	private static final int FINISHED_TIME = 4000;
	
	private String mConnectString = "HHH";
	
	public OnGuiMessage mGUIMessage = null;
	
	/* MPWorld(input)
	 * Loads a world from specified XML file
	 * @param input an InputStream representing the level  
	 */
	public MPWorld(InputStream input, String connect) throws IOException {
		mConnectString = connect;
		LoadFromXML(input);
		//loadSpecific is then called with root element to parse
		for(int i = 0; i < 4; i++)
		{
			mCursorPositions[i] = new Vector2i(-1, -1);
			mScores[i] = 0;
		}
	}
	
	public MPWorld() {
		super();
		mSpecialSquares = new MPSquareType[mWidth * mHeight];
		for(int i = 0; i < mWidth * mHeight; i++){
			mSpecialSquares[i] = new MPSquareType();
			mSpecialSquares[i].square_type = SquareType.Empty;
			mSpecialSquares[i].player_id = -1;
		}
		for(int i = 0; i < 4; i++)
		{
			mCursorPositions[i] = new Vector2i(-1, -1);
			mScores[i] = 0;
		}
	}
	
	public final Vector2i[] getCursorPositions() {
		return mCursorPositions;
	}
	
	public ArrayList<Walker> getLiveMice() {
		return mLiveMice;
	}
	
	public ArrayList<Walker> getLiveCats() {
		return mLiveCats;
	}
	
	public ArrayList<Walker> getDeadMice() {
		return mDeadMice;
	}
	
	public ArrayList<Walker> getDeadCats() {
		return mDeadCats;
	}
	
	public ArrayList<Walker> getRescuedMice() {
		return mRescuedMice;
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
		w.setWorld(this);
		switch (walker_type) {
		case Mouse:
		case MouseGold:
		case MouseSpecial:
			mLiveMice.add(w);
			w.setSpeed(Walker.MouseSpeed);
			break;
		default:
			mLiveCats.add(w);
			w.setSpeed(Walker.CatSpeed);
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
			mSpecialSquares[i].player_id = -1;
		}
		loadEntities(root);
	}
	
	/**
	 * Loads entities from XML
	 * @param root the document element of the XML
	 */
	private void loadEntities(Element root) {
		
		//Load rockets (player locations)
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
		
		//Load spawners
		NodeList spawner_list = root.getElementsByTagName("Spawner");
		for(int i = 0; i < spawner_list.getLength(); i++)
		{
			Node spawner = spawner_list.item(i);
			NamedNodeMap spawner_attr = spawner.getAttributes();
			Node pos_x = spawner_attr.getNamedItem("x");
			Node pos_y = spawner_attr.getNamedItem("y");
			Node dir = spawner_attr.getNamedItem("d");
			if(pos_x == null || pos_y == null || dir == null)
			{
				throw new InvalidParameterException("Both x, y & d must be specified in Spawner");
			} else
			{
				try
				{
					int x = Integer.parseInt(pos_x.getNodeValue());
					int y = Integer.parseInt(pos_y.getNodeValue());
					Direction direction = Direction.valueOf(dir.getNodeValue());
					
					setSpawner(x, y, direction);
				} catch(NullPointerException nfe)
				{
					throw new InvalidParameterException("Both x, y and id must be specified in Spawner");
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
	
	public void Tick(int timespan) {
		//Initialise as a temporary measure
		if(mSync == null){
			mSync = new LocalSync(this);
			mSync.Connect(mConnectString);
			mPlayerID = mSync.getClientID();
			mRandom = new Random(0);
		}
		mStateTimerLTV = mStateTimer;
		if(mStateTimer > 0)
		{
			mStateTimer -= REAL_FIXED_TIMESTEP; //20ms, FIXED_TIMESTEP is 100ms
			if(mStateTimer <= 0)
			{
				if(mGameState == MPGameState.Countdown && mGUIMessage != null)
				{
					mGUIMessage.show("GO!", 1200);
				}
				mGameState = MPGameState.InPlay;
			}
		}
		mSpawnMax = SPAWN_DEFAULT;
		switch(mGameState)
		{
		case MouseMania:
			mSpawnMax = 2 * SPAWN_DEFAULT;
		case CatMania:
		case InPlay:
			timespan = FIXED_TIMESTEP;
			break;
		case Countdown:
			if(mStateTimer < 3000 && mStateTimerLTV >= 3000)
			{
				if(mGUIMessage != null)
				{
					mGUIMessage.show("3", 800);
				}
			}
			if(mStateTimer < 2000 && mStateTimerLTV >= 2000)
			{
				if(mGUIMessage != null)
				{
					mGUIMessage.show("2", 800);
				}
			}
			if(mStateTimer < 1000 && mStateTimerLTV >= 1000)
			{
				if(mGUIMessage != null)
				{
					mGUIMessage.show("1", 800);
				}
			}
		case GoldSelect:
		case Finished:
		default:
			timespan = 0;
			break;
		case SpeedUp:
			timespan = FIXED_TIMESTEP * 2;
			break;
		case SlowDown:
			timespan = FIXED_TIMESTEP / 1;
			break;
		}

		//Freeze until
		mCommunicationFrameTime += FIXED_TIMESTEP;
		if(mCommunicationFrameTime >= FIXED_TIMESTEP * COMM_RATIO)
		{
			if(mCommunicationFrameTime == FIXED_TIMESTEP * COMM_RATIO)
			{
				mSync.SendFrameEnd();
			}
			//Allow to advance once sync frame is within 2 of sent frame
			if(mSync.getReadyFrame() >= mSync.getSentFrame() - 2)
			{
				mCommunicationFrameTime = 0;
				mSubFrame = 0;
				mMessages = mSync.popMessages();
			}
		}
		
		//If mCommunicationFrameTime has been reset then simulation is synced and can continue
		if(mCommunicationFrameTime < FIXED_TIMESTEP * COMM_RATIO)
		{
			mTimer += timespan;
			if(mSpawnTimer <= mTimer && mLiveMice.size() + mLiveCats.size() < mSpawnMax)
			{
				for(int x = 0; x < mWidth; x++)
				{
					for(int y = 0; y < mHeight; y++)
					{
						Direction spawn_dir = getSpawner(x, y);
						if(spawn_dir != Direction.Invalid)
						{
							WalkerType wt;
							int rn = mRandom.nextInt(60);
							if(rn == 0)
								wt = WalkerType.MouseSpecial;
							else if(rn == 1)
								wt = WalkerType.MouseGold;
							else if(rn == 2)
								wt = WalkerType.Cat;
							else
								wt = WalkerType.Mouse;
							addWalker(x, y, spawn_dir, wt);
						}
					}
				}
				mSpawnTimer = mTimer + mSpawnInterval;
			}
			
			//Action any messages this frame
			if(mMessages.size() > 0)
			{
				Message next = mMessages.get(0); 
				while(next != null && next.sub_frame_id <= mSubFrame)
				{
					handleMessage(next);
					
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
			ArrayList<Walker> expiredWalkers = new ArrayList<Walker>();
			
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
				if(mouse.getDeathTime() > 5000)
					expiredWalkers.add(mouse);
			}
			for(Walker mouse : mRescuedMice)
			{
				mouse.DeathTick(timespan);
				if(mouse.getDeathTime() > 5000)
					expiredWalkers.add(mouse);
			}
			for(Walker cat : mDeadCats)
			{
				cat.DeathTick(timespan);
				if(cat.getDeathTime() > 5000)
					expiredWalkers.add(cat);
			}
			//TODO remove dead walkers once animation complete
			mDeadMice.removeAll(expiredWalkers);
			mRescuedMice.removeAll(expiredWalkers);
			mDeadCats.removeAll(expiredWalkers);
			mSubFrame++;
		}
	}
	
	/**
	 * Called when walker reaches a new gird square and must turn/die
	 */
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
			int player = getPlayer(x, y);
			//TODO score increment
			switch(walker.getWalkerType())
			{
			case Mouse:
				mScores[player]++;
				break;
			case MouseGold:
				mScores[player] += 50;
				break;
			case MouseSpecial:
				//TODO special mice
				break;
			case Cat:
				mScores[player] = (mScores[player]* 2) / 3;
				break;
			}
			if(mScores[player] < 0) mScores[player] = 0;
			if(mScores[player] > 999) mScores[player] = 999;
			walker.setWalkerState(WalkerState.Rescued);
		}
		//Arrows
		Direction arrow_direction = square.toArrowDirection(); 
		if(arrow_direction != Direction.Invalid)
		{
			if(arrow_direction == Turns.TurnAround(d) && walker.getWalkerType() == WalkerType.Cat)
			{
				SquareType reduced = square.DiminishMP();
				setSpecialSquare(x, y, reduced, getPlayer(x, y));
			}
			walker.setDirection2(arrow_direction);
		}
		/* Now interact with walls */
		super.walkerReachNewSquare(walker, x, y, walker.getDirection());
	}
	
	private void handleMessage(Message next_message) {
		//Action message
		switch(next_message.message_type)
		{
		case Message.MESSAGE_CURSOR_POSITION:
			{
				CursorPositioningMessage message = (CursorPositioningMessage)next_message;
				mCursorPositions[message.user_id].x = message.x;
				mCursorPositions[message.user_id].y = message.y;
			}
			break;
		case Message.MESSAGE_ARROW_PLACEMENT:
			{
				ArrowPlacementMessage message = (ArrowPlacementMessage)next_message;
				if(getArrowCount(message.user_id) < 3)
					toggleArrow(message.x, message.y, message.direction, message.user_id, true);
				else
					toggleArrow(message.x, message.y, message.direction, message.user_id, false);
			}
			break;
		case Message.MESSAGE_ARROW_CLEAR:
			{
				clearPlayerArrows(next_message.user_id);
			}
			break;
		}
	}

	
	/**
	 * Toggles the hole at (x,y)
	 */
	public void toggleHole(int x, int y) {
		mSpecialSquares[wallIndex(x, y)].square_type = getHole(x, y) ? SquareType.Hole : SquareType.Empty;
	}
	
	/**
	 * Sets the hole at (x,y)
	 */
	public void setHole(int x, int y, boolean hole) {
		mSpecialSquares[wallIndex(x, y)].square_type = hole ? SquareType.Hole : SquareType.Empty; 
	}
	
	/**
	 * @return true if a hole is at (x,y)
	 */
	public boolean getHole(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)].square_type == SquareType.Hole;
	}
	
	/**
	 * Toggles spawner at (x,y). If already one present with same direction then clears.
	 */
	public void toggleSpawner(int x, int y, Direction direction) {
		mSpecialSquares[wallIndex(x, y)].square_type = getSpawner(x, y) != direction ? direction.toSpawner() : SquareType.Empty;
	}
	
	/**
	 * Sets the spawner at (x,y)
	 */
	public void setSpawner(int x, int y, Direction direction) {
		mSpecialSquares[wallIndex(x, y)].square_type = direction.toSpawner(); 
	}
	
	/**
	 * @return Direction of spawner at (x,y). Direction.Invalid if empty
	 */
	public Direction getSpawner(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)].square_type.toSpawnerDirection();
	}
	
	public void clearPlayerArrows(int player)
	{
		for(int y = 0; y < mHeight; y++)
		{
			for(int x = 0; x < mWidth; x++)
			{
				if(getArrow(x, y) != Direction.Invalid && getPlayer(x, y) == player)
				{
					setArrow(x, y, Direction.Invalid, -1);
				}
			}
		}
	}
	
	/**
	 * Toggles arrow at (x,y) for player p. If already one present with same direction  
	 * and player then clears. Different players do not overwrite existing arrows
	 */
	public void toggleArrow(int x, int y, Direction direction, int player, boolean allow_new) {
		SquareType square_type = getSpecialSquare(x, y);
		if(square_type == SquareType.Hole || square_type == SquareType.Rocket || 
		   square_type == SquareType.SouthSpawner || square_type == SquareType.NorthSpawner ||
		   square_type== SquareType.WestSpawner || square_type == SquareType.EastSpawner)
			return;
		Direction cur_dir = getArrow(x, y);
		if(cur_dir == direction && getPlayer(x, y) == player) //Allow clear
			setArrow(x, y, Direction.Invalid, player);
		else if(getPlayer(x, y) == player) //Allow change
			setArrow(x, y, direction, player);
		else if(cur_dir == Direction.Invalid && allow_new) //Allow new
			setArrow(x, y, direction, player);
	}
	
	/**
	 * Sets the arrow at (x,y) for player p. Different players do not overwrite existing
	 * arrows
	 */
	public void setArrow(int x, int y, Direction direction, int player) {
		mSpecialSquares[wallIndex(x, y)].square_type = direction.toArrow();
		mSpecialSquares[wallIndex(x, y)].player_id = direction == Direction.Invalid ? -1 : player;
	}
	
	/**
	 * @return Direction of arrow at (x,y). Direction.Invalid if empty
	 */
	public Direction getArrow(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)].square_type.toArrowDirection();
	}
	
	public int getArrowCount(int id) {
		int count = 0;
		for(int y = 0; y < mHeight; y++)
		{
			for(int x = 0; x < mWidth; x++)	
			{
				if(getArrow(x, y) != Direction.Invalid && getPlayer(x, y) == id)
					count++;
			}
		}
		return count;
	}
	
	/**
	 * @return ID of player square at (x,y). If not a spawner or arrow square
	 * behaviour undefined
	 */
	public int getPlayer(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)].player_id;
	}
	
	
	/**
	 * Sets a rocket at (x,y)
	 */
	public void setRocket(int x, int y, int player) {
		mSpecialSquares[wallIndex(x, y)].square_type = SquareType.Rocket;
		mSpecialSquares[wallIndex(x, y)].player_id = player;
	}
	
	/**
	 * @return true if a rocket is at (x,y)
	 */
	public boolean getRocket(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)].square_type == SquareType.Rocket;
	}
	
	/* getSpecialSquare
	 * Gets the square type at x/y
	 * @return the square type at x/y
	 */
	public SquareType getSpecialSquare(int x, int y) {
		return mSpecialSquares[wallIndex(x, y)].square_type;
	}
	
	public void setSpecialSquare(int x, int y, SquareType square_type, int player_id) {
		mSpecialSquares[wallIndex(x, y)].square_type = square_type;
		switch(square_type)
		{
		case EastArrow:
		case EastHalfArrow:
		case NorthArrow:
		case NorthHalfArrow:
		case WestArrow:
		case WestHalfArrow:
		case SouthArrow:
		case SouthHalfArrow:
		case Rocket:
			mSpecialSquares[wallIndex(x, y)].player_id = player_id;
			break;
		default:
			mSpecialSquares[wallIndex(x, y)].player_id = -1;
			break;
		}
	}
	
	public int getPlayerID() {
		return mPlayerID;
	}
	
	public final int[] getPlayerScores() {
		return mScores;
	}
	
	/**
	 * Sends a message to other players to place an arrow
	 */
	public void arrowPlacement(int x, int y, Direction d)
	{
		mSync.sendMessage(new ArrowPlacementMessage(x, y, d));
	}
	
	/**
	 * Clears all of this players arrows
	 */
	public void clearArrows() {
		mSync.sendMessage(new ArrowClearMessage());
	}
	
	/**
	 * Moves the cursor for this player - purely aesthetic
	 */
	public void cursorPlacement(int x, int y) {
		mSync.sendMessage(new CursorPositioningMessage(x, y));
	}
}
