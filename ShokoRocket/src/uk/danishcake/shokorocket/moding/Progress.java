package uk.danishcake.shokorocket.moding;

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


import android.content.Context;

/**
 * Represents the progress made, allows levels to be marked as completed
 * @author Edward
 *
 */
public class Progress {
	private Context mContext;
	private ArrayList<String> mBeatenLevels = new ArrayList<String>();
	
	public Progress(Context context)
	{
		mContext = context;
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
				
				mBeatenLevels.add(text.getNodeValue());
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
			for (String level : mBeatenLevels) {
				osw.write("  <Level>" + level + "</Level>\n");
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
		if(!mBeatenLevels.contains(level))
		{
			mBeatenLevels.add(level);
			saveData();
		}
	}
	
	public boolean IsComplete(String level)
	{
		return mBeatenLevels.contains(level);
	}
	
	public static boolean IsFirstRun(Context context)
	{
		List<String> files = (List<String>)Arrays.asList(context.fileList()); 
		return !files.contains("ShokoRocketProgress.xml");
	}
}
