/**
 * 
 */
package repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import utils.Logger;
import config.GlobalConfig;

/**
 * @author Anh
 *
 */
public class RepoListProcessing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		ArrayList<String> repoList = getRepoListFromDir(GlobalConfig.localRepoDir);
//		Logger.log(repoList);
		
		ArrayList<String> repoList = getRepoListFromFile(GlobalConfig.repoFilePath);
		Logger.log(repoList);
	}

	public static ArrayList<String> getRepoListFromDir(String dirPath){
		ArrayList<String> repoList =new ArrayList<String>();
		try{
			File dir = new File(dirPath);
			File[] children = dir.listFiles();
			for(File child:children){
				if (child.isDirectory()){
					repoList.add(child.getName());
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return repoList;
	}
	
	
	public static ArrayList<String> getRepoListFromFile(String filePath){
		ArrayList<String> repoList =new ArrayList<String>();
		try{
			Scanner sc = new Scanner(new File(filePath));
			while (sc.hasNextLine()){
				repoList.add(sc.nextLine().trim());
			}
			sc.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return repoList;
	}
}
