package uk.danishcake.shokorocket.networking;

import java.util.ArrayList;

import uk.danishcake.shokorocket.networking.messages.ArrowPlacementMessage;
import uk.danishcake.shokorocket.networking.messages.CursorPositioningMessage;
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
	int[] mDifficulty = new int[]{0, 0, 0};

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
				mDifficulty[i] = 0;
				break;
			case 'E':
				mDifficulty[i] = 1;
				break;
			case 'M':
				mDifficulty[i] = 2;
				break;
			case 'H':
				mDifficulty[i] = 3;
				break;
			}
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
		messages.addAll(mPendMessages);
		
		for(int i = 0; i < 3; i++){
			switch(mDifficulty[i])
			{
			case 0:
			default:
				break;
			case 1:
			case 2:
			case 3:
				break;
			}
		}


		mMessageStack.add(messages);
		mPendMessages.clear();
	}
}
