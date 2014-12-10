package repository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.io.NullOutputStream;

import utils.FileIO;
import utils.Logger;

public class GitConnector extends VCSConnector {
	private String url;
	private int numberOfCommits = -1, numberOfCodeCommits = -1;
	
	public Git getGit() {
		return git;
	}

	public Repository getRepository() {
		return repository;
	}

	private Git git;
	private Repository repository;
	
	public GitConnector(String url) {
		this.url = url;
	}
	
	public int getNumberOfCommits() {
		return numberOfCommits;
	}

	public int getNumberOfCodeCommits() {
		return numberOfCodeCommits;
	}

	public boolean connect() {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			repository = builder.setGitDir(new File(url))
			  .readEnvironment() // scan environment GIT_* variables
			  .findGitDir() // scan up the file system tree
			  .build();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return false;
		}
		git = new Git(repository);
		return true;
	}
	
	public Iterable<RevCommit> log() {
		try {
			return git.log().call();
		} catch (GitAPIException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}
	
	public void getFileChanges(String extension) {
		Iterable<RevCommit> commits = null;
		try {
			commits = git.log().call();
		} catch (GitAPIException e) {
			System.err.println(e.getMessage());
		}
		if (commits == null) return;
		for (RevCommit commit : commits) {
			if (commit.getParentCount() > 0) {
				RevWalk rw = new RevWalk(repository);
				RevCommit parent = null;
				try {
					parent = rw.parseCommit(commit.getParent(0).getId());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
				if (parent == null) continue;
				DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE);
				df.setRepository(repository);
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);
				if (extension != null)
					df.setPathFilter(PathSuffixFilter.create(extension));
				List<DiffEntry> diffs = null;
				try {
					diffs = df.scan(parent.getTree(), commit.getTree());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
				if (diffs == null) continue;
				if (!diffs.isEmpty()) {
					//System.out.println(commit.getName());
					System.out.println(commit.getCommitTime());
					//System.out.println(commit.getFullMessage());
					for (DiffEntry diff : diffs) {
						if (diff.getOldMode().getObjectType() == Constants.OBJ_BLOB && diff.getNewMode().getObjectType() == Constants.OBJ_BLOB) {
							//System.out.println(diff.getChangeType() + ": " + diff.getOldPath() + " --> " + diff.getNewPath());
							ObjectLoader ldr = null;
							String oldContent = null, newContent = null;
							try {
								ldr = repository.open(diff.getOldId().toObjectId(), Constants.OBJ_BLOB);
								oldContent = new String(ldr.getCachedBytes());
							} catch (IOException e) {
								System.err.println(e.getMessage());
							}
							try {
								ldr = repository.open(diff.getNewId().toObjectId(), Constants.OBJ_BLOB);
								newContent = new String(ldr.getCachedBytes());
							} catch (IOException e) {
								System.err.println(e.getMessage());
							}
							/*System.out.println(oldContent);
							System.out.println(newContent);*/
						}
					}
				}
			}
		}
	}
	
	public  LinkedHashMap<String, String> getFileContent(String commitTag, List<String> files, String extension)
	{
		if(files == null|| files.isEmpty())
			return null;
		
		LinkedHashMap<String, String> filesConntent = new LinkedHashMap<>();
		
		try
		{
			ObjectId lastCommitId = repository.resolve(commitTag);
			//if(lastCommitId == null)
			//	System.out.println("Last Commit Null");
		    RevWalk revWalk = new RevWalk(repository);
		    RevCommit commit = revWalk.parseCommit(lastCommitId);
		    RevTree tree= commit.getTree();
		    TreeWalk treeWalk = new TreeWalk(repository);
		    treeWalk.addTree(tree);
		    treeWalk.setRecursive(true);
		    
		    List<TreeFilter> filters = new ArrayList<>();
		    for(String file : files)
		    {
		    	if(file.endsWith(extension))
		    	{
		    		filters.add(PathFilter.create(file));
		    	}	    	
		    }
			
			if(!filters.isEmpty())
			{
				TreeFilter filter;
				if(filters.size() > 1)
					 filter = OrTreeFilter.create(filters);
				else
					filter = filters.get(0);
					
				treeWalk.setFilter(filter);

				while(treeWalk.next())
				{
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					System.out.println(treeWalk.getPathString());				
					ByteArrayOutputStream out = new ByteArrayOutputStream();
				    loader.copyTo(out);    
				    filesConntent.put(treeWalk.getPathString(), out.toString());
				}
		
		    }
			else
				return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	    
	    //treeWalk.setFilter(PathSuffixFilter.create(".java"))
	    //treeWalk.setFilter(PathFilter.create("src/main/java/nds/socket/server/Reader.java"));

		
		return filesConntent;
	}
	
	public String getFileContent(ObjectId objectId, int objectType) {
		String content = null;
		try {
			ObjectLoader ldr = repository.open(objectId, objectType);
			content = new String(ldr.getCachedBytes());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return content;
	}

	private String getFileContent(ObjectId objectId) {
		return getFileContent(objectId, Constants.OBJ_BLOB);
	}
	
	public int[] getLastSnapshotCounters(String extension) {
		int numOfFiles = 0, numOfLOCs = 0;
		RevWalk rw = new RevWalk(repository);
		try {
			ObjectId object = repository.resolve(Constants.HEAD);
			RevCommit commit = rw.parseCommit(object);
			TreeWalk tw = new TreeWalk(repository);
			tw.reset();
			try {
				tw.addTree(commit.getTree());
				tw.setRecursive(true);
				while (tw.next()) {
					if (!tw.isSubtree()) {
						String path = tw.getPathString();
						if (extension == null || path.endsWith(extension)) {
							numOfFiles++;
							numOfLOCs += FileIO.getNumOfLines(getFileContent(tw.getObjectId(0)));
						}
					}
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
			tw.release();
		} catch (RevisionSyntaxException | IOException e) {
			System.err.println(e.getMessage());
		}
		rw.release();
		
		int[] counters = new int[2];
		counters[0] = numOfFiles; counters[1] = numOfLOCs;
		return counters;
	}
	
	public void getLastSnapshot(String extension) {
		RevWalk rw = new RevWalk(repository);
		try {
			//ObjectID object = repository.
			ObjectId object = repository.resolve(Constants.HEAD);
			RevCommit commit = rw.parseCommit(object);
			TreeWalk tw = new TreeWalk(repository);
			tw.reset();
			try {
				tw.addTree(commit.getTree());
				tw.setRecursive(true);
				while (tw.next()) {
					if (!tw.isSubtree()) {
						String path = tw.getPathString();
						if (extension == null || path.endsWith(extension)) {
							System.out.println(path);
							System.out.println(getFileContent(tw.getObjectId(0)));
						}
					}
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
			tw.release();
		} catch (RevisionSyntaxException | IOException e) {
			System.err.println(e.getMessage());
		}
		rw.release();
	}
	
	public void getSnapshots(String extension) {
		Iterable<RevCommit> commits = null;
		try {
			commits = git.log().call();
		} catch (GitAPIException e) {
			System.err.println(e.getMessage());
		}
		if (commits == null) return;
		for (RevCommit commit : commits) {
			TreeWalk tw = new TreeWalk(repository);
			tw.reset();
			try {
				tw.addTree(commit.getTree());
				tw.setRecursive(true);
				while (tw.next()) {
					if (!tw.isSubtree()) {
						String path = tw.getPathString();
						if (extension == null || path.endsWith(extension)) {
							System.out.println(path);
							System.out.println(getFileContent(tw.getObjectId(0)));
						}
					}
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
				continue;
			}
			tw.release();
		}
	}
	
	public ArrayList<Integer> getJavaFixRevisions() {
		ArrayList<Integer> revisions = new ArrayList<>();
		Iterable<RevCommit> commits = null;
		try {
			commits = git.log().call();
		} catch (GitAPIException e) {
			System.err.println(e.getMessage());
		}
		if (commits == null) return revisions;
		for (RevCommit commit : commits) {
			if (commit.getParentCount() > 0) {
				if (!isFixingCommit(commit.getFullMessage())) continue;
				RevWalk rw = new RevWalk(repository);
				RevCommit parent = null;
				try {
					parent = rw.parseCommit(commit.getParent(0).getId());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
				if (parent == null) continue;
				DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE);
				df.setRepository(repository);
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);
				df.setPathFilter(PathSuffixFilter.create(".java"));
				List<DiffEntry> diffs = null;
				try {
					diffs = df.scan(parent.getTree(), commit.getTree());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
				if (diffs != null && !diffs.isEmpty()) {
					int index = Collections.binarySearch(revisions, commit.getCommitTime());
					if (index < 0) index = -index - 1;
					revisions.add(index, commit.getCommitTime());
					
					
					Logger.logDebug(getBugID(commit.getFullMessage()) + "-" + commit.getId().getName());
					for (DiffEntry diff : diffs) 
					{
						Logger.logDebug(MessageFormat.format("({0} {1}", diff.getChangeType().name(), /*diff.getNewMode().getBits(),*/ diff.getNewPath()));
					}
				
				}
			}
		}
		return revisions;
	}

	public int getNumberOfCommits(String extension) {
		Iterable<RevCommit> commits = null;
		try {
			commits = git.log().call();
		} catch (GitAPIException e) {
			System.err.println(e.getMessage());
		}
		if (commits == null) return -1;
		this.numberOfCommits = 0;
		this.numberOfCodeCommits = 0;
		for (RevCommit commit : commits) {
			this.numberOfCommits++;
			if (extension == null) {
				continue;
			}
			if (commit.getParentCount() > 0) {
				RevWalk rw = new RevWalk(repository);
				RevCommit parent = null;
				try {
					parent = rw.parseCommit(commit.getParent(0).getId());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
				if (parent == null) continue;
				DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE);
				df.setRepository(repository);
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);
				if (extension != null)
					df.setPathFilter(PathSuffixFilter.create(extension));
				List<DiffEntry> diffs = null;
				try {
					diffs = df.scan(parent.getTree(), commit.getTree());
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
				if (diffs == null) continue;
				if (!diffs.isEmpty()) {
					this.numberOfCodeCommits++;
				}
			}
		}
		//git.
		if (extension == null) return this.numberOfCommits;
		return this.numberOfCodeCommits;
	}
}
