package dirtool;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class NormalizeText {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}
	
	//TODO: I temporarily replace ">>" by "> >" to avoid generic problem, it is not good because it makes 
	//shift left operation wrong. Should change the parser (CSharpParser.jj) for the complete solution;  
	public static String getNormString(String fileName){
		StringBuilder sb =new StringBuilder();
		Scanner sc;
		try {
			sc = new Scanner(new File(fileName));
			sc.useDelimiter("\\Z");
			String tmp = sc.next();
			char lastch = ' ';
			for (int i = 0; i < tmp.length(); i++) {
			    char ch = tmp.charAt(i);
			    
			    if (ch <= 0x7F) {
			    	if(lastch=='>' && ch == '>')
			    	{
			    		sb.append(' ');
			    	}
			        sb.append(ch);
			        lastch = ch;
			    }
			   
			}
			
			
			sc.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//	    Logger.log("sbTotal: \r\n" + sb );

		//TODO: should solve this case better;
		String tmp = sb.toString().trim();
		tmp = tmp.replaceAll("///", "//");
		tmp = tmp.replaceAll("////", "// //");
		tmp = tmp + "\r\n";
		return tmp;
	}
}
