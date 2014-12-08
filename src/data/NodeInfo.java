/**
 * 
 */
package data;

import groumvisitors.Configurations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;



/**
 * @author ANH
 *
 */
public class NodeInfo {
	
	public static final int METHODINVOC_TYPE = 0;
	public static final int CONTROL_TYPE = 1;
	public static final int DATA_TYPE = 2;
	public static final int ASSIGN_TYPE = 3;

	
	public int nodeType = METHODINVOC_TYPE; 
	public long ID = 0;
	
	public MethodInfo containingMethod;
	public NodeInfo parentNode;
	
	public Object nodeContent;
	
	public NodeInfo[] previousControlNodes;
	public NodeInfo[] previousDataNodes;

	public static LinkedHashSet<NodeInfo> previousControlNodesTmp = new LinkedHashSet<NodeInfo>(1);
	public static LinkedHashSet<NodeInfo> previousDataNodesTmp = new LinkedHashSet<NodeInfo>(1);

	public NodeInfo() {
	}
	
	

	public NodeInfo(int nodeType, long ID, MethodInfo containingMethod,
			NodeInfo parentNode, Object nodeContent,
			ArrayList<NodeInfo> previousControlNodes,
			ArrayList<NodeInfo> previousDataNodes) {
		super();
		previousControlNodesTmp.clear();
		previousDataNodesTmp.clear();
		this.nodeType = nodeType;
		this.ID = ID;
		this.containingMethod = containingMethod;
		this.parentNode = parentNode;
		this.nodeContent = nodeContent;
		
		if (previousControlNodes!=null)
		{
			previousControlNodesTmp.addAll(previousControlNodes);
		}
		if (previousDataNodes!=null)
		{
			previousDataNodesTmp.addAll(previousDataNodes);
		}
		synchronizeControlDataNodes();
	}

	public boolean isContainPrevControlNode(NodeInfo node){
		if (previousControlNodes == null)
			return false;
		for (NodeInfo prevNode:previousControlNodes){
			if (prevNode == node)
				return true;
		}
		return false;
	}
	
	public boolean isContainPrevDataNode(NodeInfo node){
		if (previousDataNodes == null)
			return false;
		for (NodeInfo prevNode:previousDataNodes){
			if (prevNode == node)
				return true;
		}
		return false;
	}
	
	public void addPrevControlNode(NodeInfo node){
		if (previousControlNodes==null){
			previousControlNodes = new NodeInfo[1];
			previousControlNodes[0] = node;
			return;
		}
		int len = previousControlNodes.length;
		NodeInfo[] tmp = Arrays.copyOf(previousControlNodes, len);
		NodeInfo[] tmp2 = new NodeInfo[len+1];
		for (int i=0; i<len; i++)
			tmp2[i] = tmp[i];
		tmp2[len] = node ;
		previousControlNodes = tmp2;
	}
	
	public void addPrevDataNode(NodeInfo node){
		if (previousDataNodes==null){
			previousDataNodes = new NodeInfo[1];
			previousDataNodes[0] = node;
			return;
		}
		int len = previousDataNodes.length;
		NodeInfo[] tmp = Arrays.copyOf(previousDataNodes, len);
		NodeInfo[] tmp2 = new NodeInfo[len+1];
		for (int i=0; i<len; i++)
			tmp2[i] = tmp[i];
		tmp2[len] = node ;
		previousDataNodes = tmp2;
	}
	
	

	public void synchronizeControlDataNodes(){
		synchronizeControlNodes();
		synchronizeDataNodes();
	}

	public void synchronizeControlNodes(){
		if ((previousControlNodesTmp!=null)&&(previousControlNodesTmp.size()>0)){
			previousControlNodes = new NodeInfo[previousControlNodesTmp.size()];
			int i =0;
			for (NodeInfo node: previousControlNodesTmp){
				previousControlNodes[i] = node;
				i++;
			}
		}
		previousControlNodesTmp.clear();
	}
	
	public void synchronizeDataNodes(){
		if ((previousDataNodesTmp!=null)&&(previousDataNodesTmp.size()>0)){
			previousDataNodes = new NodeInfo[previousDataNodesTmp.size()];
			int i =0;

			for (NodeInfo node: previousDataNodesTmp){
				previousDataNodes[i] = node;
				i++;

			}
		}
		previousDataNodesTmp.clear();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}


