package bug;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;


public class BugReportReader {

	public static void main(String[] args) 
	{
		 List<BugReport> test = BugReportReader.readAPI("dataset/aspectj_bug.csv");
		 if(true)
		 {
			 System.out.println("abc");
		 }
	}
	
	public static List<BugReport> readAPI(String filepath)
	{
		ArrayList<BugReport> ret = new ArrayList<BugReport>();
		Reader in;
		try 
		{
			int i = 1;
			in = new FileReader(filepath);
			Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
			for (CSVRecord record : records) 
			{

				if(i > 0)
				{
					i--;
					continue;
					
				}
			    String id = record.get(0);
			    String bug_id= record.get(1);
				 String summary= record.get(2);
				 String description= record.get(3);
				 String report_time= record.get(4);
				 String reporter= record.get(5);
				 String assignee= record.get(6);
				 String status= record.get(7);
				 String product= record.get(8);
				 String component= record.get(9);
				 String importance= record.get(10);
				 String commit= record.get(11);
				 String author= record.get(12);
				 String commit_time= record.get(13);
				 String log= record.get(14);
				 String file= record.get(15);
				 List<String> files = Arrays.asList(file.split("\n"));
				 BugReport api = new BugReport( id,  bug_id,  summary,  description,  report_time,
							 reporter,  assignee,  status,  product,  component,
							 importance,  commit,  author,  commit_time,  log,  files);
			    ret.add(api);
			    //System.out.println(ret.size());
			} 
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}

}
