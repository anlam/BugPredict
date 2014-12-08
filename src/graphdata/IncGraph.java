/**
 * 
 */
package graphdata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import config.GlobalConfig;
import utils.Logger;

/**
 * @author Anh
 *
 */
public class IncGraph extends Graph implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -175583288032926178L;
	
	public IncGraph parentGraph = null;
//	/**
//	 * this value to refer to hash code and index in list of the parent graph
//	 */
//	public long parentGraphIdx = 0;
	
//	ArrayList<IncGraph> tmpSubgraphs = new ArrayList<IncGraph>();
	
//	HashMap<Integer, ArrayList<IncGraph>> subgraphMap = new HashMap<>(); 
	public long graphIdx = 0;
	
	static long staGraphIdx = 0;
	
	
	public static void main(String[] args){
		IncGraph grapha = new IncGraph(null, 1);
	
		Node node1 = new Node(null, null, Node.ACTION, 1, 1);
		grapha.addNode(node1);
		
		Node node2 = new Node(null, null, Node.ACTION, 2, 1);
		grapha.addNode(node2);
		
		Edge edge12 = new Edge(node1, node2, 1);
		grapha.addEdge(edge12);
		

		Logger.log("grapha: " + grapha);
		
		IncGraph graphb = new IncGraph(grapha, 1);
		
		Node node3 = new Node(null, null, Node.ACTION, 3, 1);
		graphb.addNode(node3);
		
		Edge edge13 = new Edge(node1, node3, 1);
		graphb.addEdge(edge13);
		
		Edge edge23 = new Edge(node2, node3, 1);
		graphb.addEdge(edge23);
		
		Node node4 = new Node(null, null, Node.ACTION, 4, 1);
		graphb.addNode(node4);
		
		Edge edge34 = new Edge(node3, node4, 1);
		graphb.addEdge(edge34);
		
		IncGraph graphb2 = graphb.getExpandGraph();
		
		Logger.log("\r\ngraphb: " + graphb2 + "\r\n");
		ArrayList<IncGraph> allSubgraphs = graphb2.getAllSubgraphs();
		for (int i=0; i<allSubgraphs.size();i++){
			IncGraph subgraph = allSubgraphs.get(i);
			Logger.log("\r\nsubgraph " + subgraph.graphIdx + ": " + subgraph.getExpandGraph());
		}
	
		
		IncGraph graphb3 = new IncGraph(graphb, 1);
//		Node node4 = new Node(null, null, Node.ACTION, 4, 1);
//		graphb3.addNode(node4);
//		
//		Edge edge34 = new Edge(node3, node4, 1);
//		graphb3.addEdge(edge34);
		
		Edge edge24 = new Edge(node2, node4, 1);
		graphb3.addEdge(edge24);
		
//		Node node4b = new Node(null, null, Node.ACTION, 4, 1);
//		graphb3.addNode(node4b);
		
		Logger.log("roleEquals? " + graphb3.roleEquals(graphb2));
		
		Logger.log("sequence: " + graphb3.getSequence());
		Logger.log("sequence hashcode: " + graphb3.getSequence().level1HashCode());

		
//		for (int i=5;i<20;i++)
//		{
//			Node nodei = new Node(null, null, Node.ACTION, i, 1);
//			graphb3.addNode(nodei);
//		
//			Edge edge3i = new Edge(node3, nodei, 1);
//			graphb3.addEdge(edge3i);
//		}
//		ArrayList<IncGraph> allSubgraphs = graphb3.getAllSubgraphs();
//		for (int i=0; i<allSubgraphs.size();i++){
//			IncGraph subgraph = allSubgraphs.get(i);
//			Logger.log("\r\nsubgraph " + subgraph.graphIdx + ": " + subgraph.getExpandGraph());
//		}
//		
//		Logger.log("\r\n\r\n\r\n graphb3: " + graphb3 );
//
//		List<IncGraph> incGraphList = graphb3.getIncGraphList();
//		for (IncGraph graph:incGraphList){
//			Logger.log("\r\nincgraph " + graph.graphIdx + ": " + graph );
//		}
	}
	
	public IncGraph(IncGraph parentGraph, int count){
		staGraphIdx++;
		this.graphIdx = staGraphIdx;
		this.parentGraph = parentGraph;
		this.count = count;
	}
	
	/**
	 * Get all subgraphs of a graph, including its own 
	 * @return
	 */
	
	//FIXME: should improve the performance of this step
	public ArrayList<IncGraph> getAllSubgraphs(){
//		tmpSubgraphs.clear();
//		subgraphMap.clear();
		IncGraph expandedGraph = null;
		if (this.parentGraph!=null){
			expandedGraph = this.getExpandGraph();
		}
		else {
			expandedGraph = this.clone();
		}
		
		ArrayList<IncGraph> allSubgraphs = new ArrayList<IncGraph>();
//		Logger.log("expandedGraph : " + expandedGraph);

		List<Node> nodes = Arrays.asList(expandedGraph.addedNodes);
		for (Node node:nodes){
			IncGraph start = new IncGraph(null, 1);			
			start.addNode(node);

			allSubgraphs.addAll(getExtendedGraphs(start, expandedGraph));
			
		}
		
		for (IncGraph graph:allSubgraphs){
			graph.setGraphIdx(staGraphIdx);
			staGraphIdx++;

		}
		return allSubgraphs;
	}
	
	
	private ArrayList<IncGraph> getExtendedGraphs(IncGraph start, IncGraph mainGraph){
		
		ArrayList<IncGraph> extendedGraphs = new ArrayList<IncGraph>();
		boolean shouldAdd = true;
//		int startHash = start.simpleHashCode();
//		for(IncGraph graph:tmpSubgraphs){
//			if (graph.memoryEquals(start)){
//				shouldAdd = false;
//				break;
//			}
//		}
		if (shouldAdd)
		{
			extendedGraphs.add(start);
//			tmpSubgraphs.add(start);
		}
		
		//Very important to limit the size of graph
		if (start.getGraphSize()>=GlobalConfig.maxGroumSize){
//			Logger.log("big Graph");
			return extendedGraphs;
		}
		
		List<Node> nodes = Arrays.asList(start.addedNodes);
				
		for (Edge edge:mainGraph.addedEdges){
			if (nodes.contains(edge.sourceNode)){
				if (!nodes.contains(edge.sinkNode)){
					Node newNode = edge.sinkNode;
//					IncrementalGraph newStart = start.clone();
					IncGraph newStart = new IncGraph(start, count);
					newStart.addNode(newNode);
					
					for (Edge edgem:mainGraph.addedEdges){
						//anhnt: note about ==, not equals - i.e. comparison via memory
						if (edgem.sinkNode == newNode){
//							if (nodes.contains(edgem.sourceNode)&&!edgem.sourceNode.equals(edge.sourceNode))
							if (nodes.contains(edgem.sourceNode))
							{
								newStart.addEdge(edgem);
							}
						}
						else if (edgem.sourceNode == newNode){
//							if (nodes.contains(edgem.sourceNode)&&!edgem.sourceNode.equals(edge.sourceNode))
							if (nodes.contains(edgem.sinkNode))
							{
								newStart.addEdge(edgem);
							}
						}
					}
					ArrayList<IncGraph> newExtendedGraphs = getExtendedGraphs(newStart, mainGraph);
					extendedGraphs.addAll(newExtendedGraphs);
//					tmpSubgraphs.addAll(newExtendedGraphs);
				}
			}
		}
		return extendedGraphs;
	}

	public int getGraphSize(){
//		return this.getExpandGraph().addedNodes.size();
		return this.getExpandGraph().addedNodes.length;

	}
	
	
	
	
	

	public void setParentGraph(IncGraph parentGraph){
		this.parentGraph = parentGraph;
	}
	
	public void setGraphIdx(long graphIdx){
		this.graphIdx = graphIdx;
	}
	
	public void addNode(Node node){
		
//		if (this.addedNodes==null)
//			this.addedNodes = new ArrayList<Node>();
//		this.addedNodes.add(node);
		if (this.addedNodes==null)
		{
			this.addedNodes = new Node[1];
			this.addedNodes[0] = node;
		}
		else
		{
			int length = this.addedNodes.length;
			Node[] tmp = Arrays.copyOf(this.addedNodes, length);
			this.addedNodes = new Node[length+1];
			for (int i=0; i<length;i++)
			{
				this.addedNodes[i] = tmp[i];
			}
			this.addedNodes[length] = node;
		}
	}
	
	public void addEdge(Edge edge){
//		if (this.addedEdges==null)
//			this.addedEdges = new ArrayList<Edge>();
//		this.addedEdges.add(edge);
		
		if (this.addedEdges==null)
		{
			this.addedEdges = new Edge[1];
			this.addedEdges[0] = edge;
		}
		else
		{
			int length = this.addedEdges.length;
			Edge[] tmp = Arrays.copyOf(this.addedEdges, length);
			this.addedEdges = new Edge[length+1];
			for (int i=0; i<length;i++)
			{
				this.addedEdges[i] = tmp[i];
			}
			this.addedEdges[length] = edge;
		}
	}
	
	public void addProjectIndex(int projectID, int count){
		int idx = -1;
		if (projectIDs==null){
			projectIDs = new int[1];
			projectIDs[0] = projectID;
			projectCounts = new int[1];
			projectCounts[0] = count;
		}
		else if ((Arrays.binarySearch(projectIDs,projectID))>=0){
			//do nothing
			idx = Arrays.binarySearch(projectIDs,projectID);
			projectCounts[idx]+=count;
		}
		else {
			int oldLen = projectIDs.length;
			int[] tmp = Arrays.copyOf(projectIDs, oldLen);
			projectIDs = new int[oldLen+1];
			for (int i=0;i<oldLen;i++){
				projectIDs[i]=tmp[i];
			}
			projectIDs[oldLen] = projectID;
			
			int[] tmp1 = Arrays.copyOf(projectCounts, oldLen);
			projectCounts = new int[oldLen+1];
			for (int i=0;i<oldLen;i++){
				projectCounts[i]=tmp1[i];
			}
			projectCounts[oldLen]=count;

		}
	}
	
	public void updateCount(int count, int[] projectIDs, int[] projectCounts){
		this.count += count;
		for (int i=0;i<projectIDs.length;i++){
			addProjectIndex(projectIDs[i], projectCounts[i]);
		}
	}
	
	
	
	/**
	 * Clone graph
	 */
	public IncGraph clone(){
		IncGraph graph = new IncGraph(this.parentGraph, this.count);
		if (this.addedNodes!=null)
		{
//			graph.addedNodes = new ArrayList<Node>();
//			graph.addedNodes.addAll(this.addedNodes);
			graph.addedNodes = Arrays.copyOf(this.addedNodes, this.addedNodes.length);
		}
		if (this.addedEdges!=null){
//			graph.addedEdges = new ArrayList<Edge>();
//			graph.addedEdges.addAll(this.addedEdges);
			graph.addedEdges = Arrays.copyOf(this.addedEdges, this.addedEdges.length);
		}
//		staGraphIdx++;
//		graph.graphIdx = staGraphIdx;
		return graph;
	}

	
	/**
	 * Expand an incgraph to a graph that contains all nodes and edges, without parents
	 * @return
	 */
	public IncGraph getExpandGraph(){
		if (this.parentGraph==null){
			return this;
		}
		IncGraph curGraph = this.clone();
		getRecursiveExpandedGraph(curGraph);
		return curGraph;
	}
	
	private void getRecursiveExpandedGraph(IncGraph curGraph){
		
		if (curGraph.parentGraph==null){
			
		}
		else
		{
//			List<Node> nodes = new ArrayList<Node>();
//			nodes.addAll(curGraph.parentGraph.addedNodes);
//			if (curGraph.addedNodes!=null)
//				nodes.addAll(curGraph.addedNodes);
//			List<Edge> edges = new ArrayList<Edge>();
//			if (curGraph.parentGraph.addedEdges!=null)
//				edges.addAll(curGraph.parentGraph.addedEdges);
//			if (curGraph.addedEdges!=null)
//				edges.addAll(curGraph.addedEdges);
//			curGraph.addedNodes = nodes;
//			curGraph.addedEdges = edges;
			
			List<Node> nodes = new ArrayList<Node>();
			nodes.addAll(Arrays.asList(curGraph.parentGraph.addedNodes));
			if (curGraph.addedNodes!=null)
				nodes.addAll(Arrays.asList(curGraph.addedNodes));
			List<Edge> edges = new ArrayList<Edge>();
			if (curGraph.parentGraph.addedEdges!=null)
				edges.addAll(Arrays.asList(curGraph.parentGraph.addedEdges));
			if (curGraph.addedEdges!=null)
				edges.addAll(Arrays.asList(curGraph.addedEdges));
			curGraph.addedNodes = nodes.toArray(new Node[]{});
			curGraph.addedEdges = edges.toArray(new Edge[]{});
			
			curGraph.parentGraph = curGraph.parentGraph.parentGraph; 
			getRecursiveExpandedGraph(curGraph);
		}
	}

	
	/**
	 * Given a graph G, get a list of graphs include G and all of its ascendants 
	 * @return
	 */
	
	public List<IncGraph> getIncGraphList(){
		List<IncGraph> incGraphList = new ArrayList<IncGraph>();
		IncGraph parent = this;
		while(parent!=null){
			incGraphList.add(parent);
			parent = parent.parentGraph; 
		}
		return incGraphList;
	}

	/**
	 * Compare if two graph contains same nodes and edges, via their memory location
	 * @param other
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean memoryEquals(IncGraph other){
		IncGraph graphExp1 = this.getExpandGraph();
		IncGraph graphExp2 = other.getExpandGraph();
		return graphExp1.memoryExpandedEquals(graphExp2);
	}
	private boolean memoryExpandedEquals( IncGraph other){
		boolean isEqualNodes = false;
		if (Arrays.asList(this.addedNodes).containsAll(Arrays.asList(other.addedNodes))
				&&Arrays.asList(other.addedNodes).containsAll(Arrays.asList(this.addedNodes))){
			isEqualNodes = true;
		}
		
		boolean isEqualEdges = false;
		if (this.addedEdges==null&&other.addedEdges==null){
			isEqualEdges = true;
		}
		else if (this.addedEdges==null){
			isEqualEdges = false;
		}
		else if (other.addedEdges==null){
			isEqualEdges = false;
		}
		else if (Arrays.asList(this.addedEdges).containsAll(Arrays.asList(other.addedEdges))
				&&Arrays.asList(other.addedEdges).containsAll(Arrays.asList(this.addedEdges))){
			isEqualEdges = true;
		}
		
		
		return isEqualNodes&&isEqualEdges;
	}
	
	/**
	 * Compare if two graph have nodes and edges with same roles (two graphs are equal)
	 * @param other
	 * @return
	 */
	public boolean roleEquals(IncGraph other){
		IncGraph expandThis = this.getExpandGraph();
		IncGraph expandOther = other.getExpandGraph();
		//Nodes
//		int nodeListSize = expandThis.addedNodes.size();
//		if (nodeListSize!=expandOther.addedNodes.size())
//			return false;
//		else 
//			for (int i=0; i<nodeListSize;i++){
//				if (!expandThis.addedNodes.get(i).roleEquals(expandOther.addedNodes.get(i)))
//					return false;
//			}

		int nodeListSize = expandThis.addedNodes.length;
		if (nodeListSize!=expandOther.addedNodes.length)
			return false;
		else 
			for (int i=0; i<nodeListSize;i++){
				if (!expandThis.addedNodes[i].roleEquals(expandOther.addedNodes[i]))
					return false;
			}
		
//		Logger.log("nodes OK!!!!!!!!!!!!!!!");
		//Edges
		if (expandThis.addedEdges==null){
			if (expandOther.addedEdges==null){
				return true;
			}
			else {
				return false;
			}
		}
		else if (expandOther.addedEdges==null){
			return false;
		}
		else {
//			int edgeListSize = expandThis.addedEdges.size();
//			if (edgeListSize!=expandOther.addedEdges.size())
//				return false;
//			else
//				for (int i=0; i<edgeListSize; i++){
//					if (!expandThis.addedEdges.get(i).roleEquals(expandOther.addedEdges.get(i))){
//						return false;
//					}
//				}
			int edgeListSize = expandThis.addedEdges.length;
			if (edgeListSize!=expandOther.addedEdges.length)
				return false;
			else
				for (int i=0; i<edgeListSize; i++){
					if (!expandThis.addedEdges[i].roleEquals(expandOther.addedEdges[i])){
						return false;
					}
				}
		}
		
		return true;
	}
	
