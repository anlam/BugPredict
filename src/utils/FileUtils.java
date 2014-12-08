/**
 * 
 */
package utils;

import graphdata.IncGraph;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.xerial.snappy.Snappy;
import org.xerial.snappy.SnappyInputStream;
import org.xerial.snappy.SnappyOutputStream;

import config.GlobalConfig;

/**
 * @author ANH
 *
 */
public class FileUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			deleteRecursive(new File(GlobalConfig.dummyDir));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static synchronized boolean deleteRecursive(File dir) throws FileNotFoundException{
		if (!dir.exists()) throw new FileNotFoundException(dir.getAbsolutePath());
		boolean ret = true;
		if (dir.isDirectory()){
			for (File f : dir.listFiles()){
				ret = ret && deleteRecursive(f);
			}
		}
		return ret && dir.delete();
	}

	/** 
	 * Delete all files and directories in directory but do not delete the
	 * directory itself.
	 * 
	 * @param strDir - string that specifies directory to delete
	 * @return boolean - sucess flag
	 */
	public static boolean deleteDirectoryContent(
			String strDir
			)
	{
		return ((strDir != null) && (strDir.length() > 0)) 
				? deleteDirectoryContent(new File(strDir)) : false;
	}

	/** 
	 * Delete all files and directories in directory but do not delete the
	 * directory itself.
	 * 
	 * @param fDir - directory to delete
	 * @return boolean - sucess flag
	 */
	public static boolean deleteDirectoryContent(
			File fDir
			)
	{
		boolean bRetval = false;

		if (fDir != null && fDir.isDirectory()) 
		{
			File[] files = fDir.listFiles();

			if (files != null)
			{
				bRetval = true;
				boolean dirDeleted;

				for (int index = 0; index < files.length; index++)
				{
					if (files[index].isDirectory())
					{
						// TODO: Performance: Implement this as a queue where you add to
						// the end and take from the beginning, it will be more efficient
						// than the recursion
						dirDeleted = deleteDirectoryContent(files[index]);
						if (dirDeleted)
						{
							bRetval = bRetval && files[index].delete();
						}
						else
						{
							bRetval = false;
						}
					}
					else
					{
						bRetval = bRetval && files[index].delete();
					}
				}
			}
		}

		return bRetval;
	}

	//	public static void deleteRecursive(File dir) throws FileNotFoundException{
	//		List<File> files = DirProcessing.getAllRecursiveFiles(dir);
	//	
	//		for (int i=files.size()-1; i>=0; i--){
	//			File file = files.get(i);
	//			Logger.log("file: " + file);
	//			file.delete();
	//		}
	//	}

	public static void copyFile(String sourceFilePath, String destFilePath){
		copyFile(new File(sourceFilePath), new File(destFilePath));
	}

	public static void copyFile(File sourceFile, File destFile){
		try{
			if(!destFile.exists()) {
				destFile.createNewFile();
			}

			FileChannel source = null;
			FileChannel destination = null;

			try {
				source = new FileInputStream(sourceFile).getChannel();
				destination = new FileOutputStream(destFile).getChannel();
				destination.transferFrom(source, 0, source.size());
			}
			finally {
				if(source != null) {
					source.close();
				}
				if(destination != null) {
					destination.close();
				}
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	public static int countNumLines(File file){
		int count = 0;               

		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) { 
				scanner.nextLine();   
				count++;              
			}  
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}    



		return count;
	}

	public static String getTextFile(File file){
		String content = "";
		try {
			content = new Scanner(file).useDelimiter("\\Z").next();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}


	public static void writeObjectFile(Object object, String filePath){
		try{

			FileOutputStream fout = new FileOutputStream(filePath);
			ObjectOutputStream oos = new ObjectOutputStream(fout);   
			oos.writeObject(object);
			oos.close();
			//			Logger.log("Write Object Done");

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}





	public static void writeCompressedObjectFile(Object object, String filePath){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream gzipOut = new GZIPOutputStream(baos,65536);
			BufferedOutputStream bos = new BufferedOutputStream(gzipOut, 65536);
			ObjectOutputStream objectOut = new ObjectOutputStream(bos);
			//			baos.flush();
			//			gzipOut.flush();
			//			bos.flush();
			//			objectOut.flush();
			objectOut.writeObject(object);
			objectOut.close();
			byte[] bytes = baos.toByteArray();

			FileOutputStream fout = new FileOutputStream(filePath);
			ObjectOutputStream oos = new ObjectOutputStream(fout);   
			//			oos.flush();
			oos.writeObject(bytes);
			oos.close();

			//			Logger.log("Write compressed object done");

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public static Object readObjectFile(String filePath){
		try{

			FileInputStream fin = new FileInputStream(filePath);
			ObjectInputStream ois = new ObjectInputStream(fin);
			Object object = ois.readObject();
			ois.close();

			return object;

		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		} 
	}




	public static Object readCompressedObjectFile(String filePath){
		try{

			FileInputStream fin = new FileInputStream(filePath);
			ObjectInputStream ois = new ObjectInputStream(fin);
			Object compObject = ois.readObject();
			ois.close();


			ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) compObject);			
			GZIPInputStream gzipIn = new GZIPInputStream(bais,65536);
			BufferedInputStream bis = new BufferedInputStream(gzipIn, 65536);
			ObjectInputStream objectIn = new ObjectInputStream(bis);
			Object object = objectIn.readObject();
			objectIn.close();

			return object;

		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		} 
	}


	//	public static void writeSnappyObjectFile(Object object, String filePath){
	//		try{
	//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	//            BufferedOutputStream bos = new BufferedOutputStream(baos, 65536);
	//			ObjectOutputStream objectOut = new ObjectOutputStream(bos);
	//			objectOut.writeObject(object);
	//			objectOut.close();
	//			byte[] bytes = baos.toByteArray();
	//			byte[] compressed = Snappy.compress(bytes);
	//
	//			FileOutputStream fout = new FileOutputStream(filePath);
	//			ObjectOutputStream oos = new ObjectOutputStream(fout);   
	//			oos.writeObject(compressed);
	//			oos.close();
	//
	//
	//		}catch(Exception ex){
	//			ex.printStackTrace();
	//		}
	//	}
	//
	//		
	//	public static Object readSnappyObjectFile(String filePath){
	//		try{
	//			
	//			FileInputStream fin = new FileInputStream(filePath);
	//			ObjectInputStream ois = new ObjectInputStream(fin);
	//			Object compObject = ois.readObject();
	//			ois.close();
	//			
	//			byte[] decompressed = Snappy.uncompress((byte[])compObject);
	//			ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) decompressed);			
	//			BufferedInputStream bis = new BufferedInputStream(bais, 65536);
	//			ObjectInputStream objectIn = new ObjectInputStream(bis);
	//			Object object = objectIn.readObject();
	//			objectIn.close();
	//
	//			return object;
	//
	//		}catch(Exception ex){
	//			ex.printStackTrace();
	//			return null;
	//		} 
	//	}

	public static void writeSnappyObjectFile(Object object, String filePath){

		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(baos, 65536);
			ObjectOutputStream objectOut = new ObjectOutputStream(bos);
			objectOut.writeObject(object);
			objectOut.close();
			byte[] bytes = baos.toByteArray();
			byte[] compressed = Snappy.compress(bytes);

			storeFC(compressed, filePath);

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public static void writeSnappyStreamObjectFile(Object object, String filePath){

		try{
			FileOutputStream fo = new FileOutputStream(new File(filePath));
			BufferedOutputStream snbo = new BufferedOutputStream(fo, 65536);
			SnappyOutputStream sn = new SnappyOutputStream(snbo);
			BufferedOutputStream bo = new BufferedOutputStream(sn, 65536);
			ObjectOutputStream objectOut = new ObjectOutputStream(bo);

			objectOut.writeObject(object);
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

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	private static void storeFC(byte[] bytes, String filePath) {
		FileOutputStream out = null;
		try {
			//Path newFile = Paths.get(filePath);
			//Files.deleteIfExists(newFile);
			out = new FileOutputStream(filePath,false);
			FileChannel file = out.getChannel();
			ByteBuffer buf = ByteBuffer.allocate(bytes.length);
			buf.put(bytes);
			buf.flip();
			file.write(buf);
			file.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			safeClose(out);
		}
	}



	private static void safeClose(OutputStream out) {
		try {
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			// do nothing
		}
	}

	public static  Object readSnappyObjectFile(String filePath) throws IOException{

		try{
			byte[] decompressed = null;
			if (new File(filePath).exists())
			{
				byte[] compressed = loadFC(filePath); 
				if (compressed!=null)
					decompressed = Snappy.uncompress(compressed);
			}
			ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) decompressed);	
			BufferedInputStream bis = new BufferedInputStream(bais, 65536);
			ObjectInputStream objectIn = new ObjectInputStream(bis);
			Object object = objectIn.readObject();
			objectIn.close();
			objectIn = null;
			bis.close();
			bis = null;
			bais.close();
			bais = null;

			return object;

		}catch(Exception ex){
//			ex.printStackTrace();
			throw new IOException();
//			return null;
		} 
	}


	public static  Object readSnappyStreamObjectFile(String filePath){

		try{
			File file = new File(filePath);
			if (file.exists())
			{
				FileInputStream fi = new FileInputStream(file);
				
				BufferedInputStream bisi = new BufferedInputStream(fi, 65536);
				SnappyInputStream si = new SnappyInputStream(bisi);
				BufferedInputStream bi = new BufferedInputStream(si, 65536);
				ObjectInputStream objectIn = new ObjectInputStream(bi);
				Object tmp = objectIn.readObject();
				
//				objectIn.reset();
//				bi.reset();
//				si.reset();
//				bisi.reset();
//				fi.reset();
				
				objectIn.close();
				bi.close();
				si.close();
				bisi.close();
				fi.close();
				return tmp;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		} 
		return null;
	}


	private static byte[] loadFC(String filePath) {
		try {
			//			return Files.readAllBytes(Paths.get(filePath));
			RandomAccessFile aFile = new RandomAccessFile(filePath, "r");
			FileChannel inChannel = aFile.getChannel();
			//			FileChannel inChannel = out[level1Val].getChannel();
			long fileSize = inChannel.size();
			ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
			inChannel.read(buffer);
			byte[] bytes = buffer.array();
//			buffer.rewind();
			buffer.flip();
			buffer.clear();
			buffer = null;
			inChannel.close();
			aFile.close();
			return bytes;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public static byte[] byteSerialize(Object obj) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		o.writeObject(obj);
		return b.toByteArray();
	}

	public static Object byteDeserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		ObjectInputStream o = new ObjectInputStream(b);
		return o.readObject();
	}


}
