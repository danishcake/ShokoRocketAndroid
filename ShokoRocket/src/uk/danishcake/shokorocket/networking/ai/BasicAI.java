package uk.danishcake.shokorocket.networking.ai;

import java.util.ArrayList;
import java.util.Iterator;

import uk.danishcake.shokorocket.networking.messages.ArrowClearMessage;
import uk.danishcake.shokorocket.networking.messages.ArrowPlacementMessage;
import uk.danishcake.shokorocket.networking.messages.CursorPositioningMessage;
import uk.danishcake.shokorocket.networking.messages.Message;
import uk.danishcake.shokorocket.simulation.Direction;
import uk.danishcake.shokorocket.simulation.MPWorld;
import uk.danishcake.shokorocket.simulation.SquareType;
import uk.danishcake.shokorocket.simulation.Turns;
import uk.danishcake.shokorocket.simulation.Vector2i;
import uk.danishcake.shokorocket.simulation.Walker;

public class BasicAI extends BaseAI {
	private int[] mRowCount;
	private int[] mColCount;
	private int mBestRow;
	private int mBestCol;
	private boolean mIntercept;
	private int mMaliceTactic = 0;
	private Vector2i mInterceptPosition = new Vector2i(0, 0);
	private Direction mInterceptDirection;
	private ArrayList<Vector2i> mSpawners = new ArrayList<Vector2i>();

	private Vector2i mRocketPosition = new Vector2i(0, 0);
	
	private final int AI_RATE = 10;
	private int mActTimer = 0;
	
	private int mTarget = 0;
	private Vector2i mTargetRocket = new Vector2i(0, 0);
	private Vector2i mLastBlockPos = new Vector2i(0, 0);
	private Direction mLastBlockDir = Direction.Invalid;

	@Override
	public void setup(MPWorld world, int player_id) {
		super.setup(world, player_id);

		mRowCount = new int[mWorld.getHeight()];
		mColCount = new int[mWorld.getWidth()];
		findRocket(mPlayerID, mRocketPosition);
		findRocket(0, mTargetRocket);
		findSpawners();
	}
	
	private void findRocket(int playerID, Vector2i position) {
		for(int y = 0; y < mWorld.getHeight(); y++)
		{
			for(int x = 0; x < mWorld.getWidth(); x++)
			{
				if(mWorld.getRocket(x, y) && mWorld.getPlayer(x, y) == playerID){
					position.x = x;
					position.y = y;
				}
			}
		}
	}
	
	private void findSpawners() {
		for(int y = 0; y < mWorld.getHeight(); y++)
		{
			for(int x = 0; x < mWorld.getWidth(); x++)
			{
				SquareType ss = mWorld.getSpecialSquare(x, y);
				if(ss == SquareType.NorthSpawner || ss == SquareType.SouthSpawner ||
				   ss == SquareType.WestSpawner || ss == SquareType.EastSpawner){
					mSpawners.add(new Vector2i(x, y));
				}
			}
		}
		
	}
	
