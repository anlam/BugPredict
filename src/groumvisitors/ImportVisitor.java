/**
 * 
 */
package groumvisitors;



import java.util.EventObject;
import java.util.List;
import java.util.TreeSet;

import config.GlobalConfig;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.java.Import;
import recoder.java.SourceVisitor;
import recoder.list.generic.ASTList;
import recoder.service.DefaultErrorHandler;
import utils.Logger;

/**
 * @author Anh
 *
 */
public class ImportVisitor extends SourceVisitor{
	private static class SilentErrorHandler extends DefaultErrorHandler {
		SilentErrorHandler(int cnt) {
		}

		@Override
		public void reportError(Exception e) {
			e.printStackTrace();
		}

		@Override
		public void modelUpdated(EventObject event) {
			isUpdating = false;
		}
	}
	public TreeSet<String> importStrSet = new TreeSet<String>();
	CrossReferenceServiceConfiguration sc = new CrossReferenceServiceConfiguration();


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ImportVisitor importVisitor = new ImportVisitor();
		importVisitor.dirParsing(GlobalConfig.processJavaDirPath);
		
		Logger.log("importStrSet size: " + importVisitor.importStrSet.size());
		Logger.log("importStrSet: " + importVisitor.importStrSet);

	}

	public void dirParsing(String path) {
		
		
		try {
			System.getProperties().put("input.path", path);

			sc = new CrossReferenceServiceConfiguration();
			sc.getProjectSettings().ensureSystemClassesAreInPath(); 

			sc.getProjectSettings().setErrorHandler(new SilentErrorHandler(10));
			sc.getChangeHistory().updateModel();
			
//			String[] args = new String[] { path };
//			RecoderProgram.setup(sc, ImportVisitor.class, args);

		
			SourceFileRepository sfr = sc.getSourceFileRepository();
			List<CompilationUnit> cul = sfr.getAllCompilationUnitsFromPath();
			for (CompilationUnit cu:cul){
				if (cu!=null)
					cu.accept(this);
			}
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void visitCompilationUnit(CompilationUnit x) {
		// TODO Auto-generated method stub
		ASTList<Import> imports = x.getImports();
		for (Import importAST:imports){
			
//			Logger.log(importAST.getLastElement().toSource());
			importStrSet.add(importAST.getLastElement().toSource());
		}
		
	}

	



}
