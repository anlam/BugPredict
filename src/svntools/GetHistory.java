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
package svntools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import repository.RepoListProcessing;
import config.ChangeConfig;
import config.GlobalConfig;
import utils.Logger;
import data.SVNHistoryData;
import data.SVNRevData;
import dirtool.DirProcessing;

/*
 * The following example program demonstrates how you can use SVNRepository to
 * obtain a history for a range of revisions including (for each revision): all
 * changed paths, log message, the author of the commit, the timestamp when the 
 * commit was made. It is similar to the "svn log" command supported by the 
 * Subversion client library.
 * 
 * As an example here's a part of one of the program layouts (for the default
 * values):
 * 
 * ---------------------------------------------
 * revision: 1240
 * author: alex
 * date: Tue Aug 02 19:52:49 NOVST 2005
 * log message: 0.9.0 is now trunk
 *
 * changed paths:
 *  A  /trunk (from /branches/0.9.0 revision 1239)
 * ---------------------------------------------
 * revision: 1263
 * author: sa
 * date: Wed Aug 03 21:19:55 NOVST 2005
 * log message: updated examples, javadoc files
 *
 * changed paths:
 *  M  /trunk/doc/javadoc-files/javadoc.css
 *  M  /trunk/doc/javadoc-files/overview.html
 *  M  /trunk/doc/examples/src/org/tmatesoft/svn/examples/wc/StatusHandler.java
 * ...
 * 
 */
public class GetHistory {


	/*
	 * args parameter is used to obtain a repository location URL, a start
	 * revision number, an end revision number, user's account name & password
	 * to authenticate him to the server.
	 */
	public static void main(String[] args) {
		if (args.length>=3)
		{
			GlobalConfig.repoFilePath = args[0];
			GlobalConfig.mainDir = args[1];
			ChangeConfig.svnRootUrl = args[2];

			GlobalConfig.refreshParams();
			ChangeConfig.refreshParams();
		}
		List<String> repoList = RepoListProcessing.getRepoListFromFile(GlobalConfig.repoFilePath);

		for (String project:repoList)
		{
			Logger.log("\r\n*****************\r\nproject:" + project);
			doMain(project);
		}
	}

