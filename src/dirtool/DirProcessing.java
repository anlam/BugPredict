package dirtool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirProcessing {


	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
	}

	


	
	public static List<File> getRecursiveFiles(File parentDir)
	{
		List<File> recursiveFiles = new ArrayList<File>();
		
		File[] childFiles = parentDir.listFiles();
		
		for (File file:childFiles)
		{
			if (file.isFile())
			{
				recursiveFiles.add(file);
				
			}
			else
			{
				List<File> subList = getRecursiveFiles(file);
				recursiveFiles.addAll(subList);
			}
		}
		
		return recursiveFiles;
	}
	
	public static List<File> getAllRecursiveFiles(File parentDir)
	{
		List<File> recursiveFiles = new ArrayList<File>();
		
		File[] childFiles = parentDir.listFiles();
		
		for (File file:childFiles)
		{
			if (file.isFile())
			{
				recursiveFiles.add(file);
				
			}
			else
			{
				recursiveFiles.add(file);

				List<File> subList = getRecursiveFiles(file);
				recursiveFiles.addAll(subList);

			}
		}
		
		return recursiveFiles;
	}
	
	
	public static List<File> getFilteredRecursiveFiles(File parentDir, String [] sourceFileExt)
	{
		List<File> recursiveFiles = new ArrayList<File>();
		
		File[] childFiles = parentDir.listFiles();
		if (childFiles == null)
			return recursiveFiles;
		for (File file:childFiles)
		{
			if (file.isFile())
			{

				if (isPassFile(file, sourceFileExt))
				{

					recursiveFiles.add(file);
				}
				
			}
			else
			{
				List<File> subList = getFilteredRecursiveFiles(file, sourceFileExt);
				recursiveFiles.addAll(subList);
			}
		}
		
		return recursiveFiles;
	}

	public static boolean isPassFile(File file, String [] sourceFileExt)
	{
		String name = file.getName();
		for (String fileExt:sourceFileExt)
		{
			if (name.endsWith(fileExt))
			{
				return true;
			}
		}
		return false;
	}
	public static boolean isPassFileName(String fileName, String [] sourceFileExt)
	{
		for (String fileExt:sourceFileExt)
		{
			if (fileName.endsWith(fileExt))
			{
				return true;
			}
		}
		return false;
	}
}
