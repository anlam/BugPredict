package main;

import java.util.LinkedHashMap;
import java.util.List;

import utils.Logger;

import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import bug.BugReport;
import bug.BugReportReader;

public class ExtractBugReportFeature 
{
	static LinkedHashMap<String, LinkedHashMap<Integer, Integer>> BugIDTermVectorDictionary = new LinkedHashMap<>();
	
	static LinkedHashMap<String, Integer> BugTermToIDDictionaryMap = new LinkedHashMap<>();
	static LinkedHashMap<Integer, String> IDToBugTermDictionaryMap = new LinkedHashMap<>();
	static String projectBugPath="dataset/aspectj_bug.csv";

	public static LinkedHashMap<String, Integer> bugTokenizer(BugReport bug)
	{
		LinkedHashMap<String, Integer> ret = new LinkedHashMap<String, Integer>();
		String bugcontent = bug.summary + " " + bug.description;
		List<String> StringTerms = FeatureExtraction.getAllNames(bugcontent.replaceAll("[^A-Za-z0-9]", " "));
		
		bugcontent = conncatToString(StringTerms, "");
		
		
		for(String name : RemoveStopWordsAndStemmer(bugcontent))
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
		
		dumpNameCountMap("TestBugTerm/" + bug.bug_id+".txt", ret);
		
		return ret;
	}
	
	public static void extractBugReportsToVectors(String datapath)
	{
		List<BugReport> bugreports =  BugReportReader.readAPI(datapath);
		for(BugReport bug : bugreports)
		{
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
	
	public static void dumpNameCountMap(String filePath, LinkedHashMap<String, Integer> nameCountMap)
	{
		Logger.initDebug(filePath);
		for(String name: nameCountMap.keySet())
		{		
			//System.out.println(name + ":= " + nameCountMap.get(name));
			Logger.logDebug(name + ":= " + nameCountMap.get(name));
		}
		Logger.closeDebug();
		
	}
	
	private static String conncatToString(List<String> comments, String ret)
	{
		for(String cmt : comments)
		{
			ret = ret + " " + cmt;				
		}
		return ret;
	}
	
	private static String[] RemoveStopWordsAndStemmer(String sentences)
	{
		 String regex = "[a-zA-Z]+|[0-9]+|\\S";
		 TokenizerFactory tf = new RegExTokenizerFactory(regex);
	     tf = new LowerCaseTokenizerFactory(tf);
	     tf = new EnglishStopTokenizerFactory(tf);
	     tf  = new PorterStemmerTokenizerFactory(tf);
	     char[] cs = sentences.toCharArray();
	     Tokenizer tokenizer = tf.tokenizer(cs,0,cs.length);
	     return tokenizer.tokenize();
	}
	
	public static void main(String[] args) 
	{
		extractBugReportsToVectors(projectBugPath);
		
	}

}
