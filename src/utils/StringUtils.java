/**
 * 
 */
package utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * @author ANH
 *
 */
public class StringUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.log(isStartUpperCase(" Test"));
		
		
		String str1 = "CodeGenerator.java";
		String str2 = "CodeGenerator.cs";
		String lcsStr = lcs(str1,str2);
		Logger.log(lcsStr);
		
		double lcsSim = calcLCSSimMin(str1, str2);
		Logger.log(lcsSim);

	}

	public static int countNumLines(String str){
		Scanner scanner = new Scanner(str);    

		int count = 0;               
		while (scanner.hasNextLine()) { 
			scanner.nextLine();   
			count++;              
		}  

		scanner.close();
		return count;
	}
	
	
	
	public static boolean isStartUpperCase(String str){
		boolean startUpperCase = false;
		if((str == null)||(str.length()==0)){
			startUpperCase = false;
		}
		else{
			String tmp = str;//str.trim();
			if (tmp.matches("[A-Z].*")){
				startUpperCase = true;
			}
		}
		return startUpperCase;
	}
	
	public static double calcLCSSimAvg(String a, String b){
		double lcsSim = 0.0;
		
		String lcsStr = lcs(a,b);
		
		double avg = ((double)a.length() + (double)b.length())/2.0;
		lcsSim = (double)lcsStr.length()/avg;
		
		return lcsSim;
	}
	
	public static double calcLCSSimMin(String a, String b){
		double lcsSim = 0.0;
		
		String lcsStr = lcs(a,b);
		
		double min = Math.min((double)a.length(), (double)b.length());
		lcsSim = (double)lcsStr.length()/min;
		
		return lcsSim;
	}
	
	
//	public static String lcs(String a, String b){
//	    int aLen = a.length();
//	    int bLen = b.length();
//	    if(aLen == 0 || bLen == 0){
//	        return "";
//	    }else if(a.charAt(aLen-1) == b.charAt(bLen-1)){
//	        return lcs(a.substring(0,aLen-1),b.substring(0,bLen-1))
//	            + a.charAt(aLen-1);
//	    }else{
//	        String x = lcs(a, b.substring(0,bLen-1));
//	        String y = lcs(a.substring(0,aLen-1), b);
//	        return (x.length() > y.length()) ? x : y;
//	    }
//	}
	
	public static String lcs(String a, String b){
	    char[]s1 = a.toCharArray();
	    char[]s2 = b.toCharArray();
	    List<Character> lcsList = LongestCommonSubsequence(s1, s2);
	    
	    StringBuilder tmp = new StringBuilder();
	    for (Character c: lcsList){
	    	tmp.append(c);
	    }
	    return tmp.toString();
	}
	
	public static  List<Character> LongestCommonSubsequence(char[] s1, char[] s2)
	{
	        int[][] num = new int[s1.length+1][s2.length+1];  //2D array, initialized to 0
	 
	        //Actual algorithm
	        for (int i = 1; i <= s1.length; i++)
	                for (int j = 1; j <= s2.length; j++)
	                        if (s1[i-1] == s2[j-1])
	                                num[i][j] = 1 + num[i-1][j-1];
	                        else
	                                num[i][j] = Math.max(num[i-1][j], num[i][j-1]);
	 
	 
	        int s1position = s1.length, s2position = s2.length;
	        List<Character> result = new LinkedList<Character>();
	 
	        while (s1position != 0 && s2position != 0)
	        {
	                if (s1[s1position - 1]==(s2[s2position - 1]))
	                {
	                        result.add(s1[s1position - 1]);
	                        s1position--;
	                        s2position--;
	                }
	                else if (num[s1position][s2position - 1] >= num[s1position][s2position])
	                {
	                        s2position--;
	                }
	                else
	                {
	                        s1position--;
	                }
	        }
	        Collections.reverse(result);
	        return result;
	}
	
	
	public static double calcLongestSubStrSimAvg(String a, String b){
		double longestSubStrSim = 0.0;
		
		int len = longestSubstr(a,b);
		
		double avg = ((double)a.length() + (double)b.length())/2.0;
		longestSubStrSim = (double)len/avg;
		
		return longestSubStrSim;
	}
	
	public static double calcLongestSubStrSimMin(String a, String b){
		double longestSubStrSim = 0.0;
		
		int len = longestSubstr(a,b);
		
		double min = Math.min((double)a.length(),(double)b.length());
		longestSubStrSim = (double)len/min;
		
		return longestSubStrSim;
	}
	
	public static int longestSubstr(String first, String second) {
	    if (first == null || second == null || first.length() == 0 || second.length() == 0) {
	        return 0;
	    }
	 
	    int maxLen = 0;
	    int fl = first.length();
	    int sl = second.length();
	    int[][] table = new int[fl][sl];
	 
	    for (int i = 0; i < fl; i++) {
	        for (int j = 0; j < sl; j++) {
	            if (first.charAt(i) == second.charAt(j)) {
	                if (i == 0 || j == 0) {
	                    table[i][j] = 1;
	                }
	                else {
	                    table[i][j] = table[i - 1][j - 1] + 1;
	                }
	                if (table[i][j] > maxLen) {
	                    maxLen = table[i][j];
	                }
	            }
	        }
	    }
	    return maxLen;
	}
}
