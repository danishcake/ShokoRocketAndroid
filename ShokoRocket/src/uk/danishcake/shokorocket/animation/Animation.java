package uk.danishcake.shokorocket.animation;

import java.util.ArrayList;
import android.graphics.Bitmap;

public class Animation {
	private ArrayList<Bitmap> mFrames = new ArrayList<Bitmap>();
	private int mFPS = 15;
	
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
	
	public void AddFrame(Bitmap frame) {
		mFrames.add(frame);
	}
	

	
	
}
