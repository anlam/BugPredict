/**
 * 
 */
package json;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import main.FeatureExtraction;
import utils.FileUtils;
import utils.Logger;
import utils.PrintingUtils;

import com.eclipsesource.json.JsonObject;

/**
 * @author Anh
 *
 */
public class JSONReader {
	static String jsonMainDir = "F:/Courses/AI class/TermProject/json/json/";
	static ArrayList<JSONInfo> jsonList = new ArrayList<>();
	
	static LinkedHashSet<String> allAudiences = new LinkedHashSet<>();
	static LinkedHashMap<String, Integer> audienceIdxMap = new LinkedHashMap<>(); 
	static LinkedHashMap<Integer, String> idxAudienceMap = new LinkedHashMap<>(); 

	static String jsonDataFilePath = "jsonList.dat";
	static String csvPath = "class.csv";
	static String csvBinPath = "classBin.csv";
	
//	public static int selectValue = 37;

//	public static int[] selectedIDs = {37, 27, 69, 161, 234, 260, } ;
	public static int[] selectedIDs = {5};
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		doMain();
		doMain2();
		
	}

	public static void doMain2(){
		
		jsonList = (ArrayList<JSONInfo>) FileUtils.readObjectFile(jsonDataFilePath );
		LinkedHashMap<String, ArrayList<Integer>> sparseProjectAudienceIdxMap = new LinkedHashMap<>();
		ArrayList<String> projectList = readProjectList(FeatureExtraction.repoListPath);
		for (String project:projectList){
			sparseProjectAudienceIdxMap.put(project, null);
		}
		
		Logger.log("jsonList size:" + jsonList.size());
		int count = 0;
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
				ArrayList<String> audiences = getAudiences(content);
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
//		Logger.log(sparseProjectIdxMap);
		
		int idxSize = audienceIdxMap.size();
		LinkedHashMap<String, ArrayList<Integer>> projectAudienceIdxMap = new LinkedHashMap<>();
		
		for (String project:sparseProjectAudienceIdxMap.keySet()){
			
			ArrayList<Integer> sparseIdxList = sparseProjectAudienceIdxMap.get(project);
//			if (sparseIdxList==null)
//				continue;
			Logger.log("project: " + project);
			Logger.log("sparseIdxList: " + sparseIdxList);
			ArrayList<Integer> idxList = new ArrayList<>(idxSize);
			for (int i=0; i< idxSize;i++){
				if (sparseIdxList.contains(i))
					idxList.add(1);
				else
					idxList.add(0);
			}
			projectAudienceIdxMap.put(project, idxList);
		}
	
		Logger.log("projectAudienceIdxMap:");
		for (String project:projectAudienceIdxMap.keySet()){
			Logger.log(project + " = " + projectAudienceIdxMap.get(project));
		}
		Logger.log(count);
		
		Logger.log("audienceIdxMap: " + audienceIdxMap);
		
		TreeMap<Integer, Integer> idxCountMap = new TreeMap<Integer, Integer>();
		
		for (String project:projectAudienceIdxMap.keySet()){
			ArrayList<Integer> audienceIdxMap = projectAudienceIdxMap.get(project);
			for (int i=0; i<audienceIdxMap.size(); i++){
				if (audienceIdxMap.get(i)>0){
					if (!idxCountMap.containsKey(i)){
						idxCountMap.put(i, 1);
					}
					else{
						int count2 = idxCountMap.get(i);
						idxCountMap.put(i, count2+1);
					}
				}
			}
		}
		
		Logger.log("idxCountMap: " + idxCountMap);
		
		TreeMap<Integer, ArrayList<Integer>> countIdxMap = new TreeMap<>();
		for (int idx:idxCountMap.keySet()){
			int count2 = idxCountMap.get(idx);
			if (!countIdxMap.containsKey(count2)){
				countIdxMap.put(count2, new ArrayList<Integer>());
			}
			countIdxMap.get(count2).add(idx);
		}
		Logger.log("countIdxMap: " + countIdxMap);
		
		LinkedHashSet<Integer> overall = new LinkedHashSet<>();
		for (int count2:countIdxMap.keySet()){
			overall.addAll(countIdxMap.get(count2));
		}

		Logger.log("overall: " + overall);

		LinkedHashMap<String, ArrayList<Integer>> newProjectAudienceIdxMap = new LinkedHashMap<>();
		
		for (String project:projectAudienceIdxMap.keySet()){
			ArrayList<Integer> audienceIdxs = projectAudienceIdxMap.get(project);
			ArrayList<Integer> newAudienceIdxs = new ArrayList<>();
			for (int i=0; i<audienceIdxs.size();i++){
				newAudienceIdxs.add(0);
			}
			for (Integer idx:overall){
				if (audienceIdxs.get(idx)>0){
//					Logger.log(idx);
					newAudienceIdxs.set(idx, 1);
					break;
				}
			}
			
			newProjectAudienceIdxMap.put(project, newAudienceIdxs);
		}
		

//		writeCSV(csvPath, newProjectAudienceIdxMap);
		writeCSV(csvPath, projectAudienceIdxMap);
		writeCSVBin(csvBinPath, projectAudienceIdxMap);
	}
	
	public static void writeCSVBin(String csvPath, LinkedHashMap<String, ArrayList<Integer>> projectIdxMap ){
		try{
			FileWriter fw = new FileWriter(csvPath);
			for (String project:projectIdxMap.keySet()){
				ArrayList<Integer> idxList = projectIdxMap.get(project);
				if (isContainSelectIDs(idxList))
				{
					fw.append("1,0");
				}
				else
				{
					fw.append("0,1");
				}
				fw.append(System.lineSeparator());
			}
			
			fw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static boolean isContainSelectIDs(ArrayList<Integer> idxList){
		for (int selectIdx:selectedIDs){
			if (idxList.get(selectIdx)>0)
					return true;
		}
		
		return false;
	}
	
	public static void writeCSV(String csvPath, LinkedHashMap<String, ArrayList<Integer>> projectIdxMap ){
		try{
			FileWriter fw = new FileWriter(csvPath);
			for (String project:projectIdxMap.keySet()){
				ArrayList<Integer> idxList = projectIdxMap.get(project);
				for (int i=0; i<idxList.size()-1;i++){
					fw.append(idxList.get(i) + ", ");
				}
				fw.append(idxList.get(idxList.size()-1) +System.lineSeparator());
			}
			
			fw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static ArrayList<String> getAudiences(String audienceStr){
		String tmp = audienceStr.substring(1, audienceStr.length()-1);
		String[] splits =tmp.split("\",\"");
		ArrayList<String> audiences = new ArrayList<>(); 
		for (String split:splits){
			String tmp1  = split.replace("\"", "");
			audiences.add(tmp1);
				
		}
		return audiences;
	}
	
	
	public static void doMain(){
		ArrayList<String> projectList = readProjectList(FeatureExtraction.repoListPath);
		Logger.log("projectList size: " + projectList.size());
		
		File[] jsonFileList = getJSONList(jsonMainDir);
		Logger.log(jsonFileList.length);
		
		int count =0;
		
		for (File file:jsonFileList){
			if (file.isDirectory())
				continue;
			count++;
			if (count%1000==0){
				System.out.print(count + "  ");
			}
			
			JSONInfo json = readFile(file);
			if (json!=null)
			{
				if (projectList.contains(json.shortdesc))
				{
					System.out.print("found:" + json.shortdesc + "  ");
					jsonList.add(json);
				}
			}
			
//			Logger.log(json);
		}
		Logger.log("jsonList size: " + jsonList.size());
		PrintingUtils.debugCollectionByLine(jsonList, "jsonList.txt");
		FileUtils.writeObjectFile(jsonList, jsonDataFilePath);
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
	
	
	public static File[] getJSONList(String jsonMainDir){
		return new File(jsonMainDir).listFiles();
		
	}
	
	public static JSONInfo readFile(File file){
		JSONInfo json = null;
		try{
			Scanner sc = new Scanner(file);
			StringBuilder sb = new StringBuilder();
			while (sc.hasNextLine()){
				sb.append(sc.nextLine() + System.lineSeparator());
			}
			JsonObject jsonObject = JsonObject.readFrom(sb.toString());
//			Logger.log(.get("name"));
			sc.close();
			JsonObject project = jsonObject.get("Project").asObject();
			String name= project.get("name").asString();
			String shortdesc= project.get("shortdesc").asString();
			String desc =  project.get("description").asString();
			String status =  project.get("status").asString();
			
			String topics = null;
			if(project.names().contains("topics"))
				 topics = project.get("topics").asArray().toString();
			
			String language = null;
			if(project.names().contains("programming-languages"))
			 language = project.get("programming-languages").asArray().toString();
			
			String audience = null;
			if(project.names().contains("audiences"))
				 audience = project.get("audiences").asArray().toString();
			String categories =  project.get("categories").asArray().toString();
			json = new JSONInfo(name, shortdesc, desc, status, topics, language, audience, categories);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return json;
	}

}
