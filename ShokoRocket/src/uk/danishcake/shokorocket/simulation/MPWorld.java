package uk.danishcake.shokorocket.simulation;

import java.util.ArrayList;
import org.w3c.dom.Element;

import uk.danishcake.shokorocket.networking.GameSync;
import uk.danishcake.shokorocket.networking.LocalSync;
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
		// TODO Auto-generated method stub

	}
	
	public void Tick(int timespan) {
		//Initialise as a temporary measure
		if(mSync == null){
			mSync = new LocalSync();
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
					
					//Now action the received messages
				}
			}	
		}
		
		//If mCommunicationFrameTime has been reset then simulation is synced and can continue
		if(mCommunicationFrameTime < mCommunicationTimestep)
		{
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
		}
	}
	
	//TODO provide level loading
	//TODO override walkerReachNewGridSquare
}
