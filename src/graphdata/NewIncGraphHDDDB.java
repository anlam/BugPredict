/**
 * 
 */
package graphdata;

import gnu.trove.map.hash.TObjectIntHashMap;
import graphprocessing.GraphDBUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.xerial.snappy.Snappy;












import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;
import structure.ArrBasedPrefixTree;
import utils.Logger;
import config.GlobalConfig;

/**
 * @author Administrator
 *
 */
public class NewIncGraphHDDDB extends IncGraphHDDDB {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4177805130699910471L;

	private String databasePath="";
	private String hashDBDir = "";
	private boolean isReadLZ4 = true;
	private boolean isWriteLZ4 = true;

	private static LZ4Compressor myCompressor =  LZ4Factory.fastestInstance().fastCompressor();//.highCompressor();//.fastCompressor(); 

	private static LZ4SafeDecompressor myDecompressor = 
	LZ4Factory.fastestInstance().safeDecompressor();//.decompressor();
//	byte[] decompressed;
	
	public transient TreeMap<Integer, Long> allSizeMap = new TreeMap<Integer, Long>();
	public transient TreeMap<Integer, Long> allSizeUniqueMap = new TreeMap<Integer, Long>();
	public transient TreeMap<Integer, Long> allSizeConcernedUniqueMap = new TreeMap<Integer, Long>();


//	public transient TreeMap<Integer, TreeSet<GraphUsageElementStat>> sizeUsagesMap = new TreeMap<>();
//	public transient TreeMap<Integer, TreeSet<GraphProjectElementStat>> sizeProjectsMap = new TreeMap<>();
//	public transient TreeMap<Integer, TreeSet<GraphUsageProjectElementStat>> sizeUsagesProjectsMap = new TreeMap<>();

	public transient TreeMap<Integer, TreeMap<Integer, Integer>> sizeUsagesMap = new TreeMap<>();
	public transient TreeMap<Integer, TreeMap<Integer, Integer>> sizeProjectsMap = new TreeMap<>();
	public transient TreeMap<Integer, TreeMap<Double, Integer>> sizeUsagesProjectsMap = new TreeMap<>();
	
	public transient TreeMap<Integer, TreeMap<Integer, Integer>> sizeUsagesMultiMap = new TreeMap<>();
	public transient TreeMap<Integer, TreeMap<Integer, Integer>> sizeProjectsMultiMap = new TreeMap<>();
	public transient TreeMap<Integer, TreeMap<Double, Integer>> sizeUsagesProjectsMultiMap = new TreeMap<>();

	public transient TreeMap<Integer, TreeMap<Integer, Integer>> concernedUsagesMap = new TreeMap<>();
	public transient TreeMap<Integer, TreeMap<Integer, Integer>> concernedProjectsMap = new TreeMap<>();
	public transient TreeMap<Integer, TreeMap<Double, Integer>> concernedUsagesProjectsMap = new TreeMap<>();
	
	public long numGraphs = 0;
	public long numConcernedGraphs = 0;
	public long numHashVals = 0; 
	public long numMultiProjectGraphs = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String databasePath1 = GlobalConfig.mainDir + "Storage/db_0_360/";
		String databasePath2 = GlobalConfig.mainDir + "Storage/db_361_500/";
		String databasePathOverall = GlobalConfig.mainDir + "Storage/db_0_500/";

		if (args.length>=4){
			GlobalConfig.mainDir = args[0];
			
			databasePath1 = GlobalConfig.mainDir + args[1];
			databasePath2 = GlobalConfig.mainDir + args[2];
			databasePathOverall = GlobalConfig.mainDir + args[3];
		}
		

		Logger.log("Loading database: " + databasePath1);
		NewIncGraphHDDDB hddDB1 = new NewIncGraphHDDDB(databasePath1, true,true, GlobalConfig.maxUnCompSize);
		hddDB1.doSimpleStatistics();
		Logger.log("Loading database: " + databasePath2);
		NewIncGraphHDDDB hddDB2 = new NewIncGraphHDDDB(databasePath2, true, true, GlobalConfig.maxUnCompSize);
		hddDB2.doSimpleStatistics();

		
		int cacheSize = Integer.MAX_VALUE;
		Logger.log("\r\n**********************\r\nCreating database: " + databasePathOverall);
		NewIncGraphHDDDB hddDBOverall = new NewIncGraphHDDDB(databasePathOverall, false, false, 0);
		hddDBOverall.createNew();
		
		GraphDBUtils.mergeNewHDDDatabase(hddDBOverall, hddDB1, hddDB2,cacheSize );
		hddDBOverall.writeFile(databasePathOverall + "hash_database.dat");	

