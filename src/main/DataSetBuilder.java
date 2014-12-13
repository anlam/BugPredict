package main;

import groumvisitors.JavaGroumVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;




import repository.GitConnector;
import utils.Logger;
import apidescription.APIDescription;
import apidescription.APIDescriptionReader;
import bug.BugReport;

public class DataSetBuilder 
{
	public static String projectBugPath="/home/anlam/workspace/dataset/aspectj_bug.csv";
	public static String projectSourcePath="/home/anlam/workspace/dataset/org.aspectj";
	public static String sourceFolder="/home/anlam/workspace/dataset/aspectj_testParse";
	public static String projectAPIDesPath="/home/anlam/workspace/dataset/aspectj_api.csv";
	public static String commitFiles = "/home/anlam/workspace/dataset/CommitFiles";
	public static LinkedHashMap<String, APIDescription> apiDes = new LinkedHashMap<>();
	
	public static LinkedHashMap<String, LinkedHashMap<String, List<LinkedHashMap<Integer, Integer>>>> possitiveDataSet = new LinkedHashMap<>();

	public static void buildDataSet()
	{
		Logger.initDebug("ErrorFile.txt");
		System.out.println("ExtractBugReportFeature....");
		ExtractBugReportFeature.extractBugReportsToVectors(projectBugPath);
		System.out.println("ProjectSourceParsing....");
		JavaGroumVisitor javagroum = FeatureExtraction.projectSourceParsing(projectSourcePath);
		//GitConnector gitConn = new GitConnector(projectSourcePath + "/.git");
		System.out.println("ReadAPIToMap....");
		apiDes = APIDescriptionReader.readAPIToMap(projectAPIDesPath);
		//if (gitConn.connect()) 
		{
			//gitConn.getFileChanges(".java");
			for(BugReport bug : ExtractBugReportFeature.bugReports)
			{
				System.out.println(bug.bug_id);
				LinkedHashMap<String, List<LinkedHashMap<Integer, Integer>>> bugFilesMap = FeatureExtraction.ExtractFilesInFolder(commitFiles + "/" + bug.commit, bug.files, javagroum, apiDes);
				
				//LinkedHashMap<String, List<LinkedHashMap<Integer, Integer>>> bugFilesMap = new LinkedHashMap<>();
				//LinkedHashMap<String, String> files = gitConn.getFileContent(bug.commit , bug.files, ".java");
				/*LinkedHashMap<String, String> files = gitConn.getChangedFileContent(bug.commit);
				if(files == null||files.isEmpty())
					continue;
				for(String filename : files.keySet())
				{
					String filecontent = files.get(filename);
					try 
					{
						File file = File.createTempFile(filename, ".tmp");
						FileWriter fw = new FileWriter(file);
						fw.write(filecontent);
						fw.close();
						List<LinkedHashMap<Integer, Integer>> filefeatures = FeatureExtraction.ExtractFileFeatures(file, filename, javagroum, apiDes);
						bugFilesMap.put(filename, filefeatures);
						System.out.println(filename);
						file.delete();
					} 
					catch (IOException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						//return;
					} 
				}*/
				possitiveDataSet.put(bug.bug_id, bugFilesMap);
			}
		}
		Logger.closeDebug();
		
	}
	
	public static void main(String[] args) 
	{
		buildDataSet();
		System.out.println("end");
	}

}
