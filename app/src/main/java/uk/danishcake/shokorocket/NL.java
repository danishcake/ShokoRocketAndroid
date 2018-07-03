package uk.danishcake.shokorocket;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;

public class NL implements NodeList {
	private ArrayList<Node> mNodes;
	
	private NL()
	{
		mNodes = new ArrayList<Node>();
	}
	
	public static NodeList ElementsByTag(Element element, String tag)
	{
		NodeList all_nodes = element.getChildNodes();
		NL tag_nodes = new NL();
		
		for(int i = 0; i < all_nodes.getLength(); i++)
		{
			short node_type = all_nodes.item(i).getNodeType();
			String node_tag = all_nodes.item(i).getNodeName();
			
			if(node_type == Node.ELEMENT_NODE && node_tag.equals(tag))
				tag_nodes.mNodes.add(all_nodes.item(i));
		}

		return tag_nodes;
	}

	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return mNodes.size();
	}

	@Override
	public Node item(int arg0) {
		// TODO Auto-generated method stub
		return mNodes.get(arg0);
	}
}
