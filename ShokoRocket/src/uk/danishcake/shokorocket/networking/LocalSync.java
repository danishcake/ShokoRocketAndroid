package uk.danishcake.shokorocket.networking;

/**
 * LocalSync is a simplistic multiplayer connection that is all local
 * It provides indications of several local clients
 * @author Edward Woolhouse
 */
public class LocalSync extends GameSync {

	@Override
	public void Connect(String dest) {
		// TODO parse dest for client count/difficulty etc 
		
	}

	@Override
	public void SendFrameEnd() {
		mLocalFrame++;
		mSyncedFrame = mLocalFrame - 1;
	}
}
