package utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
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

public class LocalVariableNameMarker extends ASTVisitor {
	public static final String PROPERTY_LOCAL_VARIABLE = "lv";
	
	private HashSet<String> localVarNames = new HashSet<String>();
	private Stack<HashSet<String>> scopedVarNames = new Stack<HashSet<String>>();
	
	/*
	 * @see ASTVisitor#visit(AnnotationTypeDeclaration)
	 * @since 3.1
	 */
	public boolean visit(AnnotationTypeDeclaration node) {
		this.scopedVarNames.push(new HashSet<String>());
		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			d.accept(this);
		}
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AnonymousClassDeclaration)
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		this.scopedVarNames.push(new HashSet<String>());
		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
			BodyDeclaration b = (BodyDeclaration) it.next();
			b.accept(this);
		}
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayAccess)
	 */
	public boolean visit(ArrayAccess node) {
		node.getArray().accept(this);
		node.getIndex().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayCreation)
	 */
	public boolean visit(ArrayCreation node) {
		ArrayType at = node.getType();
		int dims = at.getDimensions();
		Type elementType = at.getElementType();
		elementType.accept(this);
		for (Iterator it = node.dimensions().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
			dims--;
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
		for (Iterator it = node.expressions().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ArrayType)
	 */
	public boolean visit(ArrayType node) {
		node.getComponentType().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(AssertStatement)
	 */
	public boolean visit(AssertStatement node) {
		node.getExpression().accept(this);
		if (node.getMessage() != null) {
			node.getMessage().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Assignment)
	 */
	public boolean visit(Assignment node) {
		node.getLeftHandSide().accept(this);
		node.getRightHandSide().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Block)
	 */
	public boolean visit(Block node) {
		this.scopedVarNames.push(new HashSet<String>());
		for (Iterator it = node.statements().iterator(); it.hasNext(); ) {
			Statement s = (Statement) it.next();
			s.accept(this);
		}
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BlockComment)
	 * @since 3.0
	 */
	public boolean visit(BlockComment node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BooleanLiteral)
	 */
	public boolean visit(BooleanLiteral node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(BreakStatement)
	 */
	public boolean visit(BreakStatement node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CastExpression)
	 */
	public boolean visit(CastExpression node) {
		//node.getType().accept(this);
		node.getExpression().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CatchClause)
	 */
	public boolean visit(CatchClause node) {
		this.scopedVarNames.push(new HashSet<String>());
		node.getException().accept(this);
		node.getBody().accept(this);
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CharacterLiteral)
	 */
	public boolean visit(CharacterLiteral node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ClassInstanceCreation)
	 */
	public boolean visit(ClassInstanceCreation node) {
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
		}
		if (node.getAnonymousClassDeclaration() != null) {
			node.getAnonymousClassDeclaration().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(CompilationUnit)
	 */
	public boolean visit(CompilationUnit node) {
		this.scopedVarNames.push(new HashSet<String>());
		for (Iterator it = node.types().iterator(); it.hasNext(); ) {
			AbstractTypeDeclaration d = (AbstractTypeDeclaration) it.next();
			d.accept(this);
		}
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConditionalExpression)
	 */
	public boolean visit(ConditionalExpression node) {
		node.getExpression().accept(this);
		node.getThenExpression().accept(this);
		node.getElseExpression().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ConstructorInvocation)
	 */
	public boolean visit(ConstructorInvocation node) {
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ContinueStatement)
	 */
	public boolean visit(ContinueStatement node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(DoStatement)
	 */
	public boolean visit(DoStatement node) {
		this.scopedVarNames.push(new HashSet<String>());
		node.getBody().accept(this);
		node.getExpression().accept(this);
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EmptyStatement)
	 */
	public boolean visit(EmptyStatement node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnhancedForStatement)
	 * @since 3.1
	 */
	public boolean visit(EnhancedForStatement node) {
		this.scopedVarNames.push(new HashSet<String>());
		node.getParameter().accept(this);
		node.getExpression().accept(this);
		node.getBody().accept(this);
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(EnumConstantDeclaration)
	 * @since 3.1
	 */
	public boolean visit(EnumConstantDeclaration node) {
		if (!node.arguments().isEmpty()) {
			for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
				Expression e = (Expression) it.next();
				e.accept(this);
			}
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
		for (Iterator it = node.enumConstants().iterator(); it.hasNext(); ) {
			EnumConstantDeclaration d = (EnumConstantDeclaration) it.next();
			d.accept(this);
		}
		if (!node.bodyDeclarations().isEmpty()) {
			for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
				BodyDeclaration d = (BodyDeclaration) it.next();
				d.accept(this);
				// other body declarations include trailing punctuation
			}
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ExpressionStatement)
	 */
	public boolean visit(ExpressionStatement node) {
		node.getExpression().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldAccess)
	 */
	public boolean visit(FieldAccess node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ForStatement)
	 */
	public boolean visit(ForStatement node) {
		this.scopedVarNames.push(new HashSet<String>());
		for (Iterator it = node.initializers().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
		}
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		}
		for (Iterator it = node.updaters().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
		}
		node.getBody().accept(this);
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(IfStatement)
	 */
	public boolean visit(IfStatement node) {
		this.scopedVarNames.push(new HashSet<String>());
		node.getExpression().accept(this);
		node.getThenStatement().accept(this);
		if (node.getElseStatement() != null) {
			node.getElseStatement().accept(this);
		}
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ImportDeclaration)
	 */
	public boolean visit(ImportDeclaration node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(InfixExpression)
	 */
	public boolean visit(InfixExpression node) {
		node.getLeftOperand().accept(this);
		node.getRightOperand().accept(this);
		final List extendedOperands = node.extendedOperands();
		if (extendedOperands.size() != 0) {
			for (Iterator it = extendedOperands.iterator(); it.hasNext(); ) {
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
		this.scopedVarNames.push(new HashSet<String>());
		node.getBody().accept(this);
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(InstanceofExpression)
	 */
	public boolean visit(InstanceofExpression node) {
		node.getLeftOperand().accept(this);
		node.getRightOperand().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Javadoc)
	 */
	public boolean visit(Javadoc node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LabeledStatement)
	 */
	public boolean visit(LabeledStatement node) {
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(LineComment)
	 * @since 3.0
	 */
	public boolean visit(LineComment node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MarkerAnnotation)
	 * @since 3.1
	 */
	public boolean visit(MarkerAnnotation node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MemberRef)
	 * @since 3.0
	 */
	public boolean visit(MemberRef node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MemberValuePair)
	 * @since 3.1
	 */
	public boolean visit(MemberValuePair node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodDeclaration)
	 */
	public boolean visit(MethodDeclaration node) {
		this.scopedVarNames.push(new HashSet<String>());
		for (Iterator it = node.parameters().iterator(); it.hasNext(); ) {
			SingleVariableDeclaration v = (SingleVariableDeclaration) it.next();
			v.accept(this);
		}
		if (node.getBody() == null) {
		} else {
			node.getBody().accept(this);
		}
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodInvocation)
	 */
	public boolean visit(MethodInvocation node) {
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		}
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodRef)
	 * @since 3.0
	 */
	public boolean visit(MethodRef node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(MethodRefParameter)
	 * @since 3.0
	 */
	public boolean visit(MethodRefParameter node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(Modifier)
	 * @since 3.1
	 */
	public boolean visit(Modifier node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NormalAnnotation)
	 * @since 3.1
	 */
	public boolean visit(NormalAnnotation node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NullLiteral)
	 */
	public boolean visit(NullLiteral node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(NumberLiteral)
	 */
	public boolean visit(NumberLiteral node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PackageDeclaration)
	 */
	public boolean visit(PackageDeclaration node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ParameterizedType)
	 * @since 3.1
	 */
	public boolean visit(ParameterizedType node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ParenthesizedExpression)
	 */
	public boolean visit(ParenthesizedExpression node) {
		node.getExpression().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PostfixExpression)
	 */
	public boolean visit(PostfixExpression node) {
		node.getOperand().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrefixExpression)
	 */
	public boolean visit(PrefixExpression node) {
		node.getOperand().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(PrimitiveType)
	 */
	public boolean visit(PrimitiveType node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedName)
	 */
	public boolean visit(QualifiedName node) {
		node.getQualifier().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(QualifiedType)
	 * @since 3.1
	 */
	public boolean visit(QualifiedType node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ReturnStatement)
	 */
	public boolean visit(ReturnStatement node) {
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleName)
	 */
	public boolean visit(SimpleName node) {
		if (this.localVarNames.contains(node.getIdentifier()))
			node.setProperty(PROPERTY_LOCAL_VARIABLE, "");
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SimpleType)
	 */
	public boolean visit(SimpleType node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SingleMemberAnnotation)
	 * @since 3.1
	 */
	public boolean visit(SingleMemberAnnotation node) {
		node.getValue().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SingleVariableDeclaration)
	 */
	public boolean visit(SingleVariableDeclaration node) {
		this.scopedVarNames.peek().add(node.getName().getIdentifier());
		this.localVarNames.add(node.getName().getIdentifier());
		node.getName().accept(this);
		if (node.getInitializer() != null) {
			node.getInitializer().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(StringLiteral)
	 */
	public boolean visit(StringLiteral node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperConstructorInvocation)
	 */
	public boolean visit(SuperConstructorInvocation node) {
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperFieldAccess)
	 */
	public boolean visit(SuperFieldAccess node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SuperMethodInvocation)
	 */
	public boolean visit(SuperMethodInvocation node) {
		for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
			Expression e = (Expression) it.next();
			e.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchCase)
	 */
	public boolean visit(SwitchCase node) {
		if (node.isDefault()) {
		} else {
			node.getExpression().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SwitchStatement)
	 */
	public boolean visit(SwitchStatement node) {
		this.scopedVarNames.push(new HashSet<String>());
		node.getExpression().accept(this);
		for (Iterator it = node.statements().iterator(); it.hasNext(); ) {
			Statement s = (Statement) it.next();
			s.accept(this);
		}
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(SynchronizedStatement)
	 */
	public boolean visit(SynchronizedStatement node) {
		node.getExpression().accept(this);
		node.getBody().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TagElement)
	 * @since 3.0
	 */
	public boolean visit(TagElement node) {
		boolean previousRequiresNewLine = false;
		for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
			ASTNode e = (ASTNode) it.next();
			// assume text elements include necessary leading and trailing whitespace
			// but Name, MemberRef, MethodRef, and nested TagElement do not include white space
			boolean currentIncludesWhiteSpace = (e instanceof TextElement);
			if (previousRequiresNewLine && currentIncludesWhiteSpace) {
			}
			previousRequiresNewLine = currentIncludesWhiteSpace;
			e.accept(this);
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
		return false;
	}

	/*
	 * @see ASTVisitor#visit(ThrowStatement)
	 */
	public boolean visit(ThrowStatement node) {
		node.getExpression().accept(this);
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TryStatement)
	 */
	public boolean visit(TryStatement node) {
		node.getBody().accept(this);
		for (Iterator it = node.catchClauses().iterator(); it.hasNext(); ) {
			CatchClause cc = (CatchClause) it.next();
			cc.accept(this);
		}
		if (node.getFinally() != null) {
			node.getFinally().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclaration)
	 */
	public boolean visit(TypeDeclaration node) {
		this.scopedVarNames.push(new HashSet<String>());
		for (Iterator it = node.bodyDeclarations().iterator(); it.hasNext(); ) {
			BodyDeclaration d = (BodyDeclaration) it.next();
			d.accept(this);
		}
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeDeclarationStatement)
	 */
	public boolean visit(TypeDeclarationStatement node) {
		if (node.getAST().apiLevel() >= AST.JLS3) {
			node.getDeclaration().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeLiteral)
	 */
	public boolean visit(TypeLiteral node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(TypeParameter)
	 * @since 3.1
	 */
	public boolean visit(TypeParameter node) {
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationExpression)
	 */
	public boolean visit(VariableDeclarationExpression node) {
		for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
			f.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationFragment)
	 */
	public boolean visit(VariableDeclarationFragment node) {
		this.localVarNames.add(node.getName().getIdentifier());
		this.scopedVarNames.peek().add(node.getName().getIdentifier());
		node.getName().accept(this);
		if (node.getInitializer() != null) {
			node.getInitializer().accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(VariableDeclarationStatement)
	 */
	public boolean visit(VariableDeclarationStatement node) {
		for (Iterator it = node.fragments().iterator(); it.hasNext(); ) {
			VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
			f.accept(this);
		}
		return false;
	}

	/*
	 * @see ASTVisitor#visit(WhileStatement)
	 */
	public boolean visit(WhileStatement node) {
		this.scopedVarNames.push(new HashSet<String>());
		node.getExpression().accept(this);
		node.getBody().accept(this);
		this.localVarNames.removeAll(this.scopedVarNames.pop());
		return false;
	}

	/*
	 * @see ASTVisitor#visit(WildcardType)
	 * @since 3.1
	 */
	public boolean visit(WildcardType node) {
		return false;
	}
}
