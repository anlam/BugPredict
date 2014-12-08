package treeMapping;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * @author Nguyen Anh Hoan
 *
 */
public class MapFragment implements Serializable {
	protected static final long serialVersionUID = 1L;
	
	public static final int maxSizeOfGram = 2;	// now < 8
	
	/**
	 * Categories of AST node types
	 */
	public static final byte ClassFragment = 1;
	public static final byte MethodFragment = 2;
	public static final byte LoopStatementFragment = 3;
	public static final byte IfStatementFragment = 4;
	public static final byte SwitchStatementFragment = 5;
	//public static final byte BlockFragment = 6;
	public static final byte MethodState = 17;
	public static final byte DeclarationState = 18;
	public static final byte AssertState = 19;
	public static final byte AssignState = 20;
	
	public static final byte ArrayState = 22;
	public static final byte Expression = 23;
	public static final byte DeclarationExp = 24;
	public static final byte SimpleName = 25;
	public static final byte Literal = 26;
	public static final byte StatementsGroupFragment = 27;
	public static final byte MethodsGroupFragment = 28;
	public static final byte OtherStatementFragment = 29;
	public static final byte OtherFragments = 30;
	public static final byte NotConsideredFrags = 31;

	public static String[] typeName = {"", "Class", "Method", "Loop", "If", "Switch", "", "MethodState", "Decl", "Assert", "Assign"};
	/**
	 * Global Maps between an n-gram and its index and vice versa
	 */
	public static HashMap<Integer, Integer> gram2Index = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> index2Gram = new HashMap<Integer, Integer>();

	//public static HashSet<Fragment> all = new HashSet<Fragment>();
	public static HashMap<Integer, MapFragment> all = new HashMap<Integer, MapFragment>();
	public static HashMap<Integer, MapFragment> addeds = new HashMap<Integer, MapFragment>();
	//public static HashMap<Integer, Fragment> deleteds = new HashMap<Integer, Fragment>();
	public static int nextID = 1;
	
	int id;
	//String fileName;
	MapSourceFile sourceFile;
	byte type = OtherFragments; 
	String code = null;
	//byte[] tokens;
	int startChar;	//start character
	int length;	//in characters including CrLf
	int startLine;
	int endLine;
	
	/**
	 * 
	 */
	public MapFragment() {
		this.id = nextID++;
	}
	public MapFragment(int id)
	{
		this.id = id;
	}
	/**
	 * @return the sourceFile
	 */
	public MapSourceFile getSourceFile() {
		return sourceFile;
	}
	/**
	 * @param sourceFile the sourceFile to set
	 */
	public void setSourceFile(MapSourceFile sourceFile) {
		this.sourceFile = sourceFile;
	}
	HashMap<Integer, Integer> gramVector = new HashMap<Integer, Integer>();
	HashMap<String, Integer> leafVector = new HashMap<String, Integer>();
	HashMap<Integer, Integer> changeGramVector = new HashMap<Integer, Integer>();
	HashMap<String, Integer> changeLeafVector = new HashMap<String, Integer>();
	
	double vectorLength, nodeTypeVectorLength, leafLabelVectorLength, changeVectorLength;
	
