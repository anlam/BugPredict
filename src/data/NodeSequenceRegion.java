/**
 * 
 */
package data;

/**
 * @author Anh
 *
 */
public class NodeSequenceRegion {

	public int startRegion = 0;
	public int endRegion = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public NodeSequenceRegion(int startRegion, int endRegion) {
		this.startRegion = startRegion;
		this.endRegion = endRegion;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeSequenceRegion [startRegion=");
		builder.append(startRegion);
		builder.append(", endRegion=");
		builder.append(endRegion);
		builder.append("]");
		return builder.toString();
	}

	
}
