/**
 * 
 */
package utils;

import java.io.FileWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author ANH
 *
 */
public class PrintingUtils {
	static String debugFilePath = "debug.txt";
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	
	public static void writeDebugStr(String str, String debugFilePath){
		try{
			FileWriter debugFW = new FileWriter(debugFilePath);
		
			debugFW.append(str);
						
			debugFW.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	@SuppressWarnings("rawtypes")
	public static void printCollectionByLine(Collection collection)
	{
		for (Object val:collection)
		{
			System.out.println(val);
		}
			
	}	
	
	/**
	 * 
	 * @param collection
	 * @param debugFilePath
	 */
	@SuppressWarnings("rawtypes")
	public static void debugCollectionByLine(Collection collection, String debugFilePath)
	{
		try{
			FileWriter debugFW = new FileWriter(debugFilePath);
		
			for (Object val:collection)
			{
				debugFW.append(val+"\r\n");
			}
			
			debugFW.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public static void printMapByLine(Map map)
	{
		for (Object key:map.keySet())
		{
			System.out.println(key +"="+map.get(key));
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void debugMapByLine(Map map, String debugFilePath)
	{
		try{
			FileWriter debugFW = new FileWriter(debugFilePath);
		
			for (Object key:map.keySet())
			{
				debugFW.append(key +"="+map.get(key) +"\r\n");
			}
			
			debugFW.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void printMapByCount(Map map)
	{
		TreeSet<String> countNameSet = new TreeSet<String>();
		for (Object key:map.keySet())
		{
			Object value = map.get(key);
			if (value instanceof Integer)
			{
				String strCount = String.format("%08d", value);
				String combination = strCount + "_" + key.toString();
				countNameSet.add(combination);
			}
		}
		
		for (String countName:countNameSet.descendingSet())
		{
			System.out.println(countName);
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	public static void debugMapByCount(Map map, String debugFilePath)
	{
		try{
			FileWriter debugFW = new FileWriter(debugFilePath);
			TreeSet<String> countNameSet = new TreeSet<String>();
			
			for (Object key:map.keySet())
			{
				Object value = map.get(key);
				if (value instanceof Integer)
				{
					String strCount = String.format("%08d", value);
					String combination = strCount + "_" + key.toString();
					countNameSet.add(combination);
				}
			}
			
			for (String countName:countNameSet.descendingSet())
			{
//				System.out.println(countName);
				debugFW.append(countName+"\r\n");
			}
			debugFW.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static  String getStrDoubleArr(double[] arr){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i<arr.length-1; i++){
			double var = arr[i];
			sb.append(var + ", ");
		}
		
		return sb.toString();
	}
	
	
	@SuppressWarnings("rawtypes")
	public static String getStrMapByLine(Map map)
	{
		StringBuffer sb = new StringBuffer();
		for (Object key:map.keySet())
		{
			sb.append(key.toString().trim() +"="+map.get(key).toString().trim() + "\r\n");
		}
		return sb.toString();
	}
	
	
	@SuppressWarnings("rawtypes")
	public static String getStrAlignedListByLine(List list1, List list2)
	{
		StringBuffer sb = new StringBuffer();
		
		for (int i=0; i<list1.size(); i++){
			sb.append(list1.get(i).toString().trim() +"=" + list2.get(i).toString().trim() + "\r\n");
		}
		return sb.toString();
	}
}