//	@SuppressWarnings("unused")
//	private int simpleHashCode() {
//		int result = 0;
//		for (Node node:addedNodes){
//			result += node.hashCode();
//		}
//
//		return result;
//	}

	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IncrementalGraph [parentGraph idx=");
		if (parentGraph!=null)
			builder.append(parentGraph.graphIdx);
		else
			builder.append("null");
		builder.append("\r\n, addedNodes=");
//		builder.append(addedNodes);
		if (addedNodes!=null)
			for (Node node:addedNodes){
				builder.append("\r\n" + node);
			}
		else
			builder.append(addedNodes);
		builder.append("\r\n, addedEdges=");
//		builder.append(addedEdges);
		if (addedEdges!=null)
			for (Edge edge:addedEdges){
				builder.append("\r\n" + edge );
			}
		else
			builder.append(addedEdges);
		builder.append(", count=");
		builder.append(count);
		builder.append("]");
		return builder.toString();
	}
	
	public ArrayList<Integer> getProjectIdxList(){
		ArrayList<Integer> projectIdxList = new ArrayList<>(projectIDs.length);
		for (int projectID:projectIDs){
			projectIdxList.add(projectID);
		}
		return projectIdxList;
	}

	
	public Sequence getSequence(){
//		return new Sequence(this.getExpandGraph().addedNodes, this.count);
		return new Sequence(Arrays.asList(this.getExpandGraph().addedNodes), this.count);

	}
	
	
}
