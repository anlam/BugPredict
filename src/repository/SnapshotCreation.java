/**
 * 
 */
package repository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import org.tmatesoft.svn.core.io.SVNRepository;

import config.GlobalConfig;
import utils.FileUtils;
import utils.Logger;

/**
 * @author Anh
 *
 */
public class SnapshotCreation {
	
	static String[] filteredExtArr = {".java"};
	TreeMap<String, String> fileContentMap = new TreeMap<String, String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		doMain();
		if(args.length>0){
			GlobalConfig.SVNURL =args[0];
		}
		doForAllProjects();
	}



	public static void doForAllProjects(){
//		String url = "http://anhdesktop:8080/svn/" ;
		String url = GlobalConfig.SVNURL ;

//		String name = "anh";
//		String password = "Vietus09";
//		
		
		
		List<String> repoList = RepoListProcessing.getRepoListFromFile(GlobalConfig.repoFilePath);
		SVNUtils.setupLibrary();

		for (String project:repoList)
		{
			String repoUrl = url+project;
			if (GlobalConfig.useTrunkOnly){
				repoUrl = repoUrl +"/trunk";  
			}
			SVNRepository repository = SVNUtils.doAuthenticate(repoUrl, GlobalConfig.name, GlobalConfig.password);
			
			String subUrl = repoUrl;
			if (GlobalConfig.useSourcePathFilter){
				String tmp = SVNUtils.getSourcePathOfLastSnapshot(repository, repoUrl); 
				if ( tmp == null)
					subUrl = repoUrl;
				else{ 
					/**
					 * Go deep to 2 levels
					 */
					if (tmp.startsWith("/"))
						tmp = tmp.substring(1);
					if (tmp.contains("/"))
					{
						int begin1 = tmp.indexOf("/");
						String tmp1 = tmp.substring(0,begin1);
						String tmp2 = tmp.substring(begin1+1);
						if (tmp2.contains("/")){
							tmp2 = tmp2.substring(0,tmp2.indexOf("/"));
						}
						
						if (tmp1.contains("trunk"))
							tmp = tmp1;
						else
							tmp = tmp1  + "/" + tmp2;
					}
					subUrl = repoUrl + "/" + tmp;
				}
				Logger.log("repoUrl: " + repoUrl);
				Logger.log("\tsubUrl: " + subUrl);
				
			}
			
			repository = SVNUtils.doAuthenticate(subUrl, GlobalConfig.name, GlobalConfig.password);

			TreeMap<String, String> fileContentMap = getContentMap(repository, subUrl);
			writeData(GlobalConfig.projectDataDir, project, fileContentMap);
		}

		
	}
	public static void doMain(){

		/*
		 * Default values:
		 */
		String project = "jts-topo-suite";
		String url = "https://anhnguyenlt:8443/svn/" + project;
		String name = "anh";
		String password = "Vietus09";

		SVNRepository repository = SVNUtils.doAuthenticate(url, name, password);

		/*
		 * Initializes the library (it must be done before ever using the
		 * library itself)
		 */
		SVNUtils.setupLibrary();

		dirCleanup(GlobalConfig.dummyDir);

		TreeMap<String, String> fileContentMap = getContentMap(repository, url);
		writeData(GlobalConfig.projectDataDir, project, fileContentMap);
	}
	
	public static void writeData(String projectDataPath, String project, TreeMap<String, String> fileContentMap ){
//		FileUtils.writeObjectFile(fileContentMap, projectDataPath + "/" + project + ".dat");
		FileUtils.writeCompressedObjectFile(fileContentMap, projectDataPath + "/" + project + GlobalConfig.dataExt);

	}
	
	@SuppressWarnings("unchecked")
	public static TreeMap<String, String> readData(String projectDataPath, String project){
//		return (TreeMap<String, String>) FileUtils.readObjectFile( projectDataPath + "/" + project + ".dat");
		return (TreeMap<String, String>) FileUtils.readCompressedObjectFile( projectDataPath + "/" + project + GlobalConfig.dataExt);
	}

	public static void buildDummyDir(String dummyDir,  TreeMap<String, String> fileContentMap){
		for (String entryPath:fileContentMap.keySet()) {
			String content = fileContentMap.get(entryPath);
			if (content == null)
				continue;
			String path = dummyDir + entryPath;
//			Logger.log("path: " + entryPath);
			File file = new File(path);
			File parent = file.getParentFile();
			if (!parent.exists()){
				parent.mkdirs();
			}
//			try{
//				FileWriter fw = new FileWriter(file);
//				fw.append(content);
//				fw.close();
//				fw.flush();
//				fw = null;
//				System.gc();
////				save(file, content);
//			}
//			catch(Exception e){
//				e.printStackTrace();
//			}
			try {
				org.apache.commons.io.FileUtils.writeStringToFile(file, content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			Logger.log("content of " + entryPath + " : " + content);
		}
	}
	
	public static void save(File file, String textToSave) {

	    try {
	        if (textToSave!=null)
	        {
	        	BufferedWriter out = new BufferedWriter(new FileWriter(file), 200000);
	        	out.write(textToSave);
	        	out.flush();
	        	out.close();
	        	out = null;
	            System.gc();
	        }
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	public static TreeMap<String, String> getContentMap(SVNRepository repository, String url){
		TreeMap<String, String> fileContentMap = new TreeMap<String, String>();
		List<String> filteredEntries = DisplayRepositoryTree.getFilteredEntries(repository, url, filteredExtArr);
		Logger.log("\tfilteredEntries size: " + filteredEntries.size());

		for (String entryPath:filteredEntries) {
//			Logger.log(entryPath);
			String content = DisplayFile.getFileContent(repository, url, entryPath.substring(1)); //remember to remove the "/" at the beginning of entry
			fileContentMap.put(entryPath,content);

		}
		return fileContentMap;
	}
	
	
	
	public static void buildDummyDir(SVNRepository repository, String url, String dummyDir){
		List<String> filteredEntries = DisplayRepositoryTree.getFilteredEntries(repository, url, filteredExtArr);
		Logger.log("\tfilteredEntries size: " + filteredEntries.size());

		for (String entryPath:filteredEntries) {
//			Logger.log(entryPath);
			String content = DisplayFile.getFileContent(repository, url, entryPath.substring(1)); //remember to remove the "/" at the beginning of entry
			String path = dummyDir + entryPath;
			File file = new File(path);
			File parent = file.getParentFile();
			if (!parent.exists()){
				parent.mkdirs();
			}
			try{
				FileWriter fw = new FileWriter(file);
				fw.append(content);
				fw.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
//			Logger.log("content of " + entryPath + " : " + content);
		}
	}
	
	public static synchronized void dirCleanup(String dirPath) {
		File dir = new File(dirPath);
		try {
			FileUtils.deleteRecursive(dir);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dir.mkdirs();
	}
}
