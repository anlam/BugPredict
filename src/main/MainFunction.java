/**
 * 
 */
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import graphdata.IncGraphDB;
import graphdata.IncGraphHDDDB;
import graphprocessing.GraphDBUtils;
import groumvisitors.JavaGroumVisitor;
import repository.SnapshotCreation;
import utils.FileUtils;
import utils.Logger;
import config.GlobalConfig;
import data.MethodInfo;
import data.NodeSequenceInfoMap;
import data.TypeInfo;

/**
 * @author Anh
 *
 */
public class MainFunction {
	static IncGraphDB incGraphDB = new IncGraphDB();
	static long numJavaProjects = 0;
	static long numClasses = 0;
	static long numMethods = 0;
	static long LOCs = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//		doMain();
		Logger.log("args: " + Arrays.asList(args));
		if (args.length>=3){
			GlobalConfig.mainDir = args[0];
			GlobalConfig.minCachedItemSize = Integer.parseInt(args[1]);
			GlobalConfig.maxLowCachedItemSize = Integer.parseInt(args[2]);
			GlobalConfig.refreshParams();
			if (args.length>=4){
				GlobalConfig.isStartFromBeginning = Boolean.parseBoolean(args[3]);
			}
			if (args.length>=5){
				GlobalConfig.startMergeSlotCount = Integer.parseInt(args[4]);

			}
			
			if (args.length>=6){
				GlobalConfig.backupStep = Integer.parseInt(args[5]);
			}
		}
		//
		//		Logger.log("mainDir: " + GlobalConfig.mainDir);

		//		List<String> projectList = getProjectFromDirDat(GlobalConfig.projectDataDir);
		//		processSlotProjects(projectList, "");
		//		test();

		//		if (GlobalConfig.isRedirectErrMsg){
		//			try {
		//				System.setErr(new PrintStream(GlobalConfig.logPath));
		//			} catch (FileNotFoundException e) {
		//				// TODO Auto-generated catch block
		//				e.printStackTrace();
		//			}
		//		}

		//		buildDatabases();
		//		System.gc();

