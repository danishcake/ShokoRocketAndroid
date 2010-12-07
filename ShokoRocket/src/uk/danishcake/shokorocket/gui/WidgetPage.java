package uk.danishcake.shokorocket.gui;

import java.util.ArrayList;

import uk.danishcake.shokorocket.simulation.Vector2i;
import android.graphics.Canvas;

/**
 * Represents a collection of widgets, and distributes events to them
 * @author Edward Woolhouse
 *
 */
public class WidgetPage {
	private ArrayList<Widget> mWidgets = new ArrayList<Widget>();
	private Vector2i mOffset = new Vector2i(0, 0);
	private int mFontSize = -1;
	private boolean mEnabled = true;
	
	public void addWidget(Widget widget) {
		mWidgets.add(widget);
		if(mFontSize > 0)
			widget.setFontSize(mFontSize);
	}
	
	public void handleTap(int x, int y) {
		if(!mEnabled) return;
		for (Widget widget : mWidgets) {
			widget.handleTap(x, y);
		}			
	}
	
	public void Draw(Canvas canvas) {
		for (Widget widget : mWidgets) {
			widget.Draw(canvas, mOffset);
		}
	}
	
	public void Tick(int timespan) {
		for (Widget widget : mWidgets) {
			widget.Tick(timespan);
		}	
	}
	
	public void setFontSize(int fontSize) {
		mFontSize = fontSize;
	}
	
	public void setOffset(int x, int y) {
		mOffset.x = x;
		mOffset.y = y;
	}
	
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}
}