	public String getContentString(){
		StringBuilder builder = new StringBuilder();
		if (nodeContent instanceof ControlInfo){
			builder.append((ControlInfo)nodeContent);
		}
		else if (nodeContent instanceof MethodInvocInfo){
			MethodInvocInfo tmp = (MethodInvocInfo)nodeContent;
			builder.append(tmp.typeName +"." +tmp.methodName);

			if (Configurations.isAddMethodParam)
			{
				builder.append("(" );
				if (tmp.parameterList!=null)
				{
				if (tmp.parameterList.length>0)
				{
					for (int i=0; i<tmp.parameterList.length-1;i++){
						builder.append(tmp.parameterList[i].trim() +", ");
					}
					builder.append(tmp.parameterList[tmp.parameterList.length-1]);
				}
				}
				builder.append(")");
			}
		
		}
		else if (nodeContent instanceof VariableInfo){
//			builder.append(((VariableInfo)nodeContent).typeName + "#" + ((VariableInfo)nodeContent).varName);
			builder.append(((VariableInfo)nodeContent).typeName + "#var");

		}
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeInfo [nodeType=");
		builder.append(nodeType);
		builder.append(", ID=");
		builder.append(ID);

		
		builder.append(", nodeContent=");
		if (nodeContent instanceof ControlInfo){
			builder.append((ControlInfo)nodeContent);
		}
		else if (nodeContent instanceof MethodInvocInfo){
			MethodInvocInfo tmp = (MethodInvocInfo)nodeContent;
			builder.append(tmp.typeName +"." +tmp.methodName);

			if (Configurations.isAddMethodParam)
			{
				builder.append("(" );
				if (tmp.parameterList!=null)
				{
				if (tmp.parameterList.length>0)
				{
					for (int i=0; i<tmp.parameterList.length-1;i++){
						builder.append(tmp.parameterList[i].trim() +", ");
					}
					builder.append(tmp.parameterList[tmp.parameterList.length-1]);
				}
				}
				builder.append(")");
			}
		
		}
		else if (nodeContent instanceof VariableInfo){
			builder.append(((VariableInfo)nodeContent).typeName + "#" + ((VariableInfo)nodeContent).varName);
		}
		
//		builder.append(", containingMethod=");
//		if (containingMethod!=null)
//		{
//			builder.append(containingMethod.getFullMethodSignature());
//		}
		if (parentNode!=null)
		{
			builder.append(", parentNode=");
			builder.append(parentNode.ID);
//			builder.append("\t" + parentNode.toString());
		}
//		builder.append(", previousControlNodes=");
//		builder.append(previousControlNodes);
//		if (previousDataNodes!=null)
//		{
//			builder.append(", previousDataNodes=");
//			builder.append(Arrays.asList(previousDataNodes));
//		}
		builder.append("]");
		return builder.toString().replaceAll("\\s", "");
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toStringSimple() {
		StringBuilder builder = new StringBuilder();

		if (nodeContent instanceof ControlInfo){
			builder.append(((ControlInfo)nodeContent).getNodeTypeName());
		}
		else if (nodeContent instanceof MethodInvocInfo){
//			builder.append(((MethodInvocInfo)nodeContent).typeName +"." +((MethodInvocInfo)nodeContent).methodName
//					+ "#" + ((MethodInvocInfo)nodeContent).scopeList);
			MethodInvocInfo tmp = (MethodInvocInfo)nodeContent;
			builder.append(tmp.typeName +"." +tmp.methodName);
			
			if (Configurations.isAddMethodParam)
			{
				builder.append("(" );
				if (tmp.parameterList!=null)
				{
					if (tmp.parameterList.length>0)
					{
						for (int i=0; i<tmp.parameterList.length-1;i++){
							builder.append(tmp.parameterList[i].trim() +", ");
						}
						builder.append(tmp.parameterList[tmp.parameterList.length-1]);
					}
				}
				builder.append(")");
			}
		}
		else if (nodeContent instanceof VariableInfo){
			builder.append(((VariableInfo)nodeContent).typeName + "#" + ((VariableInfo)nodeContent).varName
					);
		}
		return builder.toString().replaceAll("\\s", "");
	}



	public boolean roleEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeInfo other = (NodeInfo) obj;
		if (nodeContent == null) {
			if (other.nodeContent != null)
				return false;
		} else if (!nodeContent.equals(other.nodeContent))
			return false;
		if (nodeType != other.nodeType)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (ID ^ (ID >>> 32));
		return result;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeInfo other = (NodeInfo) obj;
		if (ID != other.ID)
			return false;
		return true;
	}

}
