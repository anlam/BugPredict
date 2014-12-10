/**
 * 
 */
package main;

import groumvisitors.JavaGroumVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import apidescription.APIDescription;
import apidescription.APIDescriptionReader;

import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import json.JSONInfo;
import json.JSONReader;
import data.MethodInfo;
import data.TypeInfo;
import dirtool.DirProcessing;
import repository.SnapshotCreation;
import utils.DumpToFileUtils;
import utils.FileUtils;
import utils.Logger;
import utils.NLPUtils;
import utils.PrintingUtils;

/**
 * @author Anh
 *
 */
public class FeatureExtraction {
	static String mainPath = "f:/Courses/AI class/TermProject/";
	static String dummyPath = mainPath + "Snapshots/";
	public static String repoListPath = mainPath + "repoList.txt";


	static LinkedHashMap<String, LinkedHashMap<String, Integer>> projectNameIdentifierCountMap = new LinkedHashMap<>();
	static LinkedHashMap<String, LinkedHashMap<String, Integer>> projectNameCommentCountMap = new LinkedHashMap<>();
	
	//static LinkedHashMap<String, LinkedHashMap<Integer, Integer>> fileNameCountMap = new LinkedHashMap<>();
	
	static LinkedHashMap<String, LinkedHashMap<Integer, Integer>> fileNameIdentifierCountMap = new LinkedHashMap<>();
	static LinkedHashMap<String, LinkedHashMap<Integer, Integer>> fileNameCommentCountMap = new LinkedHashMap<>();
	static LinkedHashMap<String, LinkedHashMap<Integer, Integer>> fileNameAPICountMap = new LinkedHashMap<>();
	static LinkedHashMap<String, LinkedHashMap<Integer, Integer>> fileNameAPIDesCountMap = new LinkedHashMap<>();
	
	static LinkedHashMap<String, Integer> identifierDictionaryIdMap = new LinkedHashMap<>();
	static LinkedHashMap<Integer, String> idIdenditiferDictionaryMap = new LinkedHashMap<>();
	
	static LinkedHashMap<String, Integer> commentDictionaryIdMap = new LinkedHashMap<>();
	static LinkedHashMap<Integer, String> idcommentDictionaryMap = new LinkedHashMap<>();
	
	
	static LinkedHashMap<String, Integer> APIDictionaryIdMap = new LinkedHashMap<>();
	static LinkedHashMap<Integer, String> idAPIDictionaryMap = new LinkedHashMap<>();
	
	static LinkedHashMap<String, Integer> APIDesDictionaryIdMap = new LinkedHashMap<>();
	static LinkedHashMap<Integer, String> idAPIDesDictionaryMap = new LinkedHashMap<>();
	
	
	static String projectSourcePath="dataset/org.aspectj";
	static String projectAPIDesPath="dataset/aspectj_api.csv";
	static String projectBugPath="dataset/aspectj_bug.csv";
	
	static int numMethods = 0;
	static int numFiles = 0;
	static long LOCs = 0;
	
	
	static int start =1;
	static int end = 1000;
	
	static int shortLen = 3;
	static int longLen = 50;
	
	static String csvPath = "features.csv";


	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		try{
//			File file = new File("ErrorLog.txt");
//			FileOutputStream fos = new FileOutputStream(file);
//			PrintStream ps = new PrintStream(fos);
//			System.setErr(ps);
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//
//		doMain();
//		
//		doMain2();
		doMainWeka();
	}

