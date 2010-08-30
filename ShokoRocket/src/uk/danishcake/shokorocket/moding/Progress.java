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

/**
 * Represents the progress made, allows levels to be marked as completed
 * @author Edward Woolhouse
 */
public class Progress {
	private class ProgressRecord
	{
		public boolean beaten = false;
		public String filename = "";
	}
	private class LevelPack
	{
		public String levelPackName = "";
		public ArrayList<ProgressRecord> levels = new ArrayList<ProgressRecord>();
		public int levelIndex;
	}

	private ArrayList<LevelPack> mLevels = new ArrayList<LevelPack>();
	private int mLevelPackIndex = 0;
	private Context mContext;
	
	public void nextLevelPack() {
		mLevelPackIndex++;
		mLevelPackIndex %= mLevels.size();
	}
	public void prevLevelPack() {
		mLevelPackIndex--;
		if(mLevelPackIndex < 0)
			mLevelPackIndex += mLevels.size();
	}
	
	public void nextLevel() {
		mLevels.get(mLevelPackIndex).levelIndex++;
		mLevels.get(mLevelPackIndex).levelIndex %= mLevels.get(mLevelPackIndex).levels.size();
	}
	public void prevLevel() {
		mLevels.get(mLevelPackIndex).levelIndex--;
		if(mLevels.get(mLevelPackIndex).levelIndex < 0)
			mLevels.get(mLevelPackIndex).levelIndex += mLevels.get(mLevelPackIndex).levels.size();	
	}
	
	public String getLevelPack() {
		return mLevels.get(mLevelPackIndex).levelPackName;
	}
	
	public String getLevel() {
		return mLevels.get(mLevelPackIndex).levels.get(mLevels.get(mLevelPackIndex).levelIndex).filename;
	}
	
	public int getLevelPackSize() {
		return mLevels.get(mLevelPackIndex).levels.size();
	}
	
	public int getLevelIndex() {
		return mLevels.get(mLevelPackIndex).levelIndex;
	}
	
	public int getCompletedCount() {
		int complete = 0;
		for(int i = 0; i < mLevels.get(mLevelPackIndex).levels.size(); i++)
		{
			if(mLevels.get(mLevelPackIndex).levels.get(i).beaten)
				complete++;
		}
		return complete;
	}
	
	public boolean getBeaten() {
		return mLevels.get(mLevelPackIndex).levels.get(mLevels.get(mLevelPackIndex).levelIndex).beaten;
	}
	
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
	
	public World getWorld() throws FileNotFoundException, IOException {
		String level_name = getLevel();
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
	
	public Progress(Context context)
	{
		mContext = context;
		Reload();
	}
	
	public void Reload() {
		reloadLevels();
		reloadProgress();
	}
	
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
	
	public static boolean IsFirstRun(Context context)
	{
		List<String> files = (List<String>)Arrays.asList(context.fileList()); 
		return !files.contains("ShokoRocketProgress.xml");
	}
}
