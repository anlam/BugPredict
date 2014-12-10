package repository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VCSConnector {
	static String[] fixingPatterns = {
		//"issue"
		//"issue[\\s]+[0-9]+"
		//,"issues[\\s]+[0-9]+"
		//,"issue[\\s]+#[0-9]+"
		//,"issues[\\s]+#[0-9]+"
		//,"issue[\\s]+# [0-9]+"
		//,"bug"
		"bug[\\s]+[0-9]+"
		,"bug[\\s]+#[0-9]+"
		,"bug[\\s]+# [0-9]+"
		,"bug id=[0-9]+"
		//,"fix"
//		,"fix[ ]+#[0-9]+"
//		,"fixes[ ]+#[0-9]+"
//		,"pr[ ]+[0-9]+"   
//		,"pr[\\:][ ]+[0-9]+"
		//,"error"
		//,"exception"
		/*"\\bfix(s|es|ing|ed)?\\b",
		"\\berror(s)?\\b",
		"\\bbug(s)?",
		"\\bissue(s)?"*/
	};
	
	static String[] fixingPatterns_old = {
		//"issue"
		"issue[\\s]+[0-9]+"
		,"issues[\\s]+[0-9]+"
		,"issue[\\s]+#[0-9]+"
		,"issues[\\s]+#[0-9]+"
		,"issue[\\s]+# [0-9]+"
		,"bug"
		/*,"bug[\\s]+[0-9]+"
		,"bug[\\s]+[0-9]+"
		,"bug[\\s]+#[0-9]+"
		,"bug[\\s]+#[0-9]+"
		,"bug[\\s]+# [0-9]+"
		,"bug id=[0-9]+"*/
		,"fix"
//		,"fix[ ]+#[0-9]+"
//		,"fixes[ ]+#[0-9]+"
//		,"pr[ ]+[0-9]+"   
//		,"pr[\\:][ ]+[0-9]+"
		,"error"
		,"exception"
		/*"\\bfix(s|es|ing|ed)?\\b",
		"\\berror(s)?\\b",
		"\\bbug(s)?",
		"\\bissue(s)?"*/
	};
	
	public static int getBugID(String commitLog)
	{
		int ret = -1;
		
		if (commitLog != null){
			Pattern p;
			String tmpLog = commitLog.toLowerCase();
			for (int i = 0; i < fixingPatterns.length; i++){
				String patternStr = fixingPatterns[i];
				p = Pattern.compile(patternStr);
				Matcher m = p.matcher(tmpLog);
				boolean isFixing = m.find();
				if (isFixing)
				{
					String tmpId = m.group();
					p = Pattern.compile("[0-9]+");
					m = p.matcher(tmpId);
					if(m.find())
						ret = Integer.parseInt(m.group());
				}
			}
		}
		
		return ret;
	}
	
	public static boolean isFixingCommit(String commitLog){
		if (commitLog != null){
			Pattern p;
			String tmpLog = commitLog.toLowerCase();
			for (int i = 0; i < fixingPatterns.length; i++){
				String patternStr = fixingPatterns[i];
				p = Pattern.compile(patternStr);
				Matcher m = p.matcher(tmpLog);
				boolean isFixing = m.find();
				if (isFixing){
					return true;
				}
			}
		}
		return false;
	}

}
