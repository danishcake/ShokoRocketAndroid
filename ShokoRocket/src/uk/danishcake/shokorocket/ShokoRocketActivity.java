package uk.danishcake.shokorocket;

import uk.danishcake.shokorocket.simulation.Direction;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

public class ShokoRocketActivity extends Activity {
	
	private GameView mGameView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGameView = new GameView(getApplicationContext());
        setContentView(mGameView);
    }
    
    @Override
    public void onBackPressed() {
    	//Sometimes I want to override back
    	if(!mGameView.OverrideBack())
    		super.onBackPressed();
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
		{
			mGameView.HandleDPad(Direction.South);
			return true;
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_UP)
		{
			mGameView.HandleDPad(Direction.North);
			return true;
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
		{
			mGameView.HandleDPad(Direction.West);
			return true;
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
		{
			mGameView.HandleDPad(Direction.East);
			return true;
		}
		
		return false;
    }
}