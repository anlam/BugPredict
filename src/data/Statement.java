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
public class Statement {
	

		
	public int startPos = 0;
	public int endPos = 0;
	
	public String content = null;
	public List<NodeSequenceInfo> nodeSequenceInfo = null;
	
	public List<StatementPart> statementParts = new ArrayList<StatementPart>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public Statement(List<StatementPart> statementParts) {
//		super();
		this.statementParts = new ArrayList<StatementPart>();
		this.statementParts.addAll(statementParts);
	}
	public Statement(int startPos, int endPos, String content,
			List<NodeSequenceInfo> nodeSequenceInfo,
			List<StatementPart> statementParts) {
		super();
		this.startPos = startPos;
		this.endPos = endPos;
		this.content = content;
		this.nodeSequenceInfo = nodeSequenceInfo;
		this.statementParts = new ArrayList<StatementPart>();
		this.statementParts.addAll(statementParts);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Statement [statementParts=");
		builder.append(statementParts);
		builder.append("]");
		return builder.toString();
	}

	
}
