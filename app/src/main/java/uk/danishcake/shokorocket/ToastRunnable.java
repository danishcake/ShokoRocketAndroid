package uk.danishcake.shokorocket;

import android.content.Context;
import android.widget.Toast;

public class ToastRunnable implements Runnable {
	Context mContext;
	String mText;
	int mDuration;
	
	public ToastRunnable(Context context, String text, int duration)
	{
		mContext = context;
		mText = text;
		mDuration = duration;
	}
	
	@Override
	public void run() {
		Toast.makeText(mContext, mText, mDuration).show();
	}

}
