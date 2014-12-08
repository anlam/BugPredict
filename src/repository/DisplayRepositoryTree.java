
/*
 * ====================================================================
 * Copyright (c) 2004-2011 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.SVNRepository;

import utils.Logger;


/*
 * This example shows how to get the repository tree at the latest (HEAD)
 * revision starting with the directory that is the path/to/repository part of
 * the repository location URL. The main point is SVNRepository.getDir() method
 * that is called recursively for each directory (till the end of the tree).
 * getDir collects all entries located inside a directory and returns them as a
 * java.util.Collection. As an example here's one of the program layouts (for
 * the default url used in the program ):
 * 
 * Repository Root: http://svn.svnkit.com/repos/svnkit
 * Repository UUID: 0a862816-5deb-0310-9199-c792c6ae6c6e
 * 
 * /examples (author: 'sa'; revision: 2794; date: Tue Nov 14 03:21:11 NOVT 2006)
 * /examples/svnkit-examples.iml (author: 'alex'; revision: 2775; date: Fri Nov 10 02:08:45 NOVT 2006)
 * /examples/src (author: 'sa'; revision: 2794; date: Tue Nov 14 03:21:11 NOVT 2006)
 * /examples/src/org (author: 'sa'; revision: 2794; date: Tue Nov 14 03:21:11 NOVT 2006)
 * /examples/src/org/tmatesoft (author: 'sa'; revision: 2794; date: Tue Nov 14 03:21:11 NOVT 2006)
 * /examples/src/org/tmatesoft/svn (author: 'sa'; revision: 2794; date: Tue Nov 14 03:21:11 NOVT 2006)
 * /examples/src/org/tmatesoft/svn/examples (author: 'sa'; revision: 2794; date: Tue Nov 14 03:21:11 NOVT 2006)
 * /examples/src/org/tmatesoft/svn/examples/wc (author: 'alex'; revision: 2776; date: Fri Nov 10 02:25:08 NOVT 2006)
 * ......................................................
 * ---------------------------------------------
 * Repository latest revision: 2802
 */
public class DisplayRepositoryTree {
	/*
	 * args parameter is used to obtain a repository location URL, user's
	 * account name & password to authenticate him to the server.
	 */
	public static void main(String[] args) {

		/*
		 * Default values:
		 */
		String url = "https://anhnguyenlt:8443/svn";
		String name = "anh";
		String password = "Vietus09";

		SVNRepository repository = SVNUtils.doAuthenticate(url, name, password);

		/*
		 * Initializes the library (it must be done before ever using the
		 * library itself)
		 */
		SVNUtils.setupLibrary();

		List<String> narrowEntries = getNarrowEntries(repository, url );
		Logger.log(narrowEntries);

	}