	public static void doMain(String projectName){

		Logger.initDebug(ChangeConfig.logPath);
		String url = ChangeConfig.svnRootUrl + projectName ;//ChangeConfig.svnUrl;
		String account = ChangeConfig.account;
		String password = ChangeConfig.password;
		long startRevision = 0;
		long endRevision = -1;
		ChangeConfig.SVNHistoryPath = ChangeConfig.changePath  + projectName +"_SVNHistory.dat";
		doMain(url, account, password, startRevision, endRevision, ChangeConfig.SVNHistoryPath);

		Logger.closeDebug();
	}
	public static void doMain(String url, String account, String password, long startRevision, long endRevision,
			String historyDataPath) {


		TreeMap<Long, SVNRevData> SVNRevDataMap = new TreeMap<>(); 
		/*
		 * Initializes the library (it must be done before ever using the
		 * library itself)
		 */
		setupLibrary();



		SVNRepository repository = null;

		try {
			/*
			 * Creates an instance of SVNRepository to work with the repository.
			 * All user's requests to the repository are relative to the
			 * repository location used to create this SVNRepository.
			 * SVNURL is a wrapper for URL strings that refer to repository locations.
			 */
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
		} catch (SVNException svne) {
			/*
			 * Perhaps a malformed URL is the cause of this exception.
			 */
			Logger.logDebug("error while creating an SVNRepository for the location '"
					+ url + "': " + svne.getMessage());
			//			System.exit(1);
		}

		/*
		 * User's authentication information (name/password) is provided via  an 
		 * ISVNAuthenticationManager  instance.  SVNWCUtil  creates  a   default 
		 * authentication manager given user's name and password.
		 * 
		 * Default authentication manager first attempts to use provided user name 
		 * and password and then falls back to the credentials stored in the 
		 * default Subversion credentials storage that is located in Subversion 
		 * configuration area. If you'd like to use provided user name and password 
		 * only you may use BasicAuthenticationManager class instead of default 
		 * authentication manager:
		 * 
		 *  authManager = new BasicAuthenticationsManager(userName, userPassword);
		 *  
		 * You may also skip this point - anonymous access will be used. 
		 */
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(account, password);
		repository.setAuthenticationManager(authManager);

		ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		SVNClientManager clientManager = SVNClientManager.newInstance(options, repository.getAuthenticationManager());
		SVNDiffClient diffClient = clientManager.getDiffClient();

		/*
		 * Gets the latest revision number of the repository
		 */
		try {
			endRevision = repository.getLatestRevision();
		} catch (SVNException svne) {
			Logger.logDebug("error while fetching the latest repository revision: " + svne.getMessage());
			//			System.exit(1);
		}

		Collection logEntries = null;
		try {
			/*
			 * Collects SVNLogEntry objects for all revisions in the range
			 * defined by its start and end points [startRevision, endRevision].
			 * For each revision commit information is represented by
			 * SVNLogEntry.
			 * 
			 * the 1st parameter (targetPaths - an array of path strings) is set
			 * when restricting the [startRevision, endRevision] range to only
			 * those revisions when the paths in targetPaths were changed.
			 * 
			 * the 2nd parameter if non-null - is a user's Collection that will
			 * be filled up with found SVNLogEntry objects; it's just another
			 * way to reach the scope.
			 * 
			 * startRevision, endRevision - to define a range of revisions you are
			 * interested in; by default in this program - startRevision=0, endRevision=
			 * the latest (HEAD) revision of the repository.
			 * 
			 * the 5th parameter - a boolean flag changedPath - if true then for
			 * each revision a corresponding SVNLogEntry will contain a map of
			 * all paths which were changed in that revision.
			 * 
			 * the 6th parameter - a boolean flag strictNode - if false and a
			 * changed path is a copy (branch) of an existing one in the repository
			 * then the history for its origin will be traversed; it means the 
			 * history of changes of the target URL (and all that there's in that 
			 * URL) will include the history of the origin path(s).
			 * Otherwise if strictNode is true then the origin path history won't be
			 * included.
			 * 
			 * The return value is a Collection filled up with SVNLogEntry Objects.
			 */
			logEntries = repository.log(new String[] {""}, null,
					startRevision, endRevision, true, true);

		} catch (SVNException svne) {
			Logger.logDebug("error while collecting log information for '"
					+ url + "': " + svne.getMessage());
			//			System.exit(1);
		}
		
		Logger.log("revision: " );

		for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
			/*
			 * gets a next SVNLogEntry
			 */
			SVNLogEntry logEntry = (SVNLogEntry) entries.next();
			Logger.logDebug("---------------------------------------------");


			/*
			 * gets the revision number
			 */
			Logger.logDebug("revision: " + logEntry.getRevision());
			long revision = logEntry.getRevision();
			if (revision%100==0)
				System.out.print("  " +revision);
			/*
			 * gets the author of the changes made in that revision
			 */
			Logger.logDebug("author: " + logEntry.getAuthor());
			String author = logEntry.getAuthor();
			/*
			 * gets the time moment when the changes were committed
			 */
			Logger.logDebug("date: " + logEntry.getDate());
			long dateVal  = -1;
			try{
				dateVal = logEntry.getDate().getTime();
			}
			catch(Exception e){

			}
			/*
			 * gets the commit log message
			 */
			Logger.logDebug("log message: " + logEntry.getMessage());
			String log = logEntry.getMessage();


			/*
			 * displaying all paths that were changed in that revision; cahnged
			 * path information is represented by SVNLogEntryPath.
			 */
			LinkedHashMap<String, String> changedPathOldPath = new LinkedHashMap<>();
			LinkedHashMap<String, String> changedPathType = new LinkedHashMap<>(); 
			LinkedHashMap<String, String> changedPathContentPrev = new LinkedHashMap<>();
			LinkedHashMap<String, String> changedPathContentNext = new LinkedHashMap<>();

			long prevRevision = revision - 1L;

			if (logEntry.getChangedPaths().size() > 0) {
				Logger.logDebug("");
				Logger.logDebug("changed paths:");
				/*
				 * keys are changed paths
				 */
				Set changedPathsSet = logEntry.getChangedPaths().keySet();

				for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths
						.hasNext();) {
					/*
					 * obtains a next SVNLogEntryPath
					 */
					SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry
							.getChangedPaths().get(changedPaths.next());
					/*
					 * SVNLogEntryPath.getPath returns the changed path itself;
					 * 
					 * SVNLogEntryPath.getType returns a charecter describing
					 * how the path was changed ('A' - added, 'D' - deleted or
					 * 'M' - modified);
					 * 
					 * If the path was copied from another one (branched) then
					 * SVNLogEntryPath.getCopyPath &
					 * SVNLogEntryPath.getCopyRevision tells where it was copied
					 * from and what revision the origin path was at.
					 */
					Logger.logDebug(" "
							+ entryPath.getType()
							+ "	"
							+ entryPath.getPath()
							+ ((entryPath.getCopyPath() != null) ? " (from "
									+ entryPath.getCopyPath() + " revision "
									+ entryPath.getCopyRevision() + ")" : ""));
					String changedPath = entryPath.getPath();
					if (!DirProcessing.isPassFileName(changedPath, ChangeConfig.allSourceFileExt)){
						continue;
					}
					if (entryPath.getCopyPath()==null)
					{
						changedPathOldPath.put(changedPath, null);
						String changeType = String.valueOf(entryPath.getType());
						changedPathType.put(changedPath, changeType);

						if (!changeType.equals("A"))
						{
							String fileContentPrev = getFileContent(url, repository, changedPath, prevRevision);
							changedPathContentPrev.put(changedPath, fileContentPrev);
						}
						else 
						{
							changedPathContentPrev.put(changedPath, null);
						}

						if (!changeType.equals("D"))
						{
							String fileContentNext = getFileContent(url, repository, changedPath, revision);
							changedPathContentNext.put(changedPath, fileContentNext);
						}
						else
						{
							changedPathContentNext.put(changedPath, null);
						}
					}
					else
					{
						String oldPath = entryPath.getCopyPath();
						changedPathOldPath.put(changedPath, oldPath);
						String changeType = String.valueOf(entryPath.getType());
						changedPathType.put(changedPath, changeType);


						if (!changeType.equals("A"))
						{
							String fileContentPrev = getFileContent(url, repository, oldPath, prevRevision);
							changedPathContentPrev.put(changedPath, fileContentPrev);
						}
						else 
						{
							changedPathContentPrev.put(changedPath, null);
						}

						if (!changeType.equals("D"))
						{
							String fileContentNext = getFileContent(url, repository, changedPath, revision);
							changedPathContentNext.put(changedPath, fileContentNext);
						}
						else
						{
							changedPathContentNext.put(changedPath, null);
						}
					}


				}
			}

