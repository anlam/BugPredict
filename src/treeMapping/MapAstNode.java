package treeMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;

public class MapAstNode {
	private ASTNode astNode;
	private int startLine, endLine;
	private ArrayList<Integer> lineStarts = new ArrayList<Integer>();
	private ArrayList<Integer> unChangedLines;
	private HashMap<ASTNode, ArrayList<ASTNode>> tree = new HashMap<ASTNode, ArrayList<ASTNode>>();
	private ArrayList<ASTNode> unchangedLeaves = new ArrayList<ASTNode>();
	private ArrayList<ASTNode> allLeaves = new ArrayList<ASTNode>();
	
	public MapAstNode(String fileContent, ASTNode astNode, int startLine)
	{
		this.astNode = astNode;
		this.startLine = startLine;
		this.endLine = startLine - 1;
		int index = astNode.getStartPosition()-1;
		do
		{
			lineStarts.add(index+1);
			this.endLine++;
			index = fileContent.indexOf('\n', index + 1);
		}
		while(index != -1 && index+1 <= astNode.getStartPosition() + astNode.getLength()-1);
	}
	
	public ASTNode getAstNode() {
		return astNode;
	}

	public void setAstNode(ASTNode node) {
		this.astNode = node;
	}
	public int getStartLine() {
		return startLine;
	}
	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}
	public int getEndLine() {
		return endLine;
	}
	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}
	public ArrayList<Integer> getLineStarts() {
		return lineStarts;
	}
	public void setLineStarts(ArrayList<Integer> lineStarts) {
		this.lineStarts = lineStarts;
	}

	public ArrayList<Integer> getUnChangedLines() {
		return unChangedLines;
	}

	public void setUnChangedLines(ArrayList<Integer> unChangedLines) {
		this.unChangedLines = unChangedLines;
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

	public void setUnchangedLeaves(ArrayList<ASTNode> unchangedLeaves) {
		this.unchangedLeaves = unchangedLeaves;
	}

	public ArrayList<ASTNode> getAllLeaves() {
		return allLeaves;
	}

	public void setAllLeaves(ArrayList<ASTNode> allLeaves) {
		this.allLeaves = allLeaves;
	}

	public void buildTree()
	{
		MapVisitor mapVisitor = new MapVisitor();
		mapVisitor.setUnchangedLines(new HashSet<Integer>(unChangedLines));
    	mapVisitor.setLines(lineStarts);
    	astNode.accept(mapVisitor);
    	tree = new HashMap<ASTNode, ArrayList<ASTNode>>(mapVisitor.getTree());
    	unchangedLeaves = new ArrayList<ASTNode>(mapVisitor.getUnchangedLeaves());
    	allLeaves = new ArrayList<ASTNode>(mapVisitor.getAllLeaves());
	}
	
}
