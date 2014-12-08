/**
 * 
 */
package graphdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import data.MethodInfo;
import data.NodeInfo;

/**
 * @author Anh
 *
 */
public class DataUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static void compactGroum(MethodInfo methodInfo){
		ArrayList<NodeInfo> controlNodeList = new ArrayList<>(); 
		if (methodInfo.controlNodeList==null){
			return;
		}
		controlNodeList.addAll(	methodInfo.controlNodeList);
		Map<NodeInfo, NodeInfo> removedNodes = new LinkedHashMap<NodeInfo, NodeInfo>();
		for (NodeInfo node:controlNodeList){
			boolean isFoundPrevious =false;
			if (node.previousControlNodes!=null)
			{
//				Logger.log("\tfound:" + node);

				for (NodeInfo previousNode:node.previousControlNodes){
					if(previousNode.toStringSimple().equals(node.toStringSimple())){
//						Logger.log("\tfound:" + node);
						if (removedNodes.containsKey(previousNode))
							removedNodes.put(node, removedNodes.get(previousNode));
						else
							removedNodes.put(node, previousNode);
						isFoundPrevious = true;
						break;
					}
				}
			}
			if (!isFoundPrevious) 
				removedNodes.put(node, node);
		}
		ArrayList<NodeInfo> newControlNodeList = new ArrayList<>();
		for (NodeInfo node:controlNodeList) {
			if (removedNodes.get(node)==node) {
				newControlNodeList.add(node);
			}
			else 
			{
				NodeInfo mainNode = removedNodes.get(node);
				if (node.previousControlNodes!=null){
					for (NodeInfo prevNode:node.previousControlNodes)
						if (!mainNode.isContainPrevControlNode(prevNode))
							mainNode.addPrevControlNode(prevNode);
				}
				if (node.previousDataNodes!=null){
					for (NodeInfo prevNode:node.previousDataNodes)
						if (!mainNode.isContainPrevDataNode(prevNode))
							mainNode.addPrevDataNode(prevNode);
				}
				if (!newControlNodeList.contains(mainNode)){
					newControlNodeList.add(mainNode);
				}
			}
		}
		methodInfo.controlNodeList =newControlNodeList;
	}
	public static IncGraph convertGroumToGraph(MethodInfo methodInfo, IncGraphDB graphDB){
		Map<NodeInfo, Node> nodeInfoMap =  new HashMap<NodeInfo, Node>(); 
		List<Node> nodes = new ArrayList<Node>();
		List<Edge> edges = new ArrayList<Edge>();
		
		ArrayList<NodeInfo> allNodeList = new ArrayList<NodeInfo>(); 
		if (methodInfo.controlNodeList!=null)
			allNodeList.addAll(methodInfo.controlNodeList);
		if (methodInfo.dataNodeList!=null)
			allNodeList.addAll(methodInfo.dataNodeList);
		for (NodeInfo nodeInfo:allNodeList){
			short nodeRole = Node.UNKNOWN;
			int nodeType = nodeInfo.nodeType;
			if(nodeType==NodeInfo.ASSIGN_TYPE||nodeType==NodeInfo.METHODINVOC_TYPE){
				nodeRole = Node.ACTION;
			}
			else if (nodeType == NodeInfo.CONTROL_TYPE){
				nodeRole = Node.CONTROL; 
			}
			else if (nodeType == NodeInfo.DATA_TYPE){
				nodeRole = Node.DATA;
			}
			String nodeContentStr = nodeInfo.getContentString();
			int count = 1;
			Node node = new Node(null, null, nodeRole, graphDB.getTokenIdx(nodeContentStr), count);
			nodes.add(node);
			nodeInfoMap.put(nodeInfo, node);
		}
		
		for (NodeInfo nodeInfo:allNodeList){
			Node curNode = nodeInfoMap.get(nodeInfo);
			if (nodeInfo.previousControlNodes!=null)
			{
				for (NodeInfo previousControlNode:nodeInfo.previousControlNodes){
					Node previousNode = nodeInfoMap.get(previousControlNode);
					Edge edge = new Edge(previousNode, curNode, 1);
					edges.add(edge);
				}
			}
			if (nodeInfo.previousDataNodes!=null)
			{
				for (NodeInfo previousDataNode:nodeInfo.previousDataNodes){
					Node previousNode = nodeInfoMap.get(previousDataNode);
					Edge edge = new Edge(previousNode, curNode, 1);
					edges.add(edge);
				}
			}
		}
		IncGraph graph = new IncGraph(null, 1);
		
//		graph.addedNodes = new ArrayList<Node>();
//		graph.addedNodes.addAll(nodes);
		graph.addedNodes = nodes.toArray(new Node[]{});
		
//		graph.addedEdges = new ArrayList<Edge>();
//		graph.addedEdges.addAll(edges);
		graph.addedEdges = edges.toArray(new Edge[]{});
		
		return graph;
	}
}
