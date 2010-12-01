package uk.danishcake.shokorocket.networking;
import uk.danishcake.shokorocket.networking.messages.Message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * GameSync
 * @author Edward Woolhouse
 * 
 * Provides an interface to a networking solution as described in
 * http://www.gamasutra.com/view/feature/3094/1500_archers_on_a_288_network_.php
 * where messages are sent to all other players
 */
public abstract class GameSync {
	protected int mLocalFrame = 0;
	protected int mSyncedFrame = 0;
	private Comparator<Message> mMessageSorter = new Comparator<Message>() {
		@Override
		public int compare(Message object1, Message object2) {
			return object1.sub_frame_id - object2.sub_frame_id;
		}
	};
	
	
	protected LinkedList<ArrayList<Message>> mMessageStack = new LinkedList<ArrayList<Message>>();
	/**
	 * Connects to a destination
	 * @param dest A string describing the destination - could be an IP address
	 * for networked clients, or and internal addressing scheme for local games
	 */
	public abstract void Connect(String dest);
	
	/**
	 * HasNewGameData
	 * @return the id of the frame which all clients agree on
	 */
	public int getReadyFrame()
	{
		return mSyncedFrame;
	}
	
	public int getSentFrame()
	{
		return mLocalFrame;
	}
	
	/**
	 * SendFrameEnd
	 * Sends a frame end message to all other client
	 */
	public abstract void SendFrameEnd();
	
	/**
	 * Sends a message to all other clients
	 */
	public abstract void sendMessage(Message message);
	
	// TODO some sort of pop messages
	public List<Message> popMessages() {
		ArrayList<Message> frame_messages = mMessageStack.removeFirst();
		
		java.util.Collections.sort(frame_messages, mMessageSorter);
		return frame_messages;
	}
	
}
