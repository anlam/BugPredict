package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

/**
 * Internal AST visitor for serializing an AST in a quick and dirty fashion.
 * For various reasons the resulting string is not necessarily legal
 * Java code; and even if it is legal Java code, it is not necessarily the string
 * that corresponds to the given AST. Although useless for most purposes, it's
 * fine for generating debug print strings.
 * <p>
 * Example usage:
 * <code>
 * <pre>
 *    NaiveASTFlattener p = new NaiveASTFlattener();
 *    node.accept(p);
 *    String result = p.getResult();
 * </pre>
 * </code>
 * Call the <code>reset</code> method to clear the previous result before reusing an
 * existing instance.
 * </p>
 *
 * @since 2.0
 */
public class ASTFlattener extends ASTVisitor {
	public static final String PROPERTY_SRC = "src";
	public static final String PROPERTY_HEIGHT = "height";
	public static final String PREFIX_LOCAL_VARIABLE = "lv|";
	public static final String PREFIX_LOCAL_VARIABLE_NAME = "_lv_";
	public static final String PREFIX_LITERAL = "lit|";
	
	/**
	 * Internal synonym for {@link AST#JLS2}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private static final int JLS2 = AST.JLS2;


	/**
	 * The string buffer into which the serialized representation of the AST is
	 * written.
	 */
	protected ArrayList<String> tokens;

	//private int indent = 0;
	private Stack<Byte> heights = new Stack<Byte>();

	/**
	 * Creates a new AST printer.
	 */
	public ASTFlattener() {
		this.tokens = new ArrayList<String>();
	}

	/**
	 * Internal synonym for {@link ClassInstanceCreation#getName()}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private Name getName(ClassInstanceCreation node) {
		return node.getName();
	}

	/**
	 * Returns the string accumulated in the visit.
	 *
	 * @return the serialized
	 */
	public String getResult() {
		return this.tokens.toString();
	}

	/**
	 * Internal synonym for {@link MethodDeclaration#getReturnType()}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private Type getReturnType(MethodDeclaration node) {
		return node.getReturnType();
	}

	/**
	 * Internal synonym for {@link TypeDeclaration#getSuperclass()}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private Name getSuperclass(TypeDeclaration node) {
		return node.getSuperclass();
	}

	/**
	 * Internal synonym for {@link TypeDeclarationStatement#getTypeDeclaration()}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private TypeDeclaration getTypeDeclaration(TypeDeclarationStatement node) {
		return node.getTypeDeclaration();
	}

	/*void printIndent() {
		for (int i = 0; i < this.indent; i++)
			this.tokens.append("  "); //$NON-NLS-1$
	}*/

	/**
	 * Appends the text representation of the given modifier flags, followed by a single space.
	 * Used for JLS2 modifiers.
	 *
	 * @param modifiers the modifier flags
	 */
	void printModifiers(int modifiers) {
		if (Modifier.isPublic(modifiers)) {
			this.tokens.add("public");//$NON-NLS-1$
		}
		if (Modifier.isProtected(modifiers)) {
			this.tokens.add("protected");//$NON-NLS-1$
		}
		if (Modifier.isPrivate(modifiers)) {
			this.tokens.add("private");//$NON-NLS-1$
		}
		if (Modifier.isStatic(modifiers)) {
			this.tokens.add("static");//$NON-NLS-1$
		}
		if (Modifier.isAbstract(modifiers)) {
			this.tokens.add("abstract");//$NON-NLS-1$
		}
		if (Modifier.isFinal(modifiers)) {
			this.tokens.add("final");//$NON-NLS-1$
		}
		if (Modifier.isSynchronized(modifiers)) {
			this.tokens.add("synchronized");//$NON-NLS-1$
		}
		if (Modifier.isVolatile(modifiers)) {
			this.tokens.add("volatile");//$NON-NLS-1$
		}
		if (Modifier.isNative(modifiers)) {
			this.tokens.add("native");//$NON-NLS-1$
		}
		if (Modifier.isStrictfp(modifiers)) {
			this.tokens.add("strictfp");//$NON-NLS-1$
		}
		if (Modifier.isTransient(modifiers)) {
			this.tokens.add("transient");//$NON-NLS-1$
		}
	}

	/**
	 * Appends the text representation of the given modifier flags, followed by a single space.
	 * Used for 3.0 modifiers and annotations.
	 *
	 * @param ext the list of modifier and annotation nodes
	 * (element type: <code>IExtendedModifiers</code>)
	 */
	void printModifiers(List ext) {
		for (Iterator it = ext.iterator(); it.hasNext(); ) {
			ASTNode p = (ASTNode) it.next();
			p.accept(this);
		}
	}

