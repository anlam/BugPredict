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
public class StatementLexeme {

	public int startPos = 0;
	public int endPos = 0;
	
	public String content = null;
	public List<String> lexemeList = null;
	
	public List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public StatementLexeme(List<StatementLexemePart> statementLexemeParts) {
//		super();
//		this.statementParts = new ArrayList<StatementSememePart>();
//		this.statementParts.addAll(statementParts);
		this.statementLexemeParts = statementLexemeParts;
	}
	
	public StatementLexeme(StatementLexemePart statementLexemePart){
		this.statementLexemeParts.add(statementLexemePart);
	}
	public StatementLexeme(int startPos, int endPos, String content,
			List<String> lexemeList, List<StatementLexemePart> statementLexemeParts) {
		super();
		this.startPos = startPos;
		this.endPos = endPos;
		this.content = content;
		this.lexemeList = lexemeList;
		this.statementLexemeParts = new ArrayList<StatementLexemePart>();
		this.statementLexemeParts.addAll(statementLexemeParts);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StatementLexeme [statementLexemeParts=");
		builder.append(statementLexemeParts);
		builder.append("]");
		return builder.toString();
	}
}
