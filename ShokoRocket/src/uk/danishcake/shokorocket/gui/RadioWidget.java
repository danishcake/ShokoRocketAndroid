package uk.danishcake.shokorocket.gui;

import java.util.Iterator;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class RadioWidget extends Widget {
	protected Bitmap mBackbuffer_unset;
	protected Bitmap mBackbuffer_set;
	protected boolean mValue = false;
	protected OnClickListener mOnChangeListener;
	protected List<RadioWidget> mRadioGroup;

	/* Radio buttons have two state, toggled and untoggled. Internally handles toggling and toggle groups */
	public RadioWidget(NinePatchData set_np, NinePatchData unset_np, Rect sizeAndPosition, List<RadioWidget> radio_group) {
		super(set_np, sizeAndPosition);
		mRadioGroup = radio_group;
		
		mBackbuffer_unset = Bitmap.createBitmap(mBounds.width(), mBounds.height(), Bitmap.Config.ARGB_8888);
		drawNP(mBackbuffer_unset, unset_np);
		
		mBackbuffer_set = mBackbuffer;
		mBackbuffer = mBackbuffer_unset;
		setOnClickListener(new OnClickListener() {
			@Override
			public void OnClick(Widget widget) {
				setValue(!mValue);
			}
		});
		mInvalidated = true;
		mRadioGroup.add(this);
	}
	
	public void setValue(boolean value)
	{
		if(value)
		{
			if(!mValue) {
				if(mOnChangeListener != null)
					mOnChangeListener.OnClick(this);
				mInvalidated = true;
			}
			mValue = true;
			dominate_group();
		} else
		{
			if(mValue) {
				if(mOnChangeListener != null)
					mOnChangeListener.OnClick(this);
				mInvalidated = true;
			}
			mValue = false;
		}
		if(mValue)
			mBackbuffer = mBackbuffer_set;
		else
			mBackbuffer = mBackbuffer_unset;
	}
	
	public boolean getValue() {return mValue;}
	/**
	 * Unsets all other radio widgets in the group
	 */
	private void dominate_group()
	{
		Iterator<RadioWidget> it = mRadioGroup.iterator();
		while(it.hasNext())
		{
			RadioWidget rw = it.next();
			if(rw != this)
				rw.setValue(false);
		}
	}
	
	public void setOnValueChangedCallback(OnClickListener onChange)
	{
		mOnChangeListener = onChange;
	}
}