	public static void doMainWeka(){
		LinkedHashMap<String, LinkedHashMap<Integer, Integer>> projectIdxCountMap = 
				(LinkedHashMap<String, LinkedHashMap<Integer, Integer>>) FileUtils.readObjectFile("projectIdxCountMap_" + start + "_" + end +".dat");

		int maxIdx = 0;
		for(String project:projectIdxCountMap.keySet()){
			 LinkedHashMap<Integer, Integer> idxCountMap = projectIdxCountMap.get(project);
			 for (Integer idx:idxCountMap.keySet()){
				 if(idx>maxIdx)
					 maxIdx = idx; 
			 }
		}
		
		String jsonDataFilePath = "jsonList.dat";
		ArrayList<JSONInfo> jsonList = (ArrayList<JSONInfo>) FileUtils.readObjectFile(jsonDataFilePath );
		LinkedHashMap<String, ArrayList<Integer>> sparseProjectAudienceIdxMap = new LinkedHashMap<>();
		ArrayList<String> projectList = readProjectList(repoListPath);
		for (String project:projectList){
			sparseProjectAudienceIdxMap.put(project, null);
		}
		
		int count = 0;
		LinkedHashSet<String> allAudiences = new LinkedHashSet<>();
		LinkedHashMap<String, Integer> audienceIdxMap = new LinkedHashMap<>(); 
		LinkedHashMap<Integer, String> idxAudienceMap = new LinkedHashMap<>(); 

		for (JSONInfo json:jsonList){
//			Logger.log(json);
			String content = json.audience;
			String project = json.shortdesc;
			if (content==null){
				Logger.log(project);
				count++;
				content = json.audience;
			}
			
			{
				ArrayList<String> audiences = JSONReader.getAudiences(content);
				Logger.log(audiences);
				ArrayList<Integer> audienceIdxs = new ArrayList<>();
				allAudiences.addAll(audiences);
				for (String audience:audiences){
					if(!audienceIdxMap.containsKey(audience)){
						int idx = audienceIdxMap.size();
						audienceIdxMap.put(audience, idx);
						idxAudienceMap.put(idx, audience);
					}
					audienceIdxs.add(audienceIdxMap.get(audience));
				}
				sparseProjectAudienceIdxMap.put(project, audienceIdxs);
			}
		}

//		Logger.log(sparseProjectAudienceIdxMap.keySet());
		try {
			FileWriter fw = new FileWriter("Software.arff");
			fw.append("@RELATION software\r\n\r\n");
			for (int i=0; i<maxIdx;i++){
				fw.append("@ATTRIBUTE attr"+i+  " NUMERIC\r\n");
			}
			fw.append("@ATTRIBUTE class" + " {y,n}\r\n\r\n");
			
			fw.append("@DATA\r\n");
			for (String project:projectIdxCountMap.keySet()){
				LinkedHashMap<Integer, Integer> idxCountMap = projectIdxCountMap.get(project);
				for (int i=0; i<maxIdx; i++){
					int tmp = 0;
					if (idxCountMap.containsKey(i)){
						tmp = idxCountMap.get(i);
					}
					fw.append(tmp + ",");
				}
//				Logger.log(project);
				ArrayList<Integer> sparseAudience = sparseProjectAudienceIdxMap.get(project);
				boolean isContain = false;
				for (int idx:JSONReader.selectedIDs)
				{
					if (sparseAudience.contains(idx)){
						isContain = true;
						break;
					}
				}
				if (isContain){
					fw.append("y");
				}
				else {
					fw.append("n");
				}
				fw.append("\r\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void doMain2(){
		Logger.log("Load data");

		LinkedHashMap<String, LinkedHashMap<Integer, Integer>> projectIdxCountMap = 
				(LinkedHashMap<String, LinkedHashMap<Integer, Integer>>) FileUtils.readObjectFile("projectIdxCountMap_" + start + "_" + end +".dat");
		Logger.log("Find MaxIdx");
		int maxIdx = 0;
		for(String project:projectIdxCountMap.keySet()){
			 LinkedHashMap<Integer, Integer> idxCountMap = projectIdxCountMap.get(project);
			 for (Integer idx:idxCountMap.keySet()){
				 if(idx>maxIdx)
					 maxIdx = idx; 
			 }
		}
		
		Logger.log("Write csv file");

		
		writeCSV(csvPath, maxIdx, projectIdxCountMap);
	}
	
	public static void writeCSV(String csvPath, int maxIdx, 
			LinkedHashMap<String, LinkedHashMap<Integer, Integer>> projectIdxCountMap ){
		try{
			FileWriter fw = new FileWriter(csvPath);
			int count =0;
			for (String project:projectIdxCountMap.keySet()){
				count ++;
				System.out.print(count + "  ");
				if (count%50==0){
					System.out.println();
				}
				LinkedHashMap<Integer, Integer> idxCountMap = projectIdxCountMap.get(project);
				for (int i=0; i<maxIdx; i++){
					if (idxCountMap.containsKey(i)){
						fw.append(idxCountMap.get(i) + ",");
					}
					else {
						fw.append("0,");
					}
				}
				if (idxCountMap.containsKey(maxIdx)){
					fw.append(idxCountMap.get(maxIdx) + " ");
				}
				else {
					fw.append("0" + " ");
				}
				fw.append("\r\n");
			}
			
			fw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void doMain(){
		

		ArrayList<String> projectList = readProjectList(repoListPath);
		
		int count = 0;
		for (String project:projectList){
			
			count++;
			if (count<start||count>end){
				continue;
			}
			Logger.log(project);
			getCodeData(project);
			LinkedHashMap<String, Integer> nameCountMap =  doParsing(project);
			projectNameIdentifierCountMap.put(project, nameCountMap);
		}
//		PrintingUtils.debugMapByLine(projectNameCountMap, "projectNameCountMap.txt");

		LinkedHashMap<String, LinkedHashMap<Integer, Integer>> projectIdxCountMap = getProjectIdxCountMap(projectNameIdentifierCountMap);
//		PrintingUtils.debugMapByLine(projectIdxCountMap, "projectIdxCountMap.txt");
		FileUtils.writeObjectFile(projectIdxCountMap, "projectIdxCountMap_" + start + "_" + end +".dat");
		FileUtils.writeObjectFile(projectNameIdentifierCountMap, "projectNameCountMap_" + start + "_" + end +".dat");

		Logger.log("Files: " + numFiles);
		Logger.log("Methods: " + numMethods);
		Logger.log("LOCs: " + LOCs);
	}
	

	public static LinkedHashMap<String, LinkedHashMap<Integer, Integer>> getProjectIdxCountMap(
			LinkedHashMap<String, LinkedHashMap<String, Integer>> projectNameCountMap 
			){

		LinkedHashMap<String, LinkedHashMap<Integer, Integer>> projectIdxCountMap = new LinkedHashMap<>();
		LinkedHashMap<Integer,String> idxNameMap = new LinkedHashMap<Integer, String>();
		LinkedHashMap<String, Integer> nameIdxMap = new LinkedHashMap<String, Integer>();
		
		LinkedHashMap<String, LinkedHashSet<String>> nameProjectsMap = new LinkedHashMap<>();
		int maxCount = 0;
		for (String project:projectNameCountMap.keySet()){
			LinkedHashMap<String, Integer> nameCountMap = projectNameCountMap.get(project);
			for (String name:nameCountMap.keySet()){
				if (!nameProjectsMap.containsKey(name)){
					nameProjectsMap.put(name, new LinkedHashSet<String>());
				}
				nameProjectsMap.get(name).add(project);
				int count = nameCountMap.get(name);
				if (maxCount<count)
					maxCount = count;
			}
		}

		int projectSize = projectNameCountMap.size();
		for (String project:projectNameCountMap.keySet()){
			LinkedHashMap<String, Integer> nameCountMap = projectNameCountMap.get(project);
			LinkedHashMap<Integer, Integer> idxCountMap = new LinkedHashMap<>();
			
			int maxCount2 = 0;
			for (String name:nameCountMap.keySet()){
				if ((nameProjectsMap.get(name).size()>1)&&(nameProjectsMap.get(name).size()<projectSize))
				{
				
					if (nameCountMap.get(name)>maxCount2)
						maxCount2 = nameCountMap.get(name); 
				}
			}
			
			for (String name:nameCountMap.keySet()){
				if ((nameProjectsMap.get(name).size()>1)&&(nameProjectsMap.get(name).size()<projectSize))
				{
					if (!nameIdxMap.containsKey(name)){
						int idx = nameIdxMap.size();
						nameIdxMap.put(name, idx);
						idxNameMap.put(idx, name);
					}
					int normCount = nameCountMap.get(name)*255/maxCount2;
					//if (nameCountMap.get(name)>0){
					//	normCount = 1;
					//}
					
					idxCountMap.put(nameIdxMap.get(name), normCount);
				}
			}
			projectIdxCountMap.put(project, idxCountMap);
		}


		Logger.log("nameIdxMap.size(): " + nameIdxMap.size());
		PrintingUtils.debugMapByLine(nameIdxMap, "nameIdxMap.txt");

		return projectIdxCountMap;
	}

	//Extracting feature here:
	public static LinkedHashMap<String, Integer> doParsing(String project){
		LinkedHashMap<String, Integer> nameCountMap = new LinkedHashMap<>();
		String javaDirPath = dummyPath + project + "/";

		JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();
		javaGroumVisitor.dirParsing(javaDirPath);
		String[] javaSourceFileExt = new String[] { ".java" };

		List<File> javaFiles = DirProcessing.getFilteredRecursiveFiles(
				new File(javaDirPath), javaSourceFileExt);
		int totalMethods =0; 
		Logger.initDebug("DebugJavaGroum.txt");
		//		FileWriter sentenceWriter = new FileWriter(Configurations.javaSentencePath);

		for (File file : javaFiles) {
			String filePath = file.getAbsolutePath();
//			Logger.log("fileName: "
//					+ file.getAbsolutePath().replace('\\', '/'));
			javaGroumVisitor.getJavaCU(filePath, file);
			totalMethods+= javaGroumVisitor.countMethod;
//			Logger.log(javaGroumVisitor.typeList.size());
			javaGroumVisitor.allTypeList.addAll(javaGroumVisitor.typeList);
			numFiles ++;

		}

		ArrayList<TypeInfo> allTypeList = javaGroumVisitor.allTypeList;
		for (TypeInfo type:allTypeList){
			addNames(nameCountMap, type.typeName);
			for (MethodInfo method:type.methodDecList){
				numMethods++;
				addNames(nameCountMap, method.methodName);
				LOCs += getLOCsMethod(method);
			}
		}
		//		sentenceWriter.close();

		Logger.closeDebug();
		Logger.log("total Methods: " + totalMethods);

		return nameCountMap;

	}
	
	
	public static void doMyMain()
	{
		
	}
	
	
	//parsing each file in project
	public static void testParsing(String project, String codedir, String api_desPath)
	{
		//LinkedHashMap<String, LinkedHashMap<Integer, Integer>> projectFilenameCountMap = new LinkedHashMap<>();
		String javaDirPath = project + "/";
		JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();
		javaGroumVisitor.dirParsing(javaDirPath);
		
		String[] javaSourceFileExt = new String[] { ".java" };

		LinkedHashMap<String, APIDescription> apiDes = APIDescriptionReader.readAPIToMap(api_desPath);
		List<File> javaFiles = DirProcessing.getFilteredRecursiveFiles(
														new File(codedir), javaSourceFileExt);
		for(File file : javaFiles)
		{
			LinkedHashMap<String, Integer> nameCountMap = doFileParsing(file, javaGroumVisitor);
			LinkedHashMap<Integer, Integer> fileNameCountMap = getFileNameCountMap(nameCountMap, identifierDictionaryIdMap, idIdenditiferDictionaryMap);
			fileNameIdentifierCountMap.put(file.getAbsolutePath(), fileNameCountMap);
			
			LinkedHashMap<String, Integer> commentTermCountMap = getCommentTermMap(javaGroumVisitor);
			LinkedHashMap<Integer, Integer> fileNameCommentTermCountMap = getFileNameCountMap(commentTermCountMap, commentDictionaryIdMap, idcommentDictionaryMap);
			fileNameCommentCountMap.put(file.getAbsolutePath(), fileNameCommentTermCountMap);
			
			LinkedHashMap<String, Integer> APITermCountMap = getAPITermMap(javaGroumVisitor);
			LinkedHashMap<Integer, Integer> fileNameAPITermCountMap = getFileNameCountMap(APITermCountMap, APIDictionaryIdMap, idAPIDictionaryMap);
			fileNameAPICountMap.put(file.getAbsolutePath(), fileNameAPITermCountMap);
			
			LinkedHashMap<String, Integer> APIDesTermCountMap = getAPIDescriptionTerm(javaGroumVisitor, apiDes);
			LinkedHashMap<Integer, Integer> fileNameAPIDesTermCountMap = getFileNameCountMap(APIDesTermCountMap, APIDesDictionaryIdMap, idAPIDesDictionaryMap);
			fileNameAPIDesCountMap.put(file.getAbsolutePath(), fileNameAPIDesTermCountMap);
			
		}
		//return projectFilenameCountMap;
	}
	
	
	public static JavaGroumVisitor projectSourceParsing(String project)
	{
		String javaDirPath = project + "/";
		JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();
		javaGroumVisitor.dirParsing(javaDirPath);
		return javaGroumVisitor;
	}
	
	public static List<LinkedHashMap<Integer, Integer>> ExtractFileFeatures(File file, String filePath, 
																			JavaGroumVisitor javaGroumVisitor,
																			LinkedHashMap<String, APIDescription> apiDes)
	{
		List<LinkedHashMap<Integer, Integer>> ret = new ArrayList<>();
		LinkedHashMap<String, Integer> nameCountMap = doFileParsing(file, javaGroumVisitor);
		LinkedHashMap<Integer, Integer> fileNameCountMap = getFileNameCountMap(nameCountMap, identifierDictionaryIdMap, idIdenditiferDictionaryMap);
		fileNameIdentifierCountMap.put(filePath, fileNameCountMap);
		ret.add(fileNameCountMap);
		
		LinkedHashMap<String, Integer> commentTermCountMap = getCommentTermMap(javaGroumVisitor);
		LinkedHashMap<Integer, Integer> fileNameCommentTermCountMap = getFileNameCountMap(commentTermCountMap, commentDictionaryIdMap, idcommentDictionaryMap);
		fileNameCommentCountMap.put(filePath, fileNameCommentTermCountMap);
		ret.add(fileNameCommentTermCountMap);
		
		LinkedHashMap<String, Integer> APITermCountMap = getAPITermMap(javaGroumVisitor);
		LinkedHashMap<Integer, Integer> fileNameAPITermCountMap = getFileNameCountMap(APITermCountMap, APIDictionaryIdMap, idAPIDictionaryMap);
		fileNameAPICountMap.put(filePath, fileNameAPITermCountMap);
		ret.add(fileNameAPITermCountMap);
		
		LinkedHashMap<String, Integer> APIDesTermCountMap = getAPIDescriptionTerm(javaGroumVisitor, apiDes);
		LinkedHashMap<Integer, Integer> fileNameAPIDesTermCountMap = getFileNameCountMap(APIDesTermCountMap, APIDesDictionaryIdMap, idAPIDesDictionaryMap);
		fileNameAPIDesCountMap.put(filePath, fileNameAPIDesTermCountMap);
		ret.add(fileNameAPIDesTermCountMap);
		
		return ret;
	}
	
	//parsing each file in project
	public static void doProjectParsing(String project)
	{
		//LinkedHashMap<String, LinkedHashMap<Integer, Integer>> projectFilenameCountMap = new LinkedHashMap<>();
		String javaDirPath = project + "/";
		JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();
		javaGroumVisitor.dirParsing(javaDirPath);
		
		String[] javaSourceFileExt = new String[] { ".java" };

		List<File> javaFiles = DirProcessing.getFilteredRecursiveFiles(
														new File(javaDirPath), javaSourceFileExt);
		for(File file : javaFiles)
		{
			LinkedHashMap<String, Integer> nameCountMap = doFileParsing(file, javaGroumVisitor);
			LinkedHashMap<Integer, Integer> fileNameCountMap = getFileNameCountMap(nameCountMap, identifierDictionaryIdMap, idIdenditiferDictionaryMap);
			fileNameIdentifierCountMap.put(file.getAbsolutePath(), fileNameCountMap);
			
			LinkedHashMap<String, Integer> commentTermCountMap = getCommentTermMap(javaGroumVisitor);
			LinkedHashMap<Integer, Integer> fileNameCommentTermCountMap = getFileNameCountMap(commentTermCountMap, commentDictionaryIdMap, idcommentDictionaryMap);
			fileNameCommentCountMap.put(file.getAbsolutePath(), fileNameCommentTermCountMap);
			
		}
		//return projectFilenameCountMap;
	}
		
	
	//Updating dictionary
	private static LinkedHashMap<Integer, Integer> getFileNameCountMap(LinkedHashMap<String, Integer> nameCountMap, 
																	   LinkedHashMap<String, Integer> dictionaryIDMap,
																	   LinkedHashMap<Integer, String> IDdictionaryMap)
	{
		LinkedHashMap<Integer, Integer> fileNameCountMap = new LinkedHashMap<>();
		
		for(String name : nameCountMap.keySet())
		{
			int nameid = 0;
			if(dictionaryIDMap.containsKey(name))
			{
				nameid = dictionaryIDMap.get(name); 
			}
			else
			{
				nameid = dictionaryIDMap.size();
				dictionaryIDMap.put(name, nameid);
				IDdictionaryMap.put(nameid, name);
			}
			int count  = nameCountMap.get(name);
			fileNameCountMap.put(nameid, count);
		}
		
		return fileNameCountMap;
	}
	
		//Extracting feature of a file
		private static LinkedHashMap<String, Integer> doFileParsing(File file, JavaGroumVisitor javaGroumVisitor)
		{
			LinkedHashMap<String, Integer> nameCountMap = new LinkedHashMap<>();	
				String filePath = file.getAbsolutePath();
				javaGroumVisitor.getJavaCU(filePath, file);
				
				//dumpListToFile("id.txt", javaGroumVisitor.identifierList);
				//dumpListToFile("api.txt", javaGroumVisitor.API);
				
				//Counting the name
				for(String fullname : javaGroumVisitor.identifierList)
				{
					if(fullname != null && !fullname.isEmpty())
					{
						List<String> allName = getAllNames(fullname);
						if(!allName.contains(fullname))
							allName.add(fullname.trim());
						
						for(String name : allName)
						{
							
							if(nameCountMap.containsKey(name))
							{
								int count = nameCountMap.get(name);
								nameCountMap.put(name, count+1);
							}
							else
							{
								nameCountMap.put(name, 1);
							}
						}
					}								
				}			
			DumpToFileUtils.dumpNameCountMap("IdentifierTerm.txt", nameCountMap);
			return nameCountMap;

		}
		
		private static LinkedHashMap<String, Integer> getAPITermMap(JavaGroumVisitor javaGroumVisitor)
		{
			LinkedHashMap<String, Integer> ret = new LinkedHashMap<>();	
			
			for(String fullname : javaGroumVisitor.API)
			{
				if(fullname != null && !fullname.isEmpty())
				{
					List<String> allName = getAllNames(fullname);
					if(!allName.contains(fullname))
						allName.add(fullname.trim());
					
					for(String name : allName)
					{
						
						if(ret.containsKey(name))
						{
							int count = ret.get(name);
							ret.put(name, count+1);
						}
						else
						{
							ret.put(name, 1);
						}
					}
				}								
			}
			
			DumpToFileUtils.dumpNameCountMap("APITerm.txt", ret);
			return ret;
		}
		
		private static LinkedHashMap<String, Integer> getCommentTermMap(JavaGroumVisitor javaGroumVisitor)
		{
			LinkedHashMap<String, Integer> ret = new LinkedHashMap<>();	
			List<String> commentTerms = extractCommentToTerms(javaGroumVisitor.commentList);
			String comment = normalizeComment(commentTerms, "");
			
			List<String> StringTerms = extractCommentToTerms(javaGroumVisitor.stringLiteralList);
			
			comment = normalizeComment(StringTerms, comment);
			
			
			//remove stopword and stemming
			/*String regex = "[a-zA-Z]+|[0-9]+|\\S";
			 TokenizerFactory tf = new RegExTokenizerFactory(regex);
		     tf = new LowerCaseTokenizerFactory(tf);
		     tf = new EnglishStopTokenizerFactory(tf);
		     tf  = new PorterStemmerTokenizerFactory(tf);
		     char[] cs = comment.toCharArray();
		     Tokenizer tokenizer = tf.tokenizer(cs,0,cs.length);*/
			for(String name : NLPUtils.RemoveStopWordsAndStemmer(comment))
			{
				if(ret.containsKey(name))
				{
					int count = ret.get(name);
					ret.put(name, count+1);
				}
				else
				{
					ret.put(name, 1);
				}
			}
			
			
			/*for(String name : StringTerms)
			{
				if(ret.containsKey(name))
				{
					int count = ret.get(name);
					ret.put(name, count+1);
				}
				else
				{
					ret.put(name, 1);
				}
			}*/
			
			DumpToFileUtils.dumpNameCountMap("commentTerm.txt", ret);
			
			return ret;
		}
		
		
		/*private static String[] RemoveStopWordsAndStemmer(String sentences)
		{
			 String regex = "[a-zA-Z]+|[0-9]+|\\S";
			 TokenizerFactory tf = new RegExTokenizerFactory(regex);
		     tf = new LowerCaseTokenizerFactory(tf);
		     tf = new EnglishStopTokenizerFactory(tf);
		     tf  = new PorterStemmerTokenizerFactory(tf);
		     char[] cs = sentences.toCharArray();
		     Tokenizer tokenizer = tf.tokenizer(cs,0,cs.length);
		     return tokenizer.tokenize();
		}*/
		
		private static String normalizeComment(List<String> comments, String ret)
		{
			for(String cmt : comments)
			{
				ret = ret + " " + cmt;				
			}
			return ret;
		}
		
		private static List<String> extractCommentToTerms(List<String> comments)
		{
			List<String> ret = new ArrayList<>();
			
			for(String cmt : comments)
			{
				//value.replaceAll("[^A-Za-z0-9]", "")
				//System.out.println(cmt.replaceAll("[^A-Za-z0-9]", " "));
				List<String> terms = getAllNames(cmt.replaceAll("[^A-Za-z0-9]", " "));
				ret.addAll(terms);
			}
			
			
			
			return ret;
		}
		
		
		public static LinkedHashMap<String, Integer> getAPIDescriptionTerm(JavaGroumVisitor javaGroumVisitor, LinkedHashMap<String, APIDescription> apiDes)
		{
			LinkedHashMap<String, Integer> ret = new LinkedHashMap<>();	
			List<String> typeRef = javaGroumVisitor.typeReferenceList;
			
			String typeDes = "";
			for(String type : typeRef)
			{
				if(apiDes.containsKey(type))
				{
					//System.out.println("found type: " + type);
					typeDes = typeDes + " " + apiDes.get(type).description;
				}
			}
						
			List<String> StringTerms = getAllNames(typeDes.replaceAll("[^A-Za-z0-9]", " "));
			
			typeDes = normalizeComment(StringTerms, "");
			
			
			for(String name : NLPUtils.RemoveStopWordsAndStemmer(typeDes))
			{
				if(ret.containsKey(name))
				{
					int count = ret.get(name);
					ret.put(name, count+1);
				}
				else
				{
					ret.put(name, 1);
				}
			}
			
			DumpToFileUtils.dumpNameCountMap("APIDesTerm.txt", ret);
			
			return ret;
		}
		
		

	
	public static long getLOCsMethod (MethodInfo method){
		long LOCs = 0;
		String tmp = method.content;
		try {
			Scanner sc = new Scanner (tmp);
			
			while (sc.hasNextLine()){
				String tmpLine = sc.nextLine();
				if (tmpLine.trim().length()>0)
					LOCs++;
			}
			sc.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return LOCs; 
	}
	
	static void addTypeName_Test(LinkedHashMap<String,Integer> nameCountMap, String fullName)
	{
		if(nameCountMap.containsKey(fullName))
		{
			int count = nameCountMap.get(fullName);
			nameCountMap.put(fullName, count+1);
		}
		else
		{
			nameCountMap.put(fullName, 1);
		}
	}
	
	static void addNames(LinkedHashMap<String,Integer> nameCountMap, String fullName){
		ArrayList<String> allNames = getAllNames(fullName);
		for (String name:allNames){
			if (nameCountMap.containsKey(name)){
				int count = nameCountMap.get(name);
				nameCountMap.put(name, count+1);
			}
			else{
				//An Debug
				//System.out.println(name);
				nameCountMap.put(name, 1);
			}
		}
	}

	public static ArrayList<String> getAllNames(String fullName){
		ArrayList<String> allNames = new ArrayList<>();

		if (fullName==null)
			return allNames; 
//		allNames.add(fullName.toLowerCase());
		String tmp = fullName.replace(".", " ").replace("_", " ");
		String splitName = splitCamelCase(tmp);
		try{

			Scanner sc = new Scanner(splitName);
			while (sc.hasNext()){
				String tmp2 = sc.next().trim();
				if ((tmp2.length()>=shortLen)&&(tmp2.length()<=longLen))
					if (!isAllNumber(tmp2))
						allNames.add(tmp2.toLowerCase());
			}
			sc.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return allNames;
	}
	
	static boolean isAllNumber(String tmp){
		String regex = "\\d+";
		return tmp.matches(regex);
	}
	
	static boolean isAllNonWord (String tmp)
	{
		String regex = ".*\\W+.*";
		return tmp.matches(regex);
	}
	
	static String splitCamelCase(String s) {
		return s.replaceAll(
				String.format("%s|%s|%s",
						"(?<=[A-Z])(?=[A-Z][a-z])",
						"(?<=[^A-Z])(?=[A-Z])",
						"(?<=[A-Za-z])(?=[^A-Za-z])"
						),
						" "
				);
	}

	public static ArrayList<String> readProjectList(String repoListPath){
		ArrayList<String> projectList = new ArrayList<>();
		try{
			Scanner sc = new Scanner(new File(repoListPath));
			while (sc.hasNextLine()){
				projectList.add(sc.nextLine().trim());
			}

			sc.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return projectList;
	}

	public static void getCodeData(String project){
		String projectDataPath = mainPath + "project_data/";
		Map<String, String> fileCodeMap = SnapshotCreation.readData(projectDataPath, project);

		createSnapshop(dummyPath, project, fileCodeMap);
	}



	public static void createSnapshop(String path, String projectName, Map<String, String> fileCodeMap){
		String projectPath = path + projectName +"/";
		new File(projectPath).mkdirs();
		for (String fileName:fileCodeMap.keySet()){
			String content = fileCodeMap.get(fileName);
			String filePath = projectPath + fileName;
			(new File(filePath)).getParentFile().mkdirs();
			try{
				FileWriter fw = new FileWriter(filePath);
				fw.append(content);
				fw.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}
