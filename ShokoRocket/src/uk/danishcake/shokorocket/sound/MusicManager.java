package uk.danishcake.shokorocket.sound;

import java.io.File;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import android.os.Environment;
import android.util.Log;

public class MusicManager implements OnPreparedListener {
	private enum MusicMode
	{
		Menu, Multiplayer, Puzzle, None
	}
	
	private MediaPlayer mMediaPlayer = null;
	private static MusicManager mInstance = null;
	private static boolean mEnabled = true;
	private static MusicMode mMusicMode = MusicMode.None;

	private MusicManager()
	{
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnPreparedListener(this);
	}
	
	private static MusicManager GetInstance()
	{
		if(mInstance == null)
		{
			mInstance = new MusicManager();
		}
		return mInstance;
	}
	
	private static void SetMusic(FileDescriptor fd) {
		try
		{
			MusicManager instance = GetInstance();
			instance.mMediaPlayer.reset();
			if(fd != null)
			{
				instance.mMediaPlayer.setDataSource(fd);
				instance.mMediaPlayer.prepareAsync();
				instance.mMediaPlayer.setLooping(true);
			}
		} catch(IOException io_ex)
		{
			Log.e("ShokoRocket.SoundManager", "Unable to load music");
		}
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		if(mEnabled)
			mp.start();
	}
	
	private static void PlayMusic() {
		String music = "nomusic";
		switch(mMusicMode)
		{
		case Menu:
			music = "Menu";
			break;
		case Multiplayer:
			music = "Multiplayer";
			break;
		case Puzzle:
			music = "Puzzle";
			break;
		}
		
		File root = new File(Environment.getExternalStorageDirectory(), "ShokoRocket");
		File music_folder = new File(root, "Music");
		File music_filestem = new File(music_folder, music);
		File music_ogg = new File(music_filestem.getAbsolutePath() + ".ogg");
		File music_mp3 = new File(music_filestem.getAbsolutePath() + ".mp3");
		
		File music_file = music_ogg;
		if(!music_file.exists())
			music_file = music_mp3;

		try
		{
			FileInputStream fis = new FileInputStream(music_file);
			SetMusic(fis.getFD());
		} catch(IOException io_ex)
		{
			Log.e("ShokoRocket.MusicManager", "Unable to open Menu.ogg in ShokoRocket/Music");
		}
	}
	
	public static void PlayMenuMusic() {
		if(mMusicMode != MusicMode.Menu)
		{
			mMusicMode = MusicMode.Menu;
			PlayMusic();
		}
	}
	
	public static void PlayMultiplayerMusic()
	{
		if(mMusicMode != MusicMode.Multiplayer)
		{
			mMusicMode = MusicMode.Multiplayer;
			PlayMusic();
		}
	}
	
	public static void PlayPuzzleMusic()
	{
		if(mMusicMode != MusicMode.Puzzle)
		{
			mMusicMode = MusicMode.Puzzle;
			PlayMusic();
		}
	}
	
	/**
	 * Called when sound is toggled on/off in menu
	 * @param enabled
	 */
	public static void setEnabled(boolean enabled)
	{
		MusicManager instance = GetInstance();
		if(mEnabled && !enabled)
		{
			instance.mMediaPlayer.pause();
		}
		if(!mEnabled && enabled)
		{
			instance.mMediaPlayer.start();
		}
		mEnabled = enabled;
	}
	
	public static void ReleaseMusic()
	{
		if(mInstance != null)
		{
			mInstance.mMediaPlayer.release();
			mInstance.mMediaPlayer = null;
			mInstance = null;
		}
	}
	
	public static void ResumeMusic()
	{
		if(mInstance != null)
		{
			mInstance.mMediaPlayer.release();
			mInstance.mMediaPlayer = null;
			mInstance = null;
		}
		//Create new instance and play last played music
		PlayMusic();
	}
}
