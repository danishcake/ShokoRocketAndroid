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
	int[] mDifficulty = new int[]{0, 0, 0};
	
	//AI stuff
	private int[] mRowCount;
	private int[] mColCount;
	private int mBestRow;
	private int mBestCol;
	Vector2i[] mRocketPosition = new Vector2i[3];
	private int[] mActTimer = {0, 0, 0};
	private int[] mAIRate = {40, 25, 10};

	public LocalSync(MPWorld world) {
		mWorld = world;
		
		mRowCount = new int[mWorld.getHeight()];
		mColCount = new int[mWorld.getWidth()];
		for(int y = 0; y < mWorld.getHeight(); y++)
		{
			for(int x = 0; x < mWorld.getWidth(); x++)
			{
				if(mWorld.getRocket(x, y)){
					int player = mWorld.getPlayer(x, y);
					if(player > 0)
						mRocketPosition[player-1] = new Vector2i(x, y);
				}
			}
		}
		
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
		
		
		scan_sweep();
		for(int i = 0; i < 3; i++){
			int player_id = i + 1;
			
			switch(mDifficulty[i])
			{
			case 0:
			default:
				break;
			case 1:
			case 2:
			case 3:
				mActTimer[i]++;
				if(mActTimer[i] < mAIRate[mDifficulty[i] - 1])
				{
					continue;
				} else if(mActTimer[i] == mAIRate[mDifficulty[i] - 1])
				{
					ArrowClearMessage acm = new ArrowClearMessage();
					acm.setCommon(i+1, 1);
					messages.add(acm);
					continue;
				} else if(mActTimer[i] == mAIRate[mDifficulty[i] - 1]+1)
				{
					mActTimer[i] = 0;
				}
				int arrow_count = mWorld.getArrowCount(player_id);
				if(arrow_count == 0)
				{
					int pref = 0;
					if(mBestRow != -1 && mBestCol != -1)
						if(mRowCount[mBestRow] > mColCount[mBestCol])
							pref = 1;
						else
							pref = 2;
					else if(mBestRow != -1)
						pref = 1;
					else if(mBestCol != -1)
						pref = 2;
						
					
					if(pref == 1)
					{
						if(mBestRow < mRocketPosition[i].y)
						{
							//Place down arrow
							CursorPositioningMessage cpm = new CursorPositioningMessage(mRocketPosition[i].x, mBestRow);
							cpm.setCommon(i+1, 1);
							messages.add(cpm);
							
							ArrowPlacementMessage apm = new ArrowPlacementMessage(mRocketPosition[i].x, mBestRow, Direction.South);
							apm.setCommon(i+1, 1);
							messages.add(apm);
						} else if(mBestRow > mRocketPosition[i].y)
						{
							CursorPositioningMessage cpm = new CursorPositioningMessage(mRocketPosition[i].x, mBestRow);
							cpm.setCommon(i+1, 1);
							messages.add(cpm);
							
							ArrowPlacementMessage apm = new ArrowPlacementMessage(mRocketPosition[i].x, mBestRow, Direction.North);
							apm.setCommon(i+1, 1);
							messages.add(apm);
						}
					} else if(pref == 2)
					{
						if(mBestCol < mRocketPosition[i].x)
						{
							CursorPositioningMessage cpm = new CursorPositioningMessage(mBestCol, mRocketPosition[i].y);
							cpm.setCommon(i+1, 1);
							messages.add(cpm);
							
							ArrowPlacementMessage apm = new ArrowPlacementMessage(mBestCol, mRocketPosition[i].y, Direction.East);
							apm.setCommon(i+1, 1);
							messages.add(apm);
						} else if(mBestRow > mRocketPosition[i].x)
						{
							CursorPositioningMessage cpm = new CursorPositioningMessage(mBestCol, mRocketPosition[i].y);
							cpm.setCommon(i+1, 1);
							messages.add(cpm);
							
							ArrowPlacementMessage apm = new ArrowPlacementMessage(mBestCol, mRocketPosition[i].y, Direction.West);
							apm.setCommon(i+1, 1);
							messages.add(apm);
						}						
					}
				}
				break;
			}
		}


		mMessageStack.add(messages);
		mPendMessages.clear();
	}
	
	private void scan_sweep() {
		for(int i = 0; i < mWorld.getWidth(); i++)
			mColCount[i] = 0;
		for(int i = 0; i < mWorld.getHeight(); i++)
			mRowCount[i] = 0;
		ArrayList<Walker> mice = mWorld.getLiveMice();
		Iterator<Walker> mouse_it = mice.iterator();
		while(mouse_it.hasNext()) {
			Walker mouse = mouse_it.next();
			int x = mouse.getPosition().x;
			int y = mouse.getPosition().y;
			mColCount[x]++;
			mRowCount[y]++;
		}
		int max_count = 0;
		mBestCol = -1;
		for(int i = 0; i < mWorld.getWidth(); i++)
		{
			if(mColCount[i] > max_count)
			{
				max_count = mColCount[i];
				mBestCol = i;
			}
		}
		max_count = 0;
		mBestRow = -1;
		for(int i = 0; i < mWorld.getHeight(); i++)
		{
			if(mRowCount[i] > max_count)
			{
				max_count = mRowCount[i];
				mBestRow = i;
			}
		}
	}
}
