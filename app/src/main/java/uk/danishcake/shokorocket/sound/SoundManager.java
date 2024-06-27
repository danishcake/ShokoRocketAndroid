package uk.danishcake.shokorocket.sound;

import java.io.IOException;
import java.util.HashMap;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import android.util.Log;

public class SoundManager {
	private static SoundManager mInstance = null;
	private SoundPool mPool = null;
	private Context mContext = null;
	private AudioManager mAudioManager = null;
	private HashMap<String, Integer> mSoundIDs = new HashMap<String, Integer>();
	private boolean mEnabled = true;
	
	private boolean getEnabled() {return mEnabled;}
	private void setEnabled(boolean enabled) {
		mEnabled = enabled;
		MusicManager.setEnabled(enabled);
	}
	
	private SoundManager()
	{
		mPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
	}
	
	private static SoundManager GetInstance()
	{
		if(mInstance == null)
		{
			mInstance = new SoundManager();
		}
		return mInstance;
	}
	
	public static void Initialise(Context context)
	{
		SoundManager instance = GetInstance();
		instance.mContext = context;
		instance.mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	}
	
	public static int LoadSound(String file) throws IOException
	{
		SoundManager instance = GetInstance();
		if(instance.mSoundIDs.containsKey(file))
		{
			return instance.mSoundIDs.get(file);
		} else
		{
			int soundID = instance.mPool.load(instance.mContext.getAssets().openFd(file), 1);
			instance.mSoundIDs.put(file, soundID);

			return soundID;
		}
	}
	
	public static void PlaySound(int soundID)
	{
		SoundManager instance = GetInstance();
		if(soundID == -1 || !instance.getEnabled())
			return;
		
	    float streamVolume = instance.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	    streamVolume = streamVolume / instance.mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	    
		instance.mPool.play(soundID, streamVolume, streamVolume, 1, 0, 1.0f);
	}
	
	public static void PlaySound(String sound)
	{
		try
		{
			int sound_id = LoadSound(sound);
			PlaySound(sound_id);
		} catch(IOException io_ex)
		{
			Log.e("SoundManager.PlaySound", "Unable to load sound " + sound);
		}
	}
	
	public static boolean GetEnabled() {return GetInstance().getEnabled();}
	public static void SetEnabled(boolean enabled) {GetInstance().setEnabled(enabled);}
}
