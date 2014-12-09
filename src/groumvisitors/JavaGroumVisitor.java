/**
 * 
 */
package groumvisitors;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Stack;

import net.sf.retrotranslator.runtime.impl.TypeArgument;
import config.GlobalConfig;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.abstraction.Type;
import recoder.convenience.Naming;
import recoder.io.DefaultClassFileRepository;
import recoder.io.DefaultSourceFileRepository;
import recoder.io.PropertyNames;
import recoder.java.Comment;
import recoder.java.CompilationUnit;
import recoder.java.DocComment;
import recoder.java.Expression;
import recoder.java.Identifier;
import recoder.java.Import;
import recoder.java.JavaProgramElement;
import recoder.java.NonTerminalProgramElement;
import recoder.java.PackageSpecification;
import recoder.java.PrettyPrintingException;
import recoder.java.ProgramElement;
import recoder.java.SingleLineComment;
import recoder.java.SourceElement;
import recoder.java.SourceElement.Position;
import recoder.java.SourceVisitor;
import recoder.java.StatementBlock;
import recoder.java.declaration.AnnotationDeclaration;
import recoder.java.declaration.AnnotationElementValuePair;
import recoder.java.declaration.AnnotationPropertyDeclaration;
import recoder.java.declaration.AnnotationUseSpecification;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ClassInitializer;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.DeclarationSpecifier;
import recoder.java.declaration.EnumConstantDeclaration;
import recoder.java.declaration.EnumConstantSpecification;
import recoder.java.declaration.EnumDeclaration;
import recoder.java.declaration.Extends;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.Implements;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.LocalVariableDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.Modifier;
import recoder.java.declaration.ParameterDeclaration;
import recoder.java.declaration.Throws;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.declaration.VariableDeclaration;
import recoder.java.declaration.VariableSpecification;
import recoder.java.declaration.modifier.Abstract;
import recoder.java.declaration.modifier.Final;
import recoder.java.declaration.modifier.Native;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.java.declaration.modifier.Static;
import recoder.java.declaration.modifier.StrictFp;
import recoder.java.declaration.modifier.Synchronized;
import recoder.java.declaration.modifier.Transient;
import recoder.java.declaration.modifier.Volatile;
import recoder.java.expression.ArrayInitializer;
import recoder.java.expression.Assignment;
import recoder.java.expression.ElementValueArrayInitializer;
import recoder.java.expression.ExpressionStatement;
import recoder.java.expression.Literal;
import recoder.java.expression.Operator;
import recoder.java.expression.ParenthesizedExpression;
import recoder.java.expression.literal.BooleanLiteral;
import recoder.java.expression.literal.CharLiteral;
import recoder.java.expression.literal.DoubleLiteral;
import recoder.java.expression.literal.FloatLiteral;
import recoder.java.expression.literal.IntLiteral;
import recoder.java.expression.literal.LongLiteral;
import recoder.java.expression.literal.NullLiteral;
import recoder.java.expression.literal.StringLiteral;
import recoder.java.expression.operator.BinaryAnd;
import recoder.java.expression.operator.BinaryAndAssignment;
import recoder.java.expression.operator.BinaryNot;
import recoder.java.expression.operator.BinaryOr;
import recoder.java.expression.operator.BinaryOrAssignment;
import recoder.java.expression.operator.BinaryXOr;
import recoder.java.expression.operator.BinaryXOrAssignment;
import recoder.java.expression.operator.Conditional;
import recoder.java.expression.operator.CopyAssignment;
import recoder.java.expression.operator.Divide;
import recoder.java.expression.operator.DivideAssignment;
import recoder.java.expression.operator.Equals;
import recoder.java.expression.operator.GreaterOrEquals;
import recoder.java.expression.operator.GreaterThan;
import recoder.java.expression.operator.Instanceof;
import recoder.java.expression.operator.LessOrEquals;
import recoder.java.expression.operator.LessThan;
import recoder.java.expression.operator.LogicalAnd;
import recoder.java.expression.operator.LogicalNot;
import recoder.java.expression.operator.LogicalOr;
import recoder.java.expression.operator.Minus;
import recoder.java.expression.operator.MinusAssignment;
import recoder.java.expression.operator.Modulo;
import recoder.java.expression.operator.ModuloAssignment;
import recoder.java.expression.operator.Negative;
import recoder.java.expression.operator.New;
import recoder.java.expression.operator.NewArray;
import recoder.java.expression.operator.NotEquals;
import recoder.java.expression.operator.Plus;
import recoder.java.expression.operator.PlusAssignment;
import recoder.java.expression.operator.Positive;
import recoder.java.expression.operator.PostDecrement;
import recoder.java.expression.operator.PostIncrement;
import recoder.java.expression.operator.PreDecrement;
import recoder.java.expression.operator.PreIncrement;
import recoder.java.expression.operator.ShiftLeft;
import recoder.java.expression.operator.ShiftLeftAssignment;
import recoder.java.expression.operator.ShiftRight;
import recoder.java.expression.operator.ShiftRightAssignment;
import recoder.java.expression.operator.Times;
import recoder.java.expression.operator.TimesAssignment;
import recoder.java.expression.operator.TypeCast;
import recoder.java.expression.operator.UnsignedShiftRight;
import recoder.java.expression.operator.UnsignedShiftRightAssignment;
import recoder.java.reference.AnnotationPropertyReference;
import recoder.java.reference.ArrayReference;
import recoder.java.reference.EnumConstructorReference;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MetaClassReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.PackageReference;
import recoder.java.reference.SuperConstructorReference;
import recoder.java.reference.SuperReference;
import recoder.java.reference.ThisConstructorReference;
import recoder.java.reference.ThisReference;
import recoder.java.reference.TypeReference;
import recoder.java.reference.UncollatedReferenceQualifier;
import recoder.java.reference.VariableReference;
import recoder.java.statement.Assert;
import recoder.java.statement.Branch;
import recoder.java.statement.Break;
import recoder.java.statement.Case;
import recoder.java.statement.Catch;
import recoder.java.statement.Continue;
import recoder.java.statement.Default;
import recoder.java.statement.Do;
import recoder.java.statement.Else;
import recoder.java.statement.EmptyStatement;
import recoder.java.statement.EnhancedFor;
import recoder.java.statement.Finally;
import recoder.java.statement.For;
import recoder.java.statement.If;
import recoder.java.statement.JavaStatement;
import recoder.java.statement.LabeledStatement;
import recoder.java.statement.LoopStatement;
import recoder.java.statement.Return;
import recoder.java.statement.Switch;
import recoder.java.statement.SynchronizedBlock;
import recoder.java.statement.Then;
import recoder.java.statement.Throw;
import recoder.java.statement.Try;
import recoder.java.statement.While;
import recoder.service.CrossReferenceSourceInfo;
import recoder.service.DefaultCrossReferenceSourceInfo;
import recoder.service.DefaultErrorHandler;
import recoder.service.DefaultNameInfo;
import recoder.util.StringUtils;
import utils.Logger;
import application.RecoderProgram;



import data.ControlInfo;
import data.FileInfo;
import data.LexemeInfo;
import data.MethodInfo;
import data.MethodInvocInfo;
import data.NodeInfo;
import data.StatementLexeme;
import data.StatementLexemePart;
import data.TypeInfo;
import dirtool.DirProcessing;

/**
 * @author Anh
 * 
 */

