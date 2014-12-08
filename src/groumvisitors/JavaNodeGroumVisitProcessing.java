/**
 * 
 */
package groumvisitors;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import data.ControlInfo;
import data.MethodInfo;
import data.MethodInvocInfo;
import data.NodeInfo;
import data.TypeInfo;
import data.VariableInfo;


import recoder.java.JavaProgramElement;



/**
 * @author ANH
 *
 */
public class JavaNodeGroumVisitProcessing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


	}

	public static boolean isPrimitiveType(String typeStr){
		String[] primitiveList = {
						  "boolean"
						, "char"
						, "byte"
						, "short"
						, "int"
						, "long"
						, "float"
						, "double" 
						
		
		};
		
		boolean isPrimitive = false;
		String tmpTypeStr = typeStr.trim();
		for (String tmp:primitiveList){
			if(tmp.equals(tmpTypeStr)){
				isPrimitive = true;
				break;
			}
		}
		return isPrimitive;
	}

	public static NodeInfo addNewFieldAccessNode(String varName, String typeVarName, 
			String fieldName, String typeFieldName,
			MethodInfo curMethodInfo, Stack< NodeInfo> parentNodeStack, 
			Stack<NodeInfo> previousControlFlowNodeStack , 
			Stack<NodeInfo> previousDataNodeStack ,
			LinkedHashMap<String, NodeInfo> previousDataNodeMap,
			long curID, JavaProgramElement n,
			ArrayList<Long> scopeList
			){

		if (!Configurations.includePrimitiveFieldType)
		{
			if (isPrimitiveType(typeFieldName)){
				return null;
			}
		}

		long ID = curID;
		int nodeType = NodeInfo.DATA_TYPE;
		MethodInfo containingMethod = null;
		if (curMethodInfo!=null){
			containingMethod = curMethodInfo;
		}

		NodeInfo parentNode = null;

		Object nodeContent = null;

		NodeInfo nodeInfo = null;

		ArrayList<NodeInfo> previousControlNodes = new ArrayList<NodeInfo>();
		
		ArrayList<NodeInfo> previousDataNodes = null;

		nodeInfo = new NodeInfo(nodeType, ID + Configurations.dataShift, containingMethod, parentNode, nodeContent, 
				previousControlNodes, previousDataNodes);

		nodeInfo.nodeContent = new VariableInfo(fieldName, typeFieldName, scopeList); 		
		
		String combineName = combineVarName(typeFieldName, fieldName);

		if (previousDataNodeMap.containsKey(combineName))
		{
			NodeInfo tmp = previousDataNodeMap.get(combineName);

			VariableInfo varInfo = (VariableInfo)(tmp.nodeContent);
			ArrayList<Long> varScopeList = varInfo.scopeList;
			boolean isAcceptedScope = true;
			if (varScopeList.size()>scopeList.size())
			{
				isAcceptedScope = false;
			}
			else {
				for (int i=0; i<varScopeList.size(); i++){
					if (scopeList.get(i)!=varScopeList.get(i)){
						isAcceptedScope = false;
						break;
					}
				}
			}
			if (isAcceptedScope)
			{
				NodeInfo.previousDataNodesTmp.add(tmp);
			}
			else
			{
				
				previousDataNodeMap.put(combineName,nodeInfo);
				if (curMethodInfo!=null)
				{
					if (curMethodInfo.dataNodeList==null){
						curMethodInfo.dataNodeList = new ArrayList<NodeInfo>(1);
					}
					curMethodInfo.dataNodeList.add(nodeInfo);
				}
			}
	

		}
		else
		{
			previousDataNodeMap.put(combineName, nodeInfo);
			if (curMethodInfo!=null)
			{
				if (curMethodInfo.dataNodeList==null){
					curMethodInfo.dataNodeList = new ArrayList<NodeInfo>(1);
				}
				curMethodInfo.dataNodeList.add(nodeInfo);
			}
			
		}
		nodeInfo.synchronizeControlDataNodes();

		
		int nodeControlType = NodeInfo.CONTROL_TYPE;
		NodeInfo parentNodeControl = null;
//		Object nodeControlContent = new VariableInfo(fieldName, typeFieldName, scopeList);
		Object nodeControlContent = new VariableInfo(fieldName, typeVarName, scopeList); 		

		NodeInfo nodeControlInfo = null;
		ArrayList<NodeInfo> previousDataNodesControl = new ArrayList<NodeInfo>();
		previousDataNodesControl.add(previousDataNodeMap.get(combineName));
		
		ArrayList<NodeInfo> previousControlNodesControl = new ArrayList<NodeInfo>();

		if (!parentNodeStack.isEmpty()){
			parentNodeControl = parentNodeStack.peek();
		}
		
		if(previousControlFlowNodeStack.size()>0)
			previousControlNodesControl.add(previousControlFlowNodeStack.peek());
		
		nodeControlInfo = new NodeInfo(nodeControlType, ID, containingMethod, parentNodeControl, nodeControlContent, 
				previousControlNodesControl, previousDataNodesControl);
		
		if (curMethodInfo!=null)
		{
			if (curMethodInfo.controlNodeList==null){
				curMethodInfo.controlNodeList = new ArrayList<NodeInfo>(1);
			}
			curMethodInfo.controlNodeList.add(nodeControlInfo);
			
		}	
		previousControlFlowNodeStack.push(nodeControlInfo);
		
		
		nodeControlInfo.synchronizeControlDataNodes();
		return nodeControlInfo;
	}

	

	



	//TODO: should check this
	public static NodeInfo addVarNode(String varName, String typeName, 
			MethodInfo curMethodInfo, Stack< NodeInfo> parentNodeStack, 
			Stack<NodeInfo> previousControlFlowNodeStack , 
			Stack<NodeInfo> previousDataNodeStack ,
			LinkedHashMap<String, NodeInfo> previousDataNodeMap,
			long curID, JavaProgramElement n,
			ArrayList<Long> scopeList,
			boolean isAddToDataMap
			
			){

		if (!Configurations.includePrimitiveVarType)
		{
			if (isPrimitiveType(typeName)){
				return null;
			}
		}
		

		long ID = curID;
		int nodeType = NodeInfo.DATA_TYPE;
		MethodInfo containingMethod = null;
		if (curMethodInfo!=null){
			containingMethod = curMethodInfo;
		}

		NodeInfo parentNode = null;
		
		Object nodeContent = null;

		NodeInfo nodeInfo = null;

		ArrayList<NodeInfo> previousControlNodes = new ArrayList<NodeInfo>();
		
		ArrayList<NodeInfo> previousDataNodes = new ArrayList<NodeInfo>();

		nodeInfo = new NodeInfo(nodeType, ID + Configurations.dataShift, containingMethod, parentNode, nodeContent, 
				previousControlNodes, previousDataNodes);

		nodeInfo.nodeContent = new VariableInfo(varName, typeName, scopeList); 		
		
		String combineName = combineVarName(typeName, varName);

		boolean isAddedToDataList = true;
		
		if (!Configurations.includePrimitiveVarType)
			if (isPrimitiveType(typeName))
				isAddedToDataList= false;
		
		if (previousDataNodeMap.containsKey(combineName))
		{
			NodeInfo tmp = previousDataNodeMap.get(combineName);

			VariableInfo varInfo = (VariableInfo)(tmp.nodeContent);
			ArrayList<Long> varScopeList = varInfo.scopeList;
			boolean isAcceptedScope = true;
			if (varScopeList.size()>scopeList.size())
			{
				isAcceptedScope = false;
			}
			else {
				for (int i=0; i<varScopeList.size(); i++){
					if (scopeList.get(i)!=varScopeList.get(i)){
						isAcceptedScope = false;
						break;
					}
				}
			}
			if (isAcceptedScope)
			{
				NodeInfo.previousDataNodesTmp.add(tmp);
			}
			else
			{
				if(isAddedToDataList)
				{
					previousDataNodeMap.put(combineName,nodeInfo);
					if (curMethodInfo!=null)
					{
						if (curMethodInfo.dataNodeList==null){
							curMethodInfo.dataNodeList = new ArrayList<NodeInfo>(1);
						}
						curMethodInfo.dataNodeList.add(nodeInfo);
					}
				}
			}
		}
		else{
			if(isAddedToDataList)
			{
				previousDataNodeMap.put(combineName, nodeInfo);
				
				if (curMethodInfo!=null)
				{
					if (curMethodInfo.dataNodeList==null){
						curMethodInfo.dataNodeList = new ArrayList<NodeInfo>(1);
					}
					curMethodInfo.dataNodeList.add(nodeInfo);
				}
			}
		}

		nodeInfo.synchronizeControlDataNodes();
		
		if (isAddedToDataList&&Configurations.isAddVarToControl)
		{
			int nodeControlType = NodeInfo.CONTROL_TYPE;
			NodeInfo parentNodeControl = null;
			Object nodeControlContent =  new VariableInfo(varName, typeName, scopeList); 		
			if (!parentNodeStack.isEmpty()){
				parentNodeControl = parentNodeStack.peek();
			}
			ArrayList<NodeInfo> previousDataNodesControl = new ArrayList<NodeInfo>();
			previousDataNodesControl.add(previousDataNodeMap.get(combineName));
			
			ArrayList<NodeInfo> previousControlNodesControl = new ArrayList<NodeInfo>();
			
			if (previousControlFlowNodeStack.size()>0)
				previousControlNodesControl.add(previousControlFlowNodeStack.peek());
			
			NodeInfo nodeControlInfo = new NodeInfo(nodeControlType, ID, containingMethod, parentNodeControl, 
					nodeControlContent, 
					previousControlNodesControl, previousDataNodesControl);
			
			if (curMethodInfo!=null)
			{
				if (curMethodInfo.controlNodeList==null){
					curMethodInfo.controlNodeList = new ArrayList<NodeInfo>(1);
				}
				curMethodInfo.controlNodeList.add(nodeControlInfo);
			}	
			previousControlFlowNodeStack.push(nodeControlInfo);
			return nodeControlInfo;
		}
		else
		{
			return null;
		}
	}
	
	public static String combineVarName(String typeName, String varName){
		return typeName + "#" + varName;
	}
	public static NodeInfo addNewControlNode(MethodInfo curMethodInfo, Stack< NodeInfo> parentNodeStack, 
			Stack<NodeInfo> previousControlFlowNodeStack , long curID, int ControlType,
			ArrayList<Long> scopeList
			){

		long ID = curID;
		int nodeType = NodeInfo.CONTROL_TYPE;
		MethodInfo containingMethod = null;
		if (curMethodInfo!=null){
			containingMethod = curMethodInfo;
		}
		//fileInfo.numIfControls++;

		NodeInfo parentNode = null;
		if (!parentNodeStack.isEmpty()){
			parentNode = parentNodeStack.peek();
		}

		ControlInfo nodeContent = new ControlInfo(ControlType);
		ArrayList<NodeInfo> previousControlNodes = null;
		if (previousControlFlowNodeStack.size()>0)
		{	
			previousControlNodes = new ArrayList<NodeInfo>();
			previousControlNodes.add(previousControlFlowNodeStack.peek());
		}

		ArrayList<NodeInfo> previousDataNodes =  new ArrayList<NodeInfo>();

		NodeInfo nodeInfo = new NodeInfo(nodeType, ID, containingMethod, parentNode, nodeContent, 
				previousControlNodes, previousDataNodes);

		if (curMethodInfo!=null)
		{
			if (curMethodInfo.controlNodeList==null){
				curMethodInfo.controlNodeList = new ArrayList<NodeInfo>(1);
			}
			curMethodInfo.controlNodeList.add(nodeInfo);
		}		

		parentNodeStack.push(nodeInfo);

		ArrayList<NodeInfo> newPreviousControlNodes = new ArrayList<NodeInfo>();
		newPreviousControlNodes.add(nodeInfo);
		previousControlFlowNodeStack.push(nodeInfo);

//		if (parentNode!=null)
//		{
//			parentNode.synchronizeControlDataNodes();
//		}
		nodeInfo.synchronizeControlDataNodes();
		return nodeInfo;
	}

	public static void removeControlNodeInfo( NodeInfo nodeInfo , Stack< NodeInfo> parentNodeStack,
			Stack<NodeInfo> previousControlFlowNodeStack ){
		if (parentNodeStack.size()>0)
		parentNodeStack.pop();

		//		if (previousControlFlowNodeStack.size()==0){
		//			return;
		//		}
		//			
		//		
		//		boolean isFound = false;
		//		for (ArrayList<NodeInfo> nodeList:previousControlFlowNodeStack){
		//			if (nodeList.get(0).ID==nodeInfo.ID){
		//				isFound = true;
		//				break;
		//			}
		//		}
		//		
		//		if (isFound)
		//		{
		//			while(previousControlFlowNodeStack.peek().get(0).ID!=nodeInfo.ID){
		//				previousControlFlowNodeStack.pop();
		//
		//			}
		//		}
	}
	public static NodeInfo addNewInvocNode(TypeInfo typeInfo, MethodInfo curMethodInfo, 
			Stack< NodeInfo> parentNodeStack, 
			Stack<NodeInfo> previousControlFlowNodeStack , 
			Stack<NodeInfo> previousDataNodeStack ,
			LinkedHashMap<String, NodeInfo> previousDataNodeMap,
			long curID, MethodInvocInfo methodInvoc,
			JavaProgramElement n,
			ArrayList<Long> scopeList,
			ArrayList<Long> classScopeList,
			Map<String, String> varTypeMap

			){
		if (methodInvoc.typeName!=null)
		{
			if (methodInvoc.typeName.contains("<unknownClassType>")){
				if(varTypeMap.containsKey(methodInvoc.varName)){
					if (varTypeMap.get(methodInvoc.varName).trim().length()>0)
						methodInvoc.typeName = varTypeMap.get(methodInvoc.varName);
				}
			}
		}
		long ID = curID;
		int nodeType = NodeInfo.METHODINVOC_TYPE;
		MethodInfo containingMethod = null;
		if (curMethodInfo!=null){
			containingMethod = curMethodInfo;
		}
		//fileInfo.numIfControls++;

		NodeInfo parentNode = null;
		if (!parentNodeStack.isEmpty()){
			parentNode = parentNodeStack.peek();
		}

		MethodInvocInfo nodeContent = methodInvoc;


		NodeInfo nodeInfo = null;

		ArrayList<NodeInfo> previousControlNodes = new ArrayList<NodeInfo>();
		if(previousControlFlowNodeStack.size()>0)
			previousControlNodes.add(previousControlFlowNodeStack.peek());
		
		ArrayList<NodeInfo> previousDataNodes =  new ArrayList<NodeInfo>();

		nodeInfo = new NodeInfo(nodeType, ID, containingMethod, parentNode, nodeContent, 
				previousControlNodes, previousDataNodes);

		if (curMethodInfo!=null)
		{
			if (curMethodInfo.controlNodeList==null){
				curMethodInfo.controlNodeList = new ArrayList<NodeInfo>(1);
			}
			curMethodInfo.controlNodeList.add(nodeInfo);
		}	
		previousControlFlowNodeStack.push(nodeInfo);

//		parentNodeStack.push(nodeInfo);

		String combineName = combineVarName(methodInvoc.typeName, methodInvoc.varName);
//		Logger.logDebug("Invoc combineName: " + combineName);
		if (previousDataNodeMap.containsKey(combineName))
		{
			NodeInfo tmp = previousDataNodeMap.get(combineName);
			VariableInfo varInfo = (VariableInfo)(tmp.nodeContent);
			ArrayList<Long> varScopeList = varInfo.scopeList;
			boolean isAcceptedScope = true;
			if (varScopeList.size()>scopeList.size())
			{
				isAcceptedScope = false;
			}
			else {
				for (int i=0; i<varScopeList.size(); i++){
					if (scopeList.get(i)!=varScopeList.get(i)){
						isAcceptedScope = false;
						break;
					}
				}
			}
			if (isAcceptedScope)
			{
				NodeInfo.previousDataNodesTmp.add(tmp);
			}
			
		}
		else{
			
			int nodeDataType = NodeInfo.DATA_TYPE;
			
			NodeInfo parentNodeData = null;
			
			//If a call of method in current class
			Object nodeDataContent = null;
			
			String tmpTypeName ="";
			if (typeInfo != null)
			{
				if(typeInfo.typeName!=null)
					tmpTypeName = typeInfo.typeName;
			}
			
			String methodTypeName = "@";
			if (methodInvoc.typeName!=null)
			{
				methodTypeName = methodInvoc.typeName;
			}
			if (methodTypeName.equals(tmpTypeName))
			{
				 nodeDataContent = new VariableInfo(methodInvoc.varName, methodInvoc.typeName, classScopeList);
			}
			else
			{
				 nodeDataContent = new VariableInfo(methodInvoc.varName, methodInvoc.typeName, scopeList);

			}

			NodeInfo nodeDataInfo = null;

			ArrayList<NodeInfo> previousControlNodesData = new ArrayList<NodeInfo>();
			
			ArrayList<NodeInfo> previousDataNodesData = new ArrayList<NodeInfo>();
			
			nodeDataInfo = new NodeInfo(nodeDataType, ID + Configurations.dataShift, containingMethod, parentNodeData, 
					nodeDataContent ,  previousControlNodesData, previousDataNodesData);
			
			if (curMethodInfo!=null)
			{
				if (curMethodInfo.dataNodeList==null){
					curMethodInfo.dataNodeList = new ArrayList<NodeInfo>(1);
				}
				curMethodInfo.dataNodeList.add(nodeDataInfo);
			}
			
			nodeDataInfo.synchronizeControlDataNodes();
			
			NodeInfo.previousDataNodesTmp.add(nodeDataInfo);
			
			previousDataNodeMap.put(combineName, nodeDataInfo);
		}

		nodeInfo.synchronizeControlDataNodes();
		return nodeInfo;
	}

	public static void removeInvocNodeInfo( JavaProgramElement n,  NodeInfo nodeInfo , Stack< NodeInfo> parentNodeStack,
			Stack<NodeInfo> previousControlFlowNodeStack ){

		//		Logger.log("cur MethodExpr: " + n);
		//		Logger.log("parentNodeStack: " + parentNodeStack);
		//		Logger.log("parentNodeStack size: " + parentNodeStack.size());


//		parentNodeStack.pop();


		//		while(previousControlFlowNodeStack.peek().get(0).ID!=nodeInfo.ID){
		//			previousControlFlowNodeStack.pop();
		//		}
	}

}
