/**
 * 
 */
package graphdata;

import java.io.Serializable;

/**
 * @author Anh
 *
 */
public class Graph implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3214891055420241269L;
	/**
	 * 
	 */
	
//	public List<Node> addedNodes;
//	public List<Edge> addedEdges;
	public Node[] addedNodes;
	public Edge[] addedEdges;
	public int[] projectIDs = null;
	public int[] projectCounts = null;
	public int count = 0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public void setCount(int count){
		this.count = count;
	}
	public void incCount(){
		count++;
	}	
	public void decCount(){
		count--;
	}
	public void mergeCount(int otherCount){
		count += otherCount;
	}

	@Override
	public String toString() {
		return "Graph [addedNodes=" + addedNodes + ", addedEdges=" + addedEdges
				+ ", count=" + count + "]";
	}

	
	
	
}