	public void generateMessages(ArrayList<Message> messages) {
		mActTimer++;
		if(mActTimer < AI_RATE + mPlayerID)
		{
			return;
		} else if(mActTimer == AI_RATE + mPlayerID)
		{
			ArrowClearMessage acm = new ArrowClearMessage();
			acm.setCommon(mPlayerID, 0);
			messages.add(acm);
			mActTimer = 0;
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
			if(isColClear(mRocketPosition.x, mBestRow, mRocketPosition.y) && countRowReachable(mBestRow, mRocketPosition.x) > 0) {
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
					if((i & 0x01) == 0) //TODO correct bias towards one side
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
					   isColClear(L_sel, mRocketPosition.y, mBestRow) && 
					   countRowReachable(mBestRow, L_sel) > 0)
					{
						CursorPositioningMessage cpm = new CursorPositioningMessage(L_sel, mBestRow);
						cpm.setCommon(mPlayerID, 1);
						messages.add(cpm);

						ArrowPlacementMessage apm = new ArrowPlacementMessage(L_sel, mRocketPosition.y, L_sel > mRocketPosition.x ? Direction.West : Direction.East);
						apm.setCommon(mPlayerID, 1);
						messages.add(apm);

						ArrowPlacementMessage apm2 = new ArrowPlacementMessage(L_sel, mBestRow, mBestRow > mRocketPosition.y ? Direction.North : Direction.South);
						apm2.setCommon(mPlayerID, 1);
						messages.add(apm2);
						break;
					}
				}
			}
		} else if(pref == 2)
		{
			Direction arrow_dir = mBestCol < mRocketPosition.x ? Direction.East : Direction.West;
			if(isRowClear(mRocketPosition.y, mBestCol, mRocketPosition.x) && countColReachable(mBestCol, mRocketPosition.y) > 0) {
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
					if((i & 0x01) == 0) //TODO correct bias towards one side
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
					   isRowClear(L_sel, mRocketPosition.x, mBestCol) &&
					   countColReachable(mBestCol, L_sel) > 0)
					{
						CursorPositioningMessage cpm = new CursorPositioningMessage(mRocketPosition.x, L_sel);
						cpm.setCommon(mPlayerID, 1);
						messages.add(cpm);

						ArrowPlacementMessage apm = new ArrowPlacementMessage(mRocketPosition.x, L_sel, L_sel > mRocketPosition.y ? Direction.North : Direction.South);
						apm.setCommon(mPlayerID, 1);
						messages.add(apm);

						ArrowPlacementMessage apm2 = new ArrowPlacementMessage(mBestCol, L_sel, mBestCol > mRocketPosition.x ? Direction.West : Direction.East);
						apm2.setCommon(mPlayerID, 1);
						messages.add(apm2);
						break;
					}
				}
			}
		}

		//Change target if leader > 50 ahead
		int[] scores = mWorld.getPlayerScores();
		for(int i = 0; i < 4; i++) {
			if(i != mPlayerID && i != mTarget) {
				if(scores[i] > scores[mTarget] + 25) {
					mTarget = i;
					findRocket(mTarget, mTargetRocket);
					break;
				}
			}
		}

		mIntercept = false;
		if(++mMaliceTactic < 10)
		{
			//Use final arrow to screw with others
			//Try walking a spawner, see if it leads to an enemy
			for (Vector2i spawner : mSpawners) {
				walkSpawner(spawner.x, spawner.y, mWorld.getSpecialSquare(spawner.x, spawner.y).toSpawnerDirection(), 12);
				if(mIntercept)
				{
					Direction d;
					if(mMaliceTactic < 5)
						d = Turns.TurnLeft(mInterceptDirection);
					else
						d = Turns.TurnRight(mInterceptDirection);
					ArrowPlacementMessage apm = new ArrowPlacementMessage(mInterceptPosition.x, mInterceptPosition.y, d);
					apm.setCommon(mPlayerID, 1);
					messages.add(apm);
					break;
				}
			}
		}
		if(mMaliceTactic > 15)
			mMaliceTactic = 0;
		if(!mIntercept)
		{
			//Look down each axis of rocket, count mice heading towards it
			int from_north = 0;
			int from_south = 0;
			int from_west = 0;
			int from_east = 0;
			boolean placed = false;
			
			if(from_north == 0 && from_south == 0 && 
			   from_west == 0 && from_east == 0 && 
			   mLastBlockDir != Direction.Invalid)
			{
				ArrowPlacementMessage apm = new ArrowPlacementMessage(mLastBlockPos.x, mLastBlockPos.y, mLastBlockDir);
				apm.setCommon(mPlayerID, 1);
				messages.add(apm);
				placed = true;
			}

			ArrayList<Walker> walkers = mWorld.getLiveMice();
			for (Walker walker : walkers) {
				int x = walker.getX();
				int y = walker.getY();
				Direction d = walker.getDirection();
				if(x == mTargetRocket.x && y < mTargetRocket.y && d == Direction.South) {
					from_north++;
				}
				if(x == mTargetRocket.x && y > mTargetRocket.y && d == Direction.North) {
					from_south++;
				}
				if(y == mTargetRocket.y && x < mTargetRocket.x && d == Direction.East) {
					from_west++;
				}
				if(y == mTargetRocket.y && x > mTargetRocket.x && d == Direction.West) {
					from_east++;
				}
			}
			
			//Find most incoming mice
			if(from_north >= from_south &&
			   from_north >= from_west &&
			   from_north >= from_east && !placed) {
				if(mWorld.getSpecialSquare(mTargetRocket.x, mTargetRocket.y - 1) == SquareType.Empty)
				{
					Direction redirect;
					if(mRocketPosition.x < mTargetRocket.x)
						redirect = Direction.West;
					else if(mRocketPosition.x > mTargetRocket.x) 
						redirect = Direction.East;
					else
					{
						if(mWorld.getSpecialSquare(mTargetRocket.x, mTargetRocket.y - 2).getArrowDirectionality() == Direction.South)
							redirect = Direction.East;
						else
							redirect = Direction.North;
					}
					ArrowPlacementMessage apm = new ArrowPlacementMessage(mTargetRocket.x, mTargetRocket.y - 1, redirect);
					apm.setCommon(mPlayerID, 1);
					messages.add(apm);
					
					placed = true;
				} else
				{
					from_north = 0;
				}
			} 
			if(from_south >= from_north &&
			   from_south >= from_west &&
			   from_south >= from_east && !placed) {
				if(mWorld.getSpecialSquare(mTargetRocket.x, mTargetRocket.y + 1) == SquareType.Empty)
				{
					Direction redirect;
					if(mRocketPosition.x < mTargetRocket.x)
						redirect = Direction.West;
					else if(mRocketPosition.x > mTargetRocket.x) 
						redirect = Direction.East;
					else
					{
						if(mWorld.getSpecialSquare(mTargetRocket.x, mTargetRocket.y + 2).getArrowDirectionality() == Direction.North)
							redirect = Direction.West;
						else
							redirect = Direction.South;
					}
					ArrowPlacementMessage apm = new ArrowPlacementMessage(mTargetRocket.x, mTargetRocket.y + 1, redirect);
					apm.setCommon(mPlayerID, 1);
					messages.add(apm);
					
					placed = true;
				} else
				{
					from_south = 0;
				}
			} 
			if(from_west >= from_north &&
			   from_west >= from_south &&
			   from_west >= from_east && !placed) {
				if(mWorld.getSpecialSquare(mTargetRocket.x - 1, mTargetRocket.y) == SquareType.Empty)
				{
					Direction redirect;
					if(mRocketPosition.y < mTargetRocket.y)
						redirect = Direction.North;
					else if(mRocketPosition.y > mTargetRocket.y) 
						redirect = Direction.South;
					else
					{
						if(mWorld.getSpecialSquare(mTargetRocket.x - 2, mTargetRocket.y).getArrowDirectionality() == Direction.East)
							redirect = Direction.South;
						else
							redirect = Direction.West;
					}
					
					ArrowPlacementMessage apm = new ArrowPlacementMessage(mTargetRocket.x - 1, mTargetRocket.y, redirect);
					apm.setCommon(mPlayerID, 1);
					messages.add(apm);
					
					placed = true;
				} else
				{
					from_west = 0;
				}
			}
			if(from_east >= from_north &&
			   from_east >= from_south &&
			   from_east >= from_west && !placed) {
				if(mWorld.getSpecialSquare(mTargetRocket.x + 1, mTargetRocket.y) == SquareType.Empty)
				{
					Direction redirect;
					if(mRocketPosition.y < mTargetRocket.y)
						redirect = Direction.North;
					else if(mRocketPosition.y > mTargetRocket.y) 
						redirect = Direction.South;
					else
					{
						if(mWorld.getSpecialSquare(mTargetRocket.x + 2, mTargetRocket.y).getArrowDirectionality() == Direction.West)
							redirect = Direction.North;
						else
							redirect = Direction.East;
					}
					
					ArrowPlacementMessage apm = new ArrowPlacementMessage(mTargetRocket.x + 1, mTargetRocket.y, redirect);
					apm.setCommon(mPlayerID, 1);
					messages.add(apm);
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
			SquareType ss = mWorld.getSpecialSquare(x, row);
			//TODO walls
			//TODO holes, other rockets
			if(ss == SquareType.Hole || (ss == SquareType.Rocket && mWorld.getPlayer(x, row) != mPlayerID) ||
			   (ss.getArrowDirectionality() != Direction.Invalid && mWorld.getPlayer(x, row) != mPlayerID))
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
			SquareType ss = mWorld.getSpecialSquare(col, y);
			//TODO walls
			//TODO holes, other rockets
			if(ss == SquareType.Hole || (ss == SquareType.Rocket && mWorld.getPlayer(col, y) != mPlayerID) || 
			   (ss.getArrowDirectionality() != Direction.Invalid && mWorld.getPlayer(col, y) != mPlayerID))
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Counts the number of walkers in row 'row' who will reach col
	 * @param row
	 * @param col
	 * @return
	 */
	private int countRowReachable(int row, int col) {
		ArrayList<Walker> mice = mWorld.getLiveMice();
		int reachable = 0;
		for(int i = col + 1; i < mWorld.getWidth(); i++) {
			//Terminate early if blocked
			SquareType ss = mWorld.getSpecialSquare(i, row);
			if(ss == SquareType.Hole || ss == SquareType.Rocket ||
			   (ss.getArrowDirectionality() != Direction.West && 
				ss.getArrowDirectionality() != Direction.Invalid))
				break;
			for (Walker walker : mice) {
				//Count walkers in this row
				if(walker.getY() == row && walker.getX() == i)
					reachable++;
			}
		}
		for(int i = col - 1; i > 0; i--) {
			//Terminate early if blocked
			SquareType ss = mWorld.getSpecialSquare(i, row);
			if(ss == SquareType.Hole || ss == SquareType.Rocket ||
					   (ss.getArrowDirectionality() != Direction.East && 
						ss.getArrowDirectionality() != Direction.Invalid))
				break;
			for (Walker walker : mice) {
				//Count walkers in this row
				if(walker.getY() == row && walker.getX() == i)
					reachable++;
			}			
		}
		return reachable;
	}
	
	/**
	 * Counts the number of walkers in col 'col' who will reach row
	 * @param row
	 * @param col
	 * @return
	 */
	private int countColReachable(int col, int row) {
		ArrayList<Walker> mice = mWorld.getLiveMice();
		int reachable = 0;
		for(int i = row + 1; i < mWorld.getHeight(); i++) {
			//Terminate early if blocked
			SquareType ss = mWorld.getSpecialSquare(col, i);
			if(ss == SquareType.Hole || ss == SquareType.Rocket ||
					   (ss.getArrowDirectionality() != Direction.North && 
						ss.getArrowDirectionality() != Direction.Invalid))
				break;
			for (Walker walker : mice) {
				//Count walkers in this row
				if(walker.getX() == col && walker.getY() == i)
					reachable++;
			}
		}
		for(int i = row - 1; i > 0; i--) {
			//Terminate early if blocked
			SquareType ss = mWorld.getSpecialSquare(col, i);
			if(ss == SquareType.Hole || ss == SquareType.Rocket ||
					   (ss.getArrowDirectionality() != Direction.South && 
						ss.getArrowDirectionality() != Direction.Invalid))
				break;
			for (Walker walker : mice) {
				//Count walkers in this row
				if(walker.getX() == col && walker.getY() == i)
					reachable++;
			}			
		}
		return reachable;
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
			int x = mouse.getX();
			int y = mouse.getY();
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

	/**
	 * Walks from a spawner 
	 * @param x starting location
	 * @param y starting location
	 * @param direction starting direction
	 * @param range maximum number of steps to take
	 */
	private void walkSpawner(int x, int y, Direction direction, int range) {
		boolean interceptable = false;
		for(int i = 0; i < range; i++)
		{
			switch(direction)
			{
			case North:
				y--;
				if(y < 0) y = mWorld.getHeight() - 1;
				break;
			case South:
				y++;
				if(y >= mWorld.getHeight()) y = 0;
				break;
			case West:
				x--;
				if(x < 0) x = mWorld.getWidth() - 1;
				break;
			case East:
				x++;
				if(x >= mWorld.getWidth()) x = 0;
				break;
			case Invalid:
				//Uninteresting, trapped or something. Terminate early
				mIntercept = false;
				return;
			}
			int player_id = mWorld.getPlayer(x, y);
			SquareType square_type = mWorld.getSpecialSquare(x, y);
			Direction d = square_type.getArrowDirectionality();
			if(d != Direction.Invalid)
				direction = d;
			//Check special terminations (enemy rockets, holes)
			if(square_type == SquareType.Hole)
			{
				//Terminate early, uninteresting
				mIntercept = false;
			}
			if(square_type == SquareType.Rocket)
			{
				if(player_id != mPlayerID)
				{
					//Very interesting!
					mIntercept = true && interceptable;
					mInterceptDirection = direction;
					return;
				}
			}
			if(square_type == SquareType.Empty)
			{
				mInterceptPosition.x = x;
				mInterceptPosition.y = y;
				interceptable = true;
			}
			if(player_id == mPlayerID)
			{
				//Interact with walls
				if(!mWorld.getDirection(x, y, d))
				{
					//mDirection = mDirection; //Carry straight on!
				} 
				else if(mWorld.getDirection(x, y, d) && 
						!mWorld.getDirection(x, y, Turns.TurnRight(d)))
					direction = Turns.TurnRight(direction);
				else if(mWorld.getDirection(x, y, d) &&
						mWorld.getDirection(x, y, Turns.TurnRight(d)) &&
						!mWorld.getDirection(x, y, Turns.TurnLeft(d)))
					direction = Turns.TurnLeft(direction);
				else if(!mWorld.getDirection(x, y, Turns.TurnAround(d)))
					direction = Turns.TurnAround(direction);
				else
					direction = Direction.Invalid;
			}
		}
	}
}