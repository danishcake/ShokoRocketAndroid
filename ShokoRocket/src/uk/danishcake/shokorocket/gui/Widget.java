package uk.danishcake.shokorocket.gui;

import java.util.ArrayList;

import uk.danishcake.shokorocket.simulation.Vector2i;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;

/**
 * Widget
 * Represents a tappable gui element 
 * @author Edward Woolhouse
 */
public class Widget {
	protected Bitmap mBackbuffer;
	protected Bitmap mFrontbuffer;
	protected boolean mInvalidated = true;
	private int mDepressedTime = 0;
	protected Rect mBounds;
	private String mText = "";
	private int mFontSize = 16;
	private int mFontBorder = 8;
	private OnClickListener mOnClick = null;
	private OnClickListener mOnDisabledClick = null;
	private boolean mEnabled = true;
	
	public enum VerticalAlignment {
		Top, Middle, Bottom
	}
	public enum HorizontalAlignment {
		Left, Middle, Right
	}
	private VerticalAlignment mVerticalAlignment = VerticalAlignment.Middle;
	private HorizontalAlignment mHorizontalAlignment = HorizontalAlignment.Middle;
	
	private static final int DepressedTime = 150; 
	/**
	 * Creates a widget with a background automatically scaled from a provided NinePatch
	 * @param ninePatchData Bitmap and borders
	 * @param sizeAndPosition size and position of the widget
	 */
	public Widget(NinePatchData ninePatchData, Rect sizeAndPosition)
	{
		mBounds = sizeAndPosition;
		if(mBounds.width() < mFontBorder)
			mFontBorder = 4;
		
		mBackbuffer = Bitmap.createBitmap(mBounds.width(), mBounds.height(), Bitmap.Config.ARGB_8888);
		mFrontbuffer = Bitmap.createBitmap(mBounds.width(), mBounds.height(), Bitmap.Config.ARGB_8888);
		
		drawNP(mBackbuffer, ninePatchData);
	}
	
	public Widget(Bitmap backbuffer, Point position) 
	{
		mBackbuffer = backbuffer;
		mBounds = new Rect(position.x, position.y, position.x + backbuffer.getWidth(), position.y + backbuffer.getHeight());
		mBackbuffer = Bitmap.createBitmap(mBounds.width(), mBounds.height(), Bitmap.Config.ARGB_8888);
		mFrontbuffer = Bitmap.createBitmap(mBounds.width(), mBounds.height(), Bitmap.Config.ARGB_8888);
		
		Canvas c = new Canvas(mBackbuffer);
		c.drawBitmap(backbuffer, 0, 0, null);
	}
	
