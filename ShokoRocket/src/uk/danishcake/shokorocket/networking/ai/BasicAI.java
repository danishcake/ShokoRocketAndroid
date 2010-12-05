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

		if(pref == 1) //Do a row
		{
			Direction arrow_dir = mBestRow < mRocketPosition.y ? Direction.South : Direction.North;
			if(isColClear(mRocketPosition.x, mBestRow, mRocketPosition.y)) {
				//If column clear place direct arrow
				CursorPositioningMessage cpm = new CursorPositioningMessage(mRocketPosition.x, mBestRow);
				cpm.setCommon(mPlayerID, 1);
				messages.add(cpm);

				ArrowPlacementMessage apm = new ArrowPlacementMessage(mRocketPosition.x, mBestRow, arrow_dir);
				apm.setCommon(mPlayerID, 1);
				messages.add(apm);
			} else //Find an L shape
			{
				//Walk alternate sides out to find row/col
				int L_m = mRocketPosition.x;
				int L_p = mRocketPosition.x;
				int L_sel = mRocketPosition.x;

				for(int i = 0; i < mWorld.getWidth() - 1; i++)
				{
					if(i & 0x01 == 0) //TODO correct bias towards one side
					{
						if(L_m > 0)
						{
							L_m--;
							L_sel = L_m;
						} else
						{
							L_p++;
							L_sel = L_p;
						}
					} else
					{
						if(L_p < mWorld.getWidth() - 1)
						{
							L_p++;
							L_sel = L_p;
						} else
						{
							L_m--;
							L_sel = L_m;
						}
					}
					//Check if L_sel OK
					if(isRowClear(mRocketPosition.y, mRocketPosition.x, L_sel) && 
					   isColClear(L_sel, mRocketPosition.y, mBestRow))
					{
						CursorPositioningMessage cpm = new CursorPositioningMessage(L_sel, mBestRow);
						cpm.setCommon(mPlayerID, 1);
						messages.add(cpm);

						ArrowPlacementMessage apm = new ArrowPlacementMessage(L_sel, mRocketPosition.y, L_sel > mRocketPosition.x ? Direction.West : Direction.East);
						apm.setCommon(mPlayerID, 1);
						messages.add(apm);

						ArrowPlacementMessage apm2 = new ArrowPlacementMessage(L_sel, mBestRow, mBestRow > mRocketPosition.y ? Direction.South : Direction.North);
						apm2.setCommon(mPlayerID, 1);
						messages.add(apm2);
					}
				}
			}
		} else if(pref == 2)
		{
			Direction arrow_dir = mBestCol < mRocketPosition.x ? Direction.East : Direction.West;
			if(isRowClear(mRocketPosition.y, mBestCol, mRocketPosition.x)) {
				//If row clear place direct arrow
				CursorPositioningMessage cpm = new CursorPositioningMessage(mBestCol, mRocketPosition.y);
				cpm.setCommon(mPlayerID, 1);
				messages.add(cpm);

				ArrowPlacementMessage apm = new ArrowPlacementMessage(mBestCol, mRocketPosition.y, arrow_dir);
				apm.setCommon(mPlayerID, 1);
				messages.add(apm);
			} else
			{
				//Walk alternate sides out to find row/col
				int L_m = mRocketPosition.y;
				int L_p = mRocketPosition.y;
				int L_sel = mRocketPosition.y;

				for(int i = 0; i < mWorld.getHeight() - 1; i++)
				{
					if(i & 0x01 == 0) //TODO correct bias towards one side
					{
						if(L_m > 0)
						{
							L_m--;
							L_sel = L_m;
						} else
						{
							L_p++;
							L_sel = L_p;
						}
					} else
					{
						if(L_p < mWorld.getHeight() - 1)
						{
							L_p++;
							L_sel = L_p;
						} else
						{
							L_m--;
							L_sel = L_m;
						}
					}
					//Check if L_sel OK
					if(isColClear(mRocketPosition.x, mRocketPosition.y, L_sel) && 
					   isRowClear(L_sel, mRocketPosition.x, mBestCol))
					{
						CursorPositioningMessage cpm = new CursorPositioningMessage(mRocketPosition.x, L_sel);
						cpm.setCommon(mPlayerID, 1);
						messages.add(cpm);

						ArrowPlacementMessage apm = new ArrowPlacementMessage(mRocketPosition.x, L_sel, L_sel > mRocketPosition.y ? Direction.South : Direction.North);
						apm.setCommon(mPlayerID, 1);
						messages.add(apm);

						ArrowPlacementMessage apm2 = new ArrowPlacementMessage(mBestCol, L_sel, mBestCol > mRocketPosition.x ? Direction.West : Direction.East);
						apm2.setCommon(mPlayerID, 1);
						messages.add(apm2);
					}
				}
			}
		}
	}
	
	/**
	 * Checks that there is a clear route between along a row between two columns
	 */
	private boolean isRowClear(int row, int colA, int colB) {
		int low = colA < colB ? colA : colB;
		int high = colA < colB ? colB : colA;
		for(int x = low; x <= high; x++)
		{
			SquareType ss = mWorld.getSpecialSquare(x, row)
			//TODO walls
			//TODO holes, other rockets
			if(ss == SquareType.Hole || (ss == SquareType.Rocket && mWorld.getPlayer(x, row) != mPlayer))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks that there is a clear route between along a row between two columns
	 */
	private boolean isColClear(int col, int rowA, int rowB) {
		int low = rowA < rowB ? rowA : rowB;
		int high = rowA < rowB ? rowB : rowA;
		for(int y = low; y <= high; y++)
		{
			SquareType ss = mWorld.getSpecialSquare(col, y)
			//TODO walls
			//TODO holes, other rockets
			if(ss == SquareType.Hole || (ss == SquareType.Rocket && mWorld.getPlayer(col, y) != mPlayer))
			{
				return false;
			}
		}
		return true;
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