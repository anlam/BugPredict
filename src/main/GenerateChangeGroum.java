/**
 * 
 */
package main;

import graphdata.DataUtils;
import graphdata.IncGraph;
import graphdata.IncGraphDB;
import graphdata.NewIncGraphHDDDB;
import groumvisitors.JavaGroumVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import repository.RepoListProcessing;
import repository.SVNConnector;
import config.ChangeConfig;
import config.GlobalConfig;
import utils.FileUtils;
import utils.Logger;
import data.DiffPairMethodInfo;
import data.DiffPairsExtract;
import data.MethodInfo;
import data.SVNHistoryData;
import data.SVNRevData;
import data.TypeInfo;

/**
 * @author Anh
 *
 */
public class GenerateChangeGroum {

	static String databasePath = GlobalConfig.mainDir + "Storage/db_0_51_big/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		if(args.length>0){




			Logger.log("Loading graph database");
			
			String tmp = new File(args[0]).getAbsolutePath();
			GlobalConfig.mainDir  = tmp +"/";
			GlobalConfig.repoFilePath = GlobalConfig.mainDir + args[2];
			databasePath = GlobalConfig.mainDir + args[1];
			GlobalConfig.refreshParams();

			ChangeConfig.mainPath = GlobalConfig.mainDir + "change/";
			String revCodePath =  ChangeConfig.changePath + args[3] ;
			ChangeConfig.refreshParams();
			if (GlobalConfig.isRedirectErrMsg){
				try {
					System.setErr(new PrintStream(GlobalConfig.logGroumGenPath));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			NewIncGraphHDDDB graphDB = new NewIncGraphHDDDB(databasePath, true, true, GlobalConfig.maxUnCompSize);
			graphDB.doSimpleStatistics();

			String outputDataPath =ChangeConfig.changePath +"/diff_data/";
			List<String> repoList = RepoListProcessing.getRepoListFromFile(GlobalConfig.repoFilePath);
			for (String projectName:repoList)
			{
				Logger.log("\r\n**********************\r\nProject: " + projectName);
				ChangeConfig.SVNHistoryPath = revCodePath + projectName +"_SVNHistory.dat";

				doMainProject(projectName, outputDataPath, graphDB);
			}
		}
		else {

			if (GlobalConfig.isRedirectErrMsg){
				try {
					System.setErr(new PrintStream(GlobalConfig.logGroumGenPath));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			String projectName = "a2gameserver";

			Logger.log("\r\n**********************\r\nProject: " + projectName);
			String outputDataPath = ChangeConfig.changePath +"/diff_data/";
			ChangeConfig.SVNHistoryPath = ChangeConfig.changePath + "rev_code/"  + projectName +"_SVNHistory.dat";

			Logger.log("Loading graph database");

			NewIncGraphHDDDB graphDB = new NewIncGraphHDDDB(databasePath, true, true, GlobalConfig.maxUnCompSize);

			Logger.log(databasePath);
			graphDB.doSimpleStatistics();

			doMainProject(projectName, outputDataPath, graphDB);	
		}

	}

	
	
	public static void doMainProject(String projectName, String outputDataPath, IncGraphDB graphDB){
		String outputDiffPairFilePath = outputDataPath + projectName +ChangeConfig.diffPostFix + ChangeConfig.datExt;
		if (! (new File(outputDiffPairFilePath)).exists())
			return;
		if (! (new File(ChangeConfig.SVNHistoryPath)).exists())
			return;
		DiffPairsExtract diffPairsExtract = DiffPairsExtract.readData(outputDiffPairFilePath);
		//		Logger.log(diffPairsExtract.diffPairMethodListMap.keySet());


		SVNHistoryData svnHistory = SVNHistoryData.readDataFile(ChangeConfig.SVNHistoryPath); 
		//		Logger.log(svnHistory.SVNRevDataMap.keySet());

		String hostDir = ChangeConfig.revMainDummyDir + projectName + "/";
		new File(hostDir).mkdirs();

		Logger.log("\tExtracting Groums");
		LinkedHashMap<Long, ArrayList<MethodInfo>> revGroumsMap = doExtractGroums(projectName, diffPairsExtract, svnHistory, hostDir);


		Logger.log("\tProcessing Groums");
		LinkedHashMap<Long, ArrayList<IncGraph>> revIncGraphsMap = processGroum(revGroumsMap,graphDB);


		Logger.log("\r\n\tWriting Groums data");
		new File(ChangeConfig.fixingGroumPath).mkdirs();
		String groumPath = ChangeConfig.fixingGroumPath + projectName + "_groums.dat";
		FileUtils.writeSnappyStreamObjectFile(revIncGraphsMap, groumPath);

		try {
			FileUtils.deleteRecursive(new File(hostDir));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	public static LinkedHashMap<Long, ArrayList<IncGraph>> processGroum(LinkedHashMap<Long, ArrayList<MethodInfo>> revGroumsMap,  IncGraphDB graphDB){
		System.out.print("\trev: ");
		LinkedHashMap<Long, ArrayList<IncGraph>> revIncGraphsMap = new LinkedHashMap<>();
		for (Long rev:revGroumsMap.keySet()){
			System.out.print(rev + " ");
			ArrayList<MethodInfo> revGroumList = revGroumsMap.get(rev);
			for (MethodInfo methodInfo:revGroumList){
				DataUtils.compactGroum(methodInfo);
				IncGraph graph = DataUtils.convertGroumToGraph(methodInfo, graphDB);
				ArrayList<IncGraph> allSubGraphs = graph.getAllSubgraphs();
				if (!revIncGraphsMap.containsKey(rev))
					revIncGraphsMap.put(rev, new ArrayList<IncGraph>());
				revIncGraphsMap.get(rev).addAll(allSubGraphs);
			}
		}
		return revIncGraphsMap;
	}

	public static LinkedHashMap<Long, ArrayList<MethodInfo>> 
	doExtractGroums(String project, DiffPairsExtract diffPairsExtract, SVNHistoryData svnHistory, String hostDir){

		LinkedHashMap<Long, ArrayList<MethodInfo>> revGroumsMap = new LinkedHashMap<>();

		LinkedHashMap<Long, ArrayList<DiffPairMethodInfo>> diffPairMethodListMap = diffPairsExtract.diffPairMethodListMap;
		//		Logger.log("hostDir: " + hostDir);
		//		System.out.print("edf:" + diffPairMethodListMap.size() + " ");
		//		for(Long rev:diffPairMethodListMap.keySet()){
		//			System.out.println(diffPairMethodListMap.get(rev).size() + " ");
		//
		//		}

		TreeMap<Long, SVNRevData> SVNRevDataMap = svnHistory.SVNRevDataMap;
		long lastRevision = 1;
		System.out.print("\trev: ");
		//		long startRev = 0;
		for (long revision:SVNRevDataMap.keySet()){


			if (!diffPairMethodListMap.containsKey(revision))
				continue;
			ArrayList<DiffPairMethodInfo> diffFilePairMethodList = diffPairMethodListMap.get(revision);

			if (diffFilePairMethodList.size()<=0)
				continue;

			String log = diffPairsExtract.revLogMap.get(revision);

			//Check if a revision is fixing commit via its log message 
			if (!SVNConnector.isFixingCommit(log))
			{
				continue;
			}

			//			if (revision>=startRev)
			{
				//				startRev+= 100;
				System.out.print(revision + " ");
				//				System.out.print("\r\n" + revision + " ");
				//				if (log!=null)
				//					System.out.print("\r\n" + log.replaceAll("\\s", " ") +"\r\n");
			}

			createDummyProject(svnHistory, lastRevision-1, revision-1, hostDir);

			lastRevision = revision;
			if (diffPairMethodListMap.containsKey(revision)){
//				System.out.print("df:" + diffFilePairMethodList.size() + " ");

				LinkedHashSet<String> tobeParsedPaths = getAllFilePaths(diffFilePairMethodList);
				//				System.out.print(" " + tobeParsedPaths );
				JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();
				LinkedHashMap<String, ArrayList<TypeInfo>> filePathTypeListMap = javaGroumVisitor.
						doMainParseFileList(hostDir, tobeParsedPaths);
//				System.out.println("hostDir: " + hostDir );

//				System.out.println("filePathTypeListMap: " + filePathTypeListMap );

				LinkedHashMap<String, ArrayList<DiffPairMethodInfo>> filePathDiffMap = getFilePathDiffMap(diffFilePairMethodList);
				//				System.out.print(" " + filePathDiffMap );

				for (String filePath:filePathDiffMap.keySet()){
//					System.out.print("\r\nfilePath: " + filePath);
					ArrayList<DiffPairMethodInfo>  diffPairMethodList = filePathDiffMap.get(filePath);
//					System.out.println(diffPairMethodList);
					ArrayList<TypeInfo> typeList = filePathTypeListMap.get(filePath);
//					System.out.println(typeList);

					ArrayList<MethodInfo> methodGroums = getMethodGroums(diffPairMethodList, typeList);
//					System.out.print(" " + methodGroums.size() + " ");
					//					if (methodGroums.size()==0){
					//						System.out.println("filePath: " + filePath);
					//						System.out.println("filePathDiffMap: " + filePathDiffMap);
					//						System.out.println("diffPairMethodList: " + diffPairMethodList);
					//						System.out.println("filePathTypeListMap: " + filePathTypeListMap);
					//						System.exit(0);
					//					}
					if (!revGroumsMap.containsKey(revision)){
						revGroumsMap.put(revision, new ArrayList<MethodInfo>());
					}
					revGroumsMap.get(revision).addAll(methodGroums);
				}
			}

		}

		System.out.println();
		return revGroumsMap;
	}

	public static ArrayList<MethodInfo> getMethodGroums(ArrayList<DiffPairMethodInfo>  diffPairMethodList, ArrayList<TypeInfo> typeList ){
		ArrayList<MethodInfo> methodGroums = new ArrayList<>();
		LinkedHashMap<String, ArrayList<MethodInfo>> typeMethodGroumMap = getTypeMethodGroumMap(typeList);

		LinkedHashMap<String, ArrayList<DiffPairMethodInfo>> diffTypeMethodMap = getDiffTypeMethodMap(diffPairMethodList);
		for (String type:diffTypeMethodMap.keySet()){
			if (typeMethodGroumMap.containsKey(type)){
				//				System.out.println("type: " + type);

				ArrayList<MethodInfo> typeMethodGroums = typeMethodGroumMap.get(type);
				//				System.out.println("typeMethodGroums: " + typeMethodGroums);

				ArrayList<DiffPairMethodInfo> diffTypeMethods = diffTypeMethodMap.get(type);
				//				System.out.println("diffTypeMethods: " + diffTypeMethods);

				LinkedHashMap<String, ArrayList<MethodInfo>> methodMethodGroumMap = getMethodMethodGroumMap(typeMethodGroums);
				//				System.out.println("methodMethodGroumMap: " + methodMethodGroumMap);

				LinkedHashMap<String, ArrayList<DiffPairMethodInfo>> diffMethodMethodMap = getDiffMethodMethodMap(diffTypeMethods);
				//				System.out.println("diffMethodMethodMap: " + diffMethodMethodMap);

				for (String methodSig:diffMethodMethodMap.keySet()){
					if (methodMethodGroumMap.containsKey(methodSig))
						methodGroums.addAll(methodMethodGroumMap.get(methodSig));
				}
			}
		}
		return methodGroums;
	}



	public static LinkedHashMap<String, ArrayList<MethodInfo>> getMethodMethodGroumMap(ArrayList<MethodInfo> typeMethodGroums ){
		LinkedHashMap<String, ArrayList<MethodInfo>> methodMethodGroumMap = new LinkedHashMap<>();
		for(MethodInfo method:typeMethodGroums){
			String methodSig = method.methodName + method.getParamList();
			if (!methodMethodGroumMap.containsKey(methodSig)){
				methodMethodGroumMap.put(methodSig, new ArrayList<MethodInfo>());
			}
			methodMethodGroumMap.get(methodSig).add(method);
		}
		return methodMethodGroumMap;
	}
	public static LinkedHashMap<String, ArrayList<DiffPairMethodInfo>> getDiffMethodMethodMap(ArrayList<DiffPairMethodInfo> diffTypeMethods){
		LinkedHashMap<String, ArrayList<DiffPairMethodInfo>> diffMethodMethodMap = new LinkedHashMap<>();
		for (DiffPairMethodInfo diffPair:diffTypeMethods){
			String methodSig = diffPair.methodName + diffPair.paramTypeList.toString();
			if (!diffMethodMethodMap.containsKey(methodSig)){
				diffMethodMethodMap.put(methodSig, new ArrayList<DiffPairMethodInfo>());
			}
			diffMethodMethodMap.get(methodSig).add(diffPair);
		}
		return diffMethodMethodMap;
	}

	public static LinkedHashMap<String, ArrayList<MethodInfo>> getTypeMethodGroumMap( ArrayList<TypeInfo> typeList){
		LinkedHashMap<String, ArrayList<MethodInfo>> typeMethodGroumMap = new LinkedHashMap<>();
		if (typeList!=null)
			for (TypeInfo type:typeList){
				String className = getShortenType(type.typeName);
				if (!typeMethodGroumMap.containsKey(className))
					typeMethodGroumMap.put(className, new ArrayList<MethodInfo>());
				for (MethodInfo method:type.methodDecList){
					typeMethodGroumMap.get(className).add(method);
				}
			}

		return typeMethodGroumMap;
	}

	public static String getShortenType(String type){
		String tmp = type;
		if (tmp.contains("."))
			tmp = tmp.substring(tmp.lastIndexOf(".")+1);
		return tmp;
	}


	public static LinkedHashMap<String, ArrayList<DiffPairMethodInfo>> getDiffTypeMethodMap(ArrayList<DiffPairMethodInfo> diffPairMethodList){
		LinkedHashMap<String, ArrayList<DiffPairMethodInfo>> diffTypeMethodMap = new LinkedHashMap<>();
		for (DiffPairMethodInfo diffPair:diffPairMethodList){
			String className = diffPair.className;
			if (!diffTypeMethodMap.containsKey(className)){
				diffTypeMethodMap.put(className, new ArrayList<DiffPairMethodInfo>());
			}
			diffTypeMethodMap.get(className).add(diffPair);
		}
		return diffTypeMethodMap;
	}

	public static LinkedHashMap<String, ArrayList<DiffPairMethodInfo>> getFilePathDiffMap(ArrayList<DiffPairMethodInfo> diffPairMethodList){
		LinkedHashMap<String, ArrayList<DiffPairMethodInfo>> filePathDiffMap = new LinkedHashMap<>();
		for (DiffPairMethodInfo diffPair:diffPairMethodList){
			String filePath = diffPair.filePath;
			if (!filePathDiffMap.containsKey(filePath)){
				filePathDiffMap.put(filePath, new ArrayList<DiffPairMethodInfo>());
			}
			filePathDiffMap.get(filePath).add(diffPair);
		}
		return filePathDiffMap;
	}


	public static LinkedHashSet<String> getAllFilePaths(ArrayList<DiffPairMethodInfo> diffPairs){
		LinkedHashSet<String> filePaths = new LinkedHashSet<>();
		for (DiffPairMethodInfo diffPair:diffPairs){
			filePaths.add(diffPair.filePath);
		}
		return filePaths;
	}

	public static void createDummyProject(SVNHistoryData svnHistory, long startRevision, long toRevision,
			String hostDir){
		TreeMap<Long, SVNRevData> SVNRevDataMap = svnHistory.SVNRevDataMap;
		TreeSet<String> changeTypes = new TreeSet<>();

		for (Long revision:SVNRevDataMap.keySet()){
			if (revision < startRevision)
				continue;
			if (revision > toRevision){
				break;
			}
			//			Logger.log("rev: " + revision);
			LinkedHashMap<String, String> changedPathOldPath = SVNRevDataMap.get(revision).changedPathOldPath;
			LinkedHashMap<String, String> changedPathType = SVNRevDataMap.get(revision).changedPathType;
			LinkedHashMap<String, String> changedPathContentPrev = SVNRevDataMap.get(revision).changedPathContentPrev;
			LinkedHashMap<String, String> changedPathContentNext = SVNRevDataMap.get(revision).changedPathContentNext;

			for (String changedPath:changedPathType.keySet()){
				String changeType = changedPathType.get(changedPath);
				//				Logger.log(changedPath + "\t\t" + changeType + "\t\t" + changedPathOldPath.get(changedPath) );

				if (changeType.equals("A"))
				{
					createFile(changedPath, hostDir, changedPathContentNext.get(changedPath));
				}
				else if (changeType.equals("D"))
				{
					deleteFile(changedPath, hostDir);
				}
				else if (changeType.equals("M")){
					modifyFile(changedPath,hostDir, changedPathContentNext.get(changedPath));;
				}
				else if (changeType.equals("R")){

					replaceFile(changedPathOldPath.get(changedPath), changedPath, hostDir, changedPathContentNext.get(changedPath));
				}
				changeTypes.add(changeType);
			}
			//			Logger.log(SVNRevDataMap.get(revision).changedPathContentNext);
		}
	}
	public static void createFile(String path, String hostDirPath, String content){
		try{
			String combinePath = hostDirPath + path;
			File file = new File(combinePath);
			File parentDir = file.getParentFile();
			if (!parentDir.exists()){
				parentDir.mkdirs();
			}
			FileWriter fw = new  FileWriter(file) ;
			fw.append(content + System.lineSeparator());
			fw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void deleteFile(String path, String hostDirPath){
		try{
			String combinePath = hostDirPath + path;
			File file = new File(combinePath);
			if (file.exists()){
				file.delete();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void modifyFile(String path, String hostDirPath, String newContent){
		deleteFile(path, hostDirPath);
		createFile(path, hostDirPath, newContent);
	}

	public static void replaceFile(String oldPath, String newPath, String hostDirPath, String content){
		deleteFile(oldPath, hostDirPath);
		deleteFile(newPath, hostDirPath);
		createFile(newPath, hostDirPath, content);
	}

}
