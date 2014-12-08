package change;

import java.util.ArrayList;
import java.util.List;

import utils.ASTFlattener;

public class CTree {
	byte height = 0, type = -1;
	List<CTree> parents = new ArrayList<CTree>();
	public ArrayList<String> lexsymSequence, tokenSequence;
	public int startPosition=-1;
	//ArrayList<Integer> indexSequence;
	
	public CTree() {
		
	}

	public CTree(byte type, byte height, ArrayList<String> lexsymSequence, int startPosition) {
		this.type = type;
		this.height = height;
		this.lexsymSequence = new ArrayList<String>(lexsymSequence);
		this.startPosition = startPosition;
	}

	void abstractout() {
		this.tokenSequence = new ArrayList<String>();
		for (int i = 0; i < lexsymSequence.size(); i++) {
			String t = lexsymSequence.get(i);
			if (t.startsWith(ASTFlattener.PREFIX_LITERAL)) {
				tokenSequence.add(t.substring(ASTFlattener.PREFIX_LITERAL.length(), t.indexOf('|', ASTFlattener.PREFIX_LITERAL.length())));
			}
			else
				this.tokenSequence.add(t);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CTree))
			return false;
		CTree other = (CTree) obj;
		return this.lexsymSequence.equals(other.lexsymSequence);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.height);
		for (int i = 0; i < this.tokenSequence.size(); i++) {
			sb.append(" " + this.tokenSequence.get(i));
		}
		return sb.toString();
	}
}
