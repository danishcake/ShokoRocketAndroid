package uk.danishcake.shokorocket.animation;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class Animation {
	private ArrayList<Bitmap> mFrames = new ArrayList<Bitmap>();
	private int mFPS = 15;
	private int mTime = 0;
	private String mName = "Default";
	private int mOffsetX = 0;
	private int mOffsetY = 0;
	
	/* getFrameCount
	 * @return the number of frames in the animation 
	 */
	public int getFrameCount() {
		return mFrames.size();
	}
	
	/* getFPS
	 * @return the FPS of the animation
	 */
	public int getFPS() {
		return mFPS;	
	}
	/* setFPS
	 * Sets the FPS of the animation
	 */
	public void setFPS(int fps) {
		mFPS = fps;
	}
	
	public String getName() {
		return mName;
	}

	/* AddFrame
	 * Manually adds an extra frame to the animation
	 */
	public void AddFrame(Bitmap frame) {
		mFrames.add(frame);
	}
	
	public void Tick(int timespan)
	{
		mTime += timespan;
		
		int frameTime = 1000 / mFPS;
		int animationTime = frameTime * mFrames.size();
		if(mTime > animationTime)
		{
			mTime -= animationTime;
		}
	}
	
	/* getCurrentFrame
	 * Returns the current frame
	 */
	public Bitmap getCurrentFrame() {
		int frameTime = 1000 / mFPS;
		int index = mTime / frameTime;
		index %= mFrames.size();
		return mFrames.get(index);
	}
	
	/* getFrameByIndex
	 * returns the nth frame. If index is outside bounds will return index % frame count
	 */
	public Bitmap getFrameByIndex(int index) {
		index %= mFrames.size();
		return mFrames.get(index);
	}
	
	/**
	 * Gets the frame at a particular time
	 * @param time milliseconds
	 */
	public Bitmap getFrameByTime(int time) {
		int frameTime = 1000 / mFPS;
		int index = time / frameTime;
		index %= mFrames.size();
		return mFrames.get(index);
	}
	
	/**
	 * Draws the current frame at the specified location. It will be automatically be adjusted for offset 
	 * @param canvas The canvas to draw with
	 * @param x The x coordinate (before offsets)
	 * @param y The y coordinate (before offsets)
	 */
	public void DrawCurrentFrame(Canvas canvas, int x, int y)
	{
		canvas.drawBitmap(getCurrentFrame(), x + mOffsetX, y + mOffsetY, null);
	}
	
	/**
	 * Draws the frame at the specified time at the specified loation, adjusted for offsets
	 * @param canvas
	 * @param x
	 * @param y
	 * @param time
	 */
	public void DrawFrameAtTime(Canvas canvas, int x, int y, int time)
	{
		canvas.drawBitmap(getFrameByTime(time), x + mOffsetX, y +mOffsetY, null);
	}	
	
	/* loadAnimation
	 * Takes an animation element from XML and loads the frames associated
	 */
	public static Animation loadAnimation(Context context, Element animation, float scale) throws IOException
	{
		Animation an = new Animation();
		String animation_name = animation.getAttribute("Name");
		String fps_string = animation.getAttribute("FPS");
		String xOffsetString = animation.getAttribute("OffsetX");
		String yOffsetString = animation.getAttribute("OffsetY");
		
		HashMap<String, Bitmap> src_cache = new HashMap<String, Bitmap>();
		int fps = 15;
		try
		{
			fps = Integer.parseInt(fps_string);	
		} catch(NumberFormatException nfe)
		{
			//Nothing to do
		}
	
		int xOffset = 0;
		try
		{
			xOffset = (int)(Integer.parseInt(xOffsetString) * scale);	
		} catch(NumberFormatException nfe)
		{
			//Nothing to do
		}
		
		int yOffset = 0;
		try
		{
			yOffset = (int)(Integer.parseInt(yOffsetString) * scale);	
		} catch(NumberFormatException nfe)
		{
			//Nothing to do
		}
		
		an.mFPS = fps;
		an.mName = animation_name;
		an.mOffsetX = xOffset;
		an.mOffsetY = yOffset;
		
		NodeList frames = ((Element)animation).getElementsByTagName("Frame");
		for(int i = 0; i < frames.getLength(); i++)
		{
			Element frame = (Element)frames.item(i);
			String filename = frame.getAttribute("File");
			String top_str = frame.getAttribute("Top");
			String left_str = frame.getAttribute("Left");
			String height_str = frame.getAttribute("Height");
			String width_str = frame.getAttribute("Width");
			
			try
			{
				int top = Integer.parseInt(top_str);
				int left = Integer.parseInt(left_str);
				int width = Integer.parseInt(width_str);
				int height = Integer.parseInt(height_str);
			
	
				Bitmap src;
				if(src_cache.containsKey(filename))
					src = src_cache.get(filename);
				else
				{
					InputStream bmp = context.getAssets().open(filename);
					src = BitmapFactory.decodeStream(bmp);
				}
				src_cache.put(filename, src);
				Bitmap dest = Bitmap.createBitmap(src, left, top, width, height);
				if(scale != 1.0f)
				{
					int scaled_width = (int)(dest.getWidth() * scale);
					int scaled_height = (int)(dest.getHeight() * scale);
					if(scaled_width == 0) scaled_width = 1;
					if(scaled_height == 0) scaled_height = 1;
					dest = Bitmap.createScaledBitmap(dest, scaled_width, scaled_height, true);
				}
				
				an.AddFrame(dest);
			} catch(NumberFormatException nfe)
			{
				//TODO error handling or logging or something
			}
		}
		for (Bitmap bitmap : src_cache.values()) {
			bitmap.recycle();
		}
		src_cache.clear();
		
		return an;
	}
	
	public static Map<String, Animation> GetAnimations(Context context, String file) throws IOException
	{
		return GetAnimations(context, file, 1.0f);
	}
	
	public static Map<String, Animation> GetAnimations(Context context, int res_id) throws IOException
	{
		return GetAnimations(context, res_id, 1.0f);
	}
	
	/* GetAnimations
	 * 
	 */
	public static Map<String, Animation> GetAnimations(Context context, int res_id, float scale) throws IOException
	{
	 	return GetAnimations(context, context.getResources().openRawResource(res_id), scale);
	
	}
	
	/* GetAnimations
	 * 
	 */
	public static Map<String, Animation> GetAnimations(Context context, String file, float scale) throws IOException
	{
		return GetAnimations(context, context.getAssets().open(file), scale);
	}
	
	public static Map<String, Animation> GetAnimations(Context context, InputStream input, float scale) throws IOException
	{
		try
		{
			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setCoalescing(false);
			dbf.setExpandEntityReferences(false);
			javax.xml.parsers.DocumentBuilder dbuilder = dbf.newDocumentBuilder();
			Document document = dbuilder.parse(input);
			Element root = document.getDocumentElement();
						
			NodeList animations = root.getElementsByTagName("Animation");
			Map<String, Animation> animation_map = new HashMap<String, Animation>(); 
			for(int i = 0; i < animations.getLength(); i++)
			{
				Animation animation = loadAnimation(context, (Element)animations.item(i), scale);
				animation_map.put(animation.getName(), animation);
			}
			return animation_map;
		}
		catch(ParserConfigurationException parse_config_error)
		{
			throw new IOException("Unable to create parser to read XML: " + parse_config_error.getMessage());
		}
		catch(SAXException sax_error)
		{
			throw new IOException("Unable to load level due to SAX exception: " + sax_error.getMessage());
		}
		catch(InvalidParameterException xml_error)
		{
			throw new IOException("Unable to load level due to XML parameter error : " + xml_error.getMessage());
		}		
	}
	
	/* Animation
	 * Creates an empty animation
	 */
	public Animation()
	{

	}
	
	/**
	 * Attempts to free all resources associated with this animation
	 */
	public void Teardown()
	{
		if(mFrames != null)
		{
			for (Bitmap bitmap : mFrames) {
				if(bitmap != null)
					bitmap.recycle();
			}
			mFrames.clear();
		}
		mFrames = null;
	}
}