		hddDBOverall.doStatistics();
	}

	
	public NewIncGraphHDDDB(String databasePath, boolean isReadLZ4, boolean isWriteLZ4,
			int decompSize){
		this.isReadLZ4 = isReadLZ4;
		this.isWriteLZ4 = isWriteLZ4;
		this.databasePath = databasePath;
		this.hashDBDir = this.databasePath + "hash_database/"; 
		String path = databasePath + "hash_database.dat";
		if (new File(path).exists())
			loadDBFromPath(path);
//		decompressed = new byte[decompSize];
	}
	public NewIncGraphHDDDB(ArrBasedPrefixTree sequencePTree,
			Map<Integer, LinkedHashMap<Integer, ArrayList<IncGraph>>> graphDB,
			//			Map<Integer, String> idxTokenMap, 
			ArrayList<String> idxTokenMap,
			//			Map<String, Integer> tokenIdxMap,
			TObjectIntHashMap<String> tokenIdxMap,
			Map<Integer, String> idxProjectMap,
			//			Map<String, Integer> projectIdxMap, 
			TObjectIntHashMap<String> projectIdxMap,
			long numAddedGraphs, long numClasses, long numMethods,
			String dbName, long LOCs) {
		this.sequencePTree = sequencePTree;
		this.graphDB.putAll(graphDB);
		//		this.idxTokenMap.putAll( idxTokenMap);
		this.idxTokenMap.addAll(idxTokenMap);
		this.tokenIdxMap.putAll( tokenIdxMap);
		this.idxProjectMap.putAll( idxProjectMap);
		this.projectIdxMap .putAll( projectIdxMap);
		this.numAddedGraphs = numAddedGraphs;
		this.numClasses = numClasses;
		this.numMethods = numMethods;
		this.dbName = dbName;
		this.LOCs = LOCs;
		//		initDBFiles();
	}
	public void createNew(){
		new File(databasePath).mkdirs();
		new File(hashDBDir).mkdirs();
	}
	
	public void loadDBFromPath(String path){
		IncGraphDB dbTmp = readFile(path); 
		this.sequencePTree = dbTmp.sequencePTree;
		this.graphDB.putAll(dbTmp.graphDB);
		//		this.idxTokenMap.putAll( idxTokenMap);
		this.idxTokenMap.addAll(dbTmp.idxTokenMap);
		this.tokenIdxMap.putAll(dbTmp. tokenIdxMap);
		this.idxProjectMap.putAll(dbTmp. idxProjectMap);
		this.projectIdxMap.putAll(dbTmp. projectIdxMap);
		this.numAddedGraphs = dbTmp.numAddedGraphs;
		this.numClasses = dbTmp.numClasses;
		this.numMethods = dbTmp.numMethods;
		this.dbName = dbTmp.dbName;
		this.LOCs = dbTmp.LOCs;
//		this.doStatistics();
	}
	
	public void wrapDB(NewIncGraphHDDDB dbTmp){
		this.copyDBComponents(dbTmp);
	}
	public void copyDBComponents(NewIncGraphHDDDB dbTmp){
		this.sequencePTree = dbTmp.sequencePTree;
		this.graphDB.putAll(dbTmp.graphDB);
		//		this.idxTokenMap.putAll( idxTokenMap);
		this.idxTokenMap.addAll(dbTmp.idxTokenMap);
		this.tokenIdxMap.putAll(dbTmp. tokenIdxMap);
		this.idxProjectMap.putAll(dbTmp. idxProjectMap);
		this.projectIdxMap.putAll(dbTmp. projectIdxMap);
		this.numAddedGraphs = dbTmp.numAddedGraphs;
		this.numClasses = dbTmp.numClasses;
		this.numMethods = dbTmp.numMethods;
		this.dbName = dbTmp.dbName;
		this.LOCs = dbTmp.LOCs;
//		this.doStatistics();
	

	}
	
	public void copyDBHash(NewIncGraphHDDDB dbTmp){
		Logger.log("hashDBDir: " + dbTmp.hashDBDir);
		File[] tmpChildren = new File(dbTmp.hashDBDir).listFiles();
		for (File tmp:tmpChildren)
		{
			Path hashDBTmp = Paths.get(dbTmp.hashDBDir + tmp.getName());
			Path hashDBThis = Paths.get(this.hashDBDir + tmp.getName());
			try {
				Files.copy(hashDBTmp, hashDBThis, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}
	
	/**
	 * It is very important due to caching mechanism
	 */
	public void flushAllCache(){
		for (int level1Val:cache2.keys()){
			String filePath = hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
			try{
				byte[] compressed = cache2.get(level1Val);
				storeFC(compressed, filePath, level1Val);

			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		cache2.clear();
		
		for (int level1Val:cache1.keys()){
			graphDB.put(level1Val, cache1.get(level1Val));
			flushSubDB(level1Val);
			graphDB.remove(level1Val);
		}
		cache1.clear();
	}
	/*
	 * Flush all existing ram subdb to hard drive
	 */
	public void flushSubDBWithoutCache(int level1Val){
		LinkedHashMap<Integer, ArrayList<IncGraph>> subDB = graphDB.get(level1Val);
		writeDataFileWithoutCache(level1Val,subDB);
		graphDB.put(level1Val, null);
		graphDB.remove(level1Val);
	}
	
//	public void writeDataFileWithoutCache(int level1Val, Object object){
//		String filePath = hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
//
//		try{
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			BufferedOutputStream bos = new BufferedOutputStream(baos, 65536);
//			ObjectOutputStream objectOut = new ObjectOutputStream(bos);
//			objectOut.writeObject(object);
//			objectOut.close();
//			objectOut = null;
//			byte[] bytes = baos.toByteArray();
//			byte[] compressed = compress(bytes, level1Val);
//			storeFC(compressed, filePath, level1Val);
//			compressed = null;
//
//			bos.close();
//			bos = null;
//			baos.close();
//			baos = null;
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
//	}
	
	public void writeDataFileWithoutCache
	(int level1Val, LinkedHashMap<Integer, ArrayList<IncGraph>> object){
		String filePath = hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
		try{
			FileOutputStream fo = new FileOutputStream(new File(filePath));
			SnappyOutputStream sn = new SnappyOutputStream(fo);
			ObjectOutputStream objectOut = new ObjectOutputStream(sn);

			objectOut.writeObject(object);
			objectOut.close();
			objectOut = null;

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	
//	public void writeDataFile(int level1Val, LinkedHashMap<Integer, ArrayList<IncGraph>> object){
//		String filePath = hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
//		cache1.put(level1Val, object);
//
//		try{
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			BufferedOutputStream bos = new BufferedOutputStream(baos, 65536);
//			ObjectOutputStream objectOut = new ObjectOutputStream(bos);
//			objectOut.writeObject(object);
//			objectOut.close();
//			objectOut = null;
//			byte[] bytes = baos.toByteArray();
//
//			if (bytes.length<=GlobalConfig.minCachedItemSize){
//				if (cache1.containsKey(level1Val)){
//					cache1.remove(level1Val);
//				}
//				if (bytes.length<=GlobalConfig.maxLowCachedItemSize){
//					cache2.put(level1Val, compress(bytes, level1Val));
//					//				if (new File(filePath).exists())
//					//					new File(filePath).delete();
//				}
//				else{
//					byte[] compressed = compress(bytes, level1Val);
//					
//					if (cache2.containsKey(level1Val)){
//						cache2.remove(level1Val);
//					}
//					//				FileOutputStream fout = new FileOutputStream(filePath);
//					//				ObjectOutputStream oos = new ObjectOutputStream(fout);   
//					//				oos.writeObject(compressed);
//					//				oos.close();
//					//				fout.close();
//					storeFC(compressed, filePath, level1Val);
//					compressed = null;
//				}
//
//				bos.close();
//				bos = null;
//				baos.close();
//				baos = null;
//			}
//
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
//	}
	
	public void writeDataFile(int level1Val, LinkedHashMap<Integer, ArrayList<IncGraph>> object){
		if (GlobalConfig.cacheLevel1ValList.contains(level1Val)){
			this.cache1.put(level1Val, object);
			return;
		}
		String filePath = hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
		try{
			FileOutputStream fo = new FileOutputStream(new File(filePath));
			BufferedOutputStream snbo = new BufferedOutputStream(fo, 65536);
			SnappyOutputStream sn = new SnappyOutputStream(snbo);
			BufferedOutputStream bo = new BufferedOutputStream(sn, 65536);
			ObjectOutputStream objectOut = new ObjectOutputStream(bo);

			objectOut.writeObject(object);
			bo.flush();
			sn.flush();
			snbo.flush();
			fo.flush();
			objectOut.reset();
			objectOut.close();
			objectOut = null;

			bo.close();
			sn.close();
			snbo.close();
			fo.close();
			
			bo.close();
			sn.close();
			snbo.close();
			fo.close();

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public byte[] compress(byte[] bytes, int level1Val) throws IOException{

//		if (isWriteLZ4)
//		{
//			int srcLen = bytes.length;
//			dataFileSizeMap[level1Val] = srcLen;
//			//
//			int maxCompressedLength = myCompressor.maxCompressedLength(srcLen);
//			byte[] compressed = new byte[maxCompressedLength];
//			int compressedLength = myCompressor.compress(bytes, 0, srcLen, compressed, 0, maxCompressedLength);
//			byte[] realCompressed = Arrays.copyOf(compressed, compressedLength);
//			compressed = null;
//			return realCompressed;
//		}
		return Snappy.compress(bytes);


	}

//	public  LinkedHashMap<Integer, ArrayList<IncGraph>> readDataFile(int level1Val){
//		String filePath = hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
////		if (level1Val%1000==0){
////			System.out.print(level1Val +"\t");
////		}
//		try{
//			byte[] decompressed = null;
//			if (cache1.containsKey(level1Val))
//			{
//				//				decompressed = uncompress((byte[])cache.get(level1Val), level1Val);
////				decompressed = (byte[])cache1.get(level1Val);
//				return cache1.get(level1Val);
//
//			}
//			if (cache2.containsKey(level1Val))
//			{
//				decompressed = uncompress((byte[])cache2.get(level1Val), level1Val);
//			}
//			else {
//				if (new File(filePath).exists())
//				{
//					//					FileInputStream fin = new FileInputStream(filePath);
//					//					ObjectInputStream ois = new ObjectInputStream(fin);
//					//					Object compObject = ois.readObject();
//					//					ois.close();
//					byte[] compressed = loadFC(filePath, level1Val); 
//					//					decompressed = Snappy.uncompress((byte[])compObject);
//					if (compressed!=null)
//						decompressed = uncompress(compressed, level1Val);
//				}
//			}
//
//			if (decompressed==null){
//				return null;
//			}
//			ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) decompressed);	
//			BufferedInputStream bis = new BufferedInputStream(bais, 65536);
//			ObjectInputStream objectIn = new ObjectInputStream(bis);
//			Object object = objectIn.readObject();
//			objectIn.close();
//			objectIn = null;
//			bis.close();
//			bis = null;
//			bais.close();
//			bais = null;
//
//			return (LinkedHashMap<Integer, ArrayList<IncGraph>>)object;
//
//		}catch(Exception ex){
//			ex.printStackTrace();
//			return null;
//		} 
//	}
	
	@SuppressWarnings("unchecked")
	public  LinkedHashMap<Integer, ArrayList<IncGraph>> readDataFile(int level1Val){
		String filePath = hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
		try{
			byte[] decompressed = null;
			if (cache1.containsKey(level1Val))
			{
				return cache1.get(level1Val);

			}
			else if (cache2.containsKey(level1Val))
			{
				decompressed = uncompress((byte[])cache2.get(level1Val), level1Val);
			}
			else {
				File file = new File(filePath);
				if (file.exists())
				{
					FileInputStream fi = new FileInputStream(file);
					
					BufferedInputStream bisi = new BufferedInputStream(fi, 65536);
					SnappyInputStream si = new SnappyInputStream(bisi);
					BufferedInputStream bi = new BufferedInputStream(si, 65536);
					ObjectInputStream objectIn = new ObjectInputStream(bi);
					LinkedHashMap<Integer, ArrayList<IncGraph>> tmp =(LinkedHashMap<Integer, ArrayList<IncGraph>>) objectIn.readObject();
					
//					objectIn.reset();
//					bi.reset();
//					si.reset();
//					bisi.reset();
//					fi.reset();
					
					objectIn.close();
					bi.close();
					si.close();
					bisi.close();
					fi.close();
					return tmp;
				}
			}


		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		} 
		return null;
	}

//	public byte[] uncompress(byte[] bytes, int level1Val) throws IOException{
//
//		if (isReadLZ4)
//		{
////			byte[] decompressed = myDecompressor.decompress(bytes, GlobalConfig.maxUnCompSize);
//			int realSize = myDecompressor.decompress(bytes, this.decompressed);
////			byte[] realValue = Arrays.copyOf(decompressed, realSize);
//		
//			return Arrays.copyOf(decompressed,realSize);
//		}
//		return Snappy.uncompress(bytes);
//	}

	public int countHashVals(){
		int numHashVals = 0;
		Set<Integer> allKeys = getAllKeys();

		//		for (int i=0; i<graphDB.size();i++){
		//			int hashValue = graphDB.keys().get(i);
		//			numGraphs += ((ArrayList<IncGraph>) graphDB.get(hashValue)).size();
		//		}

		for (Integer level1Val:allKeys)
		{
			this.graphDB.clear();
			this.addSubDB(level1Val);
			numHashVals +=  graphDB.get(level1Val).size();
		}
		return numHashVals;
	}
	
	public int countGraphs(){
		int numGraphs = 0;
		Set<Integer> allKeys = getAllKeys();

		//		for (int i=0; i<graphDB.size();i++){
		//			int hashValue = graphDB.keys().get(i);
		//			numGraphs += ((ArrayList<IncGraph>) graphDB.get(hashValue)).size();
		//		}

		for (Integer level1Val:allKeys)
		{
			this.graphDB.clear();
			this.addSubDB(level1Val);
			LinkedHashMap<Integer, ArrayList<IncGraph>> tmp = graphDB.get(level1Val);
			for (int level2Val:tmp.keySet())
			{
				numGraphs +=  tmp.get(level2Val).size();
			}
		}
		return numGraphs;
	}

//	public void doSizeStatistics(){
//		Set<Integer> allKeys = getAllKeys();
//		//		Logger.log("allKeys: " + allKeys);
//		TreeMap<Integer, Long> allSizeMap = new TreeMap<Integer, Long>();
//		for (Integer key:allKeys)
//		{
//			this.graphDB.clear();
//			TreeSet<Integer> tmpKeys = new TreeSet<>();
//			tmpKeys.add(key);
//			this.addSubDBs(tmpKeys);
//			TreeMap<Integer, ArrayList<IncGraph>> sizeMap = getSizeMap();
//			//			Logger.log(tmpKeys);
//			for (int size:sizeMap.keySet()){
//				long total = 0;
//				ArrayList<IncGraph> sizeList = sizeMap.get(size);
//				for (IncGraph graph:sizeList){
//					total+= graph.count;
//				}
//
//				if (allSizeMap.containsKey(size)){
//					total+= allSizeMap.get(size);
//				}
//				allSizeMap.put(size, total);
//			}
//		}
//		for (Integer size:allSizeMap.keySet()){
//			Logger.log("size = " + size + "\ttotal = " + allSizeMap.get(size));
//		}
//	}

	
	public void doSizeStatistics(){
		Set<Integer> allKeys = getAllKeys();
		//		Logger.log("allKeys: " + allKeys);
		
//		long idx = 0l;
		Logger.log("");
		for (Integer key:allKeys)
		{
			if (key%1000==0){
				System.out.print("  " + key);
			}
			this.graphDB.clear();
			this.addSubDB(key);
			
			numHashVals += this.graphDB.get(key).size();

			TreeMap<Integer, ArrayList<IncGraph>> sizeMap = getSizeMap();
			//			Logger.log(tmpKeys);
			for (int size:sizeMap.keySet()){
				long total = 0;
				long totalUnique = 0;
				long totalConcernedUnique = 0;
				ArrayList<IncGraph> sizeList = sizeMap.get(size);
				if (!sizeUsagesMap.containsKey(size))
					sizeUsagesMap.put(size, new TreeMap<Integer, Integer>());
				TreeMap<Integer, Integer> tmpUsage = sizeUsagesMap.get(size);
				
				if (!sizeProjectsMap.containsKey(size))
					sizeProjectsMap.put(size, new TreeMap<Integer, Integer>());
				TreeMap<Integer, Integer> tmpProject = sizeProjectsMap.get(size);
				
				if (!sizeUsagesProjectsMap.containsKey(size))
					sizeUsagesProjectsMap.put(size, new TreeMap<Double, Integer>());
				TreeMap<Double, Integer> tmpUsageProject = sizeUsagesProjectsMap.get(size);
				
				if (!sizeUsagesMultiMap.containsKey(size))
					sizeUsagesMultiMap.put(size, new TreeMap<Integer, Integer>());
				TreeMap<Integer, Integer> tmpUsageMulti = sizeUsagesMultiMap.get(size);
				
				if (!sizeProjectsMultiMap.containsKey(size))
					sizeProjectsMultiMap.put(size, new TreeMap<Integer, Integer>());
				TreeMap<Integer, Integer> tmpProjectMulti = sizeProjectsMultiMap.get(size);
				
				if (!sizeUsagesProjectsMultiMap.containsKey(size))
					sizeUsagesProjectsMultiMap.put(size, new TreeMap<Double, Integer>());
				TreeMap<Double, Integer> tmpUsageProjectMulti = sizeUsagesProjectsMultiMap.get(size);
				
				
				if (!concernedUsagesMap.containsKey(size))
					concernedUsagesMap.put(size, new TreeMap<Integer, Integer>());
				TreeMap<Integer, Integer> tmpConcernedUsage = concernedUsagesMap.get(size);
				
				if (!concernedProjectsMap.containsKey(size))
					concernedProjectsMap.put(size, new TreeMap<Integer, Integer>());
				TreeMap<Integer, Integer> tmpConcernedProject = concernedProjectsMap.get(size);
				
				if (!concernedUsagesProjectsMap.containsKey(size))
					concernedUsagesProjectsMap.put(size, new TreeMap<Double, Integer>());
				TreeMap<Double, Integer> tmpConcernedUsageProject = concernedUsagesProjectsMap.get(size);
				
				for (IncGraph graph:sizeList){
					total+= graph.count;
					totalUnique+=1;
					numGraphs++;
					
					int usages = graph.count;
					int projects = graph.projectIDs.length;
					double usageProject = (double)usages/(double)projects;

					if (!tmpUsage.containsKey(usages))
						tmpUsage.put(usages, 1);
					else{
						int val = tmpUsage.get(usages);
						tmpUsage.put(usages, val+1);
					}
					
					if (!tmpProject.containsKey(projects))
						tmpProject.put(projects, 1);
					else{
						int val = tmpProject.get(projects);
						tmpProject.put(projects, val+1);
					}
					
					if (!tmpUsageProject.containsKey(usageProject))
						tmpUsageProject.put(usageProject, 1);
					else{
						int val = tmpUsageProject.get(usageProject);
						tmpUsageProject.put(usageProject, val+1);
					}
					
					if(graph.projectIDs.length>1){
						numMultiProjectGraphs++;
						if (!tmpUsageMulti.containsKey(usages))
							tmpUsageMulti.put(usages, 1);
						else{
							int val = tmpUsageMulti.get(usages);
							tmpUsageMulti.put(usages, val+1);
						}
						if (!tmpProjectMulti.containsKey(projects))
							tmpProjectMulti.put(projects, 1);
						else{
							int val = tmpProjectMulti.get(projects);
							tmpProjectMulti.put(projects, val+1);
						}
						if (!tmpUsageProjectMulti.containsKey(usageProject))
							tmpUsageProjectMulti.put(usageProject, 1);
						else{
							int val = tmpUsageProjectMulti.get(usageProject);
							tmpUsageProjectMulti.put(usageProject, val+1);
						}
					}
					
					if (isConcernedGraph(graph)&&isContainGraph(graph)){
						numConcernedGraphs++;
						totalConcernedUnique++;
						if (!tmpConcernedUsage.containsKey(usages))
							tmpConcernedUsage.put(usages, 1);
						else{
							int val = tmpConcernedUsage.get(usages);
							tmpConcernedUsage.put(usages, val+1);
						}
						
						if (!tmpConcernedProject.containsKey(projects))
							tmpConcernedProject.put(projects, 1);
						else{
							int val = tmpConcernedProject.get(projects);
							tmpConcernedProject.put(projects, val+1);
						}
						
						if (!tmpConcernedUsageProject.containsKey(usageProject))
							tmpConcernedUsageProject.put(usageProject, 1);
						else{
							int val = tmpConcernedUsageProject.get(usageProject);
							tmpConcernedUsageProject.put(usageProject, val+1);
						}
					}
					
//					idx++;
				}
				
				
				if (allSizeMap.containsKey(size)){
					total+= allSizeMap.get(size);
				}
				allSizeMap.put(size, total);
				
				if (allSizeUniqueMap.containsKey(size)){
					totalUnique+= allSizeUniqueMap.get(size);
				}
				allSizeUniqueMap.put(size, totalUnique);
				
				if (allSizeConcernedUniqueMap.containsKey(size)){
					totalConcernedUnique+= allSizeConcernedUniqueMap.get(size);
				}
				allSizeConcernedUniqueMap.put(size, totalConcernedUnique);

			}
		}
		Logger.log("");
		
	}
	

	public boolean isConcernedGraph(IncGraph graph){
		Sequence seq = graph.getSequence();
		List<Node> nodes = seq.nodeSequence;
		
		for (Node node:nodes)
		{
			String content = idxTokenMap.get(node.content);
			content = content.replaceAll("\\s", "");
			
			boolean isContainConcernedLibs = false;
			for (String concern:GlobalConfig.concernedLibs){
				if (content.startsWith(concern)){
					isContainConcernedLibs = true;
					for (String notConcern:GlobalConfig.notConcernedLibs){
						if (content.startsWith(notConcern)){
							isContainConcernedLibs = false;
							break;
						}
					}
					if (isContainConcernedLibs)
						break;
				}
			}
			if (!isContainConcernedLibs)
				return false;
		}
		return true;
	}
	
	public boolean isContainGraph(IncGraph graph){
		Sequence seq = graph.getSequence();
		List<Node> nodes = seq.nodeSequence;
		
		for (Node node:nodes)
		{
			String content = idxTokenMap.get(node.content);
			content = content.replaceAll("\\s", "");
			
			boolean isContainConcernedLibs = false;
			for (String concern:GlobalConfig.containedLibs){
				if (content.startsWith(concern)){
					isContainConcernedLibs = true;
					return true;
				}
			}
			if (!isContainConcernedLibs)
				return false;
		}
		return false;
	}

	//FIXME: fix it soon!
	public void doTopStatistics(){
		
		Logger.log("Doing top ranked statistics:");
		Logger.initDebug("topListStats.txt");
		int topListSize = GlobalConfig.topListSize; 
		Set<Integer> allKeys = getAllKeys();
		//		Logger.log("allKeys: " + allKeys);

		TreeMap<Integer, TreeMap<Integer, ArrayList<IncGraph>>> allSizeMap = new TreeMap<>();
		
		for (Integer key:allKeys)
		{
			if (key%1000==0){
				System.out.print("  " + key);
			}
			this.graphDB.clear();
			//			if(key%10==0){
			//				Logger.log(key);
			//			}
			this.addSubDB(key);
			TreeMap<Integer, TreeMap<Integer, ArrayList<IncGraph>>> sizeCountMap = getSizeCountMap();
			for (int size:sizeCountMap.keySet()){
				TreeMap<Integer, ArrayList<IncGraph>> countMap = sizeCountMap.get(size);

				if (allSizeMap.containsKey(size)){
					TreeMap<Integer, ArrayList<IncGraph>> tmp = allSizeMap.get(size);
					for (Integer count:countMap.keySet()){
						if (tmp.containsKey(count))
							tmp.get(count).addAll(countMap.get(count));
						else
							tmp.put(count, countMap.get(count));
					}
				}
				else
				{
					allSizeMap.put(size, countMap);
				}
				TreeMap<Integer, ArrayList<IncGraph>> newCountMap = allSizeMap.get(size);
				TreeMap<Integer, ArrayList<IncGraph>> tmp =  new TreeMap<>();
				int n = 0;

				for(int count:newCountMap.descendingKeySet()){

					ArrayList<IncGraph> graphList = newCountMap.get(count);
					ArrayList<IncGraph> graphListTmp = new ArrayList<>();
					for (IncGraph graph:graphList){
//						Sequence seq = graph.getSequence();
//						String seqStr = seq.getSimpleString(this); 
						if (GlobalConfig.isFilterLirary)
						{
//							boolean isContainConcernedLibs = false;
//							for (String concern:GlobalConfig.concernedLibs){
//								if (seqStr.contains(concern)){
//									isContainConcernedLibs = true;
//									break;
//								}
//							}
//							if (!isContainConcernedLibs)
//								continue;
							if (!isConcernedGraph(graph))
								continue;
							if (!isContainGraph(graph))
								continue;
						}
						graphListTmp.add(graph);
						n++;
						if (n>topListSize+10)
						{
							tmp.put(count, graphListTmp);
							break;
						}
					}
					if (n>topListSize+10)
					{
						tmp.put(count, graphListTmp);
						break;
					}
					tmp.put(count, graphListTmp);
				}
				allSizeMap.put(size, tmp);
			}
		}
		for (Integer size:allSizeMap.keySet())	{
			Logger.logDebug("size = " + size);

			int n = 0;
			TreeMap<Integer, ArrayList<IncGraph>> countMap = allSizeMap.get(size);

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
					Logger.logDebug("\t" + seqStr + "\tcount:" + seq.count + "\t#projects:" + graph.projectIDs.length);//getProjects(graph));
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

	public void doStatistics(){
		Logger.log("Max Groum Size = "+GlobalConfig.maxGroumSize);
		Logger.log("Database name = "+ dbName);
		Logger.log("Number of Java projects = "+ projectIdxMap.size());
		Logger.log("Number of classes = "+ numClasses);
		Logger.log("Number of methods = "+ numMethods);
		Logger.log("Number of LOCs = "+ LOCs);
		Logger.log("Number of added Graphs: " + numAddedGraphs);
		Logger.log("Dictionary size: " + idxTokenMap.size());
		Logger.log("graphDB statistics:");
		long time1 = System.currentTimeMillis();
		doSizeStatistics();
		Logger.log("\t Number of hash values: " + numHashVals);
		Logger.log("\t Number of unique graphs: " + numGraphs);
		Logger.log("\t Number of unique multi-project graphs: " + numMultiProjectGraphs);
		long numProjectSpecificGraphs = numGraphs-numMultiProjectGraphs;
		double ratio = (double)numProjectSpecificGraphs/(double)numGraphs;
		Logger.log("\t Number of unique project-specific graphs: " + numProjectSpecificGraphs);
		Logger.log("\t % of unique project-specific graphs: " + String.format("%1.2f", ratio*100.0));
		Logger.log("\t Number of graphs at each size : " );
		for (Integer size:allSizeMap.keySet()){
			Logger.log("\t\tsize = " + size + "\ttotal = " + allSizeMap.get(size) + "\ttotalUnique = " + allSizeUniqueMap.get(size)
					 + "\ttotalConcernedUnique = " + allSizeConcernedUniqueMap.get(size)
//					+"\tsizeUsages size = " + sizeUsagesMap.get(size).size() 
//					+  "\tlist: " + sizeUsagesMap.get(size)
					);
		}
		long time2 = System.currentTimeMillis();
		Logger.log("Time for doing size statistics = " + ((time2-time1)/1000));

//		Logger.log("Fiding Common sequences and writing to file: " );
//		doTopStatistics();
//		long time3 = System.currentTimeMillis();
//		Logger.log("Time for finding top list = " + ((time3-time2)/1000));

		
	}




	public Set<Integer> getAllKeys(){
		Set<Integer> allKeys = new TreeSet<Integer>();
		try{
			File hashDir = new File(hashDBDir);
			File[] children = hashDir.listFiles();
			for (File child:children){
				String name = child.getName();
				if (name.endsWith(GlobalConfig.dataExt)){
					String tmp = name.substring(0, name.indexOf(GlobalConfig.dataExt));
					int key = Integer.parseInt(tmp);
					allKeys.add(key);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return allKeys;
	}

	/**
	 * Flush all existing ram subdb to hard drive
	 */
	public void flushSubDBs(){
		if (!(new File(hashDBDir).exists()))
			new File(hashDBDir).mkdirs();
		for (int level1Val:graphDB.keys()){
			LinkedHashMap<Integer, ArrayList<IncGraph>> subDB = graphDB.get(level1Val);
			writeDataFileWithoutCache(level1Val, subDB);
		}
	}

	

	public void clearDB(){
		if (!(new File(hashDBDir).exists()))
			new File(hashDBDir).mkdirs();
		try {
			org.apache.commons.io.FileUtils.forceDelete(new File(hashDBDir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!(new File(hashDBDir).exists()))
			new File(hashDBDir).mkdirs();

	}
	/*
	 * Flush all existing ram subdb to hard drive
	 */
	public void flushSubDB(int level1Val){
		LinkedHashMap<Integer, ArrayList<IncGraph>> subDB = graphDB.get(level1Val);
		writeDataFile(level1Val,subDB);
//		subDB.clear();
//		subDB = null;
//		graphDB.put(level1Val, null);
//		graphDB.remove(level1Val);
//		graphDB.compact();
	}
	
	


	/**
	 * Add subDBs from hard drive to ram db, a subdb will replace the entry in ram db if they share same hash value 
	 * @param level1Vals
	 */
	@SuppressWarnings("unchecked")
	public void addSubDBs(Set<Integer> level1Vals){
		for (int level1Val:level1Vals){
			//			String hashPath = hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
			//			Logger.log(hashPath);

			//			if (new File(hashPath).exists())
			{
				try{
					LinkedHashMap<Integer, ArrayList<IncGraph>> subDB = 
							(LinkedHashMap<Integer, ArrayList<IncGraph>>) readDataFile(level1Val);
					if (subDB!=null)
						graphDB.put(level1Val, subDB);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}


	/**
	 * Add subDBs from hard drive to ram db, a subdb will replace the entry in ram db if they share same hash value 
	 * @param level1Vals
	 */
	@SuppressWarnings("unchecked")
	public void addSubDB(int level1Val){
		//			String hashPath = hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
		//			Logger.log(hashPath);


		try{
			LinkedHashMap<Integer, ArrayList<IncGraph>> subDB = 
					(LinkedHashMap<Integer, ArrayList<IncGraph>>) readDataFile(level1Val);
			if (subDB!=null)
				graphDB.put(level1Val, subDB);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean isContainsKey(int level1Val){
		if (cache1.contains(level1Val))
			return true;
		if (cache2.contains(level1Val))
			return true;
		if (graphDB.contains(level1Val))
			return true;
		String filePath = hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
		if (new File(filePath).exists())
			return true;
		return false;
	}
	
	public void compactAll(){
		this.graphDB.compact();;
		this.projectIdxMap.compact();;
		this.tokenIdxMap.compact();;
		this.cache2.compact();
		this.cache1.compact();

	}
	
	
}
