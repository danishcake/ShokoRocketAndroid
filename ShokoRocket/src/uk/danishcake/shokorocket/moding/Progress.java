package uk.danishcake.shokorocket.moding;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.danishcake.shokorocket.NL;
import uk.danishcake.shokorocket.simulation.SPWorld;


import android.content.Context;
import android.os.Environment;

/**
 * Represents the progress made, allows levels to be marked as completed
 * @author Edward Woolhouse
 */
public class Progress {
	/**
	 * Represents the status of a level, whether beaten or not.
	 * @author Edward Woolhouse
	 */
	private class ProgressRecord
	{
		public boolean beaten = false;
		public String filename = "";
	}
	/**
	 * Represents a directory or asset directory of levels
	 * @author Edward Woolhouse
	 */
	private class LevelPack
	{
		public String levelPackName = "";
		public ArrayList<ProgressRecord> levels = new ArrayList<ProgressRecord>();
		public int levelIndex;
	}

	private ArrayList<LevelPack> mLevels = new ArrayList<LevelPack>();
	private ArrayList<String> mUserLevels = null;
	private int mLevelPackIndex = 0;
	private Context mContext;
	
	/**
	 * Advances to next level pack, cycling at end
	 */
	public void nextLevelPack() {
		mLevelPackIndex++;
		mLevelPackIndex %= mLevels.size();
	}
	
	/**
	 * Advances to previous level pack, cycling at -1
	 */
	public void prevLevelPack() {
		mLevelPackIndex--;
		if(mLevelPackIndex < 0)
			mLevelPackIndex += mLevels.size();
	}
	
	/**
	 * Advances to the first unbeaten level
	 * If the current level is unbeaten it will not change
	 * Then it checks all others in the level pack
	 * Then it advances through the level packs
	 */
	public boolean gotoFirstUnbeaten() {
		if(!getBeaten()) 
			return false;
		if(nextUnbeaten())
			return true;
		else
		{
			for(int i = 0; i < mLevels.size(); i++)
			{
				mLevelPackIndex++;
				mLevelPackIndex %= mLevels.size();
				if(!getBeaten())
					return true;
				if(nextUnbeaten())
					return true;
			}
		}
		return false;
	}
	
	public void gotoTrainingPack() {
		for(int i = 0; i < mLevels.size(); i++)
		{
			if(mLevels.get(i).levelPackName.equals("11-Training"))
			{
				mLevelPackIndex = i;
			}
		}
	}
	
	/**
	 * Advances to next level within pack, cycling at end to first level with same pack
	 */
	public void nextLevel() {
		mLevels.get(mLevelPackIndex).levelIndex++;
		mLevels.get(mLevelPackIndex).levelIndex %= mLevels.get(mLevelPackIndex).levels.size();
	}
	
	/**
	 *  Advances to previous level within pack, cycling at -1 within the same pack
	 */
	public void prevLevel() {
		mLevels.get(mLevelPackIndex).levelIndex--;
		if(mLevels.get(mLevelPackIndex).levelIndex < 0)
			mLevels.get(mLevelPackIndex).levelIndex += mLevels.get(mLevelPackIndex).levels.size();	
	}
	
	/**
	 * Obtains the current level pack name
	 */
	public String getLevelPack() {
		return mLevels.get(mLevelPackIndex).levelPackName;
	}
	
	/**
	 * Obtains the name of the current level
	 */
	public String getLevel() {
		return mLevels.get(mLevelPackIndex).levels.get(mLevels.get(mLevelPackIndex).levelIndex).filename;
	}
	
	/**
	 * Obtains the size of the current level pack
	 */
	public int getLevelPackSize() {
		return mLevels.get(mLevelPackIndex).levels.size();
	}
	
	/**
	 * Obtains the index of the current level
	 * @return
	 */
	public int getLevelIndex() {
		return mLevels.get(mLevelPackIndex).levelIndex;
	}
	
	/**
	 * Gets the number of completed levels
	 * @return The number of completed levels within the current pack
	 */
	public int getCompletedCount() {
		int complete = 0;
		for(int i = 0; i < mLevels.get(mLevelPackIndex).levels.size(); i++)
		{
			if(mLevels.get(mLevelPackIndex).levels.get(i).beaten)
				complete++;
		}
		return complete;
	}
	
	/**
	 * Gets the beaten state of the current level
	 * @return true if the current level is beaten
	 */
	public boolean getBeaten() {
		return mLevels.get(mLevelPackIndex).levels.get(mLevels.get(mLevelPackIndex).levelIndex).beaten;
	}
	
