package repository;

import java.io.File;
import java.io.IOException;
import java.util.List;

import main.ExtractBugReportFeature;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import bug.BugReport;
import bug.BugReportReader;
import utils.Logger;

public class TestGit {

	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException 
	{
		String projectBugPath="dataset/aspectj_bug.csv";
		GitConnector gitConn = new GitConnector("dataset/org.aspectj/.git");
		if (gitConn.connect()) {
			/*gc.getLastSnapshot(".tex");
			gc.getSnapshots(".tex");*/
			//gc.getFileChanges(".tex");
			//System.out.println(gc.getNumberOfCommits(null));
			//System.out.println(gc.getNumberOfCommits(".tex"));
			System.out.println("Connected.");
			List<BugReport> bugReports =  BugReportReader.readAPI(projectBugPath);
			for(BugReport bug : bugReports)
			{
			//BugReport bug = bugReports.get(3314);
			
				gitConn.getFileContent(bug.commit+"^1", bug.files, ".java");
			}
			//Logger.initDebug("Fixed revisions.txt");
			//long startProjectTime = System.currentTimeMillis();
			//gitConn.getNumberOfCommits(".java");
			//List<Integer>fixes = gitConn.getJavaFixRevisions();
	    	//long endProjectTime = System.currentTimeMillis();
	    	//Logger.closeDebug();
	    	//System.out.println("Fixed Revision Count: " + fixes.size());
	    	//System.out.println(gitConn.getNumberOfCommits() + "," + gitConn.getNumberOfCodeCommits() + "," + (endProjectTime - startProjectTime));
		}
	}

}
