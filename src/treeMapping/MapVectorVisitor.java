package treeMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.eclipse.jdt.core.dom.*;

/**
 * @author Nguyen Anh Hoan
 *
 */
public class MapVectorVisitor extends ASTVisitor {
	public static final String propertyFragment = "vector";
	public static byte[] indexer = new byte[127];	//category of this node type (Class, Method, Block, Statement, ..)
	//MapSourceFile sourceFile;
    /*
	 * Stack of children's n-gram vectors
	 */
	private Stack<ArrayList<HashMap<Integer, Integer>>> stackChildrenVectors = new Stack<ArrayList<HashMap<Integer, Integer>>>();
	private Stack<ArrayList<HashMap<Integer, Integer>>> stackChangeChildrenVectors = new Stack<ArrayList<HashMap<Integer, Integer>>>();
	/*
	 * Stack of VERTICAL n-grams starting from the ROOT of the subtrees 
	 */
	private Stack<ArrayList<HashMap<Integer, Integer>>> stackChildrenRootVGrams = new Stack<ArrayList<HashMap<Integer, Integer>>>();
	private Stack<ArrayList<HashMap<Integer, Integer>>> stackChangeChildrenRootVGrams = new Stack<ArrayList<HashMap<Integer, Integer>>>();
    
	private Stack<HashMap<String, Integer>> stackLeafVectors = new Stack<HashMap<String,Integer>>();
	private Stack<HashMap<String, Integer>> stackChangeLeafVectors = new Stack<HashMap<String,Integer>>();
	
	private Stack<Integer> stackAllStatus = new Stack<Integer>();

