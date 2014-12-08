/**
 * 
 */
package data;

/**
 * @author Anh
 *
 */
public class AlignmentIdx implements Comparable<AlignmentIdx>{

	public int startIdx = Integer.MAX_VALUE;
	public int endIdx = Integer.MAX_VALUE;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	
	
	public AlignmentIdx(int startIdx, int endIdx) {
		this.startIdx = startIdx;
		this.endIdx = endIdx;
	}

	//"2:2"
	public AlignmentIdx(String pairString) {
		String[] pairSplit = pairString.split(":");
		this.startIdx = Integer.parseInt(pairSplit[0].trim());
		this.endIdx = Integer.parseInt(pairSplit[1].trim());
	}
	
	@Override
	public int compareTo(AlignmentIdx o) {
		Integer curDataStartIdx = new Integer(startIdx);
		Integer oDataStartIdx = new Integer(o.startIdx);
		return curDataStartIdx.compareTo(oDataStartIdx);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AlignmentIdxData [startIdx=");
		builder.append(startIdx);
		builder.append(", endIdx=");
		builder.append(endIdx);
		builder.append("]");
		return builder.toString();
	}

	
}
