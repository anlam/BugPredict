/**
 * 
 */
package graphdata;

import java.io.Serializable;

/**
 * @author Anh
 *
 */
public class Edge implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2589876235143169628L;
	
	public Node sourceNode;
	public Node sinkNode;
//	public int count;
	
	public Edge(Node sourceNode, Node sinkNode, int count) {
		this.sourceNode = sourceNode;
		this.sinkNode = sinkNode;
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
//	
//	public void decCount(){
//		count--;
//	}
//	public void mergeCount(int otherCount){
//		count += otherCount;
//	}

	@Override
	public String toString() {
		return "Edge [sourceNode=" + sourceNode + ", sinkNode=" + sinkNode
//				+ ", count=" + count 
				+ "]";
	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result
//				+ ((sinkNode == null) ? 0 : sinkNode.hashCode());
//		result = prime * result
//				+ ((sourceNode == null) ? 0 : sourceNode.hashCode());
//		return result;
//	}

	public boolean roleEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (sinkNode == null) {
			if (other.sinkNode != null)
				return false;
		} else if (!sinkNode.roleEquals(other.sinkNode))
			return false;
		if (sourceNode == null) {
			if (other.sourceNode != null)
				return false;
		} else if (!sourceNode.roleEquals(other.sourceNode))
			return false;
		return true;
	}

	
}
