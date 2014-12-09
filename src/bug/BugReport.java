package bug;

import java.io.Serializable;
import java.util.List;

public class BugReport implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2872123543171781851L;
	
	public String id;
	public String bug_id;
	public String summary;
	public String description;
	public String report_time;
	public String reporter;
	public String assignee;
	public String status;
	public String product;
	public String component;
	public String importance;
	public String commit;
	public String author;
	public String commit_time;
	public String log;
	public List<String> files;
	
	public BugReport(String id, String bug_id, String summary, String description, String report_time,
						String reporter, String assignee, String status, String product, String component,
						String importance, String commit, String author, String commit_time, String log, List<String> files)
	 {
		this.id = id;
		this.bug_id=bug_id;
		this.summary=summary;
		this.description=description;
		this.report_time=report_time;
		this.reporter=reporter;
		this.assignee=assignee;
		this.status=status;
		this.product=product;
		this.component=component;
		this.importance=importance;
		this.commit=commit;
		this.author=author;
		this.commit_time=commit_time;
		this.log=log;
		this.files=files;
	 }
}
