package uk.danishcake.shokorocket.simulation;

import java.util.ArrayList;
import org.w3c.dom.Element;

import uk.danishcake.shokorocket.simulation.Walker.WalkerType;

/* MPWorld
 * Represents a world designed to run in lockstep with other instances
 * Walkers are not preserved past death, but rather thrown away (or recycled?)
 */
public class MPWorld extends WorldBase {

	protected ArrayList<Walker> mMice = new ArrayList<Walker>();
	protected ArrayList<Walker> mCats = new ArrayList<Walker>();
	
	public ArrayList<Walker> getMice() {
		return mMice;
	}
	
	public ArrayList<Walker> getCats() {
		return mCats;
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
			mMice.add(w);
			break;
		default:
			mCats.add(w);
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
	
	
}
