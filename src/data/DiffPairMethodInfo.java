/**
 * 
 */
package data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Anh
 *
 */
public class DiffPairMethodInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5054052656971914202L;
	
	
	
	public String filePath = "";
	public String className = "";
	public String methodName = "";
	public ArrayList<String> paramTypeList = new ArrayList<>();
	public ArrayList<DiffPairInfo> diffPairs = new ArrayList<>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public DiffPairMethodInfo(String filePath, String className,
			String methodName, ArrayList<String> paramTypeList,
			ArrayList<DiffPairInfo> diffPairs) {
		this.filePath = filePath;
		this.className = className;
		this.methodName = methodName;
		this.paramTypeList = paramTypeList;
		this.diffPairs = diffPairs;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\r\nDiffPairMethodInfo [filePath=");
		builder.append(filePath);
		builder.append(", className=");
		builder.append(className);
		builder.append(", methodName=");
		builder.append(methodName);
		builder.append(", paramTypeList=");
		builder.append(paramTypeList);
//		builder.append(", diffPairs=");
//		builder.append(diffPairs);
		builder.append("]");
		return builder.toString();
	}

}
