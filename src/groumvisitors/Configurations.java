/**
 * 
 */
package groumvisitors;



/**
 * @author Anh
 *
 */
public class Configurations {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public static boolean isGetTypeFullName = true;//false;//true;
	public static boolean isAddMethodParam = false;//true;
	
	public static boolean isDisplayData = true;
	public static int maxNumDisplayNodes = 512;
	static boolean includePrimitiveVarType = false;
	static boolean includePrimitiveFieldType = true;
	public static boolean isAddVarToControl = false;

	public static String graphJavaOutPath = "tmpJavaGraph/";
	public static String graphNetOutPath = "tmpNetGraph/";
	
	public static String javaSentencePath = "tmpSentences/javaSentence.txt";
	public static String netSentencePath = "tmpSentences/netSentence.txt";
	public static String javaSentencePath2 = "tmpSentences/sentence.jv";
	public static String netSentencePath2 = "tmpSentences/sentence.net";

	public static String javaSentenceFullPathMulti = "tmpSentences/sentenceFullMulti";
	public static String netSentenceFullPathMulti = "tmpSentences/sentenceFullMulti";
	public static String javaSentencePathMulti = "tmpSentences/sentenceMulti";
	public static String netSentencePathMulti = "tmpSentences/sentenceMulti";
	public static String shortToFullUsageMapPath = "tmpSentences/shortToFullUsageMap.dat";
	
	public static String translationTablePath = "tmpSentences/phrases-om";
	public static String sortedTranslationTablePath = "tmpSentences/sortedTranslationTablePath";
	public static String singlepairTranslationTablePath = "tmpSentences/singlepairTranslationTablePath";
	public static String singlepairTranslationTableCheckedPath = "tmpSentences/singlepairTranslationTableCheckedPath";
	public static String singlepairMAMTranslationTablePath = "tmpSentences/singlepairMAMTranslationTablePath";

	public static String filteredLibPairs = "tmpSentences/filteredLibPairs";


	public static String translationTablePath1 = "tmpSentences/phrases-om_1";
	public static String sortedTranslationTablePath1 = "tmpSentences/sortedTranslationTablePath_1.txt";
	public static String translationTablePath2 = "tmpSentences/phrases-om_2";
	public static String sortedTranslationTablePath2 = "tmpSentences/sortedTranslationTablePath_2.txt";
	public static String translationTablePath3 = "tmpSentences/phrases-om_3";
	public static String sortedTranslationTablePath3 = "tmpSentences/sortedTranslationTablePath_3.txt";

	public static String separateStr = " ";
	public static String seqSepStr = ",";

	public static int shortFlatternThreshold = 1; 
	public static long dataShift = 1000000;
	
	public static long flattenShift = 10000000;
	
	public static boolean isToDotFile = false;
	public static boolean isPrintGraph = false;//false;//true;
	public static String outImageExt = "svg";

	public static int listParentDepth = 1;
//	public static int listParentDepth = 2;

	public static boolean isGoMoreInFields = false;
	public static boolean isKeptCrossedPair = false;
	public static boolean isSkipSinglePair = true;
	
	public static int maxNumPatternObjects = 3;
	public static double pairWeightCut = 0.1;
	
	public static String baseLibrary  = "D:/Research/OOPSLA2013/Work/data/CSLibrary";
	
	public static int maxPhraseLength = 16;
	public static int minPhraseLength = 1;

	public static boolean isForBigProject = true;
	public static boolean isFilterSmaller = true;
	
	
	//FOR Referenced MAM tool
	public static double nameMAMSimThreshold = 0.0;
	public static final boolean isRemoveInstanceof = true;
}