/**
 * 
 */
package repository;


import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import config.GlobalConfig;

/**
 * @author Anh
 *
 */
public class SVNUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/*
	 * Initializes the library to work with a repository via 
	 * different protocols.
	 */
	public static void setupLibrary() {
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

	public static SVNRepository doAuthenticate(String url, String name, String password){
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
			 * Perhaps a malformed URL is the cause of this exception
			 */
			System.err
			.println("error while creating an SVNRepository for the location '"
					+ url + "': " + svne.getMessage());
			System.exit(1);
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
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name, password);
		repository.setAuthenticationManager(authManager);
		
		return repository;
	}

	
	public static String getSourcePathOfLastSnapshot(SVNRepository repository, String url ) {
		long latestRevision = 0l;
		try {
			//this.latestRevision = this.repository.getLatestRevision();
			SVNDirEntry entry = repository.info("", -1);
			latestRevision = entry.getRevision();
		} catch (SVNException e) {
            System.err.println("Error while getting lastest revision number for '"
                    + url + "': " + e.getMessage());
			return null;
		}
//		if (this.latestRevision < 100) {
//			System.out.println("Too few revisions!");
//			return null;
//		}
		Collection<?> logEntries = null;
        try {
            logEntries = repository.log(new String[] {""}, null, latestRevision<GlobalConfig.SVNWindowSize?0:latestRevision - GlobalConfig.SVNWindowSize, -1, true, true);
        } catch (SVNException svne) {
            System.err.println("Error while collecting log information for '"
                    + url + "': " + svne.getMessage());
            return null;
        }
        HashMap<String, String> checkedSrcPath = new HashMap<String, String>();
        HashMap<String, Integer> changedDirPathCounts = new HashMap<String, Integer>();
        for (Iterator<?> entries = logEntries.iterator(); entries.hasNext();) {
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
        	if (logEntry.getChangedPaths().size() > 0) {
        		HashMap<String, String> changedDirPaths = new HashMap<String, String>();
                Set<?> changedPathsSet = logEntry.getChangedPaths().keySet();
                for (Iterator<?> changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
                    SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths().get(changedPaths.next());
                    String path = entryPath.getPath();
                    if(entryPath.getType() != SVNLogEntryPath.TYPE_DELETED && isSourceFile(path))
                    {
                    	changedDirPaths.put(path.substring(0, path.lastIndexOf('/')), path);
                    }
                }
                HashSet<String> changedSrcDirs = new HashSet<String>();
                for (String dirPath : changedDirPaths.keySet()) {
                	String srcDir = checkedSrcPath.get(dirPath);
                	if (srcDir == null) {
                		srcDir = getSrcPath(repository, changedDirPaths.get(dirPath), logEntry.getRevision());
                		checkedSrcPath.put(dirPath, srcDir);
                	}
                	changedSrcDirs.add(srcDir);
                }
                for (String dir : changedSrcDirs) {
                	int count = 1;
                	if (changedDirPathCounts.containsKey(dir))
                		count = changedDirPathCounts.get(dir) + 1;
                	changedDirPathCounts.put(dir, count);
                }
            }
        }
        if (changedDirPathCounts.isEmpty())
        	return null;
        int max = 0;
        String srcDir = "";
        for (String dir : changedDirPathCounts.keySet()) {
        	int count = changedDirPathCounts.get(dir);
        	if (count > max) {
        		max = count;
        		srcDir = dir;
        	}
        }
        return srcDir;
	}
	
	private static boolean isSourceFile(String path){
		for (String ext:GlobalConfig.sourceFileJavaExt){
			if (path.endsWith(ext))
				return true;
		}
		return false;
	}
	
	/**
	 * getSystemRootPath using package name
	 * @param path
	 * @param revision 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	private static String getSrcPath(SVNRepository repository, String path, long revision) {
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
    	String parent = path.substring(0, path.lastIndexOf("/"));
    	int index = 0;
    	try{
			String source = getFile(repository, path, revision);
	    	parser.setSource(source.toCharArray());
	    	parser.setCompilerOptions(options);
	    	ASTNode ast = parser.createAST(null);
	    	org.eclipse.jdt.core.dom.CompilationUnit cu = (org.eclipse.jdt.core.dom.CompilationUnit) ast;
			org.eclipse.jdt.core.dom.PackageDeclaration pd = cu.getPackage();
	    	if (pd == null) {
	    		return parent;
	    	}
	    	index = parent.lastIndexOf("/" + pd.getName().getFullyQualifiedName().replace('.', '/'));
	    	if (index == -1)
	    		return parent;
	    	if (index == 0)
	    		return "";
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    	return parent.substring(0, index);
	}
	
//	private static String getSrcPath(SVNRepository repository, String path, long revision) {
//    	String parent = path.substring(0, path.lastIndexOf("/"));
//    	int index = 0;
//    	try{
//			String source = getFile(repository, path, revision);
//			InputStream in = IOUtils.toInputStream(source);
//			CompilationUnit cu = JavaParser.parse(in);
//	    	PackageDeclaration pd = cu.getPackage();
//	    	if (pd == null) {
//	    		return parent;
//	    	}
//	    	index = parent.lastIndexOf("/" + pd.getName().getName().replace('.', '/'));
//	    	if (index == -1)
//	    		return parent;
//	    	if (index == 0)
//	    		return "";
//    	}
//    	catch(Exception e){
//    		e.printStackTrace();
//    		return null;
//    	}
//    	return parent.substring(0, index);
//	}
	
	private static String getFile(SVNRepository repository, String path, long revision)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			repository.getFile(path, revision, null, out);
		}
		catch (SVNException e) {
			e.printStackTrace();
		}
		
		return out.toString();
	}
	
}
