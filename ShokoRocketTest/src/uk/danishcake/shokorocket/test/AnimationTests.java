package uk.danishcake.shokorocket.test;

import java.io.IOException;
import java.util.Map;

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
	
	public void testBitmapLoadsAndCanBeCopied() {
		try
		{
			Bitmap bmp = BitmapFactory.decodeStream(getContext().getAssets().open("Bitmaps/UnitTest/UnitTest1.PNG"));
			
			assertNotNull(bmp);
			
			Bitmap sample = Bitmap.createBitmap(bmp, 4, 4, 16, 16);
			
			assertTrue(sample.getWidth() > 0);
			assertTrue(sample.getHeight() > 0);
		} catch(IOException io_exception)
		{
			assertTrue(false);
		}
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
	
	public void testLoadAnimtationSet() {
		try
		{
			Map<String, Animation> anset = Animation.GetAnimations(getContext(), "Animations/UnitTest/UnitTest.animation");
			assertEquals(2, anset.size());
		} catch (IOException ex)
		{
			assertTrue(false);
		}
	}
}
