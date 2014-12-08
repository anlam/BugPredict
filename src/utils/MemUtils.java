/**
 * 
 */
package utils;

/**
 * @author ANH
 *
 */
public class MemUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}

	public static void displayDiffMem(Runtime runtime, long freeMemory){
	
		long freeMemoryAfter = runtime.freeMemory();
	
		long diff = freeMemory - freeMemoryAfter;
		
		long divGB = 1024L*1024L*1024L;
		long diffGB = diff / divGB;
	
		long divMB = 1024L*1024L; 
		long diffMB = (diff  - diffGB*divGB)  / (divMB);
		
		long divKB = 1024L;
		long diffKB = (diff - diffGB*divGB - diffMB*divMB)/divKB;
		
		Logger.log("memory used Total: " + (freeMemory - freeMemoryAfter)+"\t" + diffGB +"GB, " + diffMB + "MB, " + diffKB + "KB");
	
	}

}
