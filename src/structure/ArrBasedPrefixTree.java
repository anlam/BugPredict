/**
 * 
 */
package structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;






import utils.Logger;

/**
 * @author ANH
 *
 */
public class ArrBasedPrefixTree implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7030724865753016620L;

	final static int INIT_VAL = -1;
	final static int UN_FOUND = -2;

	final public static TreeSet<Integer> countSet = new TreeSet<Integer>();
	public static  int numNodes = 0;

	static	int key = -1;
	static int level = 0;

	public int count = INIT_VAL;

	int[] childrenIdx = null;
	ArrBasedPrefixTree[] childrenPTrees = null;
	static ArrayList<Integer> idxList = new ArrayList<>();
	public static LinkedHashMap<ArrBasedPrefixTree, Integer> trainingPrefixTreeMap = new LinkedHashMap<>();
	public  ArrayList<Integer> myIdxList = new ArrayList<>();


	public void simpleClear(){
		key = -1;
		level = 0;
		numNodes = 0;
		countSet.clear();
		childrenIdx = null;
		childrenPTrees = null;
		idxList.clear();
		trainingPrefixTreeMap.clear();
		myIdxList.clear();
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return myIdxList.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrBasedPrefixTree other = (ArrBasedPrefixTree) obj;
		if (!Arrays.equals(childrenPTrees, other.childrenPTrees))
			return false;
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		Runtime runtime = Runtime.getRuntime();

		long maxMemory = runtime.maxMemory();
		long freeMemory = runtime.freeMemory();

		ArrBasedPrefixTree root = new ArrBasedPrefixTree();

		//		LinkedHashMap<List<Integer>, Integer> sequenceCountMap = new LinkedHashMap<List<Integer>, Integer>();

		int max = 50;
		int maxi = max;
		int maxj = max;
		int maxk = max;
		int maxl = max;
		for (int i=0; i<maxi;i++){
			for (int j = 0; j<maxj; j++){
				for (int k = 0; k<maxk; k++){
					int count3 = (int)i + (int)j +(int)k ;
					List<Integer> sequence3 = new ArrayList<Integer>();
					sequence3.add(i);
					sequence3.add(j);
					sequence3.add(k);
					root.appendSubTree(sequence3, count3);
					for (int l = 0; l<maxl; l++){

						int count4 = (int)i + (int)j +(int)k + (int)l;
						List<Integer> sequence4 = new ArrayList<Integer>();
						sequence4.add(i);
						sequence4.add(j);
						sequence4.add(k);
						sequence4.add(l);
						root.appendSubTree(sequence4, count4);
					}
				}
			}
		}

		long freeMemoryAfter = runtime.freeMemory();

		List<Integer> seqAdd = Arrays.asList(new Integer[]{1,2,1,2});
		root.appendSubTree(seqAdd, 12);
		//
//				List<Integer> seqAdd2 = Arrays.asList(new Integer[]{1,2,3,4});
//				root.appendSubTree(seqAdd2, 12);


		Logger.log("memory: " + maxMemory);
		Logger.log("memory used: " + (freeMemory - freeMemoryAfter));
		//		Logger.log("sequenceCountMap: " + sequenceCountMap);

		//		root.buildTreeFromMap(sequenceCountMap);
		//		Logger.log("root: \r\n" + root);
		Logger.log("root num Nodes: " + root.countNodes());

		Integer[] tmpArr = new Integer[] {0,0,0,0}; 
		List<Integer> tmpList = Arrays.asList(tmpArr);
		Logger.log("get count at 0000:  " + root.getCountByKey(tmpList) );


		Integer[] tmpArr2 = new Integer[] {1,2,1,2}; 
		List<Integer> tmpList2 = Arrays.asList(tmpArr2);
//		for (int i=0;i<10000000;i++){
//			root.getObjectByKey(tmpList2).getChildrenIdx();
//		}
		Logger.log("get count at 1212:  " + root.getCountByKey(tmpList2) );

	}

	public int[] getChildrenIdx(){
		return childrenIdx;
	}

	public ArrBasedPrefixTree[] getChildrenPTrees(){
		return childrenPTrees;
	}



	public int countNodes(){
		if (childrenIdx==null){
			return 0;
		}
		int numNodes = childrenIdx.length;
		for (int i =0; i<childrenIdx.length;i++){
			if (childrenPTrees[i]!=null)
			{
				numNodes += childrenPTrees[i].countNodes();
			}
		}

		ArrBasedPrefixTree.numNodes = numNodes; 
		return numNodes;
	}
	
	
	public void getSubs(){
		if (childrenIdx==null){
			return;
		}
		
	
		
		for (int i =0; i<childrenIdx.length;i++){
			
			
			if (childrenPTrees[i]!=null)
			{
				
				
				key = childrenIdx[i];
				idxList.add(key);
				
				
//				for (ArrBasedPrefixTree childrenPTreesChild: ((childrenPTrees[i]).getChildrenPTrees()))
//				{
//					trainingPrefixTreeMap.put(childrenPTreesChild, childrenPTreesChild.count);
//				}
				childrenPTrees[i].myIdxList.addAll(idxList);
				trainingPrefixTreeMap.put(childrenPTrees[i], childrenPTrees[i].count);
				childrenPTrees[i].getSubs();
				
				ArrayList<Integer> idxTmpList = new ArrayList<>();
				idxTmpList.addAll(idxList);
				if (idxTmpList.size()>0)
				{
					idxList = new ArrayList<>();
					idxList.addAll(idxTmpList.subList(0, idxTmpList.size()-1));
				}
				
			}

			
		}
		
		
		
		
	}
	
	

	public int getCountByKey(List<Integer> sequence){
		int count = UN_FOUND;
		Integer firstKey = sequence.get(0);
//		int idx = Arrays.binarySearch(childrenIdx, firstKey);
		int idx = binarySearch(childrenIdx, firstKey);

		if (idx >=0){			
			ArrBasedPrefixTree childrenTree = childrenPTrees[idx];

			if (sequence.size()==1){
				count = childrenTree.count;
				Logger.log("\t\tcount: " + count);
			}
			else {
				List<Integer> subSequence = sequence.subList(1, sequence.size());
				count = childrenTree.getCountByKey(subSequence);
				Logger.log("\t\tcount: " + count);

			}
		}
		return count; 
	}

	public ArrBasedPrefixTree getObjectByKey(List<Integer> sequence){
		ArrBasedPrefixTree object = null;
		if (childrenIdx!=null)
		{
			Integer firstKey = sequence.get(0);
//			int idx = Arrays.binarySearch(childrenIdx, firstKey);
			int idx = binarySearch(childrenIdx, firstKey);
			
			if (idx>=0){
				ArrBasedPrefixTree childrenTree = childrenPTrees[idx];

				if (sequence.size()==1){
					object = childrenTree;
				}
				else {
					List<Integer> subSequence = sequence.subList(1, sequence.size());
					object = childrenTree.getObjectByKey(subSequence);
				}
			}
		}
		return object; 
	}


	public void buildTreeFromMap(Map<List<Integer>, Integer> sequenceCountMap){
		//		for (List<Integer> sequence:sequenceCountMap.keySet()){
		//			this.addSubTree(sequence, sequenceCountMap.get(sequence));
		//		}

		for (Entry<List<Integer>, Integer> entry:   sequenceCountMap.entrySet()){
			this.addSubTree(entry.getKey(), entry.getValue());
		}
	}

	public void addSubTree(List<Integer> sequence, int count){
		if (sequence.size()>0)
		{
			Integer firstKey = sequence.get(0);
			List<Integer> subSequence = sequence.subList(1, sequence.size());

			if (childrenIdx!=null)
			{
//				int idx = Arrays.binarySearch(childrenIdx, firstKey);
				int idx = binarySearch(childrenIdx, firstKey);

				if (idx>=0){
					//					if(subSequence.size()>0)
					{
						childrenPTrees[idx].addSubTree(subSequence, count);
					}
				}
				else{
					ArrBasedPrefixTree childrenTree = new ArrBasedPrefixTree();
					childrenTree.addSubTree(subSequence, count);
					int insertPoint = -idx -1;
					childrenIdx = insertIntElement(childrenIdx, firstKey, insertPoint);
					childrenPTrees = insertPTreeElement(childrenPTrees, childrenTree, insertPoint);
				}
			}
			else
			{
				childrenIdx = new int[1];
				childrenPTrees = new ArrBasedPrefixTree[1];

				ArrBasedPrefixTree childrenTree = new ArrBasedPrefixTree();
				childrenTree.addSubTree(subSequence, count);
				childrenIdx[0] = firstKey;
				childrenPTrees[0] = childrenTree; 
			}

			if (subSequence.size()==0)
			{
//				int idx = Arrays.binarySearch(childrenIdx, firstKey);
				int idx = binarySearch(childrenIdx, firstKey);

				childrenPTrees[idx].count = count;
			}
		}
	}


	public void appendSubTree(List<Integer> sequence, int count){
		if (sequence.size()>0){
			Integer firstKey = sequence.get(0);
			List<Integer> subSequence = sequence.subList(1, sequence.size());
			int curIdx = UN_FOUND;
			if (childrenIdx!=null)
			{
//				int idx = Arrays.binarySearch(childrenIdx, firstKey);
				int idx = binarySearch(childrenIdx, firstKey);
				if (idx>=0){
					curIdx = idx;
					childrenPTrees[idx].appendSubTree(subSequence, count);
					
				}
				else{
					ArrBasedPrefixTree childrenTree = new ArrBasedPrefixTree();
					childrenTree.appendSubTree(subSequence, count);
					int insertPoint = -idx -1;
					curIdx = insertPoint;
					childrenIdx = insertIntElement(childrenIdx, firstKey, insertPoint);
					childrenPTrees = insertPTreeElement(childrenPTrees, childrenTree, insertPoint);
				}

			}
			else
			{
				childrenIdx = new int[1];
				childrenPTrees = new ArrBasedPrefixTree[1];

				ArrBasedPrefixTree childrenTree = new ArrBasedPrefixTree();
				childrenTree.appendSubTree(subSequence, count);
				childrenIdx[0] = firstKey;
				childrenPTrees[0] = childrenTree; 
				
				curIdx = 0;
			}

			if (subSequence.size()==0)
			{
//				int idx = Arrays.binarySearch(childrenIdx, firstKey);
//				int idx = binarySearch(childrenIdx, firstKey);

				//				Logger.log("idx 1: " + idx);
				ArrBasedPrefixTree target = childrenPTrees[curIdx];
				//				Logger.log("count: " + target.count);
				if (target.count == INIT_VAL)
				{
					target.count = count;
				}
				else {
					target.count += count;
				}

			}
		}
	}