	protected void drawNP(Bitmap dest, NinePatchData ninePatchData)
	{
		//Copy bitmap into position
		Canvas canvas = new Canvas(dest);
		canvas.drawARGB(0,0,0,0);


		int src_middle_w = ninePatchData.srcBitmap.getWidth() - ninePatchData.leftBorder - ninePatchData.rightBorder;
		int src_middle_h = ninePatchData.srcBitmap.getHeight() - ninePatchData.topBorder - ninePatchData.bottomBorder;

		//Top left
		Bitmap source = ninePatchData.srcBitmap;
		canvas.drawBitmap(source, ninePatchData.topLeftRect(), ninePatchData.topLeftRect(), null);

		//Top right
		Rect top_right = ninePatchData.topRightRect();
		top_right.offsetTo(mBackbuffer.getWidth() - ninePatchData.rightBorder, 0);
		canvas.drawBitmap(source, ninePatchData.topRightRect(), top_right, null);

		//Bottom left
		Rect bottom_left = ninePatchData.bottomLeftRect();
		bottom_left.offsetTo(0, mBackbuffer.getHeight() - ninePatchData.bottomBorder);
		canvas.drawBitmap(source, ninePatchData.bottomLeftRect(), bottom_left, null);
		
		//Bottom right
		Rect bottom_right = ninePatchData.bottomRightRect();
		bottom_right.offsetTo(mBackbuffer.getWidth() - ninePatchData.rightBorder, mBackbuffer.getHeight() - ninePatchData.bottomBorder);
		canvas.drawBitmap(source, ninePatchData.bottomRightRect(), bottom_right, null);
		
		
		//Top & bottom
		int h_tiles;
		if(src_middle_w > 0)
			h_tiles = (mBounds.width() - ninePatchData.leftBorder - ninePatchData.rightBorder) / src_middle_w;
		else
			h_tiles = 0;
		int h_remainder = (mBounds.width() - ninePatchData.leftBorder - ninePatchData.rightBorder) - h_tiles * src_middle_w;
		for(int x = 0; x < h_tiles; x++)
		{
			Rect top = ninePatchData.topRect();
			top.offsetTo(ninePatchData.leftBorder + x * src_middle_w, 0);
			canvas.drawBitmap(source, ninePatchData.topRect(), top, null);
			
			Rect bottom = ninePatchData.bottomRect();
			bottom.offsetTo(ninePatchData.leftBorder + x * src_middle_w, mBackbuffer.getHeight() - ninePatchData.bottomBorder);
			canvas.drawBitmap(source, ninePatchData.bottomRect(), bottom, null);
		}
		if(h_remainder > 0)
		{
			Rect top_src = ninePatchData.topRect();
			top_src.right = top_src.left + h_remainder;
			
			Rect top = new Rect(top_src);
			top.offsetTo(ninePatchData.leftBorder + h_tiles * src_middle_w, 0);
			canvas.drawBitmap(source, top_src, top, null);
			
			Rect bottom_src = ninePatchData.bottomRect();
			bottom_src.right = bottom_src.left + h_remainder;
			
			Rect bottom = new Rect(bottom_src);
			bottom.offsetTo(ninePatchData.leftBorder + h_tiles * src_middle_w, mBackbuffer.getHeight() - ninePatchData.bottomBorder);
			canvas.drawBitmap(source, bottom_src, bottom, null);
		}

		//Left & right
		int v_tiles;
		if(src_middle_h > 0)
			v_tiles = (mBounds.height() - ninePatchData.topBorder- ninePatchData.bottomBorder) / src_middle_h;
		else
			v_tiles = 0;
		int v_remainder = (mBounds.height() - ninePatchData.topBorder - ninePatchData.bottomBorder) - v_tiles * src_middle_h;
		for(int y = 0; y < v_tiles; y++)
		{
			Rect left = ninePatchData.leftRect();
			left.offsetTo(0, ninePatchData.topBorder + y * src_middle_h);
			canvas.drawBitmap(source, ninePatchData.leftRect(), left, null);
			
			Rect right = ninePatchData.rightRect();
			right.offsetTo(mBackbuffer.getWidth() - ninePatchData.rightBorder, ninePatchData.topBorder + y * src_middle_h);
			canvas.drawBitmap(source, ninePatchData.rightRect(), right, null);
		}
		if(v_remainder > 0)
		{
			Rect left_src = ninePatchData.leftRect();
			left_src.bottom = left_src.top + v_remainder;
			
			Rect left = new Rect(left_src);
			left.offsetTo(0, ninePatchData.topBorder + v_tiles * src_middle_h);
			canvas.drawBitmap(source, left_src, left, null);
			
			Rect right_src = ninePatchData.rightRect();
			right_src.bottom = right_src.top + v_remainder;
			
			Rect right = new Rect(right_src);
			right.offsetTo(mBackbuffer.getWidth() - ninePatchData.rightBorder, ninePatchData.topBorder + v_tiles * src_middle_h);
			canvas.drawBitmap(source, ninePatchData.rightRect(), right, null);
		}

		//Fill centre
		for(int x = 0; x < h_tiles; x++)
		{
			for(int y = 0; y < v_tiles; y++)
			{
				Rect src_rect = ninePatchData.centreRect();
				Rect dest_rect = new Rect(src_rect);
				dest_rect.offsetTo(ninePatchData.leftBorder + x * src_middle_w, ninePatchData.topBorder + y * src_middle_h);
				canvas.drawBitmap(source, src_rect, dest_rect, null);
			}
		}
		for(int x = 0; x < h_tiles; x++)
		{
			Rect src_rect = ninePatchData.centreRect();
			src_rect.bottom = src_rect.top + v_remainder;
			
			Rect dest_rect = new Rect(src_rect);
			dest_rect.bottom = dest_rect.top + v_remainder;
			dest_rect.offsetTo(ninePatchData.leftBorder + x * src_middle_w, ninePatchData.topBorder + v_tiles * src_middle_h);
			canvas.drawBitmap(source, src_rect, dest_rect, null);
		}
		for(int y = 0; y < v_tiles; y++)
		{
			Rect src_rect = ninePatchData.centreRect();
			src_rect.right = src_rect.left + h_remainder;
			
			Rect dest_rect = new Rect(src_rect);
			dest_rect.right = dest_rect.left + h_remainder;
			dest_rect.offsetTo(ninePatchData.leftBorder + h_tiles * src_middle_w, ninePatchData.topBorder + y * src_middle_h);
			canvas.drawBitmap(source, src_rect, dest_rect, null);
		}
		{
			Rect src_rect = ninePatchData.centreRect();
			src_rect.right = src_rect.left + h_remainder;
			src_rect.bottom = src_rect.top + v_remainder;
			
			Rect dest_rect = new Rect(src_rect);
			dest_rect.offsetTo(ninePatchData.leftBorder + h_tiles * src_middle_w, ninePatchData.topBorder + v_tiles * src_middle_h);
			canvas.drawBitmap(source, src_rect, dest_rect, null);
		}	
	}
	
	/**
	 * Sets the text displayed in the widget
	 * @param text
	 */
	public void setText(String text) {
		if(!text.equals(mText))
		{
			mText = text;
			mInvalidated = true;
		}
	}
	
	public void setVerticalAlignment(VerticalAlignment alignment) {
		mVerticalAlignment = alignment;
	}
	
	public void setHorizontalAlignment(HorizontalAlignment alignment) {
		mHorizontalAlignment = alignment;
	}
	
	public void setFontSize(int fontSize) {
		mFontSize = fontSize;
	}
	
