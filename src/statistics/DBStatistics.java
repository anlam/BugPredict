/**
 * 
 */
package statistics;

import graphdata.NewIncGraphHDDDB;

import java.util.ArrayList;
import java.util.TreeMap;

import utils.Logger;
import config.GlobalConfig;



class BoxPlotStats{
	TreeMap<Integer, Integer> valCountMap = new TreeMap<>();
	public long idx5P = 0;
	public long idx25P = 0;
	public long idx50P = 0;
	public long idx75P = 0;
	public long idx95P = 0;
	

	
	public long val5P = 0;
	public long val25P = 0;
	public long val50P = 0;
	public long val75P = 0;
	public long val95P = 0;

	public int max = 0;
	public BoxPlotStats(TreeMap<Integer, Integer> valCountMap) {
		this.valCountMap.putAll(valCountMap);
		long size = 0;
		for (Integer val:valCountMap.keySet()){
			size+= valCountMap.get(val);
		}
		
		idx5P = 5*(size-1)/100;
		val5P = getValAtIdx(idx5P);
		
		idx25P = 25*(size-1)/100;
		val25P = getValAtIdx(idx25P);
		
		idx50P = 50*(size-1)/100;
		val50P = getValAtIdx(idx50P);
		
		idx75P = 75*(size-1)/100;
		val75P = getValAtIdx(idx75P);
		
		idx95P = 95*(size-1)/100;
		val95P = getValAtIdx(idx95P);
		
		max = valCountMap.lastKey();
	}
	
	public long getValAtIdx(long idx){
		ArrayList<Integer> valList = new ArrayList<>(valCountMap.keySet());
		long totalCount = 0;
		for (int i=0;i<valList.size();i++ ){
			long count = valCountMap.get(valList.get(i));
			if (totalCount<=idx&&totalCount+count>idx)
				return valList.get(i);
			totalCount+= count;
		}
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Project BoxPlot [at 5%=");
		builder.append(val5P);
		builder.append(", at 25%=");
		builder.append(val25P);
		builder.append(", at 50%=");
		builder.append(val50P);
		builder.append(", at 75%=");
		builder.append(val75P);
		builder.append(", at 95%=");
		builder.append(val95P);
		builder.append(", maximum value=");
		builder.append(max);
		builder.append("]");
		return builder.toString();
	}
	
	
	public String toStringSimple() {
		StringBuilder builder = new StringBuilder();
//		builder.append("Project BoxPlot [at 5%=");
		builder.append(val5P +"\t");
//		builder.append(", at 25%=");
		builder.append(val25P+"\t");
//		builder.append(", at 50%=");
		builder.append(val50P+"\t");
//		builder.append(", at 75%=");
		builder.append(val75P+"\t");
//		builder.append(", at 95%=");
		builder.append(val95P);
//		builder.append(", maximum value=");
//		builder.append(max);
//		builder.append("]");
		return builder.toString();
	}
}


class DoubleBoxPlotStats{
	TreeMap<Double, Integer> valCountMap = new TreeMap<>();
	public long idx5P = 0;
	public long idx25P = 0;
	public long idx50P = 0;
	public long idx75P = 0;
	public long idx95P = 0;
	
	public double val5P = 0;
	public double val25P = 0;
	public double val50P = 0;
	public double val75P = 0;
	public double val95P = 0;

	public double max = 0;
	public DoubleBoxPlotStats(TreeMap<Double, Integer> valCountMap) {
		this.valCountMap.putAll(valCountMap);
		long size = 0;
		for (Double val:valCountMap.keySet()){
			size+= valCountMap.get(val);
		}
		
		idx5P = 5*(size-1)/100;
		val5P = getValAtIdx(idx5P);
		
		idx25P = 25*(size-1)/100;
		val25P = getValAtIdx(idx25P);
		
		idx50P = 50*(size-1)/100;
		val50P = getValAtIdx(idx50P);
		
		idx75P = 75*(size-1)/100;
		val75P = getValAtIdx(idx75P);
		
		idx95P = 95*(size-1)/100;
		val95P = getValAtIdx(idx95P);
		
		max = valCountMap.lastKey();
	}
	
	public double getValAtIdx(long idx){
		ArrayList<Double> valList = new ArrayList<>(valCountMap.keySet());
		long totalCount = 0;
		for (int i=0;i<valList.size();i++ ){
			long count = valCountMap.get(valList.get(i));
			if (totalCount<=idx&&totalCount+count>idx)
				return valList.get(i);
			totalCount+= count;
		}
		return 0.0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Usage/Project BoxPlot [at 5%=");
		builder.append(formatD(val5P));
		builder.append(", at 25%=");
		builder.append(formatD(val25P));
		builder.append(", at 50%=");
		builder.append(formatD(val50P));
		builder.append(", at 75%=");
		builder.append(formatD(val75P));
		builder.append(", at 95%=");
		builder.append(formatD(val95P));
		builder.append(", maximum usages/projects=");
		builder.append(formatD(max));
		builder.append("]");
		return builder.toString();
	}
	
	public String toStringSimple() {
		StringBuilder builder = new StringBuilder();
//		builder.append("Usage/Project BoxPlot [at 5%=");
		builder.append(formatD(val5P)+"\t");
//		builder.append(", at 25%=");
		builder.append(formatD(val25P)+"\t");
//		builder.append(", at 50%=");
		builder.append(formatD(val50P)+"\t");
//		builder.append(", at 75%=");
		builder.append(formatD(val75P)+"\t");
//		builder.append(", at 95%=");
		builder.append(formatD(val95P));
//		builder.append(", maximum usages/projects=");
//		builder.append(formatD(max));
//		builder.append("]");
		return builder.toString();
	}
	
	String formatD(double val){
		return String.format("%1.2f", val);
	}
}

/**
 * @author Anh
 *
 */
public class DBStatistics {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String databasePath1 = GlobalConfig.mainDir + "Storage/db_0_955/";
		if (args.length>=2){
			GlobalConfig.mainDir = args[0];
			databasePath1 = GlobalConfig.mainDir + args[1];
			GlobalConfig.refreshParams();
		}
//		String databasePath1 = GlobalConfig.mainDir + "Storage/small_db_1_106/";

