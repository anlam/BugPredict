package change;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Stack;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import utils.Config;
import utils.FileIO;
import change.ChangeEntity.Type;

public class RevisionAnalyzer {
	private ChangeAnalyzer changeAnalyzer;
	private long revision;
	
	private HashSet<CFile> mappedFilesM = new HashSet<CFile>(), mappedFilesN = new HashSet<CFile>();
	private HashSet<CClass> classesM = new HashSet<CClass>(), classesN = new HashSet<CClass>();
	private HashSet<CClass> mappedClassesM = new HashSet<CClass>(), mappedClassesN = new HashSet<CClass>();
	private HashSet<CMethod> methodsM = new HashSet<CMethod>(), methodsN = new HashSet<CMethod>();
	private HashSet<CMethod> mappedMethodsM = new HashSet<CMethod>(), mappedMethodsN = new HashSet<CMethod>();
	private HashSet<CField> fieldsM = new HashSet<CField>(), fieldsN = new HashSet<CField>();
	private HashSet<CField> mappedFieldsM = new HashSet<CField>(), mappedFieldsN = new HashSet<CField>();
	private HashSet<CInitializer> initsM = new HashSet<CInitializer>(), initsN = new HashSet<CInitializer>();
	private HashSet<CInitializer> mappedInitsM = new HashSet<CInitializer>(), mappedInitsN = new HashSet<CInitializer>();
	private HashSet<CEnumConstant> constsM = new HashSet<CEnumConstant>(), constsN = new HashSet<CEnumConstant>();
	private HashSet<CEnumConstant> mappedConstsM = new HashSet<CEnumConstant>(), mappedConstsN = new HashSet<CEnumConstant>();
	
	public RevisionAnalyzer(ChangeAnalyzer changeAnalyzer, long revision) {
		this.changeAnalyzer = changeAnalyzer;
		this.revision = revision;
	}
	
	public ChangeAnalyzer getChangeAnalyzer() {
		return changeAnalyzer;
	}

	public long getRevision() {
		return revision;
	}

	public HashSet<CFile> getMappedFilesM() {
		return mappedFilesM;
	}

	public HashSet<CFile> getMappedFilesN() {
		return mappedFilesN;
	}

	public HashSet<CClass> getMappedClassesM() {
		return mappedClassesM;
	}

	public HashSet<CClass> getMappedClassesN() {
		return mappedClassesN;
	}

	public HashSet<CMethod> getMappedMethodsM() {
		return mappedMethodsM;
	}

	public HashSet<CMethod> getMappedMethodsN() {
		return mappedMethodsN;
	}

	public HashSet<CField> getMappedFieldsM() {
		return mappedFieldsM;
	}

	public HashSet<CField> getMappedFieldsN() {
		return mappedFieldsN;
	}

	public HashSet<CInitializer> getMappedInitsM() {
		return mappedInitsM;
	}

	public HashSet<CInitializer> getMappedInitsN() {
		return mappedInitsN;
	}

	public HashSet<CEnumConstant> getMappedConstsM() {
		return mappedConstsM;
	}

	public HashSet<CEnumConstant> getMappedConstsN() {
		return mappedConstsN;
	}

