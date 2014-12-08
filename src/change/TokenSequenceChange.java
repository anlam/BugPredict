package change;

import java.util.ArrayList;
import java.util.HashMap;

public class TokenSequenceChange {
	CTokenSequence ts1, ts2;
	HashMap<Integer, ArrayList<Long>> projectTimestamps = new HashMap<Integer, ArrayList<Long>>();

	public TokenSequenceChange(CTokenSequence ts1, CTokenSequence ts2) {
		this.ts1 = ts1;
		this.ts2 = ts2;
	}
	
	public TokenSequenceChange(int projectId, long timestamp) {
		ArrayList<Long> timestamps = this.projectTimestamps.get(projectId);
		if (timestamps == null) {
			timestamps = new ArrayList<Long>();
			this.projectTimestamps.put(projectId, timestamps);
		}
		timestamps.add(timestamp);
	}
	
	public TokenSequenceChange(int projectId, long timestamp, CTokenSequence ts1, CTokenSequence ts2) {
		ArrayList<Long> timestamps = this.projectTimestamps.get(projectId);
		if (timestamps == null) {
			timestamps = new ArrayList<Long>();
			this.projectTimestamps.put(projectId, timestamps);
		}
		timestamps.add(timestamp);
		this.ts1 = ts1;
		this.ts2 = ts2;
	}
}
