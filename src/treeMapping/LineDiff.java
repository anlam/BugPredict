package treeMapping;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.core.dom.*;

import utils.StringProcessor;

public class LineDiff {
	private MapAstNode method1, method2;
	
	private class TreeVisitor extends ASTVisitor {
		private ArrayList<String> lineContents;
		private ArrayList<Integer> lineStarts;
		private boolean isLeaf = true;
		
		public TreeVisitor(ArrayList<Integer> lineStarts)
		{
			this.lineStarts = lineStarts;
			this.lineContents = new ArrayList<String>();
			for(int i = 0; i < lineStarts.size(); i++)
				lineContents.add("");
		}
		
		public ArrayList<String> getLineContents() {
			return lineContents;
		}

		public void preVisit(ASTNode node)
		{
			isLeaf = true;
			int nodeType = node.getNodeType();
			if(nodeType != ASTNode.BLOCK && nodeType != ASTNode.EXPRESSION_STATEMENT && node.getNodeType() != ASTNode.JAVADOC /*&& nodeType != ASTNode.PARENTHESIZED_EXPRESSION*/
					&& (nodeType != ASTNode.ARRAY_TYPE || node.getParent().getNodeType() != ASTNode.ARRAY_CREATION))
			{
				int line = binSearch(node.getStartPosition() + node.getLength() - 1, lineStarts);
		    	String code = node.getClass().getSimpleName();
				lineContents.set(line, lineContents.get(line) + code);
			}
		}
		public void postVisit(ASTNode node)
		{
			int nodeType = node.getNodeType();
			if(nodeType != ASTNode.BLOCK && nodeType != ASTNode.EXPRESSION_STATEMENT && nodeType != ASTNode.JAVADOC /*&& nodeType != ASTNode.PARENTHESIZED_EXPRESSION*/
					&& (nodeType != ASTNode.ARRAY_TYPE || node.getParent().getNodeType() != ASTNode.ARRAY_CREATION))
			{
				int line = binSearch(node.getStartPosition(), lineStarts);
		    	String code = "";
		    	if(isLeaf)
				{
		    		code = node.toString();
		    		isLeaf = false;
				}
				else
				{
					code = node.getClass().getSimpleName();
				}
		    	lineContents.set(line, lineContents.get(line) + code);
			}
			else
				isLeaf = false;
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
	
	public LineDiff(MapAstNode mm1, MapAstNode mm2)
	{
		this.method1 = mm1;
		this.method2 = mm2;
	}
	
	public ArrayList<ArrayList<Integer>> doDiff()
	{
		ArrayList<String> lines1 = new ArrayList<String>();
		if(method1 != null)
		{
			TreeVisitor visitor = new TreeVisitor(method1.getLineStarts());
			method1.getAstNode().accept(visitor);
			lines1 = visitor.getLineContents();
		}
		ArrayList<String> lines2 = new ArrayList<String>();
		if(method2 != null)
		{
			TreeVisitor visitor = new TreeVisitor(method2.getLineStarts());
			method2.getAstNode().accept(visitor);
			lines2 = visitor.getLineContents();
		}
		
		ArrayList<String> nonblankLines1 = new ArrayList<String>(), nonblankLines2 = new ArrayList<String>();
		HashMap<Integer, Integer> lineMap1 = new HashMap<Integer, Integer>(), lineMap2 = new HashMap<Integer, Integer>();
		for (int i = 0; i < lines1.size(); i++)
		{
			String line = lines1.get(i);
			if (!line.isEmpty())
			{
				lineMap1.put(nonblankLines1.size(), i);
				nonblankLines1.add(line);
			}
		}
		for (int i = 0; i < lines2.size(); i++)
		{
			String line = lines2.get(i);
			if (!line.isEmpty())
			{
				lineMap2.put(nonblankLines2.size(), i);
				nonblankLines2.add(line);
			}
		}
		ArrayList<Integer> lcsM = new ArrayList<Integer>();
		ArrayList<Integer> lcsN = new ArrayList<Integer>();
		StringProcessor.doLCS(nonblankLines1, nonblankLines2, 4, 0, lcsM, lcsN);
		if (lcsM.size() == nonblankLines1.size() && lcsN.size() == nonblankLines2.size())
			return null;
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < lcsM.size(); i++)
		{
			int n = lcsM.get(i);
			lcsM.set(i, lineMap1.get(n));
		}
		for (int i = 0; i < lcsN.size(); i++)
		{
			int n = lcsN.get(i);
			lcsN.set(i, lineMap2.get(n));
		}
		result.add(lcsM);
		result.add(lcsN);
		
		return result;
	}
	