		//				mergeWithRamDB();
		//				patchMergeWithHDDDB();

//		mergeWithHDDDB();
		mergeWithHDDDBWithDisk();
		//		IncGraphHDDDB hddDB = IncGraphHDDDB.readFile(GlobalConfig.hashDBPath);
		//		hddDB.doStatistics();

	}

	public static int minCountSlot = -1;
	public static void buildDatabases(){
		/////////////Building databases 
		Map<Integer, ArrayList<String>> slots = generateSlots(GlobalConfig.projectDataDir, GlobalConfig.slotDiv);
		FileUtils.writeObjectFile(slots, GlobalConfig.slotMapPath);
		Logger.log("slots: " + slots);
		try {
			org.apache.commons.io.FileUtils.forceDelete(new File(GlobalConfig.mainDummyDir));
			new File(GlobalConfig.mainDummyDir).mkdirs();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (Integer slotCount:slots.keySet()){
			if (slotCount<minCountSlot)
				continue;
			String slotStr = String.valueOf(slotCount);
			ArrayList<String> slotList = slots.get(slotCount);
			GlobalConfig.slotDummyDir = GlobalConfig.mainDummyDir + "slot" + slotStr + "/";
			(new File(GlobalConfig.slotDummyDir)).mkdirs();

			Logger.log("\r\n***********************\r\nslot: " + slotStr);

			processSlotProjects(slotList, slotStr);
		}
	}

	@SuppressWarnings("unchecked")
	public static void mergeWithHDDDB(){
		/////////////Merging databases using RamDB
		Logger.log("*************************\r\nMerging using hdd database");

		Map<Integer, ArrayList<String>> slotMap = 
				(Map<Integer, ArrayList<String>>) FileUtils.readObjectFile(GlobalConfig.slotMapPath);
		//Combine databases
		IncGraphHDDDB hddDB = new IncGraphHDDDB();
		if (GlobalConfig.isStartFromBeginning)
		{
			Logger.log("Clear old DB");
			hddDB.clearDB();
			new File(GlobalConfig.hashDBDir).mkdirs();
			hddDB.dbName = "";
		}
		else {
			if (new File(GlobalConfig.hashDBPath).exists())
			{
				IncGraphDB tmp = IncGraphHDDDB.readFile(GlobalConfig.hashDBPath);
				hddDB.wrapIncGraphDB(tmp);
			}
		}
		for (Integer slotCount:slotMap.keySet()){
			//			if (!GlobalConfig.isStartFromBeginning)
			{
				if (slotCount<GlobalConfig.startMergeSlotCount){
					continue;
				}
			}
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			System.out.println("Current date: " + dateFormat.format(date));		

			Logger.log("add slot " + slotCount + " to total database");

			String path = getDatabasePath(String.valueOf(slotCount));
			if (!(new File(path).exists()))
				continue;
			IncGraphDB slotDB = IncGraphDB.readFile(path);
			hddDB = GraphDBUtils.mergeHDDDatabase(hddDB, slotDB);
			Logger.log("cache 1 size: " + hddDB.cache1.keySet().size());
			Logger.log("cache 2 size: " + hddDB.cache2.keySet().size());
			slotDB.clearAll();
			System.gc();
			if ((slotCount%GlobalConfig.backupStep==0)&&(slotCount>0)){
				Logger.log("back up at: " + slotCount);
				hddDB.flushAllCache();
				hddDB.writeFile(GlobalConfig.hashDBPath);
			}
		}
		/**
		 * Very important for caching mechanism
		 */
		Logger.log("Flushing all cache items");
		hddDB.flushAllCache();

		hddDB.writeFile(GlobalConfig.hashDBPath);

		hddDB.doSimpleStatistics();
	}
	
	

	@SuppressWarnings("unchecked")
	public static void mergeWithHDDDBWithDisk(){
		/////////////Merging databases using RamDB
		Logger.log("*************************\r\nMerging using hdd database");

		Map<Integer, ArrayList<String>> slotMap = 
				(Map<Integer, ArrayList<String>>) FileUtils.readObjectFile(GlobalConfig.slotMapPath);
		//Combine databases
		IncGraphHDDDB hddDB = new IncGraphHDDDB();
		if (GlobalConfig.isStartFromBeginning)
		{
			Logger.log("Clear old DB");
			hddDB.clearDB();
			new File(GlobalConfig.hashDBDir).mkdirs();
			hddDB.dbName = "";
		}
		else {
			if (new File(GlobalConfig.hashDBPath).exists())
			{
				IncGraphDB tmp = IncGraphHDDDB.readFile(GlobalConfig.hashDBPath);
				hddDB.wrapIncGraphDB(tmp);
			}
		}
		for (Integer slotCount:slotMap.keySet()){
			//			if (!GlobalConfig.isStartFromBeginning)
			{
				if (slotCount<GlobalConfig.startMergeSlotCount){
					continue;
				}
				if (slotCount>GlobalConfig.endMergeSlotCount){
					continue;
				}
			}
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			System.out.println("Current date: " + dateFormat.format(date));		

			Logger.log("add slot " + slotCount + " to total database");

			String path = getDatabasePath(String.valueOf(slotCount));
			if (!(new File(path).exists()))
				continue;
			IncGraphDB slotDB = IncGraphDB.readFile(path);
			hddDB = GraphDBUtils.mergeHDDDatabase(hddDB, slotDB);
			Logger.log("cache 1 size: " + hddDB.cache1.keySet().size());
			Logger.log("cache 2 size: " + hddDB.cache2.keySet().size());
			slotDB.clearAll();
//			System.gc();
			if ((slotCount%GlobalConfig.backupStep==0)&&(slotCount>0)){
				Logger.log("back up at: " + slotCount);
				hddDB.flushAllCacheWithDisk();
				hddDB.writeFile(GlobalConfig.hashDBPath);
			}
		}
		/**
		 * Very important for caching mechanism
		 */
		Logger.log("Flushing all cache items");
		hddDB.flushAllCacheWithDisk();

		hddDB.writeFile(GlobalConfig.hashDBPath);

		hddDB.doSimpleStatistics();
	}


	//	@SuppressWarnings("unchecked")
	//	public static void patchMergeWithHDDDB(){
	//		/////////////Merging databases using RamDB
	//
	//		Logger.log("*************************\r\nPacth Merging using hdd database");
	//		Map<Integer, ArrayList<String>> slotMap = 
	//				(Map<Integer, ArrayList<String>>) FileUtils.readObjectFile(GlobalConfig.slotMapPath);
	//		//Combine databases
	//		IncGraphHDDDB hddDB = new IncGraphHDDDB();
	//		Logger.log("Clear old DB");
	//		hddDB.clearDB();
	//		hddDB.dbName = "";
	//
	//		for (Integer slotCount:slotMap.keySet()){
	//			Logger.log("add slot " + slotCount + " to overall database");
	//			String path = getDatabasePath(String.valueOf(slotCount));
	//			IncGraphDB slotDB = IncGraphDB.readFile(path);
	//			hddDB = GraphDBUtils.patchMergeHDDDatabase(hddDB, slotDB);
	//			System.gc();
	//
	//		}
	//
	//		hddDB.doStatistics();
	//	}

	@SuppressWarnings("unchecked")
	public static void mergeWithRamDB(){
		/////////////Merging databases using RamDB
		Logger.log("*************************\r\nMerging using ram database");

		Map<Integer, ArrayList<String>> slotMap = 
				(Map<Integer, ArrayList<String>>) FileUtils.readObjectFile(GlobalConfig.slotMapPath);
		//Combine databases
		IncGraphDB tmpDB = new IncGraphDB();
		tmpDB.dbName = "";

		for (Integer slotCount:slotMap.keySet()){
			Logger.log("add slot " + slotCount + " to total database");
			String path = getDatabasePath(String.valueOf(slotCount));
			IncGraphDB slotDB = IncGraphDB.readFile(path);
			tmpDB = GraphDBUtils.mergeDatabase(tmpDB, slotDB, true);
			System.gc();

		}

		tmpDB.doStatistics();
	}

	public static void test(){
		Logger.log("Loading IncGraph database...");
		IncGraphDB graphDB = IncGraphDB.readFile(GlobalConfig.graphDBPath);
		Logger.log("Doing statistics...");
		graphDB.doStatistics();
	}

	public static Map<Integer, ArrayList<String>> generateSlots(String parentDir, int div){
		File[] children = (new File(parentDir)).listFiles();

		ArrayList<File> dataFiles = new ArrayList<>();
		long totalSize = 0;

		for (File child:children){
			if (child.isFile()){
				if (child.getName().endsWith(GlobalConfig.dataExt)){
					dataFiles.add(child);
					totalSize += child.length();
				}
			}
		}

		Logger.log("totalSize: " + totalSize);
		long slotMaxSize = totalSize/div;

		Map<Integer, ArrayList<String>> slots = new TreeMap<Integer, ArrayList<String>>();
		ArrayList<String> tmp = new ArrayList<String>();		
		long totalSlotSize = 0;
		int slotCount = 0;
		for (int i=0; i<dataFiles.size(); i++){
			File dataFile = dataFiles.get(i);
			totalSlotSize += dataFile.length();
			String name = dataFile.getName().substring(0, dataFile.getName().lastIndexOf(GlobalConfig.dataExt));
			tmp.add(name);
			if ((i==dataFiles.size()-1)||(totalSlotSize + dataFiles.get(i+1).length()>slotMaxSize)){
				ArrayList<String> slotList = new ArrayList<>();
				slotList.addAll(tmp);
				totalSlotSize = 0;
				slots.put(slotCount, slotList);
				tmp.clear(); 
				slotCount++;
			}
		}
		return slots;
	}

	public static void processSlotProjects(List<String> projectList, String slotStr){
		incGraphDB.clearAll();
		incGraphDB = new IncGraphDB();
		numJavaProjects = 0;
		numClasses = 0;
		numMethods = 0;
		LOCs = 0;

		Logger.log(projectList);
		//		Logger.initDebugBis("AllDebug.txt");
		new File(GlobalConfig.slotDummyDir).mkdirs();
		try {
			Logger.log("Delete slot dir recursively");
			org.apache.commons.io.FileUtils.forceDelete(new File(GlobalConfig.slotDummyDir));
			Thread.sleep(500);
			(new File(GlobalConfig.slotDummyDir)).mkdirs();
		} catch (Exception e) {
			e.printStackTrace();
		}

		int countProject = 0;
		//FIXME: temporarily, I make a dummy directory for each project. I cannot re-use the dummy dir, because the tool cannot delete directory completely. There are issues from groum visitor
		for (String project:projectList){
			countProject++;
			GlobalConfig.dummyDir = GlobalConfig.slotDummyDir + countProject + "/";
			processProject(project);
		}

		Logger.log("\r\n**************************\r\nWriting graph database to hard drive");
		incGraphDB.numClasses = numClasses;
		incGraphDB.numMethods = numMethods;
		incGraphDB.dbName = slotStr;
		incGraphDB.LOCs = LOCs;

		incGraphDB.writeFile(getDatabasePath(slotStr));

		//		Logger.log("\r\n**************************\r\nDoing statistics");
		//		doStatistics();
		//		incGraphDB.doStatistics();

		//		Logger.closeDebugBis();
	}

	public static String getDatabasePath(String slotStr){
		String outputPath = GlobalConfig.graphDBPath.substring(0, GlobalConfig.graphDBPath.lastIndexOf(GlobalConfig.dataExt));
		return outputPath + slotStr + GlobalConfig.dataExt;
	}

	public static void processProject(String project){
		new File(GlobalConfig.dummyDir).mkdirs();
		synchronized (MainFunction.class) {
			new File(GlobalConfig.dummyDir).mkdirs();
			Logger.log("\r\nproject: " + project);
			//			Logger.logDebugBis("\r\nproject: " + project);
			Logger.log("\tcreating dummy dir");
			TreeMap<String, String> fileContentMap = SnapshotCreation.readData(GlobalConfig.projectDataDir, project);
			SnapshotCreation.buildDummyDir(GlobalConfig.dummyDir, fileContentMap);
			//			ImportVisitor importVisitor = new ImportVisitor();
			//			importVisitor.dirParsing(GlobalConfig.dummyDir);
			//
			//			Logger.log("all type list size: " + javaGroumVisitor.allTypeList.size());
			//			Logger.log("importStrSet size: " + importVisitor.importStrSet.size());
			//			Logger.log("importStrSet: " + importVisitor.importStrSet);
			//
			//			Logger.logDebugBis("all type list size: " + javaGroumVisitor.allTypeList.size());
			//			Logger.logDebugBis("importStrSet size: " + importVisitor.importStrSet.size());
			//			Logger.logDebugBis("importStrSet: " + importVisitor.importStrSet);
			//
			//
			/**
			 * Browse all methods and add their groums and subgroums to database
			 */
			Logger.log("\tbuilding groums");
			JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();
			javaGroumVisitor.doMain(GlobalConfig.dummyDir);


			Logger.log("\tadding groums to database");

			List<TypeInfo> allTypeList = javaGroumVisitor.allTypeList;
			//			int count = 0;
			if (allTypeList.size()>0)
				numJavaProjects++;
			for (TypeInfo typeInfo:allTypeList){
				numClasses++;
				List<MethodInfo> methodList = typeInfo.methodDecList;

				for (MethodInfo method:methodList){
					//					Logger.log(count);

					numMethods++;
					LOCs += method.LOCs;
					//					Logger.log(method.controlNodeList);
					incGraphDB.addGroumToDatabase(method, project);

					//					count++;
				}

			}	

		}
		try {
			Logger.log("\tDelete project dir recursively");
			//			FileUtils.deleteDirectoryContent(new File(GlobalConfig.dummyDir));
			org.apache.commons.io.FileUtils.forceDelete(new File(GlobalConfig.dummyDir));
			Thread.sleep(300);
			NodeSequenceInfoMap.clearAll();	

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void doStatistics(){
		//		Logger.log("Number of Java Projects: " + numJavaProjects);
		//		Logger.log("Number of Classes: " + numClasses);
		//		Logger.log("number of Methods: " + numMethods);
	}




	public static List<String> getProjectFromDirDat(String dirDatPath){
		List<String> projectList = new ArrayList<String>();
		File dirDat = new File(dirDatPath);
		if (dirDat.exists()){
			File[] subs = dirDat.listFiles();
			for (File sub:subs){
				String name = sub.getName();
				if (name.endsWith(".dat")){
					projectList.add(name.substring(0,name.length()-4));
				}
			}
		}
		return projectList;
	}

}