	public boolean analyze() {
		if (!buildModifiedFiles())
			return false;
		map();
		deriveChanges();
		/*try {
			printChanges(new PrintStream(new FileOutputStream("output/changes.txt")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
		return true;
	}

	private boolean buildModifiedFiles() {
		SVNLogEntry logEntry = this.changeAnalyzer.getLogEntry(revision);
		HashSet<String> changedPaths = new HashSet<String>(logEntry.getChangedPaths().keySet());
		// TODO add copied/moved/replaced paths
		HashSet<String> javaChangedPaths = new HashSet<String>();
		for (String changedPath : changedPaths) {
			if (changedPath.endsWith(".java")) {
				String name = FileIO.getSimpleFileName(changedPath).toLowerCase();
				if (!name.contains("lexer") && !name.contains("parser")) {
					SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths().get(changedPath);
					if (entryPath.getType() == SVNLogEntryPath.TYPE_MODIFIED) {
						javaChangedPaths.add(changedPath);
					}
				}
			}
		}
		if (javaChangedPaths.size() > 50)
			return false;
		for (String changedPath : javaChangedPaths) {
			String contentM = getSourceCode(changedPath, revision-1);
			if (contentM.length() > CFile.MAX_SIZE)
				continue;
			String contentN = getSourceCode(changedPath, revision);
			if (contentN.length() > CFile.MAX_SIZE)
				continue;
			CFile fileM = new CFile(this, changedPath, contentM);
			this.mappedFilesM.add(fileM);
			CFile fileN = new CFile(this, changedPath, contentN);
			this.mappedFilesN.add(fileN);
			fileM.setCType(Type.Modified);
			fileN.setCType(Type.Modified);
			fileM.setMappedFile(fileN);
			fileN.setMappedFile(fileM);
		}
		return true;
	}

	private void map() {
		mapClasses();
		mapMethods();
		//mapFields();
		//mapEnumConstants();
	}
	
	private void mapClasses() {
		// diff classes in modified files
		for (CFile fileM : mappedFilesM)
		{
			CFile fileN = fileM.getMappedFile();
			fileM.computeSimilarity(fileN);
			for (CClass cc : fileM.getClasses())
			{
				if (cc.getMappedClass() != null) {
					mappedClassesM.add(cc);
					mappedClassesN.add(cc.getMappedClass());
					Stack<CClass> stkClasses = new Stack<CClass>();
					stkClasses.push(cc);
					while (!stkClasses.isEmpty()) {
						CClass stkClass = stkClasses.pop();
						stkClass.computeSimilarity(stkClass.getMappedClass(), false);
						for (CClass icc : stkClass.getInnerClasses(false)) {
							if (icc.getMappedClass() != null) {
								mappedClassesM.add(icc);
								mappedClassesN.add(icc.getMappedClass());
								stkClasses.push(icc);
							}
						}
					}
				}
				else
					classesM.add(cc);
			}
			for (CClass cc : fileN.getClasses())
			{
				if (cc.getMappedClass() != null)
					mappedClassesN.add(cc);
				else
					classesN.add(cc);
				for (CClass icc : cc.getInnerClasses(true))
				{
					if (icc.getMappedClass() != null)
						mappedClassesN.add(icc);
					else
						classesN.add(icc);
				}
			}
		}
		
		// map any classes
		CClass.mapAll(classesM, classesN, mappedClassesM, mappedClassesN);
		
		// done diffing classes
		clearClassBodyMapping();
		for (CClass cc : new HashSet<CClass>(mappedClassesM)) {
			cc.computeSimilarity(cc.getMappedClass(), true);
			for (CMethod cm : cc.getMethods()) {
				if (cm.getMappedMethod() != null)
					mappedMethodsM.add(cm);
				else
					methodsM.add(cm);
			}
			for (CField cf : cc.getFields()) {
				if (cf.getMappedField() != null)
					mappedFieldsM.add(cf);
				else
					fieldsM.add(cf);
			}
			for (CInitializer ci : cc.getInitializers()) {
				if (ci.getMappedInitializer() != null)
					mappedInitsM.add(ci);
				else
					initsM.add(ci);
			}
			for (CEnumConstant ce : cc.getEnumConstants()) {
				if (ce.getMappedEnumConstant() != null)
					mappedConstsM.add(ce);
				else
					constsM.add(ce);
			}
		}
		for (CClass cc : new HashSet<CClass>(mappedClassesN)) {
			for (CMethod cm : cc.getMethods())
			{
				if (cm.getMappedMethod() != null)
					mappedMethodsN.add(cm);
				else
					methodsN.add(cm);
			}
			for (CField cf : cc.getFields())
			{
				if (cf.getMappedField() != null)
					mappedFieldsN.add(cf);
				else
					fieldsN.add(cf);
			}
			for (CInitializer ci : cc.getInitializers()) {
				if (ci.getMappedInitializer() != null)
					mappedInitsN.add(ci);
				else
					initsN.add(ci);
			}
			for (CEnumConstant ce : cc.getEnumConstants()) {
				if (ce.getMappedEnumConstant() != null)
					mappedConstsN.add(ce);
				else
					constsN.add(ce);
			}
		}
		for (CClass cc : classesM)
		{
			for (CMethod cm : cc.getMethods()) {
				methodsM.add(cm);
			}
			for (CField cf : cc.getFields()) {
				fieldsM.add(cf);
			}
			for (CInitializer ci : cc.getInitializers()) {
				initsM.add(ci);
			}
			for (CEnumConstant ce : cc.getEnumConstants()) {
				constsM.add(ce);
			}
		}
		for (CClass cc : classesN)
		{
			for (CMethod cm : cc.getMethods()) {
				methodsN.add(cm);
			}
			for (CField cf : cc.getFields()) {
				fieldsN.add(cf);
			}
			for (CInitializer ci : cc.getInitializers()) {
				initsN.add(ci);
			}
			for (CEnumConstant ce : cc.getEnumConstants()) {
				constsN.add(ce);
			}
		}
	}

	private void clearClassBodyMapping() {
		for (CClass cc : this.classesM) {
			cc.clearBodyMapping();
		}
		for (CClass cc : this.classesN) {
			cc.clearBodyMapping();
		}
		for (CClass cc : this.mappedClassesM) {
			cc.clearBodyMapping();
		}
		for (CClass cc : this.mappedClassesN) {
			cc.clearBodyMapping();
		}
	}

	private void mapMethods() {
		CMethod.mapAll(methodsM, methodsN, mappedMethodsM, mappedMethodsN, false);
	}

	private void deriveChanges() {
		//deriveFieldChanges();
		deriveMethodChanges();
		deriveInitChanges();
		//deriveEnumConstantChanges();
		//deriveClassChanges();
	}

	private void deriveMethodChanges() {
		for (CMethod cmM : new HashSet<CMethod>(mappedMethodsM)) {
			cmM.deriveChanges();
			if (cmM.getCType() == Type.Unchanged) {
				mappedMethodsM.remove(cmM);
				mappedMethodsN.remove(cmM.getMappedMethod());
			}
		}
	}

	private void deriveInitChanges() {
		for (CInitializer ciM : new HashSet<CInitializer>(mappedInitsM)) {
			ciM.deriveChanges();
			if (ciM.getCType() == Type.Unchanged) {
				mappedInitsM.remove(ciM);
				mappedInitsN.remove(ciM.getMappedInitializer());
			}
		}
	}

	private void deriveClassChanges() {
		for (CClass cc : classesM) {
			cc.setCType(Type.Deleted);
		}
		for (CClass cc : classesN) {
			cc.setCType(Type.Added);
		}
		for (CClass ccM : new HashSet<CClass>(mappedClassesM)) {
			ccM.deriveChanges();
			if (ccM.getCType() == Type.Unchanged) {
				mappedClassesM.remove(ccM);
				mappedClassesN.remove(ccM.getMappedClass());
			}
		}
	}
	
	public void printChanges(PrintStream ps) {
		ps.println("Revision: " + this.revision);
		ps.println("Old system");
		printChanges(ps, mappedFilesM);
		ps.println("New system");
		printChanges(ps, mappedFilesN);
	}

	private void printChanges(PrintStream ps, HashSet<CFile> files) {
		for (CFile cf : files) {
			cf.printChanges(ps);
		}
	}

	private String getSourceCode(String changedPath, long revision) {
		return this.changeAnalyzer.getSourceCode(changedPath, revision);
	}
}