	public static List<String> getNarrowEntries(SVNRepository repository, String url){
		List<String> allEntries = new ArrayList<String>();
		try {
			/*
			 * Checks up if the specified path/to/repository part of the URL
			 * really corresponds to a directory. If doesn't the program exits.
			 * SVNNodeKind is that one who says what is located at a path in a
			 * revision. -1 means the latest revision.
			 */
			SVNNodeKind nodeKind = repository.checkPath("", -1);
			if (nodeKind == SVNNodeKind.NONE) {
				System.err.println("There is no entry at '" + url + "'.");
//				System.exit(1);
			} else if (nodeKind == SVNNodeKind.FILE) {
				System.err.println("The entry at '" + url + "' is a file while a directory was expected.");
//				System.exit(1);
			}
			/*
			 * getRepositoryRoot() returns the actual root directory where the
			 * repository was created. 'true' forces to connect to the repository 
			 * if the root url is not cached yet. 
			 */
//			System.out.println("Repository Root: " + repository.getRepositoryRoot(true));
			/*
			 * getRepositoryUUID() returns Universal Unique IDentifier (UUID) of the 
			 * repository. 'true' forces to connect to the repository 
			 * if the UUID is not cached yet.
			 */
//			System.out.println("Repository UUID: " + repository.getRepositoryUUID(true));
//			System.out.println("");

			/*
			 * Displays the repository tree at the current path - "" (what means
			 * the path/to/repository directory)
			 */
			allEntries =  list1LevelEntries(repository, "");
		} catch (SVNException svne) {
			System.err.println("error while listing entries: "
					+ svne.getMessage());
//			System.exit(1);
		}
		
		
		return allEntries;
	}

	
	public static List<String> getEntries(SVNRepository repository, String url){
		List<String> allEntries = new ArrayList<String>();
		try {
			/*
			 * Checks up if the specified path/to/repository part of the URL
			 * really corresponds to a directory. If doesn't the program exits.
			 * SVNNodeKind is that one who says what is located at a path in a
			 * revision. -1 means the latest revision.
			 */
			SVNNodeKind nodeKind = repository.checkPath("", -1);
			if (nodeKind == SVNNodeKind.NONE) {
				System.err.println("There is no entry at '" + url + "'.");
//				System.exit(1);
			} else if (nodeKind == SVNNodeKind.FILE) {
				System.err.println("The entry at '" + url + "' is a file while a directory was expected.");
//				System.exit(1);
			}
			/*
			 * getRepositoryRoot() returns the actual root directory where the
			 * repository was created. 'true' forces to connect to the repository 
			 * if the root url is not cached yet. 
			 */
//			System.out.println("Repository Root: " + repository.getRepositoryRoot(true));
			/*
			 * getRepositoryUUID() returns Universal Unique IDentifier (UUID) of the 
			 * repository. 'true' forces to connect to the repository 
			 * if the UUID is not cached yet.
			 */
//			System.out.println("Repository UUID: " + repository.getRepositoryUUID(true));
//			System.out.println("");

			/*
			 * Displays the repository tree at the current path - "" (what means
			 * the path/to/repository directory)
			 */
			allEntries =  listEntries(repository, "");
		} catch (SVNException svne) {
			System.err.println("error while listing entries: "
					+ svne.getMessage());
//			System.exit(1);
		}
		
		
		return allEntries;
	}

	
	public static List<String> getFilteredEntries(SVNRepository repository, String url, String[] fileExtFilter){
		List<String> allEntries = new ArrayList<String>();
		try {
			/*
			 * Checks up if the specified path/to/repository part of the URL
			 * really corresponds to a directory. If doesn't the program exits.
			 * SVNNodeKind is that one who says what is located at a path in a
			 * revision. -1 means the latest revision.
			 */
			SVNNodeKind nodeKind = repository.checkPath("", -1);
			if (nodeKind == SVNNodeKind.NONE) {
				System.err.println("There is no entry at '" + url + "'.");
//				System.exit(1);
			} else if (nodeKind == SVNNodeKind.FILE) {
				System.err.println("The entry at '" + url + "' is a file while a directory was expected.");
//				System.exit(1);
			}
			/*
			 * getRepositoryRoot() returns the actual root directory where the
			 * repository was created. 'true' forces to connect to the repository 
			 * if the root url is not cached yet. 
			 */
//			System.out.println("Repository Root: " + repository.getRepositoryRoot(true));
			/*
			 * getRepositoryUUID() returns Universal Unique IDentifier (UUID) of the 
			 * repository. 'true' forces to connect to the repository 
			 * if the UUID is not cached yet.
			 */
//			System.out.println("Repository UUID: " + repository.getRepositoryUUID(true));
//			System.out.println("");

			/*
			 * Displays the repository tree at the current path - "" (what means
			 * the path/to/repository directory)
			 */
			allEntries =  listEntries(repository, "");
		} catch (SVNException svne) {
			System.err.println("error while listing entries: "
					+ svne.getMessage());
//			System.exit(1);
		}
		
		List<String> filteredEntries = new ArrayList<String>();
		for (String entry:allEntries){
			for (String ext:fileExtFilter){
				if (entry.trim().endsWith(ext)){
					filteredEntries.add(entry.trim());
					break;
				}
			}
		}
		return filteredEntries;
	}