    public MapVectorVisitor() {

    }
    static {
    	int index = 0;
		for(byte i = 0; i < indexer.length; i++)
		{
			if (i==0 || i==6  || i==17 || i==20 || i==26 || i==28 ||  i==30 || i==35 || 
					//i==46 || i==47 || i==11 || i==19 || i==22 || 
					i==52 || i==53 || i==54 || i==12 || i==63 || i==64 || (i>=71 && i<=83) ||
					i == ASTNode.EXPRESSION_STATEMENT || i == ASTNode.BLOCK //|| i == ASTNode.PARENTHESIZED_EXPRESSION
					//|| i == ASTNode.QUALIFIED_NAME
					//|| i == ASTNode.SIMPLE_NAME
				)
			{
				indexer[i] = MapFragment.NotConsideredFrags;
			}
			else
			{
				int gram = i << 24;
				MapFragment.gram2Index.put(gram, index);
				MapFragment.index2Gram.put(index++, gram);
				
				switch (i) {
				//case ASTNode.COMPILATION_UNIT: indexer[i] = Fragment.ClassFragment; break;
				//case ASTNode.TYPE_DECLARATION_STATEMENT: indexer[i] = Fragment.ClassFragment; break;
				case ASTNode.TYPE_DECLARATION: indexer[i] = MapFragment.ClassFragment; break;
				case ASTNode.METHOD_DECLARATION: indexer[i] = MapFragment.MethodFragment; break;
				//case ASTNode.BLOCK: indexer[i] = Fragment.BlockFragment; break;
				case ASTNode.DO_STATEMENT: indexer[i] = MapFragment.LoopStatementFragment; break;
				case ASTNode.FOR_STATEMENT: indexer[i] = MapFragment.LoopStatementFragment; break;
				case ASTNode.ENHANCED_FOR_STATEMENT: indexer[i] = MapFragment.LoopStatementFragment; break;
				case ASTNode.WHILE_STATEMENT: indexer[i] = MapFragment.LoopStatementFragment; break;
				case ASTNode.IF_STATEMENT: indexer[i] = MapFragment.IfStatementFragment; break;
				//case ASTNode.SWITCH_CASE: indexer[i] = Fragment.IfStatementFragment; break;
				case ASTNode.SWITCH_STATEMENT: indexer[i] = MapFragment.SwitchStatementFragment; break;
				
				case ASTNode.CONDITIONAL_EXPRESSION: indexer[i] = MapFragment.Expression; break;
				case ASTNode.INFIX_EXPRESSION: indexer[i] = MapFragment.Expression; break;
				case ASTNode.POSTFIX_EXPRESSION: indexer[i] = MapFragment.Expression; break;
				case ASTNode.PREFIX_EXPRESSION: indexer[i] = MapFragment.Expression; break;
				case ASTNode.PARENTHESIZED_EXPRESSION: indexer[i] = MapFragment.Expression; break;
				case ASTNode.INSTANCEOF_EXPRESSION: indexer[i] = MapFragment.Expression; break;
				
				case ASTNode.METHOD_INVOCATION: indexer[i] = MapFragment.MethodState; break;
				case ASTNode.SUPER_METHOD_INVOCATION: indexer[i] = MapFragment.MethodState; break;
				
				//case ASTNode.VARIABLE_DECLARATION_EXPRESSION: indexer[i] = Fragment.DeclarationExp; break;
				//case ASTNode.VARIABLE_DECLARATION_FRAGMENT: indexer[i] = Fragment.VarState; break;
				case ASTNode.VARIABLE_DECLARATION_STATEMENT: indexer[i] = MapFragment.DeclarationState; break;
				case ASTNode.FIELD_DECLARATION: indexer[i] = MapFragment.DeclarationState; break;
				
				case ASTNode.SIMPLE_NAME: indexer[i] = MapFragment.SimpleName; break;
				case ASTNode.BOOLEAN_LITERAL: indexer[i] = MapFragment.Literal; break;
				case ASTNode.CHARACTER_LITERAL: indexer[i] = MapFragment.Literal; break;
				case ASTNode.STRING_LITERAL: indexer[i] = MapFragment.Literal; break;
				case ASTNode.NUMBER_LITERAL: indexer[i] = MapFragment.Literal; break;
				
				case ASTNode.ARRAY_ACCESS: indexer[i] = MapFragment.ArrayState; break;
				case ASTNode.ARRAY_CREATION: indexer[i] = MapFragment.ArrayState; break;
				case ASTNode.ARRAY_INITIALIZER: indexer[i] = MapFragment.ArrayState; break;
				case ASTNode.ARRAY_TYPE: indexer[i] = MapFragment.ArrayState; break;
				
				case ASTNode.ASSERT_STATEMENT: indexer[i] = MapFragment.AssertState; break;
				case ASTNode.ASSIGNMENT: indexer[i] = MapFragment.AssignState; break;
				
				case ASTNode.MEMBER_REF: indexer[i] = MapFragment.OtherFragments; break;
				case ASTNode.METHOD_REF: indexer[i] = MapFragment.OtherFragments; break;
				case ASTNode.METHOD_REF_PARAMETER: indexer[i] = MapFragment.OtherFragments; break;
				case ASTNode.PRIMITIVE_TYPE: indexer[i] = MapFragment.OtherFragments; break;
				case ASTNode.QUALIFIED_NAME: indexer[i] = MapFragment.OtherFragments; break;
				case ASTNode.TAG_ELEMENT: indexer[i] = MapFragment.OtherFragments; break;
				case ASTNode.TEXT_ELEMENT: indexer[i] = MapFragment.OtherFragments; break;
				case ASTNode.TYPE_PARAMETER: indexer[i] = MapFragment.OtherFragments; break;
				default: indexer[i] = MapFragment.OtherStatementFragment; break;
				}
			}
		}
    }
    public void preVisit(ASTNode node) {
    	if(node.getNodeType() == ASTNode.EXPRESSION_STATEMENT || node.getNodeType() == ASTNode.BLOCK || node.getNodeType() == ASTNode.JAVADOC/* || node.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION*/
				|| (node.getNodeType() == ASTNode.ARRAY_TYPE && node.getParent().getNodeType() == ASTNode.ARRAY_CREATION))
    		return;
    		
    	stackChildrenVectors.push(new ArrayList<HashMap<Integer,Integer>>());
    	stackChangeChildrenVectors.push(new ArrayList<HashMap<Integer,Integer>>());
    	stackChildrenRootVGrams.push(new ArrayList<HashMap<Integer,Integer>>());
    	stackChangeChildrenRootVGrams.push(new ArrayList<HashMap<Integer,Integer>>());
    	
		stackLeafVectors.push(new HashMap<String, Integer>());
		stackChangeLeafVectors.push(new HashMap<String, Integer>());
    	
    	stackAllStatus.push(-1);
    }
    public void postVisit(ASTNode node) {
    	if(node.getNodeType() == ASTNode.EXPRESSION_STATEMENT || node.getNodeType() == ASTNode.BLOCK || node.getNodeType() == ASTNode.JAVADOC/* || node.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION*/
				|| (node.getNodeType() == ASTNode.ARRAY_TYPE && node.getParent().getNodeType() == ASTNode.ARRAY_CREATION))
    		return;
    	
    	int status = stackAllStatus.pop();
    	if(status == -1)
    		status = (Integer)node.getProperty(MapVisitor.propertyStatus);
    	else
    		node.setProperty(MapVisitor.propertyStatus, status);
    	//if(node.getNodeType() != ASTNode.METHOD_DECLARATION)
    	if (!stackAllStatus.isEmpty())
    	{
    		int pStatus = stackAllStatus.pop();
    		if(pStatus == -1)
    			pStatus = status;
    		else if(pStatus != status)
    			pStatus = MapVisitor.STATUS_PARTLY_CHANGED;
    		stackAllStatus.push(pStatus);
    	}
    	buildFragment(node, status);
    }
    private void buildFragment(ASTNode node, int status) {
    	int nodeType = node.getNodeType();

    	ArrayList<HashMap<Integer, Integer>> childrenVectors = stackChildrenVectors.pop();
    	ArrayList<HashMap<Integer, Integer>> changeChildrenVectors = stackChangeChildrenVectors.pop();
    	ArrayList<HashMap<Integer, Integer>> childrenRootVGrams = stackChildrenRootVGrams.pop();
    	ArrayList<HashMap<Integer, Integer>> changeChildrenRootVGrams = stackChangeChildrenRootVGrams.pop();
    	
    	HashMap<Integer, Integer> myRootVGrams = new HashMap<Integer, Integer>();
    	HashMap<Integer, Integer> myChangeRootVGrams = new HashMap<Integer, Integer>();
    	HashMap<Integer, Integer> vector = new HashMap<Integer, Integer>();
    	HashMap<Integer, Integer> changeVector = new HashMap<Integer, Integer>();
    	/*
    	 * Adding vectors of all children
    	 */
    	if (!childrenVectors.isEmpty()) {
    		vector.putAll(new HashMap<Integer, Integer>(childrenVectors.get(0)));
    		for(int i = 1; i < childrenVectors.size(); i++) {
    			HashMap<Integer, Integer> childVector = childrenVectors.get(i);
    			for (int index : childVector.keySet()) {
    				if (vector.containsKey(index))
    					vector.put(index, (vector.get(index) + childVector.get(index)));
    				else 
    					vector.put(index, childVector.get(index));
    			}
    		}
    	}
    	/*
    	 * This node is also a single node type in the vector
    	 */
		if ((indexer[nodeType] != MapFragment.NotConsideredFrags)) {
			int gram = nodeType << 24;
			int index =	MapFragment.gram2Index.get(gram);
			if (vector.containsKey(index))
				vector.put(index, vector.get(index) + 1);
			else 
				vector.put(index, 1);
		}
		/*
		 * This node is also a 1-gram in the vector
		 */
		if(indexer[nodeType] <= 11) {
			int gram = -indexer[nodeType];
			int tmpIndex;
			if (MapFragment.gram2Index.containsKey(gram))
				tmpIndex = MapFragment.gram2Index.get(gram);
			else {
				tmpIndex = MapFragment.gram2Index.size();
				MapFragment.gram2Index.put(gram, tmpIndex);
				MapFragment.index2Gram.put(tmpIndex, gram);
			}
			if (myRootVGrams.containsKey(tmpIndex))
				myRootVGrams.put(tmpIndex, myRootVGrams.get(tmpIndex) + 1);
			else 
				myRootVGrams.put(tmpIndex, 1);
		}
		/*
		 * Building all n-grams starting from this node (will be used by its parent)
		 */
		if (!childrenRootVGrams.isEmpty()) {
			if(indexer[nodeType] <= 11) {
				for (HashMap<Integer, Integer> childGram : childrenRootVGrams) {
					for (int index : childGram.keySet()) {
						int gram = MapFragment.index2Gram.get(index);
						gram = -((indexer[nodeType] << (4 * getSizeOfGram(gram))) - gram);
						int tmpIndex;
						if (MapFragment.gram2Index.containsKey(gram))
							tmpIndex = MapFragment.gram2Index.get(gram);
	    				else {
	    					tmpIndex = MapFragment.gram2Index.size();
	    					MapFragment.gram2Index.put(gram, tmpIndex);
	    					MapFragment.index2Gram.put(tmpIndex, gram);
	    				}
	    				if (vector.containsKey(tmpIndex))
	    					vector.put(tmpIndex, vector.get(tmpIndex) + childGram.get(index));
	    				else 
	    					vector.put(tmpIndex, childGram.get(index));
	    				if (getSizeOfGram(gram) < MapFragment.maxSizeOfGram) {
	    					if (myRootVGrams.containsKey(tmpIndex))
	    						myRootVGrams.put(tmpIndex, myRootVGrams.get(tmpIndex) + childGram.get(index));
		    				else 
		    					myRootVGrams.put(tmpIndex, childGram.get(index));
	    				}
	    			}
				}
			}
			else {
				for (HashMap<Integer, Integer> childGram : childrenRootVGrams) {
					for (int index : childGram.keySet()) {
    					if (myRootVGrams.containsKey(index))
    						myRootVGrams.put(index, myRootVGrams.get(index) + childGram.get(index));
	    				else 
	    					myRootVGrams.put(index, childGram.get(index));
	    			}
				}
			}
		}
		
		if(status != MapVisitor.STATUS_ADDED && status != MapVisitor.STATUS_DELETED)
		{
			if (!changeChildrenVectors.isEmpty()) {
	    		changeVector.putAll(new HashMap<Integer, Integer>(changeChildrenVectors.get(0)));
	    		for(int i = 1; i < changeChildrenVectors.size(); i++) {
	    			HashMap<Integer, Integer> childVector = changeChildrenVectors.get(i);
	    			for (int index : childVector.keySet()) {
	    				if (changeVector.containsKey(index))
	    					changeVector.put(index, (changeVector.get(index) + childVector.get(index)));
	    				else 
	    					changeVector.put(index, childVector.get(index));
	    			}
	    		}
	    	}
	    	/*
	    	 * This node is also a single node type in the vector
	    	 */
			if ((indexer[nodeType] != MapFragment.NotConsideredFrags)) {
				int gram = nodeType << 24;
				int index =	MapFragment.gram2Index.get(gram);
				if (changeVector.containsKey(index))
					changeVector.put(index, changeVector.get(index) + 1);
				else 
					changeVector.put(index, 1);
			}
			/*
			 * This node is also a 1-gram in the vector
			 */
			if(indexer[nodeType] <= 11) {
				int gram = -indexer[nodeType];
				int tmpIndex;
				if (MapFragment.gram2Index.containsKey(gram))
					tmpIndex = MapFragment.gram2Index.get(gram);
				else {
					tmpIndex = MapFragment.gram2Index.size();
					MapFragment.gram2Index.put(gram, tmpIndex);
					MapFragment.index2Gram.put(tmpIndex, gram);
				}
				if (myChangeRootVGrams.containsKey(tmpIndex))
					myChangeRootVGrams.put(tmpIndex, myChangeRootVGrams.get(tmpIndex) + 1);
				else 
					myChangeRootVGrams.put(tmpIndex, 1);
			}
			/*
			 * Building all n-grams starting from this node (will be used by its parent)
			 */
			if (!changeChildrenRootVGrams.isEmpty()) {
				if(indexer[nodeType] <= 11) {
					for (HashMap<Integer, Integer> childGram : changeChildrenRootVGrams) {
						for (int index : childGram.keySet()) {
							int gram = MapFragment.index2Gram.get(index);
							gram = -((indexer[nodeType] << (4 * getSizeOfGram(gram))) - gram);
							int tmpIndex;
							if (MapFragment.gram2Index.containsKey(gram))
								tmpIndex = MapFragment.gram2Index.get(gram);
		    				else {
		    					tmpIndex = MapFragment.gram2Index.size();
		    					MapFragment.gram2Index.put(gram, tmpIndex);
		    					MapFragment.index2Gram.put(tmpIndex, gram);
		    				}
		    				if (changeVector.containsKey(tmpIndex))
		    					changeVector.put(tmpIndex, changeVector.get(tmpIndex) + childGram.get(index));
		    				else 
		    					changeVector.put(tmpIndex, childGram.get(index));
		    				if (getSizeOfGram(gram) < MapFragment.maxSizeOfGram) {
		    					if (myChangeRootVGrams.containsKey(tmpIndex))
		    						myChangeRootVGrams.put(tmpIndex, myChangeRootVGrams.get(tmpIndex) + childGram.get(index));
			    				else 
			    					myChangeRootVGrams.put(tmpIndex, childGram.get(index));
		    				}
		    			}
					}
				}
				else {
					for (HashMap<Integer, Integer> childGram : changeChildrenRootVGrams) {
						for (int index : childGram.keySet()) {
	    					if (myChangeRootVGrams.containsKey(index))
	    						myChangeRootVGrams.put(index, myChangeRootVGrams.get(index) + childGram.get(index));
		    				else 
		    					myChangeRootVGrams.put(index, childGram.get(index));
		    			}
					}
				}
			}
		}
		/*
		 * Build the corresponding fragment
		 */
		MapFragment fragment = new MapFragment();
		//fragment.setSourceFile(this.sourceFile);
    	fragment.setStartChar(node.getStartPosition());
    	fragment.setLength(node.getLength());
    	fragment.setGramVector(vector);
    	fragment.setNodeTypeVectorLength();
    	fragment.setType((byte)((indexer[nodeType] < 8) ? indexer[nodeType] : 0));
    	
    	HashMap<String, Integer> leafVector = stackLeafVectors.pop();
    	HashMap<String, Integer> changeLeafVector = stackChangeLeafVectors.pop();
		if(nodeType == ASTNode.SIMPLE_NAME)
    	{
    		leafVector.put(node.toString(), 1);
    		if(status != MapVisitor.STATUS_ADDED && status != MapVisitor.STATUS_DELETED)
    			changeLeafVector.put(node.toString(), 1);
    	}
		//if(node.getParent() != null)
		//if(node.getNodeType() != ASTNode.METHOD_DECLARATION)
		if (!stackLeafVectors.isEmpty())
		{
			HashMap<String, Integer> pLeafVector = stackLeafVectors.pop();
			for(String key : leafVector.keySet())
			{
				int c = 0;
				if(pLeafVector.containsKey(key))
					c = pLeafVector.get(key);
				c += leafVector.get(key);
				pLeafVector.put(key, c);
			}
			stackLeafVectors.push(pLeafVector);
			if(status != MapVisitor.STATUS_ADDED && status != MapVisitor.STATUS_DELETED)
			{
				pLeafVector = stackChangeLeafVectors.pop();
				for(String key : changeLeafVector.keySet())
				{
					int c = 0;
					if(pLeafVector.containsKey(key))
						c = pLeafVector.get(key);
					c += changeLeafVector.get(key);
					pLeafVector.put(key, c);
				}
				stackChangeLeafVectors.push(pLeafVector);
			}
		}
		fragment.setLeafVector(leafVector);
		fragment.setLeafLabelVectorLength();
    	fragment.setVectorLength();
    	
    	fragment.setChangeGramVector(changeVector);
    	fragment.setChangeLeafVector(changeLeafVector);
    	fragment.setChangeVectorLength();
    	
		node.setProperty(propertyFragment, fragment);
		
		/*
		 * Pushing to the stacks respectively
		 */
    	if (!stackChildrenVectors.isEmpty()) {	//if not root
    		if (!vector.isEmpty()) {
	    		ArrayList<HashMap<Integer, Integer>> parentVectors = stackChildrenVectors.pop();	//get siblings
	    		parentVectors.add(vector); 	//join them (append this node type)
	    		stackChildrenVectors.push(parentVectors);	//back home
    		}
    		if (!myRootVGrams.isEmpty()) {
    			ArrayList<HashMap<Integer, Integer>> parentGrams = stackChildrenRootVGrams.pop();	//get siblings
        		parentGrams.add(myRootVGrams); 	//join them (append this node type)
        		stackChildrenRootVGrams.push(parentGrams);	//back home
    		}
    		if (!changeVector.isEmpty()) {
	    		ArrayList<HashMap<Integer, Integer>> parentVectors = stackChangeChildrenVectors.pop();	//get siblings
	    		parentVectors.add(changeVector); 	//join them (append this node type)
	    		stackChangeChildrenVectors.push(parentVectors);	//back home
    		}
    		if (!myChangeRootVGrams.isEmpty()) {
    			ArrayList<HashMap<Integer, Integer>> parentGrams = stackChangeChildrenRootVGrams.pop();	//get siblings
        		parentGrams.add(myChangeRootVGrams); 	//join them (append this node type)
        		stackChangeChildrenRootVGrams.push(parentGrams);	//back home
    		}
    	}
    }
    private byte getSizeOfGram(int gram) {
    	byte i = 0;
    	gram = Math.abs(gram);
    	while (gram != 0) {
    		i++;
    		gram = gram / 16;
    	}
    	return i;
    }
 }
