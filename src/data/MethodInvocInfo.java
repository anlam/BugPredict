/**
 * 
 */
package data;

import java.util.ArrayList;

/**
 * @author ANH
 *
 */
public class MethodInvocInfo {

	public boolean isInner = true;
	public String methodName;
	public String varName;
	public String typeName;
//	public ArrayList<String> parameterList = null;
//	public ArrayList<String> paramTypeList = null;

	public String[] parameterList = null;
	public String[] paramTypeList = null;
	public String methodQuanlifiedName;
	
	public ArrayList<Long> scopeList = new ArrayList<Long>(1);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public MethodInvocInfo(boolean isInner, String methodName, String varName,
			ArrayList<String> parameterList) {
		super();
		this.isInner = isInner;
		this.methodName = methodName.intern();
		this.varName = varName.intern();
		if ((parameterList!=null)&&(parameterList.size()>0))
		{
//			this.parameterList = new ArrayList<String>();
//			this.parameterList.addAll(parameterList);
			this.parameterList = new String[parameterList.size()];
			for (int i=0; i<parameterList.size();i++){
				this.parameterList[i] = parameterList.get(i).intern();
			}
			this.paramTypeList = new String[parameterList.size()];
		}
	}

	public MethodInvocInfo(boolean isInner, String methodName, String varName, String typeName,
			ArrayList<String> parameterList, ArrayList<Long> scopeList) {
		super();
		this.isInner = isInner;
		this.methodName = methodName.intern();
		this.varName = varName.intern();
		if (typeName != null)
			this.typeName = typeName.intern();
		else
			this.typeName = null;
		if ((parameterList!=null)&&(parameterList.size()>0))
		{
//			this.parameterList = new ArrayList<String>();
//			this.parameterList.addAll(parameterList);
			this.parameterList = new String[parameterList.size()];
			for (int i=0; i<parameterList.size();i++){
				this.parameterList[i] = parameterList.get(i).intern();
			}
			this.paramTypeList = new String[parameterList.size()];
		}
		this.scopeList.addAll(scopeList);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MethodInvocationInfo [isInner=");
		builder.append(isInner);
		builder.append(", methodName=");
		builder.append(methodName);
		builder.append(", parameterList=");
		builder.append(parameterList);
		builder.append("]");
		return builder.toString();
	}

	
}