	/**
	 * @return the vectorLength
	 */
	public double getVectorLength() {
		return vectorLength;
	}
	/**
	 * @param vectorLength the vectorLength to set
	 */
	public void setVectorLength() {
		/*double len = 0;
		for(int val : this.gramVector.values())
			len += val * val;
		for(int val : this.leafVector.values())
			len += val * val;
		len  = Math.sqrt(len);
		this.vectorLength = len;*/
		this.vectorLength = Math.sqrt(Math.pow(this.nodeTypeVectorLength, 2) + Math.pow(this.leafLabelVectorLength, 2));
	}
	public double getNodeTypeVectorLength() {
		return nodeTypeVectorLength;
	}
	public void setNodeTypeVectorLength() {
		/*double len = 0;
		for(int val : this.gramVector.values())
			len += val * val;
		len  = Math.sqrt(len);
		this.nodeTypeVectorLength = len;*/
		this.nodeTypeVectorLength = 0.0;
		for(int val : this.gramVector.values())
			this.nodeTypeVectorLength += val;
	}
	public double getLeafLabelVectorLength() {
		return leafLabelVectorLength;
	}
	public void setLeafLabelVectorLength() {
		/*double len = 0;
		for(int val : this.leafVector.values())
			len += val * val;
		len  = Math.sqrt(len);
		this.leafLabelVectorLength = len;*/
		this.leafLabelVectorLength = 0.0;
		for(int val : this.leafVector.values())
			this.leafLabelVectorLength += val;
	}
	public double getChangeVectorLength() {
		return changeVectorLength;
	}
	public void setChangeVectorLength() {
		/*double len = 0;
		for(int val : this.changeGramVector.values())
			len += val * val;
		len  = Math.sqrt(len);
		this.changeVectorLength = len;*/
		this.changeVectorLength = 0.0;
		for(int val : this.changeGramVector.values())
			this.changeVectorLength += val;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.id;
	}
	/**
	 * @return the type
	 */
	public byte getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(byte type) {
		this.type = type;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	/*public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}*/
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getStartChar() {
		return startChar;
	}
	public void setStartChar(int startChar) {
		this.startChar = startChar;
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
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	/*public int getStartToken() {
		return startToken;
	}
	public void setStartToken(int startToken) {
		this.startToken = startToken;
	}
	public int getEndToken() {
		return endToken;
	}
	public void setEndToken(int endToken) {
		this.endToken = endToken;
	}*/
	/**
	 * This map contains only the grams appearing in this fragment
	 */
	public HashMap<Integer, Integer> getSparseGramVector() {
		return this.gramVector;
	}
	/**
	 * This vector has the full size of all grams in the project. Thus, there might be many zeroed elements
	 * @return
	 */
	public int[] getFullGramVector() {
		int[] full = new int[gram2Index.size()];
		for(int index = 0; index < gram2Index.size(); index++)
			full[index] = (this.gramVector.containsKey(index)?this.gramVector.get(index):0);
		return full;
	}
	public void setGramVector(HashMap<Integer, Integer> gramVector) {
		this.gramVector = new HashMap<Integer, Integer>(gramVector);
	}
	public void setLeafVector(HashMap<String, Integer> leafVector) {
		this.leafVector = leafVector;
	}
	public HashMap<String, Integer> getLeafVector() {
		return leafVector;
	}
	public HashMap<Integer, Integer> getChangeGramVector() {
		return changeGramVector;
	}
	public void setChangeGramVector(HashMap<Integer, Integer> changeGramVector) {
		this.changeGramVector = changeGramVector;
	}
	public HashMap<String, Integer> getChangeLeafVector() {
		return changeLeafVector;
	}
	public void setChangeLeafVector(HashMap<String, Integer> changeLeafVector) {
		this.changeLeafVector = changeLeafVector;
	}
	/*
	 * 
	 */
	public boolean enclose(MapFragment other)
	{
		//if(this.sourceFile.equals(other.getSourceFile()) && (this.startChar <= other.getStartChar()) && 
		if(this.sourceFile == other.getSourceFile() && (this.startChar < other.getStartChar()) &&
				((this.startChar+this.length) > (other.getStartChar()+other.getLength())))
			return true;
		return false;
	}
	
	public double nodeTypeSimilarity(MapFragment fragment)
	{
		HashMap<Integer, Integer> v1 = new HashMap<Integer, Integer>(this.gramVector);
		HashMap<Integer, Integer> v2 = new HashMap<Integer, Integer>(fragment.getSparseGramVector());
		HashSet<Integer> keys = new HashSet<Integer>(v1.keySet());
		keys.retainAll(v2.keySet());
		
		double sim = 0.0;
		for(int key : keys) {
			sim += Math.min(v1.get(key), v2.get(key));
		}
		
		return sim * 2.0 / (this.nodeTypeVectorLength + fragment.getNodeTypeVectorLength());
	}
	public double leafLabelSimilarity(MapFragment fragment)
	{
		HashMap<String, Integer> v1 = new HashMap<String, Integer>(this.leafVector);
		HashMap<String, Integer> v2 = new HashMap<String, Integer>(fragment.getLeafVector());
		HashSet<String> keys = new HashSet<String>(v1.keySet());
		keys.retainAll(v2.keySet());
		double sim = 0.0;
		for(String key : keys) {
			sim += Math.min(v1.get(key), v2.get(key));
		}
		
		return sim * 2.0 / (this.leafLabelVectorLength + fragment.getLeafLabelVectorLength());
	}
	public double changeSimilarity(MapFragment fragment)
	{
		HashMap<Integer, Integer> v1 = new HashMap<Integer, Integer>(this.changeGramVector);
		HashMap<Integer, Integer> v2 = new HashMap<Integer, Integer>(fragment.getChangeGramVector());
		HashSet<Integer> keys = new HashSet<Integer>(v1.keySet());
		keys.retainAll(v2.keySet());
		
		double sim = 0.0;
		for (int key : keys) {
			sim += Math.min(v1.get(key), v2.get(key));
		}
		
		return sim * 2.0 / (this.changeVectorLength + fragment.getChangeVectorLength());
	}
	/*
	 * 
	 */
	/*public boolean isClonedTo(Fragment fragment)
	{
		//if(fragment.getType() != this.type || this.descendants.contains(fragment.getId()) || fragment.descendants.contains(this.id))
		if(fragment.getType() != this.type || this.enclose(fragment) || fragment.enclose(this))
			return false;
		//else if(this.clones.contains(fragment))
		else
		{
			HashMap<Integer, Integer> v1 = new HashMap<Integer, Integer>(this.gramVector);
			HashMap<Integer, Integer> v2 = new HashMap<Integer, Integer>(fragment.getSparseGramVector());
			HashSet<Integer> keys = new HashSet<Integer>(v1.keySet());
			keys.addAll(v2.keySet());
			
			double d = 0;
			for(int key : keys)
			{
				int temp = (v1.containsKey(key) ? v1.get(key) : 0) - (v2.containsKey(key) ? v2.get(key) : 0);
				d += temp * temp;
			}
			//d = Math.sqrt(d) * 2.0 / (this.vectorLength + fragment.getVectorLength());
			d = Math.sqrt(d) / Math.min(this.vectorLength, fragment.getVectorLength());
			return (d <= Main.threshold);
		}
	}*/
	static public String nGramToString(int gram) {
		StringBuffer strGram = new StringBuffer();
		
		if (gram % (1 << 24) == 0) {
			strGram.append((byte)(gram >> 24));
		}
		else {
			String separator;
			if (gram > 0) 
				separator = "--";
			else 
				separator = "<-";
			gram = Math.abs(gram);
			while (gram != 0) {
				if(strGram.length() != 0)
					strGram.insert(0, separator);
				strGram.insert(0, typeName[gram % 16]);
				gram = gram >> 4;
			}
		}
		
		return strGram.toString();
	}
	static public ArrayList<Byte> nGramToArray(int gram) {
		ArrayList<Byte> list = new java.util.ArrayList<Byte>();
		
		if (gram % (1 << 24) == 0) {
			list.add((byte)(gram >> 24));
		}
		else {
			if (gram > 0) 
				list.add((byte)0);
			else 
				list.add((byte)1);
			gram = Math.abs(gram);
			while (gram != 0) {
				list.add(1, (byte)(gram % 16));
				gram = gram >> 4;
			}
		}
		
		return list;
	}
	/*private byte getSizeOfGram(int gram) {
		byte i = 0;
		if (gram % (1 << 24) == 0) {
			i = 1;
		}
		else {			
	    	gram = Math.abs(gram);
	    	while (gram != 0) {
	    		i++;
	    		gram = gram / 16;
	    	}
		}
    	
    	return i;
    }*/
}