	/**
	 * Sets the callback to fire when tapped and enabled
	 * @param callback
	 */
	public void setOnClickListener(OnClickListener callback) {
		mOnClick = callback;
	}
	
	/**
	 * Sets the callback to fire when tapped and disabled
	 * @param callback
	 */
	public void setOnDisabledClickListener(OnClickListener callback) {
		mOnDisabledClick = callback;
	}

	/**
	 * Enables/disables the button
	 * @param enabled Enabled state
	 */
	public void setEnabled(boolean enabled)	{
		if(enabled != mEnabled)
			mInvalidated = true;
		mEnabled = enabled;
	}
	
	/**
	 * Injects time into the widget
	 * @param timespan Elapsed time in milliseconds
	 */
	public void Tick(int timespan)
	{
		if(mDepressedTime > 0)
		{
			mDepressedTime -= timespan;
			if(mDepressedTime <= 0)
				mInvalidated = true;
		}
	}
	
	/**
	 * Causes the backbuffer to be redrawn onto the frontbuffer, and apply text and fades on top of it
	 */
	private void Redraw()
	{
		Canvas canvas = new Canvas(mFrontbuffer);
		//Clear and replace with backbuffer content
		
		Paint clear_paint = new Paint();
		clear_paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
		clear_paint.setARGB(0, 0, 0, 0);
		canvas.drawPaint(clear_paint);

		canvas.drawBitmap(mBackbuffer, 0, 0, null);

		//Apply text
		Paint text_paint = new Paint();
		text_paint.setARGB(255, 255, 255, 255);
		text_paint.setTextSize(mFontSize);
		text_paint.setAntiAlias(true);
		text_paint.setTypeface(Typeface.SANS_SERIF);
		
		
		//Split text by newline character
		String[] split_a = mText.split("\n");
		ArrayList<String> split_b = new ArrayList<String>();
		for (String line : split_a) {
			//Iterate over characters, and split once long enough
			int char_read = 0;
			while(char_read < line.length())
			{
				int char_start = char_read;
				char_read += text_paint.breakText(line.substring(char_read), true, mBounds.width() - 2 * mFontBorder, null);
				if(char_read == char_start)
					char_read++; //Must read at least one character
				if(line.length() > char_read)
				{
					//Work backwards from this point to find best point to split at
					for(int i = char_read-1; i > char_start; i--)
					{
						if(line.charAt(i) == ' ')
						{
							char_read = i;
							break;
						}
					}
				}
				split_b.add(line.substring(char_start, char_read));
			}
		}
		
		

		
		int horizontalPosition = 0;
		switch(mHorizontalAlignment)
		{
		case Left:
			text_paint.setTextAlign(Align.LEFT);
			horizontalPosition = 4;
			break;
		case Middle:
			text_paint.setTextAlign(Align.CENTER);
			horizontalPosition = mBounds.width() / 2;
			break;
		case Right:
			text_paint.setTextAlign(Align.RIGHT);
			horizontalPosition = mBounds.right - 4;
			break;
		}
		int verticalPosition = 0;
		int lineHeight = (int)(text_paint.descent() - text_paint.ascent());
		switch(mVerticalAlignment)
		{
		case Top:
			verticalPosition = 8 + lineHeight;
			break;
		case Middle:
			verticalPosition = mBounds.height() / 2 - (lineHeight * (split_b.size() - 1)) / 2 + (int)text_paint.descent();
			break;
		case Bottom:
			verticalPosition = mBounds.height() - lineHeight * split_b.size() - 8;
			break;
		}
		for(int i = 0; i < split_b.size(); i++)
		{
			canvas.drawText(split_b.get(i), horizontalPosition, verticalPosition + lineHeight * i, text_paint);	
		}
		
		
		if(mDepressedTime > 0)
		{
			Paint fade_paint = new Paint();
			fade_paint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
			fade_paint.setARGB(100, 255, 255, 255);
			canvas.drawPaint(fade_paint);
		}
		if(!mEnabled)
		{
			Paint fade_paint = new Paint();
			fade_paint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
			fade_paint.setARGB(127, 0, 0, 0);
			canvas.drawPaint(fade_paint);			
		}
		mInvalidated = false;
	}
	
	/**
	 * Draws the widget onto a canvas
	 * @param canvas
	 */
	public void Draw(Canvas canvas, Vector2i offset)
	{
		if(mInvalidated)
		{
			Redraw(); 
		}
		canvas.drawBitmap(mFrontbuffer, mBounds.left + offset.x, mBounds.top + offset.y, null);
	}
	
	/**
	 * Reacts to a tap on the screen
	 * @param x
	 * @param y
	 */
	public void handleTap(int x, int y) {
		if(mBounds.contains(x, y))
		{
			if(mEnabled && mOnClick != null)
			{
				mDepressedTime = DepressedTime;
				mInvalidated = true;
				mOnClick.OnClick(this);
			}
			if(!mEnabled && mOnDisabledClick != null)
			{
				mOnDisabledClick.OnClick(this);
			}
		}
	}
	
	public void setPosition(Point position) {
		mBounds.offsetTo(position.x, position.y);
	}
}