		Logger.log("Loading database: " + databasePath1);
		NewIncGraphHDDDB hddDB1 = new NewIncGraphHDDDB(databasePath1, false,false, GlobalConfig.maxUnCompSize);
//		hddDB1.doSimpleStatistics();
		hddDB1.doStatistics();
		
		TreeMap<Integer, TreeMap<Integer, Integer>> sizeUsagesMap = hddDB1.sizeUsagesMap;
		Logger.log("\r\n**************\r\nUsages Box plot:");
		for (Integer size:sizeUsagesMap.keySet()){
			BoxPlotStats boxPlot = new BoxPlotStats(sizeUsagesMap.get(size));
			Logger.log("\tat size " + size + ":\t" + boxPlot.toStringSimple());
		}

		
		TreeMap<Integer, TreeMap<Integer, Integer>> sizeProjectsMap = hddDB1.sizeProjectsMap;
		Logger.log("\r\n**************\r\nProjects Box plot:");
		for (Integer size:sizeProjectsMap.keySet()){
			BoxPlotStats boxPlot = new BoxPlotStats(sizeProjectsMap.get(size));
			Logger.log("\tat size " + size + ":\t" + boxPlot.toStringSimple());
		}
		
		
		TreeMap<Integer, TreeMap<Double, Integer>> sizeUsagesProjectsMap = hddDB1.sizeUsagesProjectsMap;
		Logger.log("\r\n**************\r\nUsages/Projects Box plot:");
		for (Integer size:sizeUsagesProjectsMap.keySet()){
			DoubleBoxPlotStats boxPlot = new DoubleBoxPlotStats(sizeUsagesProjectsMap.get(size));
			Logger.log("\tat size " + size + ":\t" + boxPlot.toStringSimple());
		}
		
		TreeMap<Integer, TreeMap<Integer, Integer>> sizeUsagesMultiMap = hddDB1.sizeUsagesMultiMap;
		Logger.log("\r\n**************\r\nUsages Box plot for multi:");
		for (Integer size:sizeUsagesMultiMap.keySet()){
			BoxPlotStats boxPlot = new BoxPlotStats(sizeUsagesMultiMap.get(size));
			Logger.log("\tat size " + size + ":\t" + boxPlot.toStringSimple());
		}

		
		TreeMap<Integer, TreeMap<Integer, Integer>> sizeProjectsMultiMap = hddDB1.sizeProjectsMultiMap;
		Logger.log("\r\n**************\r\nProjects Box plot for multi:");
		for (Integer size:sizeProjectsMultiMap.keySet()){
			BoxPlotStats boxPlot = new BoxPlotStats(sizeProjectsMultiMap.get(size));
			Logger.log("\tat size " + size + ":\t" + boxPlot.toStringSimple());
		}
		
		
		TreeMap<Integer, TreeMap<Double, Integer>> sizeUsagesProjectsMultiMap = hddDB1.sizeUsagesProjectsMultiMap;
		Logger.log("\r\n**************\r\nUsages/Projects Box plot for multi:");
		for (Integer size:sizeUsagesProjectsMultiMap.keySet()){
			DoubleBoxPlotStats boxPlot = new DoubleBoxPlotStats(sizeUsagesProjectsMultiMap.get(size));
			Logger.log("\tat size " + size + ":\t" + boxPlot.toStringSimple());
		}
		
		TreeMap<Integer, TreeMap<Integer, Integer>> concernedUsagesMap = hddDB1.concernedUsagesMap;
		Logger.log("\r\n**************\r\nJDK Usages Box plot:");
		for (Integer size:concernedUsagesMap.keySet()){
			BoxPlotStats boxPlot = new BoxPlotStats(concernedUsagesMap.get(size));
			Logger.log("\tat size " + size + ":\t" + boxPlot.toStringSimple());
		}

		
		TreeMap<Integer, TreeMap<Integer, Integer>> concernedProjectsMap = hddDB1.concernedProjectsMap;
		Logger.log("\r\n**************\r\nJDK Projects Box plot:");
		for (Integer size:concernedProjectsMap.keySet()){
			BoxPlotStats boxPlot = new BoxPlotStats(concernedProjectsMap.get(size));
			Logger.log("\tat size " + size + ":\t" + boxPlot.toStringSimple());
		}
		
		
		TreeMap<Integer, TreeMap<Double, Integer>> concernedUsagesProjectsMap = hddDB1.concernedUsagesProjectsMap;
		Logger.log("\r\n**************\r\nJDK Usages/Projects Box plot:");
		for (Integer size:concernedUsagesProjectsMap.keySet()){
			DoubleBoxPlotStats boxPlot = new DoubleBoxPlotStats(concernedUsagesProjectsMap.get(size));
			Logger.log("\tat size " + size + ":\t" + boxPlot.toStringSimple());
		}
		
		
		
		hddDB1.doTopStatistics();
	}

	
}
