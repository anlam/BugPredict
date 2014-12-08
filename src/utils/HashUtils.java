package utils;

import java.util.List;

import config.GlobalConfig;

public class HashUtils {

	//FIXME: I used a hash code by the first element of sequence. It  maybe not good
	public static int getLevel1HashCode(List<Integer> idxList){
//		int hashCode = 0;
//		for (Integer idx:idxList){
//			hashCode = 77*hashCode + idx;
//		}
		return idxList.get(0)%GlobalConfig.numLevel1Hash;//idxList.hashCode();//hashCode;// 
	}
	
	
	public static int getLevel2HashCode(List<Integer> idxList){
//		int hashCode = 0;
//		for (Integer idx:idxList){
//			hashCode = 77*hashCode + idx;
//		}
		return idxList.hashCode();//hashCode;// 
	}
}
