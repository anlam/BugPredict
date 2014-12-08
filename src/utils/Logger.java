/**
 * 
 */
package utils;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author ANH
 *
 */
public class Logger {

	static FileWriter fw;
	static FileWriter fwBis;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public static void initDebug(String debugName){
		try {
			fw = new FileWriter(debugName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void logDebug(Object obj){
		try {
			fw.append(obj +"\r\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeDebug(){
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void initDebugBis(String debugName){
		try {
			fwBis = new FileWriter(debugName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void logDebugBis(Object obj){
		try {
			fwBis.append(obj +"\r\n");
			fwBis.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeDebugBis(){
		try {
			fwBis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void log(Object obj){
		System.out.println(obj);
	}
}
