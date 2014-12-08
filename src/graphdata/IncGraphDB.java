/**
 * 
 */
package graphdata;


import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;









//import org.mapdb.DB;
//import org.mapdb.DBMaker;
import config.GlobalConfig;
import data.MethodInfo;
import structure.ArrBasedPrefixTree;
import utils.FileUtils;
import utils.Logger;

/**
 * @author Anh
 *
 */
public class IncGraphDB implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4224441427589172168L;

	//FIXME: this type of storage may be inefficient. Should add N-gram for sequence
	//	Map<Integer, ArrayList<Sequence>> sequenceDB = new HashMap<Integer, ArrayList<Sequence>>();
	//	Map<ArrayList<Integer>, ArrayList<Sequence>> 
	public ArrBasedPrefixTree sequencePTree = new ArrBasedPrefixTree();

	//	DB db = DBMaker
	//			.newDirectMemoryDB()
	//			.transactionDisable()
	////			.asyncFlushDelay(100)
	//			.asyncWriteDisable()
	////			.compressionEnable()
	//			.make();
	//	BTreeMap<String, ArrayList<Integer>> map = db.getTreeMap("test");

	//		Map<Integer, ArrayList<IncGraph>> graphDB =   db.createHashMap("graphDB").make();//new LinkedHashMap<Integer, ArrayList<IncGraph>>();
	//	Map<Integer, IntArrayList> graphDB =   db.createHashMap("graphDB").make();//new LinkedHashMap<Integer, ArrayList<IncGraph>>();

	//	OpenIntObjectHashMap graphDB = new OpenIntObjectHashMap();
	//	public Map<Integer, LinkedHashMap<Integer, ArrayList<IncGraph>>> graphDB =   
	//			new LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<IncGraph>>>();
	public TIntObjectHashMap<LinkedHashMap<Integer, ArrayList<IncGraph>>> graphDB = new TIntObjectHashMap<>();

	//	public Map<Integer, String> idxTokenMap = new TreeMap<Integer,String>();
	public ArrayList<String> idxTokenMap = new ArrayList<>();
	public TObjectIntHashMap<String> tokenIdxMap = new TObjectIntHashMap<>();

	public Map<Integer, String> idxProjectMap = new TreeMap<Integer, String>();
	public TObjectIntHashMap<String>  projectIdxMap = new TObjectIntHashMap<String> ();

	

	public long numAddedGraphs = 0;

	public long numClasses = 0;
	public long numMethods = 0;

	public String dbName = "";
	public long LOCs = 0;

	public IncGraphDB(){
	}

	public IncGraphDB(ArrBasedPrefixTree sequencePTree,
			TIntObjectHashMap<LinkedHashMap<Integer, ArrayList<IncGraph>>> graphDB,
			//			Map<Integer, String> idxTokenMap,
			ArrayList<String> idxTokenMap,
			//			Map<String, Integer> tokenIdxMap,
			TObjectIntHashMap<String> tokenIdxMap,
			Map<Integer, String> idxProjectMap,
			//			Map<String, Integer> projectIdxMap,
			TObjectIntHashMap<String>  projectIdxMap, 

			long numAddedGraphs, long numClasses, long numMethods,
			String dbName, long LOCs) {
		this.sequencePTree = sequencePTree;
		this.graphDB.putAll(graphDB);
		this.idxTokenMap.addAll(idxTokenMap);
		this.tokenIdxMap.putAll(tokenIdxMap);
		this.idxProjectMap.putAll(idxProjectMap);
		this.projectIdxMap.putAll(projectIdxMap);
		this.numAddedGraphs = numAddedGraphs;
		this.numClasses = numClasses;
		this.numMethods = numMethods;
		this.dbName = dbName;
		this.LOCs = LOCs;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IncGraph grapha = new IncGraph(null, 1);

		Node node1 = new Node(null, null, Node.ACTION, 1, 1);
		grapha.addNode(node1);

		Node node2 = new Node(null, null, Node.ACTION, 2, 1);
		grapha.addNode(node2);

		Edge edge12 = new Edge(node1, node2, 1);
		grapha.addEdge(edge12);


		Logger.log("grapha: " + grapha);

		IncGraph graphb = new IncGraph(grapha, 1);

		Node node3 = new Node(null, null, Node.ACTION, 3, 1);
		graphb.addNode(node3);

		Edge edge13 = new Edge(node1, node3, 1);
		graphb.addEdge(edge13);

		Edge edge23 = new Edge(node2, node3, 1);
		graphb.addEdge(edge23);

		//		Node node4 = new Node(null, null, Node.ACTION, "node4", 1);
		//		graphb.addNode(node4);
		//		
		//		Edge edge34 = new Edge(node3, node4, 1);
		//		graphb.addEdge(edge34);

		IncGraph graphb2 = graphb.getExpandGraph();

		Logger.log("\r\ngraphb: " + graphb2 + "\r\n");

		ArrayList<IncGraph> allSubgraphs = graphb2.getAllSubgraphs();
		for (int i=0; i<allSubgraphs.size();i++){
			IncGraph subgraph = allSubgraphs.get(i);
			Logger.log("subgraph " + subgraph.graphIdx + ": " + subgraph);
		}

		IncGraph graphb3 = new IncGraph(graphb, 1);
		Node node4 = new Node(null, null, Node.ACTION, 4, 1);
		graphb3.addNode(node4);

		Edge edge34 = new Edge(node3, node4, 1);
		graphb3.addEdge(edge34);

		Edge edge24 = new Edge(node2, node4, 1);
		graphb3.addEdge(edge24);

		//		Node node4b = new Node(null, null, Node.ACTION, 4, 1);
		//		graphb3.addNode(node4b);

		Logger.log("roleEquals? " + graphb3.roleEquals(graphb2));

		Logger.log("sequence: " + graphb.getSequence());
		Logger.log("sequence hashcode: " + graphb3.getSequence().level1HashCode());


		Logger.log("\r\n\r\n\r\n graphb3: " + graphb3 );

		//		List<IncGraph> incGraphList = graphb3.getIncGraphList();

		IncGraphDB incGraphDB = new IncGraphDB();
		incGraphDB.addOrUpdateGraphAndSeq(graphb3, new int[]{0}, new int[]{1});

		IncGraph graphTmp1 = graphb3;
		for (int i=0; i<10000000;i++){
			IncGraph graphTmp = new IncGraph(graphb3, 1);

			//			if (i%1==0)
			{
				if (i%1000==0){
					Logger.log(i);
				}

				Node nodeTmp = new Node(null, null, Node.ACTION, i+5, 1);
				graphTmp.addNode(nodeTmp);

				Edge edgeTmp = new Edge(node3, nodeTmp, 1);
				graphTmp.addEdge(edgeTmp);

			}

			incGraphDB.addOrUpdateGraphAndSeq(graphTmp,  new int[]{0}, new int[]{1});
			graphTmp1 = graphTmp;

		}

		Logger.log("\r\n\r\n*************\r\nsequencePTree: " + incGraphDB.sequencePTree);
		Logger.log(graphTmp1.getSequence().getIdxSequence());
		Logger.log("get count at graphb3:  " + incGraphDB.sequencePTree.getCountByKey(graphb.getSequence().getIdxSequence()) );

		Logger.log("\r\n\r\n*************\r\ngraphDB: " + incGraphDB.graphDB.size() );//+"\r\n"+ incGraphDB.graphDB);
		//				Logger.log("\r\n\r\n*************\r\ngraphDB: " + incGraphDB.graphDB.size() +"\r\n"+ incGraphDB.graphDB);


	}

	public void doStatistics(){
		Logger.log("Max Groum Size = "+GlobalConfig.maxGroumSize);
		Logger.log("Database name = "+ dbName);
		Logger.log("Number of Java projects = "+ projectIdxMap.size());
		Logger.log("Number of classes = "+ numClasses);
		Logger.log("Number of methods = "+ numMethods);
		Logger.log("Number of LOCs = "+ LOCs);
		Logger.log("Number of added Graphs: " + numAddedGraphs);
		Logger.log("Dictionary size: " + idxTokenMap.size());
		Logger.log("sequencePTree: ");
		Logger.log("\t Num Nodes:" + sequencePTree.countNodes());
		Logger.log("graphDB:");
		Logger.log("\t Number of hash values: " + graphDB.size());
		Logger.log("\t Number of unique graphs: " + countGraphs());
		//		Logger.log("Dictionary: " );
		//		Logger.log("\t Size: " + tokenIdxMap.size());
		Logger.log("Common sequences: " );
		doSizeStatistics();
		doTopStatistics();
	}

	public void doSizeStatistics(){
		TreeMap<Integer, ArrayList<IncGraph>> sizeMap = getSizeMap();
		for (int size:sizeMap.keySet()){
			long total = 0;
			ArrayList<IncGraph> sizeList = sizeMap.get(size);
			for (IncGraph graph:sizeList){
				total+= graph.count;
			}
			Logger.log("size = " + size + "\ttotal = " + total);
		}
	}


	public void doGenerateSequencePTree(){
		this.sequencePTree.simpleClear();
		for (int level1Val:graphDB.keys()){
			LinkedHashMap<Integer, ArrayList<IncGraph>> subDB = graphDB.get(level1Val);
			for (int level2Val:subDB.keySet())
			{
				ArrayList<IncGraph> graphs = subDB.get(level2Val);
				for (IncGraph updatedGraph:graphs)
					sequencePTree.appendSubTree(updatedGraph.getSequence().getIdxSequence(), updatedGraph.count);
			}

		}
	}

	public void doTopStatistics(){
		Logger.initDebug("topListStats.txt");
		int topListSize = GlobalConfig.topListSize; 
		TreeMap<Integer, TreeMap<Integer, ArrayList<IncGraph>>> sizeCountMap = getSizeCountMap();
		Logger.logDebug("sizeCountMap sizes: " + sizeCountMap.size());
		for (int size:sizeCountMap.keySet()){
			Logger.logDebug("size = " + size);
			TreeMap<Integer, ArrayList<IncGraph>> countMap = sizeCountMap.get(size);
			int n = 0;
			for(int count:countMap.descendingKeySet()){//.keySet()){

				ArrayList<IncGraph> graphList = countMap.get(count);
				for (IncGraph graph:graphList){
					Sequence seq = graph.getSequence();
					String seqStr = seq.getSimpleString(this); 
					if (GlobalConfig.isFilterLirary)
					{
						boolean isContainConcernedLibs = false;
						for (String concern:GlobalConfig.concernedLibs){
							if (seqStr.contains(concern)){
								isContainConcernedLibs = true;
								break;
							}
						}
						if (!isContainConcernedLibs)
							continue;
					}
					Logger.logDebug("\t" + seqStr + "\tcount:" + seq.count + "\tprojects:" + getProjects(graph));
					n++;
					if (n>topListSize)
						break;
				}
				if (n>topListSize)
					break;
			}
		}		
		Logger.closeDebug();
	}


	public ArrayList<String> getProjects(IncGraph graph){
		ArrayList<String> projectList = new ArrayList<>();
		for (int i=0;i<graph.projectIDs.length;i++){
			int projectID = graph.projectIDs[i];
			String project = idxProjectMap.get(projectID);
			String count = String.valueOf(graph.projectCounts[i]);
			projectList.add(project+"="+count);
		}
		return projectList;
	}

	public TreeMap<Integer, TreeMap<Integer, ArrayList<IncGraph>>> getSizeCountMap(){
		TreeMap<Integer, TreeMap<Integer, ArrayList<IncGraph>>> sizeCountMap = new TreeMap<>();
		TreeMap<Integer, ArrayList<IncGraph>> sizeMap = getSizeMap();
		//		Logger.log("sizeMap size: " + sizeMap.size() );
		for (int size:sizeMap.keySet()){
			TreeMap<Integer, ArrayList<IncGraph>> countMap = new TreeMap<Integer, ArrayList<IncGraph>>();
			ArrayList<IncGraph> graphList = sizeMap.get(size);
			for (IncGraph graph:graphList){
				int count = graph.count;
				if (!countMap.containsKey(count)){
					countMap.put(count, new ArrayList<IncGraph>());
				}
				countMap.get(count).add(graph);
			}
			sizeCountMap.put(size, countMap);

		}
		return sizeCountMap;
	}

	public TreeMap<Integer, ArrayList<IncGraph>> getSizeMap(){
		TreeMap<Integer, ArrayList<IncGraph>> sizeMap = new TreeMap<Integer, ArrayList<IncGraph>>();
		for (int level1Val:graphDB.keys()){
			LinkedHashMap<Integer, ArrayList<IncGraph>> subDB = graphDB.get(level1Val);
			for (int level2Val:subDB.keySet())
			{
				ArrayList<IncGraph> graphList = subDB.get(level2Val);
				for (IncGraph graph:graphList){
					int size = graph.getGraphSize();
					if (!sizeMap.containsKey(size)){
						sizeMap.put(size, new ArrayList<IncGraph>());
					}
					sizeMap.get(size).add(graph);
				}
			}
		}

		return sizeMap;
	}


	public int countGraphs(){
		int numGraphs = 0;
		//		for (int i=0; i<graphDB.size();i++){
		//			int hashValue = graphDB.keys().get(i);
		//			numGraphs += ((ArrayList<IncGraph>) graphDB.get(hashValue)).size();
		//		}

		for (int level1Val:graphDB.keys()){
			LinkedHashMap<Integer, ArrayList<IncGraph>> tmp = graphDB.get(level1Val);
			for (int level2Val:tmp.keySet())
			{
				numGraphs +=  tmp.get(level2Val).size();
			}
		}
		return numGraphs;
	}


	public void addGroumToDatabase(MethodInfo methodInfo, String project){
		//		Logger.log("methodInfo: " + methodInfo);
		//		System.out.print("\tconvert graph ...");
		DataUtils.compactGroum(methodInfo);
		IncGraph graph = DataUtils.convertGroumToGraph(methodInfo, this);
		//		Logger.log("graph: " + graph);
		//		System.out.print("\tget subgraph ...");
		ArrayList<IncGraph> allSubGraphs = graph.getAllSubgraphs();
		int projectIdx = getProjectIdx(project);

		//		System.out.println("\tadd subgraph to database...");
		for (IncGraph subGraph:allSubGraphs){
			//			Logger.log("subGraph: " + subGraph);
			addOrUpdateGraphAndSeq(subGraph, new int[] {projectIdx}, new int[]{1});
		}
		numAddedGraphs += allSubGraphs.size();

	}

	/**
	 * Determine if a queryGraph stored in database. If yes, return reference to that graph,
	 * else return null
	 * @param queryGraph
	 * @return If there exists a matched graph, return reference to that graph, else return null
	 */
	public IncGraph findMatchedStoredGraph(IncGraph queryGraph){
		IncGraph matched = null;
		IncGraph expandedGraph = queryGraph.getExpandGraph();
		Sequence seq = expandedGraph.getSequence();
//	Logger.log("\r\n\r\n\t\t\t seq: " + seq + "Hash: "+ seq.simpleHashCode());
		int level1Val =seq.level1HashCode();
		if (graphDB.containsKey(level1Val)){
			//			Logger.log("found contain!");
			LinkedHashMap< Integer, ArrayList<IncGraph>> subDB = graphDB.get(level1Val);
			int level2Val =seq.level2HashCode();

			if (subDB.containsKey(level2Val))
			{
				ArrayList<IncGraph> graphGroup = (ArrayList<IncGraph>) subDB.get(level2Val);
				for (int i= 0; i<graphGroup.size();i++){
					IncGraph graph = graphGroup.get(i);
					//				Logger.log("\r\n graph: " + graph);
					//				Logger.log("\r\n queryGraph: " + queryGraph);

					if (graph.roleEquals(queryGraph)){
						matched = graph;
						//					Logger.log("matched: " +matched);

						break;
					}
				}
			}
		}

		return matched;		
	}
	
	
	/**
	 * Determine if a queryGraph stored in database. If yes, return reference to that graph,
	 * else return null
	 * @param queryGraph
	 * @return If there exists a matched graph, return reference to that graph, else return null
	 */
	public IncGraph findMatchedStoredGraphNew(int level1Val, int level2Val, IncGraph queryGraph){
		IncGraph matched = null;
		if (graphDB.containsKey(level1Val)){
			LinkedHashMap< Integer, ArrayList<IncGraph>> subDB = graphDB.get(level1Val);
			if (subDB.containsKey(level2Val))
			{
				ArrayList<IncGraph> graphGroup = (ArrayList<IncGraph>) subDB.get(level2Val);
				for (int i= 0; i<graphGroup.size();i++){
					IncGraph graph = graphGroup.get(i);
					if (graph.roleEquals(queryGraph)){
						matched = graph;
						break;
					}
				}
			}
		}

		return matched;		
	}

	public void clearAll(){
		this.sequencePTree = null;
		if (this.graphDB!=null)
			this.graphDB.clear();
		if (this.idxProjectMap!=null)
			this.idxProjectMap.clear();
		if (this.projectIdxMap!=null)
			this.projectIdxMap.clear();
		if (this.idxTokenMap!=null)
			this.idxTokenMap.clear();
		if (this.tokenIdxMap!=null)	
			this.tokenIdxMap.clear();
		
		this.graphDB= null;
		this.idxProjectMap= null;
		this.projectIdxMap= null;
		this.idxTokenMap= null;
		this.tokenIdxMap= null;
	}


	/**
	 * 
	 */
	public void updateGraphCount(IncGraph matchedGraph, IncGraph updatedGraph, int count, int[] projectIDs, int[] projectCounts){
		matchedGraph.updateCount(count, projectIDs, projectCounts);
	}

	/**
	 * 
	 */
	public void updateGraphAndSeqCount(IncGraph matchedGraph, IncGraph updatedGraph, int count, int[] projectIDs, int[] projectCounts){
		sequencePTree.appendSubTree(updatedGraph.getSequence().getIdxSequence(), count);
		matchedGraph.updateCount(count, projectIDs, projectCounts);
	}

	public void addGraph(IncGraph addedGraph, int[] projectIDs, int[] projectCounts){
		//		IncGraph expandedGraph = addedGraph.getExpandGraph();

		List<IncGraph> incGraphList = addedGraph.getIncGraphList();

		for (int i=incGraphList.size()-1; i>0; i--){
			IncGraph matched = findMatchedStoredGraph(incGraphList.get(i));
			//transfer address of matched graph to parent of the next graph;
			if (matched!=null){
				incGraphList.get(i-1).parentGraph = matched;
			}
			else {
				Sequence seq = incGraphList.get(i).getSequence();
				if (!graphDB.containsKey(seq.level1HashCode())){
					graphDB.put(seq.level1HashCode(),  new LinkedHashMap<Integer, ArrayList<IncGraph>>());
				}
				if (!graphDB.get(seq.level1HashCode()).containsKey(seq.level2HashCode()))
				{
					graphDB.get(seq.level1HashCode()).put(seq.level2HashCode(), new ArrayList<IncGraph>());
				}
				((ArrayList<IncGraph>)graphDB.get(seq.level1HashCode()).get(seq.level2HashCode())).add(incGraphList.get(i));
			}
		}

		Sequence seq = addedGraph.getSequence();
		if (!graphDB.containsKey(seq.level1HashCode())){
			graphDB.put(seq.level1HashCode(),  new LinkedHashMap<Integer, ArrayList<IncGraph>>());
		}
		if (!graphDB.get(seq.level1HashCode()).containsKey(seq.level2HashCode()))
		{
			graphDB.get(seq.level1HashCode()).put(seq.level2HashCode(), new ArrayList<IncGraph>());
		}
		((ArrayList<IncGraph>)graphDB.get(seq.level1HashCode()).get(seq.level2HashCode())).add(addedGraph);
	}

	public void addOrUpdateGraph(IncGraph addedGraph, int[] projectIDs, int[] projectCounts){
		//		IncGraph expandedGraph = addedGraph.getExpandGraph();
		IncGraph matchedGraph = findMatchedStoredGraph(addedGraph);

		if (matchedGraph==null)
		{

			//			//Note the reverse order of the expanded graph;
			//			for (int i=0; i<projectIDs.length; i++){
			//				addedGraph.addProjectIndex(projectIDs[i], projectCounts[i]);
			//			}
			List<IncGraph> incGraphList = addedGraph.getIncGraphList();
			//			for (IncGraph graph:incGraphList){
			//				Logger.log("\r\nincgraph " + graph.graphIdx + ": " + graph  + "\r\n\t"  + graph.getSequence().getIdxSequence());
			//			}
			Sequence seqa = addedGraph.getSequence();

			int level1Val = seqa.level1HashCode();
			if (!graphDB.containsKey(level1Val)){
				graphDB.put(level1Val,  new LinkedHashMap<Integer, ArrayList<IncGraph>>());
			}
			LinkedHashMap<Integer, ArrayList<IncGraph>> subDB = graphDB.get(level1Val);
			
			for (int i=incGraphList.size()-1; i>0; i--){
				IncGraph matched = findMatchedStoredGraph(incGraphList.get(i));
				//transfer address of matched graph to parent of the next graph;
				if (matched!=null){
					incGraphList.get(i-1).parentGraph = matched;
				}
				else {
					Sequence seq = incGraphList.get(i).getSequence();
					int level2Val = seq.level2HashCode();
					
					if (!subDB.containsKey(level2Val))
					{
						subDB.put(level2Val, new ArrayList<IncGraph>());
					}
					subDB.get(level2Val).add(incGraphList.get(i));
				}
			}

			int level2Val = seqa.level2HashCode();
			
			if (!subDB.containsKey(level2Val))
			{
				subDB.put(level2Val, new ArrayList<IncGraph>());
			}
			subDB.get(level2Val).add(addedGraph);
		}
		else {
			updateGraphCount(matchedGraph, addedGraph,  addedGraph.count, projectIDs, projectCounts);
		}
	}
	
	
	public void addOrUpdateGraphNew(
			int level1Val, int level2Val,
			IncGraph addedGraph, int[] projectIDs, int[] projectCounts){
		//		IncGraph expandedGraph = addedGraph.getExpandGraph();
		IncGraph matchedGraph = findMatchedStoredGraphNew(level1Val, level2Val, addedGraph);

		if (matchedGraph==null)
		{

			if (!graphDB.containsKey(level1Val)){
				graphDB.put(level1Val,  new LinkedHashMap<Integer, ArrayList<IncGraph>>());
			}
			LinkedHashMap<Integer, ArrayList<IncGraph>> subDB = graphDB.get(level1Val);
			List<IncGraph> incGraphList = addedGraph.getIncGraphList();

			for (int i=incGraphList.size()-1; i>0; i--){
				IncGraph matched = findMatchedStoredGraph(incGraphList.get(i));
				//transfer address of matched graph to parent of the next graph;
				if (matched!=null){
					incGraphList.get(i-1).parentGraph = matched;
				}
				else {
					Sequence seq = incGraphList.get(i).getSequence();
					int level2ValSub = seq.level2HashCode();
					
					if (!subDB.containsKey(level2ValSub))
					{
						subDB.put(level2ValSub, new ArrayList<IncGraph>());
					}
					subDB.get(level2ValSub).add(incGraphList.get(i));
				}
			}

			if (!subDB.containsKey(level2Val))
			{
				subDB.put(level2Val, new ArrayList<IncGraph>());
			}
			subDB.get(level2Val).add(addedGraph);
		}
		else {
			updateGraphCount(matchedGraph, addedGraph,  addedGraph.count, projectIDs, projectCounts);
		}
	}

	public void addOrUpdateGraphAndSeq(IncGraph addedGraph, int[] projectIDs, int[] projectCounts){
		//		IncGraph expandedGraph = addedGraph.getExpandGraph();
		IncGraph matchedGraph = findMatchedStoredGraph(addedGraph);

		if (matchedGraph==null)
		{

			//			Sequence sequence = addedGraph.getSequence();	
			//			sequencePTree.appendSubTree(sequence.getIdxSequence(), addedGraph.count);

			//Note the reverse order of the expanded graph;
			for (int i=0; i<projectIDs.length; i++){
				addedGraph.addProjectIndex(projectIDs[i], projectCounts[i]);
			}
			List<IncGraph> incGraphList = addedGraph.getIncGraphList();
			//			for (IncGraph graph:incGraphList){
			//				Logger.log("\r\nincgraph " + graph.graphIdx + ": " + graph  + "\r\n\t"  + graph.getSequence().getIdxSequence());
			//			}

			for (int i=incGraphList.size()-1; i>0; i--){
				IncGraph matched = findMatchedStoredGraph(incGraphList.get(i));
				//transfer address of matched graph to parent of the next graph;
				if (matched!=null){
					incGraphList.get(i-1).parentGraph = matched;
				}
				else {
					Sequence seq = incGraphList.get(i).getSequence();
					if (!graphDB.containsKey(seq.level1HashCode())){
						graphDB.put(seq.level1HashCode(),  new LinkedHashMap<Integer, ArrayList<IncGraph>>());
					}
					if (!graphDB.get(seq.level1HashCode()).containsKey(seq.level2HashCode()))
					{
						graphDB.get(seq.level1HashCode()).put(seq.level2HashCode(), new ArrayList<IncGraph>());
					}
					((ArrayList<IncGraph>)graphDB.get(seq.level1HashCode()).get(seq.level2HashCode())).add(incGraphList.get(i));
				}
			}

			Sequence seq = addedGraph.getSequence();
			if (!graphDB.containsKey(seq.level1HashCode())){
				graphDB.put(seq.level1HashCode(),  new LinkedHashMap<Integer, ArrayList<IncGraph>>());
			}
			if (!graphDB.get(seq.level1HashCode()).containsKey(seq.level2HashCode()))
			{
				graphDB.get(seq.level1HashCode()).put(seq.level2HashCode(), new ArrayList<IncGraph>());
			}
			((ArrayList<IncGraph>)graphDB.get(seq.level1HashCode()).get(seq.level2HashCode())).add(addedGraph);
		}
		else {
			//			if(matchedGraph.getSequence().getSimpleString(this).contains("String.equals"))
			//			{
			//				Logger.log("matchedGraph: " + matchedGraph.getSequence().getSimpleString(this) 
			//						+ "\t"+ matchedGraph.count + "\t" + addedGraph.count);
			//				Logger.log(getProjects(matchedGraph.getProjectIdxList()));
			//				Logger.log(getProjects(addedGraph.getProjectIdxList()));
			//			}
			updateGraphAndSeqCount(matchedGraph, addedGraph,  addedGraph.count, projectIDs, projectCounts);

			//			if(matchedGraph.getSequence().getSimpleString(this).contains("String.equals")){
			//				Logger.log("matchedGraph: " + matchedGraph.getSequence().getSimpleString(this) 
			//						+ "\t"+ matchedGraph.count + "\t" + addedGraph.count);
			//				Logger.log(getProjects(matchedGraph.getProjectIdxList()));
			//			}
		}

	}

	/**
	 * return project's index. If the project idx does not exist, assign and return the idx 
	 * @param token
	 * @return
	 */
	public int getProjectIdx(String project){
		String tmp = project.trim();
		if (!projectIdxMap.containsKey(tmp)){
			int idx = projectIdxMap.size();
			projectIdxMap.put(tmp, idx);
			idxProjectMap.put(idx, tmp);
		}
		return projectIdxMap.get(tmp);
	}

	/**
	 * return token index in the token dictionary. If the token does not exist in the dictionary, add the token and return the idx 
	 * @param token
	 * @return
	 */
	public int getTokenIdx(String token){
		String tmp = token.trim();
		if (!tokenIdxMap.containsKey(tmp)){
			int idx = tokenIdxMap.size();
			tokenIdxMap.put(tmp, idx);
			//			idxTokenMap.put(idx, tmp);
			idxTokenMap.add(tmp);
		}
		return tokenIdxMap.get(tmp);
	}

	public void writeFile(String filePath){
		//		FileUtils.writeObjectFile(this, filePath);
		//			fastWriteObjectFile(this, filePath);
		FileUtils.writeSnappyObjectFile(this, filePath);
	}

	/**
	 * Fast writing object using random access
	 * @param object
	 * @param filePath
	 * @throws IOException
	 */
	public static void fastWriteObjectFile( IncGraphDB object, String filePath) throws IOException 
	{
		ObjectOutputStream objectOutputStream = null;
		try {
			RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
			FileOutputStream fos = new FileOutputStream(raf.getFD());
			//			ByteArrayOutputStream out = new ByteArrayOutputStream(fos);
			BufferedOutputStream bos = new BufferedOutputStream(fos, 32768);
			objectOutputStream = new ObjectOutputStream(bos);
			objectOutputStream.writeObject(object);
			objectOutputStream.flush();
			raf.close();
		} finally {
			if (objectOutputStream != null) {
				objectOutputStream.close();
			}
		}
	}


	public static IncGraphDB readFile(String filePath){
		//		return (IncGraphDB) FileUtils.readObjectFile(filePath);
		//		return (IncGraphDB) fastReadObjectFile(filePath);
		IncGraphDB db = null;
		try {
			Logger.log(" readSnappyStreamObjectFile!" );

			db =(IncGraphDB) FileUtils.readSnappyObjectFile(filePath);
		}
		catch(Exception e){
//			e.printStackTrace();
			Logger.log(" Change to readSnappyStreamObjectFile!" );
			db = (IncGraphDB) FileUtils.readSnappyStreamObjectFile(filePath);
		}
		return db;
	}

	/**
	 * Fast reading object with random access
	 * @param filePath
	 * @return
	 */
	public static IncGraphDB fastReadObjectFile(String filePath) 
	{
		try{
			RandomAccessFile raf = new RandomAccessFile(filePath, "r");
			FileInputStream fin = new FileInputStream(raf.getFD());
			BufferedInputStream bis = new BufferedInputStream(fin, 32768);
			//			InputStream in = new ByteArrayInputStream(fin);
			ObjectInputStream ois = new ObjectInputStream(bis);
			IncGraphDB object = (IncGraphDB) ois.readObject();
			ois.close();
			raf.close();

			return object;

		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		} 
	}






}
