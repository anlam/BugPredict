/**
 * 
 */
package data;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Anh
 *
 */
public class StatementSememe {

	public int startPos = 0;
	public int endPos = 0;
	
	public String content = null;
	public List<NodeSequenceInfo> nodeSequenceInfo = null;
	
	public List<StatementSememePart> statementSememeParts = new ArrayList<StatementSememePart>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public StatementSememe(List<StatementSememePart> statementSememeParts) {
//		super();
//		this.statementParts = new ArrayList<StatementSememePart>();
//		this.statementParts.addAll(statementParts);
		this.statementSememeParts = statementSememeParts;
	}
	public StatementSememe(int startPos, int endPos, String content,
			List<NodeSequenceInfo> nodeSequenceInfo,
			List<StatementSememePart> statementSememeParts) {
		super();
		this.startPos = startPos;
		this.endPos = endPos;
		this.content = content;
		this.nodeSequenceInfo = nodeSequenceInfo;
		this.statementSememeParts = new ArrayList<StatementSememePart>();
		this.statementSememeParts.addAll(statementSememeParts);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StatementSememe [statementSememeParts=");
		builder.append(statementSememeParts);
		builder.append("]");
		return builder.toString();
	}
}
