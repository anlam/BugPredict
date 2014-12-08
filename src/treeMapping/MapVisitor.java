/**
 * 
 */
package treeMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * @author Hoan Anh Nguyen
 *
 */
public class MapVisitor extends ASTVisitor {
	//private String propertyChildren = "children";
	public static final String propertyLocationID = "lid";
	public static final String propertyOffset = "offset";
	public static final String propertyStatus = "status";
	public static final String propertyStartLine = "sl";
	public static final String propertyEndLine = "el";
	public static final int STATUS_UNCHANGED = 0;
	public static final int STATUS_PARTLY_CHANGED = 1;
	public static final int STATUS_FULLY_CHANGED = 2;
	public static final int STATUS_RELABELED = 3;
	public static final int STATUS_DELETED = 4;
	public static final int STATUS_ADDED = 5;
	public static final int STATUS_MOVED = 6;
	private ArrayList<Integer> lines = new ArrayList<Integer>();
	private ArrayList<Integer> changedLines;
	private HashSet<Integer> unchangedLines;
	private HashMap<ASTNode, ArrayList<ASTNode>> tree = new HashMap<ASTNode, ArrayList<ASTNode>>();
	private ArrayList<ASTNode> unchangedLeaves = new ArrayList<ASTNode>();
	private ArrayList<ASTNode> allLeaves = new ArrayList<ASTNode>();
	
	public MapVisitor()
	{

	}

	public ArrayList<Integer> getLines() {
		return lines;
	}

	public void setLines(ArrayList<Integer> lines) {
		this.lines = lines;
	}

	public ArrayList<Integer> getChangedLines() {
		return changedLines;
	}

	public void setChangedLines(ArrayList<Integer> changedLines) {
		this.changedLines = changedLines;
	}

	public HashSet<Integer> getUnchangedLines() {
		return unchangedLines;
	}

	public void setUnchangedLines(HashSet<Integer> unchangedLines) {
		this.unchangedLines = unchangedLines;
	}

	public HashMap<ASTNode, ArrayList<ASTNode>> getTree() {
		return tree;
	}

	public void setTree(HashMap<ASTNode, ArrayList<ASTNode>> tree) {
		this.tree = tree;
	}

	public ArrayList<ASTNode> getUnchangedLeaves() {
		return unchangedLeaves;
	}

	public ArrayList<ASTNode> getAllLeaves() {
		return allLeaves;
	}

	public void preVisit(ASTNode node)
	{
		int nodeType = node.getNodeType();
		if(nodeType != ASTNode.BLOCK && nodeType != ASTNode.EXPRESSION_STATEMENT && node.getNodeType() != ASTNode.JAVADOC/* && nodeType != ASTNode.PARENTHESIZED_EXPRESSION*/
				&& (nodeType != ASTNode.ARRAY_TYPE || node.getParent().getNodeType() != ASTNode.ARRAY_CREATION))
		{
			tree.put(node, new ArrayList<ASTNode>());
			ASTNode pNode = getParent(node);
			if(pNode != null && tree.containsKey(pNode))
			{
				node.setProperty(propertyOffset, tree.get(pNode).size());
				node.setProperty(propertyLocationID, (String)pNode.getProperty(propertyLocationID) + tree.get(pNode).size() + ".");
				tree.get(pNode).add(node);
			}
			else
				node.setProperty(propertyLocationID, ".");
			
			node.setProperty(propertyStatus, -1);
			node.setProperty(propertyStartLine, binSearch(node.getStartPosition(), lines)+1);
			node.setProperty(propertyEndLine, binSearch(node.getStartPosition()+node.getLength()-1, lines)+1);
		}
	}
	public void postVisit(ASTNode node)
	{
		int nodeType = node.getNodeType();
		if(nodeType != ASTNode.BLOCK && nodeType != ASTNode.EXPRESSION_STATEMENT && node.getNodeType() != ASTNode.JAVADOC/* && nodeType != ASTNode.PARENTHESIZED_EXPRESSION*/
				&& (nodeType != ASTNode.ARRAY_TYPE || node.getParent().getNodeType() != ASTNode.ARRAY_CREATION))
		{
			int status = (Integer)node.getProperty(propertyStatus);
			if(status == -1)
			{
				int startLine = binSearch(node.getStartPosition(), lines);
		    	int endLine = binSearch(node.getStartPosition() + node.getLength() - 1, lines);
		    	boolean isUnchanged = true;
		    	for(int i = startLine; i <= endLine; i++)
		    	{
		    		if(!unchangedLines.contains(i))
		    		{
		    			isUnchanged = false;
		    			break;
		    		}
		    	}
		    	if(isUnchanged)
		    	{
		    		status = STATUS_UNCHANGED;
		    		unchangedLeaves.add(node);
		    		allLeaves.add(node);
		    	}
		    	else
		    	{
		    		status = STATUS_FULLY_CHANGED;
		    		// TRICK allow all literals in leaves
		    		//if(nodeType != ASTNode.BOOLEAN_LITERAL && nodeType != ASTNode.CHARACTER_LITERAL && nodeType != ASTNode.NUMBER_LITERAL)
					allLeaves.add(node);
		    	}
				node.setProperty(propertyStatus, status);
		    }
			ASTNode pNode = getParent(node);
			if(pNode != null && tree.containsKey(pNode))
			{
				int pStatus = (Integer)pNode.getProperty(propertyStatus);
				if(pStatus == -1)
					pStatus = status;
				else if(pStatus != status)
					pStatus = STATUS_PARTLY_CHANGED;
				pNode.setProperty(propertyStatus, pStatus);
			}
		}
	}
	private ASTNode getParent(ASTNode node)
	{
		ASTNode pNode = node.getParent();
		while(pNode != null && 
				(pNode.getNodeType() == ASTNode.BLOCK || pNode.getNodeType() == ASTNode.EXPRESSION_STATEMENT /*|| pNode.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION*/
				|| (pNode.getNodeType() == ASTNode.ARRAY_TYPE && pNode.getParent().getNodeType() == ASTNode.ARRAY_CREATION)))
			pNode = pNode.getParent();
		
		return pNode;
	}
	/*
	 * 
	 */
	private int binSearch(int x, ArrayList<Integer> al) {
		if (al.size() <= 1) return al.size()-1;
		int low = 0, high = al.size()-1, mid;
		while(low < high) {
			mid = (low + high) / 2;
			if(al.get(mid) < x)
				low = mid + 1;
			else if(al.get(mid) > x)
				high = mid - 1;
			else
				return mid;
		}
		if (al.get(low) > x) return low - 1;
		else return low;
	}
}
