package uk.danishcake.shokorocket.gui;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Canvas;

/**
 * Represents a collection of widgets, and distributes events to them
 * @author Edward Woolhouse
 *
 */
public class WidgetPage {
	private ArrayList<Widget> mWidgets = new ArrayList<Widget>();
	
	public void addWidget(Widget widget) {
		mWidgets.add(widget);
	}
	
	public void handleTap(int x, int y) {
		for (Widget widget : mWidgets) {
			widget.handleTap(x, y);
		}			
	}
	
	public void Draw(Canvas canvas) {
		for (Widget widget : mWidgets) {
			widget.Draw(canvas);
		}	
	}
	
	public void Tick(int timespan) {
		for (Widget widget : mWidgets) {
			widget.Tick(timespan);
		}	
	}
}