	/**
	 * Resets this printer so that it can be used again.
	 */
	public void reset() {
		this.tokens.clear();
	}

	/**
	 * Internal synonym for {@link TypeDeclaration#superInterfaces()}. Use to alleviate
	 * deprecation warnings.
	 * @deprecated
	 * @since 3.4
	 */
	private List superInterfaces(TypeDeclaration node) {
		return node.superInterfaces();
	}

	private boolean isLocalVariableName(SimpleName node) {
		return node.getProperty(LocalVariableNameMarker.PROPERTY_LOCAL_VARIABLE) != null;
	}
	
	@Override
	public void preVisit(ASTNode node) {
		node.setProperty(PROPERTY_SRC, tokens.size());
		this.heights.push((byte) 1);
	}
	
	@Override
	public void postVisit(ASTNode node) {
		int size = (Integer) node.getProperty(PROPERTY_SRC);
		ArrayList<String> src = new ArrayList<String>();
		HashMap<String, Integer> indexes = new HashMap<String, Integer>();
		for (int i = size; i < this.tokens.size(); i++) {
			String token = this.tokens.get(i);
			if (token.startsWith(PREFIX_LOCAL_VARIABLE)) {
				String name = token.substring(PREFIX_LOCAL_VARIABLE.length());
				int index = indexes.size();
				if (indexes.containsKey(name))
					index = indexes.get(name);
				else
					indexes.put(name, index);
				token = PREFIX_LOCAL_VARIABLE_NAME + index;
			}
			src.add(token);
		}
		node.setProperty(PROPERTY_SRC, src);
		byte h = this.heights.pop();
		node.setProperty(PROPERTY_HEIGHT, h);
		if (!this.heights.isEmpty()) {
			byte ph = this.heights.pop();
			if (h+1 > ph)
				ph = (byte) (h + 1);
			this.heights.push(ph);
		}
	}
	
	@Override
	public void endVisit(SimpleType node) {
		this.heights.pop();
		this.heights.push((byte) 1);
	}

