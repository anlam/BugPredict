package utils;

import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class NLPUtils 
{
	public static String[] RemoveStopWordsAndStemmer(String sentences)
	{
		 String regex = "[a-zA-Z]+|[0-9]+|\\S";
		 TokenizerFactory tf = new RegExTokenizerFactory(regex);
	     tf = new LowerCaseTokenizerFactory(tf);
	     tf = new EnglishStopTokenizerFactory(tf);
	     tf  = new PorterStemmerTokenizerFactory(tf);
	     char[] cs = sentences.toCharArray();
	     Tokenizer tokenizer = tf.tokenizer(cs,0,cs.length);
	     return tokenizer.tokenize();
	}
}
