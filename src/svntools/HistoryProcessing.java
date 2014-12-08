/**
 * 
 */
package svntools;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;


import config.ChangeConfig;
import utils.Logger;
import data.SVNHistoryData;
import data.SVNRevData;

/**
 * @author Anh
 *
 */
public class HistoryProcessing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String SVNHistoryDataPath = ChangeConfig.SVNHistoryPath;
		String hostDir = ChangeConfig.revMainDummyDir;
		
//		DirProcessing.deleteDirDescendant(new File(GlobalConfig.processJavaDirPath));
		long toRevision = 8282;
		doMain(SVNHistoryDataPath, hostDir, toRevision);
		
		
		
		
	}
	

	public static String getNormString(String str){
		StringBuilder sb =new StringBuilder();
		Scanner sc;
		sc = new Scanner(str);
		sc.useDelimiter("\\Z");
		String tmp = sc.next().replaceAll("\\s", " ");
		for (int i = 0; i < tmp.length(); i++) {
			char ch = tmp.charAt(i);

			if (ch <= 0x7F) {
				
				sb.append(ch);
			}

		}
		sc.close();
		return sb.toString();
	}
	
	
	public static void doMain(String SVNHistoryDataPath, String hostDir, long toRevision){
		SVNHistoryData readSVNHistory = SVNHistoryData.readObject(SVNHistoryDataPath);

		Logger.log("readSVNHistory.SVNRevDataMap.size(): " + readSVNHistory.SVNRevDataMap.size());
		
		TreeMap<Long, SVNRevData> SVNRevDataMap = readSVNHistory.SVNRevDataMap;
		TreeSet<String> changeTypes = new TreeSet<>();
		
		for (Long revision:SVNRevDataMap.keySet()){
			if (revision > toRevision){
				break;
			}
			Logger.log("rev: " + revision);
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
		Logger.log(changeTypes);
	}
	
	public static void replaceFile(String oldPath, String newPath, String hostDirPath, String content){
		deleteFile(oldPath, hostDirPath);
		deleteFile(newPath, hostDirPath);
		createFile(newPath, hostDirPath, content);
	}

	public static void modifyFile(String path, String hostDirPath, String newContent){
		deleteFile(path, hostDirPath);
		createFile(path, hostDirPath, newContent);
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
	
	
}
