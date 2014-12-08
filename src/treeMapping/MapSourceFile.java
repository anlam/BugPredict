package treeMapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class MapSourceFile implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	
	public static int nextID = 1;
	/**
	 * This will contain the file ID, which will be the hashCode
	 */
	private int fileID;
	private String filePath;
	private String fileContent;
	private ArrayList<Integer> lines = new ArrayList<Integer>();
	private ASTNode ast;
	private ArrayList<Integer> unchangedLines;
	private HashSet<Integer> commentLines = new HashSet<Integer>();
	private HashMap<ASTNode, ArrayList<ASTNode>> tree = new HashMap<ASTNode, ArrayList<ASTNode>>();
	private ArrayList<ASTNode> unchangedLeaves = new ArrayList<ASTNode>();
	private ArrayList<ASTNode> allLeaves = new ArrayList<ASTNode>();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.fileID;
	}
	public MapSourceFile(String fileContent)
	{
		this.fileID = nextID++;
		this.fileContent = fileContent;
		buildLines();
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	/**
	 * @return the lines
	 */
	public ArrayList<Integer> getLines() {
		return lines;
	}
	/**
	 * @param lines the lines to set
	 */
	public void setLines(ArrayList<Integer> lines) {
		this.lines = lines;
	}
	/**
	 * @return the ast
	 */
	public ASTNode getAst() {
		return ast;
	}
	/**
	 * @param ast the ast to set
	 */
	public void setAst(ASTNode ast) {
		this.ast = ast;
	}
	public ArrayList<Integer> getUnchangedLines() {
		return unchangedLines;
	}
	public void setUnchangedLines(ArrayList<Integer> unchangedLines) {
		this.unchangedLines = unchangedLines;
	}
	public HashSet<Integer> getCommentLines() {
		return commentLines;
	}
	public void setCommentLines(HashSet<Integer> commentLines) {
		this.commentLines = commentLines;
	}
	public HashMap<ASTNode, ArrayList<ASTNode>> getTree() {
		return tree;
	}
	public ArrayList<ASTNode> getUnchangedLeaves() {
		return unchangedLeaves;
	}
	public ArrayList<ASTNode> getAllLeaves() {
		return allLeaves;
	}
	/**
	 * @return the fileContent
	 */
	public String getFileContent() {
		return fileContent;
	}
	/**
	 * @param fileContent the fileContent to set
	 */
	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}
	public int getLineNumber(int pos)
	{
		return binSearch(pos, lines);
	}
	/**
	 * 
	 */
	public void buildLines()
	{
		lines = new ArrayList<Integer>();
    	int index = -1;
    	do {
    		index++;
    		lines.add(index);
    		index = fileContent.indexOf('\n', index);
    	} while (index > -1);
		
    	buildAST();
	}
	
	@SuppressWarnings("unchecked")
	private void buildAST() {
		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(fileContent.toCharArray());
    	parser.setCompilerOptions(options);
    	ast = parser.createAST(null);
	}
	/**
	 * 
	 */
	public void buildBlockCommentLines()
	{
		if(ast == null)
			return;
		int startChar = 0;
		int startLine = 0;
		int endChar = -1;
		int endLine = 0;
		StringBuffer buff = new StringBuffer();
		List<?> comments = ((CompilationUnit)ast).getCommentList();
		if (comments != null) {
			for (int i=0; i < comments.size(); ++i) {
				Comment comment = (Comment) comments.get(i);
				//System.out.println(comment.toString() + "\t" + comment.getStartPosition() + "\t" + comment.getLength());
				startChar = comment.getStartPosition();
				if(startChar < endChar)
					System.err.println("Something wrong");
				buff.append(fileContent.substring(endChar+1, startChar));
				startLine = binSearch(startChar, lines);
				endChar = comment.getStartPosition() + comment.getLength() - 1;
				endLine = binSearch(endChar, lines);
				if(endLine > startLine)
					for(int j = startLine; j <= endLine; j++)
						commentLines.add(j);
				if(comment.isDocComment())
				{
					buff.append("/* ");
					buff.append(fileContent.substring(startChar+3, endChar+1));
				}
				else
					buff.append(fileContent.substring(startChar, endChar+1));
			}
			buff.append(fileContent.substring(endChar + 1));
			fileContent = buff.toString();
			buildAST();
		}
	}
	/**
	 * 
	 */
	public void buildTree()
	{
		MapVisitor mapVisitor = new MapVisitor();
    	/*if(unchangedLines.size() == lines.size())
    		System.out.println("No changes made");*/
    	//mapVisitor.setChangedLines(new ArrayList<Integer>(changedLines));
		mapVisitor.setUnchangedLines(new HashSet<Integer>(unchangedLines));
    	mapVisitor.setLines(lines);
    	ast.accept(mapVisitor);
    	tree = new HashMap<ASTNode, ArrayList<ASTNode>>(mapVisitor.getTree());
    	unchangedLeaves = new ArrayList<ASTNode>(mapVisitor.getUnchangedLeaves());
    	allLeaves = new ArrayList<ASTNode>(mapVisitor.getAllLeaves());
	}
	private class Visitor extends ASTVisitor
	{
		private ArrayList<Integer> lines = new ArrayList<Integer>();
		private int status;
		public HashMap<ASTNode, ArrayList<ASTNode>> tree = new HashMap<ASTNode, ArrayList<ASTNode>>();
		public Visitor(ArrayList<Integer> lines, int status)
		{
			this.lines = lines;
			this.status = status;
		}
		public void preVisit(ASTNode node)
		{
			int nodeType = node.getNodeType();
			if(nodeType != ASTNode.BLOCK && nodeType != ASTNode.EXPRESSION_STATEMENT && node.getNodeType() != ASTNode.JAVADOC/*&& nodeType != ASTNode.PARENTHESIZED_EXPRESSION*/
					&& (nodeType != ASTNode.ARRAY_TYPE || node.getParent().getNodeType() != ASTNode.ARRAY_CREATION))
			{
				tree.put(node, new ArrayList<ASTNode>());
				ASTNode pNode = getParent(node);
				if(pNode != null && tree.containsKey(pNode))
				{
					node.setProperty(MapVisitor.propertyOffset, tree.get(pNode).size());
					node.setProperty(MapVisitor.propertyLocationID, (String)pNode.getProperty(MapVisitor.propertyLocationID) + tree.get(pNode).size() + ".");
					tree.get(pNode).add(node);
				}
				else
					node.setProperty(MapVisitor.propertyLocationID, ".");
				node.setProperty(MapVisitor.propertyStatus, status);
				node.setProperty(MapVisitor.propertyStartLine, binSearch(node.getStartPosition(), lines)+1);
				node.setProperty(MapVisitor.propertyEndLine, binSearch(node.getStartPosition()+node.getLength()-1, lines)+1);
			}
		}
		private ASTNode getParent(ASTNode node)
		{
			ASTNode pNode = node.getParent();
			while(pNode != null && 
					(pNode.getNodeType() == ASTNode.BLOCK || pNode.getNodeType() == ASTNode.EXPRESSION_STATEMENT 
							/*|| pNode.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION*/
					|| (pNode.getNodeType() == ASTNode.ARRAY_TYPE && pNode.getParent().getNodeType() == ASTNode.ARRAY_CREATION)))
				pNode = pNode.getParent();
			
			return pNode;
		}
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
	public void buildAddedTree()
	{
		Visitor visitor = new Visitor(this.lines, MapVisitor.STATUS_ADDED);
		ast.accept(visitor);
		this.tree = new HashMap<ASTNode, ArrayList<ASTNode>>(visitor.tree);
	}
	public void buildDeletedTree()
	{
		Visitor visitor = new Visitor(this.lines, MapVisitor.STATUS_DELETED);
		ast.accept(visitor);
		this.tree = new HashMap<ASTNode, ArrayList<ASTNode>>(visitor.tree);
	}
	/**
	 * 
	 */
	public void printTree(ASTNode root)
	{
		System.out.println(root.getProperty(MapVisitor.propertyLocationID) + 
				" line: " + root.getProperty(MapVisitor.propertyStartLine) + 
				" status: " + root.getProperty(MapVisitor.propertyStatus) +
				" vector: " + (root.getProperty(MapVectorVisitor.propertyFragment) != null));
		if(root.getProperty(MapVectorVisitor.propertyFragment) == null)
			System.out.println(root + "@" + root.getParent());
		ArrayList<ASTNode> children = tree.get(root);
		if(!children.isEmpty())
			for(ASTNode child : children)
				printTree(child);
	}
	/**
	 * 
	 * @param x
	 * @param al
	 * @return
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