	public ArrayList<ArrayList<Integer>> doDiff1()
	{
		ArrayList<String> lines1 = new ArrayList<String>();
		if(method1 != null)
		{
			TreeVisitor visitor = new TreeVisitor(method1.getLineStarts());
			method1.getAstNode().accept(visitor);
			lines1 = visitor.getLineContents();
		}
		ArrayList<String> lines2 = new ArrayList<String>();
		if(method2 != null)
		{
			TreeVisitor visitor = new TreeVisitor(method2.getLineStarts());
			method2.getAstNode().accept(visitor);
			lines2 = visitor.getLineContents();
		}
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		/*if(lines1.isEmpty())
		{
			result.add(new ArrayList<Integer>());
			ArrayList<Integer> in2 = new ArrayList<Integer>();
			for(int i = 0; i < lines2.size(); i++)
				in2.add(i);
			result.add(in2);
			
			return result;
		}
		else if(lines2.isEmpty())
		{
			ArrayList<Integer> in1 = new ArrayList<Integer>();
			for(int i = 0; i < lines1.size(); i++)
				in1.add(i);
			result.add(in1);
			result.add(new ArrayList<Integer>());
			
			return result;
		}*/
		int lenM = lines1.size(), lenN = lines2.size();
		int[][] d = new int[lenM+1][lenN+1];
		String[] codeM = new String[lenM+1];
		String[] codeN = new String[lenN+1];
		String[][] p = new String[lenM+1][lenN+1];
		d[0][0] = 0;
		for(int i = 1; i <= lenM; i++)
		{
			d[i][0] = 0;
			codeM[i] = lines1.get(i-1).toString();
		}
		for(int i = 1; i <= lenN; i++)
		{
			d[0][i] = 0;
			codeN[i] = lines2.get(i - 1).toString();
		}
		for(int i = 1; i <= lenM; i++)
		{
			for(int j = 1; j <= lenN; j++)
			{
				if(codeM[i].equals(codeN[j]))
				{
					d[i][j] = d[i-1][j-1] + 1;
					p[i][j] = "LU";
				}
				else if(d[i-1][j] >= d[i][j-1])
				{
					d[i][j] = d[i-1][j];
					p[i][j] = "U";
				}
				else
				{
					d[i][j] = d[i][j-1];
					p[i][j] = "L";
				}
			}
		}
		ArrayList<Integer> lcsM = new ArrayList<Integer>();
		ArrayList<Integer> lcsN = new ArrayList<Integer>();
		printLCS(p, lenM, lenN, lcsM, lcsN);
		
		result.add(lcsM);
		result.add(lcsN);
		
		return result;
	}
	/**
	 * 
	 */
	/*private void printLCS(String[][] p, int i, int j, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN)
	{
		if(i == 0 || j == 0)
			return;
		if(p[i][j].equals("LU"))
		{
			printLCS(p, i-1, j-1, lcsM, lcsN);
			lcsM.add(i-1);
			lcsN.add(j-1);
		}
		else if(p[i][j].equals("U"))
			printLCS(p, i-1, j, lcsM, lcsN);
		else
			printLCS(p, i, j-1, lcsM, lcsN);
	}*/
	private void printLCS(String[][] p, int lenM, int lenN, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN)
	{
		int i = lenM, j = lenN;
		while(i > 0 && j > 0)
		{
			if(p[i][j].equals("LU"))
			{
				lcsM.add(0, i-1);
				lcsN.add(0, j-1);
				i--; j--;
			}
			else if(p[i][j].equals("U"))
				i--;
			else
				j--;
		}
	}
}