	/**
	 * Advances to the next unbeaten level within the pack. If all are beaten already then will 
	 * not move position.
	 */
	public boolean nextUnbeaten() {
		LevelPack lp = mLevels.get(mLevelPackIndex);
		if(getCompletedCount() < lp.levels.size())
		{
			for(int i = 0; i < lp.levels.size(); i++)
			{
				lp.levelIndex++;
				lp.levelIndex %= lp.levels.size();
				if(!getBeaten())
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the levels stored on the SD card
	 */
	ArrayList<String> getUserLevels() {
		return mUserLevels;
	}
	
	/**
	 * Creates a level from the specified string in the form 
	 * assets:// or external storage://
	 */
	public SPWorld getWorld(String level_name) throws FileNotFoundException, IOException {
		if(level_name.startsWith("assets://"))
		{
			SPWorld world = new SPWorld(mContext.getAssets().open(level_name.substring(9)));
			world.setIdentifier(level_name);
			return world;
		} else
		{
			SPWorld world = new SPWorld(new FileInputStream(level_name));
			File level_file = new File(level_name);
			world.setFilename(level_file.getName());
			return world;
		}
	}
	
	/**
	 * Create a SPWorld from the currently selected level
	 * @return the currently selected world
	 */
	public SPWorld getWorld() throws FileNotFoundException, IOException {
		String level_name = getLevel();
		return getWorld(level_name);
	}
	
	public Progress(Context context)
	{
		mContext = context;
		Reload();
	}
	
	/**
	 * Causes the list of levels to be reloaded, and the progress to be reloaded
	 */
	public void Reload() {
		reloadLevels();
		reloadProgress();
	}
	
	/**
	 * Reloads the list of levels from assets and external storage
	 */
	private void reloadLevels() {
		try
		{
			String[] level_packs = mContext.getAssets().list("Levels");

			//If external storage mounted then search it for levels
			File[] user_packs = new File[]{};
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				File root = new File(Environment.getExternalStorageDirectory(), "ShokoRocket");
				user_packs = root.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isDirectory() && !pathname.getName().equals("Music");
					}
				});
				if(user_packs == null) //Folder doesn't exist
					user_packs = new File[]{};
			}
			
			mLevels.clear();
			mUserLevels = new ArrayList<String>();
			//First list levels in assets
			for (String level_pack : level_packs) {
				LevelPack lp = new LevelPack();
				lp.levelPackName = level_pack;
				
				String[] levels = mContext.getAssets().list("Levels/" + level_pack);
				for (String level : levels) {
					ProgressRecord pr = new ProgressRecord();
					pr.beaten = false;
					pr.filename = "assets://Levels/" + level_pack + "/" + level;
					lp.levels.add(pr);
					if(level_pack.equals("12-User contributed"))
					{
						mUserLevels.add(pr.filename);
					}
				}
				mLevels.add(lp);
			}
			
			//Now list levels on external storage
			for (File user_pack : user_packs)
			{
				LevelPack lp = new LevelPack();
				lp.levelPackName = user_pack.getName();
				
				File[] levels = user_pack.listFiles();
				for(File level : levels)
				{
					String path = level.getPath();
					if(path.endsWith(".Level"))
					{
						ProgressRecord pr = new ProgressRecord();
						pr.beaten = false;
						pr.filename = level.getAbsolutePath();
						lp.levels.add(pr);
					}
				}
				if(lp.levels.size() > 0)
					mLevels.add(lp);
			}
		} catch(IOException io_ex)
		{
			//TODO log
		}
	}
	
	/**
	 * Reloads the completed level list
	 */
	private void reloadProgress() {
		//Load
		try {
			FileInputStream is = mContext.openFileInput("ShokoRocketProgress.xml");
			javax.xml.parsers.DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setCoalescing(false);
			dbf.setExpandEntityReferences(false);
			javax.xml.parsers.DocumentBuilder dbuilder = dbf.newDocumentBuilder();
			Document document = dbuilder.parse(is);
			Element root = document.getDocumentElement();
						
			
			NodeList levels = NL.ElementsByTag(root, "Level");
			for(int i = 0; i < levels.getLength(); i++)
			{
				Node level = levels.item(i);
			  	Node text = level.getFirstChild();
				
				String identifier = text.getNodeValue();
				//Slow and silly way to set this, but works and speed not essential
				for(int j = 0; j < mLevels.size(); j++)
				{
					LevelPack lp = mLevels.get(j);
					for(int k = 0; k < lp.levels.size(); k++)
					{
						ProgressRecord pr = lp.levels.get(k); 
						if(pr.filename.equals(identifier))
						{
							pr.beaten = true;
						}
					}
				}
			}

			
		} catch(IOException io_ex) {
			//Do nothing
		} catch(ParserConfigurationException parse_config_error)
		{
			//Do nothing
		} catch(InvalidParameterException xml_error)
		{
			//Do nothing
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * Saves the completed level list
	 */
	private void saveData() {
		try {
			FileOutputStream os = mContext.openFileOutput("ShokoRocketProgress.xml", Context.MODE_PRIVATE);
			//javax.xml.Transform.* is not present until SDK 8, so write out XML by hand
			OutputStreamWriter osw = new OutputStreamWriter(os);
			osw.write("<Progress>\n");
			for(int j = 0; j < mLevels.size(); j++)
			{
				LevelPack lp = mLevels.get(j);
				for(int k = 0; k < lp.levels.size(); k++)
				{
					ProgressRecord pr = lp.levels.get(k); 
					if(pr.beaten)
					{
						osw.write("  <Level>" + pr.filename + "</Level>\n");
					}
				}
			}
			
			osw.write("</Progress>");
			
			osw.close();
		} catch (FileNotFoundException e) {
			//Shouldn't occur
		} catch(IOException io_ex) {
			//TODO log or something
		}
	}
	
	/**
	 * Marks a specified level as completed
	 */
	public void MarkComplete(String level)
	{
		for(int j = 0; j < mLevels.size(); j++)
		{
			LevelPack lp = mLevels.get(j);
			for(int k = 0; k < lp.levels.size(); k++)
			{
				ProgressRecord pr = lp.levels.get(k); 
				if(pr.filename.equals(level))
				{
					if(!pr.beaten)
					{
						pr.beaten = true;
						saveData();
					} else
					{
						pr.beaten = true;
					}
				}
			}
		}
	}
	
	/**
	 * Gets the total number of levels in all packs 
	 */
	public int getTotalLevelCount()
	{
		int total = 0;
		for(int j = 0; j < mLevels.size(); j++)
		{
			LevelPack lp = mLevels.get(j);
			for(int k = 0; k < lp.levels.size(); k++)
			{
				total++;
			}
		}
		return total;
	}
	
	/**
	 * Gets the number of levels beaten in all packs
	 */
	public int getBeatenLevelCount()
	{
		int beatenCount = 0;
		for(int j = 0; j < mLevels.size(); j++)
		{
			LevelPack lp = mLevels.get(j);
			for(int k = 0; k < lp.levels.size(); k++)
			{
				ProgressRecord pr = lp.levels.get(k); 
				if(pr.beaten) beatenCount++;
			}
		}
		return beatenCount;
	}
	
	public void AssessUnlockable(SkinProgress skin) {
		int beatenCount = 0;
		for(int j = 0; j < mLevels.size(); j++)
		{
			LevelPack lp = mLevels.get(j);
			for(int k = 0; k < lp.levels.size(); k++)
			{
				ProgressRecord pr = lp.levels.get(k); 
				if(pr.beaten) beatenCount++;
			}
		}
		
		//Pink mice unlocks at 5 beaten levels 
		if(beatenCount >= 5)
		{
			skin.unlockSkin("Animations/PinkMice.xml");
		}
		//Black mice unlocks at 30 beaten levels
		if(beatenCount >= 30)
		{
			skin.unlockSkin("Animations/ObsidianMice.xml");
		}
		//Ghost mice unlocks at 60 beaten levels
		if(beatenCount >= 60)
		{
			skin.unlockSkin("Animations/GhostMice.xml");
		}
		//Line mice unlocks at 100 beaten levels
		if(beatenCount >= 100)
		{
			skin.unlockSkin("Animations/Line.xml");
		}
		//Christmas theme unlocks if played during December
		Calendar cal = new GregorianCalendar();
		if(cal.get(Calendar.MONTH) == Calendar.DECEMBER)
		{
			skin.unlockSkin("Animations/Christmas.xml");
		}
		//Developer theme unlocks if a level submitted (do in editor)
	}
	
	/**
	 * Gets the completion state of the current level
	 */
	public boolean IsComplete(String level)
	{
		for(int j = 0; j < mLevels.size(); j++)
		{
			LevelPack lp = mLevels.get(j);
			for(int k = 0; k < lp.levels.size(); k++)
			{
				ProgressRecord pr = lp.levels.get(k); 
				if(pr.filename.equals(level) && pr.beaten)
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets if this is the first run
	 * @return true if the progress list has not been completed (eg no levels beaten)
	 */
	public static boolean IsFirstRun(Context context)
	{
		List<String> files = (List<String>)Arrays.asList(context.fileList()); 
		return !files.contains("ShokoRocketProgress.xml");
	}
}
