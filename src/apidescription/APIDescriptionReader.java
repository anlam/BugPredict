package apidescription;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class APIDescriptionReader 
{
	public static List<APIDescription> readAPI(String filepath)
	{
		//return (ArrayList<APIDescription>) FileUtils.readObjectFile(filepath );
		
		ArrayList<APIDescription> ret = new ArrayList<APIDescription>();
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
			    String class_url = record.get(1);
			    String class_name = class_url.substring(class_url.lastIndexOf("/") + 1, class_url.length() - 5);
			    //System.out.println(class_name);
			    String description = record.get(2);
			    APIDescription api = new APIDescription(id, class_url, description);
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
	
	
	public static LinkedHashMap<String, APIDescription> readAPIToMap(String filepath)
	{
		//return (ArrayList<APIDescription>) FileUtils.readObjectFile(filepath );
		
		LinkedHashMap<String, APIDescription> ret = new LinkedHashMap<String, APIDescription>();
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
			    String class_url = record.get(1);
			    String class_name = class_url.substring(class_url.lastIndexOf("/") + 1, class_url.length() - 5);
			    //System.out.println(class_name);
			    String description = record.get(2);
			    APIDescription api = new APIDescription(id, class_url, description);
			    ret.put(class_name, api);
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
	
	public static void main(String[] args) 
	{
		String path = "C:\\Users\\An\\workspace\\BugPredict\\dataset\\aspectj_api.csv";
		LinkedHashMap<String, APIDescription> test = APIDescriptionReader.readAPIToMap(path);
		//while(true);
	}

}
