/**
 * 
 */
package data;

/**
 * @author Anh
 *
 */
public class StatementPart {
	public static final int UNKNOWN = -1;
	public static final int BREAK = 0;
	public static final int CASE = 1;
	public static final int CATCH = 2;
	public static final int CONTINUE = 3;
	public static final int DEFAULT = 4;
	public static final int DO = 5;
	public static final int ELSE = 6;
	public static final int EMPTY = 7;
	public static final int FINALLY = 8;
	public static final int FOR = 9;
	public static final int ENHANCEDFOR = 10;
	public static final int ASSERT = 11;
	public static final int IF = 12;
	public static final int LABELED = 13;
	public static final int RETURN = 14;
	public static final int SWITCH = 15;
	public static final int SYNC = 16;
	public static final int THEN = 17;
	public static final int THROW = 18;
	public static final int TRY = 19;
	public static final int WHILE = 20;
	public static final int EXPRESSION = 21;
	public static final int BODYSTAMENT = 22;
	public static final int INITIALIZER = 23;
	public static final int CATCHTYPE = 24;
	public static final int IDENTIFIER = 25;
	public static final int SWITCHBLOCKGROUPS = 25;
	public static final int LOCKED = 26;
	public static final int CHECKED = 27;
	public static final int UNCHECKED = 28;
	public static final int GOTO = 29;
	public static final int USING = 30;
	public static final int SUPER = 31;
	public static final int THIS = 32;
	public static final int PARAM = 33;
	public static final int PARAMS = 34;
	public static final int METHOD = 35;
	public static final int CONSTRUCTOR = 36;
	public static final int THROWS = 37;
	public static final int BLOCK = 38;

	
	public String content = null;
	public int type = StatementPart.UNKNOWN;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public StatementPart(int type, String content) {
//		super();
		this.type = type;
		this.content = content.intern();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StatementPart [content=");
		builder.append(content);
		builder.append("]");
		return builder.toString();
	}

	
}
