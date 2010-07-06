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
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.location.Address;

public class Animation {
	private ArrayList<Bitmap> mFrames = new ArrayList<Bitmap>();
	private int mFPS = 15;
	private int mTime = 0;
	private String mName = "Default";
	
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
	
	
	/* loadAnimation
	 * Takes an animation element from XML and loads the frames associated
	 */
	private static Animation loadAnimation(Context context, Element animation) throws IOException
	{
		Animation an = new Animation();
		String animation_name = animation.getAttribute("Name");
		String fps_string = animation.getAttribute("FPS");
		HashMap<String, Bitmap> src_cache = new HashMap<String, Bitmap>();
		int fps = Integer.getInteger(fps_string, 15);
		
		an.mFPS = fps;
		an.mName = animation_name;
		
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
				
				an.AddFrame(dest);
			} catch(NumberFormatException nfe)
			{
				//TODO error handling or logging or something
			}
		}
		
		return an;
	}
	
	/* GetAnimations
	 * 
	 */
	public static Map<String, Animation> GetAnimations(Context context, String file) throws IOException
	{
		try
		{
			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setCoalescing(false);
			dbf.setExpandEntityReferences(false);
			javax.xml.parsers.DocumentBuilder dbuilder = dbf.newDocumentBuilder();
			Document document = dbuilder.parse(context.getAssets().open(file));
			Element root = document.getDocumentElement();
						
			NodeList animations = root.getElementsByTagName("Animation");
			Map<String, Animation> animation_map = new HashMap<String, Animation>(); 
			for(int i = 0; i < animations.getLength(); i++)
			{
				Animation animation = loadAnimation(context, (Element)animations.item(i));
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
}
