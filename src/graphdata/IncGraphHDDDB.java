package graphdata;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.xerial.snappy.Snappy;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;


import structure.ArrBasedPrefixTree;
import utils.Logger;
import config.GlobalConfig;

@SuppressWarnings("unused")
public class IncGraphHDDDB extends IncGraphDB {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6220233555466502239L;

	ByteBuffer buffer;


	public TIntObjectHashMap<LinkedHashMap<Integer, ArrayList<IncGraph>>> cache1 = new TIntObjectHashMap<>();

	public TIntObjectHashMap<byte[]> cache2 = new TIntObjectHashMap<>();

	//	public HashMap<Integer,byte[]> cache = new HashMap<>();
	//	static RandomAccessFile[] out = new RandomAccessFile[GlobalConfig.numLevel1Hash];

	private static LZ4Compressor myCompressor =  LZ4Factory.fastestInstance().fastCompressor();//.highCompressor();//.fastCompressor(); 
	private static LZ4FastDecompressor myDecompressor =  LZ4Factory.fastestInstance().fastDecompressor();//.decompressor();
	//	private Map<Integer, Integer> dataFileSizeMap = new LinkedHashMap<Integ.er, Integer>();
	public int[] dataFileSizeMap = new int[GlobalConfig.numLevel1Hash];

