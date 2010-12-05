package uk.danishcake.shokorocket.networking.ai;

import uk.danishcake.shokorocket.simulation.MPWorld;
import uk.danishcake.shokorocket.networking.messages.Message;

public abstract class BaseAI {
	protected MPWorld mWorld;
	protected int mPlayerID;
	
	public void setup(MPWorld world, int player_id) {
		mWorld = world
		mPlayerID = player_id;
	}
	public abstract void generateMessages(ArrayList<Message> messages);
}