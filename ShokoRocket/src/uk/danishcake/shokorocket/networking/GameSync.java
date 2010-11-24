package uk.danishcake.shokorocket.networking;

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
	
	// TODO some sort of pop messages
}
