package uk.danishcake.shokorocket.networking;

import java.util.ArrayList;

import uk.danishcake.shokorocket.networking.messages.ArrowPlacementMessage;
import uk.danishcake.shokorocket.networking.messages.Message;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.MPWorld;

/**
 * LocalSync is a simplistic multiplayer connection that is all local
 * It provides indications of several local clients
 * @author Edward Woolhouse
 */
public class LocalSync extends GameSync {
	
	private MPWorld mWorld;
	ArrayList<Message> mPendMessages = new ArrayList<Message>();

	public LocalSync(MPWorld world) {
		mWorld = world;
	}
	
	@Override
	public void Connect(String dest) {
		// TODO parse dest for client count/difficulty etc 
		
	}

	@Override
	public void SendFrameEnd() {
		mLocalFrame++;
		mSyncedFrame = mLocalFrame - 1;
		generateMessages();
	}
	
	@Override
	public void sendMessage(Message message) {
		mPendMessages.add(message);
	}
	
	private void generateMessages()	{
		ArrayList<Message> messages = new ArrayList<Message>();
		messages.addAll(mPendMessages);
		//Message m = new ArrowPlacementMessage(1, 1, Direction.East);
		//m.setCommon(1, 1);
		//messages.add(m);

		mMessageStack.add(messages);
		mPendMessages.clear();
	}
}
