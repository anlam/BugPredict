package main;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import utils.DumpToFileUtils;
import utils.NLPUtils;
import bug.BugReport;
import bug.BugReportReader;

public class ExtractBugReportFeature 
{
	static public LinkedHashMap<String, LinkedHashMap<Integer, Integer>> BugIDTermVectorDictionary = new LinkedHashMap<>();
	
	static public  LinkedHashMap<String, Integer> BugTermToIDDictionaryMap = new LinkedHashMap<>();
	static public LinkedHashMap<Integer, String> IDToBugTermDictionaryMap = new LinkedHashMap<>();
	static String projectBugPath="dataset/aspectj_bug.csv";
	public static List<BugReport> bugReports = new ArrayList<>();

	public static LinkedHashMap<String, Integer> bugTokenizer(BugReport bug)
	{
		LinkedHashMap<String, Integer> ret = new LinkedHashMap<String, Integer>();
		String bugcontent = bug.summary + " " + bug.description;
		List<String> StringTerms = FeatureExtraction.getAllNames(bugcontent.replaceAll("[^A-Za-z0-9]", " "));
		
		bugcontent = conncatToString(StringTerms, "");
		
		
		for(String name : NLPUtils.RemoveStopWordsAndStemmer(bugcontent))
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
		
		DumpToFileUtils.dumpNameCountMap("TestBugTerm/" + bug.bug_id+".txt", ret);
		
		return ret;
	}
	
	public static void extractBugReportsToVectors(String datapath)
	{
		bugReports =  BugReportReader.readAPI(datapath);
		for(BugReport bug : bugReports)
		{
			if(BugIDTermVectorDictionary.containsKey(bug.bug_id))
				continue;
			
			LinkedHashMap<String, Integer> bugTermCountMap =  bugTokenizer(bug);
			LinkedHashMap<Integer, Integer> bugTermIDCountMap = getFileNameCountMap(bugTermCountMap, BugTermToIDDictionaryMap, IDToBugTermDictionaryMap);
			BugIDTermVectorDictionary.put(bug.bug_id, bugTermIDCountMap);
		}
		
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
	

	
	private static String conncatToString(List<String> comments, String ret)
	{
		for(String cmt : comments)
		{
			ret = ret + " " + cmt;				
		}
		return ret;
	}
	

	
	public static void main(String[] args) 
	{
		extractBugReportsToVectors(projectBugPath);
		
	}

}
