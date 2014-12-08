/**
 * 
 */
package graphprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import config.GlobalConfig;
import structure.ArrBasedPrefixTree;
import utils.Logger;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import graphdata.Edge;
import graphdata.IncGraph;
import graphdata.IncGraphDB;
import graphdata.IncGraphHDDDB;
import graphdata.NewIncGraphHDDDB;
import graphdata.Node;

/**
 * @author Anh
 *
 */
public class GraphDBUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Logger.log("Loading database 1");
		String graphDB1Path = GlobalConfig.intermediateData + "graphDB1.dat";
		IncGraphDB dB1 = IncGraphDB.readFile(graphDB1Path);
		
		Logger.log("Loading database 2");
		String graphDB2Path = GlobalConfig.intermediateData + "graphDB2.dat";
		IncGraphDB dB2 = IncGraphDB.readFile(graphDB2Path);
		
		Logger.log("Merging databases");
		
		IncGraphDB dB = new IncGraphDB();
		if (dB1.countGraphs()>dB2.countGraphs())
			dB = mergeDatabase(dB1, dB2, true);
		else
			dB = mergeDatabase(dB2, dB1, true);

//		String graphDBMergedPath = GlobalConfig.intermediateData + "graphDBMerged.dat";
//		Logger.log("Writing database");
//		dB.writeFile(graphDBMergedPath);
		
		dB.doStatistics();

		
		Logger.log("Loadding total database");
		String graphDBTotalPath = GlobalConfig.intermediateData + "graphDB_total.dat";
		IncGraphDB dBTotal = IncGraphDB.readFile(graphDBTotalPath);
		
		Logger.log("Doing comparison");
		for (int hashValue:dBTotal.graphDB.keys()){
			int dBSize = dB.graphDB.get(hashValue).size(); 
			int dBTotalSize = dBTotal.graphDB.get(hashValue).size(); 
			if (dBSize!=dBTotalSize){
				Logger.log("diff:");
				Logger.log("db: " + dB.graphDB.get(hashValue));
				Logger.log("dbTotal: " + dBTotal.graphDB.get(hashValue));
				System.exit(0);
			}
		}
	}

	
	//TODO: implement merging Database
	/**
	 * Note that the sinkDB and sourceDB will be clear internally;
	 * @param sinkDB: input database, the source DB will be merged with it and return sinkDB
	 * @param sourceDB: input database
	 */
	public static  synchronized  IncGraphDB mergeDatabase(IncGraphDB dB1, IncGraphDB dB2, boolean isGeneratePTree){
		//merging values, including graph, count and projects
		ArrBasedPrefixTree newSequencePTree = new ArrBasedPrefixTree();
		TObjectIntHashMap<String>  combinedTokenIdxMap = new TObjectIntHashMap<String> ();
		Map<Integer, String> combinedIdxTokenMap = new TreeMap<Integer, String>();
		getCombinedMap(combinedTokenIdxMap, dB1.tokenIdxMap, dB2.tokenIdxMap, combinedIdxTokenMap);
		Logger.log("\tcombinedIdxTokenMap size: " + combinedIdxTokenMap.size());
		TObjectIntHashMap<String>  combinedProjectIdxMap = new TObjectIntHashMap<String> ();
		Map<Integer, String> combinedIdxProjectMap = new TreeMap<Integer, String>();
		getCombinedMap(combinedProjectIdxMap, dB1.projectIdxMap, dB2.projectIdxMap, combinedIdxProjectMap);
		Logger.log("\tcombinedIdxProjectMap size: " + combinedIdxProjectMap.size());

		long newNumAddedGraphs = dB1.numAddedGraphs + dB2.numAddedGraphs;
		long numClasses = dB1.numClasses + dB2.numClasses;
		long numMethods = dB1.numMethods + dB2.numMethods;
		String dbName = dB1.dbName + " + " + dB2.dbName;
		long LOCs = dB1.LOCs + dB2.LOCs;
		
		TIntObjectHashMap<LinkedHashMap<Integer, ArrayList<IncGraph>>> newGraphDB = new TIntObjectHashMap<>();
		ArrayList<String> nCombinedIdxTokenMap = new ArrayList<>();
		for (int tmp:combinedIdxTokenMap.keySet()){
			nCombinedIdxTokenMap.add(combinedIdxTokenMap.get(tmp));
		}
		IncGraphDB newIncGraphDB = new IncGraphDB
				(newSequencePTree, newGraphDB, nCombinedIdxTokenMap, combinedTokenIdxMap, 
						combinedIdxProjectMap, combinedProjectIdxMap, newNumAddedGraphs, numClasses, numMethods,
						dbName, LOCs);

		Logger.log("\tMerging " + dB1.dbName + " with graph map size: " + dB1.graphDB.size());

		newGraphDB.putAll(dB1.graphDB);
		dB1.clearAll();
		
		Logger.log("\tMerging " +  dB2.dbName);
		System.out.print("\tItem: " );
		int count2 = 0;
		ArrayList<Integer> hashValList2 = new ArrayList<>();
		TreeMap<Integer, ArrayList<IncGraph>> db2SizeMap = dB2.getSizeMap();
		hashValList2.addAll(db2SizeMap.keySet());
		for (Integer hashVal:hashValList2){
			ArrayList<IncGraph> graphList = db2SizeMap.get(hashVal);
			for (IncGraph graph:graphList){
				count2++;
				if (count2%100000==0)
					System.out.print("\t" + count2);
				Map<Integer, Node> newNodeMap = new LinkedHashMap<>();
				IncGraph convertedGraph = convertGraphToNewIdx
						(graph, dB2.idxTokenMap, combinedTokenIdxMap, dB2.idxProjectMap, combinedProjectIdxMap, newNodeMap);
				newIncGraphDB.addOrUpdateGraph(convertedGraph, convertedGraph.projectIDs, convertedGraph.projectCounts);
			}
			//clear that hash
			db2SizeMap.remove(hashVal);
		}
		System.out.println();

		dB2.clearAll();	
		if (isGeneratePTree)
			newIncGraphDB.doGenerateSequencePTree();
	
		return newIncGraphDB;
	}
	
	
	/**
	 * This function will merge a database to hard drive
	 * @param db
	 */
	public static IncGraphHDDDB patchMergeHDDDatabase(IncGraphHDDDB hddDB, IncGraphDB dB2){
		hddDB.graphDB.clear();
		Logger.log("\tReading DB from disk");

		ArrBasedPrefixTree newSequencePTree = new ArrBasedPrefixTree();
		TObjectIntHashMap<String>  combinedTokenIdxMap = new TObjectIntHashMap<String> ();
		Map<Integer, String> combinedIdxTokenMap = new TreeMap<Integer, String>();
		getCombinedMap(combinedTokenIdxMap, hddDB.tokenIdxMap, dB2.tokenIdxMap, combinedIdxTokenMap);
		TObjectIntHashMap<String>  combinedProjectIdxMap = new TObjectIntHashMap<String> ();
		Map<Integer, String> combinedIdxProjectMap = new TreeMap<Integer, String>();
		getCombinedMap(combinedProjectIdxMap, hddDB.projectIdxMap, dB2.projectIdxMap, combinedIdxProjectMap);

		
		ArrayList<String> nCombinedIdxTokenMap = new ArrayList<>();
		for (int tmp:combinedIdxTokenMap.keySet()){
			nCombinedIdxTokenMap.add(combinedIdxTokenMap.get(tmp));
		}
		
		IncGraphDB newDB2 = new IncGraphDB
				(newSequencePTree, 
				new TIntObjectHashMap<LinkedHashMap<Integer, ArrayList<IncGraph>>>(), 
				nCombinedIdxTokenMap, combinedTokenIdxMap, combinedIdxProjectMap, combinedProjectIdxMap
				, dB2.numAddedGraphs, dB2.numClasses, dB2.numMethods, dB2.dbName, dB2.LOCs);
		
		ArrayList<Integer> hashValList2 = new ArrayList<>();
		TreeMap<Integer, ArrayList<IncGraph>> db2SizeMap = dB2.getSizeMap();
		hashValList2.addAll(db2SizeMap.keySet());
		for (Integer hashVal:hashValList2){
			ArrayList<IncGraph> graphList = db2SizeMap.get(hashVal);
			for (IncGraph graph:graphList){
				Map<Integer, Node> newNodeMap = new LinkedHashMap<>();
				IncGraph convertedGraph = convertGraphToNewIdx
						(graph, dB2.idxTokenMap, combinedTokenIdxMap, dB2.idxProjectMap, combinedProjectIdxMap, newNodeMap);
				newDB2.addOrUpdateGraph(convertedGraph, convertedGraph.projectIDs, convertedGraph.projectCounts);
			}
			//clear that hash
			db2SizeMap.remove(hashVal);
		}
				
		
		Set<Integer> level1Vals  = new TreeSet<Integer>();
		for (int tmp:newDB2.graphDB.keys()){
			level1Vals.add(tmp);
		}

		hddDB.addSubDBs(level1Vals);
		
		IncGraphHDDDB newHDDDB = new IncGraphHDDDB( mergeDatabase(hddDB, dB2, false));
		Logger.log("\tFlushing DB to disk");
		newHDDDB.flushSubDBs();
		
		return newHDDDB;
	}
	
	
	/**
	 * Note that the sinkDB and sourceDB will be clear internally for memory saving;
	 * @param sinkDB: input database, the source DB will be merged with it and return sinkDB
	 * @param sourceDB: input database
	 */
	public static  IncGraphHDDDB mergeHDDDatabase(IncGraphHDDDB hddDB, IncGraphDB dB2){
		//merging values, including graph, count and projects
		ArrBasedPrefixTree newSequencePTree = new ArrBasedPrefixTree();
		TObjectIntHashMap<String>  combinedTokenIdxMap = new TObjectIntHashMap<String> ();
		Map<Integer, String> combinedIdxTokenMap = new TreeMap<Integer, String>();
		getCombinedMap(combinedTokenIdxMap, hddDB.tokenIdxMap, dB2.tokenIdxMap, combinedIdxTokenMap);
		Logger.log("\tcombinedIdxTokenMap size: " + combinedIdxTokenMap.size());
		TObjectIntHashMap<String>  combinedProjectIdxMap = new TObjectIntHashMap<String> ();
		Map<Integer, String> combinedIdxProjectMap = new TreeMap<Integer, String>();
		getCombinedMap(combinedProjectIdxMap, hddDB.projectIdxMap, dB2.projectIdxMap, combinedIdxProjectMap);
		Logger.log("\tcombinedIdxProjectMap size: " + combinedIdxProjectMap.size());

		long newNumAddedGraphs = hddDB.numAddedGraphs + dB2.numAddedGraphs;
		long numClasses = hddDB.numClasses + dB2.numClasses;
		long numMethods = hddDB.numMethods + dB2.numMethods;
		String dbName = hddDB.dbName + " + " + dB2.dbName;
		long LOCs = hddDB.LOCs + dB2.LOCs;
		
		ArrayList<String> nCombinedIdxTokenMap = new ArrayList<>();
		for (int tmp:combinedIdxTokenMap.keySet()){
			nCombinedIdxTokenMap.add(combinedIdxTokenMap.get(tmp));
		}
		
		Map<Integer, LinkedHashMap<Integer,ArrayList<IncGraph>>> newGraphDB = 
				new LinkedHashMap<Integer,  LinkedHashMap<Integer,ArrayList<IncGraph>>> ();
		IncGraphHDDDB newHDDDB = new IncGraphHDDDB
				(newSequencePTree, newGraphDB, nCombinedIdxTokenMap, combinedTokenIdxMap, 
						combinedIdxProjectMap, combinedProjectIdxMap, newNumAddedGraphs, numClasses, numMethods,
						dbName, LOCs);

		Logger.log("\tMerging " + hddDB.dbName + " with graph map size: " + hddDB.graphDB.size());

		Logger.log("Reading DB from disk");
		
		Logger.log("\tMerging " +  dB2.dbName);
		int count2 = 0;
		
		IncGraphDB newDB2 = new IncGraphDB
				(newSequencePTree, 
				new TIntObjectHashMap<LinkedHashMap<Integer, ArrayList<IncGraph>>>(), 
				nCombinedIdxTokenMap, combinedTokenIdxMap, combinedIdxProjectMap, combinedProjectIdxMap
				, dB2.numAddedGraphs, dB2.numClasses, dB2.numMethods, dB2.dbName, dB2.LOCs);
		ArrayList<Integer> hashValList2 = new ArrayList<>();
		TreeMap<Integer, ArrayList<IncGraph>> db2SizeMap = dB2.getSizeMap();
		hashValList2.addAll(db2SizeMap.keySet());
		for (Integer hashVal:hashValList2){
			ArrayList<IncGraph> graphList = db2SizeMap.get(hashVal);
			for (IncGraph graph:graphList){
				Map<Integer, Node> newNodeMap = new LinkedHashMap<>();
				IncGraph convertedGraph = convertGraphToNewIdx
						(graph, dB2.idxTokenMap, combinedTokenIdxMap, dB2.idxProjectMap, combinedProjectIdxMap, newNodeMap);
				newDB2.addGraph(convertedGraph, convertedGraph.projectIDs, convertedGraph.projectCounts);
				graph = null;
			}
			graphList.clear();
			graphList = null;
			//clear that hash
			db2SizeMap.remove(hashVal);
		}
		dB2.clearAll();
		
		Set<Integer> level1Vals  = new TreeSet<Integer>();
		for (int tmp:newDB2.graphDB.keys()){
			level1Vals.add(tmp);
		}
		
		System.out.print("\tItem: " );
		
		//FIXME: this direct transfer action can be dangerous
		newHDDDB.cache1 = hddDB.cache1;
		newHDDDB.cache2 = hddDB.cache2;
		newHDDDB.dataFileSizeMap = hddDB.dataFileSizeMap;

		for (int level1Val:level1Vals){
			newHDDDB.addSubDB(level1Val);

			Map<Integer, ArrayList<IncGraph>> subDB2 = newDB2.graphDB.get(level1Val);
			for (int level2Val:subDB2.keySet()){
				ArrayList<IncGraph> graphList = subDB2.get(level2Val);
				for (IncGraph graph:graphList){
					count2++;
					if (count2%100000==0)
						System.out.print("\t" + count2);
					newHDDDB.addOrUpdateGraphNew(
							level1Val, level2Val,
							graph, graph.projectIDs, graph.projectCounts);
				}
				graphList.clear();
				graphList = null;
			}
			newHDDDB.flushSubDB(level1Val);
			newDB2.graphDB.remove(level1Val);
			newHDDDB.graphDB.remove(level1Val);

		}
		System.out.println();
//		newDB2.clearAll();
//		newDB2 = null;
//		newGraphDB.clear();
//		nCombinedIdxTokenMap.clear();
//		combinedTokenIdxMap.clear(); 
//		combinedIdxProjectMap.clear();
//		combinedProjectIdxMap.clear();
//		
//		newGraphDB= null;
//		nCombinedIdxTokenMap= null;
//		combinedTokenIdxMap= null; 
//		combinedIdxProjectMap= null;
//		combinedProjectIdxMap= null;
//		
//		newHDDDB.compactAll();
		return newHDDDB;
	}
		
	
	