	public IncGraphHDDDB(ArrBasedPrefixTree sequencePTree,
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

	public void clearDB(){
		if (!(new File(GlobalConfig.hashDBDir).exists()))
			new File(GlobalConfig.hashDBDir).mkdirs();
		try {
			org.apache.commons.io.FileUtils.forceDelete(new File(GlobalConfig.hashDBDir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!(new File(GlobalConfig.hashDBDir).exists()))
			new File(GlobalConfig.hashDBDir).mkdirs();

	}
	/**
	 * Flush all existing ram subdb to hard drive
	 */
	public void flushSubDBs(){
		if (!(new File(GlobalConfig.hashDBDir).exists()))
			new File(GlobalConfig.hashDBDir).mkdirs();
		for (int level1Val:graphDB.keys()){
			LinkedHashMap<Integer, ArrayList<IncGraph>> subDB = graphDB.get(level1Val);
			writeDataFile(level1Val, subDB);
		}
	}

	/*
	 * Flush all existing ram subdb to hard drive
	 */
	public void flushSubDB(int level1Val){
		LinkedHashMap<Integer, ArrayList<IncGraph>> subDB = graphDB.get(level1Val);
		writeDataFile(level1Val,subDB);
		graphDB.put(level1Val, null);
		graphDB.remove(level1Val);
	}




	/**
	 * Add subDBs from hard drive to ram db, a subdb will replace the entry in ram db if they share same hash value 
	 * @param level1Vals
	 */
	@SuppressWarnings("unchecked")
	public void addSubDBs(Set<Integer> level1Vals){
		for (int level1Val:level1Vals){
			//			String hashPath = GlobalConfig.hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
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
		//			String hashPath = GlobalConfig.hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
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
	public void doSimpleStatistics(){
		Logger.log("Max Groum Size = "+GlobalConfig.maxGroumSize);
		Logger.log("Database name = "+ dbName);
		Logger.log("Number of Java projects = "+ projectIdxMap.size());
		Logger.log("Number of classes = "+ numClasses);
		Logger.log("Number of methods = "+ numMethods);
		Logger.log("Number of LOCs = "+ LOCs);
		Logger.log("Number of added Graphs: " + numAddedGraphs);
		Logger.log("Dictionary size: " + idxTokenMap.size());
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
		Logger.log("graphDB:");
		Logger.log("\t Number of hash values: " + countHashVals());
		Logger.log("\t Number of unique graphs: " + countGraphs());

		Logger.log("Number of node at each size : " );
		doSizeStatistics();

		Logger.log("Common sequences: " );


		doTopStatistics();
	}

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

	public Set<Integer> getAllKeys(){
		Set<Integer> allKeys = new TreeSet<Integer>();
		try{
			File hashDir = new File(GlobalConfig.hashDBDir);
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

	public void doSizeStatistics(){
		Set<Integer> allKeys = getAllKeys();
		//		Logger.log("allKeys: " + allKeys);
		TreeMap<Integer, Long> allSizeMap = new TreeMap<Integer, Long>();
		for (Integer key:allKeys)
		{
			this.graphDB.clear();
			TreeSet<Integer> tmpKeys = new TreeSet<>();
			tmpKeys.add(key);
			this.addSubDBs(tmpKeys);
			TreeMap<Integer, ArrayList<IncGraph>> sizeMap = getSizeMap();
			//			Logger.log(tmpKeys);
			for (int size:sizeMap.keySet()){
				long total = 0;
				ArrayList<IncGraph> sizeList = sizeMap.get(size);
				for (IncGraph graph:sizeList){
					total+= graph.count;
				}

				if (allSizeMap.containsKey(size)){
					total+= allSizeMap.get(size);
				}
				allSizeMap.put(size, total);
			}
		}
		for (Integer size:allSizeMap.keySet()){
			Logger.log("size = " + size + "\ttotal = " + allSizeMap.get(size));
		}
	}


	//FIXME: fix it soon!
	public void doTopStatistics(){
		Logger.initDebug("topListStats.txt");
		int topListSize = GlobalConfig.topListSize; 
		Set<Integer> allKeys = getAllKeys();
		//		Logger.log("allKeys: " + allKeys);

		TreeMap<Integer, TreeMap<Integer, ArrayList<IncGraph>>> allSizeMap = new TreeMap<>();
		for (Integer key:allKeys)
		{
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

	public IncGraphHDDDB(){
		super();
	}
	public IncGraphHDDDB(IncGraphDB incGraphDB){
		wrapIncGraphDB(incGraphDB);
	}
	public void wrapIncGraphDB(IncGraphDB incGraphDB){
		this.sequencePTree = incGraphDB.sequencePTree;
		this.graphDB.putAll(incGraphDB.graphDB);
		//		this.idxTokenMap.putAll(incGraphDB.idxTokenMap);
		this.idxTokenMap.addAll(incGraphDB.idxTokenMap);
		this.tokenIdxMap.putAll(incGraphDB.tokenIdxMap);
		this.idxProjectMap.putAll(incGraphDB.idxProjectMap);
		this.projectIdxMap.putAll(incGraphDB.projectIdxMap);
		this.numAddedGraphs = incGraphDB.numAddedGraphs;
		this.numClasses = incGraphDB.numClasses;
		this.numMethods = incGraphDB.numMethods;
		this.dbName = incGraphDB.dbName;
		this.LOCs = incGraphDB.LOCs;
	}

	/**
	 * It is very important due to caching mechanism
	 */
	public void flushAllCacheWithDisk(){
		if (cache1.size()>0)
		{
			for (int level1Val:cache1.keys()){
				writeDataFileWithoutCache(level1Val, cache1.get(level1Val));
				cache1.remove(level1Val);
			}
			cache1.clear();
			cache1.compact();
		}
	}
	
	/**
	 * It is very important due to caching mechanism
	 */
	public void flushAllCache(){
		for (int level1Val:cache2.keys()){
			String filePath = GlobalConfig.hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
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
			writeDataFileWithoutCache(level1Val, cache1.get(level1Val));
			graphDB.remove(level1Val);
		}
		cache1.clear();
	}


//	/**
//	 * It is very important due to caching mechanism
//	 */
//	public void backupAllCache(){
//		for (int level1Val:cache2.keys()){
//			String filePath = GlobalConfig.hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
//			try{
//				byte[] compressed = cache2.get(level1Val);
//				storeFC(compressed, filePath, level1Val);
//
//			}
//			catch(Exception e){
//				e.printStackTrace();
//			}
//		}
//	}
	public void writeDataFileWithoutCache
	(int level1Val, LinkedHashMap<Integer, ArrayList<IncGraph>> object){
		String filePath = GlobalConfig.hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
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
//		String filePath = GlobalConfig.hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
//		if (cache2.contains(level1Val))
//			cache2.remove(level1Val);
//		cache1.put(level1Val, object);
//
//		try{
//			ByteArrayOutputStream baos = new ByteArrayOutputStream(131072);
//			BufferedOutputStream bos = new BufferedOutputStream(baos, 131072);
//			ObjectOutputStream objectOut = new ObjectOutputStream(bos);
//			objectOut.writeObject(object);
//			objectOut.close();
//			objectOut = null;
//			
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
		String filePath = GlobalConfig.hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
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
			objectOut.close();
			bo.close();
			sn.close();
			snbo.close();
			fo.close();
			objectOut = null;

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static byte[] serializeObject(Object obj) throws IOException
	{
	    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(bytesOut);
	    oos.writeObject(obj);
	    oos.flush();
	    byte[] bytes = bytesOut.toByteArray();
	    bytesOut.close();
	    oos.close();
	    return bytes;
	}

	public byte[] compress(byte[] bytes, int level1Val) throws IOException{
		return Snappy.compress(bytes);
		//		int srcLen = bytes.length;
		//		dataFileSizeMap[level1Val] = srcLen;
		//		//
		//		int maxCompressedLength = myCompressor.maxCompressedLength(srcLen);
		//		byte[] compressed = new byte[maxCompressedLength];
		//		int compressedLength = myCompressor.compress(bytes, 0, srcLen, compressed, 0, maxCompressedLength);
		//		byte[] realCompressed = Arrays.copyOf(compressed, compressedLength);
		//		compressed = null;
		//		return realCompressed;

	}

	//		private static void storeFC(byte[] bytes, String filePath) {
	//			try {
	//				Path newFile = Paths.get(filePath);
	//				Files.deleteIfExists(newFile);
	//				newFile = Files.createFile(newFile);
	//				Files.write(newFile, bytes, StandardOpenOption.WRITE);
	//			} catch (IOException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
	//		}

	protected static void storeFC(byte[] bytes, String filePath, int level1Val) {
		try {
			FileUtils.writeByteArrayToFile(new File(filePath), bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		FileOutputStream out = null;
		//		try {
		//			//Path newFile = Paths.get(filePath);
		//			//Files.deleteIfExists(newFile);
		//			out = new FileOutputStream(filePath,false);
		//			FileChannel file = out.getChannel();
		//			ByteBuffer buf = ByteBuffer.allocate(bytes.length);
		//			buf.put(bytes);
		//			buf.flip();
		//			file.write(buf);
		//			file.close();
		//			file = null;
		//			out.close();
		//			out = null;
		//			buf.clear();
		//			buf =null;
		//		} catch (IOException e) {
		//			throw new RuntimeException(e);
		//		} finally {
		//			safeClose(out);
		//		}

		//		try{
		//			RandomAccessFile file = new RandomAccessFile(filePath, "rw");
		//			FileChannel channel =file.getChannel();
		//			ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		//			for (int i = 0; i < bytes.length; i++)
		//			{
		//				buffer.put(bytes[i]); 
		//			
		//			}
		//			buffer.rewind();
		//			channel.write(buffer);
		//			
		//			
		//			channel.close();
		//			buffer.flip();
		//			file.close();
		//		}
		//		catch(Exception e){
		//			e.printStackTrace();
		//		}
	}



	private static void safeClose(OutputStream out) {
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			// do nothing
		}
	}


//	public  LinkedHashMap<Integer, ArrayList<IncGraph>> readDataFile(int level1Val){
//		String filePath = GlobalConfig.hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
//
//		try{
//			byte[] decompressed = null;
//			if (cache1.containsKey(level1Val))
//			{
//				//				decompressed = uncompress((byte[])cache.get(level1Val), level1Val);
////				decompressed = (byte[])cache1.get(level1Val);
//				return cache1.get(level1Val);
//
//			}
//			else if (cache2.containsKey(level1Val))
//			{
//				decompressed = uncompress((byte[])cache2.get(level1Val), level1Val);
//				//				decompressed = (byte[])cache2.get(level1Val);
//
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
		String filePath = GlobalConfig.hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;

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
	public byte[] uncompress(byte[] bytes, int level1Val) throws IOException{
		return Snappy.uncompress(bytes);
		//		int destSize = dataFileSizeMap[level1Val];
		//		byte[] decompressed = new byte[destSize];
		//		myDecompressor.decompress(bytes, 0, decompressed, 0, destSize);
		//		return decompressed;
	}


	protected static byte[] loadFC(String filePath, int level1Val) {
		try {
			//			return Files.readAllBytes(Paths.get(filePath));
			//			RandomAccessFile aFile = new RandomAccessFile(filePath, "r");
			//			FileChannel inChannel = aFile.getChannel();
			//			//			FileChannel inChannel = out[level1Val].getChannel();
			//			long fileSize = inChannel.size();
			//			ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
			//			inChannel.read(buffer);
			//			byte[] bytes = buffer.array();
			//			buffer.rewind();
			//			buffer.flip();
			//			buffer.clear();
			//			buffer = null;
			//			inChannel.close();
			//			aFile.close();

			File file = new File(filePath);
			byte[] bytes = FileUtils.readFileToByteArray(file);

			return bytes;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}


	//	 public static void copy(InputStream input,
	//		      OutputStream output,
	//		      int bufferSize)
	//		      throws IOException {
	//		    byte[] buf = new byte[bufferSize];
	//		    int bytesRead = input.read(buf);
	//		    while (bytesRead != -1) {
	//		      output.write(buf, 0, bytesRead);
	//		      bytesRead = input.read(buf);
	//		    }
	//		    output.flush();
	//    }

	//	private static void initDBFiles(){
	//		for (int level1Val =0; level1Val<GlobalConfig.numLevel1Hash;level1Val++)
	//		{
	//
	//
	//			try {
	//				String filePath = GlobalConfig.hashDBDir + String.valueOf(level1Val) + GlobalConfig.dataExt;
	//				//Mapping a file into memory
	//				out[level1Val]= new RandomAccessFile(filePath, "rw");
	//				//				out[level1Val] = memoryMappedFile;//memoryMappedFile.getChannel();
	//				//				memoryMappedFile.close();
	//
	//			} catch (IOException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
	//
	//		}
	//	}

	public void compactAll(){
		this.graphDB.compact();;
		this.projectIdxMap.compact();;
		this.tokenIdxMap.compact();;
		this.cache2.compact();
		this.cache1.compact();

	}
}
