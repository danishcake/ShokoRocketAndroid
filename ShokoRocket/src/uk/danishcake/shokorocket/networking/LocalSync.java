package uk.danishcake.shokorocket.networking;

import java.util.ArrayList;

public class LocalSync implements IGameSync {

	private static ArrayList<LocalSync> mSyncs = new ArrayList<LocalSync>();
	
	@Override
	public void Connect(String dest) {
		// TODO Auto-generated method stub
		mSyncs.add(this);
	}
	
	@Override
	public void Setup() {
		// TODO Auto-generated method stub
		mSyncs = new ArrayList<LocalSync>();	
	}
	
	@Override
	public void Shutdown() {
		// TODO Auto-generated method stub
		mSyncs = new ArrayList<LocalSync>();	
	}

	@Override
	public boolean HasNewFrameData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void SendFrameEnd() {
		// TODO Auto-generated method stub

	}
}
