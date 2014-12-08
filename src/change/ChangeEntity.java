package change;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Javadoc;
import treeMapping.MapVisitor;
import treeMapping.TreeMapper;
import utils.ASTFlattener;
import utils.LocalVariableNameMarker;

public abstract class ChangeEntity {
	public enum Type {Unchanged, Deleted, Added, Modified, Modified_Modifiers, Modified_Name, Modified_Body}

	private static final int MAX_FRAGMENT_SIZE = 1000;

	protected static int thresholdDistance = 20;
	
	private Type cType = Type.Unchanged;
	protected HashMap<Integer, Integer> vector;
	protected int vectorLength = 0;
	protected HashMap<ASTNode, ArrayList<ASTNode>> tree;
	
	protected Type getCType() {
		return cType;
	}

	protected void setCType(Type type) {
		this.cType = type;
	}
	
	protected void computeVectorLength()
	{
		this.vectorLength = 0;
		for (int key : vector.keySet())
			this.vectorLength += vector.get(key);
	}

	protected Map<Integer, Integer> getVector() {
		return this.vector;
	}

	protected int getVectorLength() {
		return this.vectorLength;
	}
	
	protected double computeVectorSimilarity(ChangeEntity other)
	{
		HashMap<Integer, Integer> v1 = new HashMap<Integer, Integer>(this.vector);
		HashMap<Integer, Integer> v2 = new HashMap<Integer, Integer>(other.getVector());
		HashSet<Integer> keys = new HashSet<Integer>(v1.keySet());
		keys.retainAll(v2.keySet());
		
		int commonSize = 0;
		for(int key : keys)
		{
			commonSize += Math.min(v1.get(key), v2.get(key));
		}
		return commonSize * 2.0 / (this.vectorLength + other.getVectorLength());
	}

	abstract public String getName();
	
	abstract public CFile getCFile();

	public ChangeEntity getMappedEntity() {
		if (this instanceof CClass)
			return ((CClass) this).getMappedClass();
		if (this instanceof CField)
			return ((CField) this).getMappedField();
		if (this instanceof CMethod)
			return ((CMethod) this).getMappedMethod();
		return null;
	}

	abstract public String getQualName();
	
	abstract public CClass getCClass();
	
	public ArrayList<TreeChange> getTreeChanges() {
		if (this instanceof CInitializer)
			return ((CInitializer)this).getTreeChanges();
		if (this instanceof CMethod)
			return ((CMethod)this).getTreeChanges();
		return null;
	}
	
	protected ArrayList<TreeChange> getTreeChanges(HashMap<ASTNode, ArrayList<ASTNode>> tree1, ASTNode root1,
												HashMap<ASTNode, ArrayList<ASTNode>> tree2, ASTNode root2) {
		root1.accept(new LocalVariableNameMarker());
		root1.accept(new ASTFlattener());
		root2.accept(new LocalVariableNameMarker());
		root2.accept(new ASTFlattener());
		
		ArrayList<TreeChange> changes = new ArrayList<TreeChange>();
		changes.addAll(getAdditions(tree2, root2));
		changes.addAll(getTreeModificationsAndDeletions(tree1, root1, tree2));
		return changes;
	}
	
	private ArrayList<TreeChange> getAdditions(HashMap<ASTNode, ArrayList<ASTNode>> tree, ASTNode root) {
		ArrayList<TreeChange> changes = new ArrayList<TreeChange>();
		int status = (Integer)root.getProperty(MapVisitor.propertyStatus);
		if(status <= MapVisitor.STATUS_UNCHANGED)
			return changes;
		int nodeType = root.getNodeType();
		if (nodeType != ASTNode.SIMPLE_TYPE && 
				(nodeType != ASTNode.ARRAY_TYPE || root.getProperty(ASTFlattener.PROPERTY_HEIGHT) != null)) {
			ArrayList<String> tokens = (ArrayList<String>) root.getProperty(ASTFlattener.PROPERTY_SRC);
			if (tokens.size() <= MAX_FRAGMENT_SIZE) {
				byte height = (Byte) root.getProperty(ASTFlattener.PROPERTY_HEIGHT);
				TreeChange change = new TreeChange((byte) root.getNodeType(), height);
				change.tree2 = new CTree((byte) root.getNodeType(), height, tokens, 
						root.getStartPosition());
				change.abstractout();
				//change.doIndexing();
				RevisionAnalyzer ra = this.getCFile().getcRevisionAnalyzer();
				if (root.getProperty(TreeMapper.propertyRevisionMap) == null) {
					changes.add(change);
				}
			}
		}
		ArrayList<ASTNode> children = tree.get(root);
		for(ASTNode child : children) {
			changes.addAll(getAdditions(tree, child));
		}
		return changes;
	}