//method visitMethodDeclaration has the code to get the groum of the code (MethodInfo class)
public class JavaGroumVisitor extends SourceVisitor implements
PropertyNames {

	private final static class SilentErrorHandler extends DefaultErrorHandler {
		SilentErrorHandler(int cnt) {
		}

		@Override
		public void reportError(Exception e) {
//			e.printStackTrace();
		}

		@Override
		public void modelUpdated(EventObject event) {
			isUpdating = true;
		}
	}

	private CompilationUnit cu;
	private CrossReferenceServiceConfiguration sc;
	private CrossReferenceSourceInfo sourceInfo;

	FileInfo fileInfo = null;
	private TypeInfo curTypeInfo = null;
	private MethodInfo curMethodInfo = null; 

	private Stack<TypeInfo> storeCurTypeInfo = new Stack<TypeInfo>();
	private Stack<MethodInfo> storeCurMethodInfo = new Stack<MethodInfo>();

	List<LexemeInfo> curMethodTokenList = new ArrayList<LexemeInfo>();


	List<StatementLexeme> statementLexemeList = new ArrayList<StatementLexeme>();
	Stack<StatementLexeme> statementLexemeStack = new Stack<StatementLexeme>();

	Stack<StatementLexemePart> statementLexemePartStack = new Stack<StatementLexemePart>();

	List<StatementLexeme> methodStatementLexList = new ArrayList<StatementLexeme>();
	LinkedHashMap<String, String> varTypeMap = new LinkedHashMap<String, String>();
	ArrayList<Integer> methodGroumSizes = new ArrayList<Integer>();

	public List<String> identifierList = new ArrayList<String>(); //varname, method name, class name
	public List<String> commentList = new ArrayList<String>();
	public List<String> stringLiteralList = new ArrayList<String>();
	
	public List<String> API = new ArrayList<String>(); //TypeRef, PackageRef and MethodCall
	public List<String> typeReferenceList = new ArrayList<String>();
	public int countMethod = 0;

	int isAddToMethodLexList = 0;
	File file;

	// int startTokenIdx = -1;
	int endTokenIdx = -1;

	int isInAMethod = 0;

	long curID = 0L;

	private NodeInfo curParentNode = null;
	private Stack< NodeInfo> parentNodeStack = new Stack<NodeInfo>();
	private NodeInfo curNode = null;
	private Stack<NodeInfo> previousControlFlowNodeStack = new Stack<NodeInfo>();
	private Stack<NodeInfo> previousDataNodeStack = new Stack<NodeInfo>();
	private LinkedHashMap<String, NodeInfo> previousDataNodeMap = new LinkedHashMap<String, NodeInfo>();

	private Stack<NodeInfo> storeCurParentNode = new Stack<NodeInfo>();
	private Stack<Stack< NodeInfo>> storeParentNodeStack = new Stack<Stack<NodeInfo>>();
	private Stack<NodeInfo> storeCurNode = new Stack<NodeInfo>();
	private Stack<Stack<NodeInfo>> storePreviousControlFlowNodeStack = new Stack<Stack<NodeInfo>>();
	private Stack<Stack<NodeInfo>> storePreviousDataNodeStack = new Stack<Stack<NodeInfo>>();
	private Stack<LinkedHashMap<String, NodeInfo>> storePreviousDataNodeMap = new Stack<LinkedHashMap<String, NodeInfo>>();

	private ArrayList<Long> curLocalScopeList = new ArrayList<Long>();
	private ArrayList<Long> curClassScopeList = new ArrayList<Long>();

	static int count =0;
	
	public ArrayList<TypeInfo> typeList = new ArrayList<TypeInfo>();
	
	public ArrayList<TypeInfo> allTypeList = new ArrayList<TypeInfo>();
	public 	static boolean isProcessOneFile = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.getProperties().put("input.path",
				GlobalConfig.dummyDir);


		String javaDirPath = GlobalConfig.dummyDir;

		JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();

		//Parse all files of the project's dir to get info about Type/method
		javaGroumVisitor.dirParsing(javaDirPath);

		String[] javaSourceFileExt = new String[] { ".java" };


		if (isProcessOneFile) 
		{
			String filePath =GlobalConfig.processJavaDirPath + "/db4oj/core/src/com/db4o/DTrace.java";
			File file = new File(filePath);

			try {
				Logger.initDebug("DebugJavaGroum.txt");

				//To parse one file 
				javaGroumVisitor.getJavaCU(file.getAbsolutePath(), file);
				Logger.closeDebug();
				Logger.log(javaGroumVisitor.typeList.size());
			} catch (Exception e) {
				e.printStackTrace();
				
			}

		} else {
			try {
				List<File> javaFiles = DirProcessing.getFilteredRecursiveFiles(
						new File(javaDirPath), javaSourceFileExt);
				int totalMethods =0; 
				Logger.initDebug("DebugJavaGroum.txt");
//				FileWriter sentenceWriter = new FileWriter(Configurations.javaSentencePath);

				for (File file : javaFiles) {
					String filePath = file.getAbsolutePath();
//					Logger.log("fileName: "
//							+ file.getAbsolutePath().replace('\\', '/'));
					javaGroumVisitor.getJavaCU(filePath, file);
					totalMethods+= javaGroumVisitor.countMethod;
//					Logger.log(javaGroumVisitor.typeList.size());
					javaGroumVisitor.allTypeList.addAll(javaGroumVisitor.typeList);

				}
//				sentenceWriter.close();

				Logger.closeDebug();
				Logger.log("total Methods: " + totalMethods);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//		Logger.closeDebug();

	}

	public synchronized void doMain(String path){
		System.getProperties().put("input.path",path);

		String javaDirPath = path;
//		JavaGroumVisitor javaGroumVisitor = new JavaGroumVisitor();

		//Parse all files of the project's dir to get info about Type/method
//		javaGroumVisitor.dirParsing(javaDirPath);
		dirParsing(javaDirPath);

		try {
			List<File> javaFiles = DirProcessing.getFilteredRecursiveFiles(
					new File(javaDirPath), GlobalConfig.sourceFileJavaExt);
//			int totalMethods =0; 
//			Logger.initDebug("DebugJavaGroum.txt");

			for (File file : javaFiles) {
				String filePath = file.getAbsolutePath();
//				Logger.log("fileName: " + file.getAbsolutePath().replace('\\', '/'));
				getJavaCU(filePath, file);
//				totalMethods+= countMethod;
//				Logger.log(typeList.size());
				allTypeList.addAll(typeList);

			}

//			Logger.closeDebug();
//			Logger.log("total Methods: " + totalMethods);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public synchronized LinkedHashMap<String, ArrayList<TypeInfo>> doMainParseFileList(String hostDir,
			LinkedHashSet<String>  tobeParsedPaths){ 
		
		LinkedHashMap<String, ArrayList<TypeInfo>> fileTypeListMap = new LinkedHashMap<>();
		LinkedHashMap<File, String> file2FilePathMap = new LinkedHashMap<>();

		try {
			System.getProperties().put("input.path",hostDir);

			dirParsing(hostDir);
			
			List<File> javaFiles = DirProcessing.getFilteredRecursiveFiles(
					new File(hostDir), GlobalConfig.sourceFileJavaExt);
			ArrayList<File> tobeParsedFiles = new  ArrayList<>();
			for (File file:javaFiles){
				String filePath = file.getAbsolutePath();
				String normFilePath = filePath.replace("\\", "/");
				normFilePath = normFilePath.substring(hostDir.length()-1);
//				Logger.log("nFilePath: " + normFilePath);
				for (String tobeParsed:tobeParsedPaths){
					if (normFilePath.equals(tobeParsed)){
						tobeParsedFiles.add(file);
						file2FilePathMap.put(file, tobeParsed);
						break;
					}
				}
			}
			
			for (File file : tobeParsedFiles) {
				String filePath = file.getAbsolutePath();
				getJavaCU(filePath, file);
//				allTypeList.addAll(typeList);
				ArrayList<TypeInfo> tmpTypeList = new ArrayList<>();
				tmpTypeList.addAll(typeList);
				fileTypeListMap.put(file2FilePathMap.get(file), tmpTypeList);
				
				typeList.clear();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileTypeListMap;
	}


	public void getJavaCU(String javaFilePath, File javaFile) {
		this.file = javaFile;

		this.fileInfo = new FileInfo();
		this.fileInfo.fileName = javaFile.getName();
		this.fileInfo.filePath = javaFile.getAbsolutePath();

		try {
			out = new FileWriter("text.txt");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		curMethodTokenList.clear();
		statementLexemeList.clear();
		statementLexemeStack.clear();
		statementLexemePartStack.clear();
		methodStatementLexList.clear();
		countMethod = 0;
		isAddToMethodLexList = 0;
		endTokenIdx = -1;
		isInAMethod = 0;
		curID = 0;
		curParentNode = null;
		parentNodeStack.clear();
		curNode = null;
		previousControlFlowNodeStack.clear();
		typeList.clear();
		identifierList.clear();
		commentList.clear();
		stringLiteralList.clear();
		typeReferenceList.clear();
		API.clear();

		try {
			cu = sc.getSourceFileRepository().getCompilationUnitFromFile(
					javaFilePath);
			
			if(cu.getComments() != null)
			for(Comment comment : cu.getComments())
			{
				//System.out.print("here");
				//System.out.print(comment.getText());
				commentList.add(comment.getText());
			}
		
		} catch (ParserException e) {
			e.printStackTrace();
		}

		try {
//			Logger.log(cu.toSource());
			if (cu!=null)
				cu.accept(this);
			
		} catch (Exception e) {
			e.printStackTrace();
			//			System.exit(1);
		}

	}

	public void dirParsing(String path) {
		System.getProperties().put("input.path", path);

		String[] args = new String[] { path, "-Q" };
		sc = new CrossReferenceServiceConfiguration();
		sc.getProjectSettings().setErrorHandler(new SilentErrorHandler(10));


		//sc.getChangeHistory().updateModel();
		RecoderProgram.setup(sc, JavaGroumVisitor.class, args);


//		System.out.println("Importing Initial Project Files...");
//		
//		System.out.println("\nSystem Settings...");
//		//Properties props = sc.getProjectSettings().getProperties();
//		//props.setProperty("jdk1.7", "true");
//
//				System.out.println("\nFiles...");
//				System.out.println(((DefaultSourceFileRepository) sc
//						.getSourceFileRepository()).information());
//				System.out.println(((DefaultClassFileRepository) sc
//						.getClassFileRepository()).information());
//
//				System.out.println("\nNames...");
//				System.out.println(((DefaultNameInfo) sc.getNameInfo()).information());
//				System.out.println("\nReferences...");
//				System.out.println(((DefaultCrossReferenceSourceInfo) sc
//						.getCrossReferenceSourceInfo()).information());
//		System.out.println();

		sourceInfo = sc.getCrossReferenceSourceInfo();
		
		

	}

	public String getMethodContent(MethodDeclaration x) {
		String content = x.toSource();
		return content;
	}

	public void storeFields(boolean isForType){

		Stack<NodeInfo> tmpParentNodeStack = new Stack<NodeInfo>();
		Stack<NodeInfo> tmpPreviousControlFlowNodeStack = new Stack<NodeInfo>();
		Stack<NodeInfo> tmpPreviousDataNodeStack = new Stack<NodeInfo>();
		LinkedHashMap<String, NodeInfo> tmpPreviousDataNodeMap = new LinkedHashMap<String, NodeInfo>();

		tmpParentNodeStack.addAll(parentNodeStack);
		tmpPreviousControlFlowNodeStack.addAll(previousControlFlowNodeStack);
		tmpPreviousDataNodeStack.addAll(tmpPreviousDataNodeStack);
		tmpPreviousDataNodeMap.putAll(previousDataNodeMap);

		if (isForType)
			storeCurTypeInfo.push(curTypeInfo);

		storeCurMethodInfo.push(curMethodInfo);

		storeCurParentNode.push(curParentNode);
		storeParentNodeStack.push(tmpParentNodeStack);

		storeCurNode.push(curNode);

		storePreviousControlFlowNodeStack.push(tmpPreviousControlFlowNodeStack);
		storePreviousDataNodeStack.push(tmpPreviousDataNodeStack);
		storePreviousDataNodeMap.push(tmpPreviousDataNodeMap);

		if (isForType)
			curTypeInfo = null;
		curMethodInfo = null;
		curParentNode = new NodeInfo();
		parentNodeStack = new Stack<NodeInfo>();
		curNode = new NodeInfo();
		previousControlFlowNodeStack = new Stack<NodeInfo>();
		previousDataNodeStack = new Stack<NodeInfo>();
		previousDataNodeMap = new LinkedHashMap<String, NodeInfo>();
	}

	public void restoreFields(boolean isForType){
		if (isForType)
			curTypeInfo = storeCurTypeInfo.pop();
		curMethodInfo = storeCurMethodInfo.pop();
		curParentNode = storeCurParentNode.pop();
		parentNodeStack = storeParentNodeStack.pop();
		curNode = storeCurNode.pop();
		previousControlFlowNodeStack = storePreviousControlFlowNodeStack.pop();
		previousDataNodeStack = storePreviousDataNodeStack.pop();
		previousDataNodeMap = storePreviousDataNodeMap.pop();
	}

	/**
	 * A snapshot of the system properties at creation time of this instance.
	 */
	private Properties properties;

	/**
	 * The destination writer stream.
	 */
	private Writer out = null;

	/**
	 * Line number.
	 */
	private int line = 1;

	/**
	 * Column number. Used to keep track of indentations.
	 */
	private int column = 1;

	/**
	 * Level.
	 */
	private int level = 0;

	/**
	 * Worklist of single line comments that must be delayed till the next
	 * linefeed.
	 */
	private List<SingleLineComment> singleLineCommentWorkList = new ArrayList<SingleLineComment>();

	/**
	 * Flag to indicate that a single/multi line comment has just been printed
	 * and a line feed is mandatory. Needed if no relative start positions of
	 * PEs present: After a single line comment, there must always be a line
	 * feed; for multi line comments, we use a heuristic: print a line feed
	 * after a multi line comment that spans more than one line.
	 * 
	 */
	private boolean hasJustPrintedComment = false;

	/**
	 * Set a new stream to write to. Useful to redirect the output while
	 * retaining all other settings. Resets the current source positions and
	 * comments.
	 */
	public void setWriter(Writer out) {
		if (out == null) {
			throw new IllegalArgumentException("Impossible to write to null");
		}
		this.out = out;
		column = 1;
		line = 1;
		singleLineCommentWorkList.clear();
	}

	/**
	 * Gets the currently used writer. Be careful when using.
	 * 
	 * @return the currently used writer.
	 */
	public Writer getWriter() {
		return out;
	}

	/**
	 * Get current line number.
	 * 
	 * @return the line number, starting with 0.
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Get current column number.
	 * 
	 * @return the column number, starting with 0.
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * Get indentation level.
	 * 
	 * @return the int value.
	 */
	public int getIndentationLevel() {
		return level;
	}

	/**
	 * Set indentation level.
	 * 
	 * @param level
	 *            an int value.
	 */
	public void setIndentationLevel(int level) {
		this.level = level;
	}

	/**
	 * Get total indentation.
	 * 
	 * @return the int value.
	 */
	public int getTotalIndentation() {
		return indentation * level;
	}

	/**
	 * Change level.
	 * 
	 * @param delta
	 *            an int value.
	 */
	public void changeLevel(int delta) {
		level += delta;
	}

	private static char[] BLANKS = new char[128];

	private static char[] FEEDS = new char[8];

	static {
		for (int i = 0; i < FEEDS.length; i++) {
			FEEDS[i] = '\n';
		}
		for (int i = 0; i < BLANKS.length; i++) {
			BLANKS[i] = ' ';
		}
	}

	/**
	 * Replace all unicode characters above ? by their explicit representation.
	 * 
	 * @param str
	 *            the input string.
	 * @return the encoded string.
	 */
	protected static String encodeUnicodeChars(String str) {
		int len = str.length();
		StringBuilder buf = new StringBuilder(len + 4);
		for (int i = 0; i < len; i += 1) {
			char c = str.charAt(i);
			// TODO Is that all ?
			// should be checked by someone who knows a lot about unicode
			// characters...
			if (c >= 0x0100 || c <= 0x001F || (c >= 0x007F && c <= 0x009F)) {
				buf.append("\\u");
				if (c < 0x1000)
					buf.append("0");
				if (c < 0x100)
					buf.append("0");
				if (c < 0x10)
					buf.append("0");
				buf.append(Integer.toString(c, 16));
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	/**
	 * Convenience method to write indentation chars.
	 */
	protected void printIndentation(int lf, int blanks) {
		//		if (lf > 0) {
		//			do {
		//				int n = Math.min(lf, FEEDS.length);
		//				print(FEEDS, 0, n);
		//				lf -= n;
		//			} while (lf > 0);
		//		}
		//		while (blanks > 0) {
		//			int n = Math.min(blanks, BLANKS.length);
		//			print(BLANKS, 0, n);
		//			blanks -= n;
		//		}
	}

	/**
	 * Shared and reused position object.
	 */
	private Position overwritePosition = new Position(0, 0);

	/**
	 * Sets the indentation of the specified element to at least the specified
	 * minimum.
	 * 
	 * @return the final relative position of the element.
	 */
	protected Position setElementIndentation(int minlf, int minblanks,
			SourceElement element) {
		Position indent = element.getRelativePosition();
		if (hasJustPrintedComment) {
			minlf = Math.max(1, minlf);
			hasJustPrintedComment = false;
		}
		if (indent == Position.UNDEFINED) {
			if (minlf > 0) {
				minblanks += getTotalIndentation();
			}
			indent = new Position(minlf, minblanks);
		} else if (overwriteIndentation) {
			if (minlf > 0) {
				minblanks += getTotalIndentation();
			}
			indent.setPosition(minlf, minblanks);
		} else {
			if (minlf > 0 && indent.getColumn() == 0 && indent.getLine() == 0) {
				indent.setLine(1);
			}
			if (indent.getLine() > 0 && !(element instanceof Comment)) {
				// do not change comment indentation!
				minblanks += getTotalIndentation();
			}
			if (minblanks > indent.getColumn()) {
				indent.setColumn(minblanks);
			}
		}
		element.setRelativePosition(indent);
		return indent;
	}

	/**
	 * Sets the indentation of the specified element to at least the specified
	 * minimum and writes it.
	 */
	protected void printElementIndentation(int minlf, int minblanks,
			SourceElement element) {
		//		Position indent = setElementIndentation(minlf, minblanks, element);
		//		printIndentation(indent.getLine(), indent.getColumn());
		//		if (overwriteParsePositions) {
		//			indent.setPosition(line, column);
		//			element.setStartPosition(indent);
		//		}
	}

	protected void printElementIndentation(int minblanks, SourceElement element) {
		//		printElementIndentation(0, minblanks, element);
	}

	protected void printElementIndentation(SourceElement element) {
		//		printElementIndentation(0, 0, element);
	}

	/**
	 * Adds indentation for a program element if necessary and if required, but
	 * does not print the indentation itself.
	 */
	protected void printElement(int lf, int levelChange, int blanks,
			SourceElement elem) {
		level += levelChange;
		//		setElementIndentation(lf, blanks, findFirstElementInclComment(elem));
		elem.accept(this);
	}

	/**
	 * Write a source element.
	 * 
	 * @param lf
	 *            an int value.
	 * @param blanks
	 *            an int value.
	 * @param elem
	 *            a source element.
	 */
	protected void printElement(int lf, int blanks, SourceElement elem) {
		//		setElementIndentation(lf, blanks, findFirstElementInclComment(elem));
		elem.accept(this);
	}

	/**
	 * Write source element.
	 * 
	 * @param blanks
	 *            an int value.
	 * @param elem
	 *            a source element.
	 */
	protected void printElement(int blanks, SourceElement elem) {
		//		setElementIndentation(0, blanks, findFirstElementInclComment(elem));
		elem.accept(this);
	}

	/**
	 * Write source element.
	 * 
	 * @param elem
	 *            a source element.
	 */
	protected void printElement(SourceElement elem) {
		//		setElementIndentation(0, 0, findFirstElementInclComment(elem));
		elem.accept(this);
	}

	/**
	 * Write a complete ProgramElementList.
	 */
	protected void printProgramElementList(int firstLF, int levelChange,
			int firstBlanks, String separationSymbol, int separationLF,
			int separationBlanks, List<? extends ProgramElement> list) {
		int s = list.size();
		if (s == 0) {
			level += levelChange;
			return;
		}
		printElement(firstLF, levelChange, firstBlanks, list.get(0));
		for (int i = 1; i < s; i += 1) {
			//			print(separationSymbol);
			printElement(separationLF, separationBlanks, list.get(i));
		}
	}

	/**
	 * Write a complete ProgramElementList using "Keyword" style.
	 * 
	 * @param list
	 *            a program element list.
	 */
	protected void printKeywordList(List<? extends ProgramElement> list) {
		printProgramElementList(0, 0, 0, "", 0, 1, list);
	}

	protected void printCommaList(int firstLF, int levelChange,
			int firstBlanks, List<? extends ProgramElement> list) {
		printProgramElementList(firstLF, levelChange, firstBlanks, ",", 0, 1,
				list);
	}

	/**
	 * Write comma list.
	 * 
	 * @param list
	 *            a program element list.
	 */
	protected void printCommaList(int separationBlanks,
			List<? extends ProgramElement> list) {
		printProgramElementList(0, 0, 0, ",", 0, separationBlanks, list);
	}

	/**
	 * Write comma list.
	 * 
	 * @param list
	 *            a program element list.
	 */
	protected void printCommaList(List<? extends ProgramElement> list) {
		printProgramElementList(0, 0, 0, ",", 0, 1, list);
	}

	/**
	 * Write a complete ProgramElementList using "Line" style.
	 */
	protected void printLineList(int firstLF, int levelChange,
			List<? extends ProgramElement> list) {
		printProgramElementList(firstLF, levelChange, 0, "", 1, 0, list);
	}

	/**
	 * Write a complete ProgramElementList using "Block" style.
	 */
	protected void printBlockList(int firstLF, int levelChange,
			List<? extends ProgramElement> list) {
		printProgramElementList(firstLF, levelChange, 0, "", 2, 0, list);
	}

	//	private void dumpComments() {
	//		int size = singleLineCommentWorkList.size();
	//		if (size > 0) {
	//			for (int i = 0; i < size; i++) {
	//				singleLineCommentWorkList.get(i).accept(this);
	//			}
	//			singleLineCommentWorkList.clear();
	//		}
	//	}

	/**
	 * Write a single character.
	 * 
	 * @param c
	 *            an int value.
	 * @exception PrettyPrintingException
	 *                wrapping an IOException.
	 */
	protected void print(int c) {
		//		if (c == '\n') {
		//			if (!isPrintingSingleLineComments) {
		//				dumpComments();
		//			}
		//			column = 1;
		//			line += 1;
		//		} else {
		//			column += 1;
		//		}
		//		try {
		//			if (String.valueOf(Character.toChars(c)).trim().length() > 0) {
		//				LexemeInfo token = new LexemeInfo(String.valueOf(Character
		//						.toChars(c)));
		//				curMethodTokenList.add(token);
		//			}
		//			out.write(c);
		//		} catch (Exception ioe) {
		//			// throw new PrettyPrintingException(ioe);
		//			// ioe.printStackTrace();
		//		}
	}

	/**
	 * Write a sequence of characters.
	 * 
	 * @param cbuf
	 *            an array of char.
	 * @param off
	 *            an int value.
	 * @param len
	 *            an int value.
	 */
	protected void print(char[] cbuf, int off, int len) {
		//		boolean col = false;
		//
		//		for (int i = off + len - 1; i >= off; i -= 1) {
		//			if (cbuf[i] == '\n') {
		//				if (!isPrintingSingleLineComments) {
		//					dumpComments();
		//				}
		//				line += 1;
		//				if (!col) {
		//					column = (off + len - 1 - i) + 1;
		//					col = true;
		//				}
		//			}
		//		}
		//		if (!col) {
		//			column += len;
		//			// int i;
		//			// for (i = off + len - 1; (i >= off && cbuf[i] != '\n'); i -= 1) ;
		//			// column = (i >= off) ? (off + len - 1 - i) : (column + len);
		//		}
		//		try {
		//			// TokenInfo token = new TokenInfo(new String(cbuf, off, len));
		//			// curMethodTokenList.add(token);
		//
		//			out.write(cbuf, off, len);
		//		} catch (Exception e) {
		//			// throw new PrettyPrintingException(ioe);
		//			e.printStackTrace();
		//		}
	}

	/**
	 * Writes a string.
	 * 
	 * @param str
	 *            a string.
	 * @exception PrettyPrintingException
	 *                wrapping an IOException.
	 */
	protected void print(String str) {
		//		int i = str.lastIndexOf('\n');
		//		if (i >= 0) {
		//			column = str.length() - i + 1 + 1;
		//			do {
		//				//				dumpComments();
		//				line += 1;
		//				i = str.lastIndexOf('\n', i - 1);
		//			} while (i >= 0);
		//		} else {
		//			column += str.length();
		//		}
		//		try {
		//			if (str.trim().length() > 0) {
		//				LexemeInfo token = new LexemeInfo(str);
		//				curMethodTokenList.add(token);
		//			}
		//			out.write(str);
		//		} catch (Exception e) {
		//			// throw new PrettyPrintingException(ioe);
		//			e.printStackTrace();
		//		}
	}

	/**
	 * Indentation (cached).
	 */
	private int indentation;

	/*
	 * Wrap threshold (cached). private int wrap;
	 */

	/**
	 * Overwrite indentation flag (cached).
	 */
	private boolean overwriteIndentation;

	/**
	 * Overwrite parse positions flag (cached).
	 */
	private boolean overwriteParsePositions;

	public boolean getBooleanProperty(String key) {
		if (properties == null) {
			return false;
		}
		return StringUtils.parseBooleanProperty(properties.getProperty(key));
	}

	/**
	 * Get indentation amount (blanks per level).
	 * 
	 * @return the value of getIntegerProperty("indentationAmount").
	 */
	protected int getIndentation() {
		return indentation;
	}

	/**
	 * Returns true if the pretty printer should also reformat existing code.
	 * 
	 * @return the value of the overwriteIndentation property.
	 */
	protected boolean isOverwritingIndentation() {
		return overwriteIndentation;
	}

	/**
	 * Returns true if the pretty printer should reset the parse positions
	 * accordingly.
	 * 
	 * @return the value of the overwriteParsePositions property.
	 */
	protected boolean isOverwritingParsePositions() {
		return overwriteParsePositions;
	}

	/**
	 * Print program element header.
	 * 
	 * @param lf
	 *            an int value.
	 * @param blanks
	 *            an int value.
	 * @param elem
	 *            a program element.
	 */
	protected void printHeader(int lf, int blanks, ProgramElement elem) {
		printHeader(lf, 0, blanks, elem);
	}

	/**
	 * Print program element header.
	 * 
	 * @param blanks
	 *            an int value.
	 * @param elem
	 *            a program element.
	 */
	protected void printHeader(int blanks, ProgramElement elem) {
		printHeader(0, 0, blanks, elem);
	}

	/**
	 * Print program element header.
	 * 
	 * @param elem
	 *            a program element.
	 */
	protected void printHeader(ProgramElement elem) {
		printHeader(0, 0, 0, elem);
	}

	private SourceElement findFirstElementInclComment(SourceElement x) {
		if (!(x instanceof ProgramElement))
			return x.getFirstElement();
		List<Comment> cl = ((ProgramElement) x).getComments();
		int s = cl == null ? 0 : cl.size();
		for (int i = 0; i < s; i++) {
			Comment c = cl.get(i);
			if (c.isPrefixed()) {
				return c;
			}
		}
		return x.getFirstElement();
	}

	/**
	 * Print program element header.
	 * 
	 * @param lf
	 *            number of line feeds.
	 * @param levelChange
	 *            the level change.
	 * @param blanks
	 *            number of white spaces.
	 * @param x
	 *            the program element.
	 */
	protected void printHeader(int lf, int levelChange, int blanks,
			ProgramElement x) {
		level += levelChange;
		if (lf > 0) {
			blanks += getTotalIndentation();
		}
		SourceElement first = findFirstElementInclComment(x);

		setElementIndentation(lf, blanks, first);
		/*
		 * Position indent = first.getRelativePosition(); if (indent ==
		 * Position.UNDEFINED) { indent = new Position(lf, blanks); } else if
		 * (overwriteIndentation) { indent.setPosition(lf, blanks); } else { if
		 * (lf > indent.getLine()) { indent.setLine(lf); } if (blanks >
		 * indent.getColumn()) { indent.setColumn(blanks); } }
		 * first.setRelativePosition(indent);
		 */
		hasJustPrintedComment = false;
		int s = (x.getComments() != null) ? x.getComments().size() : 0;
		for (int i = 0; i < s; i += 1) {
			Comment c = x.getComments().get(i);
			if (c.isPrefixed()) {
				c.accept(this);
			}
		}
	}

	/**
	 * Sets end positions if required, and prints program element footer.
	 * 
	 * @param x
	 *            the program element.
	 */
	protected void printFooter(ProgramElement x) {
		// also in visitComment!
		if (overwriteParsePositions) {
			overwritePosition.setPosition(line, column);
			x.setEndPosition(overwritePosition);
		}
		int s = (x.getComments() != null) ? x.getComments().size() : 0;
		for (int i = 0; i < s; i += 1) {
			Comment c = x.getComments().get(i);
			if (!c.isPrefixed() && !c.isContainerComment()) {
				if (c instanceof SingleLineComment) {
					// Store until the next line feed is written.
					singleLineCommentWorkList.add((SingleLineComment) c);
				} else {
					c.accept(this);
				}
			}
		}
	}

	/**
	 * 
	 * @param x
	 * @return true if any comment has been printed, false otherwise
	 */
	protected boolean printContainerComments(ProgramElement x) {
		//System.out.println("printContainerComments: " + x.toString());
		// TODO overwriteParsePositions???
		boolean commentPrinted = false;
		int s = (x.getComments() != null) ? x.getComments().size() : 0;
		for (int i = 0; i < s; i += 1) {
			Comment c = x.getComments().get(i);
			if (c.isContainerComment()) {
				c.accept(this);
				printIndentation(1, getIndentation());
				commentPrinted = true;
				//System.out.println(c.getText());
			}
		}
		return commentPrinted;
	}

	protected void printOperator(Operator x, String symbol) {
		List<Expression> children = x.getArguments();
		if (children != null) {
			boolean addParentheses = x.isToBeParenthesized();
			if (addParentheses) {
				print('(');
			}
			switch (x.getArity()) {
			case 2:
				printElement(0, children.get(0));
				if (getBooleanProperty(GLUE_INFIX_OPERATORS)) {
					printElementIndentation(0, x);
					print(symbol);
					printElement(children.get(1));
				} else {
					printElementIndentation(1, x);
					print(symbol);
					printElement(1, children.get(1));
				}
				break;
			case 1:
				switch (x.getNotation()) {
				case Operator.PREFIX:

					print(symbol);
					if (getBooleanProperty(GLUE_UNARY_OPERATORS)) {
						printElement(0, children.get(0));
					} else {
						printElement(1, children.get(0));
					}
					break;
				case Operator.POSTFIX:
					printElement(0, children.get(0));
					if (getBooleanProperty(GLUE_UNARY_OPERATORS)) {

						print(symbol);
					} else {
						printElementIndentation(1, x);
						print(symbol);
					}
					break;
				default:
					break;
				}
			}
			if (addParentheses) {
				print(')');
			}
			if (x instanceof Assignment) {
				if (((Assignment) x).getStatementContainer() != null) {
					print(';');
				}
			}
		}
	}

	public void simpleAddStructure(int startTokenIdx, int statementType,
			String statementContent) {
		int endTokenIdx = curMethodTokenList.size();
		StatementLexemePart lexemePart = new StatementLexemePart(statementType,
				statementContent);
		lexemePart.addLexemeList(curMethodTokenList.subList(startTokenIdx,
				endTokenIdx));
		StatementLexeme statemenLexeme = new StatementLexeme(lexemePart);

		if (isAddToMethodLexList == 0) {
			methodStatementLexList.add(statemenLexeme);
		}
	}

	public void simplePrintAndAddStructure(String toPrintVal,
			int statementType, String statementContent) {
		int startTokenIdx = curMethodTokenList.size();
		print(toPrintVal);
		int endTokenIdx = curMethodTokenList.size();
		StatementLexemePart lexemePart = new StatementLexemePart(statementType,
				statementContent);
		lexemePart.addLexemeList(curMethodTokenList.subList(startTokenIdx,
				endTokenIdx));
		StatementLexeme statemenLexeme = new StatementLexeme(lexemePart);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statemenLexeme);
	}

	public void simpleAddStructureToList(int startTokenIdx, int statementType,
			String statementContent,
			List<StatementLexemePart> statementLexemeParts) {
		int endTokenIdx = curMethodTokenList.size();
		{
			StatementLexemePart lexemePart = new StatementLexemePart(
					statementType, statementContent);
			lexemePart.addLexemeList(curMethodTokenList.subList(startTokenIdx,
					endTokenIdx));
			statementLexemeParts.add(lexemePart);
		}
	}

	public void simplePrintAndAddStructureToList(String toPrintVal,
			int statementType, String statementContent,
			List<StatementLexemePart> statementLexemeParts) {
		int startTokenIdx = curMethodTokenList.size();
		print(toPrintVal);
		int endTokenIdx = curMethodTokenList.size();
		{
			StatementLexemePart lexemePart = new StatementLexemePart(
					statementType, statementContent);
			lexemePart.addLexemeList(curMethodTokenList.subList(startTokenIdx,
					endTokenIdx));
			statementLexemeParts.add(lexemePart);
		}
	}
	
	private void collectComments(ProgramElement x)
	{
		if(x.getComments() != null)
		{
			for(Comment cmt : x.getComments())
			{
				commentList.add(cmt.getText());
				/*if(cmt instanceof SingleLineComment)
					System.out.println("1." + cmt.getText());
				else if(cmt instanceof DocComment)
					System.out.println("2." + cmt.getText());
				else 
					System.out.println("3." + cmt.getText());*/
			}
		}
	}

	@Override
	public void visitIdentifier(Identifier x) {
		curID ++;

		print(x.getText());
		
		
		//if()
		/*if(!(x.getParent() != null && x.getParent() instanceof MethodReference))
		{
			identifierList.add(x.getText());
		}*/
		
		if(x.getParent() != null && (x.getParent() instanceof TypeReference || x.getParent() instanceof PackageReference 
																			|| x.getParent() instanceof MethodReference))
		{
			//these belong to API
			//System.out.println(x.getParent());
		}
		else
		{
			identifierList.add(x.getText());
		}
		
		collectComments(x);
			
		//System.out.println("Identifier: " + x.getText() + "-" + x.getParent());
	}

	@Override
	public void visitIntLiteral(IntLiteral x) {
		curID ++;

		print(x.getValue());
		collectComments(x);

	}

	@Override
	public void visitBooleanLiteral(BooleanLiteral x) {
		curID ++;

		print(x.getValue() ? "true" : "false");
		collectComments(x);

	}

	@Override
	public void visitStringLiteral(StringLiteral x) {
		curID ++;
		String tmp = encodeUnicodeChars(x.getValue());
		tmp = tmp.replaceAll("\\s", "_");
		print(tmp);
		//System.out.println("String: " + tmp);
		stringLiteralList.add(tmp.substring(1, tmp.length() -1));
		collectComments(x);
	}

	@Override
	public void visitNullLiteral(NullLiteral x) {
		curID ++;

		print("null");
		collectComments(x);

	}

	@Override
	public void visitCharLiteral(CharLiteral x) {
		curID ++;

		print(encodeUnicodeChars(x.getValue()));
		collectComments(x);

	}

	@Override
	public void visitDoubleLiteral(DoubleLiteral x) {
		curID ++;

		print(x.getValue());
		collectComments(x);

	}

	@Override
	public void visitLongLiteral(LongLiteral x) {
		curID ++;

		print(x.getValue());
		collectComments(x);

	}

	@Override
	public void visitFloatLiteral(FloatLiteral x) {
		curID ++;

		print(x.getValue());
		collectComments(x);

	}

	@Override
	public void visitPackageSpecification(PackageSpecification x) {
		
		identifierList.add(Naming.toPathName(x.getPackageReference()));
		curID ++;

		int startTokenIdx = curMethodTokenList.size();
		int m = 0;
		if (x.getAnnotations() != null && x.getAnnotations().size() > 0) {
			m = x.getAnnotations().size();
			printKeywordList(x.getAnnotations());
			m = 1;
		}

		printElementIndentation(m, x);
		print("package");

		printElement(1, x.getPackageReference());
		print(';');
		simpleAddStructure(startTokenIdx, StatementLexemePart.PACKAGESPEC,
				"PACKAGESPEC");

		fileInfo.packageDec = x.getPackageReference().toSource();

		collectComments(x);
	}

	@Override
	public void visitTypeReference(TypeReference x) 
	{
		
		//API.add(Naming.toPathName(x));
		String typeref = x.getName();
		if(!typeReferenceList.contains(typeref))
		{
			typeReferenceList.add(typeref);
			//System.out.println(typeref);
		}
		
		try
		{
			String name = sourceInfo.getType(x).getFullName();
			if(name.toLowerCase().startsWith("<unknown"))
				name = x.getName();
			API.add(name);
			
		}
		catch(Exception e)
		{
			API.add(x.getName());
		}
		
		curID ++;

		int startTokenIdx = 0;

		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}

		if (x.getReferencePrefix() != null) {
			printElement(x.getReferencePrefix());

			//			print('.');
			if (curMethodTokenList.size()>0)
				curMethodTokenList.get(curMethodTokenList.size()-1).content = 
				curMethodTokenList.get(curMethodTokenList.size()-1).content.trim() + ".";
		}
		if (x.getIdentifier() != null) {
			printElement(x.getIdentifier());
		}
		if (x.getTypeArguments() != null && x.getTypeArguments().size() > 0) {
			print('<');
			printCommaList(x.getTypeArguments());
			print('>');
		}
		for (int i = 0; i < x.getDimensions(); i += 1) {
			print("[ ]");
		}

		if (isStatement) {
			simpleAddStructure(startTokenIdx,
					StatementLexemePart.TYPEREFERENCE, "TYPEREFERENCE");
		}
		
		//System.out.println("visitTypeReference: " + x.getReferencePrefix() + "." + x.getName() + "." + x.getReferenceSuffix());
		
		collectComments(x);
	}

	@Override
	public void visitPackageReference(PackageReference x) {
		
		//API.add(x.getName());
		curID ++;

		int startTokenIdx = 0;

		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}

		if (x.getReferencePrefix() != null) {
			printElement(x.getReferencePrefix());

			//			print('.');
			if (curMethodTokenList.size()>0)
				curMethodTokenList.get(curMethodTokenList.size()-1).content = 
				curMethodTokenList.get(curMethodTokenList.size()-1).content.trim() + ".";
		}
		if (x.getIdentifier() != null) {
			printElement(x.getIdentifier());
		}

		if (isStatement) {
			simpleAddStructure(startTokenIdx,
					StatementLexemePart.PACKAGEREFERENCE, "PACKAGEREFERENCE");
		}
		
		collectComments(x);
	}

	@Override
	public void visitThrows(Throws x) {
		curID ++;
		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.THROWS, curLocalScopeList);
		curNode = nodeInfo;

		if (x.getExceptions() != null) {

			print("throws");
			printCommaList(0, 0, 1, x.getExceptions());
		}

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		collectComments(x);
	}

	@Override
	public void visitArrayInitializer(ArrayInitializer x) {
		curID ++;

		print('{');
		printContainerComments(x);

		if (x.getArguments() != null) {
			printCommaList(0, 0, 1, x.getArguments());
		}
		if (x.getArguments() != null && x.getArguments().size() > 0
				&& x.getRelativePosition().getLine() > 0) {
			printIndentation(1, getTotalIndentation());
			print('}');
		} else {
			print(" }");
		}
		collectComments(x);

	}

	@Override
	public void visitElementValueArrayInitializer(ElementValueArrayInitializer x) {
		curID ++;

		print('{');
		if (x.getElementValues() != null) {
			printCommaList(0, 0, 1, x.getElementValues());
		}
		if (x.getElementValues() != null && x.getElementValues().size() > 0
				&& x.getRelativePosition().getLine() > 0) {
			printIndentation(1, getTotalIndentation());
			print('}');
		} else {
			print(" }");
		}
		collectComments(x);

	}

	@Override
	public void visitCompilationUnit(CompilationUnit x) {
		curID ++;

		line = column = 1;

		setIndentationLevel(0);
		boolean hasPackageSpec = (x.getPackageSpecification() != null);
		if (hasPackageSpec) {
			printElement(x.getPackageSpecification());
		}
		boolean hasImports = (x.getImports() != null)
				&& (x.getImports().size() > 0);
		if (hasImports) {
			printLineList((x.getPackageSpecification() != null) ? 2 : 1, 0,
					x.getImports());
		}
		if (x.getDeclarations() != null) {
			printBlockList((hasImports || hasPackageSpec) ? 2 : 0, 0,
					x.getDeclarations());
		}

		// we do this linefeed here to allow flushing of the pretty printer
		// single line comment work list
		printIndentation(1, 0);
		collectComments(x);
	}

	@Override
	public void visitClassDeclaration(ClassDeclaration x) {
		curID ++;
		storeFields(true);
		curLocalScopeList.add(curID);
		curClassScopeList.add(curID);

		TypeInfo typeInfo = new TypeInfo();

		typeInfo.typeName = x.getName();
		if(Configurations.isGetTypeFullName)
		{
			try{
				if(x.getFullName()!=null)
				{
					typeInfo.typeName = x.getFullName();
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		typeInfo.packageDec = fileInfo.packageDec;
		fileInfo.typeInfoList.add(typeInfo);
		typeInfo.fileInfo = fileInfo;
		curTypeInfo = typeInfo;

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();
		int m = 0;
		if (x.getDeclarationSpecifiers() != null) {
			m = x.getDeclarationSpecifiers().size();
		}
		if (m > 0) {
			int startTokenIdx = curMethodTokenList.size();
			printKeywordList(x.getDeclarationSpecifiers());
			m = 1;
			simpleAddStructureToList(startTokenIdx, StatementLexemePart.SPEC,
					"SPEC", statementLexemeParts);
		}
		if (x.getIdentifier() != null) {
			int startTokenIdx = curMethodTokenList.size();
			printElementIndentation(m, x);
			print("class");
			simpleAddStructureToList(startTokenIdx, StatementLexemePart.CLASS,
					"CLASS", statementLexemeParts);

			startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getIdentifier());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.IDENTIFIER, "CLASSNAME",
					statementLexemeParts);
		}
		if (x.getTypeParameters() != null && x.getTypeParameters().size() > 0) {
			int startTokenIdx = curMethodTokenList.size();
			print("<");
			printCommaList(x.getTypeParameters());
			print("> ");
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.TYPEPARAMS, "TYPEPARAMS",
					statementLexemeParts);
		}
		if (x.getExtendedTypes() != null) {
			int startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getExtendedTypes());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.EXTENDS, "EXTENDS",
					statementLexemeParts);
		}
		if (x.getImplementedTypes() != null) {
			int startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getImplementedTypes());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.IMPLEMENTS, "IMPLEMENTS",
					statementLexemeParts);
		}
		if (x.getIdentifier() != null) {
			print(' ');
		}
		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		int startTokenIdx = curMethodTokenList.size();
		print('{');
		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN, "{");

		printContainerComments(x);
		if (x.getMembers() != null && !x.getMembers().isEmpty()) {
			printBlockList(2, 1, x.getMembers());
			changeLevel(-1);
		}
		printIndentation(1, getTotalIndentation());

		startTokenIdx = curMethodTokenList.size();
		print('}');
		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN, "}");

		curLocalScopeList.remove(curLocalScopeList.size()-1);
		curClassScopeList.remove(curClassScopeList.size()-1);

		typeList.add(typeInfo);
		
		restoreFields(true);
		collectComments(x);
	}

	@Override
	public void visitInterfaceDeclaration(InterfaceDeclaration x) {
		curID ++;

		visitInterfaceDeclaration(x, false);
	}

	private void visitInterfaceDeclaration(InterfaceDeclaration x,
			boolean annotation) {
		curID ++;
		storeFields(true);
		curLocalScopeList.add(curID);
		curClassScopeList.add(curID);

		TypeInfo typeInfo = new TypeInfo();
		typeInfo.typeName = x.getName();
		if(Configurations.isGetTypeFullName)
		{
			try{
				if(x.getFullName()!=null)
				{
					typeInfo.typeName = x.getFullName();
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		typeInfo.packageDec = fileInfo.packageDec;
		fileInfo.typeInfoList.add(typeInfo);
		typeInfo.fileInfo = fileInfo;
		curTypeInfo = typeInfo;

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();
		int m = 0;
		if (x.getDeclarationSpecifiers() != null) {
			m = x.getDeclarationSpecifiers().size();
		}
		if (m > 0) {
			int startTokenIdx = curMethodTokenList.size();
			printKeywordList(x.getDeclarationSpecifiers());
			m = 1;
			simpleAddStructureToList(startTokenIdx, StatementLexemePart.SPEC,
					"SPEC", statementLexemeParts);
		}
		if (x.getIdentifier() != null) {
			int startTokenIdx = curMethodTokenList.size();
			printElementIndentation(m, x);
			startTokenIdx = curMethodTokenList.size();
			if (annotation)
				print("@");
			print("interface");
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.INTERFACE, "INTERFACE",
					statementLexemeParts);

			startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getIdentifier());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.IDENTIFIER, "INTERFACENAME",
					statementLexemeParts);

		}
		if (x.getTypeParameters() != null && x.getTypeParameters().size() > 0) {
			int startTokenIdx = curMethodTokenList.size();
			print("<");
			printCommaList(x.getTypeParameters());
			print("> ");
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.TYPEPARAMS, "TYPEPARAMS",
					statementLexemeParts);
		}
		if (x.getExtendedTypes() != null) {
			int startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getExtendedTypes());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.EXTENDS, "EXTENDS",
					statementLexemeParts);
		}

		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		int startTokenIdx = curMethodTokenList.size();
		print(" {");
		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN, "{");

		printContainerComments(x);
		if (x.getMembers() != null && !x.getMembers().isEmpty()) {
			printBlockList(2, 1, x.getMembers());
			changeLevel(-1);
		}
		printIndentation(1, getTotalIndentation());

		startTokenIdx = curMethodTokenList.size();
		print('}');
		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN, "}");

		curLocalScopeList.remove(curLocalScopeList.size()-1);
		curClassScopeList.remove(curClassScopeList.size()-1);

		typeList.add(typeInfo);

		restoreFields(true);
		
		collectComments(x);
	}

	@Override
	public void visitAnnotationDeclaration(AnnotationDeclaration x) {
		curID ++;

		visitInterfaceDeclaration(x, true);
		
		collectComments(x);
	}

	@Override
	public void visitFieldDeclaration(FieldDeclaration x) {


		int startTokenIdx = curMethodTokenList.size();

		int m = 0;
		if (x.getDeclarationSpecifiers() != null) {
			m = x.getDeclarationSpecifiers().size();
			printKeywordList(x.getDeclarationSpecifiers());
		}
		printElement((m > 0) ? 1 : 0, x.getTypeReference());
		List<? extends VariableSpecification> varSpecs = x.getVariables();
		if (varSpecs != null) {
			printCommaList(0, 0, 1, varSpecs);
		}

		print(';');
		simpleAddStructure(startTokenIdx, StatementLexemePart.FIELDECLARATION,
				"FIELDECLARATION");

		//		NodeGroumVisitProcessing.addVarNode(curMethodInfo, parentNodeStack, previousControlFlowNodeStack, curID, x);

		for (int i=0; i<x.getVariables().size(); i++)
		{
			FieldSpecification tmp = x.getFieldSpecifications().get(i);
			curID ++;

			String fieldName = tmp.getName();
			String varName = "this";
			String typeVarName = "this";
			if (curTypeInfo!=null&&curTypeInfo.typeName!=null)
				if (curTypeInfo.typeName.length()>0)
				{
					typeVarName = curTypeInfo.typeName;
					varName = typeVarName;
				}


			String typeFieldName = "";
			if (sourceInfo.getType(x)!=null)
			{
				if(Configurations.isGetTypeFullName)
				{
					try{
						typeFieldName = sourceInfo.getType(x).getFullName();
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				else
				{
					typeFieldName = sourceInfo.getType(x).getName();
				}
			}
			curID ++;
			varTypeMap.put(fieldName, typeFieldName);
			JavaNodeGroumVisitProcessing.addNewFieldAccessNode(
					varName, typeVarName,
					fieldName, typeFieldName, 
					curMethodInfo, parentNodeStack, previousControlFlowNodeStack, 
					previousDataNodeStack,
					previousDataNodeMap,
					curID, x,
					curClassScopeList
					);	
		}

		collectComments(x);
	}

	@Override
	public void visitLocalVariableDeclaration(LocalVariableDeclaration x) {


		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		int startTokenIdx = 0;
		;

		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();
		int m = 0;
		if (x.getDeclarationSpecifiers() != null) {
			m = x.getDeclarationSpecifiers().size();
			startTokenIdx = curMethodTokenList.size();
			printKeywordList(x.getDeclarationSpecifiers());
			simpleAddStructureToList(startTokenIdx, StatementLexemePart.SPEC,
					"SPEC", statementLexemeParts);

		}

		startTokenIdx = curMethodTokenList.size();
		printElement((m > 0) ? 1 : 0, x.getTypeReference());
		simpleAddStructureToList(startTokenIdx,
				StatementLexemePart.TYPEREFERENCE, "TYPEREFERENCE",
				statementLexemeParts);

		List<? extends VariableSpecification> varSpecs = x.getVariables();
		if (varSpecs != null) {
			startTokenIdx = curMethodTokenList.size();
			printCommaList(0, 0, 1, varSpecs);
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.VARIABLESPECIFICATION,
					"VARIABLESPECIFICATION", statementLexemeParts);
		}

		if (!(x.getStatementContainer() instanceof LoopStatement)) {
			startTokenIdx = curMethodTokenList.size();
			print(';');
			//			simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
			//					";", statementLexemeParts);
			statementLexemeParts.get(statementLexemeParts.size()-1).addLexeme(new LexemeInfo(";"));

		}

		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isStatement) {
			if (isAddToMethodLexList == 0)
				methodStatementLexList.add(statement);
		}


		//		NodeGroumVisitProcessing.addVarNode(curMethodInfo, parentNodeStack, previousControlFlowNodeStack, curID, x);

		for (int i=0; i<x.getVariables().size(); i++)
		{
			VariableSpecification tmp = x.getVariables().get(i);
			String varName = tmp.getName();

			String typeName = tmp.getType().getName();
			if(Configurations.isGetTypeFullName)
			{
				try{
					typeName = tmp.getType().getFullName();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			curID ++;
			varTypeMap.put(varName, typeName);

			JavaNodeGroumVisitProcessing.addVarNode(varName,typeName,curMethodInfo, parentNodeStack, 
					previousControlFlowNodeStack, previousDataNodeStack, previousDataNodeMap,curID, x, 
					curLocalScopeList,
					true);
		}
		
		collectComments(x);
	}

	@Override
	protected void visitVariableDeclaration(VariableDeclaration x) {
		curID ++;

		visitVariableDeclaration(x, false);
		
		collectComments(x);
	
	}

	protected void visitVariableDeclaration(VariableDeclaration x, boolean spec) {


		int startTokenIdx = 0;

		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((parent instanceof StatementBlock)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}

		int m = 0;
		if (x.getDeclarationSpecifiers() != null) {
			m = x.getDeclarationSpecifiers().size();
			printKeywordList(x.getDeclarationSpecifiers());
		}
		printElement((m > 0) ? 1 : 0, x.getTypeReference());
		if (spec) {
			print(" ...");
			// printElement(spec);
		}
		List<? extends VariableSpecification> varSpecs = x.getVariables();
		if (varSpecs != null) {
			printCommaList(0, 0, 1, varSpecs);
		}
		if (isStatement) {
			simpleAddStructure(startTokenIdx,
					StatementLexemePart.VARIABLEDECLARATION,
					"VARIABLEDECLARATION");
		}

		//		NodeGroumVisitProcessing.addVarNode(curMethodInfo, parentNodeStack, previousControlFlowNodeStack, curID, x);
		for (int i=0; i<x.getVariables().size(); i++)
		{
			VariableSpecification tmp = x.getVariables().get(i);
			String varName = tmp.getName();
			String typeName = x.getTypeReference().getName();//tmp.getType().getName();
			if(Configurations.isGetTypeFullName)
			{
				//				typeName =  x.getTypeReference().getPackageReference().getName() + "." +  x.getTypeReference().getName();
				try{
					typeName = sourceInfo.getType(x).getFullName();
				}
				catch(Exception e){
					//					e.printStackTrace();
				}
			}
			curID ++;
			varTypeMap.put(varName, typeName);

			JavaNodeGroumVisitProcessing.addVarNode(varName,typeName,curMethodInfo, parentNodeStack, 
					previousControlFlowNodeStack, previousDataNodeStack, previousDataNodeMap,curID, x, 
					curLocalScopeList,
					true);

		}
	}


	@Override
	public void visitMethodDeclaration(MethodDeclaration x) {
		
		//System.out.println("visitMethodDeclaration: " + x.getName());
		
		curID ++;
		storeFields(false);
		curLocalScopeList.add(curID);

		String mName = x.getName();
		ArrayList<String> paramsList = null;
		if (x.getParameters()!=null){
			if (x.getParameters().size()>0){
				paramsList = new ArrayList<String>(); 
				for (ParameterDeclaration p:x.getParameters()){
					String type = p.getTypeReference().toSource();
					paramsList.add(type);
				}
			}
		}	
//		MethodInfo methodInfo = new MethodInfo(mName, x.toSource(),  paramsList, curTypeInfo, fileInfo, getLOCs(x));
		//FIXME: temporarily give content = empty string to save memory
		MethodInfo methodInfo = new MethodInfo(mName, x.toSource(),  paramsList, curTypeInfo, fileInfo, getLOCs(x));

		

		curMethodInfo = methodInfo;
		curTypeInfo.methodDecList.add(curMethodInfo);

		boolean isInDeepMethod = false;
		if (isInAMethod > 0) {
			isInDeepMethod = true;
		}
		isInAMethod++;
		//		countMethod++;

		if (!isInDeepMethod) {
			countMethod++;
			curMethodTokenList = new ArrayList<LexemeInfo>();
			methodStatementLexList = new ArrayList<StatementLexeme>();
		}
		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();
		int m = 0;

		int startTokenIdx = curMethodTokenList.size();

		if (x.getDeclarationSpecifiers() != null) {
			m = x.getDeclarationSpecifiers().size();
			printKeywordList(x.getDeclarationSpecifiers());
		}

		if (x.getTypeParameters() != null && x.getTypeParameters().size() > 0) {
			//			int startTokenIdx = curMethodTokenList.size();
			if (m > 0)
				print(' ');
			else

				print('<');
			printCommaList(x.getTypeParameters());
			print('>');
			m = 1; // print another blank afterwards

		}

		if (x.getTypeReference() != null) {
			//			int startTokenIdx = curMethodTokenList.size();
			if (m > 0) {
				printElement(1, x.getTypeReference());
			} else {
				printElement(x.getTypeReference());
			}
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.MODIFIER, "MODIFIER",
					statementLexemeParts);

			startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getIdentifier());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.IDENTIFIER, "MNAME",
					statementLexemeParts);

		} else {
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.MODIFIER, "MODIFIER",
					statementLexemeParts);

			startTokenIdx = curMethodTokenList.size();
			if (m > 0) {
				printElement(1, x.getIdentifier());
			} else {
				printElement(x.getIdentifier());
			}
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.IDENTIFIER, "MNAME",
					statementLexemeParts);
		}

		startTokenIdx = curMethodTokenList.size();
		if (getBooleanProperty(GLUE_PARAMETER_LISTS)) {
			print('(');
		} else {
			print(" (");
		}
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				"(", statementLexemeParts);


		startTokenIdx = curMethodTokenList.size();
		if (x.getParameters() != null) {
			List<? extends ParameterDeclaration> params = x.getParameters();
			printCommaList(getBooleanProperty(GLUE_PARAMETERS) ? 0 : 1, params);
		}
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.PARAMS,
				"PARAMS", statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		print(')');		
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				")", statementLexemeParts);


		if (x.getThrown() != null) {
			startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getThrown());
			simpleAddStructureToList(startTokenIdx, StatementLexemePart.THROWS,
					"THROWS", statementLexemeParts);
		}

		if (x instanceof AnnotationPropertyDeclaration) {
			startTokenIdx = curMethodTokenList.size();
			AnnotationPropertyDeclaration apd = (AnnotationPropertyDeclaration) x;
			Expression e = apd.getDefaultValueExpression();
			if (e != null) {
				print(" default ");
				e.accept(this);
			}
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.PROPDEC, "PROPDEC",
					statementLexemeParts);
		}

		StatementLexeme methodStartStatement = new StatementLexeme(
				statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(methodStartStatement);

		if (x.getBody() != null) {
			printElement(1, x.getBody());
		} else {
			startTokenIdx = curMethodTokenList.size();
			print(';');
			//			statementLexemeParts.get(statementLexemeParts.size()-1).addLexeme(new LexemeInfo(";"));
			simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN, ";");
		}


		isInAMethod--;

