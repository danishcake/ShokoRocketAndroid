package uk.danishcake.shokorocket.networking;

import java.util.ArrayList;
import java.util.Iterator;

import uk.danishcake.shokorocket.networking.messages.ArrowClearMessage;
import uk.danishcake.shokorocket.networking.messages.ArrowPlacementMessage;
import uk.danishcake.shokorocket.networking.messages.CursorPositioningMessage;
import uk.danishcake.shokorocket.networking.messages.Message;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.MPWorld;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.simulation.Walker;

/**
 * LocalSync is a simplistic multiplayer connection that is all local
 * It provides indications of several local clients
 * @author Edward Woolhouse
 */
public class LocalSync extends GameSync {
	
	private MPWorld mWorld;
	ArrayList<Message> mPendMessages = new ArrayList<Message>();
	BaseAI[] mAI = new BaseAI[3];

	public LocalSync(MPWorld world) {
		mWorld = world;
	}
	
	@Override
	public void Connect(String dest) {
		// TODO parse dest for client count/difficulty etc 
		//Format [0EMH][0EMH][0EMH]
		for(int i = 0; i < 3; i++)
		{
			char player = dest.charAt(i);
			switch(player)
			{
			case '0':
			default:
				mAI[player] = null;
				break;
			case 'E':
				mAI[player] = new BasicAI();
				break;
			case 'M':
				mAI[player] = new BasicAI(); //TODO harder AI
				break;
			case 'H':
				mAI[player] = new BasicAI(); //TODO harder AI
				break;
			}
			if(mAI[player] != null)
				mAI[player].setup(mWorld, i + 1);
		}
	}

	@Override
	public void SendFrameEnd() {
		mLocalFrame++;
		mSyncedFrame = mLocalFrame - 1;
		generateMessages();
	}
	
	@Override
	public void sendMessage(Message message) {
		message.user_id = mClientID;
		mPendMessages.add(message);
	}
	
	private void generateMessages()	{
		ArrayList<Message> messages = new ArrayList<Message>();
		messages.addAll(mPendMessages); /* Add the users pending messages */

		/* Perform AI work, generate responses */
		for(int i = 0; i < 3; i++){
			mAi[i].generateMessages(messages);
		}
		mMessageStack.add(messages);
		mPendMessages.clear();
	}
}
