package test;

import main.FeatureExtraction;

public class Test {

	public static void main(String[] args) 
	{
		//C:\Users\An\workspace\RECODER\src
		String projectSourcePath="dataset/org.aspectj";
		String sourceFolder="dataset/aspectj_testParse";
		String projectAPIDesPath="dataset/aspectj_api.csv";
		FeatureExtraction.testParsing(projectSourcePath, sourceFolder, projectAPIDesPath);
		//FeatureExtraction.testParsing("C:\\Users\\An\\workspace\\RECODER\\src", "C:\\Users\\An\\workspace\\RECODER\\test");
		//FeatureExtraction.doProjectParsing("C:\\Users\\An\\workspace\\RECODER\\test");
		//while(true);
	}

}