//	/* (non-Javadoc)
//	 * @see java.lang.Object#toString()
//	 */
//	@Override
//	public String toString() {
//		StringBuilder builder = new StringBuilder();
//		builder.append( "PrefixTree [");
//		//		builder.append( "PrefixTree [key=");
//		//		builder.append(key);
//		builder.append("count=");
//
//		builder.append(count);
//		//		builder.append(", level=");
//		//		builder.append(level);
//		builder.append(", childrenTrees=");
//		level++;
//		if (childrenIdx!=null){
//			int size = childrenIdx.length;
//			for (int i = 0; i<size; i++)
//			{
//				Integer key = childrenIdx[i];
//				//				String tmp = SystemTable.nodeSeqStrRevDic.get(key);
//
//				String tmp = String.valueOf(key);
//				if (idxTokenMap!=null){
//					tmp = idxTokenMap.get(key);
//				}
//				builder.append("\r\n" + tabsAdded(level) + "\t" + level +"," +tmp +" = "  + childrenPTrees[i]);
//			}
//
//		}
//		level--;
//		builder.append("]");
//		return builder.toString();
//	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
//		builder.append( "PrefixTree [");
		builder.append(myIdxList + ", ");
		builder.append("count = " + countNodes() + ";");

		return builder.toString();
	}

	private static int[] insertIntElement(int original[], int element, int insertPoint)
	{
		int length = original.length;
		int destination[] = new int[length+1];
		System.arraycopy(original, 0, destination, 0, insertPoint);
		destination[insertPoint] = element;
		System.arraycopy(original, insertPoint, destination, insertPoint+1, length-insertPoint);
		return destination;
	}

	private static ArrBasedPrefixTree[] insertPTreeElement(ArrBasedPrefixTree original[], ArrBasedPrefixTree element, int insertPoint)
	{
		int length = original.length;
		ArrBasedPrefixTree destination[] = new ArrBasedPrefixTree[length+1];
		System.arraycopy(original, 0, destination, 0, insertPoint);
		destination[insertPoint] = element;
		System.arraycopy(original, insertPoint, destination, insertPoint+1, length-insertPoint);
		return destination;
	}


	//	/**
	//	 */
	//	public String toStringWithLevel(int maxLevel) {
	//		StringBuilder builder = new StringBuilder();
	//		if(this.level<=maxLevel)
	//		{
	//			builder.append( "PrefixTree [");
	//			//		builder.append( "PrefixTree [key=");
	//			//		builder.append(key);
	//			builder.append("count=");
	//	
	//			builder.append(count);
	//			builder.append(", level=");
	//			builder.append(level);
	//			builder.append(", childrenTrees=");
	//			if (childrenTrees!=null){
	//				int size = childrenTrees.size();
	//				for (int i = 0; i<size; i++)
	//				{
	//					Integer key = childrenTrees.keys().get(i);
	//					String tmp = SystemTable.nodeSeqStrRevDic.get(key);
	//					builder.append("\r\n" + tabsAdded(level) + "\t" +tmp +" = "  + ((PrefixTree)(childrenTrees.get(key))).toStringWithLevel(maxLevel));
	//				}
	//	
	//			}
	//			builder.append("]");
	//		}
	//		return builder.toString();
	//	}

	@SuppressWarnings("unused")
	private String tabsAdded(int level){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<=level;i++){
			sb.append("\t");
		}
		return sb.toString();
	}


	private static int binarySearch(int[] a, int key) {
		if(a.length==1){
			if (a[0]==key){
				return 0;
			}
			else if (a[0]>key){
				return -1;
			}
			else {
				return -2;
			}
		}
		int low = 0;
		int high = a.length - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
		int midVal = a[mid];

		if (midVal < key)
			low = mid + 1;
		else if (midVal > key)
			high = mid - 1;
		else
			return mid; // key found
		}
		return -(low + 1);  // key not found.
	}
	
	@SuppressWarnings("unused")
	private static int binarySearch0(int[] a, int fromIndex, int toIndex,
			int key) {
		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
		int midVal = a[mid];

		if (midVal < key)
			low = mid + 1;
		else if (midVal > key)
			high = mid - 1;
		else
			return mid; // key found
		}
		return -(low + 1);  // key not found.
	}
}
