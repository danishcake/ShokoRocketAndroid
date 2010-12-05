package uk.danishcake.shokorocket.networking.ai;

public class BasicAI extends BaseAI {
	private int[] mRowCount;
	private int[] mColCount;
	private int mBestRow;
	private int mBestCol;
	Vector2i mRocketPosition;
	private int mActTimer = 0;
	private final int AI_RATE = 10;

	@Override
	public void setup(MPWorld world, int player_id) {
		super(world, player_id);

		mRowCount = new int[mWorld.getHeight()];
		mColCount = new int[mWorld.getWidth()];
		for(int y = 0; y < mWorld.getHeight(); y++)
		{
			for(int x = 0; x < mWorld.getWidth(); x++)
			{
				if(mWorld.getRocket(x, y) && mWorld.getPlayer(x, y) == mPlayerID){
					mRocketPosition = new Vector2i(x, y);
				}
			}
		}
	}
	
	public void generateMessages(ArrayList<Message> messages) {
		if(mActTimer < AI_RATE)
		{
			return;
		} else if(mActTimer == AI_RATE)
		{
			ArrowClearMessage acm = new ArrowClearMessage();
			acm.setCommon(mPlayerID, 1);
			messages.add(acm);
			return;
		} else if(mActTimer > mPlayerID)
		{
			mActTimer = 0; //Now place an arrow
		}

		//Chose whether to use columns or rows based on area with most walkers
		scan_sweep();
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
			if(mBestRow < mRocketPosition.y)
			{
				//Place down arrow
				CursorPositioningMessage cpm = new CursorPositioningMessage(mRocketPosition.x, mBestRow);
				cpm.setCommon(mPlayerID, 1);
				messages.add(cpm);

				ArrowPlacementMessage apm = new ArrowPlacementMessage(mRocketPosition.x, mBestRow, Direction.South);
				apm.setCommon(mPlayerID, 1);
				messages.add(apm);
			} else if(mBestRow > mRocketPosition.y)
			{
				CursorPositioningMessage cpm = new CursorPositioningMessage(mRocketPosition.x, mBestRow);
				cpm.setCommon(mPlayerID, 1);
				messages.add(cpm);

				ArrowPlacementMessage apm = new ArrowPlacementMessage(mRocketPosition.x, mBestRow, Direction.North);
				apm.setCommon(mPlayerID, 1);
				messages.add(apm);
			}
		} else if(pref == 2)
		{
			if(mBestCol < mRocketPosition.x)
			{
				CursorPositioningMessage cpm = new CursorPositioningMessage(mBestCol, mRocketPosition.y);
				cpm.setCommon(mPlayerID, 1);
				messages.add(cpm);

				ArrowPlacementMessage apm = new ArrowPlacementMessage(mBestCol, mRocketPosition.y, Direction.East);
				apm.setCommon(mPlayerID, 1);
				messages.add(apm);
			} else if(mBestRow > mRocketPosition.x)
			{
				CursorPositioningMessage cpm = new CursorPositioningMessage(mBestCol, mRocketPosition.y);
				cpm.setCommon(mPlayerID, 1);
				messages.add(cpm);

				ArrowPlacementMessage apm = new ArrowPlacementMessage(mBestCol, mRocketPosition.y, Direction.West);
				apm.setCommon(mPlayerID, 1);
				messages.add(apm);
			}
		}
	}

	/**
	 * Finds the number of mice in each row/column moving towards the rocket in that axis
	 */
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
			if((y <= mRocketPosition.y && mouse.getDirection() == Direction.South) || 
			   (y >= mRocketPosition.y && mouse.getDirection() == Direction.North))
				mColCount[x]++;
			if((x <= mRocketPosition.x && mouse.getDirection() == Direction.East) || 
			   (x >= mRocketPosition.x && mouse.getDirection() == Direction.West))
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