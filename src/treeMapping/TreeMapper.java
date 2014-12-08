package treeMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TreeMapper {
	public static final String propertyRevisionMap = "rMap";
	private static final int MAX_NUM_PAIR_COMPARISONS = 100;
	private final String propertySimilarity = "similarity";
	private final double simThreshold = 0.75; // TODO need to investigate
	private ASTNode astM, astN;
	private HashMap<ASTNode, ArrayList<ASTNode>> treeM, treeN;
	
	public TreeMapper() {
		
	}
	
	public HashMap<ASTNode, ArrayList<ASTNode>> getTreeM() {
		return treeM;
	}

	public HashMap<ASTNode, ArrayList<ASTNode>> getTreeN() {
		return treeN;
	}

	public int map(MapAstNode mm1, MapAstNode mm2)
	{
		mm1.buildTree();
		mm2.buildTree();
		
		astM = mm1.getAstNode();
		astN = mm2.getAstNode();
		treeM = new HashMap<ASTNode, ArrayList<ASTNode>>(mm1.getTree());
		treeN = new HashMap<ASTNode, ArrayList<ASTNode>>(mm2.getTree());
		
		int result = mapIsomorphicLeaves(mm1.getAllLeaves(), mm2.getAllLeaves());
		if (result == -1)
			return -1;
		
		MapVectorVisitor vectorVisitor = new MapVectorVisitor();
		astM.accept(vectorVisitor);
		vectorVisitor = new MapVectorVisitor();
		astN.accept(vectorVisitor);
		vectorVisitor = null;
		
		astM.setProperty(propertyRevisionMap, astN);
		astM.setProperty(propertySimilarity, 1.0);
		astN.setProperty(propertyRevisionMap, astM);
		astN.setProperty(propertySimilarity, 1.0);
		
		mapDeletion(astM);
		
		mapAddition(astN);
		
		mapMoving(astM);
		markAddition(astN);
		
		mapRelabeling(astM, treeM);
		
		checkUnchange(astM, treeM);
		checkUnchange(astN, treeN);
		mapUnchange(astM, treeM);
		mapUnchange(astN, treeN);
		
		/*printTree(treeM, astM);
		printTree(treeN, astN);*/
		return 0;
	}
	/**
	 * 
	 */
	private int mapIsomorphicLeaves(ArrayList<ASTNode> leavesM, ArrayList<ASTNode> leavesN)
	{
		int m = -1, n = -1;
		ArrayList<ASTNode> deletedLeaves = new ArrayList<ASTNode>(), addedLeaves = new ArrayList<ASTNode>();
		while(m < leavesM.size() || n < leavesN.size())
		{
			int prevM = m, prevN = n;
			m++; n++;
			boolean hasNeighbor = false;
			ASTNode leafM = null;
			ASTNode leafN = null;
			while(!hasNeighbor && (m < leavesM.size() || n < leavesN.size()))
			{
				while(m < leavesM.size())
				{
					leafM = leavesM.get(m);
					int status = (Integer)leafM.getProperty(MapVisitor.propertyStatus);
					if(status == MapVisitor.STATUS_UNCHANGED)
						break;
					m++;
				}
				while(n < leavesN.size())
				{
					leafN = leavesN.get(n);
					int status = (Integer)leafN.getProperty(MapVisitor.propertyStatus);
					if(status == MapVisitor.STATUS_UNCHANGED)
						break;
					n++;
				}
				if(m < leavesM.size() && n < leavesN.size())
				{
					hasNeighbor = false;
					/*if(!leafM.toString().equals(leafN.toString()))
					{
						System.out.println(leafM.getProperty(MapVisitor.propertyLocationID) + "@" + 
						leafM.getProperty(MapVisitor.propertyStartLine));
						System.out.println(leafM);
						System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
						System.out.println(leafN.getProperty(MapVisitor.propertyLocationID) + "@" + 
								leafN.getProperty(MapVisitor.propertyStartLine));
						System.out.println(leafN);
					}*/
					if(m > 0 && (Integer)leavesM.get(m-1).getProperty(MapVisitor.propertyStatus) == MapVisitor.STATUS_UNCHANGED)
						hasNeighbor = true;
					int nodeTypeM = leafM.getNodeType();
					if(nodeTypeM != ASTNode.RETURN_STATEMENT && nodeTypeM != ASTNode.BREAK_STATEMENT && nodeTypeM != ASTNode.CONTINUE_STATEMENT && 
							getParent(leafM).getNodeType() != ASTNode.RETURN_STATEMENT && 
							m < leavesM.size()-1 && 
							(Integer)leavesM.get(m+1).getProperty(MapVisitor.propertyStatus) == MapVisitor.STATUS_UNCHANGED)
						hasNeighbor = true;
					if(!hasNeighbor)
					{
						if(n > 0 && (Integer)leavesN.get(n-1).getProperty(MapVisitor.propertyStatus) == MapVisitor.STATUS_UNCHANGED)
							hasNeighbor = true;
						int nodeTypeN = leafN.getNodeType();
						if(nodeTypeN != ASTNode.RETURN_STATEMENT && nodeTypeN != ASTNode.BREAK_STATEMENT && nodeTypeN != ASTNode.CONTINUE_STATEMENT &&
								getParent(leafN).getNodeType() != ASTNode.RETURN_STATEMENT && 
								n < leavesN.size()-1 && 
								(Integer)leavesN.get(n+1).getProperty(MapVisitor.propertyStatus) == MapVisitor.STATUS_UNCHANGED)
							hasNeighbor = true;
					}
					if(!hasNeighbor)
					{
						leafM.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_FULLY_CHANGED);
						leafN.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_FULLY_CHANGED);
					}
				}
				else if(m < leavesM.size() || n < leavesN.size())
				{
					// FIXME Something wrong with isomorphic leaves
					System.out.println("Something wrong with isomorphic leaves HERE");
					if(m < leavesM.size())
					{
						leafM = leavesM.get(m);
						String pidM = (String)leafM.getProperty(MapVisitor.propertyLocationID);
						System.out.println("\n\n");
						System.out.println(pidM + "@" + leafM.getProperty(MapVisitor.propertyStartLine));
						System.out.println(leafM);
						
					}
					else
					{
						leafN = leavesN.get(n);
						String pidN = (String)leafN.getProperty(MapVisitor.propertyLocationID);
						System.out.println("\n\n");
						System.out.println(pidN + "@" + leafN.getProperty(MapVisitor.propertyStartLine));
						System.out.println(leafN);
					}
					return -1;
				}
			}
			
			if(m < leavesM.size() && n < leavesN.size())
			{
				leafM.setProperty(propertyRevisionMap, leafN);
				leafM.setProperty(propertySimilarity, 1.0);
				leafN.setProperty(propertyRevisionMap, leafM);
				leafN.setProperty(propertySimilarity, 1.0);
				/*System.out.println(leafM.getProperty(MapVisitor.propertyLocationID) + "@" + 
						leafM.getProperty(MapVisitor.propertyStartLine));
				System.out.println(leafM);
				System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
				System.out.println(leafN.getProperty(MapVisitor.propertyLocationID) + "@" + 
						leafN.getProperty(MapVisitor.propertyStartLine));
				System.out.println(leafN);*/
			}
			else if(m < leavesM.size() || n < leavesN.size())
			{
				// FIXME Something wrong with isomorphic leaves
				System.out.println("Something wrong with isomorphic leaves");
				if(m < leavesM.size())
				{
					leafM = leavesM.get(m);
					String pidM = (String)leafM.getProperty(MapVisitor.propertyLocationID);
					System.out.println("\n\n");
					System.out.println(pidM + "@" + leafM.getProperty(MapVisitor.propertyStartLine));
					System.out.println(leafM);
					
				}
				else
				{
					leafN = leavesN.get(n);
					String pidN = (String)leafN.getProperty(MapVisitor.propertyLocationID);
					System.out.println("\n\n");
					System.out.println(pidN + "@" + leafN.getProperty(MapVisitor.propertyStartLine));
					System.out.println(leafN);
				}
				return -1;
			}
			int lenM = (m-1) - (prevM+1) + 1;
			int lenN = (n-1) - (prevN+1) + 1;
			if(lenM > 0 && lenN > 0)
			{
				int[][] d = new int[lenM+1][lenN+1];
				String[] codeM = new String[lenM+1];
				String[] codeN = new String[lenN+1];
				String[][] p = new String[lenM+1][lenN+1];
				d[0][0] = 0;
				for(int i = 1; i <= lenM; i++)
				{
					d[i][0] = 0;
					codeM[i] = leavesM.get(i + prevM).toString();
					/*System.out.println(i + " - " + codeM[i] + "@" + 
							leavesM.get(i + prevM).getProperty(MapVisitor.propertyStartLine) + "@" +
							leavesM.get(i + prevM).getProperty(MapVisitor.propertyStatus));*/
				}
				for(int i = 1; i <= lenN; i++)
				{
					d[0][i] = 0;
					codeN[i] = leavesN.get(i + prevN).toString();
					/*System.out.println(i + " - " + codeN[i] + "@" + 
							leavesN.get(i + prevN).getProperty(MapVisitor.propertyStartLine) + "@" +
							leavesN.get(i + prevN).getProperty(MapVisitor.propertyStatus));*/
				}
				for(int i = 1; i <= lenM; i++)
				{
					for(int j = 1; j <= lenN; j++)
					{
						boolean matched = codeM[i].equals(codeN[j]);
						if (matched) {
							int astNodeType = leavesM.get(i + prevM).getNodeType();
							if (astNodeType == ASTNode.BOOLEAN_LITERAL || astNodeType == ASTNode.CHARACTER_LITERAL || astNodeType == ASTNode.NUMBER_LITERAL) {
								matched = (i == 1 && j == 1) || (i == lenM && j == lenN) ||
										(i > 1 && j > 1 && codeM[i-1].equals(codeN[j-1])) || 
										(i < lenM && j < lenN && codeM[i+1].equals(codeN[j+1]));
							}
						}
						if(matched)
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
				int pM = prevM, pN = prevN;
				for(int i = 0; i < lcsM.size(); i++)
				{
					//System.out.println(lcsM.get(i) + " - " + lcsN.get(i));
					int cM = lcsM.get(i) + prevM, cN = lcsN.get(i) + prevN;
					leafM = leavesM.get(cM);
					leafN = leavesN.get(cN);
					leafM.setProperty(propertyRevisionMap, leafN);
					leafM.setProperty(propertySimilarity, 1.0);
					leafN.setProperty(propertyRevisionMap, leafM);
					leafN.setProperty(propertySimilarity, 1.0);
					/*System.out.println(leafM.getProperty(MapVisitor.propertyLocationID) + "@" + 
							leafM.getProperty(MapVisitor.propertyStartLine));
					System.out.println(leafM);
					System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
					System.out.println(leafN.getProperty(MapVisitor.propertyLocationID) + "@" + 
							leafN.getProperty(MapVisitor.propertyStartLine));
					System.out.println(leafN);*/
					if(cM - pM == 1 && cN - pN > 1)
						for(int j = pN+1; j < cN; j++)
						{
							leavesN.get(j).setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_ADDED);
							addedLeaves.add(leavesN.get(j));
							/*System.out.println(leavesN.get(j).getProperty(MapVisitor.propertyLocationID) + "@" + 
									leavesN.get(j).getProperty(MapVisitor.propertyStartLine) + " - Added in mapping iso leaves");
							System.out.println(leavesN.get(j));*/
						}
					if(cN - pN == 1 && cM - pM > 1)
						for(int j = pM+1; j < cM; j++)
						{
							leavesM.get(j).setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_DELETED);
							deletedLeaves.add(leavesM.get(j));
							/*System.out.println(leavesM.get(j).getProperty(MapVisitor.propertyLocationID) + "@" + 
									leavesM.get(j).getProperty(MapVisitor.propertyStartLine) + " - Deleted in mapping iso leaves");
							System.out.println(leavesM.get(j));*/
						}
					pM = cM; pN = cN;
				}
				if(m - pM == 1 && n - pN > 1)
					for(int j = pN+1; j < n; j++)
					{
						leavesN.get(j).setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_ADDED);
						addedLeaves.add(leavesN.get(j));
						/*System.out.println(leavesN.get(j).getProperty(MapVisitor.propertyLocationID) + "@" + 
								leavesN.get(j).getProperty(MapVisitor.propertyStartLine) + " - Added in mapping iso leaves");
						System.out.println(leavesN.get(j));*/
					}
				if(n - pN == 1 && m - pM > 1)
					for(int j = pM+1; j < m; j++)
					{
						leavesM.get(j).setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_DELETED);
						deletedLeaves.add(leavesM.get(j));
						/*System.out.println(leavesM.get(j).getProperty(MapVisitor.propertyLocationID) + "@" + 
								leavesM.get(j).getProperty(MapVisitor.propertyStartLine) + " - Deleted in mapping iso leaves");
						System.out.println(leavesM.get(j));*/
					}
			}
			else if(lenN > 0)
			{
				for(int j = prevN+1; j < n; j++)
				{
					leavesN.get(j).setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_ADDED);
					addedLeaves.add(leavesN.get(j));
					/*System.out.println(leavesN.get(j).getProperty(MapVisitor.propertyLocationID) + "@" + 
							leavesN.get(j).getProperty(MapVisitor.propertyStartLine) + " - Added in mapping iso leaves");
					System.out.println(leavesN.get(j));*/
				}
			}
			else if(lenM > 0)
			{
				for(int j = prevM+1; j < m; j++)
				{
					leavesM.get(j).setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_DELETED);
					deletedLeaves.add(leavesM.get(j));
					/*System.out.println(leavesM.get(j).getProperty(MapVisitor.propertyLocationID) + "@" + 
							leavesM.get(j).getProperty(MapVisitor.propertyStartLine) + " - Deleted in mapping iso leaves");
					System.out.println(leavesM.get(j));*/
				}
			}
		}
		return 0;
	}
	private void printLCS(String[][] p, int i, int j, ArrayList<Integer> lcsM, ArrayList<Integer> lcsN)
	{
		/*if(i == 0 || j == 0)
			return;
		if(p[i][j].equals("LU"))
		{
			printLCS(p, i-1, j-1, lcsM, lcsN);
			lcsM.add(i);
			lcsN.add(j);
		}
		else if(p[i][j].equals("U"))
			printLCS(p, i-1, j, lcsM, lcsN);
		else
			printLCS(p, i, j-1, lcsM, lcsN);*/
		int ii = i, jj = j;
		while(ii != 0 && jj != 0)
		{
			if(p[ii][jj].equals("LU"))
			{
				lcsM.add(0, ii);
				lcsN.add(0, jj);
				ii--; jj--;
			}
			else if(p[ii][jj].equals("U"))
				ii--;
			else
				jj--;
		}
	}
	/**
	 * 
	 */
	private void mapDeletion(ASTNode nodeM)
	{
		ArrayList<ASTNode> childrenM = treeM.get(nodeM);
		if(!childrenM.isEmpty())
		{
			for(int i = 0; i < childrenM.size(); i++)
			{
				ASTNode child = childrenM.get(i);
				mapDeletion(child);

			}
			HashSet<ASTNode> nodesN = new HashSet<ASTNode>();
			for(ASTNode childM : childrenM)
			{
				ASTNode childN = (ASTNode)childM.getProperty(propertyRevisionMap);
				if(childN != null)
					if(getParent(childN) != null)
						nodesN.add(getParent(childN));
			}
			if(nodesN.isEmpty())
			{
				nodeM.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_DELETED);
				/*System.out.println(nodeM.getProperty(MapVisitor.propertyLocationID) + "@" + 
						nodeM.getProperty(MapVisitor.propertyStartLine) + " - Deleted by no nodes");
				System.out.println(nodeM);*/
			}
			/*else if(nodesN.size() == 1)
			{
				ASTNode simNode = (ASTNode)nodesN.toArray()[0];
				nodeM.setProperty(propertyMap, simNode);
				simNode.setProperty(propertyMap, nodeM);
				System.out.println(nodeM.getProperty(MapVisitor.propertyLocationID) + "@" + 
						nodeM.getProperty(MapVisitor.propertyStartLine));
				System.out.println(nodeM);
				System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
				System.out.println(simNode.getProperty(MapVisitor.propertyLocationID) + "@" + 
						simNode.getProperty(MapVisitor.propertyStartLine));
				System.out.println(simNode);
			}*/
			else
			{
				MapFragment fragmentM = (MapFragment)nodeM.getProperty(MapVectorVisitor.propertyFragment);
				double similarity = 0;
				ASTNode simNode = null;
				for(ASTNode nodeN : nodesN)
				{
					ASTNode next = nodeN;
					do {
						MapFragment fragmentN = (MapFragment)next.getProperty(MapVectorVisitor.propertyFragment);
						double sim = fragmentM.changeSimilarity(fragmentN);
						//System.out.println(sim);
						int nodeTypeM = nodeM.getNodeType(), nodeTypeN = next.getNodeType();
						if(nodeTypeN == nodeTypeM && (nodeTypeM != ASTNode.ARRAY_TYPE || next.getParent().getNodeType() == nodeM.getParent().getNodeType()) 
								&& sim >= simThreshold && sim > similarity)
						//if(sim > similarity)
						{
							similarity = sim;
							simNode = next;
							if(sim == 1.0)
								break;
						}
						next = getParent(next);
					} while(next != null && next.getNodeType() != ASTNode.METHOD_DECLARATION);
					if(similarity == 1.0)
						break;
				}
				if(simNode != null)
				{
					Double simM = (Double)nodeM.getProperty(propertySimilarity);
					Double simN = (Double)simNode.getProperty(propertySimilarity);
					if(simM == null)
						simM = 0.0;
					if(simN == null)
						simN = 0.0;
					if(simM < similarity && simN < similarity)
					{
						nodeM.setProperty(propertyRevisionMap, simNode);
						nodeM.setProperty(propertySimilarity, similarity);
						simNode.setProperty(propertyRevisionMap, nodeM);
						simNode.setProperty(propertySimilarity, similarity);
						/*System.out.println(nodeM.getProperty(MapVisitor.propertyLocationID) + "@" + 
								nodeM.getProperty(MapVisitor.propertyStartLine));
						System.out.println(nodeM);
						System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
						System.out.println(simNode.getProperty(MapVisitor.propertyLocationID) + "@" + 
								simNode.getProperty(MapVisitor.propertyStartLine));
						System.out.println(simNode);*/
					}
				}
				else
				{
					if(nodeM.getProperty(propertyRevisionMap) == null)
					{
						nodeM.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_DELETED);
						/*System.out.println(nodeM.getProperty(MapVisitor.propertyLocationID) + "@" + 
								nodeM.getProperty(MapVisitor.propertyStartLine) + " - Deleted by no similar nodes " + similarity);
						System.out.println(nodeM);*/
					}
				}
			}
		}
	}
	/**
	 * 
	 */
	private void mapAddition(ASTNode nodeN)
	{
		ArrayList<ASTNode> childrenN = treeN.get(nodeN);
		//System.out.println(nodeN);
		if(!childrenN.isEmpty())
		{
			for(int i = 0; i < childrenN.size(); i++)
			{
				ASTNode child = childrenN.get(i);
				mapAddition(child);

			}
			HashSet<ASTNode> nodesM = new HashSet<ASTNode>();
			for(ASTNode childN : childrenN)
			{
				ASTNode childM = (ASTNode)childN.getProperty(propertyRevisionMap);
				if(childM != null)
					if(getParent(childM) != null)
						nodesM.add(getParent(childM));
			}
			if(nodesM.isEmpty())
			{
				nodeN.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_ADDED);
				/*System.out.println(nodeN.getProperty(MapVisitor.propertyLocationID) + "@" + 
						nodeN.getProperty(MapVisitor.propertyStartLine) + " - Added");
				System.out.println(nodeN);*/
			}
			/*else if(nodesM.size() == 1)
			{
				ASTNode simNode = (ASTNode)nodesM.toArray()[0];
				nodeN.setProperty(propertyMap, simNode);
				simNode.setProperty(propertyMap, nodeN);
				System.out.println(nodeN.getProperty(MapVisitor.propertyLocationID) + "@" + 
						nodeN.getProperty(MapVisitor.propertyStartLine));
				System.out.println(nodeN);
				System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
				System.out.println(simNode.getProperty(MapVisitor.propertyLocationID) + "@" + 
						simNode.getProperty(MapVisitor.propertyStartLine));
				System.out.println(simNode);
			}*/
			else
			{
				MapFragment fragmentN = (MapFragment)nodeN.getProperty(MapVectorVisitor.propertyFragment);
				double similarity = 0;
				ASTNode simNode = null;
				for(ASTNode nodeM : nodesM)
				{
					ASTNode next = nodeM;
					do {
						MapFragment fragmentM = (MapFragment)next.getProperty(MapVectorVisitor.propertyFragment);
						if (fragmentM != null) {
							double sim = fragmentN.changeSimilarity(fragmentM);
							int nodeTypeM = next.getNodeType(), nodeTypeN = nodeN.getNodeType();
							if(nodeTypeM == nodeTypeN && (nodeTypeM != ASTNode.ARRAY_TYPE || next.getParent().getNodeType() == nodeN.getParent().getNodeType()) && 
									sim >= simThreshold && sim > similarity)
							//if(sim > similarity)
							{
								similarity = sim;
								simNode = next;
								if(sim == 1.0)
									break;
							}
						}
						next = getParent(next);
					} while(next != null && next.getNodeType() != ASTNode.METHOD_DECLARATION);
					if(similarity == 1.0)
						break;
				}
				if(simNode != null)
				{
					Double simM = (Double)simNode.getProperty(propertySimilarity);
					Double simN = (Double)nodeN.getProperty(propertySimilarity);
					if(simM == null)
						simM = 0.0;
					if(simN == null)
						simN = 0.0;
					if(simM < similarity && simN < similarity)
					{
						nodeN.setProperty(propertyRevisionMap, simNode);
						nodeN.setProperty(propertySimilarity, similarity);
						simNode.setProperty(propertyRevisionMap, nodeN);
						simNode.setProperty(propertySimilarity, similarity);
						/*System.out.println(nodeN.getProperty(MapVisitor.propertyLocationID) + "@" + 
								nodeN.getProperty(MapVisitor.propertyStartLine));
						System.out.println(nodeN);
						System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
						System.out.println(simNode.getProperty(MapVisitor.propertyLocationID) + "@" + 
								simNode.getProperty(MapVisitor.propertyStartLine));
						System.out.println(simNode);*/
					}
				}
				else
				{
					if(nodeN.getProperty(propertyRevisionMap) == null)
					{
						nodeN.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_ADDED);
						/*System.out.println(nodeN.getProperty(MapVisitor.propertyLocationID) + "@" + 
								nodeN.getProperty(MapVisitor.propertyStartLine) + " - Added");
						System.out.println(nodeN);*/
					}
				}
			}
		}
	}
	private void mapMoving(ASTNode rootM)
	{
		ArrayList<ASTNode> childrenM = treeM.get(rootM);
		ASTNode rootN = (ASTNode)rootM.getProperty(propertyRevisionMap);
		if(rootN != null)
		{
			ASTNode pM = getParent(rootM), pN = getParent(rootN);
			if (pM != null && pN != null && pN != pM.getProperty(propertyRevisionMap)) {
				int pStatusM = -1, pStatusN = -1;
				if (pM != null)
					pStatusM = (Integer) pM.getProperty(MapVisitor.propertyStatus);
				if (pN != null)
					pStatusN = (Integer) pN.getProperty(MapVisitor.propertyStatus);
				if (pStatusM != MapVisitor.STATUS_DELETED && pStatusN != MapVisitor.STATUS_ADDED) {
					rootM.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_MOVED);
					rootN.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_MOVED);
				}
			}
			ArrayList<ASTNode> childrenN = treeN.get(rootN);
			ArrayList<ASTNode> deletedChildren = new ArrayList<ASTNode>();
			ArrayList<ASTNode> addedChildren = new ArrayList<ASTNode>();
			for(ASTNode child : childrenM)
			{
				if(child.getProperty(propertyRevisionMap) == null)
					deletedChildren.add(child);
			}
			for(ASTNode child : childrenN)
			{
				if(child.getProperty(propertyRevisionMap) == null)
					addedChildren.add(child);
			}
			if (deletedChildren.size() * addedChildren.size() <= MAX_NUM_PAIR_COMPARISONS) {	// TRICK avoid large pair-wise comparison
				HashMap<ASTNode, HashMap<ASTNode, Double[]>> similarityPairs = new HashMap<ASTNode, HashMap<ASTNode,Double[]>>();
				for(ASTNode nodeM : deletedChildren)
				{
					MapFragment fM = (MapFragment)nodeM.getProperty(MapVectorVisitor.propertyFragment);
					HashMap<ASTNode, Double[]> pairs = new HashMap<ASTNode, Double[]>();
					for(ASTNode nodeN : addedChildren)
					{
						Double[] sim = new Double[2];
						MapFragment fN = (MapFragment)nodeN.getProperty(MapVectorVisitor.propertyFragment);
						sim[0] = fM.nodeTypeSimilarity(fN);
						int nodeTypeM = nodeM.getNodeType(), nodeTypeN = nodeN.getNodeType();
						if(nodeTypeM == nodeTypeN && (nodeTypeM != ASTNode.ARRAY_TYPE || nodeM.getParent().getNodeType() == nodeN.getParent().getNodeType()) &&
								sim[0] >= simThreshold)
						{
							sim[1] = fM.leafLabelSimilarity(fN);
							pairs.put(nodeN, sim);
						}
					}
					if(!pairs.isEmpty())
						similarityPairs.put(nodeM, pairs);
				}
				while(!similarityPairs.isEmpty())
				{
					double simType = 0.0, simLabel = 0.0;
					ASTNode node1 = null, node2 = null;
					for(ASTNode nodeM : similarityPairs.keySet())
					{
						HashMap<ASTNode, Double[]> pairs = similarityPairs.get(nodeM);
						for(ASTNode nodeN : pairs.keySet())
						{
							Double[] sim = pairs.get(nodeN);
							if(nodeM.getNodeType() == nodeN.getNodeType())
							{
								if(sim[0] > simType)
								{
									simType = sim[0];
									node1 = nodeM;
									node2 = nodeN;
								}
								else if(sim[0] == simType && sim[1] > simLabel)
								{
									simLabel = sim[1];
									node1 = nodeM;
									node2 = nodeN;
								}
							}
						}
					}
					node1.setProperty(propertyRevisionMap, node2);
					node1.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_MOVED);
					node2.setProperty(propertyRevisionMap, node1);
					node2.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_MOVED);
					if (childrenM.size() == 1 || childrenN.size() == 1) {
						node1.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_PARTLY_CHANGED);
						node2.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_PARTLY_CHANGED);
					}
					for(ASTNode nodeM : new HashSet<ASTNode>(similarityPairs.keySet()))
					{
						HashMap<ASTNode, Double[]> pairs = similarityPairs.get(nodeM);
						pairs.remove(node2);
						if(pairs.isEmpty())
							similarityPairs.remove(nodeM);
					}
					similarityPairs.remove(node1);
				}
				// TODO refine moving
				/*int lenM = childrenM.size(), lenN = childrenN.size();
				double[][] d = new double[lenM+1][lenN+1];
				ASTNode[] childM = new ASTNode[lenM+1];
				ASTNode[] childN = new ASTNode[lenN+1];
				String[][] p = new String[lenM+1][lenN+1];
				d[0][0] = 0;
				for(int i = 1; i <= lenM; i++)
				{
					d[i][0] = 0;
					childM[i] = childrenM.get(i-1);
				}
				for(int i = 1; i <= lenN; i++)
				{
					d[0][i] = 0;
					childN[i] = childrenN.get(i-1);
				}
				for(int i = 1; i <= lenM; i++)
				{
					for(int j = 1; j <= lenN; j++)
					{
						if(childM[i].equals(childN[j].getProperty(propertyRevisionMap)))
						{
							double score = 1.0;
							int status = (Integer) (childM[i].getProperty(MapVisitor.propertyStatus));
							score = 1 - status / (MapVisitor.STATUS_MOVED * 10.0);
							d[i][j] = d[i-1][j-1] + score;
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
				int start = -1, end = -1;
				for(int i = 0; i < lcsM.size(); i++)
				{
					for (int j = end+1; j < lcsM.get(i); j++) {
						
					}
					childM[lcsM.get(i)].setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_PARTLY_CHANGED);
					childN[lcsN.get(i)].setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_PARTLY_CHANGED);
				}*/
			}
		}
		else
		{
			rootM.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_DELETED);
		}
		for(ASTNode childM : childrenM)
			mapMoving(childM);
	}
	private void markAddition(ASTNode rootN)
	{
		if(rootN.getProperty(propertyRevisionMap) == null)
			rootN.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_ADDED);
		ArrayList<ASTNode> children = treeN.get(rootN);
		for(ASTNode child : children)
		{
			markAddition(child);
		}
	}
	/*private void mapNameRelabeling(ASTNode node1, HashMap<ASTNode, ArrayList<ASTNode>> tree1)
	{
		ArrayList<ASTNode> children = tree1.get(node1);
		if(children.isEmpty())
		{
			ASTNode node2 = (ASTNode)node1.getProperty(propertyRevisionMap);
			if(node2 != null)
				if(!node1.toString().equals(node2.toString()))
				{
					node1.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
					node2.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
				}
		}
		else
			for(ASTNode child : children)
				mapNameRelabeling(child, tree1);
	}*/
	/*private void mapNameRelabeling()
	{
		for(ASTNode leafM : fileM.getAllLeaves())
		{
			ASTNode leafN = (ASTNode)leafM.getProperty(propertyRevisionMap);
			if(leafN != null)
				if(!leafM.toString().equals(leafN.toString()))
				{
					leafM.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
					leafN.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
				}
		}
	}*/
	private void mapRelabeling(ASTNode node1, HashMap<ASTNode, ArrayList<ASTNode>> tree1)
	{
		ArrayList<ASTNode> children = tree1.get(node1);
		if(children.isEmpty())
		{
			ASTNode node2 = (ASTNode)node1.getProperty(propertyRevisionMap);
			if(node2 != null)
				if(!node1.toString().equals(node2.toString()))
				{
					node1.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
					node2.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
				}
		}
		else
		{
			int nodeType = node1.getNodeType();
			if((nodeType == ASTNode.INFIX_EXPRESSION || nodeType == ASTNode.PREFIX_EXPRESSION || nodeType == ASTNode.POSTFIX_EXPRESSION) && 
					node1.getProperty(propertyRevisionMap) != null)
			{
				ASTNode node2 = (ASTNode)node1.getProperty(propertyRevisionMap);
				if(node2.getNodeType() == nodeType)
				{
					if(nodeType == ASTNode.INFIX_EXPRESSION && !((InfixExpression)node1).getOperator().equals(((InfixExpression)node2).getOperator()))
					{
						node1.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
						node2.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
					}
					else if(nodeType == ASTNode.PREFIX_EXPRESSION && !((PrefixExpression)node1).getOperator().equals(((PrefixExpression)node2).getOperator()))
					{
						node1.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
						node2.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
					}
					else if(nodeType == ASTNode.POSTFIX_EXPRESSION && !((PostfixExpression)node1).getOperator().equals(((PostfixExpression)node2).getOperator()))
					{
						node1.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
						node2.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_RELABELED);
					}
				}
			}
			for(ASTNode child : children)
				mapRelabeling(child, tree1);
		}
	}

	private boolean checkUnchange(ASTNode node, HashMap<ASTNode, ArrayList<ASTNode>> tree)
	{
		int status = (Integer)node.getProperty(MapVisitor.propertyStatus);
		boolean unchanged = status <= MapVisitor.STATUS_FULLY_CHANGED;
		ArrayList<ASTNode> children = tree.get(node);
		for(ASTNode child : children)
		{
			boolean unChild = checkUnchange(child, tree);
			unchanged = (unchanged && unChild);
		}
		if (unchanged)
			node.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_UNCHANGED);

		return unchanged;
	}
	/**
	 * 
	 * @param node
	 * @param tree
	 * @return [0]: number of nodes, [1]: #relabels, [2]: #dels or adds
	 */
	private void mapUnchange(ASTNode node, HashMap<ASTNode, ArrayList<ASTNode>> tree)
	{
		ASTNode mappedNode = (ASTNode)node.getProperty(propertyRevisionMap);
		int status = (Integer)node.getProperty(MapVisitor.propertyStatus);
		if(mappedNode != null)
		{
			int mappedStatus = (Integer)mappedNode.getProperty(MapVisitor.propertyStatus);
			if(status == MapVisitor.STATUS_UNCHANGED && mappedStatus != MapVisitor.STATUS_UNCHANGED) {
				node.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_PARTLY_CHANGED);
			}
		}
		ArrayList<ASTNode> children = tree.get(node);
		for(ASTNode child : children)
		{
			mapUnchange(child, tree);
		}
		status = (Integer)node.getProperty(MapVisitor.propertyStatus);
		ASTNode p = getParent(node);
		if (status != MapVisitor.STATUS_UNCHANGED && p != null) {
			int pStatus = (Integer) p.getProperty(MapVisitor.propertyStatus);
			if (pStatus == MapVisitor.STATUS_UNCHANGED) {
				p.setProperty(MapVisitor.propertyStatus, MapVisitor.STATUS_PARTLY_CHANGED);
			}
		}
	}
	/**
	 * 
	 */
	private ASTNode getParent(ASTNode node)
	{
		if(/*node.getParent() instanceof TypeDeclaration ||
				node.getNodeType() == ASTNode.TYPE_DECLARATION || node.getNodeType() == ASTNode.METHOD_DECLARATION ||*/ 
				node == this.astM || node == this.astN)
			return null;
		ASTNode pNode = node.getParent();
		while(pNode != null && 
				(pNode.getNodeType() == ASTNode.BLOCK || pNode.getNodeType() == ASTNode.EXPRESSION_STATEMENT /*|| pNode.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION*/
				|| (pNode.getNodeType() == ASTNode.ARRAY_TYPE && pNode.getParent().getNodeType() == ASTNode.ARRAY_CREATION)))
			pNode = pNode.getParent();
		
		return pNode;
	}
	public String printTree(HashMap<ASTNode, ArrayList<ASTNode>> tree, ASTNode root)
	{
		StringBuffer buf = new StringBuffer();
		int status = (Integer)root.getProperty(MapVisitor.propertyStatus);
		if(status > MapVisitor.STATUS_FULLY_CHANGED)
		{
			ASTNode simNode = (ASTNode)root.getProperty(propertyRevisionMap);
			buf.append(root.getProperty(MapVisitor.propertyLocationID) + "@" + 
					root.getProperty(MapVisitor.propertyStartLine) + ": " + getStatus(status) + "\r\n");
			buf.append(root + "\r\n");
			if(simNode != null)
			{
				buf.append("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\r\n");
				buf.append(simNode.getProperty(MapVisitor.propertyLocationID) + "@" + 
						simNode.getProperty(MapVisitor.propertyStartLine) + "\r\n");
				buf.append(simNode + "\r\n");
			}
		}
		ArrayList<ASTNode> children = tree.get(root);
		for(ASTNode child : children)
		{
			buf.append(printTree(tree, child));
		}
		
		return buf.toString();
	}
	public static String getStatus(int status)
	{
		switch(status) {
		case MapVisitor.STATUS_ADDED: return "ADDED";
		case MapVisitor.STATUS_DELETED: return "DELETED";
		case MapVisitor.STATUS_FULLY_CHANGED: return "FULLY_CHANGED";
		case MapVisitor.STATUS_MOVED: return "MOVED";
		case MapVisitor.STATUS_PARTLY_CHANGED: return "PARTLY_CHANGED";
		case MapVisitor.STATUS_RELABELED: return "RELABELED";
		case MapVisitor.STATUS_UNCHANGED: return "UNCHANGED";
		}
		return "NOTHING_ELSE";
	}
}
