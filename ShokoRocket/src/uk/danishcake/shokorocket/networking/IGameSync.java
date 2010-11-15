package uk.danishcake.shokorocket.networking;

/**
 * IGameSync
 * @author Edward Woolhouse
 * Provides an interface to a networking solution as described in
 * http://www.gamasutra.com/view/feature/3094/1500_archers_on_a_288_network_.php
 * where messages are sent to all other players
 */
public interface IGameSync {
	/**
	 * Connects to a destination
	 * @param dest A string describing the destination - could be an IP address
	 * for networked clients, or and internal addressing scheme for local games
	 */
	public void Connect(String dest);
	
	/**
	 * Setup
	 * Initialises
	 */
	public void Setup();
	
	/** 
	 * Shutdown
	 */
	public void Shutdown();
	
	/**
	 * HasNewGameData
	 * @return true when a new frame of messages has been agreed upon and is 
	 * ready for implementation
	 */
	public boolean HasNewFrameData();
	
	/**
	 * SendFrameEnd
	 * Sends a frame end message to all other client
	 */
	public void SendFrameEnd();
}
