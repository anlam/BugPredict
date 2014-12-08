package data;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import change.TreeChange;

public class DiffPairStr implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1797031617900043527L;
	public static final String diffSeperator = "#################";
	
	String[] idxArr;
	int height;
	byte type = -1;

	public DiffPairStr(TreeChange change){
		ArrayList<String> tmp = new ArrayList<String>();
		if (change.getTree1()!=null)
		{
			tmp.addAll(change.getTree1().tokenSequence);
		}
		tmp.add(diffSeperator);
		if (change.getTree2()!=null)
		{
			tmp.addAll(change.getTree2().tokenSequence);
		}
		height = change.height;
		type = change.type;
		idxArr = new String[tmp.size()];
		for (int i=0; i<tmp.size(); i++){
			idxArr[i] = tmp.get(i);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(idxArr);
		return result;
	}

	public String[] getIdxArr() {
		return idxArr;
	}

	public void setIdxArr(String[] idxArr) {
		this.idxArr = idxArr;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
	public byte getType(){
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiffPairStr other = (DiffPairStr) obj;
		if (!Arrays.equals(idxArr, other.idxArr))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DiffPairStr [idxArr=");
		builder.append(Arrays.toString(idxArr));
		builder.append(", height=");
		builder.append(height);
		builder.append("]");
		return builder.toString();
	}
	

	
}
