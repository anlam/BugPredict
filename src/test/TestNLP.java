package test;

import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;


public class TestNLP {

	public static void main(String[] args) 
	{
		 String regex = "[a-zA-Z]+|[0-9]+|\\S";
		 //String regex = "\\S";
	     TokenizerFactory tf = new RegExTokenizerFactory(regex);
	     tf = new LowerCaseTokenizerFactory(tf);
	     tf = new EnglishStopTokenizerFactory(tf);
	     tf  = new PorterStemmerTokenizerFactory(tf);
	     char[] cs = "ABC de 123. This is a b an my word. words. service services That that. these".toCharArray();
	     Tokenizer tokenizer = tf.tokenizer(cs,0,cs.length);
	     for(String i : tokenizer.tokenize())
	     {
	    	 System.out.println(i);
	     }
	}

}
