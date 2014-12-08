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
public class StatementLexemePart {

	public static final int UNKNOWN = -1;
	public static final int BREAK = 0;
	public static final int CASE = 1;
	public static final int CATCH = 2;
	public static final int CONTINUE = 3;
	public static final int DEFAULT = 4;
	public static final int DO = 5;
	public static final int ELSE = 6;
	public static final int EMPTYSTATEMENT = 7;
	public static final int FINALLY = 8;
	public static final int FOR = 9;
	public static final int ENHANCEDFOR = 10;
	public static final int ASSERT = 11;
	public static final int IF = 12;
	public static final int LABELED = 13;
	public static final int RETURN = 14;
	public static final int SWITCH = 15;
	public static final int SYNCHRONIZED = 16;
	public static final int THEN = 17;
	public static final int THROW = 18;
	public static final int TRY = 19;
	public static final int WHILE = 20;
	public static final int EXPRESSION = 21;
	public static final int BODYSTAMENT = 22;
	public static final int INITIALIZER = 23;
	public static final int CATCHTYPE = 24;
	public static final int IDENTIFIER = 25;
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
	public static final int CONDITIONAL = 39;
	public static final int UPDATES = 40;
	public static final int VARIABLEDECLARATION = 41;
	public static final int REFERENCE = 42;
	public static final int NEW = 43;
	public static final int METHODREFERENCE = 44;
	public static final int OPERATOR = 45;
	public static final int FOREACH = 46;
	public static final int STOKEN = 47;
	public static final int SPEC = 48;
	public static final int TYPEPARAMS = 49;
	public static final int TYPEREFERENCE = 50;
	public static final int PROPDEC = 51;
	public static final int ABSTRACT = 52;
	public static final int FINAL = 53;
	public static final int ENUMCONSTDEC = 54;
	public static final int ENUMDECLARATION = 55;
	public static final int PACKAGESPEC = 56;
	public static final int PACKAGEREFERENCE = 57;
	public static final int CLASS = 58;
	public static final int EXTENDS = 59;
	public static final int IMPLEMENTS = 60;
	public static final int SWITCHBLOCKGROUPS = 61;
	public static final int INTERFACE = 62;
	public static final int FIELDECLARATION = 63;
	public static final int IMPORT = 64;
	public static final int UNCOLREFERENCE = 65;
	public static final int VARIABLESPECIFICATION = 66;
	public static final int ARRAYREFERENCE = 67;
	public static final int FIELDREFERENCE = 68;
	public static final int VARIABLEREFERENCE = 69;
	public static final int METACLASSREFERENCE = 70;
	public static final int SUPERCONSTRUCTORREFERENCE = 71;
	public static final int THISCONSTRUCTORREFERENCE = 72;
	public static final int SUPERREFERENCE = 73;
	public static final int THISREFERENCE = 74;
	public static final int ASSIGNMENT = 75;
	public static final int ANNOTATION = 76;
	public static final int ANNOTATIONPAIR = 77;
	public static final int ENUMCONSTRUCTORREFERENCE = 78;
	public static final int ATTRIBUTE = 79;
	public static final int SPECIALCONSTRUCTOR = 80;
	public static final int IN = 81;
	public static final int METHODGROUPREFERENCE = 82;
	public static final int ADDACCESSOR = 83;
	public static final int TARGET = 84;
	public static final int AS = 85;
	public static final int ATTRIBUTESECTION = 86;
	public static final int DELEGATE = 87;
	public static final int EVENTDECLARATION = 88;
	public static final int GET = 89;
	public static final int INDEXERDECLARATION = 90;
	public static final int NAMESPACEREFERENCE = 91;
	public static final int NAMESPACESPEC = 92;
	public static final int USINGBLOCK = 93;
	public static final int CONSTANT = 94;
	public static final int MODIFIER = 95;

	
	public String content = null;
	public int type = UNKNOWN;
	
	public int startPart = -1;
	public int endPart = -1;
	
	public List<LexemeInfo> lexemeList = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public StatementLexemePart(int type, String content) {
		this.type = type;
		this.content = content.intern();
	}
	
	
	public StatementLexemePart(int type, String content, List<LexemeInfo> lexemeList) {
//		super();
		this.type = type;
		this.content = content.intern();
		if (this.lexemeList==null){
			this.lexemeList = new ArrayList<LexemeInfo>();
		}
		this.lexemeList.addAll(lexemeList);
	}
	
	public void addLexeme(LexemeInfo lexeme){
		if (this.lexemeList==null){
			this.lexemeList = new ArrayList<LexemeInfo>();
		}
		this.lexemeList.add(lexeme);
	}
	
	public void addLexemeList(List<LexemeInfo> lexemeList){
		if (this.lexemeList==null){
			this.lexemeList = new ArrayList<LexemeInfo>();
		}
		this.lexemeList.addAll(lexemeList);
		
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StatementSememePart [content=");
		builder.append(content);
		builder.append(", lexemeList=");
		builder.append(lexemeList);
		builder.append("]");
		return builder.toString().replaceAll("\r\n", " ");
	}


}
