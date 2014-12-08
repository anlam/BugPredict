/**
 * 
 */
package data;

import java.io.Serializable;

/**
 * @author Anh
 *
 */
public class DiffPairInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4394207397629114760L;
	
	public int sourceStartPosition = -1;
	public String sourceRepresentStr = null;
	
	public int targetStartPosition = -1;
	public String targetRepresentStr = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public DiffPairInfo(int sourceStartPosition, String sourceRepresentStr, 
			int targetStartPosition, String targetRepresentStr) {
		this.sourceStartPosition = sourceStartPosition;
		this.sourceRepresentStr = sourceRepresentStr;
		this.targetStartPosition = targetStartPosition;
		this.targetRepresentStr = targetRepresentStr;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DiffPairInfo [sPos=");
		builder.append(sourceStartPosition);
		builder.append(", sStr=");
		builder.append(sourceRepresentStr);
		builder.append(", tPos=");
		builder.append(targetStartPosition);
		builder.append(", tStr=");
		builder.append(targetRepresentStr);
		builder.append("]");
		return builder.toString();
	}

}
