/**
 * 
 */
package graphdata;

import java.io.Serializable;


/**
 * @author Anh
 *
 */
public class Node implements Serializable{
	public static short UNKNOWN = 0; 
	public static short ACTION = 1;
	public static short CONTROL = 2;
	public static short DATA = 3;
	/**
	 * 
	 */
	private static final long serialVersionUID = 7577539702091908228L;
	
//	public Edge[] inEdges;
//	public Edge[] outEdges;
	public short nodeRole;
	public int content;
//	public int count;
	
	
	public Node(Edge[] inEdges, Edge[] outEdges, short nodeRole, int nodeContent,
			int count) {
//		this.inEdges = inEdges;
//		this.outEdges = outEdges;
		this.nodeRole = nodeRole;
		this.content = nodeContent;
//		this.count = count;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
//	public void incCount(){
//		count++;
//	}
	
//	public void decCount(){
//		count--;
//	}
//	public void mergeCount(int otherCount){
//		count += otherCount;
//	}

	public boolean roleEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (nodeRole != other.nodeRole)
			return false;
		if (content != other.content)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Node [nodeRole=");
		builder.append(nodeRole);
		builder.append(", content=");
		builder.append(content);
//		builder.append(", count=");
//		builder.append(count);
		builder.append("]");
		return builder.toString();
	}

	public int simpleHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + content;
//		result = prime * result + count;
		result = prime * result + nodeRole;
		return result;
	}

	

//	@Override
//	public String toString() {
//		return "Node [inEdges=" + Arrays.toString(inEdges) + ", outEdges="
//				+ Arrays.toString(outEdges) + ", nodeRole=" + nodeRole
//				+ ", nodeContent=" + content + ", count=" + count + "]";
//	}

		
	
}
