/**
 * 
 */
package data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import config.ChangeConfig;
import treeMapping.MapVisitor;
import treeMapping.TreeMapper;
import utils.Logger;
import change.CInitializer;
import change.CMethod;
import change.TreeChange;

class DiffPairComparator implements Comparator<DiffPairStr>, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4447454373804764941L;

	@Override
	public int compare(DiffPairStr o1, DiffPairStr o2) {
		// TODO Auto-generated method stub
		if (Arrays.equals(o1.idxArr, o2.idxArr))
			return 0;
		else
			return 1;
	}
}

class StringLevel{
	String str;
	int level;
	public StringLevel(String str, int level) {
		this.str = str;
		this.level = level;
	}
	
}
/**
 * @author Anh
 *
 */
public class DiffPairsExtract implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6144382384790192528L;
	String projectName;
	public LinkedHashMap<Long,String> revLogMap = new LinkedHashMap<>();
	public LinkedHashMap<Long, ArrayList<DiffPairMethodInfo>> diffPairMethodListMap = new LinkedHashMap<>();
	HashMap<Long, ArrayList<DiffPairStr>> allTreeArrMapM = new HashMap<Long, ArrayList<DiffPairStr>>();
	HashMap<Long, ArrayList<DiffPairStr>> allTreeArrMapN = new HashMap<Long, ArrayList<DiffPairStr>>();
	HashMap<Long, Long> revisionDateMap = new HashMap<Long, Long>();

	public void addChangeMap(long revision, String log, HashMap<CMethod, ArrayList<TreeChange>> changeMap){
//		this.changeMap.putAll(changeMap);
		revLogMap.put(revision, log);
		ArrayList<DiffPairMethodInfo> diffPairMethodList = new ArrayList<>();
		for (CMethod method:changeMap.keySet()){
			String filePath = method.getCFile().getPath();
			String className = method.getCClass().getName();
			String methodName = method.getSimpleName();
			ArrayList<String> paramTypeList = new ArrayList<>();
			paramTypeList.addAll(method.getParamTypeList());
			
			ArrayList<DiffPairInfo> diffPairs = new ArrayList<>();
			for (TreeChange change:changeMap.get(method)){
				int sourceStartPosition = -1;
				String sourceRepresentStr = null;
				int targetStartPosition = -1;
				String targetRepresentStr = null;
				if (change.getTree1()!=null)
				{
					sourceStartPosition = change.getTree1().startPosition;
					sourceRepresentStr = change.getTree1().tokenSequence.toString();
				}
				if (change.getTree2()!=null)
				{
					targetStartPosition = change.getTree2().startPosition;
					targetRepresentStr = change.getTree2().tokenSequence.toString();
				}
				diffPairs.add(new DiffPairInfo(sourceStartPosition, sourceRepresentStr, targetStartPosition, targetRepresentStr));
			}
			DiffPairMethodInfo diffPairMethod = new DiffPairMethodInfo(filePath, className, methodName, paramTypeList, diffPairs);
			diffPairMethodList.add(diffPairMethod);
		}
		diffPairMethodListMap.put(revision, diffPairMethodList);
	}
	public HashMap<Long, ArrayList<DiffPairStr>> getAllTreeArrMapM() {
		return allTreeArrMapM;
	}
	public HashMap<Long, ArrayList<DiffPairStr>> getAllTreeArrMapN() {
		return allTreeArrMapN;
	}
	public HashMap<Long, Long> getRevisionDateMap() {
		return revisionDateMap;
	}

	public DiffPairsExtract(String projectName) {
		this.projectName = projectName;
	}
	public void updateRevisionDate(long revision, long dateValue){
		revisionDateMap.put(revision, dateValue);
	}
	public void getAllDiffPairs(ArrayList<TreeChange> changes, long revision ){
		for (TreeChange change:changes){
			DiffPairStr diffPairStr = new DiffPairStr(change);
			updateAllTreeArrMap(allTreeArrMapM, diffPairStr, revision);
		}
	}
	
	public void updateAllTreeArrMap(HashMap<Long, ArrayList<DiffPairStr>> allTreeArrMap, DiffPairStr diffPairStr ,
			long revision){
		if (allTreeArrMap.containsKey(revision)){
			allTreeArrMap.get(revision).add(diffPairStr);
		}
		else {
			ArrayList<DiffPairStr> diffPairStrList = new ArrayList<DiffPairStr>();
			diffPairStrList.add(diffPairStr);
			allTreeArrMap.put(revision, diffPairStrList);
		}
	}
	
//	public void writeData(String outputDiffPairFilePath){
//		try{
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
//			ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
//			objectOut.writeObject(this);
//			objectOut.close();
//			byte[] bytes = baos.toByteArray();
//
//			FileOutputStream fout = new FileOutputStream(outputDiffPairFilePath);
//			ObjectOutputStream oos = new ObjectOutputStream(fout);   
//			oos.writeObject(bytes);
//			oos.close();
//			System.out.println("Done writing DiffPairsExtract of " + projectName);
//
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
//	}
//
//	public static DiffPairsExtract readData(String diffPairFilePath){
//		try{
//			FileInputStream fin = new FileInputStream(diffPairFilePath);
//			ObjectInputStream ois = new ObjectInputStream(fin);
//			byte[] bytes  = (byte[]) ois.readObject();
//			ois.close();
//			fin.close();
//
//
//			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//			//			GZIPInputStream gzipIn = new GZIPInputStream(bais);
//			InputStream gzipIn = new BufferedInputStream(new GZIPInputStream(bais));
//
//			ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
//			DiffPairsExtract myObj = (DiffPairsExtract) objectIn.readObject();
//			objectIn.close();
//			bais.close();
//			gzipIn.close();
//			return myObj;
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
//		return null;
//	}
	
	public void writeData(String path){

		try{
			FileOutputStream fo = new FileOutputStream(new File(path));
			BufferedOutputStream snbo = new BufferedOutputStream(fo, 65536);
			SnappyOutputStream sn = new SnappyOutputStream(snbo);
			BufferedOutputStream bo = new BufferedOutputStream(sn, 65536);
			ObjectOutputStream objectOut = new ObjectOutputStream(bo);

			objectOut.writeObject(this);
			bo.flush();
			sn.flush();
			snbo.flush();
			fo.flush();
			objectOut.reset();
			objectOut.close();
			objectOut = null;

			bo.close();
			sn.close();
			snbo.close();
			fo.close();

			bo.close();
			sn.close();
			snbo.close();
			fo.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	public static DiffPairsExtract readData(String path){
		try{
			File file = new File(path);
			if (file.exists())
			{
				FileInputStream fi = new FileInputStream(file);

				BufferedInputStream bisi = new BufferedInputStream(fi, 65536);
				SnappyInputStream si = new SnappyInputStream(bisi);
				BufferedInputStream bi = new BufferedInputStream(si, 65536);
				ObjectInputStream objectIn = new ObjectInputStream(bi);
				DiffPairsExtract tmp =(DiffPairsExtract) objectIn.readObject();


				objectIn.close();
				bi.close();
				si.close();
				bisi.close();
				fi.close();
				return tmp;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DiffPairsExtract [projectName=");
		builder.append(projectName);
		builder.append(", allTreeArrMapM=");
		builder.append(allTreeArrMapM);
		builder.append(", allTreeArrMapN=");
		builder.append(allTreeArrMapN);
		builder.append(", revisionDateMap=");
		builder.append(revisionDateMap);
		builder.append("]");
		return builder.toString();
	}

}