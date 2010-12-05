package uk.danishcake.shokorocket.networking;

import java.util.ArrayList;
import uk.danishcake.shokorocket.networking.ai.BaseAI;
import uk.danishcake.shokorocket.networking.ai.BasicAI;
import uk.danishcake.shokorocket.networking.messages.Message;
import uk.danishcake.shokorocket.simulation.MPWorld;

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
				mAI[i] = null;
				break;
			case 'E':
				mAI[i] = new BasicAI();
				break;
			case 'M':
				mAI[i] = new BasicAI(); //TODO harder AI
				break;
			case 'H':
				mAI[i] = new BasicAI(); //TODO harder AI
				break;
			}
			if(mAI[i] != null)
				mAI[i].setup(mWorld, i + 1);
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
			if(mAI[i] != null)
				mAI[i].generateMessages(messages);
		}
		mMessageStack.add(messages);
		mPendMessages.clear();
	}
}
