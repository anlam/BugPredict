/**
 * 
 */
package data;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @author Anh
 *
 */
public class SVNRevData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1680494745604013315L;
	
	public long revision = 0L;
	public String author = "";
	public long dateVal = 0L;
	public String log = "";
	public LinkedHashMap<String, String> changedPathOldPath = new LinkedHashMap<>();
	public LinkedHashMap<String, String> changedPathType = new LinkedHashMap<>(); 
	public LinkedHashMap<String, String> changedPathContentPrev = new LinkedHashMap<>();
	public LinkedHashMap<String, String> changedPathContentNext = new LinkedHashMap<>();
	public  String diffContent;

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param revision
	 * @param author
	 * @param dateVal
	 * @param log
	 * @param changedPathType
	 */
	public SVNRevData(long revision, String author, long dateVal, String log,
			LinkedHashMap<String, String> changedPathOldPath, 
			LinkedHashMap<String, String> changedPathType,
			LinkedHashMap<String, String> changedPathContentPrev,
			LinkedHashMap<String, String> changedPathContentNext,
			 String diffContent) {
		this.revision = revision;
		this.author = author;
		this.dateVal = dateVal;
		this.log = log;
		this.changedPathOldPath.putAll(changedPathOldPath);
		this.changedPathType .putAll(changedPathType);
		this.changedPathContentPrev.putAll(changedPathContentPrev);
		this.changedPathContentNext.putAll(changedPathContentNext);
		this.diffContent = diffContent;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SVNRevData [revision=");
		builder.append(revision);
		builder.append(", author=");
		builder.append(author);
		builder.append(", dateVal=");
		builder.append(dateVal);
		builder.append(", log=");
		builder.append(log);
		builder.append(",\r\n changedPathOldPath=\r\n");
		builder.append(changedPathOldPath);
		builder.append(",\r\n changedPathType=\r\n");
		builder.append(changedPathType);
		builder.append(",\r\n changedPathContentPrev=\r\n");
		builder.append(changedPathContentPrev);
		builder.append(",\r\n******************\r\n changedPathContentNext=\r\n");
		builder.append(changedPathContentNext);
		builder.append(",\r\n diffContent=\r\n");
		builder.append(diffContent);
		builder.append("]");
		return builder.toString();
	}

	
}
