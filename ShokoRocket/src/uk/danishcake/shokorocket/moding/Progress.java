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
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.danishcake.shokorocket.simulation.World;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

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
		public String display_name = "";
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
	 * Obtains the display name of the current level
	 */
	public String getLevelDisplayName() {
		return mLevels.get(mLevelPackIndex).levels.get(mLevels.get(mLevelPackIndex).levelIndex).display_name;
	}

	/**
	 * getLevelGrid
	 * @return 3x3 array of level filenames
	 */
	public String[][] getLevelGrid() {
		int lv_index;
		int lp_index;
		String[][] result = new String[3][3];
		int[] offset = {-1, 0, 1};
		for(int y = 0; y < 3; y++)
		{
			lp_index = mLevelPackIndex + offset[y];
			if(lp_index < 0) lp_index += mLevels.size();
			lp_index %= mLevels.size();
			for(int x = 0; x < 3; x++)
			{
				lv_index = mLevels.get(lp_index).levelIndex + offset[x];
				if(lv_index < 0) lv_index += mLevels.get(lp_index).levels.size();
				lv_index %= mLevels.get(lp_index).levels.size();
				result[x][y] = mLevels.get(lp_index).levels.get(lv_index).filename;
			}
		}
		return result;
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
	 * not move position
	 */
	public void nextUnbeaten() {
		LevelPack lp = mLevels.get(mLevelPackIndex);
		if(getCompletedCount() < lp.levels.size())
		{
			for(int i = 0; i < lp.levels.size(); i++)
			{
				lp.levelIndex++;
				lp.levelIndex %= lp.levels.size();
				if(!getBeaten())
					return;
			}
		}
	}
	
	/**
	 * Create a World from a level filename. Able to cope with assets://
	 * @return the currently selected world
	 */
	public World getWorld(String level_name)  throws FileNotFoundException, IOException {
		if(level_name.startsWith("assets://"))
		{
			World world = new World(mContext.getAssets().open(level_name.substring(9)));
			world.setIdentifier(level_name);
			return world;
		} else
		{
			World world = new World(new FileInputStream(level_name));
			File level_file = new File(level_name);
			world.setFilename(level_file.getName());
			
			return world;
		}
	}
	
	/**
	 * Create a World from the currently selected level
	 * @return the currently selected world
	 */
	public World getWorld() throws FileNotFoundException, IOException {
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
			int total_length = level_packs.length;
			
			//If external storage mounted then search it for levels
			File[] user_packs = new File[]{};
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				File root = new File(Environment.getExternalStorageDirectory(), "ShokoRocket");
				user_packs = root.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isDirectory();
					}
				});
				if(user_packs == null) //Folder doesn't exist
					user_packs = new File[]{};
				total_length += user_packs.length;
			}
			
			mLevels.clear();
			
			//First list levels in assets
			for (String level_pack : level_packs) {
				LevelPack lp = new LevelPack();
				lp.levelPackName = level_pack;
				
				String[] levels = mContext.getAssets().list("Levels/" + level_pack);
				for (String level : levels) {
					ProgressRecord pr = new ProgressRecord();
					pr.beaten = false;
					pr.filename = "assets://Levels/" + level_pack + "/" + level;
						
					try
					{
						World world = getWorld(pr.filename);
						pr.display_name = world.getLevelName();
					} catch (IOException io_ex) {
						Log.e("Progress.Reload", "Unable to load " + pr.filename);
						pr.display_name = "Error loading";
					}

					lp.levels.add(pr);
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
						try
						{
							World world = getWorld(pr.filename);
							pr.display_name = world.getLevelName();
						} catch (IOException io_ex) {
							Log.e("Progress.Reload", "Unable to load " + pr.filename);
							pr.display_name = "Error loading";
						}
						lp.levels.add(pr);
					}
				}
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
						
			NodeList levels = root.getElementsByTagName("Level");
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
