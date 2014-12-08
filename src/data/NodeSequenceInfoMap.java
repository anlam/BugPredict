package data;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author ANH
 *
 */

public class NodeSequenceInfoMap{

	public static TObjectIntHashMap<String> typeIDMap = new TObjectIntHashMap<String>();
	public static TIntObjectHashMap<String> idTypeMap = new TIntObjectHashMap<String>();
	public static TObjectIntHashMap<String> varIDMap = new TObjectIntHashMap<String>();
	public static TIntObjectHashMap<String> idVarMap = new TIntObjectHashMap<String>();
	public static TObjectIntHashMap<String> accessIDMap = new TObjectIntHashMap<String>();
	public static TIntObjectHashMap<String> idAccessMap = new TIntObjectHashMap<String>();
	
	public static void clearAll(){
		typeIDMap.clear();
		idTypeMap.clear();
		varIDMap.clear();
		idVarMap.clear();
		accessIDMap.clear();
		idAccessMap.clear();
	}
	
}