//	/**
//	 * Note that the sinkDB and sourceDB will be clear internally;
//	 * @param sinkDB: input database, the source DB will be merged with it and return sinkDB
//	 * @param sourceDB: input database
//	 */
//	@SuppressWarnings("unchecked")
//	public static  void mergeNewHDDDatabase(NewIncGraphHDDDB mergedHDDDB,
//			NewIncGraphHDDDB hddDB1, NewIncGraphHDDDB hddDB2, int cacheSize){
//		mergedHDDDB.clearDB();
//		int tmpMaxLowCachedItemSize = GlobalConfig.maxLowCachedItemSize;
//		GlobalConfig.maxLowCachedItemSize =cacheSize;
//
//		//merging values, including graph, count and projects
//		ArrBasedPrefixTree newSequencePTree = new ArrBasedPrefixTree();
//		TObjectIntHashMap<String>  combinedTokenIdxMap = new TObjectIntHashMap<String> ();
//		Map<Integer, String> combinedIdxTokenMap = new TreeMap<Integer, String>();
//		getCombinedMap(combinedTokenIdxMap, hddDB1.tokenIdxMap, hddDB2.tokenIdxMap, combinedIdxTokenMap);
//		Logger.log("\tcombinedIdxTokenMap size: " + combinedIdxTokenMap.size());
//		TObjectIntHashMap<String>  combinedProjectIdxMap = new TObjectIntHashMap<String> ();
//		Map<Integer, String> combinedIdxProjectMap = new TreeMap<Integer, String>();
//		getCombinedMap(combinedProjectIdxMap, hddDB1.projectIdxMap, hddDB2.projectIdxMap, combinedIdxProjectMap);
//		
//		hddDB1.tokenIdxMap.clear();
//		hddDB1.idxTokenMap.clear();
//		hddDB1.idxProjectMap.clear();
//		hddDB1.projectIdxMap.clear();
//		
//		Logger.log("\tcombinedIdxProjectMap size: " + combinedIdxProjectMap.size());
//
//		long newNumAddedGraphs = hddDB1.numAddedGraphs + hddDB2.numAddedGraphs;
//		long numClasses = hddDB1.numClasses + hddDB2.numClasses;
//		long numMethods = hddDB1.numMethods + hddDB2.numMethods;
//		String dbName = hddDB1.dbName + " + " + hddDB2.dbName;
//		long LOCs = hddDB1.LOCs + hddDB2.LOCs;
//		
//		ArrayList<String> nCombinedIdxTokenMap = new ArrayList<>();
//		for (int tmp:combinedIdxTokenMap.keySet()){
//			nCombinedIdxTokenMap.add(combinedIdxTokenMap.get(tmp));
//		}
//		
//		Map<Integer, LinkedHashMap<Integer,ArrayList<IncGraph>>> newGraphDB = 
//				new LinkedHashMap<Integer,  LinkedHashMap<Integer,ArrayList<IncGraph>>> ();
//		NewIncGraphHDDDB newHDDDB = new NewIncGraphHDDDB
//				(newSequencePTree, newGraphDB, nCombinedIdxTokenMap, combinedTokenIdxMap, 
//						combinedIdxProjectMap, combinedProjectIdxMap, newNumAddedGraphs, numClasses, numMethods,
//						dbName, LOCs);
//		
////		System.out.print("\tcopying hash database..");
////		newHDDDB.copyDBComponents(hddDB1);
////		System.out.print("\tfinish");
//		mergedHDDDB.copyDBComponents(newHDDDB);
//
//		Logger.log("\tMerging " + hddDB1.dbName + " with graph map size: " + hddDB1.graphDB.size());
//
//		Logger.log("Reading DB from disk");
//		
//		Logger.log("\tMerging " +  hddDB2.dbName);
////		long time1, time2, time3, time4;
//		TreeSet<Integer> vals = new TreeSet<Integer>(hddDB2.getAllKeys());
//		long numGraph = 0;
//		Logger.log("vals.size():"+vals.size());
//		int min = 966;
//		for (int level1Val:vals){
//			System.out.print(" " + level1Val);
//
//			if (level1Val<min){
//				continue;
//			}
//			if (level1Val%1000==0){
//				System.out.println();
//			}
//			if (level1Val%100==0){
//				System.gc();
//				
//				System.out.print(" " + level1Val);
//				System.out.print("(c1="+mergedHDDDB.cache1.size()+")");
//				System.out.print("(c2="+mergedHDDDB.cache2.size()+")");
//				
//			}
////			time1 = System.currentTimeMillis();
////			System.out.print("(l " );
//			System.out.print("Reading");
//
//			LinkedHashMap<Integer, ArrayList<IncGraph>> db2 = (LinkedHashMap<Integer, ArrayList<IncGraph>>) hddDB2.readDataFile(level1Val);
//			TIntObjectHashMap<LinkedHashMap<Integer, ArrayList<IncGraph>>> tmpMap = 
//					new TIntObjectHashMap<LinkedHashMap<Integer, ArrayList<IncGraph>>>(db2.size());
//			Set<Integer> tmp = new TreeSet<>(db2.keySet());
//			
//			System.out.print("Converting");
//
//			for (Integer level2Val:tmp){
//				ArrayList<IncGraph> graphList = db2.get(level2Val);
//				for (IncGraph graph:graphList)
//				{
//					Map<Integer, Node> newNodeMap = new LinkedHashMap<>();
//					IncGraph convertedGraph = convertGraphToNewIdx
//						(graph, hddDB2.idxTokenMap, combinedTokenIdxMap, 
//								hddDB2.idxProjectMap, combinedProjectIdxMap, newNodeMap);
//					newNodeMap.clear();
//					newNodeMap = null;
//					
//					int newLevel1Val = convertedGraph.getSequence().level1HashCode();
//					int newLevel2Val = convertedGraph.getSequence().level2HashCode();
//					if (!tmpMap.containsKey(newLevel1Val)){
//						tmpMap.put(newLevel1Val, new LinkedHashMap<Integer, ArrayList<IncGraph>>(0));
//					}
//					if (!(tmpMap.get(newLevel1Val).containsKey(newLevel2Val))){
//						tmpMap.get(newLevel1Val).put(newLevel2Val, new ArrayList<IncGraph>(0));
//					}
//					tmpMap.get(newLevel1Val).get(newLevel2Val).add(convertedGraph);
//					
//				}
//				graphList.clear();
//				graphList = null;
//				db2.remove(level2Val);
//			}
////			time3 = System.currentTimeMillis();
////			System.out.print((time3-time2) + ")" );
//
//			tmp.clear();
//			db2.clear();
//			
//			System.out.print("merging");
//
//			
//			int[] tmp2 = Arrays.copyOf(tmpMap.keys(), tmpMap.keys().length);
//			
//			for (int newLevel1Val:tmp2){
//				if (!mergedHDDDB.isContainsKey(level1Val))
//				{
//					hddDB1.addSubDB(newLevel1Val);
//					mergedHDDDB.graphDB = hddDB1.graphDB;
//				}
//				else{
//					mergedHDDDB.addSubDB(newLevel1Val);
//				}
//
//				for (int newLevel2Val:tmpMap.get(newLevel1Val).keySet()){
//					ArrayList<IncGraph> graphList = tmpMap.get(newLevel1Val).get(newLevel2Val);
//					for (IncGraph convertedGraph:graphList){
//						numGraph++;
//						if (numGraph%100000==0){
//							System.out.print("g:"+numGraph + " ");
//						}
//						mergedHDDDB.addOrUpdateGraph(convertedGraph, convertedGraph.projectIDs, convertedGraph.projectCounts);
//					}
//				}
//				mergedHDDDB.flushSubDB(newLevel1Val);
//				tmpMap.remove(newLevel1Val);
//			}
////			time4 = System.currentTimeMillis();
////			System.out.print((time4-time3) + ")" );
//			tmp2 = null;
//			tmpMap.clear();
//			tmpMap = null;
//			
//
//		}
//		hddDB1.clearAll();
//		hddDB1= null;
//		hddDB2.clearAll();
//		hddDB2 = null;
//		System.out.println();
//		mergedHDDDB.flushAllCache();
//		GlobalConfig.maxLowCachedItemSize = tmpMaxLowCachedItemSize;
//	}
//	
	
	
	/**
	 * Note that the sinkDB and sourceDB will be clear internally;
	 * @param sinkDB: input database, the source DB will be merged with it and return sinkDB
	 * @param sourceDB: input database
	 */
	@SuppressWarnings("unchecked")
	public static  void mergeNewHDDDatabase(NewIncGraphHDDDB mergedHDDDB,
			NewIncGraphHDDDB hddDB1, NewIncGraphHDDDB hddDB2, int cacheSize){
		mergedHDDDB.clearDB();
		int tmpMaxLowCachedItemSize = GlobalConfig.maxLowCachedItemSize;
		GlobalConfig.maxLowCachedItemSize =cacheSize;

		//merging values, including graph, count and projects
		ArrBasedPrefixTree newSequencePTree = new ArrBasedPrefixTree();
		TObjectIntHashMap<String>  combinedTokenIdxMap = new TObjectIntHashMap<String> ();
		Map<Integer, String> combinedIdxTokenMap = new TreeMap<Integer, String>();
		getCombinedMap(combinedTokenIdxMap, hddDB1.tokenIdxMap, hddDB2.tokenIdxMap, combinedIdxTokenMap);
		Logger.log("\tcombinedIdxTokenMap size: " + combinedIdxTokenMap.size());
		TObjectIntHashMap<String>  combinedProjectIdxMap = new TObjectIntHashMap<String> ();
		Map<Integer, String> combinedIdxProjectMap = new TreeMap<Integer, String>();
		getCombinedMap(combinedProjectIdxMap, hddDB1.projectIdxMap, hddDB2.projectIdxMap, combinedIdxProjectMap);
		
		hddDB1.tokenIdxMap.clear();
		hddDB1.idxTokenMap.clear();
		hddDB1.idxProjectMap.clear();
		hddDB1.projectIdxMap.clear();
		
		Logger.log("\tcombinedIdxProjectMap size: " + combinedIdxProjectMap.size());

		long newNumAddedGraphs = hddDB1.numAddedGraphs + hddDB2.numAddedGraphs;
		long numClasses = hddDB1.numClasses + hddDB2.numClasses;
		long numMethods = hddDB1.numMethods + hddDB2.numMethods;
		String dbName = hddDB1.dbName + " + " + hddDB2.dbName;
		long LOCs = hddDB1.LOCs + hddDB2.LOCs;
		
		ArrayList<String> nCombinedIdxTokenMap = new ArrayList<>();
		for (int tmp:combinedIdxTokenMap.keySet()){
			nCombinedIdxTokenMap.add(combinedIdxTokenMap.get(tmp));
		}
		
		Map<Integer, LinkedHashMap<Integer,ArrayList<IncGraph>>> newGraphDB = 
				new LinkedHashMap<Integer,  LinkedHashMap<Integer,ArrayList<IncGraph>>> ();
		NewIncGraphHDDDB newHDDDB = new NewIncGraphHDDDB
				(newSequencePTree, newGraphDB, nCombinedIdxTokenMap, combinedTokenIdxMap, 
						combinedIdxProjectMap, combinedProjectIdxMap, newNumAddedGraphs, numClasses, numMethods,
						dbName, LOCs);
		
//		System.out.print("\tcopying hash database..");
//		newHDDDB.copyDBComponents(hddDB1);
//		System.out.print("\tfinish");
		mergedHDDDB.copyDBComponents(newHDDDB);

		Logger.log("\tMerging " + hddDB1.dbName + " with graph map size: " + hddDB1.graphDB.size());

		Logger.log("Reading DB from disk");
		
		Logger.log("\tMerging " +  hddDB2.dbName);
//		long time1, time2, time3, time4;
		TreeSet<Integer> vals = new TreeSet<Integer>(hddDB2.getAllKeys());
		long numGraph = 0;
		Logger.log("vals.size():"+vals.size());
//		int min = 10172;
		for (int level1Val:vals){

//			if (level1Val<min){
//				continue;
//			}
			
//			System.out.print(" " + level1Val);

			if (level1Val%1000==0){
				System.out.println();
			}
			if (level1Val%100==0){
				System.gc();
				
				System.out.print(" " + level1Val);
//				System.out.print("(c1="+mergedHDDDB.cache1.size()+")");
//				System.out.print("(c2="+mergedHDDDB.cache2.size()+")");
				
			}
//			System.out.print("   Reading db2");

			LinkedHashMap<Integer, ArrayList<IncGraph>> db2 = (LinkedHashMap<Integer, ArrayList<IncGraph>>) hddDB2.readDataFile(level1Val);
//			TIntObjectHashMap<LinkedHashMap<Integer, ArrayList<IncGraph>>> tmpMap = 
//					new TIntObjectHashMap<LinkedHashMap<Integer, ArrayList<IncGraph>>>(db2.size());
			Set<Integer> tmp = new TreeSet<>(db2.keySet());
			
//			System.out.print("  Reading db1");

			if (!mergedHDDDB.isContainsKey(level1Val))
			{
				hddDB1.addSubDB(level1Val);
				mergedHDDDB.graphDB = hddDB1.graphDB;
			}
			else{
				mergedHDDDB.addSubDB(level1Val);
			}
//			System.out.print("   Converting and adding");

			for (Integer level2Val:tmp){
				ArrayList<IncGraph> graphList = db2.get(level2Val);
				for (IncGraph graph:graphList)
				{
					
					numGraph++;
					if (numGraph%100000==0){
						System.out.print("g:"+numGraph + " ");
					}
					
					Map<Integer, Node> newNodeMap = new LinkedHashMap<>();
					IncGraph convertedGraph = convertGraphToNewIdx
						(graph, hddDB2.idxTokenMap, combinedTokenIdxMap, 
								hddDB2.idxProjectMap, combinedProjectIdxMap, newNodeMap);
					newNodeMap.clear();
					newNodeMap = null;
					
					int newLevel1Val = convertedGraph.getSequence().level1HashCode();
					int newLevel2Val = convertedGraph.getSequence().level2HashCode();
					
					if (!mergedHDDDB.isContainsKey(newLevel1Val))
					{
						hddDB1.addSubDB(newLevel1Val);
						mergedHDDDB.graphDB = hddDB1.graphDB;
					}
					else{
						mergedHDDDB.addSubDB(newLevel1Val);
					}
					mergedHDDDB.addOrUpdateGraphNew(newLevel1Val, newLevel2Val, convertedGraph, convertedGraph.projectIDs, convertedGraph.projectCounts);

										
				}
				
				graphList.clear();
				graphList = null;
				db2.remove(level2Val);
			}
			
//			System.out.print("   Writing");
			mergedHDDDB.flushSubDBs();

			mergedHDDDB.graphDB.clear();
//			time3 = System.currentTimeMillis();
//			System.out.print((time3-time2) + ")" );

			tmp.clear();
			db2.clear();
			


		}
		hddDB1.clearAll();
		hddDB1= null;
		hddDB2.clearAll();
		hddDB2 = null;
		System.out.println();
		mergedHDDDB.flushAllCache();
		GlobalConfig.maxLowCachedItemSize = tmpMaxLowCachedItemSize;
	}
	
	
	
	
	/**
	 * Since merging database needs modifying indices, it need to convert graph
	 * @param oriGraph
	 * @param origIdxStrMap
	 * @param combinedTokenIdxMap
	 * @param origIdxProjectMap
	 * @param combinedProjectIdxMap
	 * @return
	 */
	public static IncGraph convertGraphToNewIdx(IncGraph oriGraph, 
//			Map<Integer, String> origIdxStrMap,
			ArrayList<String> origIdxStrMap,
			TObjectIntHashMap<String>  combinedTokenIdxMap,
			Map<Integer, String> origIdxProjectMap,
			TObjectIntHashMap<String>  combinedProjectIdxMap, 
			Map<Integer, Node> newNodeMap){
		IncGraph newGraph = new IncGraph(oriGraph, oriGraph.getGraphSize());
		
		if (oriGraph.parentGraph!=null){
			newGraph.parentGraph = 
					convertGraphToNewIdx(oriGraph.parentGraph, origIdxStrMap, combinedTokenIdxMap, origIdxProjectMap, combinedProjectIdxMap,
							newNodeMap);
		}
		else {
			newGraph.parentGraph = null;
		}
		
		List<Node> newAddedNodes = new ArrayList<Node>();
//		Map<Node, Node> newNodeMap = new  LinkedHashMap<Node, Node>();
		for (Node node:oriGraph.addedNodes){
			int newContent = convertIdx(node.content, origIdxStrMap, combinedTokenIdxMap);
//			Node newNode = new Node(node.inEdges, node.outEdges, node.nodeRole, newContent, node.count);
//			Node newNode = new Node(null, null, node.nodeRole, newContent, node.count);
			Node newNode = new Node(null, null, node.nodeRole, newContent, 1);

			newNodeMap.put(node.simpleHashCode(), newNode);
			newAddedNodes.add(newNode);
		}

		
		
//		for (Node node:oriGraph.getExpandGraph().addedNodes){
//			int newContent = convertIdx(node.content, origIdxStrMap, combinedTokenIdxMap);
//			Node newNode = new Node(node.inEdges, node.outEdges, node.nodeRole, newContent, node.count);
//			newNodeMap.put(node, newNode);
//		}
		
		
		newGraph.addedNodes = newAddedNodes.toArray(new Node[]{});
		newAddedNodes.clear();

		if (oriGraph.addedEdges!=null)
		{
//			List<Edge> addedEdges = Arrays.asList(oriGraph.addedEdges);

			List<Edge>  newAddedEdges = new ArrayList<Edge>();
			for (Edge edge:oriGraph.addedEdges){
				Node newSourceNode = newNodeMap.get(edge.sourceNode.simpleHashCode());
				if (newSourceNode==null){
					Logger.log(edge.sourceNode);
					Logger.log("newSourceNode: " + newSourceNode);
					Logger.log("newNodeMap: " + newNodeMap);

					System.exit(0);
				}
				Node newSinkNode = newNodeMap.get(edge.sinkNode.simpleHashCode());
//				Edge newEdge = new Edge(newSourceNode, newSinkNode, edge.count);
				Edge newEdge = new Edge(newSourceNode, newSinkNode, 1);

				newAddedEdges.add(newEdge);
			}
			newGraph.addedEdges = newAddedEdges.toArray(new Edge[]{});
//			addedEdges.clear();
			newAddedEdges.clear();
		}
		else {
			newGraph.addedEdges = null;
		}

		newGraph.projectIDs = new int[oriGraph.projectIDs.length];
		for (int i=0; i<oriGraph.projectIDs.length;i++)
		{
			int projectIdx = oriGraph.projectIDs[i];
			newGraph.projectIDs[i] = convertIdx(projectIdx, origIdxProjectMap, combinedProjectIdxMap);
		}
		newGraph.projectCounts = new int[oriGraph.projectCounts.length];
		for (int i=0; i<oriGraph.projectCounts.length;i++)
		{
			newGraph.projectCounts[i] = oriGraph.projectCounts[i] ;
		}
		
		newGraph.graphIdx = oriGraph.graphIdx;
		newGraph.count = oriGraph.count;
		return newGraph;
	}
	

	/**
	 * Convert idx from old graph to new graph
	 * @param idx
	 * @param origIdxStrMap
	 * @param combinedStrIdxMap
	 * @return
	 */
	public static int convertIdx(int idx, List<String> origIdxStrMap,  TObjectIntHashMap<String> combinedStrIdxMap){
		int newIdx = -1;
		String tmp = "";
		try{
			tmp = origIdxStrMap.get(idx);
			newIdx = combinedStrIdxMap.get(tmp);
		}
		catch(Exception e){
			Logger.log("Exception: " + tmp);
		}
		return newIdx;
	}
	
	
	
	/**
	 * Convert idx from old graph to new graph
	 * @param idx
	 * @param origIdxStrMap
	 * @param combinedStrIdxMap
	 * @return
	 */
	public static int convertIdx(int idx, Map<Integer, String> origIdxStrMap,  TObjectIntHashMap<String>  combinedStrIdxMap){
		int newIdx = -1;
		String tmp = "";
		try{
			tmp = origIdxStrMap.get(idx);
			newIdx = combinedStrIdxMap.get(tmp);
		}
		catch(Exception e){
			Logger.log("Exception: " + tmp);
		}
		return newIdx;
	}
	
	/**
	 * Combine index maps to new map 
	 * @param combinedStrIdxMap
	 * @param map1
	 * @param map2
	 * @param combinedIdxStrMap
	 */
	
	public static void getCombinedMap(TObjectIntHashMap<String> combinedStrIdxMap, 
			TObjectIntHashMap<String>  map1, TObjectIntHashMap<String>  map2, 
			Map<Integer, String> combinedIdxStrMap){
//		Map<String, Integer> combinedStrIdxMapTmp = new LinkedHashMap<String, Integer>();
//		Map<Integer, String> combinedIdxStrMapTmp = new TreeMap<Integer, String>();
		
//		for (String str:map1.keySet()){
//			int size = combinedStrIdxMap.size();
//			combinedStrIdxMap.put(str, size);
//			combinedIdxStrMap.put(size, str);
//		}
		combinedStrIdxMap.putAll(map1);
		for (String str:combinedStrIdxMap.keySet()){
			combinedIdxStrMap.put(combinedStrIdxMap.get(str), str);
		}
		
		for (String str:map2.keySet()){
			if (!combinedStrIdxMap.containsKey(str)){
				int idx = map2.get(str);
				if (combinedIdxStrMap.containsKey(idx))
				{
					int size = combinedStrIdxMap.size();
					int tmp = size; 
					while (combinedIdxStrMap.containsKey(tmp))
					{
						tmp++;
					}
					combinedStrIdxMap.put(str, tmp);
					combinedIdxStrMap.put(tmp, str);
				}
				else
				{
					combinedStrIdxMap.put(str, idx);
					combinedIdxStrMap.put(idx, str);
				}
			}
		}
//		combinedIdxStrMap = combinedIdxStrMap;
//		combinedStrIdxMap = combinedStrIdxMap;
	}
}
