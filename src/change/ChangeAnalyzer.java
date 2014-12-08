package change;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;

import config.ChangeConfig;
import data.DiffPairsExtract;
import repository.SVNConnector;
import utils.Logger;

public class ChangeAnalyzer {
	
	private String projectName;
	private int projectId;
	private String svnUrl;
	private long startRevision = -1, endRevision = -1;
	private SVNConnector svnConn;
	private HashMap<Long, SVNLogEntry> logEntries;
	private ArrayList<RevisionAnalyzer> revisionAnalyzers = new ArrayList<RevisionAnalyzer>();
	
	public ChangeAnalyzer(String projectName, int projectId, String svnUrl, long start, long end) {
		this.projectName = projectName;
		this.projectId = projectId;
		this.svnUrl = svnUrl;
		this.startRevision = start;
		this.endRevision = end;
	}
	
	public ChangeAnalyzer(String projectName, String svnUrl, long start, long end) {
		this.projectName = projectName;
		this.svnUrl = svnUrl;
		this.startRevision = start;
		this.endRevision = end;
	}
	public ChangeAnalyzer(String projectName, int projectId, String svnUrl) {
		this.projectName = projectName;
		this.projectId = projectId;
		this.svnUrl = svnUrl;
		System.out.println(this.svnUrl);
	}
	
	
	public ChangeAnalyzer(String projectName, String svnUrl) {
		this.projectName = projectName;
		this.svnUrl = svnUrl;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public SVNConnector getSvnConn() {
		return this.svnConn;
	}

	public long getStartRevision() {
		return startRevision;
	}

	public long getEndRevision() {
		return endRevision;
	}
	
	public SVNLogEntry getLogEntry(long revision) {
		return this.logEntries.get(revision);
	}

	public ArrayList<RevisionAnalyzer> getRevisionAnalyzers() {
		return revisionAnalyzers;
	}

	public void buildSvnConnector(String account, String password) {
		svnConn = new SVNConnector(svnUrl, account, password);
		svnConn.connect();
		if (this.startRevision == -1) {
			this.startRevision = 1;
			svnConn.setLatestRevision();
			this.endRevision = svnConn.getLatestRevision();
		}
	}
	
	public void buildLogEntries() {
		this.logEntries = new HashMap<Long, SVNLogEntry>();
        long start = this.startRevision;
		while (start <= this.endRevision) {
			long end = start + 99;
			if (end > this.endRevision) {
				end = this.endRevision;
			}
			buildLogEntries(start, end);
			start = end + 1;
			System.out.println(this.logEntries.size());
		}
	}
	
	public void buildLogEntries(long startRevision, long endRevision) {
		Collection<?> logEntries = null;
        try {
            logEntries = svnConn.getRepository().log(new String[] {""}, null, startRevision, endRevision, true, true);
        } catch (SVNException svne) {
            System.out.println("Error while collecting log information for '"
                    + svnUrl + "': " + svne.getMessage());
            System.exit(1);
        }
        for (Iterator<?> entries = logEntries.iterator(); entries.hasNext();) {
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            this.logEntries.put(logEntry.getRevision(), logEntry);
        }
	}
		
	public void buildLogAndAnalyze(String outputDataPath) {
		this.logEntries = new HashMap<Long, SVNLogEntry>();
		DiffPairsExtract diffPairsExtract = new DiffPairsExtract(projectName);
		System.out.println("\r\n*******************\r\nProject: " + projectName);
		System.out.print(" Analyzing: ");
		
        long start = this.startRevision;
		while (start <= this.endRevision) {
			long end = start + 99;
			if (end > this.endRevision) {
				end = this.endRevision;
			}
			buildLogEntries(start, end);
			analyze(start, end, diffPairsExtract);
			start = end + 1;
//			System.out.println(this.logEntries.size() + " ");
		}
		System.out.println();
		String outputDiffPairFilePath = outputDataPath + projectName +ChangeConfig.diffPostFix + ChangeConfig.datExt;
		diffPairsExtract.writeData(outputDiffPairFilePath);
		
//		DiffPairsExtract recDiffPairsExtract = DiffPairsExtract.readData(outputDiffPairFilePath);
//		Logger.log(recDiffPairsExtract.diffPairMethodListMap);
	}
	

	public void analyze(long startRevision, long endRevision, DiffPairsExtract diffPairsExtract )
	{

		

		for (long r = startRevision; r <= endRevision; r++)
		{
			
			TreeMap<Integer, Integer> heightCount = new TreeMap<Integer, Integer>(); 
			if (this.logEntries.containsKey(r)) {
				if (r%100==0){
					System.out.print(r + "  ");
				}
				try{
//					System.out.println("Analyzing r " + r + " of " + projectName);
					Date revDate = this.logEntries.get(r).getDate();
//					Logger.log(revDate);
				
					long dateValue = revDate.getTime();
//					Logger.log(dateValue);
					diffPairsExtract.updateRevisionDate(r, dateValue);
					
					RevisionAnalyzer ra = new RevisionAnalyzer(this, r);
					//this.revisionAnalyzers.add(ra);
					ra.analyze();
					HashSet<CMethod> methodsM = ra.getMappedMethodsM(), methodsN = ra.getMappedMethodsN();
					HashSet<CField> fieldsM = ra.getMappedFieldsM(), fieldsN = ra.getMappedFieldsN();
					HashSet<CInitializer> initsM = ra.getMappedInitsM(), initsN = ra.getMappedInitsN();
					HashMap<CMethod, ArrayList<TreeChange>> changeMap  =new HashMap<>();
					for (CMethod e : methodsM) {
	//					System.out.println("Method: " + e.getQualName() + " - " + e.getMappedEntity().getQualName());
						ArrayList<TreeChange> changes = e.getTreeChanges();
						for (TreeChange change:changes){
							int height = change.height;
//							Logger.log("\t" + height);
							if (heightCount.containsKey(height)){
								heightCount.put(height, heightCount.get(height) + 1);
							}
							else{
								heightCount.put(height, 1);
							}
						}
	//					System.out.println(changes);
						diffPairsExtract.getAllDiffPairs(changes, r);
						changeMap.put(e, changes);
						/*String diff = e.printTree();
						System.out.println(diff);*/
					}
					String log = this.logEntries.get(r).getMessage();
					diffPairsExtract.addChangeMap(r, log, changeMap);
					/*for (CField e : fieldsM) {
						System.out.println("Field: " + e.getQualName());
						String diff = e.printTree();
						System.out.println(diff);
					}*/
					
					for (CInitializer e : initsM) {
	//					System.out.println("Init: " + e.getQualName());
						ArrayList<TreeChange> changes = e.getTreeChanges();
						
						diffPairsExtract.getAllDiffPairs(changes, r);
	
						for (TreeChange change:changes){

							int height = change.height;
//							Logger.log("\t" + height);

							if (heightCount.containsKey(height)){
								heightCount.put(height, heightCount.get(height) + 1);
							}
							else{
								heightCount.put(height, 1);
							}
						}
	//					System.out.println(changes);
	
						/*String diff = e.printTree();
						System.out.println(diff);*/
					}
					
//					boolean isBigger =  false;
//					int lastVal = Integer.MAX_VALUE;
//					for (int i:heightCount.keySet())
//					{
//						if ( heightCount.get(i) > lastVal){
//							isBigger = true;
//							break;
//						}
//						lastVal = heightCount.get(i);
//					}
//					if (isBigger)
//					{
//						Logger.log(heightCount);
//					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
			}
		}
		
		
		
//		DiffPairsExtract diffPairsExtractTmp = DiffPairsExtract.readData(outputDiffPairFilePath);
//		Logger.log(diffPairsExtractTmp);
	}

	public String getSourceCode(String changedPath, long revision) {
		return this.svnConn.getFile(changedPath, revision);
	}
}
