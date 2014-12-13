package repository;

import groumvisitors.JavaGroumVisitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import main.ExtractBugReportFeature;
import main.FeatureExtraction;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import apidescription.APIDescription;
import apidescription.APIDescriptionReader;
import bug.BugReport;
import bug.BugReportReader;
import utils.Logger;

public class TestGit {

	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException 
	{
		String projectBugPath="dataset/aspectj_bug.csv";
		String projectSourcePath="dataset/org.aspectj";
		String sourceFolder="/home/anlam/workspace/dataset/aspectj_testParse";
		String projectAPIDesPath="dataset/aspectj_api.csv";
		String outputFolder = "/home/anlam/workspace/dataset/CommitFiles";
		String testParsing = "/home/anlam/workspace/dataset/CommitFiles/5ef30bd";
		String commitFiles = ".";
		
		System.out.println("ExtractBugReportFeature....");
		//ExtractBugReportFeature.extractBugReportsToVectors(projectBugPath);
		List<BugReport> bugReports = BugReportReader.readAPI(projectBugPath);
		System.out.println("ProjectSourceParsing....");
		JavaGroumVisitor javagroum = FeatureExtraction.projectSourceParsing(projectSourcePath);
		//GitConnector gitConn = new GitConnector(projectSourcePath + "/.git");
		System.out.println("ReadAPIToMap....");
		LinkedHashMap<String, APIDescription> apiDes = APIDescriptionReader.readAPIToMap(projectAPIDesPath);

			{
				BugReport bug = bugReports.get(43);
				System.out.println(bug.bug_id);
				LinkedHashMap<String, List<LinkedHashMap<Integer, Integer>>> bugFilesMap = FeatureExtraction.ExtractFilesInFolder(commitFiles + "/" + bug.commit, bug.files, javagroum, apiDes);
			}
			System.out.println("End");
	
	}

}
