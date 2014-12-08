/**
 * 
 */
package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author ANH
 *
 */
public class CollectionUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}

	public static List<Integer> transformArrList(int[] idxArr){
		List<Integer> list = new ArrayList<Integer>(idxArr.length);
		for (int i=0; i<idxArr.length;i++){
			list.add(idxArr[i]);
		}
		return list;
	}
	
	
	public static int[] transformListArr(List<Integer> idxList){
		int[] arr = new int[idxList.size()];
		for (int i=0; i<idxList.size();i++){
			arr[i] = idxList.get(i);
		}
		return arr;
	}
	
	public static int[] transformSetArr(Set<Integer> idxSet){
		int[] arr = new int[idxSet.size()];
		int count = 0;
		for (int val: idxSet){
			arr[count] = val;
			count++;
		}
		return arr;
	}

}
