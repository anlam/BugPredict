package change;

import java.util.ArrayList;
import java.util.List;

public class CTokenSequence {
	List<CTokenSequence> parents = new ArrayList<CTokenSequence>();
	
	public CTokenSequence(CTokenSequence parent) {
		this.parents.add(parent);
	}
}
