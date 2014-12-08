/**
 * 
 */
package main;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import repository.RepoListProcessing;
import utils.Logger;
import config.ChangeConfig;
import config.GlobalConfig;
import change.CField;
import change.CInitializer;
import change.CMethod;
import change.ChangeAnalyzer;
import change.RevisionAnalyzer;

/**
 * @author anhnt
 *
 */
public class ChangeAnalyzerExecution {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String outputDataPath = ChangeConfig.mainPath +"/diff_data/";
		if (args.length>=5)
		{
			GlobalConfig.repoFilePath = args[0];
			GlobalConfig.mainDir = args[1];

			ChangeConfig.svnRootUrl = args[2];
			ChangeConfig.account = args[3];
			ChangeConfig.password =  args[4];

			GlobalConfig.refreshParams();
			ChangeConfig.refreshParams();

			ChangeConfig.outChangesPath = ChangeConfig.outPath + "/" + "changes.txt";
			outputDataPath = ChangeConfig.mainPath +"/diff_data/";
			ensureExistDir(ChangeConfig.outPath);

			ensureExistDir(outputDataPath);

			List<String> repoList = RepoListProcessing.getRepoListFromFile(GlobalConfig.repoFilePath);

			for (String projectName:repoList)
			{
				ChangeConfig.svnUrl =  ChangeConfig.svnRootUrl + projectName ;

				doMain(projectName, ChangeConfig.svnUrl, ChangeConfig.account, ChangeConfig.password,outputDataPath);
			}
		}

		else 
		{
			ensureExistDir(ChangeConfig.outPath);

			ensureExistDir(outputDataPath);

			String projectName =  ChangeConfig.projectName;
			ChangeConfig.svnUrl =  ChangeConfig.svnRootUrl + projectName ;

			doMain(projectName, ChangeConfig.svnUrl, ChangeConfig.account, ChangeConfig.password,outputDataPath);
		}
	}



	public static void ensureExistDir(String dirPath){
		try{
			File dir = new File(dirPath);
			if (!dir.exists()){
				dir.mkdirs();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}


	public static void doMain(String projectName, String svnUrl, String account, String password, String outputDataPath){
		//		Logger.initDebug("debugCA.txt");
		ChangeAnalyzer ca = new ChangeAnalyzer(projectName, svnUrl);
		ca.buildSvnConnector(account, password);
		ca.buildLogAndAnalyze(outputDataPath);


		//		Logger.logHDebug("Finish analyzing " + projectName);
		//		System.exit(0);
		//		Logger.closeDebug();
	}
}