//		if (!isInDeepMethod) {
//			Logger.logDebug("**************************************\r\n"
//					+ "MethodDeclaration " + countMethod + ": " + x.toSource());
//
//			Logger.logDebug("Data Info: " + curMethodInfo);
//		}

		curLocalScopeList.remove(curLocalScopeList.size()-1);
		restoreFields(false);
		
		collectComments(x);
	}
	
	//FIXME: add more heuristic to determine LOCs;
	public long getLOCs(MethodDeclaration x){
		long LOCs = 0;
		String tmp = x.toSource();
		Scanner sc = new Scanner(tmp);
		while (sc.hasNextLine()){
			String tmpLine = sc.nextLine();
			if (tmpLine.trim().length()==0)
				continue;
			if (tmpLine.trim().startsWith("//"))
				continue;
			if (tmpLine.trim().startsWith("/*"))
				continue;
			if (tmpLine.trim().endsWith("*/"))
				continue;
			LOCs++;
		}
		sc.close();
		return LOCs;
	}

	@Override
	public void visitClassInitializer(ClassInitializer x) {
		curID ++;
		storeFields(true);
		curLocalScopeList.add(curID);

		int m = 0;
		if (x.getDeclarationSpecifiers() != null) {
			int startTokenIdx = curMethodTokenList.size();
			m = x.getDeclarationSpecifiers().size();
			printKeywordList(x.getDeclarationSpecifiers());
			endTokenIdx = curMethodTokenList.size();
			simpleAddStructure(startTokenIdx, StatementLexemePart.SPEC, "SPEC");
		}
		if (x.getBody() != null) {
			printElement(m > 0 ? 1 : 0, x.getBody());
		}

		curLocalScopeList.remove(curLocalScopeList.size()-1);
		restoreFields(true);
		
		collectComments(x);

	}

	@Override
	public void visitStatementBlock(StatementBlock x) {
		curID ++;
		curLocalScopeList.add(curID);

		simplePrintAndAddStructure("{", StatementLexemePart.STOKEN, "{");

		boolean doNotPossiblyPrintIndentation = printContainerComments(x);
		if (x.getBody() != null && x.getBody().size() > 0) {
			printLineList(1, +1, x.getBody());
			changeLevel(-1);
			Position firstStatementEndPosition = x.getBody().get(0)
					.getEndPosition();
			Position blockEndPosition = x.getEndPosition();
			if (x.getBody().size() > 1
					|| firstStatementEndPosition.equals(Position.UNDEFINED)
					|| blockEndPosition.equals(Position.UNDEFINED)
					|| firstStatementEndPosition.getLine() < blockEndPosition
					.getLine())
				printIndentation(1, getTotalIndentation());
			else
				printIndentation(0, blockEndPosition.getColumn()
						- firstStatementEndPosition.getColumn() - 1);
		} else if (!doNotPossiblyPrintIndentation) {
			// keep old indentation
			int lf = x.getEndPosition().getLine()
					- x.getStartPosition().getLine();
			if (lf > 0)
				printIndentation(lf, getIndentation());
		}

		simplePrintAndAddStructure("}", StatementLexemePart.STOKEN, "}");
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitBreak(Break x) {
		curID ++;

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.BREAK, curLocalScopeList);
		curNode = nodeInfo;

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();

		simplePrintAndAddStructureToList("break", StatementLexemePart.BREAK,
				"BREAK", statementLexemeParts);

		if (x.getIdentifier() != null) {
			int startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getIdentifier());
			endTokenIdx = curMethodTokenList.size();
			{
				StatementLexemePart lexemePart = new StatementLexemePart(
						StatementLexemePart.IDENTIFIER, "LABEL");
				lexemePart.addLexemeList(curMethodTokenList.subList(
						startTokenIdx, endTokenIdx));
				statementLexemeParts.add(lexemePart);
			}
		}


		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		curMethodTokenList.size();
		print(';');
		statementLexemeParts.get(statementLexemeParts.size()-1).addLexeme(new LexemeInfo(";"));

		//		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN,";");

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		
		collectComments(x);

	}

	@Override
	public void visitContinue(Continue x) {
		curID ++;

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.CONTINUE, curLocalScopeList);
		curNode = nodeInfo;

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();

		simplePrintAndAddStructureToList("continue",
				StatementLexemePart.CONTINUE, "CONTINUE", statementLexemeParts);

		if (x.getIdentifier() != null) {
			int startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getIdentifier());
			endTokenIdx = curMethodTokenList.size();
			{
				StatementLexemePart lexemePart = new StatementLexemePart(
						StatementLexemePart.IDENTIFIER, "LABEL");
				lexemePart.addLexemeList(curMethodTokenList.subList(
						startTokenIdx, endTokenIdx));
				statementLexemeParts.add(lexemePart);
			}
		}


		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		curMethodTokenList.size();
		print(';');
		//		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN,";");
		statementLexemeParts.get(statementLexemeParts.size()-1).addLexeme(new LexemeInfo(";"));

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		
		collectComments(x);

	}

	@Override
	public void visitReturn(Return x) {
		curID ++;

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.RETURN, curLocalScopeList);
		curNode = nodeInfo;

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();

		simplePrintAndAddStructureToList("return", StatementLexemePart.RETURN,
				"RETURN", statementLexemeParts);

		if (x.getExpression() != null) {
			int startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getExpression());
			endTokenIdx = curMethodTokenList.size();
			{
				StatementLexemePart lexemePart = new StatementLexemePart(
						StatementLexemePart.EXPRESSION, "EXPRESSION");
				lexemePart.addLexemeList(curMethodTokenList.subList(
						startTokenIdx, endTokenIdx));
				statementLexemeParts.add(lexemePart);
			}
		}


		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		curMethodTokenList.size();
		print(';');
		//		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN,";");
		statementLexemeParts.get(statementLexemeParts.size()-1).addLexeme(new LexemeInfo(";"));
		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		
		collectComments(x);

	}

	@Override
	public void visitThrow(Throw x) {
		curID ++;

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.THROW, curLocalScopeList);
		curNode = nodeInfo;

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();

		simplePrintAndAddStructureToList("throw", StatementLexemePart.RETURN,
				"RETURN", statementLexemeParts);

		if (x.getExpression() != null) {
			int startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getExpression());
			endTokenIdx = curMethodTokenList.size();
			{
				StatementLexemePart lexemePart = new StatementLexemePart(
						StatementLexemePart.EXPRESSION, "EXPRESSION");
				lexemePart.addLexemeList(curMethodTokenList.subList(
						startTokenIdx, endTokenIdx));
				statementLexemeParts.add(lexemePart);
			}
		}


		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		curMethodTokenList.size();
		print(';');
		//		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN,";");
		statementLexemeParts.get(statementLexemeParts.size()-1).addLexeme(new LexemeInfo(";"));

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		
		collectComments(x);
	}

	@Override
	public void visitDo(Do x) {
		curID ++;
		curLocalScopeList.add(curID);

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.DO, curLocalScopeList);
		curNode = nodeInfo;

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();

		simplePrintAndAddStructureToList("do", StatementLexemePart.DO, "DO", statementLexemeParts);

		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		if (x.getBody() == null || x.getBody() instanceof EmptyStatement) {
			//			simplePrintAndAddStructure(";", StatementLexemePart.STOKEN, ";");
			print(";");
			statementLexemeParts.get(statementLexemeParts.size()-1).addLexeme(new LexemeInfo(";"));
			// w.printElement(1, body);
		} else {
			if (getBooleanProperty(GLUE_STATEMENT_BLOCKS)) {
				printElement(1, x.getBody());
			} else {
				if (x.getBody() instanceof StatementBlock) {
					printElement(1, 0, x.getBody());
				} else {
					printElement(1, +1, 0, x.getBody());
					changeLevel(-1);
				}
			}
		}

		List<StatementLexemePart> statementLexemeParts2 = new ArrayList<StatementLexemePart>();
		int startTokenIdx = curMethodTokenList.size();
		if (getBooleanProperty(GLUE_STATEMENT_BLOCKS)) {
			print(" while");
		} else {
			printIndentation(1, getTotalIndentation());
			print("while");
		}
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.WHILE,
				"WHILE", statementLexemeParts2);

		startTokenIdx = curMethodTokenList.size();
		if (getBooleanProperty(GLUE_PARAMETER_LISTS)) {
			print('(');
		} else {
			print(" (");
		}

		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				"(", statementLexemeParts2);

		if (x.getGuard() != null) {
			startTokenIdx = curMethodTokenList.size();

			boolean glueExprParentheses = getBooleanProperty(GLUE_EXPRESSION_PARENTHESES);
			if (!glueExprParentheses) {
				print(' ');
			}
			printElement(x.getGuard());
			if (!glueExprParentheses) {
				print(' ');
			}

			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.EXPRESSION, "EXPRESSION",
					statementLexemeParts2);
		}

		startTokenIdx = curMethodTokenList.size();
		print(")");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				")", statementLexemeParts2);


		StatementLexeme statement2 = new StatementLexeme(statementLexemeParts2);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement2);

		startTokenIdx = curMethodTokenList.size();
		print(";");
		//		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN,";");
		statementLexemeParts2.get(statementLexemeParts2.size()-1).addLexeme(new LexemeInfo(";"));
		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		previousControlFlowNodeStack.add(nodeInfo);

		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitFor(For x) {
		curID ++;
		curLocalScopeList.add(curID);

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();
		int startTokenIdx = curMethodTokenList.size();
		// print(getBooleanProperty(GLUE_CONTROL_EXPRESSIONS) ? "for(" :
		// "for (");
		print("for");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.FOR, "FOR",
				statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		print("(");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				"(", statementLexemeParts);

		boolean glueExprParentheses = getBooleanProperty(GLUE_EXPRESSION_PARENTHESES);
		if (!glueExprParentheses) {
			print(' ');
		}

		startTokenIdx = curMethodTokenList.size();
		if (x.getInitializers() != null) {
			printCommaList(x.getInitializers());
		}
		print(';');
		simpleAddStructureToList(startTokenIdx,
				StatementLexemePart.INITIALIZER, "INITIALIZER",
				statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		if (x.getGuard() != null) {
			printElement(1, x.getGuard());
		}
		print(';');
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.EXPRESSION,
				"EXPRESSION", statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		if (x.getUpdates() != null) {
			printCommaList(0, 0, 1, x.getUpdates());
		}
		if (!glueExprParentheses) {
			print(' ');
		}
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.UPDATES,
				"UPDATES", statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		print(')');
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				")", statementLexemeParts);

		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		curID ++;

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.FOR, curLocalScopeList);
		curNode = nodeInfo;


		if (x.getBody() == null || x.getBody() instanceof EmptyStatement) {
			startTokenIdx = curMethodTokenList.size();
			print(';');
			//			simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN, ";");
			statementLexemeParts.get(statementLexemeParts.size()-1).addLexeme(new LexemeInfo(";"));

		} else {
			if (getBooleanProperty(GLUE_STATEMENT_BLOCKS)) {
				printElement(1, x.getBody());
			} else {
				if (x.getBody() instanceof StatementBlock) {
					printElement(1, 0, x.getBody());
				} else {
					printElement(1, +1, 0, x.getBody());
					changeLevel(-1);
				}
			}
		}

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		previousControlFlowNodeStack.add(nodeInfo);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitEnhancedFor(EnhancedFor x) {

		curID ++;
		curLocalScopeList.add(curID);
		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();
		int startTokenIdx = curMethodTokenList.size();
		// print(getBooleanProperty(GLUE_CONTROL_EXPRESSIONS) ? "for(" :
		// "for (");
		print("for");
		simpleAddStructureToList(startTokenIdx,
				StatementLexemePart.ENHANCEDFOR, "ENHANCEDFOR",
				statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		print("(");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				"(", statementLexemeParts);

		boolean glueExprParentheses = getBooleanProperty(GLUE_EXPRESSION_PARENTHESES);
		if (!glueExprParentheses) {
			print(' ');
		}
		startTokenIdx = curMethodTokenList.size();
		printCommaList(x.getInitializers()); // must not be null for enhanced
		// for loop
		simpleAddStructureToList(startTokenIdx,
				StatementLexemePart.INITIALIZER, "INITIALIZER",
				statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		print(':');
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				":", statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		printElement(1, x.getGuard()); // must not be null for enhanced for loop
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.EXPRESSION,
				"EXPRESSION", statementLexemeParts);

		if (!glueExprParentheses) {
			print(' ');
		}
		startTokenIdx = curMethodTokenList.size();
		print(')');
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				")", statementLexemeParts);

		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		curID ++;

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.ENHANCEDFOR, curLocalScopeList);
		curNode = nodeInfo;

		if (x.getBody() == null || x.getBody() instanceof EmptyStatement) {
			startTokenIdx = curMethodTokenList.size();
			print(';');
			//			simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN, ";");
			statementLexemeParts.get(statementLexemeParts.size()-1).addLexeme(new LexemeInfo(";"));
		} else {
			if (getBooleanProperty(GLUE_STATEMENT_BLOCKS)) {
				printElement(1, x.getBody());
			} else {
				printElement(1, +1, 0, x.getBody());
				changeLevel(-1);
			}
		}

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		previousControlFlowNodeStack.add(nodeInfo);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitWhile(While x) {
		curID ++;
		curLocalScopeList.add(curID);


		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();

		int startTokenIdx = curMethodTokenList.size();

		print("while");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.WHILE,
				"WHILE", statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		print("(");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				"(", statementLexemeParts);

		boolean glueExpParentheses = getBooleanProperty(GLUE_EXPRESSION_PARENTHESES);
		if (!glueExpParentheses) {
			print(' ');
		}
		if (x.getGuard() != null) {
			startTokenIdx = curMethodTokenList.size();
			printElement(x.getGuard());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.EXPRESSION, "EXPRESSION",
					statementLexemeParts);
		}

		startTokenIdx = curMethodTokenList.size();
		if (glueExpParentheses) {
			print(')');
		} else {
			print(" )");
		}
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				")", statementLexemeParts);
		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		curID ++;
		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.WHILE, curLocalScopeList);
		curNode = nodeInfo;

		if (x.getBody() == null || x.getBody() instanceof EmptyStatement) {
			//			startTokenIdx = curMethodTokenList.size();
			print(';');
			//			simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN, ";");
			statementLexemeParts.get(statementLexemeParts.size()-1).addLexeme(new LexemeInfo(";"));

		} else {
			if (getBooleanProperty(GLUE_STATEMENT_BLOCKS)) {
				printElement(1, x.getBody());
			} else {
				if (x.getBody() instanceof StatementBlock) {
					printElement(1, 0, x.getBody());
				} else {
					printElement(1, +1, 0, x.getBody());
					changeLevel(-1);
				}
			}
		}

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		previousControlFlowNodeStack.add(nodeInfo);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitAssert(Assert x) {
		curID ++;

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.ASSERT, curLocalScopeList);
		curNode = nodeInfo;

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();

		int startTokenIdx = curMethodTokenList.size();
		print("assert");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.ASSERT,
				"ASSERT", statementLexemeParts);

		if (x.getCondition() != null) {
			startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getCondition());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.EXPRESSION, "EXPRESSION",
					statementLexemeParts);

		}
		if (x.getMessage() != null) {
			startTokenIdx = curMethodTokenList.size();
			print(" :");
			simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
					":", statementLexemeParts);

			startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getMessage());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.EXPRESSION, "EXPRESSION",
					statementLexemeParts);

		}


		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		startTokenIdx = curMethodTokenList.size();
		print(';');
		//		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN,";");
		statementLexemeParts.get(statementLexemeParts.size()-1).addLexeme(new LexemeInfo(";"));

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		
		collectComments(x);

	}

	@Override
	public void visitIf(If x) {
		curID ++;
		curLocalScopeList.add(curID);

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();
		int startTokenIdx = curMethodTokenList.size();
		// print(getBooleanProperty(GLUE_CONTROL_EXPRESSIONS) ? "if(" : "if (");
		print("if");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.IF, "IF",
				statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		print("(");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				"(", statementLexemeParts);

		boolean glueExpr = getBooleanProperty(GLUE_EXPRESSION_PARENTHESES);
		if (x.getExpression() != null) {
			startTokenIdx = curMethodTokenList.size();
			if (glueExpr) {
				printElement(x.getExpression());
			} else {
				printElement(1, x.getExpression());
			}
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.EXPRESSION, "EXPRESSION",
					statementLexemeParts);
		}
		startTokenIdx = curMethodTokenList.size();
		if (glueExpr) {
			print(')');
		} else {
			print(" )");
		}
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				")", statementLexemeParts);
		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		curID ++;

		Stack<NodeInfo> tmp = new Stack<NodeInfo>();
		tmp.addAll(previousControlFlowNodeStack);
		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.IF, curLocalScopeList);
		curNode = nodeInfo;

		if (x.getThen() != null) {
			if (getBooleanProperty(GLUE_STATEMENT_BLOCKS)) {
				printElement(1, x.getThen());
			} else {
				if (x.getThen().getBody() instanceof StatementBlock) {
					printElement(1, 0, x.getThen());
				} else {
					printElement(1, +1, 0, x.getThen());
					changeLevel(-1);
				}
			}
		}
		if (x.getElse() != null) {
			if (getBooleanProperty(GLUE_SEQUENTIAL_BRANCHES)) {
				printElement(1, x.getElse());
			} else {
				printElement(1, 0, x.getElse());
			}
		}

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		previousControlFlowNodeStack = tmp;
		previousControlFlowNodeStack.add(nodeInfo);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitSwitch(Switch x) {
		curID ++;
		curLocalScopeList.add(curID);

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();

		int startTokenIdx = curMethodTokenList.size();
		// print("switch (");
		print("switch");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.SWITCH,
				"SWITCH", statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		print("(");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				"(", statementLexemeParts);

		if (x.getExpression() != null) {
			startTokenIdx = curMethodTokenList.size();
			printElement(x.getExpression());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.EXPRESSION, "EXPRESSION",
					statementLexemeParts);
		}
		startTokenIdx = curMethodTokenList.size();
		print(")");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				")", statementLexemeParts);
		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		curID ++;

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.SWITCH, curLocalScopeList);
		curNode = nodeInfo;

		startTokenIdx = curMethodTokenList.size();
		print("{");
		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN,"{");
		if (x.getBranchList() != null) {
			if (getBooleanProperty(GLUE_SEQUENTIAL_BRANCHES)) {
				printLineList(1, +1, x.getBranchList());
				changeLevel(-1);
			} else {
				printLineList(1, 0, x.getBranchList());
			}
		}
		printIndentation(1, getTotalIndentation());

		startTokenIdx = curMethodTokenList.size();
		print('}');
		simpleAddStructure(startTokenIdx, StatementLexemePart.STOKEN, "}");

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		previousControlFlowNodeStack.add(nodeInfo);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitTry(Try x) {
		curID ++;
		curLocalScopeList.add(curID);
		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.TRY, curLocalScopeList);
		curNode = nodeInfo;
		int startTokenIdx = curMethodTokenList.size();
		print("try");
		simpleAddStructure(startTokenIdx, StatementLexemePart.TRY, "TRY");

		if (x.getBody() != null) {
			if (getBooleanProperty(GLUE_STATEMENT_BLOCKS)) {
				printElement(1, x.getBody());
			} else {
				printElement(1, 0, x.getBody());
			}
		}
		if (x.getBranchList() != null) {
			if (getBooleanProperty(GLUE_SEQUENTIAL_BRANCHES)) {
				for (int i = 0; i < x.getBranchList().size(); i++) {
					printElement(1, x.getBranchList().get(i));
				}
			} else {
				printLineList(1, 0, x.getBranchList());
			}
		}
		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		previousControlFlowNodeStack.add(nodeInfo);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitLabeledStatement(LabeledStatement x) {
		curID ++;

		if (x.getIdentifier() != null) {
			List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();

			int startTokenIdx = curMethodTokenList.size();
			printElement(x.getIdentifier());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.IDENTIFIER, "IDENTIFIER",
					statementLexemeParts);
			startTokenIdx = curMethodTokenList.size();
			print(':');
			simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
					":", statementLexemeParts);
			StatementLexeme statement = new StatementLexeme(
					statementLexemeParts);
			if (isAddToMethodLexList == 0)
				methodStatementLexList.add(statement);
		}
		if (x.getBody() != null) {
			printElement(1, 0, x.getBody());
			
			
		}
		
		collectComments(x);

	}

	@Override
	public void visitSynchronizedBlock(SynchronizedBlock x) {
		curID ++;
		curLocalScopeList.add(curID);

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();
		int startTokenIdx = curMethodTokenList.size();
		print("synchronized");
		simpleAddStructureToList(startTokenIdx,
				StatementLexemePart.SYNCHRONIZED, "SYNCHRONIZED",
				statementLexemeParts);

		if (x.getExpression() != null) {
			startTokenIdx = curMethodTokenList.size();
			print('(');
			simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
					"(", statementLexemeParts);

			startTokenIdx = curMethodTokenList.size();
			printElement(x.getExpression());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.EXPRESSION, "EXPRESSION",
					statementLexemeParts);

			startTokenIdx = curMethodTokenList.size();
			print(')');
			simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
					")", statementLexemeParts);

		}
		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		curID ++;
		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.SYNCHRONIZED, curLocalScopeList);
		curNode = nodeInfo;

		if (x.getBody() != null) {
			printElement(1, x.getBody());
		}
		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		previousControlFlowNodeStack.add(nodeInfo);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitImport(Import x) {
		curID ++;

		int startTokenIdx = curMethodTokenList.size();

		print("import");
		if (x.isStaticImport())
			print(" static");
		printElement(1, x.getReference());
		if (x.isMultiImport()) {
			print(".*;");
		} else {
			if (x.isStaticImport()) {
				print(".");
				printElement(x.getStaticIdentifier());
			}
			print(';');
		}

		simpleAddStructure(startTokenIdx, StatementLexemePart.IMPORT, "IMPORT");
		
		collectComments(x);
	}

	@Override
	public void visitUncollatedReferenceQualifier(UncollatedReferenceQualifier x) {
		curID ++;

		if (Configurations.isGoMoreInFields)
		{
			int startTokenIdx = 0;

			NonTerminalProgramElement parent = x.getASTParent();
			boolean isStatement = false;
			if ((parent instanceof StatementBlock)) {
				isStatement = true;
			}
			if (isStatement) {
				startTokenIdx = curMethodTokenList.size();
			}
			if (x.getReferencePrefix() != null) {
				printElement(x.getReferencePrefix());

				//			print('.');
				if (curMethodTokenList.size()>0)
					curMethodTokenList.get(curMethodTokenList.size()-1).content = 
					curMethodTokenList.get(curMethodTokenList.size()-1).content.trim() + ".";
			}
			if (x.getIdentifier() != null) {
				printElement(x.getIdentifier());
			}
			if (isStatement) {
				simpleAddStructure(startTokenIdx,
						StatementLexemePart.UNCOLREFERENCE, "UNCOLREFERENCE");
			}
		}

		//		if (x.getReferencePrefix()==null)
		//		{
		//			String varName =  "this";
		//			String typeName =  "this";
		//			if (curTypeInfo!=null&&curTypeInfo.typeName!=null)
		//				if (curTypeInfo.typeName.length()>0)
		//				{
		//					typeName = curTypeInfo.typeName;
		//					varName = typeName;
		//				}
		//			try{
		//				varName = x.getName();
		//				typeName = sourceInfo.getType(x).getName();
		//
		//			}
		//			catch(Exception e){
		//				e.printStackTrace();
		//			}
		//			curID ++;
		//
		//			NetNodeGroumVisitProcessing.addVarNode(varName,typeName,curMethodInfo, parentNodeStack, 
		//					previousControlFlowNodeStack, previousDataNodeStack, previousDataNodeMap,curID, x, 
		//					curLocalScopeList,
		//					true);
		//		}
		//		else
		//		{
		//			String fieldName = x.getName();
		//			String varName = "this";
		//			String typeVarName = "this";
		//			if (curTypeInfo!=null&&curTypeInfo.typeName!=null)
		//				if (curTypeInfo.typeName.length()>0)
		//				{
		//					typeVarName = curTypeInfo.typeName;
		//					varName = typeVarName;
		//				}
		//
		//			String typeFieldName = "";
		//			try{
		//				if (x.getReferencePrefix()!=null)
		//				{
		//					varName = x.getReferencePrefix().toSource();
		//					typeVarName = sourceInfo.getType(x.getReferencePrefix()).getName();
		//				}
		//			}
		//			catch(Exception e){
		//				e.printStackTrace();
		//			}
		//
		//			if (sourceInfo.getType(x)!=null)
		//			{
		//				typeFieldName = sourceInfo.getType(x).getName();
		//			}
		//
		//			curID ++;
		//
		//			NetNodeGroumVisitProcessing.addNewFieldAccessNode(
		//					varName, typeVarName,
		//					fieldName, typeFieldName, 
		//					curMethodInfo, parentNodeStack, previousControlFlowNodeStack, 
		//					previousDataNodeStack,
		//					previousDataNodeMap,
		//					curID, x,
		//					curClassScopeList
		//					);
		//		}
		String varName =  "<unknownClassType>";
		String typeName =  "<unknownClassType>";
		//		if (curTypeInfo!=null&&curTypeInfo.typeName!=null)
		//			if (curTypeInfo.typeName.length()>0)
		//			{
		//				typeName = curTypeInfo.typeName;
		//				varName = typeName;
		//			}

		String tmp = x.toSource();

		try{
			varName = x.getName();
			typeName = sourceInfo.getType(x).getName();
			if (Configurations.isGetTypeFullName){
				try{

					typeName = sourceInfo.getType(x).getFullName();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}

		}
		catch(Exception e){
			//			e.printStackTrace();
			if (tmp.contains("."))
			{
				typeName = tmp.substring(0,tmp.lastIndexOf(".")); 
			}
			else if (Character.isUpperCase(tmp.charAt(0)))
			{
				typeName = tmp;
			}
		}
		curID ++;

		JavaNodeGroumVisitProcessing.addVarNode(varName,typeName,curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, previousDataNodeStack, previousDataNodeMap,curID, x, 
				curLocalScopeList,
				true);
		
		collectComments(x);
	}

	@Override
	public void visitExtends(Extends x) {
		curID ++;

		if (x.getSupertypes() != null) {

			print("extends");
			printCommaList(0, 0, 1, x.getSupertypes());
		}
		
		collectComments(x);

	}

	@Override
	public void visitImplements(Implements x) {
		curID ++;
		if (x.getSupertypes() != null) {

			print("implements");
			printCommaList(0, 0, 1, x.getSupertypes());
		}
		
		collectComments(x);

	}

	@Override
	public void visitVariableSpecification(VariableSpecification x) {
		curID ++;
		int startTokenIdx = 0;

		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}
		// Logger.logDebug("\t\tVariableSpecification: " + x.toSource() +
		// "\r\n\t\t" + parent.getClass());
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printElement(x.getIdentifier());
		for (int i = 0; i < x.getDimensions(); i += 1) {
			print("[ ]");
		}
		if (x.getInitializer() != null) {
			print(" =");
			printElement(0, 0, 1, x.getInitializer());
		}

		if (isStatement) {
			if (isAddToMethodLexList == 0)
				simpleAddStructure(startTokenIdx,
						StatementLexemePart.VARIABLESPECIFICATION,
						"VARIABLESPECIFICATION");
		}
		
		collectComments(x);
		
		//System.out.println("Variable: " + x.getName());

	}

	@Override
	public void visitBinaryAnd(BinaryAnd x) {
		curID ++;

		printOperator(x, "&");
		
		collectComments(x);

	}

	@Override
	public void visitBinaryAndAssignment(BinaryAndAssignment x) {
		curID ++;
		int startTokenIdx = 0;

		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printOperator(x, "&=");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"ANDASSIGN");
		}
		
		collectComments(x);
	}

	@Override
	public void visitBinaryOrAssignment(BinaryOrAssignment x) {
		curID ++;

		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printOperator(x, "|=");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"ORASSIGN");
		}

		collectComments(x);
	}

	@Override
	public void visitBinaryXOrAssignment(BinaryXOrAssignment x) {
		curID ++;

		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}

		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printOperator(x, "^=");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"XORASSIGN");
		}
		
		collectComments(x);
	}

	@Override
	public void visitCopyAssignment(CopyAssignment x) {
		curID ++;
		//NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		//		printOperator(x, "=");
		List<Expression> children = x.getArguments();
		//TODO: switch node
		printElement(0, children.get(1));
		print("=");
		printElement(children.get(0));

		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"ASSIGN");
		}
		
		collectComments(x);
	}

	@Override
	public void visitDivideAssignment(DivideAssignment x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printOperator(x, "/=");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"DIVASSIGN");
		}
		
		collectComments(x);
	}

	@Override
	public void visitMinusAssignment(MinusAssignment x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printOperator(x, "-=");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"MINUSASSIGN");
		}
		
		collectComments(x);

	}

	@Override
	public void visitModuloAssignment(ModuloAssignment x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printOperator(x, "%=");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"MODASSIGN");
		}
		
		collectComments(x);
	}

	@Override
	public void visitPlusAssignment(PlusAssignment x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printOperator(x, "+=");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"PLUSASSIGN");
		}
		
		collectComments(x);
	}

	@Override
	public void visitPostDecrement(PostDecrement x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = curMethodTokenList.size();
		printOperator(x, "--");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.OPERATOR,
					"POSTDECREMENT");
		}
		
		collectComments(x);

	}

	@Override
	public void visitPostIncrement(PostIncrement x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = curMethodTokenList.size();
		printOperator(x, "++");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.OPERATOR,
					"POSTINCREMENT");
		}
		
		collectComments(x);
	}

	@Override
	public void visitPreDecrement(PreDecrement x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = curMethodTokenList.size();
		printOperator(x, "--");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.OPERATOR,
					"PREDECREMENT");
		}
		
		collectComments(x);
	}

	@Override
	public void visitPreIncrement(PreIncrement x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = curMethodTokenList.size();
		printOperator(x, "++");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.OPERATOR,
					"PREINCREMENT");
		}
		
		collectComments(x);
	}

	@Override
	public void visitShiftLeftAssignment(ShiftLeftAssignment x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printOperator(x, "<<=");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"SLEFTASSIGN");
		}
		
		collectComments(x);
	}

	@Override
	public void visitShiftRightAssignment(ShiftRightAssignment x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printOperator(x, ">>=");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"SRIGHTASSIGN");
		}
		
		collectComments(x);
	}

	@Override
	public void visitTimesAssignment(TimesAssignment x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printOperator(x, "*=");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"TIMESASSIGN");
		}
		
		collectComments(x);
	}

	@Override
	public void visitUnsignedShiftRightAssignment(UnsignedShiftRightAssignment x) {
		curID ++;
		// NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		printOperator(x, ">>>=");
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.ASSIGNMENT,
					"USRIGHTASSIGN");
		}
		
		collectComments(x);
	}

	@Override
	public void visitBinaryNot(BinaryNot x) {
		curID ++;

		printOperator(x, "~");
		
		collectComments(x);

	}

	@Override
	public void visitBinaryOr(BinaryOr x) {
		curID ++;

		printOperator(x, "|");
		
		collectComments(x);

	}

	@Override
	public void visitBinaryXOr(BinaryXOr x) {
		curID ++;

		printOperator(x, "^");
		
		collectComments(x);

	}

	@Override
	public void visitConditional(Conditional x) {
		curID ++;
		curLocalScopeList.add(curID);

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.CONDITIONAL, curLocalScopeList);
		curNode = nodeInfo;

		int startTokenIdx = 0;

		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((parent instanceof StatementBlock)
				&& (x instanceof ExpressionStatement)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}

		boolean addParentheses = x.isToBeParenthesized();
		if (x.getArguments() != null) {
			if (addParentheses) {
				print('(');
			}
			printElement(0, x.getArguments().get(0));
			print(" ?");
			printElement(1, x.getArguments().get(1));
			print(" :");
			printElement(1, x.getArguments().get(2));
			if (addParentheses) {
				print(')');
			}
		}
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.CONDITIONAL,
					"CONDITIONAL");
		}

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		previousControlFlowNodeStack.add(nodeInfo);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitDivide(Divide x) {
		curID ++;

		printOperator(x, "/");
		
		collectComments(x);

	}

	@Override
	public void visitEquals(Equals x) {
		curID ++;

		printOperator(x, "==");
		
		collectComments(x);

	}

	@Override
	public void visitGreaterOrEquals(GreaterOrEquals x) {
		curID ++;

		printOperator(x, ">=");
		
		collectComments(x);

	}

	@Override
	public void visitGreaterThan(GreaterThan x) {
		curID ++;

		printOperator(x, ">");
		
		collectComments(x);

	}

	@Override
	public void visitLessOrEquals(LessOrEquals x) {
		curID ++;

		printOperator(x, "<=");
		
		collectComments(x);

	}

	@Override
	public void visitLessThan(LessThan x) {
		curID ++;

		printOperator(x, "<");
		
		collectComments(x);

	}

	@Override
	public void visitNotEquals(NotEquals x) {
		curID ++;

		printOperator(x, "!=");
		
		collectComments(x);

	}

	@Override
	public void visitNewArray(NewArray x) {
		curID ++;

		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}

		boolean addParentheses = x.isToBeParenthesized();
		if (addParentheses) {
			print('(');
		}

		print("new");
		printElement(1, x.getTypeReference());
		int i = 0;
		if (x.getArguments() != null) {
			for (; i < x.getArguments().size(); i += 1) {
				print('[');
				printElement(x.getArguments().get(i));
				print(']');
			}
		}
		for (; i < x.getDimensions(); i += 1) {
			print("[ ]");
		}
		if (x.getArrayInitializer() != null) {
			printElement(1, x.getArrayInitializer());
		}
		if (addParentheses) {
			print(')');
		}

		if (isStatement) {
			if (isAddToMethodLexList == 0) {
				simpleAddStructure(startTokenIdx, StatementLexemePart.NEW,
						"NEW");
			}
		}

		//
		boolean isInner = false;
		String methodName = x.getTypeReference().getName();
		if (Configurations.isGetTypeFullName){
			//			methodName = x.getTypeReference().getPackageReference().getName() + "." + x.getTypeReference().getName();
			try{
				sourceInfo.getType(x).getFullName();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

		String typeName =methodName + "[]";
		String varName = typeName;

		ArrayList<String> parameterList = new ArrayList<String>();
		if (x.getArguments()!=null)
		{
			for ( i=0; i<x.getArguments().size();i++){
				//				parameterList.add(x.getArguments().get(i).toString());
				String argumentType = "<unknownClassType>";
				try{
					if(sourceInfo.getType(x.getArguments().get(i))!=null)
					{
						argumentType = sourceInfo.getType(x.getArguments().get(i)).getName();
						try{
							argumentType = sourceInfo.getType(x.getArguments().get(i)).getFullName();
						}
						catch(Exception e){
	
						}
					}
				}
				catch(Exception e){
					
				}
				parameterList.add(argumentType);
			}
		}
		MethodInvocInfo methodInvocInfo = new 
				MethodInvocInfo(isInner, methodName, varName, typeName, parameterList, curLocalScopeList);
		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewInvocNode(curTypeInfo, curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, previousDataNodeStack, previousDataNodeMap, curID, methodInvocInfo, x
				, curLocalScopeList, curClassScopeList, varTypeMap);
		JavaNodeGroumVisitProcessing.removeInvocNodeInfo(x, nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		
		collectComments(x);
	}

	@Override
	public void visitInstanceof(Instanceof x) {
		curID ++;

		boolean addParentheses = x.isToBeParenthesized();
		if (addParentheses) {
			print('(');
		}
		if (x.getArguments() != null) {
			printElement(0, x.getArguments().get(0));
		}
		printElementIndentation(1, x);
		print("instanceof");
		if (x.getTypeReference() != null) {
			printElement(1, x.getTypeReference());
		}
		if (addParentheses) {
			print(')');
		}

		boolean isInner = true;
		String methodName = "is@";

		String varName = x.getArguments().get(0).toSource();
		String typeName ="";

		if (sourceInfo.getType(x)!=null)
		{

			typeName = sourceInfo.getType(x).getName();
			if (Configurations.isGetTypeFullName){
				try{
					typeName = sourceInfo.getType(x).getFullName();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}

		ArrayList<String> parameterList = new ArrayList<String>();
		parameterList.add(x.getTypeReference().getName());
		MethodInvocInfo methodInvocInfo = new 
				MethodInvocInfo(isInner, methodName, varName, typeName, parameterList, curLocalScopeList);

		JavaNodeGroumVisitProcessing.addNewInvocNode(curTypeInfo, curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, previousDataNodeStack, previousDataNodeMap, curID, methodInvocInfo, x
				, curLocalScopeList, curClassScopeList, varTypeMap);
		
		collectComments(x);

	}

	@Override
	public void visitNew(New x) {
		NonTerminalProgramElement parent = x.getASTParent();
		//		Logger.log("New: " + x.toSource());
		//		Logger.log("\t" + x.getASTParent().getClass());
		boolean isStatement = false;
		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}
		int startTokenIdx = 0;

		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}

		boolean addParentheses = x.isToBeParenthesized();
		if (addParentheses) {
			print('(');
		}
		if (x.getReferencePrefix() != null) {
			printElement(0, x.getReferencePrefix());
			//			print('.');
			if (curMethodTokenList.size()>0)
				curMethodTokenList.get(curMethodTokenList.size()-1).content = 
				curMethodTokenList.get(curMethodTokenList.size()-1).content.trim() + ".";
		}

		print("new");

		if (x.getConstructorRefTypeArguments() != null
				&& x.getConstructorRefTypeArguments().size() > 0) {
			print('<');
			printCommaList(x.getConstructorRefTypeArguments());
			print('>');
		}

		printElement(1, x.getTypeReference());
		if (x.withDiamondOperator()) {
			print("<>");
		}
		if (getBooleanProperty(GLUE_PARAMETER_LISTS)) {
			print('(');
		} else {
			print(" (");
		}
		if (x.getArguments() != null) {
			printCommaList(x.getArguments());
		}
		print(')');
		if (x.getClassDeclaration() != null) {
			isAddToMethodLexList++;
			isInAMethod++;
			printElement(1, x.getClassDeclaration());
			isInAMethod--;
			isAddToMethodLexList--;

		}
		if (addParentheses) {
			print(')');
		}
		if (x.getStatementContainer() != null) {
			print(';');
		}
		if (isStatement) {
			if (isAddToMethodLexList == 0) {
				simpleAddStructure(startTokenIdx, StatementLexemePart.NEW,
						"NEW");
			}
		}

		//
		boolean isInner = false;
		String methodName = "new" ;//x.getTypeReference().getName();

		String typeName =x.getTypeReference().getName();
		if (Configurations.isGetTypeFullName){
			try{
				typeName = sourceInfo.getType(x).getFullName();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		String varName = typeName;


		ArrayList<String> parameterList = new ArrayList<String>();
		if (x.getArguments()!=null)
		{
			for (int i=0; i<x.getArguments().size();i++){
				//				parameterList.add(x.getArguments().get(i).toString());
				String argumentType = "<unknownClassType>";
				if(sourceInfo.getType(x.getArguments().get(i))!=null)
				{
					try{
						argumentType = sourceInfo.getType(x.getArguments().get(i)).getName();
						try{
							argumentType = sourceInfo.getType(x.getArguments().get(i)).getFullName();
						}
						catch(Exception e){

						}
					}
					catch(Exception e){
						
					}
				}
				parameterList.add(argumentType);
			}
		}
		MethodInvocInfo methodInvocInfo = new 
				MethodInvocInfo(isInner, methodName, varName, typeName, parameterList, curLocalScopeList);
		curID ++;
		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewInvocNode(curTypeInfo, curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, previousDataNodeStack, previousDataNodeMap, curID, methodInvocInfo, x
				, curLocalScopeList, curClassScopeList, varTypeMap);
		JavaNodeGroumVisitProcessing.removeInvocNodeInfo(x, nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		
		collectComments(x);
	}

	@Override
	public void visitTypeCast(TypeCast x) {
		curID ++;

		boolean addParentheses = x.isToBeParenthesized();
		if (addParentheses) {
			print('(');
		}

		print('(');
		if (x.getTypeReference() != null) {
			printElement(0, x.getTypeReference());
		}
		print(')');
		if (x.getArguments() != null) {
			printElement(0, x.getArguments().get(0));
		}
		if (addParentheses) {
			print(')');
		}
		
		collectComments(x);
		
		//System.out.println("visitTypeCast: " + x.getTypeReference().getName());

	}

	@Override
	public void visitLogicalAnd(LogicalAnd x) {
		curID ++;

		printOperator(x, "&&");
		
		collectComments(x);

	}

	@Override
	public void visitLogicalNot(LogicalNot x) {
		curID ++;

		printOperator(x, "!");
		
		collectComments(x);

	}

	@Override
	public void visitLogicalOr(LogicalOr x) {
		curID ++;

		printOperator(x, "||");
		
		collectComments(x);

	}

	@Override
	public void visitMinus(Minus x) {
		curID ++;

		printOperator(x, "-");
		
		collectComments(x);

	}

	@Override
	public void visitModulo(Modulo x) {
		curID ++;

		printOperator(x, "%");
		
		collectComments(x);

	}

	@Override
	public void visitNegative(Negative x) {
		curID ++;

		printOperator(x, "-");
		
		collectComments(x);

	}

	@Override
	public void visitPlus(Plus x) {
		curID ++;
		ArrayList<Plus> plusses = new ArrayList<Plus>();

		while (x.getArguments().get(0) instanceof Plus) {
			plusses.add(x);
			x = (Plus) x.getArguments().get(0);
		}
		for (Plus p : plusses) {
			printHeader(p);
		}
		printOperator(x, "+");
		Collections.reverse(plusses);
		for (Plus p : plusses) {
			int indent = getBooleanProperty(GLUE_INFIX_OPERATORS) ? 0 : 1;
			printElementIndentation(indent, x);
			print("+");
			printElement(indent, p.getArguments().get(1));
			printFooter(p);
		}
		
		collectComments(x);
	}

	@Override
	public void visitPositive(Positive x) {
		curID ++;

		printOperator(x, "+");
		
		collectComments(x);

	}

	@Override
	public void visitShiftLeft(ShiftLeft x) {
		curID ++;

		printOperator(x, "<<");
		
		collectComments(x);

	}

	@Override
	public void visitShiftRight(ShiftRight x) {
		curID ++;

		printOperator(x, ">>");
		
		collectComments(x);

	}

	@Override
	public void visitTimes(Times x) {
		curID ++;

		printOperator(x, "*");
		
		collectComments(x);

	}

	@Override
	public void visitUnsignedShiftRight(UnsignedShiftRight x) {
		curID ++;

		printOperator(x, ">>>");
		
		collectComments(x);

	}

	@Override
	public void visitArrayReference(ArrayReference x) {
		curID ++;
		NonTerminalProgramElement parent = x.getASTParent();
		int startTokenIdx = 0;

		boolean isStatement = false;
		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}

		if (x.getReferencePrefix() != null) {
			printElement(x.getReferencePrefix());
		}
		if (x.getDimensionExpressions() != null) {
			int s = x.getDimensionExpressions().size();
			for (int i = 0; i < s; i += 1) {
				print('[');
				printElement(x.getDimensionExpressions().get(i));
				print(']');
			}
		}
		if (isStatement) {
			simpleAddStructure(startTokenIdx,
					StatementLexemePart.ARRAYREFERENCE, "ARRAYREFERENCE");
		}
		
		collectComments(x);
	}

	@Override
	public void visitFieldReference(FieldReference x) {
		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		int startTokenIdx = 0;

		if (Configurations.isGoMoreInFields)
		{
			if ((parent instanceof StatementBlock)) {
				isStatement = true;
			}
			if (isStatement) {
				startTokenIdx = curMethodTokenList.size();
			}
			if (x.getReferencePrefix() != null) {
				printElement(x.getReferencePrefix());

				//			print('.');
				if (curMethodTokenList.size()>0)
					curMethodTokenList.get(curMethodTokenList.size()-1).content = 
					curMethodTokenList.get(curMethodTokenList.size()-1).content.trim() + ".";
			}

			if (x.getIdentifier() != null) {
				printElement(x.getIdentifier());
			}
			if (isStatement) {
				simpleAddStructure(startTokenIdx,
						StatementLexemePart.FIELDREFERENCE, "FIELDREFERENCE");
			}
		}
		//		NodeGroumVisitProcessing.addNewFieldAccessNode(curMethodInfo, parentNodeStack, previousControlFlowNodeStack, curID, x);
		String fieldName = x.getName();
		String varName = "this";
		String typeVarName = "this";
		if (curTypeInfo!=null&&curTypeInfo.typeName!=null)
			if (curTypeInfo.typeName.length()>0)
			{
				typeVarName = curTypeInfo.typeName;
				varName = typeVarName;
			}

		if (x.getReferencePrefix()!=null)
		{
			varName = x.getReferencePrefix().toSource();
			typeVarName = sourceInfo.getType(x.getReferencePrefix()).getName();
			if (Configurations.isGetTypeFullName){
				try{
					typeVarName = sourceInfo.getType(x.getReferencePrefix()).getFullName();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		String typeFieldName = "";
		if (sourceInfo.getType(x)!=null)
		{
			typeFieldName = sourceInfo.getType(x).getName();
			if (Configurations.isGetTypeFullName){
				try{
					typeFieldName = sourceInfo.getType(x).getFullName();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		curID ++;

		JavaNodeGroumVisitProcessing.addNewFieldAccessNode(
				varName, typeVarName,
				fieldName, typeFieldName, 
				curMethodInfo, parentNodeStack, previousControlFlowNodeStack, 
				previousDataNodeStack,
				previousDataNodeMap,
				curID, x,
				curClassScopeList
				);	
		
		collectComments(x);

	}

	@Override
	public void visitVariableReference(VariableReference x) {
		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		int startTokenIdx = 0;
		;

		if ((parent instanceof StatementBlock)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		if (x.getIdentifier() != null) {
			printElement(x.getIdentifier());
		}
		if (isStatement) {
			simpleAddStructure(startTokenIdx,
					StatementLexemePart.VARIABLEREFERENCE, "VARIABLEREFERENCE");
		}

		//		NodeGroumVisitProcessing.addVarNode(curMethodInfo, parentNodeStack, previousControlFlowNodeStack, curID, x);
		//		String varName = x.getName();
		//
		//		String typeName = "";
		//		if (sourceInfo.getType(x)!=null)
		//		{
		//			typeName = sourceInfo.getType(x).getName();
		//		}

		curID ++;
		//		NodeGroumVisitProcessing.addVarNode(varName,typeName,curMethodInfo, parentNodeStack, 
		//				previousControlFlowNodeStack, previousDataNodeStack, previousDataNodeMap, curID, x, false);
		
		collectComments(x);
		
		//System.out.println("VariableRef: " + x.getName());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * recoder.java.SourceVisitor#visitMetaClassReference(recoder.java.reference
	 * .MetaClassReference)
	 */
	@Override
	public void visitMetaClassReference(MetaClassReference x) {
		curID ++;
		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		int startTokenIdx = 0;
		;

		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		if (x.getTypeReference() != null) {
			printElement(x.getTypeReference());

			//			print('.');
			if (curMethodTokenList.size()>0)
				curMethodTokenList.get(curMethodTokenList.size()-1).content = 
				curMethodTokenList.get(curMethodTokenList.size()-1).content.trim() + ".";
		}
		print("class");
		if (isStatement) {
			simpleAddStructure(startTokenIdx,
					StatementLexemePart.METACLASSREFERENCE,
					"METACLASSREFERENCE");
		}
		
		collectComments(x);
	}

	@Override
	public void visitMethodReference(MethodReference x) {
		//		// NonTerminalProgramElement parent = x.getASTParent();

		//System.out.println("visitMethodReference: " + x.getName());
		int startTokenIdx = 0;

		if (x.getArguments() != null) {
			printCommaList(x.getArguments());
		}


		boolean isStatement = false;
		if ((x.getStatementContainer() instanceof JavaStatement)
				|| (x.getStatementContainer() instanceof Branch)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		if (x.getReferencePrefix() != null) {
			printElement(x.getReferencePrefix());
			// not yet implemented
			//			print('.');
			if (curMethodTokenList.size()>0)
				curMethodTokenList.get(curMethodTokenList.size()-1).content = 
				curMethodTokenList.get(curMethodTokenList.size()-1).content.trim() + ".";
		}
		if (x.getTypeArguments() != null && x.getTypeArguments().size() > 0) {
			// a prefix must be present to allow type arguments. Why is not
			// clear,
			// so we leave this here in case it'll change sometime
			print('<');
			printCommaList(x.getTypeArguments());
			print('>');
		}

		//
		boolean isInner = false;
		String methodName = x.getName();

		String varName = "<unknownClassType>";
		String typeName ="<unknownClassType>";
		//		if (!(x.toSource().substring(0,x.toSource().indexOf("(")).contains(".")))
		//		{
		//		if (curTypeInfo!=null&&curTypeInfo.typeName!=null)
		//			if (curTypeInfo.typeName.length()>0)
		//			{
		//				typeName = curTypeInfo.typeName;
		//				varName = typeName;
		//			}
		//		}

		try{
			String tmp = sourceInfo.getMethod(x).getFullName();

			if (tmp!=null)
			{
				typeName = tmp;
				if (typeName.contains(".")){
					typeName = tmp.substring(0, tmp.lastIndexOf("."));
				}
				if (!Configurations.isGetTypeFullName){
					try{
						typeName = typeName.substring(typeName.lastIndexOf(".")+1);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			}

		}
		catch(Exception e){
		}
		
		if (x.getReferencePrefix()!=null)
		{			
			String tmp_typename = "<unknownClassType>";
				Type type =sourceInfo.getType(x.getReferencePrefix());
				if (type!=null)
				{
					tmp_typename = type.getName();
					if (Configurations.isGetTypeFullName)
					{
						try
						{
							tmp_typename = type.getFullName();
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			
				if(!tmp_typename.startsWith("<unknown"))
				{
					typeName = tmp_typename;
				}
			varName = x.getReferencePrefix().toSource().trim();
			
		}
		else 
		{
			if(typeName.startsWith("<unknown")&&(curTypeInfo!=null))
			{
				typeName = curTypeInfo.typeName;
			}
		}
		
		//identifierList.add(typeName + "." + methodName);
		API.add(typeName + "." + methodName);
		
		ArrayList<String> parameterList = new ArrayList<String>();
		if (x.getArguments()!=null)
		{
			for (int i=0; i<x.getArguments().size();i++){
				//				parameterList.add(x.getArguments().get(i).toString());
				String argumentType = "<unknownClassType>";
				try{
					if(sourceInfo.getType(x.getArguments().get(i))!=null)
					{
						argumentType = sourceInfo.getType(x.getArguments().get(i)).getName();
						try{
							argumentType = sourceInfo.getType(x.getArguments().get(i)).getFullName();
						}
						catch(Exception e){

						}
					}
				}
				catch(Exception e){

				}
				parameterList.add(argumentType);
			}
		}
		MethodInvocInfo methodInvocInfo = new 
				MethodInvocInfo(isInner, methodName, varName, typeName, parameterList, curLocalScopeList);
		curID ++;
		Stack<NodeInfo> tmp = new Stack<NodeInfo>();
		tmp.addAll(previousControlFlowNodeStack);
		JavaNodeGroumVisitProcessing.addNewInvocNode(curTypeInfo, curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, previousDataNodeStack, previousDataNodeMap, curID, methodInvocInfo, x
				, curLocalScopeList, curClassScopeList, varTypeMap);


		if (x.getIdentifier() != null) {
			printElement(x.getIdentifier());
		}

		if (getBooleanProperty(GLUE_PARAMETER_LISTS)) {
			print('(');
		} else {
			print(" (");
		}

		print(')');

		if (x.getStatementContainer() != null) {
			print(';');
		}
		if (isStatement) {
			simpleAddStructure(startTokenIdx,
					StatementLexemePart.METHODREFERENCE, "METHODREFERENCE");
		}
		//		NodeGroumVisitProcessing.removeInvocNodeInfo(x, nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		//		previousControlFlowNodeStack = tmp;
		
		collectComments(x);
		
		

	}

	@Override
	public void visitSuperConstructorReference(SuperConstructorReference x) {

		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		int startTokenIdx = 0;

		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		if (x.getTypeArguments() != null && x.getTypeArguments().size() > 0) {
			print('<');
			printCommaList(x.getTypeArguments());
			print('>');
		}

		if (x.getReferencePrefix() != null) {
			printElement(x.getReferencePrefix());
			//			print('.');
			if (curMethodTokenList.size()>0)
				curMethodTokenList.get(curMethodTokenList.size()-1).content = 
				curMethodTokenList.get(curMethodTokenList.size()-1).content.trim() + ".";
		}

		if (getBooleanProperty(GLUE_PARAMETER_LISTS)) {
			print("super(");
		} else {
			print("super (");
		}
		if (x.getArguments() != null) {
			printCommaList(x.getArguments());
		}
		print(");");
		if (isStatement) {
			simpleAddStructure(startTokenIdx,
					StatementLexemePart.SUPERCONSTRUCTORREFERENCE,
					"SUPERCONSTRUCTORREFERENCE");
		}

		//
		boolean isInner = false;
		String methodName = "super";

		String varName = "super";
		String typeName ="super";
		//		if (sourceInfo.getType(x)!=null)
		//		{
		//			typeName = sourceInfo.getType(x).getName();
		//			if (Configurations.isGetTypeFullName){
		//				try{
		//				typeName = sourceInfo.getType(x).getFullName();
		//				}
		//				catch(Exception e){
		//					e.printStackTrace();
		//				}
		//			}
		//			varName = typeName;
		//		}

		if (typeName=="super"){
			typeName = curTypeInfo.typeName;
		}

		try{
			String tmp = sourceInfo.getConstructor(x).getFullName();

			if (tmp!=null)
			{
				typeName = tmp;
				if (typeName.contains(".")){
					typeName = tmp.substring(0, tmp.lastIndexOf("."));
				}
				if (!Configurations.isGetTypeFullName){
					try{
						typeName = typeName.substring(typeName.lastIndexOf(".")+1);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
		catch(Exception e){
		}

		//		if (x.getReferencePrefix()!=null)
		//		{
		//			Type type =sourceInfo.getType(x.getReferencePrefix());
		//			if (type!=null)
		//			{
		//				typeName = type.getName();
		//				if (Configurations.isGetTypeFullName){
		//					try{
		//						typeName = type.getFullName();
		//					}
		//					catch(Exception e){
		//						e.printStackTrace();
		//					}
		//				}
		//			}
		//		}

		ArrayList<String> parameterList = new ArrayList<String>();
		try{
			if (x.getArguments()!=null)
			{
				for (int i=0; i<x.getArguments().size();i++){
					//				parameterList.add(x.getArguments().get(i).toString());
					String argumentType = "<unknownClassType>";
					if(sourceInfo.getType(x.getArguments().get(i))!=null)
					{
						argumentType = sourceInfo.getType(x.getArguments().get(i)).getName();
						try{
							argumentType = sourceInfo.getType(x.getArguments().get(i)).getFullName();
						}
						catch(Exception e){
	
						}
					}
					parameterList.add(argumentType);
				}
			}
		}
		catch(Exception e){
		
		}
		MethodInvocInfo methodInvocInfo = new 
				MethodInvocInfo(isInner, methodName, varName, typeName, parameterList, curLocalScopeList);
		curID ++;
		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewInvocNode(curTypeInfo, curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, previousDataNodeStack, previousDataNodeMap, curID, 
				methodInvocInfo, x, curLocalScopeList, curClassScopeList, varTypeMap);
		JavaNodeGroumVisitProcessing.removeInvocNodeInfo(x, nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		
		collectComments(x);
	}

	@Override
	public void visitThisConstructorReference(ThisConstructorReference x) {
		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		int startTokenIdx = 0;

		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}

		if (x.getTypeArguments() != null && x.getTypeArguments().size() > 0) {
			print('<');
			printCommaList(x.getTypeArguments());
			print('>');
		}

		print(getBooleanProperty(GLUE_PARAMETER_LISTS) ? "this(" : "this (");
		if (x.getArguments() != null) {
			printCommaList(x.getArguments());
		}
		print(");");
		if (isStatement) {
			simpleAddStructure(startTokenIdx,
					StatementLexemePart.THISCONSTRUCTORREFERENCE,
					"THISCONSTRUCTORREFERENCE");
		}

		//
		boolean isInner = true;
		String methodName = "this";

		String varName = "this";
		String typeName =curTypeInfo.typeName;

		Type type =sourceInfo.getConstructor(x).getContainingClassType();
		if (type!=null)
		{
			typeName = type.getName();
			if (Configurations.isGetTypeFullName){
				try{
					typeName = type.getFullName();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			varName = typeName;
		}


		ArrayList<String> parameterList = new ArrayList<String>();
		if (x.getArguments()!=null)
		{
			for (int i=0; i<x.getArguments().size();i++){
				//				parameterList.add(x.getArguments().get(i).toString());
				String argumentType = "<unknownClassType>";
				if(sourceInfo.getType(x.getArguments().get(i))!=null)
				{
					argumentType = sourceInfo.getType(x.getArguments().get(i)).getName();
					try{
						argumentType = sourceInfo.getType(x.getArguments().get(i)).getFullName();
					}
					catch(Exception e){

					}
				}
				parameterList.add(argumentType);
			}
		}
		MethodInvocInfo methodInvocInfo = new 
				MethodInvocInfo(isInner, methodName, varName, typeName, parameterList, curLocalScopeList);
		curID ++;

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewInvocNode(curTypeInfo, curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, previousDataNodeStack, previousDataNodeMap, curID, 
				methodInvocInfo, x, curLocalScopeList, curClassScopeList, varTypeMap);
		JavaNodeGroumVisitProcessing.removeInvocNodeInfo(x, nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		
		collectComments(x);
	}

	@Override
	public void visitSuperReference(SuperReference x) {
		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		int startTokenIdx = 0;

		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		if (x.getReferencePrefix() != null) {
			printElement(x.getReferencePrefix());

			print(".super");
		} else {

			print("super");
		}
		if (isStatement) {
			simpleAddStructure(startTokenIdx,
					StatementLexemePart.SUPERREFERENCE, "SUPERREFERENCE");
		}
		
		collectComments(x);
	}

	@Override
	public void visitThisReference(ThisReference x) {
		curID ++;
		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		int startTokenIdx = 0;

		if ((parent instanceof StatementBlock) || (parent instanceof Branch)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}
		if (x.getReferencePrefix() != null) {
			printElement(x.getReferencePrefix());

			print(".this");
		} else {

			print("this");
		}
		if (isStatement) {
			simpleAddStructure(startTokenIdx,
					StatementLexemePart.THISREFERENCE, "THISREFERENCE");
		}
		
		collectComments(x);
	}

	@Override
	public void visitThen(Then x) {
		curID ++;

		//		Stack<NodeInfo> tmp = new Stack<NodeInfo>();
		//		tmp.addAll(previousControlFlowNodeStack);
		//		
		//		NodeInfo nodeInfo = NodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
		//				previousControlFlowNodeStack, curID, ControlInfo.THEN);
		//		curNode = nodeInfo;

		if (x.getBody() != null) {
			printElement(x.getBody());
		}

		//		NodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		//		previousControlFlowNodeStack = tmp;
		
		collectComments(x);

	}

	@Override
	public void visitElse(Else x) {
		curID ++;
		curLocalScopeList.add(curID);

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.ELSE, curLocalScopeList);
		curNode = nodeInfo;

		int startTokenIdx = curMethodTokenList.size();
		print("else");
		simpleAddStructure(startTokenIdx, StatementLexemePart.ELSE, "ELSE");

		if (x.getBody() != null) {
			if (getBooleanProperty(GLUE_STATEMENT_BLOCKS)) {
				printElement(1, x.getBody());
			} else {
				if (x.getBody() instanceof StatementBlock) {
					printElement(1, 0, x.getBody());
				} else {
					printElement(1, +1, 0, x.getBody());
					changeLevel(-1);
				}
			}
		}
		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitCase(Case x) {
		curID ++;
		curLocalScopeList.add(curID);

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.CASE, curLocalScopeList);
		curNode = nodeInfo;

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();

		int startTokenIdx = 0;
		startTokenIdx = curMethodTokenList.size();
		print("case");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.CASE,
				"CASE", statementLexemeParts);

		if (x.getExpression() != null) {
			startTokenIdx = curMethodTokenList.size();
			printElement(1, x.getExpression());
			simpleAddStructureToList(startTokenIdx,
					StatementLexemePart.EXPRESSION, "EXPRESSION",
					statementLexemeParts);
		}

		startTokenIdx = curMethodTokenList.size();
		print(':');
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				":", statementLexemeParts);
		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		if (x.getBody() != null && x.getBody().size() > 0) {
			printLineList(1, +1, x.getBody());
			changeLevel(-1);
		}
		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitCatch(Catch x) {
		curID ++;
		curLocalScopeList.add(curID);

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.CATCH, curLocalScopeList);
		curNode = nodeInfo;

		List<StatementLexemePart> statementLexemeParts = new ArrayList<StatementLexemePart>();
		int startTokenIdx = curMethodTokenList.size();
		// if (getBooleanProperty(GLUE_CONTROL_EXPRESSIONS)) {
		//
		// print("catch(");
		// } else {
		//
		// print("catch (");
		// }
		print("catch");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.CATCH,
				"CATCH", statementLexemeParts);

		startTokenIdx = curMethodTokenList.size();
		print("(");
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				"(", statementLexemeParts);

		if (x.getParameterDeclaration() != null) {
			startTokenIdx = curMethodTokenList.size();
			printElement(x.getParameterDeclaration());
			simpleAddStructureToList(startTokenIdx, StatementLexemePart.PARAM,
					"PARAM", statementLexemeParts);
		}
		startTokenIdx = curMethodTokenList.size();
		print(')');
		simpleAddStructureToList(startTokenIdx, StatementLexemePart.STOKEN,
				")", statementLexemeParts);
		StatementLexeme statement = new StatementLexeme(statementLexemeParts);
		if (isAddToMethodLexList == 0)
			methodStatementLexList.add(statement);

		if (x.getBody() != null) {
			printElement(1, x.getBody());
		}
		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitDefault(Default x) {
		curID ++;
		curLocalScopeList.add(curID);

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.DEFAULT, curLocalScopeList);
		curNode = nodeInfo;

		int startTokenIdx = curMethodTokenList.size();
		print("default:");
		simpleAddStructure(startTokenIdx, StatementLexemePart.DEFAULT,
				"DEFAULT");

		if (x.getBody() != null && x.getBody().size() > 0) {
			printLineList(1, +1, x.getBody());
			changeLevel(-1);
		}

		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitFinally(Finally x) {
		curID ++;
		curLocalScopeList.add(curID);

		NodeInfo nodeInfo = JavaNodeGroumVisitProcessing.addNewControlNode(curMethodInfo, parentNodeStack, 
				previousControlFlowNodeStack, curID, ControlInfo.FINALLY, curLocalScopeList);
		curNode = nodeInfo;

		int startTokenIdx = curMethodTokenList.size();
		print("finally");
		simpleAddStructure(startTokenIdx, StatementLexemePart.FINALLY,
				"FINALLY");

		if (x.getBody() != null) {
			printElement(1, x.getBody());
		}
		JavaNodeGroumVisitProcessing.removeControlNodeInfo(nodeInfo, parentNodeStack, previousControlFlowNodeStack);
		curLocalScopeList.remove(curLocalScopeList.size()-1);
		
		collectComments(x);

	}

	@Override
	public void visitAbstract(Abstract x) {
		curID ++;
		print("abstract");
		
		collectComments(x);
	}

	@Override
	public void visitFinal(Final x) {
		curID ++;
		print("final");
		
		collectComments(x);
	}

	@Override
	public void visitNative(Native x) {
		curID ++;

		print("native");
		
		collectComments(x);

	}

	@Override
	public void visitPrivate(Private x) {
		curID ++;

		print("private");
		
		collectComments(x);

	}

	@Override
	public void visitProtected(Protected x) {
		curID ++;

		print("protected");
		
		collectComments(x);

	}

	@Override
	public void visitPublic(Public x) {
		curID ++;

		print("public");
		
		collectComments(x);

	}

	@Override
	public void visitStatic(Static x) {
		curID ++;

		print("static");
		
		collectComments(x);

	}

	@Override
	public void visitStrictFp(StrictFp x) {
		curID ++;

		print("strictfp");
		
		collectComments(x);

	}

	@Override
	public void visitSynchronized(Synchronized x) {
		curID ++;

		print("synchronized");
		collectComments(x);

	}

	@Override
	public void visitTransient(Transient x) {
		curID ++;

		print("transient");
		collectComments(x);

	}

	@Override
	public void visitVolatile(Volatile x) {
		curID ++;

		print("volatile");
		collectComments(x);

	}

	@Override
	public void visitAnnotationUse(AnnotationUseSpecification a) {
		// TODO better indentation handling
		// int startTokenIdx = curMethodTokenList.size();
		curID ++;

		printHeader(a);
		printElementIndentation(a);
		print('@');
		printElement(a.getTypeReference());
		List<AnnotationElementValuePair> evp = a.getElementValuePairs();
		if (evp != null) {
			print('(');
			printCommaList(0, 0, 0, evp);
			print(')');
		}
		printFooter(a);
		// simpleAddStructure(startTokenIdx, StatementLexemePart.ANNOTATION,
		// "ANNOTATION");
		collectComments(a);

	}

	@Override
	public void visitElementValuePair(AnnotationElementValuePair x) {
		// TODO better indentation handling
		curID ++;

		AnnotationPropertyReference id = x.getElement();
		if (id != null) {
			printElement(id);
			print(" =");
		}
		ProgramElement ev = x.getElementValue();
		if (ev != null) {
			printElement(ev);
		}
		
		collectComments(x);

	}

	@Override
	public void visitAnnotationPropertyReference(AnnotationPropertyReference x) {
		curID ++;

		Identifier id = x.getIdentifier();
		if (id != null) {
			printElement(id);
		}
		
		collectComments(x);

	}

	@Override
	public void visitEmptyStatement(EmptyStatement x) {
		curID ++;

		int startTokenIdx = curMethodTokenList.size();
		print(';');
		simpleAddStructure(startTokenIdx, StatementLexemePart.EMPTYSTATEMENT,
				"EMPTYSTATEMENT");
		
		collectComments(x);

	}

	@Override
	public void visitComment(Comment x) 
	{
		//
		// print(x.getText());
		// if (x instanceof SingleLineComment) {
		// hasJustPrintedComment = true;
		// } else if (!x.getText().endsWith("\n") &&
		// x.getText().contains("\n")){
		// hasJustPrintedComment = true;
		// }
		// if (overwriteParsePositions) {
		// overwritePosition.setPosition(line, Math.max(0, column - 1));
		// x.getLastElement().setEndPosition(overwritePosition);
		// }
		//x.accept(this);
		//System.out.println(fileInfo.filePath + " Comment: " + x.getText());
		
		commentList.add(x.getText());
	}

	@Override
	public void visitParenthesizedExpression(ParenthesizedExpression x) {
		curID ++;
		NonTerminalProgramElement parent = x.getASTParent();
		boolean isStatement = false;
		int startTokenIdx = 0;

		if ((parent instanceof StatementBlock)
				&& (x instanceof ExpressionStatement)) {
			isStatement = true;
		}
		if (isStatement) {
			startTokenIdx = curMethodTokenList.size();
		}

		print('(');
		if (x.getArguments() != null) {
			printElement(x.getArguments().get(0));
		}
		print(')');
		if (isStatement) {
			simpleAddStructure(startTokenIdx, StatementLexemePart.EXPRESSION,
					"EXPRESSION");
		}
		
		collectComments(x);
	}

	@Override
	public void visitEnumConstructorReference(EnumConstructorReference x) {
		curID ++;

		int startTokenIdx = curMethodTokenList.size();

		List<? extends Expression> exprs = x.getArguments();
		if (exprs != null) {
			print('(');
			printCommaList(exprs);
			print(')');
		}
		if (x.getClassDeclaration() != null) {
			printElement(x.getClassDeclaration());
		}

		simpleAddStructure(startTokenIdx,
				StatementLexemePart.ENUMCONSTRUCTORREFERENCE,
				"ENUMCONSTRUCTORREFERENCE");
		
		collectComments(x);

	}

	@Override
	public void visitEnumConstantDeclaration(EnumConstantDeclaration x) {
		curID ++;

		int startTokenIdx = curMethodTokenList.size();

		if (x.getAnnotations() != null && x.getAnnotations().size() != 0) {
			printKeywordList(x.getAnnotations());
			print(' ');
		}
		printElement(1, x.getEnumConstantSpecification());

		simpleAddStructure(startTokenIdx, StatementLexemePart.ENUMCONSTDEC,
				"ENUMCONSTDEC");
		
		collectComments(x);

	}

	@Override
	public void visitEnumConstantSpecification(EnumConstantSpecification x) {
		curID ++;

		printElement(x.getIdentifier());
		printElement(x.getConstructorReference());
		
		collectComments(x);

	}

	@Override
	public void visitEnumDeclaration(EnumDeclaration x) {
		// int startTokenIdx = curMethodTokenList.size();
		curID ++;
		storeFields(true);
		curLocalScopeList.add(curID);
		curClassScopeList.add(curID);

		TypeInfo typeInfo = new TypeInfo();
		typeInfo.typeName = x.getName();
		if (Configurations.isGetTypeFullName){
			try{
				typeInfo.typeName = x.getFullName();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		typeInfo.packageDec = fileInfo.packageDec;
		fileInfo.typeInfoList.add(typeInfo);
		typeInfo.fileInfo = fileInfo;
		curTypeInfo = typeInfo;

		int m = 0;
		if (x.getDeclarationSpecifiers() != null) {
			m = x.getDeclarationSpecifiers().size();
		}
		if (m > 0) {
			printKeywordList(x.getDeclarationSpecifiers());
			m = 1;
		}
		// unlike class declarations, enum declarations always require an
		// identifier
		printElementIndentation(m, x);
		print("enum");
		printElement(1, x.getIdentifier());
		if (x.getImplementedTypes() != null) {
			printElement(1, x.getImplementedTypes());
		}
		print(' ');
		print('{');
		printContainerComments(x);
		// if (x.getMembers() != null && !x.getMembers().isEmpty()) {
		// //printBlockList(2, 1, x.getMembers());
		// printCommaList(2, 1, 1, x.getMembers())
		// changeLevel(-1);
		// }
		printCommaList(2, 1, 1, x.getConstants());
		print(";");
		changeLevel(-1);

		printBlockList(2, 1, x.getNonConstantMembers());
		changeLevel(-1);

		printIndentation(1, getTotalIndentation());
		print('}');

		// simpleAddStructure(startTokenIdx,
		// StatementLexemePart.ENUMDECLARATION, "ENUMDECLARATION");

		curLocalScopeList.remove(curLocalScopeList.size()-1);
		curClassScopeList.remove(curClassScopeList.size()-1);

		restoreFields(true);
		
		collectComments(x);
	}

	@Override
	public void visitTypeArgument(TypeArgumentDeclaration x) {
		curID ++;

		switch (x.getWildcardMode()) {
		case None:
			break;
		case Any:
			print("?");
			break;
		case Extends:
			print("? extends ");
			break;
		case Super:
			print("? super ");
			break;
		}
		if (x.getTypeReferenceCount() == 1)
			printElement(x.getTypeReferenceAt(0));
		
		collectComments(x);
		
		//System.out.println("visitTypeArgument: " + x.getTypeName());

	}

	@Override
	public void visitTypeParameter(TypeParameterDeclaration x) {
		curID ++;

		if (x.getIdentifier() != null)
			printElement(x.getIdentifier());
		if (x.getBounds() != null && x.getBounds().size() != 0) {
			print(" extends ");
			printProgramElementList(0, 0, 0, "&", 0, 1, x.getBounds());
		}
		
		collectComments(x);
		//System.out.println("visitTypeParameter: " + x.getFullSignature());

	}

	@Override
	public void visitParameterDeclaration(ParameterDeclaration x) {
		curID ++;
		visitVariableDeclaration(x, x.isVarArg());
		
		collectComments(x);
	}



	@Override
	public void visitConstructorDeclaration(ConstructorDeclaration x) {
		// TODO Auto-generated method stub
		super.visitConstructorDeclaration(x);
		
		collectComments(x);
	}



	@Override
	public void visitAnnotationPropertyDeclaration(
			AnnotationPropertyDeclaration x) {
		// TODO Auto-generated method stub
		super.visitAnnotationPropertyDeclaration(x);
		
		collectComments(x);
	}



	@Override
	public void visitFieldSpecification(FieldSpecification x) {
		// TODO Auto-generated method stub
		super.visitFieldSpecification(x);
		
		collectComments(x);
	}



	@Override
	public void visitDeclarationSpecifier(DeclarationSpecifier x) {
		// TODO Auto-generated method stub
		super.visitDeclarationSpecifier(x);
		
		
		collectComments(x);
	}



	@Override
	protected void visitModifier(Modifier x) {
		// TODO Auto-generated method stub
		super.visitModifier(x);
		
		collectComments(x);
	}



	@Override
	protected void visitLiteral(Literal x) {
		// TODO Auto-generated method stub
		super.visitLiteral(x);
		
		collectComments(x);
	}



	@Override
	protected void visitOperator(Operator x) {
		// TODO Auto-generated method stub
		super.visitOperator(x);
		
		collectComments(x);
	}



	@Override
	public void visitSingleLineComment(SingleLineComment x)
	{
		// TODO Auto-generated method stub
		commentList.add (x.getText());
		super.visitSingleLineComment(x);
	}



	@Override
	public void visitDocComment(DocComment x) {
		// TODO Auto-generated method stub
		commentList.add (x.getText());
		super.visitDocComment(x);
	}





}
