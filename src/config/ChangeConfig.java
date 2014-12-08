package config;

public class ChangeConfig {
	public static String changePath = GlobalConfig.mainDir + "change/";
	public  static String outChangesPath =  changePath +  "/" + "changes.txt";

	public static String outputDirPath =changePath + "SubjectSystems/webpatterns/output";

	public static final long revisionStep = 99L;

	public static boolean isDebug = false;
	public static boolean isDebugTree = false;

	public static boolean isTimeOut = false;
	public static int TimeOut = 1000; //1000s

	public static boolean isAnalyzeFieldChange = false;

	public static final String diffPostFix = "DiffPairs";
	public static final String datExt = ".dat";

	public static boolean isDebugMethodInitTree = false;

	public static  int maxHeight = 1000;


	public static String svnRootUrl = "https://ANHNGUYENLT:8443/svn/";
	public static String projectName = "Zxing";
	public static String svnUrl =  svnRootUrl + projectName; 
	public static String account = "anh";
	public static String password =  "Vietus09";
	public static String outPath = changePath + "out/";
	public static String mainPath = changePath;


	public static String revMainDummyDir = changePath + "dummyDir/";


	public static String [] allSourceFileExt = new String[] {".java", ".cs"};

	public static String SVNHistoryPath = changePath + projectName +"_SVNHistory.dat";

	public static String logPath = changePath + "log.txt";
	
	
	public static String fixingGroumPath = changePath + "fixing_groums/"; 

	public static void refreshParams(){
		changePath = GlobalConfig.mainDir + "change/";
		outChangesPath =  changePath +  "/" + "changes.txt";

		outputDirPath =changePath + "SubjectSystems/webpatterns/output";

		outPath = changePath + "out/";
		mainPath = changePath;

		revMainDummyDir = changePath + "dummyDir/";

		SVNHistoryPath = changePath + projectName +"_SVNHistory.dat";
		logPath = changePath + "log.txt";
		fixingGroumPath = changePath + "fixing_groums/"; 
	}

}