	/*
	 * Called recursively to obtain all entries that make up the repository tree
	 * repository - an SVNRepository which interface is used to carry out the
	 * request, in this case it's a request to get all entries in the directory
	 * located at the path parameter;
	 * 
	 * path is a directory path relative to the repository location path (that
	 * is a part of the URL used to create an SVNRepository instance);
	 *  
	 */
	public static List<String> listEntries(SVNRepository repository, String path)
			throws SVNException {
		/*
		 * Gets the contents of the directory specified by path at the latest
		 * revision (for this purpose -1 is used here as the revision number to
		 * mean HEAD-revision) getDir returns a Collection of SVNDirEntry
		 * elements. SVNDirEntry represents information about the directory
		 * entry. Here this information is used to get the entry name, the name
		 * of the person who last changed this entry, the number of the revision
		 * when it was last changed and the entry type to determine whether it's
		 * a directory or a file. If it's a directory listEntries steps into a
		 * next recursion to display the contents of this directory. The third
		 * parameter of getDir is null and means that a user is not interested
		 * in directory properties. The fourth one is null, too - the user
		 * doesn't provide its own Collection instance and uses the one returned
		 * by getDir.
		 */
		Collection<?> entries = repository.getDir(path, -1, null,
				(Collection<?>) null);
		Iterator<?> iterator = entries.iterator();
		List<String> allEntries = new ArrayList<String>();
		while (iterator.hasNext()) {
			SVNDirEntry entry = (SVNDirEntry) iterator.next();
//			System.out.println("/" + (path.equals("") ? "" : path + "/")
//					+ entry.getName() + " (author: '" + entry.getAuthor()
//					+ "'; revision: " + entry.getRevision() + "; date: " + entry.getDate() + ")");
			
			String entryPath = "/" + (path.equals("") ? "" : path + "/") + entry.getName();
			allEntries.add(entryPath);
			/*
			 * Checking up if the entry is a directory.
			 */
			if (entry.getKind() == SVNNodeKind.DIR) {
				List<String> subEntries = listEntries(repository, (path.equals("")) ? entry.getName()
						: path + "/" + entry.getName());
				allEntries.addAll(subEntries);
			}
		}
		return allEntries;
	}
	
	
	/*
	 * Called recursively to obtain all entries that make up the repository tree
	 * repository - an SVNRepository which interface is used to carry out the
	 * request, in this case it's a request to get all entries in the directory
	 * located at the path parameter;
	 * 
	 * path is a directory path relative to the repository location path (that
	 * is a part of the URL used to create an SVNRepository instance);
	 *  
	 */
	public static List<String> list1LevelEntries(SVNRepository repository, String path)
			throws SVNException {
		/*
		 * Gets the contents of the directory specified by path at the latest
		 * revision (for this purpose -1 is used here as the revision number to
		 * mean HEAD-revision) getDir returns a Collection of SVNDirEntry
		 * elements. SVNDirEntry represents information about the directory
		 * entry. Here this information is used to get the entry name, the name
		 * of the person who last changed this entry, the number of the revision
		 * when it was last changed and the entry type to determine whether it's
		 * a directory or a file. If it's a directory listEntries steps into a
		 * next recursion to display the contents of this directory. The third
		 * parameter of getDir is null and means that a user is not interested
		 * in directory properties. The fourth one is null, too - the user
		 * doesn't provide its own Collection instance and uses the one returned
		 * by getDir.
		 */
		Collection<?> entries = repository.getDir(path, -1, null,
				(Collection<?>) null);
		Iterator<?> iterator = entries.iterator();
		List<String> allEntries = new ArrayList<String>();
		while (iterator.hasNext()) {
			SVNDirEntry entry = (SVNDirEntry) iterator.next();
//			System.out.println("/" + (path.equals("") ? "" : path + "/")
//					+ entry.getName() + " (author: '" + entry.getAuthor()
//					+ "'; revision: " + entry.getRevision() + "; date: " + entry.getDate() + ")");
			
			String entryPath = "/" + (path.equals("") ? "" : path + "/") + entry.getName();
			allEntries.add(entryPath);
//			/*
//			 * Checking up if the entry is a directory.
//			 */
//			if (entry.getKind() == SVNNodeKind.DIR) {
//				List<String> subEntries = listEntries(repository, (path.equals("")) ? entry.getName()
//						: path + "/" + entry.getName());
//				allEntries.addAll(subEntries);
//			}
		}
		return allEntries;
	}
}