			String diffContent = null;

			//			if (revision >= 1){
			//				diffContent = getDiff(url, diffClient, prevRevision, revision);
			//			}
			SVNRevData svnRev = new SVNRevData(revision, author, dateVal, log, changedPathOldPath, 
					changedPathType, changedPathContentPrev, changedPathContentNext, diffContent);
			SVNRevDataMap.put(revision, svnRev);
		}
		Logger.log("");
		Logger.logDebug("SVNRevDataMap size: " + SVNRevDataMap.size());


		SVNHistoryData svnHistory = new SVNHistoryData(SVNRevDataMap);
//		svnHistory.writeObject(historyDataPath);
		svnHistory.writeDataFile(historyDataPath);

//		SVNHistoryData readSVNHistory = SVNHistoryData.readObject(historyDataPath);
//		SVNHistoryData readSVNHistory = SVNHistoryData.readDataFile(historyDataPath);
//		Logger.log("readSVNHistory.SVNRevDataMap.size(): " + readSVNHistory.SVNRevDataMap.size());

	}

	public static String getDiff(String url, SVNDiffClient diffClient, 
			long prevRevision, long revision){
		//do diff
		String diff = null;

		ByteArrayOutputStream result  = new ByteArrayOutputStream();
		try {
			diffClient.doDiff(SVNURL.parseURIEncoded(url), 
					SVNRevision.create(prevRevision), SVNRevision.create(prevRevision), 
					SVNRevision.create(revision), true, true, result);
			diff = result.toString();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			diff = null;
			e.printStackTrace();
		}
		Logger.logDebug("\tDiff: " + diff);

		return diff;
	}

	public static String getFileContent(String url, SVNRepository repository, String filePath, long revision){
		String fileContent = null;
		/*
		 * This Map will be used to get the file properties. Each Map key is a
		 * property name and the value associated with the key is the property
		 * value.
		 */
		SVNProperties fileProperties = new SVNProperties();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			/*
			 * Checks up if the specified path really corresponds to a file. If
			 * doesn't the program exits. SVNNodeKind is that one who says what is
			 * located at a path in a revision. -1 means the latest revision.
			 */
			SVNNodeKind nodeKind = repository.checkPath(filePath, revision);

			if (nodeKind == SVNNodeKind.NONE) {
				Logger.logDebug("There is no entry at '" + url + "'.");
				//                System.exit(1);
			} else if (nodeKind == SVNNodeKind.DIR) {
				Logger.logDebug("The entry at '" + url
						+ "' is a directory while a file was expected.");
				//                System.exit(1);
			}
			/*
			 * Gets the contents and properties of the file located at filePath
			 * in the repository at the latest revision (which is meant by a
			 * negative revision number).
			 */
			repository.getFile(filePath, revision, fileProperties, baos);


		} catch (SVNException svne) {
			Logger.logDebug("error while fetching the file contents and properties: " + svne.getMessage());
			//            System.exit(1);
		}

		/*
		 * Here the SVNProperty class is used to get the value of the
		 * svn:mime-type property (if any). SVNProperty is used to facilitate
		 * the work with versioned properties.
		 */
		String mimeType = fileProperties.getStringValue(SVNProperty.MIME_TYPE);

		/*
		 * SVNProperty.isTextMimeType(..) method checks up the value of the mime-type
		 * file property and says if the file is a text (true) or not (false).
		 */
		boolean isTextType = SVNProperty.isTextMimeType(mimeType);

		Iterator iterator = fileProperties.nameSet().iterator();
		//        /*
		//         * Displays file properties.
		//         */
		//        while (iterator.hasNext()) {
		//            String propertyName = (String) iterator.next();
		//            String propertyValue = fileProperties.getStringValue(propertyName);
		//            Logger.logDebug("File property: " + propertyName + "="
		//                    + propertyValue);
		//        }
		/*
		 * Displays the file contents in the console if the file is a text.
		 */
		if (isTextType) {
			//            Logger.logDebug("File contents:");
			//            Logger.logDebug();
			//            try {
			//                baos.writeTo(System.out);
			fileContent = baos.toString();
			//                Logger.logDebug(fileContent);
			//            } catch (IOException ioe) {
			//                ioe.printStackTrace();
			//            }
		} else {
			Logger.logDebug("File contents can not be displayed in the console since the mime-type property says that it's not a kind of a text file.");
		}
		return fileContent;
	}
	/*
	 * Initializes the library to work with a repository via 
	 * different protocols.
	 */
	private static void setupLibrary() {
		/*
		 * For using over http:// and https://
		 */
		DAVRepositoryFactory.setup();
		/*
		 * For using over svn:// and svn+xxx://
		 */
		SVNRepositoryFactoryImpl.setup();

		/*
		 * For using over file:///
		 */
		FSRepositoryFactory.setup();
	}
}