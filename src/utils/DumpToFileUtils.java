package utils;

import java.util.LinkedHashMap;
import java.util.List;

public class DumpToFileUtils 
{
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
	
	
	public static void dumpListToFile(String filePath, List<String> list)
	{
		Logger.initDebug(filePath);
		for(String name: list)
		{		
			//System.out.println(name + ":= " + nameCountMap.get(name));
			Logger.logDebug(name);
		}
		Logger.closeDebug();
	}

}
