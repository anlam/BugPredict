/**
 * 
 */
package graphsearch;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import structure.ArrBasedPrefixTree;
import utils.FileUtils;
import utils.Logger;
import graphdata.IncGraph;
import graphdata.NewIncGraphHDDDB;
import config.ChangeConfig;
import config.GlobalConfig;

/**
 * @author Anh
 *
 */
public class MainSearch {
	static ArrBasedPrefixTree sequencePTree = new ArrBasedPrefixTree();
	static String databasePath = GlobalConfig.mainDir + "Storage/db_0_51_big/";
	
	static TreeMap<Integer, Long> sizeCountMap = new TreeMap<>();
	static TreeMap<Integer, Long> sizeCountHitMap =new TreeMap<>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<String> dataFiles = getDataFiles(ChangeConfig.fixingGroumPath);
		Logger.log(dataFiles);
		NewIncGraphHDDDB graphDB = new NewIncGraphHDDDB(databasePath, true, true, GlobalConfig.maxUnCompSize);
		doDatabaseStat(graphDB, dataFiles);
	}
	

	public static void loadDatabaseWithSeq(String databasePath){
		NewIncGraphHDDDB graphDB = new NewIncGraphHDDDB(databasePath, true, true, GlobalConfig.maxUnCompSize);
		

	}
	
	public static void createDBSeq(int level1Val){
	}
	public static void searchGraph(IncGraph graph){
	}
	
	
	public static void doDatabaseStat(NewIncGraphHDDDB graphDB, ArrayList<String> dataFiles){ 
		long count = 0;
		long countTotal = 0;
		long countTotalHit = 0;
		for (String dataFile:dataFiles){
			LinkedHashMap<Long, ArrayList<IncGraph>> revIncGraphsMap = (LinkedHashMap<Long, ArrayList<IncGraph>>) 
					FileUtils.readSnappyStreamObjectFile(dataFile);
			Logger.log(count + "\trev size: " + revIncGraphsMap.size());
			TreeMap<Integer, ArrayList<IncGraph>> level1ValIncGraphMap = new TreeMap<>(); 
			for (long rev:revIncGraphsMap.keySet()){
				ArrayList<IncGraph> revGraphs = revIncGraphsMap.get(rev);
				for (IncGraph revGraph:revGraphs){
					List<IncGraph> subGraphs = revGraph.getIncGraphList();
					for (IncGraph subGraph:subGraphs){
//						Logger.log(subGraph);
						int level1Val = subGraph.getSequence().level1HashCode();
//						int level2Val = subGraph.getSequence().level2HashCode();
//						Logger.log("l1: " + level1Val + "\t" + "l2: " + level2Val);
//						graphDB.addSubDB(level1Val);
						if (!level1ValIncGraphMap.containsKey(level1Val)){
							level1ValIncGraphMap.put(level1Val, new ArrayList<IncGraph>());
						}
						level1ValIncGraphMap.get(level1Val).add(subGraph);
						
					}
				}
			}
			for (int level1Val:level1ValIncGraphMap.keySet()){
				System.out.print("\r\n\t: " + level1Val);
				graphDB.addSubDB(level1Val);
				System.out.print("\tl");
				for (IncGraph subGraph:level1ValIncGraphMap.get(level1Val))
				{
					
					int size = subGraph.getGraphSize();
					if (!sizeCountMap.containsKey(size)){
						sizeCountMap.put(size, 1l);
					}
					else{
						long newCount = sizeCountMap.get(size);
						sizeCountMap.put(size, newCount + 1l);
					}
					
					countTotal++;
					if (countTotal%1000==0)
						System.out.print("\t" + countTotal);

					IncGraph matchedGraph =  graphDB.findMatchedStoredGraph(subGraph);
					if (matchedGraph!=null){
						countTotalHit++;
						
						if (!sizeCountHitMap.containsKey(size)){
							sizeCountHitMap.put(size, 1l);
						}
						else{
							long newCount = sizeCountHitMap.get(size);
							sizeCountHitMap.put(size, newCount + 1l);
						}
						
					}
				}
				graphDB.graphDB.remove(level1Val);
				graphDB.graphDB.clear();

				System.out.print("\t" + countTotalHit + "/"+countTotal);
			}
			
			Logger.log("\r\n\tsizeCountMap: " + sizeCountMap);
			Logger.log("\r\n\tsizeCountHitMap: " + sizeCountHitMap);
		
			count ++;
		}
		
		Logger.log("countTotalHit:" + countTotalHit);
		Logger.log("countTotal:" + countTotal);

		
	}

	
	public static ArrayList<String> getDataFiles(String groumDirPath){
		ArrayList<String> dataFiles = new ArrayList<>();
		File groumDir = new File(groumDirPath);
		File[] children = groumDir.listFiles();
		for (File child:children){
			dataFiles.add(child.getAbsolutePath());
		}
		return dataFiles;
	}
}
