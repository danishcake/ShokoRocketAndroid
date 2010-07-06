package uk.danishcake.shokorocket.gui;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class NinePatchData {
	public Bitmap srcBitmap;
	public int leftBorder;
	public int rightBorder;
	public int topBorder;
	public int bottomBorder;
	
	public NinePatchData(Bitmap source, int left, int right, int top, int bottom)
	{
		leftBorder = left;
		rightBorder = right;
		topBorder = top;
		bottomBorder = bottom;
		this.srcBitmap = source;
	}
	
	public Rect topLeftRect() {
		return new Rect(0, 0, leftBorder, topBorder);
	}
	
	public Rect topRightRect() {
		return new Rect(srcBitmap.getWidth() - rightBorder, 0, srcBitmap.getWidth(), topBorder);
	}
	
	public Rect bottomLeftRect() {
		return new Rect(0, srcBitmap.getHeight() - bottomBorder, leftBorder, srcBitmap.getHeight());
	}
	
	public Rect bottomRightRect() {
		return new Rect(srcBitmap.getWidth() - rightBorder, srcBitmap.getHeight() - bottomBorder, srcBitmap.getWidth(), srcBitmap.getHeight());
	}
	
	public Rect topRect() {
		return new Rect(leftBorder, 0, srcBitmap.getWidth() - rightBorder, topBorder);
	}
	
	public Rect bottomRect() {
		return new Rect(leftBorder, srcBitmap.getHeight() - bottomBorder, srcBitmap.getWidth() - rightBorder, srcBitmap.getHeight());
	}
	
	public Rect leftRect() {
		return new Rect(0,  topBorder, leftBorder, srcBitmap.getHeight() - bottomBorder);
	}
	
	public Rect rightRect() {
		return new Rect(srcBitmap.getWidth() - rightBorder,  topBorder, srcBitmap.getWidth(), srcBitmap.getHeight() - bottomBorder);
	}
	
	public Rect centreRect() {
		return new Rect(leftBorder, topBorder, srcBitmap.getWidth() - rightBorder, srcBitmap.getHeight() - bottomBorder);
	}
	
}