	private ArrayList<TreeChange> getTreeModificationsAndDeletions(HashMap<ASTNode, ArrayList<ASTNode>> tree1, ASTNode root, HashMap<ASTNode, ArrayList<ASTNode>> tree2) {
		ArrayList<TreeChange> changes = new ArrayList<TreeChange>();
		int status = (Integer)root.getProperty(MapVisitor.propertyStatus);
		if(status <= MapVisitor.STATUS_UNCHANGED)
			return changes;
		int nodeType = root.getNodeType();
		if (nodeType != ASTNode.SIMPLE_TYPE && (nodeType != ASTNode.ARRAY_TYPE || root.getProperty(ASTFlattener.PROPERTY_HEIGHT) != null)) {
			ArrayList<String> tokens1 = (ArrayList<String>) root.getProperty(ASTFlattener.PROPERTY_SRC);
			byte height = (Byte) root.getProperty(ASTFlattener.PROPERTY_HEIGHT);
			CTree cTree1 = new CTree((byte) root.getNodeType(), height, tokens1, root.getStartPosition());
			if (tokens1.size() <= MAX_FRAGMENT_SIZE) {
				ASTNode simNode = (ASTNode)root.getProperty(TreeMapper.propertyRevisionMap);
				if (simNode != null && simNode.getProperty(TreeMapper.propertyRevisionMap) == root) {
					TreeChange change = new TreeChange((byte) root.getNodeType(), height);
					change.tree1 = cTree1;
					ArrayList<String> tokens2 = (ArrayList<String>) simNode.getProperty(ASTFlattener.PROPERTY_SRC);
					change.tree2 = new CTree((byte) simNode.getNodeType(), (Byte) simNode.getProperty(ASTFlattener.PROPERTY_HEIGHT), tokens2
						, root.getStartPosition());
					if (change.isChanged()) {
						if (tokens2.size() <= MAX_FRAGMENT_SIZE) {
							change.abstractout();
							if (hasMultipleChangedChildren(tree1, root) || hasMultipleChangedChildren(tree2, simNode))
								changes.add(change);
						}
					}
					else
						return changes;
				}
				else {
					TreeChange change = new TreeChange((byte) root.getNodeType(), height);
					change.tree1 = cTree1;
					change.abstractout();
					changes.add(change);
				}
			}
		}
		ArrayList<ASTNode> children = tree1.get(root);
		for(ASTNode child : children) {
			ArrayList<TreeChange> childChanges = getTreeModificationsAndDeletions(tree1, child, tree2);
			changes.addAll(childChanges);
		}
		return changes;
	}
	
//	private boolean hasMultipleChangedChildren(HashMap<ASTNode, ArrayList<ASTNode>> tree, ASTNode root) {
//		ArrayList<ASTNode> children = tree.get(root);
//		int numOfChangedChildren = 0;
//		for (ASTNode child : children) {
//			int status = (Integer) child.getProperty(MapVisitor.propertyStatus);
//			if(status > MapVisitor.STATUS_UNCHANGED) {
//				numOfChangedChildren++;
//				if (numOfChangedChildren > 1)
//					return true;
//			}
//		}
//		return false;
//	}
	
	private boolean hasMultipleChangedChildren(HashMap<ASTNode, ArrayList<ASTNode>> tree, ASTNode root) {
		int status = (Integer) root.getProperty(MapVisitor.propertyStatus);
		if (status > MapVisitor.STATUS_FULLY_CHANGED)
			return true;

		ArrayList<ASTNode> children = tree.get(root);
		if (children.isEmpty())
			return true;
		int numOfChangedChildren = 0;
		for (ASTNode child : children) {
			status = (Integer) child.getProperty(MapVisitor.propertyStatus);
			if(status > MapVisitor.STATUS_UNCHANGED) {
				numOfChangedChildren++;
				if (numOfChangedChildren > 1)
					return true;
			}
		}
		return false;
	}

	public String printTree() {
		if (this instanceof CField) {
			CField cf = (CField) this;
			if (cf.getMappedField().getInitializer() == null) {
				if (cf.getInitializer() == null)
					return "";
				return "The whole initializer";
			}
			return cf.getInitializer() == null ? null : printTree(tree, cf.getInitializer());
		}
		if (this instanceof CInitializer)
			return printTree(tree, ((CInitializer)this).getInitializer());
		if (this instanceof CMethod)
			return printTree(tree, ((CMethod)this).getDeclaration());
		return null;
	}

	private String printTree(HashMap<ASTNode, ArrayList<ASTNode>> tree, ASTNode root)
	{
		if (root instanceof Javadoc)
			return "";
		StringBuffer buf = new StringBuffer();
		int status = (Integer)root.getProperty(MapVisitor.propertyStatus);
		if(status > MapVisitor.STATUS_FULLY_CHANGED)
		{
			ASTNode simNode = (ASTNode)root.getProperty(TreeMapper.propertyRevisionMap);
			buf.append(root.getProperty(MapVisitor.propertyLocationID) + "@" + 
					root.getProperty(MapVisitor.propertyStartLine) + ": " + TreeMapper.getStatus(status) + "\r\n");
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
}
