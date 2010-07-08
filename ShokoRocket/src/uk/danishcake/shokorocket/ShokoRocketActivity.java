package uk.danishcake.shokorocket;

import android.app.Activity;
import android.os.Bundle;

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
}