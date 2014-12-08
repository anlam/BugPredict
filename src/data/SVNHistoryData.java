/**
 * 
 */
package data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.TreeMap;

import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;



/**
 * @author Anh
 *
 */
public class SVNHistoryData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5684987137562633336L;

	public TreeMap<Long, SVNRevData> SVNRevDataMap = new TreeMap<>(); 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param sVNRevDataMap
	 */
	public SVNHistoryData(TreeMap<Long, SVNRevData> SVNRevDataMap) {
		this.SVNRevDataMap.putAll(SVNRevDataMap);
	}


	public void writeObject(String path){
		try{

			FileOutputStream fout = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fout);   
			oos.writeObject(this);
			oos.close();
			System.out.println("Done Writing SVNHistoryData");
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public static SVNHistoryData readObject(String path){
		try{

			FileInputStream fin = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fin);
			SVNHistoryData svnHistory = (SVNHistoryData) ois.readObject();
			ois.close();

			return svnHistory;

		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		} 
	}

	public void writeDataFile(String path){

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

	public static SVNHistoryData readDataFile(String path){
		try{
			File file = new File(path);
			if (file.exists())
			{
				FileInputStream fi = new FileInputStream(file);

				BufferedInputStream bisi = new BufferedInputStream(fi, 65536);
				SnappyInputStream si = new SnappyInputStream(bisi);
				BufferedInputStream bi = new BufferedInputStream(si, 65536);
				ObjectInputStream objectIn = new ObjectInputStream(bi);
				SVNHistoryData tmp =(SVNHistoryData) objectIn.readObject();


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
}
