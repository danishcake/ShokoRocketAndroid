package uk.danishcake.shokorocket;

import uk.danishcake.shokorocket.FP;

public class Vector2FP {
	private int x;
	private int y;
	
	public Vector2FP(int x_fp, int y_fp)
	{
		this.x = x_fp;
		this.y = y_fp;
	}
	
	public Vector2FP(float x, float y)
	{
		this.x = FP.fromFloat(x);
		this.y = FP.fromFloat(y);
	}
	
	public int GetX_i()
	{
		return FP.toInt(x);
	}
	
	public int GetY_i()
	{
		return FP.toInt(y);
	}
	
	public float GetX()
	{
		return FP.toFloat(x);
	}
	
	public float GetY()
	{
		return FP.toFloat(y);
	}	
}
