package uk.danishcake.shokorocket.test;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;
import uk.danishcake.shokorocket.animation.*;

public class AnimationTests extends AndroidTestCase {

	public void testEmptyAnimationConstructor()
	{
		Animation animation = new Animation();
		assertEquals(0, animation.getFrameCount());
		assertEquals(15, animation.getFPS());
	}
	
	public void testManualAddingFrames() {
		Animation animation = new Animation();
		try
		{
			Bitmap bmp = BitmapFactory.decodeStream(getContext().getAssets().open("Bitmaps/UnitTest/UnitTest1.PNG"));
			Bitmap bmp2 = BitmapFactory.decodeStream(getContext().getAssets().open("Bitmaps/UnitTest/UnitTest2.PNG"));
			animation.AddFrame(bmp);
			animation.AddFrame(bmp2);
			
			assertEquals(2, animation.getFrameCount());
		} catch (IOException ex)
		{
			assertTrue(false);
		}
	}
	
	public void testLoadAnimationFromFile() { 
		
	}
}