	/*
	 * @see ASTVisitor#visit(AnnotationTypeDeclaration)
	 * @since 3.1
	 */
	public boolean visit(AnnotationTypeDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printModifiers(node.modifiers());
		this.tokens.add("@interface");//$NON-NLS-1$
		node.getName().accept(this);
		this.tokens.add("{");//$NON-NLS-1$
		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			d.accept(this);
		}
		this.tokens.add("}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AnnotationTypeMemberDeclaration)
	 * @since 3.1
	 */
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printModifiers(node.modifiers());
		node.getType().accept(this);
		node.getName().accept(this);
		if (node.getDefault() != null) {
			this.tokens.add("default");//$NON-NLS-1$
			node.getDefault().accept(this);
		}
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AnonymousClassDeclaration)
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		this.tokens.add("{");//$NON-NLS-1$
		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
			BodyDeclaration b = (BodyDeclaration) it.next();
			b.accept(this);
		}
		this.tokens.add("}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayAccess)
	 */
	public boolean visit(ArrayAccess node) {
		node.getArray().accept(this);
		this.tokens.add("[");//$NON-NLS-1$
		node.getIndex().accept(this);
		this.tokens.add("]");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayCreation)
	 */
	public boolean visit(ArrayCreation node) {
		this.tokens.add("new");//$NON-NLS-1$
		ArrayType at = node.getType();
		int dims = at.getDimensions();
		Type elementType = at.getElementType();
		elementType.accept(this);
		for (Iterator it = node.dimensions().iterator(); it.hasNext(); ) {
			this.tokens.add("[");//$NON-NLS-1$
			Expression e = (Expression) it.next();
			e.accept(this);
			this.tokens.add("]");//$NON-NLS-1$
			dims--;
		}
		// add empty "[]" for each extra array dimension
		for (int i= 0; i < dims; i++) {
			this.tokens.add("[]");//$NON-NLS-1$
		}
		if (node.getInitializer() != null) {
			node.getInitializer().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayInitializer)
	 */
	public boolean visit(ArrayInitializer node) {
		this.tokens.add("{");//$NON-NLS-1$
		for (Iterator it = node.expressions().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add("}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayType)
	 */
	public boolean visit(ArrayType node) {
		node.getComponentType().accept(this);
		this.tokens.add("[]");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AssertStatement)
	 */
	public boolean visit(AssertStatement node) {
		this.tokens.add("assert");//$NON-NLS-1$
		node.getExpression().accept(this);
		if (node.getMessage() != null) {
			this.tokens.add(":");//$NON-NLS-1$
			node.getMessage().accept(this);
		}
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Assignment)
	 */
	public boolean visit(Assignment node) {
		node.getLeftHandSide().accept(this);
		this.tokens.add(node.getOperator().toString());
		node.getRightHandSide().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Block)
	 */
	public boolean visit(Block node) {
		this.tokens.add("{");//$NON-NLS-1$
		for (Iterator it = node.statements().iterator(); it.hasNext(); ) {
			Statement s = (Statement) it.next();
			s.accept(this);
		}
		this.tokens.add("}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BlockComment)
	 * @since 3.0
	 */
	public boolean visit(BlockComment node) {
		//this.tokens.add("/* */");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BooleanLiteral)
	 */
	public boolean visit(BooleanLiteral node) {
		//this.tokens.add("true");
		this.tokens.add(PREFIX_LITERAL + "true|" + node.booleanValue());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BreakStatement)
	 */
	public boolean visit(BreakStatement node) {
		this.tokens.add("break");//$NON-NLS-1$
		if (node.getLabel() != null) {
			node.getLabel().accept(this);
		}
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CastExpression)
	 */
	public boolean visit(CastExpression node) {
		this.tokens.add("(");//$NON-NLS-1$
		node.getType().accept(this);
		this.tokens.add(")");//$NON-NLS-1$
		node.getExpression().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CatchClause)
	 */
	public boolean visit(CatchClause node) {
		this.tokens.add("catch"); this.tokens.add("(");//$NON-NLS-1$
		node.getException().accept(this);
		this.tokens.add(")");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CharacterLiteral)
	 */
	public boolean visit(CharacterLiteral node) {
		//this.tokens.add("\'");
		this.tokens.add(PREFIX_LITERAL + "\'|" + node.charValue());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ClassInstanceCreation)
	 */
	public boolean visit(ClassInstanceCreation node) {
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			this.tokens.add(".");//$NON-NLS-1$
		}
		this.tokens.add("new");//$NON-NLS-1$
		if (node.getAST().apiLevel() == JLS2) {
			getName(node).accept(this);
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {
				this.tokens.add("<");//$NON-NLS-1$
				for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.tokens.add(",");//$NON-NLS-1$
					}
				}
				this.tokens.add(">");//$NON-NLS-1$
			}
			node.getType().accept(this);
		}
		this.tokens.add("(");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add(")");//$NON-NLS-1$
		if (node.getAnonymousClassDeclaration() != null) {
			node.getAnonymousClassDeclaration().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CompilationUnit)
	 */
	public boolean visit(CompilationUnit node) {
		if (node.getPackage() != null) {
			node.getPackage().accept(this);
		}
		for (Iterator it = node.imports().iterator(); it.hasNext(); ) {
			ImportDeclaration d = (ImportDeclaration) it.next();
			d.accept(this);
		}
		for (Iterator it = node.types().iterator(); it.hasNext(); ) {
			AbstractTypeDeclaration d = (AbstractTypeDeclaration) it.next();
			d.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConditionalExpression)
	 */
	public boolean visit(ConditionalExpression node) {
		node.getExpression().accept(this);
		this.tokens.add("?");//$NON-NLS-1$
		node.getThenExpression().accept(this);
		this.tokens.add(":");//$NON-NLS-1$
		node.getElseExpression().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConstructorInvocation)
	 */
	public boolean visit(ConstructorInvocation node) {
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {
				this.tokens.add("<");//$NON-NLS-1$
				for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.tokens.add(",");//$NON-NLS-1$
					}
				}
				this.tokens.add(">");//$NON-NLS-1$
			}
		}
		this.tokens.add("this"); this.tokens.add("(");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add(")"); this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ContinueStatement)
	 */
	public boolean visit(ContinueStatement node) {
		this.tokens.add("continue");//$NON-NLS-1$
		if (node.getLabel() != null) {
			node.getLabel().accept(this);
		}
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(DoStatement)
	 */
	public boolean visit(DoStatement node) {
		this.tokens.add("do");//$NON-NLS-1$
		node.getBody().accept(this);
		this.tokens.add("while"); this.tokens.add("(");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.tokens.add(")"); this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EmptyStatement)
	 */
	public boolean visit(EmptyStatement node) {
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnhancedForStatement)
	 * @since 3.1
	 */
	public boolean visit(EnhancedForStatement node) {
		this.tokens.add("for"); this.tokens.add("(");//$NON-NLS-1$
		node.getParameter().accept(this);
		this.tokens.add(":");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.tokens.add(")");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnumConstantDeclaration)
	 * @since 3.1
	 */
	public boolean visit(EnumConstantDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printModifiers(node.modifiers());
		node.getName().accept(this);
		if (!node.arguments().isEmpty()) {
			this.tokens.add("(");//$NON-NLS-1$
			for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
				Expression e = (Expression) it.next();
				e.accept(this);
				if (it.hasNext()) {
					this.tokens.add(",");//$NON-NLS-1$
				}
			}
			this.tokens.add(")");//$NON-NLS-1$
		}
		if (node.getAnonymousClassDeclaration() != null) {
			node.getAnonymousClassDeclaration().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnumDeclaration)
	 * @since 3.1
	 */
	public boolean visit(EnumDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		printModifiers(node.modifiers());
		this.tokens.add("enum");//$NON-NLS-1$
		node.getName().accept(this);
		if (!node.superInterfaceTypes().isEmpty()) {
			this.tokens.add("implements");//$NON-NLS-1$
			for (Iterator it = node.superInterfaceTypes().iterator(); it.hasNext(); ) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					this.tokens.add(",");//$NON-NLS-1$
				}
			}
		}
		this.tokens.add("{");//$NON-NLS-1$
		for (Iterator it = node.enumConstants().iterator(); it.hasNext(); ) {
			EnumConstantDeclaration d = (EnumConstantDeclaration) it.next();
			d.accept(this);
			// enum constant declarations do not include punctuation
			if (it.hasNext()) {
				// enum constant declarations are separated by commas
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		if (!node.bodyDeclarations().isEmpty()) {
			this.tokens.add(";");//$NON-NLS-1$
			for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
				BodyDeclaration d = (BodyDeclaration) it.next();
				d.accept(this);
				// other body declarations include trailing punctuation
			}
		}
		this.tokens.add("}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ExpressionStatement)
	 */
	public boolean visit(ExpressionStatement node) {
		node.getExpression().accept(this);
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldAccess)
	 */
	public boolean visit(FieldAccess node) {
		node.getExpression().accept(this);
		this.tokens.add(".");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		if (node.getAST().apiLevel() == JLS2) {
			printModifiers(node.getModifiers());
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType().accept(this);
		for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
			f.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ForStatement)
	 */
	public boolean visit(ForStatement node) {
		this.tokens.add("for"); this.tokens.add("(");//$NON-NLS-1$
		for (Iterator it = node.initializers().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) this.tokens.add(",");//$NON-NLS-1$
		}
		this.tokens.add(";");//$NON-NLS-1$
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		}
		this.tokens.add(";");//$NON-NLS-1$
		for (Iterator it = node.updaters().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) this.tokens.add(",");//$NON-NLS-1$
		}
		this.tokens.add(")");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(IfStatement)
	 */
	public boolean visit(IfStatement node) {
		this.tokens.add("if"); this.tokens.add("(");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.tokens.add(")");//$NON-NLS-1$
		node.getThenStatement().accept(this);
		if (node.getElseStatement() != null) {
			this.tokens.add("else");//$NON-NLS-1$
			node.getElseStatement().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ImportDeclaration)
	 */
	public boolean visit(ImportDeclaration node) {
		this.tokens.add("import");//$NON-NLS-1$
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.isStatic()) {
				this.tokens.add("static");//$NON-NLS-1$
			}
		}
		node.getName().accept(this);
		if (node.isOnDemand()) {
			this.tokens.add("."); this.tokens.add("*");//$NON-NLS-1$
		}
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(InfixExpression)
	 */
	public boolean visit(InfixExpression node) {
		node.getLeftOperand().accept(this);
		this.tokens.add(node.getOperator().toString());
		node.getRightOperand().accept(this);
		final List extendedOperands = node.extendedOperands();
		if (extendedOperands.size() != 0) {
			for (Iterator it = extendedOperands.iterator(); it.hasNext(); ) {
				this.tokens.add(node.getOperator().toString());
				Expression e = (Expression) it.next();
				e.accept(this);
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Initializer)
	 */
	public boolean visit(Initializer node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		if (node.getAST().apiLevel() == JLS2) {
			printModifiers(node.getModifiers());
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(InstanceofExpression)
	 */
	public boolean visit(InstanceofExpression node) {
		node.getLeftOperand().accept(this);
		this.tokens.add("instanceof");//$NON-NLS-1$
		node.getRightOperand().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Javadoc)
	 */
	public boolean visit(Javadoc node) {
//		this.tokens.add("/** ");//$NON-NLS-1$
//		for (Iterator it = node.tags().iterator(); it.hasNext(); ) {
//			ASTNode e = (ASTNode) it.next();
//			e.accept(this);
//		}
//		this.tokens.add("\n */\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LabeledStatement)
	 */
	public boolean visit(LabeledStatement node) {
		node.getLabel().accept(this);
		this.tokens.add(":");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LineComment)
	 * @since 3.0
	 */
	public boolean visit(LineComment node) {
		//this.tokens.add("//\n");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MarkerAnnotation)
	 * @since 3.1
	 */
	public boolean visit(MarkerAnnotation node) {
		this.tokens.add("@");//$NON-NLS-1$
		node.getTypeName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MemberRef)
	 * @since 3.0
	 */
	public boolean visit(MemberRef node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
		}
		this.tokens.add("#");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MemberValuePair)
	 * @since 3.1
	 */
	public boolean visit(MemberValuePair node) {
		node.getName().accept(this);
		this.tokens.add("=");//$NON-NLS-1$
		node.getValue().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodDeclaration)
	 */
	public boolean visit(MethodDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		if (node.getAST().apiLevel() == JLS2) {
			printModifiers(node.getModifiers());
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
			if (!node.typeParameters().isEmpty()) {
				this.tokens.add("<");//$NON-NLS-1$
				for (Iterator it = node.typeParameters().iterator(); it.hasNext(); ) {
					TypeParameter t = (TypeParameter) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.tokens.add(",");//$NON-NLS-1$
					}
				}
				this.tokens.add(">");//$NON-NLS-1$
			}
		}
		if (!node.isConstructor()) {
			if (node.getAST().apiLevel() == JLS2) {
				getReturnType(node).accept(this);
			} else {
				if (node.getReturnType2() != null) {
					node.getReturnType2().accept(this);
				} else {
					// methods really ought to have a return type
					this.tokens.add("void");//$NON-NLS-1$
				}
			}
		}
		node.getName().accept(this);
		this.tokens.add("(");//$NON-NLS-1$
		for (Iterator it = node.parameters().iterator(); it.hasNext(); ) {
			SingleVariableDeclaration v = (SingleVariableDeclaration) it.next();
			v.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add(")");//$NON-NLS-1$
		for (int i = 0; i < node.getExtraDimensions(); i++) {
			this.tokens.add("[]"); //$NON-NLS-1$
		}
		if (!node.thrownExceptions().isEmpty()) {
			this.tokens.add("throws");//$NON-NLS-1$
			for (Iterator it = node.thrownExceptions().iterator(); it.hasNext(); ) {
				Name n = (Name) it.next();
				n.accept(this);
				if (it.hasNext()) {
					this.tokens.add(",");//$NON-NLS-1$
				}
			}
		}
		if (node.getBody() == null) {
			this.tokens.add(";");//$NON-NLS-1$
		} else {
			node.getBody().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodInvocation)
	 */
	public boolean visit(MethodInvocation node) {
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			this.tokens.add(".");//$NON-NLS-1$
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {
				this.tokens.add("<");//$NON-NLS-1$
				for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.tokens.add(",");//$NON-NLS-1$
					}
				}
				this.tokens.add(">");//$NON-NLS-1$
			}
		}
		node.getName().accept(this);
		this.tokens.add("(");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodRef)
	 * @since 3.0
	 */
	public boolean visit(MethodRef node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
		}
		this.tokens.add("#");//$NON-NLS-1$
		node.getName().accept(this);
		this.tokens.add("(");//$NON-NLS-1$
		for (Iterator it = node.parameters().iterator(); it.hasNext(); ) {
			MethodRefParameter e = (MethodRefParameter) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodRefParameter)
	 * @since 3.0
	 */
	public boolean visit(MethodRefParameter node) {
		node.getType().accept(this);
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.isVarargs()) {
				this.tokens.add("...");//$NON-NLS-1$
			}
		}
		if (node.getName() != null) {
			node.getName().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Modifier)
	 * @since 3.1
	 */
	public boolean visit(Modifier node) {
		this.tokens.add(node.getKeyword().toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NormalAnnotation)
	 * @since 3.1
	 */
	public boolean visit(NormalAnnotation node) {
		this.tokens.add("@");//$NON-NLS-1$
		node.getTypeName().accept(this);
		this.tokens.add("(");//$NON-NLS-1$
		for (Iterator it = node.values().iterator(); it.hasNext(); ) {
			MemberValuePair p = (MemberValuePair) it.next();
			p.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NullLiteral)
	 */
	public boolean visit(NullLiteral node) {
		this.tokens.add("null");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NumberLiteral)
	 */
	public boolean visit(NumberLiteral node) {
		//this.tokens.add(node.getToken());
		//this.tokens.add("0.0");
		this.tokens.add(PREFIX_LITERAL + "0.0|" + node.getToken());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PackageDeclaration)
	 */
	public boolean visit(PackageDeclaration node) {
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.getJavadoc() != null) {
				node.getJavadoc().accept(this);
			}
			for (Iterator it = node.annotations().iterator(); it.hasNext(); ) {
				Annotation p = (Annotation) it.next();
				p.accept(this);
			}
		}
		this.tokens.add("package");//$NON-NLS-1$
		node.getName().accept(this);
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ParameterizedType)
	 * @since 3.1
	 */
	public boolean visit(ParameterizedType node) {
		node.getType().accept(this);
		this.tokens.add("<");//$NON-NLS-1$
		for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
			Type t = (Type) it.next();
			t.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add(">");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ParenthesizedExpression)
	 */
	public boolean visit(ParenthesizedExpression node) {
		this.tokens.add("(");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.tokens.add(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PostfixExpression)
	 */
	public boolean visit(PostfixExpression node) {
		node.getOperand().accept(this);
		this.tokens.add(node.getOperator().toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrefixExpression)
	 */
	public boolean visit(PrefixExpression node) {
		this.tokens.add(node.getOperator().toString());
		node.getOperand().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrimitiveType)
	 */
	public boolean visit(PrimitiveType node) {
		this.tokens.add(node.getPrimitiveTypeCode().toString());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedName)
	 */
	public boolean visit(QualifiedName node) {
		node.getQualifier().accept(this);
		this.tokens.add(".");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedType)
	 * @since 3.1
	 */
	public boolean visit(QualifiedType node) {
		node.getQualifier().accept(this);
		this.tokens.add(".");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ReturnStatement)
	 */
	public boolean visit(ReturnStatement node) {
		this.tokens.add("return");//$NON-NLS-1$
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		}
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleName)
	 */
	public boolean visit(SimpleName node) {
		String name = node.getIdentifier();
		if (isLocalVariableName(node))
			name = PREFIX_LOCAL_VARIABLE + node.getIdentifier();
		this.tokens.add(name);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleType)
	 */
	public boolean visit(SimpleType node) {
		return true;
	}

	/*
	 * @see ASTVisitor#visit(SingleMemberAnnotation)
	 * @since 3.1
	 */
	public boolean visit(SingleMemberAnnotation node) {
		this.tokens.add("@");//$NON-NLS-1$
		node.getTypeName().accept(this);
		this.tokens.add("(");//$NON-NLS-1$
		node.getValue().accept(this);
		this.tokens.add(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SingleVariableDeclaration)
	 */
	public boolean visit(SingleVariableDeclaration node) {
		if (node.getAST().apiLevel() == JLS2) {
			printModifiers(node.getModifiers());
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType().accept(this);
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.isVarargs()) {
				this.tokens.add("...");//$NON-NLS-1$
			}
		}
		node.getName().accept(this);
		for (int i = 0; i < node.getExtraDimensions(); i++) {
			this.tokens.add("[]"); //$NON-NLS-1$
		}
		if (node.getInitializer() != null) {
			this.tokens.add("=");//$NON-NLS-1$
			node.getInitializer().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(StringLiteral)
	 */
	public boolean visit(StringLiteral node) {
		//this.tokens.add("\"");
		this.tokens.add(PREFIX_LITERAL + "\"|" + node.getEscapedValue());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperConstructorInvocation)
	 */
	public boolean visit(SuperConstructorInvocation node) {
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			this.tokens.add(".");//$NON-NLS-1$
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {
				this.tokens.add("<");//$NON-NLS-1$
				for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.tokens.add(",");//$NON-NLS-1$
					}
				}
				this.tokens.add(">");//$NON-NLS-1$
			}
		}
		this.tokens.add("super"); this.tokens.add("(");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add(")"); this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperFieldAccess)
	 */
	public boolean visit(SuperFieldAccess node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
			this.tokens.add(".");//$NON-NLS-1$
		}
		this.tokens.add("super"); this.tokens.add(".");//$NON-NLS-1$
		node.getName().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperMethodInvocation)
	 */
	public boolean visit(SuperMethodInvocation node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
			this.tokens.add(".");//$NON-NLS-1$
		}
		this.tokens.add("super"); this.tokens.add(".");//$NON-NLS-1$
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeArguments().isEmpty()) {
				this.tokens.add("<");//$NON-NLS-1$
				for (Iterator it = node.typeArguments().iterator(); it.hasNext(); ) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.tokens.add(",");//$NON-NLS-1$
					}
				}
				this.tokens.add(">");//$NON-NLS-1$
			}
		}
		node.getName().accept(this);
		this.tokens.add("(");//$NON-NLS-1$
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add(")");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchCase)
	 */
	public boolean visit(SwitchCase node) {
		if (node.isDefault()) {
			this.tokens.add("default"); this.tokens.add(":");//$NON-NLS-1$
		} else {
			this.tokens.add("case");//$NON-NLS-1$
			node.getExpression().accept(this);
			this.tokens.add(":");//$NON-NLS-1$
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchStatement)
	 */
	public boolean visit(SwitchStatement node) {
		this.tokens.add("switch"); this.tokens.add("(");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.tokens.add(")");//$NON-NLS-1$
		this.tokens.add("{");//$NON-NLS-1$
		for (Iterator it = node.statements().iterator(); it.hasNext(); ) {
			Statement s = (Statement) it.next();
			s.accept(this);
		}
		this.tokens.add("}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SynchronizedStatement)
	 */
	public boolean visit(SynchronizedStatement node) {
		this.tokens.add("synchronized"); this.tokens.add("(");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.tokens.add(")");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TagElement)
	 * @since 3.0
	 */
	public boolean visit(TagElement node) {
		if (node.isNested()) {
			// nested tags are always enclosed in braces
			this.tokens.add("{");//$NON-NLS-1$
		} else {
			// top-level tags always begin on a new line
			this.tokens.add("*");//$NON-NLS-1$
		}
		if (node.getTagName() != null) {
			this.tokens.add(node.getTagName());
		}
		boolean previousRequiresNewLine = false;
		for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
			ASTNode e = (ASTNode) it.next();
			// assume text elements include necessary leading and trailing whitespace
			// but Name, MemberRef, MethodRef, and nested TagElement do not include white space
			boolean currentIncludesWhiteSpace = (e instanceof TextElement);
			if (previousRequiresNewLine && currentIncludesWhiteSpace) {
				this.tokens.add("*");//$NON-NLS-1$
			}
			previousRequiresNewLine = currentIncludesWhiteSpace;
			e.accept(this);
		}
		if (node.isNested()) {
			this.tokens.add("}");//$NON-NLS-1$
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TextElement)
	 * @since 3.0
	 */
	public boolean visit(TextElement node) {
		//this.tokens.add(node.getText());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ThisExpression)
	 */
	public boolean visit(ThisExpression node) {
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);
			this.tokens.add(".");//$NON-NLS-1$
		}
		this.tokens.add("this");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ThrowStatement)
	 */
	public boolean visit(ThrowStatement node) {
		this.tokens.add("throw");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TryStatement)
	 */
	public boolean visit(TryStatement node) {
		this.tokens.add("try");//$NON-NLS-1$
		node.getBody().accept(this);
		for (Iterator it = node.catchClauses().iterator(); it.hasNext(); ) {
			CatchClause cc = (CatchClause) it.next();
			cc.accept(this);
		}
		if (node.getFinally() != null) {
			this.tokens.add("finally");//$NON-NLS-1$
			node.getFinally().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclaration)
	 */
	public boolean visit(TypeDeclaration node) {
		if (node.getJavadoc() != null) {
			node.getJavadoc().accept(this);
		}
		if (node.getAST().apiLevel() == JLS2) {
			printModifiers(node.getModifiers());
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		this.tokens.add(node.isInterface() ? "interface" : "class");//$NON-NLS-2$//$NON-NLS-1$
		node.getName().accept(this);
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!node.typeParameters().isEmpty()) {
				this.tokens.add("<");//$NON-NLS-1$
				for (Iterator it = node.typeParameters().iterator(); it.hasNext(); ) {
					TypeParameter t = (TypeParameter) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.tokens.add(",");//$NON-NLS-1$
					}
				}
				this.tokens.add(">");//$NON-NLS-1$
			}
		}
		if (node.getAST().apiLevel() == JLS2) {
			if (getSuperclass(node) != null) {
				this.tokens.add("extends");//$NON-NLS-1$
				getSuperclass(node).accept(this);
			}
			if (!superInterfaces(node).isEmpty()) {
				this.tokens.add(node.isInterface() ? "extends" : "implements");//$NON-NLS-2$//$NON-NLS-1$
				for (Iterator it = superInterfaces(node).iterator(); it.hasNext(); ) {
					Name n = (Name) it.next();
					n.accept(this);
					if (it.hasNext()) {
						this.tokens.add(",");//$NON-NLS-1$
					}
				}
			}
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.getSuperclassType() != null) {
				this.tokens.add("extends");//$NON-NLS-1$
				node.getSuperclassType().accept(this);
			}
			if (!node.superInterfaceTypes().isEmpty()) {
				this.tokens.add(node.isInterface() ? "extends" : "implements");//$NON-NLS-2$//$NON-NLS-1$
				for (Iterator it = node.superInterfaceTypes().iterator(); it.hasNext(); ) {
					Type t = (Type) it.next();
					t.accept(this);
					if (it.hasNext()) {
						this.tokens.add(",");//$NON-NLS-1$
					}
				}
			}
		}
		this.tokens.add("{");//$NON-NLS-1$
		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			d.accept(this);
		}
		this.tokens.add("}");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclarationStatement)
	 */
	public boolean visit(TypeDeclarationStatement node) {
		if (node.getAST().apiLevel() == JLS2) {
			getTypeDeclaration(node).accept(this);
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			node.getDeclaration().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeLiteral)
	 */
	public boolean visit(TypeLiteral node) {
		node.getType().accept(this);
		this.tokens.add("."); this.tokens.add("class");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeParameter)
	 * @since 3.1
	 */
	public boolean visit(TypeParameter node) {
		node.getName().accept(this);
		if (!node.typeBounds().isEmpty()) {
			this.tokens.add("extends");//$NON-NLS-1$
			for (Iterator it = node.typeBounds().iterator(); it.hasNext(); ) {
				Type t = (Type) it.next();
				t.accept(this);
				if (it.hasNext()) {
					this.tokens.add("&");//$NON-NLS-1$
				}
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationExpression)
	 */
	public boolean visit(VariableDeclarationExpression node) {
		if (node.getAST().apiLevel() == JLS2) {
			printModifiers(node.getModifiers());
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType().accept(this);
		for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
			f.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationFragment)
	 */
	public boolean visit(VariableDeclarationFragment node) {
		node.getName().accept(this);
		for (int i = 0; i < node.getExtraDimensions(); i++) {
			this.tokens.add("[]");//$NON-NLS-1$
		}
		if (node.getInitializer() != null) {
			this.tokens.add("=");//$NON-NLS-1$
			node.getInitializer().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationStatement)
	 */
	public boolean visit(VariableDeclarationStatement node) {
		if (node.getAST().apiLevel() == JLS2) {
			printModifiers(node.getModifiers());
		}
		if (node.getAST().apiLevel() >= AST.JLS3) {
			printModifiers(node.modifiers());
		}
		node.getType().accept(this);
		for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
			f.accept(this);
			if (it.hasNext()) {
				this.tokens.add(",");//$NON-NLS-1$
			}
		}
		this.tokens.add(";");//$NON-NLS-1$
		return false;
	}

	/*
	 * @see ASTVisitor#visit(WhileStatement)
	 */
	public boolean visit(WhileStatement node) {
		this.tokens.add("while"); this.tokens.add("(");//$NON-NLS-1$
		node.getExpression().accept(this);
		this.tokens.add(")");//$NON-NLS-1$
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(WildcardType)
	 * @since 3.1
	 */
	public boolean visit(WildcardType node) {
		this.tokens.add("?");//$NON-NLS-1$
		Type bound = node.getBound();
		if (bound != null) {
			if (node.isUpperBound()) {
				this.tokens.add("extends");//$NON-NLS-1$
			} else {
				this.tokens.add("super");//$NON-NLS-1$
			}
			bound.accept(this);
		}
		return false;
	}
	
	public static String asString(ASTNode node) {
		ASTFlattener printer = new ASTFlattener();
		node.accept(printer);
		return printer.getResult();
	}
}
