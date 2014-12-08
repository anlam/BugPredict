/**
 * 
 */
package data;

/**
 * @author ANH
 *
 */
public class ControlInfo {

	public static final int UNKNOWN = 0;
	public static final int FOR = 1;
	public static final int ENHANCEDFOR = 2;
	public static final int DO = 3;
	public static final int WHILE = 4;
	public static final int IF = 5;
	public static final int ELSE = 6;
	public static final int CONDITIONAL = 7;
	public static final int SWITCH = 8;
	public static final int TRY = 9;
	public static final int CATCH = 10;
	public static final int FINALLY = 11;
	public static final int STATIC = 12;
	public static final int CASE = 13;
	public static final int SYNC = 14;
	public static final int BREAK = 15;
	public static final int CONTINUE = 16;
	public static final int DEFAULT = 17;
	public static final int RETURN = 18;
	public static final int SYNCHRONIZED = 19;
	public static final int THEN = 20;
	public static final int THROW = 21;
	public static final int THROWS = 22;
	public static final int ASSERT = 23;

	//for c#
	public static final int FOREACH = 24;
	public static final int LOCK = 25;
	public static final int CHECKED = 26;
	public static final int UNCHECKED = 27;
	public static final int GOTO = 28;
	public static final int USINGBLOCK = 29;

	public int type= UNKNOWN;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}

	public ControlInfo(int type) {
		super();
		this.type = type;
	}
	
	public String getNodeTypeName()
	{
		return getNodeTypeName(this.type);
	}
	public static String getNodeTypeName(int type){
		String tmp = "_UNKNWON_";
		switch (type) {
		case UNKNOWN:
			tmp = "_UNKNOWN_";
			break;
			
		case FOR:
			tmp = "_FOR_";
			break;
		case ENHANCEDFOR:
			tmp = "_ENHANCEDFOR_";
			break;
		case DO:
			tmp = "_DO_";
			break;
		case WHILE:
			tmp = "_WHILE_";
			break;
		
		case IF:
			tmp = "_IF_";
			break;
		case ELSE:
			tmp = "_ELSE_";
			break;
		case CONDITIONAL:
			tmp = "_CONDITIONAL_";
			break;
		case SWITCH:
			tmp = "_SWITCH_";
			break;
		case CASE:
			tmp = "_CASE_";
			break;
		case TRY:
			tmp = "_TRY_";
			break;
					
		case CATCH:
			tmp = "_CATCH_";
			break;
		case FINALLY:
			tmp = "_FINALLY_";
			break;
		case STATIC:
			tmp = "_STATIC_";
			break;
		case SYNC:
			tmp = "_SYNC_";
			break;

		case BREAK:
			tmp = "_BREAK_";
			break;
		case CONTINUE:
			tmp = "_CONTINUE_";
			break;	
		case DEFAULT:
			tmp = "_DEFAULT_";
			break;		
		case RETURN:
			tmp = "_RETURN_";
			break;		
		case SYNCHRONIZED:
			tmp = "_SYNCHRONIZED_";
			break;		
		case THEN:
			tmp = "_THEN_";
			break;		
		case THROW:
			tmp = "_THROW_";
			break;		
		case THROWS:
			tmp = "_THROWS_";
			break;	
		case ASSERT:
			tmp = "_ASSERT_";
			break;		
			
		//C#
		case FOREACH:
			tmp = "_FOREACH_";
			break;	
		case LOCK:
			tmp = "_LOCK_";
			break;	
		case CHECKED:
			tmp = "_CHECKED_";
			break;	
		case UNCHECKED:
			tmp = "_UNCHECKED_";
			break;	
		case GOTO:
			tmp = "_GOTO_";
			break;	
		case USINGBLOCK:
			tmp = "_USINGBLOCK_";
			break;	
				
		default:
			break;
		}
		return tmp;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ControlInfo [type=");
		builder.append(getNodeTypeName(type));
		builder.append("]");
		return builder.toString();
	}

	public static boolean isControlStr(String str){
		boolean controlStr = false;
		for (int i=1; i<=29; i++){
			if(getNodeTypeName(i).equals(str.trim())){
				controlStr = true;
			}
		}
	
		return controlStr;
	}

}
