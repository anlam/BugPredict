/**
 * 
 */
package recoder.testsuite.semantics;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import junit.framework.TestCase;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ModelException;
import recoder.ParserException;
import recoder.java.CompilationUnit;
import recoder.service.DefaultErrorHandler;
import recoder.service.ErrorHandler;
import recoder.service.GenericsUseException;
import recoder.service.SemanticsChecker;
import recoder.service.TypingException;
import recoder.service.UnresolvedReferenceException;

/**
 * @author Tobias Gutzmann
 *
 */
public class SemanticsChecks extends TestCase {
	// TODO evil copy & paste from FixedBugs, with some adaptions...
	
	////////////////////////////////////////////////////////////
	// helper methods / classes
	////////////////////////////////////////////////////////////
    private CrossReferenceServiceConfiguration sc;
	private List<CompilationUnit> runIt(String ... cuTexts) throws ParserException {
		return runIt(null, cuTexts);
	}

	private List<CompilationUnit> runIt(ErrorHandler eh, String ... cuTexts) throws ParserException {
		sc = new CrossReferenceServiceConfiguration();
		sc.getProjectSettings().setErrorHandler(new ThrowingErrorHandler());
       	sc.getProjectSettings().ensureSystemClassesAreInPath();
       	ArrayList<CompilationUnit> cus = new ArrayList<CompilationUnit>();
       	for (String cuText : cuTexts) {
       		CompilationUnit cu = sc.getProgramFactory().parseCompilationUnit(cuText);
       		sc.getChangeHistory().attached(cu);
       		cus.add(cu);
       	}
       	if (eh != null)
       		sc.getProjectSettings().setErrorHandler(eh);
       	sc.getChangeHistory().updateModel();
       	for (CompilationUnit cu : cus)
       		cu.validateAll();
        return cus;
	}

	private static class ThrowingErrorHandler extends DefaultErrorHandler {
		@Override
		public void reportError(Exception e) {
			throw (ModelException)e;
		}
	}
	
	private static class SilentErrorHandler extends DefaultErrorHandler {
		private final int exp;
		private int errCnt = 0;
		SilentErrorHandler(int cnt) {
			exp = cnt;
		}
		@Override public void reportError(Exception e) {
			errCnt++;
		}
		@Override public void modelUpdated(EventObject event) {
			isUpdating = false;
			assertEquals(exp, errCnt);
		}
	}

	////////////////////////////////////////////////////////////
	// The actual test cases
	////////////////////////////////////////////////////////////

	public void testMethodInvocationOK1(){
		String cuText = 
			"class Addtion {" +
			"	static int get(){return get(1);}" +
			"	private static int get(int i) { return 2*i; }" +
			"   public static void main(){"+
			"		this.get();"+
			"	}"+
			"}" ;			
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);						
		} catch (ModelException e) {
			// as expected!!
		}
	}
	
	
	
	public void testMethodInvocation2(){//Can't make a static reference to the non-static field 
		String cuText = 
			"class TestStatic {" +
			"	int i = 100;//non-static field \n" + 
			"	int a;"+
			"	static void set() {"+
			"		int j = i; //error \n"+		
			"	}"+	
			"}"; 	
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
			fail();
		} catch (ModelException e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocationOK2(){
		String cuText = 
			"class TestStatic {" +
			"	static int i = 100;" +
			"	int a;"+
			"	static void set() {"+
			"		int j = i; //correct \n"+		
			"	}"+	
			"}"; 	
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
		} catch (ModelException e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocation3(){//abstract method can only define in an abstract class or interface
		String cuText = 
			"class IsStatic {" +
			"	abstract void is(){}"+
			"}" ;		
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
			fail();
		} catch (ModelException e) {
			// as expected!!
		}
	}
		
	public void testMethodInvocationOK3a(){//abstract method can only define in an abstract class or interface
		String cuText = 		
			"abstract class IsStatic {" +
			"	abstract void is (){}"+
			"}" +			
			"interface TestStatic {" +
			"	abstract void is(){ }"+
			"}" ;						
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
		} catch (ModelException e) {
			// as expected!!
		}
	}
	
	
	public void testMethodInvocationOK3b(){
		String cuText =
			"abstract class ant{"+
			"	abstract void run();"+
			"}"+
			"abstract class queen extends ant{"+
			"	void eat(){  }  //correct! \n"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);
		} catch (ModelException e) {
			// as expected!!
		}
	}
	
//	public void testMethodInvocationWrong4(){//java.lang.object
//		String cuText =
//			"interface I extends Cloneable { Object clone() throws CloneNotSupportedException; }"+
//			"class Animal implements I {  "+
//			"	Animal animal = new Animal();"+
//			"	class Bee { "+
//			"		void fly() throws Exception {"+
//			"			animal.clone(); //error!\n"+
//			"		}"+
//			"	}"+
//			"}";			
//		CompilationUnit cu = null;
//		try {
//			cu = runIt(cuText).get(0);
//		} catch (ParserException e) {
//			fail(e.getMessage());
//		}
//		try {
//			new SemanticsChecker(sc).check(cu);	
//			fail();
//		} catch (Exception e) {
//			// as expected!!
//		}
//	}
	
	
	public void testMethodInvocationOK6(){
		String cuText = 
			"class Parent {"+
	        "	Parent() {}"+
	        "}"+
	        "class Child extends Parent{"+
	        "	Child() {"+
	        " 		super();"+
	        " 	}"+
	        "} ";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);							
		} catch (ModelException e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocationOK7(){
		String cuText = 
			"class Naming {"+
		    "	String name;"+
		    "	Naming(String input) {"+
		    "        name = input;"+
		    "	}"+
		    "	Naming() {"+
		    "        this("+"\""+"mary"+"\""+");//correct!\n"+
		    "	}"+
		    "}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);							
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocationOK8(){//static reference to static method
		String cuText = 
			"class Parent {"+
			"	int number;"+
		    "	static void print(){System.out.println("+"\""+"Parent"+"\""+");}"+
		    "	static void set(){} {"+
		    "        number = 1;"+
		    "	}"+
		    "}"+
		    "class Children {"+
		    "	public static void main() {"+
		    "        Parent.print(); //correct!\n"+
		    "	     Parent.set();//correct!\n"+
		    "		 Parent one = new Parent();"+
		    "		 one.set();//correct!\n"+
		    "	}"+
		    "}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);							
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	
	public void testMethodInvocation8(){//static reference to non-static method
		String cuText = 
			"class Parent {"+
			"	int number;"+
		    "	void set(){} {"+
		    "        number = 1;"+
		    "	}"+
		    "}"+
		    "class Children {"+
		    "	public static void main() {"+
		    "	     Parent.set();//error!\n"+
		    "	}"+
		    "}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
			fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocation9(){//static reference to non-static method
		String cuText = 
			"class Test {"+
			"	int number;"+
		    "	void nostaticset(){"+
		    "		number = 3;"+
		    "	}"+
		    "	static void testnostatic(){"+
		    "		nostaticset();//error!\n"+
		    "	}"+
		    "}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
			fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocationOK9(){//static reference to static method
		String cuText = 
			"class Test {"+
			"	int number;"+
		    "	static void staticset(){} {"+
		    "       number = 1;"+
		    "	}"+
		    "	void nostaticset(){"+
		    "		number = 3;"+
		    "	}"+
		    "	static void teststaticset(){"+
		    "		staticset();//correct!\n"+
		    "	}"+
		    "	void testnostaticset(){"+
		    "		staticset();//correct!\n"+
		    "		nostaticset();//correct!\n"+
		    "	}"+
		    "}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	
	public void testMethodInvocation10(){//lack of one abstract method implement
		String cuText = 
			"interface I{"+
			"	void write();"+
			"	void read();"+
			"}"+
		    "abstract class X1 implements I {"+
			"	static int i=9;"+
			"	static void have(){}"+
			"	abstract void pick();"+
			"}"+
			"class X2 extends X1 {//error! lack of unimplement abstract method of X \n"+
			"	public void read() {  }"+
			"	public void write(){  }"+			
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
			//fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocation11(){//abstract and super correct
		String cuText =
			"class Z{"+
			"	Z(){int z=10;}"+
			"}"+
			"class S extends Z {"+
			"	S (){super();}"+
			"}"+
			"abstract class X1  extends S{"+
			"	int i;"+
			"	X1(){super();}"+
			"	abstract void has();"+
			"	void put(){i = 9;}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);		
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocation12(){// the visibilty only can be public or protected
		String cuText =
			"abstract class IsAbstract{"+
			"	abstract final void one();//error \n"+
			"	abstract static void two();//error \n"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
			fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocationOK12(){// the visibilty only can be public or protected
		String cuText =
			"abstract class IsAbstract{"+
			"	abstract public void one();//correct! \n"+
			"	abstract protected void two();//correct! \n"+
			"	abstract void three();//correct! \n"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocationOK13(){//correcr using
		String cuText =
			"class outer{"+
//			"	outer(){int i=2;}"+
//			"	protected Object clone(){"+
//			"		return outer.this.hashCode();"+
//			"	}"+
//			"	class inner{"+
//			"		void set(){	int j=19;}"+
//			"	}"+
//			"	public static void main(){"+
//			"		outer ou=new outer();"+
//			"		ou.clone();"+		
//			"   	new outer.inner.set();//error \n"+
//			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
			//fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocation14(){//can not directly invoke the method 
		String cuText =
			"abstract class animal{"+
			"	public abstract String toString();"+
			"}"+
			"class ant extends animal{ "+
			"	int age;"+
			"	public String toString(){"+
			"		return super.toString()+ age;//error!\n"+
			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);
			fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocationOK14(){//correct 
		String cuText =
			"abstract class animal{"+
			"	public abstract String toString();"+
			"	protected String objString(){"+
			"		return super.toString();"+
			"	}"+
			"}"+
			"class ant extends animal{ "+
			"	int age;"+
			"	public String toString(){"+
			"		return super.toString()+ age;//correct!\n"+
			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);
		} catch (Exception e) {
			// as expected!!
		}
	}
		
	public void testMethodInvocation15(){//can't not directly invoke the abstract method
		String cuText =
			"abstract class ant{"+
			"	abstract void run();"+
			"}"+
			"class queen extends ant{"+
			"	void run(){"+
			"		super.run();"+
			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);
			fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocationOK15(){
		String cuText =
			"abstract class ant{"+
			"	abstract void run();"+
			"}"+
			"class queen extends ant{"+
			"	void run(){"+
			"		super.hashCode();"+
			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);
		} catch (Exception e) {
			// as expected!!
		}
	}
	

	
	public void testMethodInvocation17(){//check reference method return type
		String cuText =
			"class A {int a;}"+
			"class B extends A{int b;}"+
			"class Test {"+
			"	static int test (B b){"+
			"		return b.b;"+
			"	}"+
			"	static String test (A a){"+
			"		return "+"\""+"A"+"\""+";"+
			"	}"+
			"	public static void main(String[] args){"+
			"		B testB = new B();"+
			"		String s = test(testB);//error!\n"+
			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);
			fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	

	public void testMethodInvocation18(){
		String cuText =
			"class A {"+
			"	static void meet(){ }"+
			"	static A test(){return null;}"+
			"	public static void main ( ){"+
			"		test().meet();//correct!\n"+
			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);			
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocation19(){//check expression object invoked
		String cuText =
			"class StringTest {"+
			"	public static void main ( ){"+
			"		String s = "+"\""+"check"+"\""+";"+
			"		if (s.endsWith("+"\""+"k"+"\""+")){//correct!\n"+
			"			s = "+"\""+"ok!"+"\""+";"+
			"		}"+
			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);			
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocation20(){//about super
		String cuText =
			"class AAA {"+
			"	int charge(){return 7;}"+
			"}"+
			"class AA extends AAA{"+			
			"	int charge(){return 5;}"+
			"}"+
			"class Battery extends AA{"+
			"	int charge(){return 0;}"+
			"	void test(){"+
			"		super.charge();"+
			"		((AA)this).charge();"+
			"		((AAA)this).charge();"+
			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);			
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocation21(){
		String cuText =
			"class Hello{"+
			"	void say(){}"+
			"	class H{"+
			"		void say(){}"+			
			"	}"+
			"	public static void main(){"+
			"		Hello h = new Hello();"+
			"		h.say();"+
			"		h.new H().say();//correct! \n"+
			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocation22(){//Cannot use this in a static context
		String cuText =
			"class Hello{"+
			"	void say(){}"+
			"	class H{"+
			"		void say(){}"+			
			"	}"+
			"	public static void main(){"+
			"		Hello.this.say();//error! \n"+
			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
			fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocation23(){//No enclosing instance of the type Hello.H1 is accessible in scope
		String cuText =
			"class Hello{"+
			"	void say(){}"+
			"	class H{"+
			"		void say(){}"+			
			"	}"+
			"	public static void main(){"+
			"		H.this.say();//error! \n"+
			"	}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		} catch (UnresolvedReferenceException e) {
			// as expected, this must be caught during type resolving already!
		}
		try {
			new SemanticsChecker(sc).check(cu);	
			fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	public void testMethodInvocation24(){
		String cuText =
			"class Hello{"+
			"	void say(){"+
			"		answer();//correct!\n"+
			"	}"+
			"	void answer(){}"+
			"}";
			
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testMethodInvocation25(){
		String cuText =
			"import java.util.ArrayList;"+
			"import java.util.List;"+
			"class H{"+
			"	ArrayList getList(){return new ArrayList();}"+
			"	void foo(){"+
			"		List list = foo();"+
			"	}"+
			"}";
			
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
			fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testSubclass1(){//final class can't have subclass
		String cuText = 
			"class Point { int x, y; }"+
			"final class WhitePoint extends Point  { int white; }"+
			"class ColorPoint extends WhitePoint { int black; }//error! \n"; 		
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);	
			//TODO
			//fail();
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testSubclassOK1(){
		String cuText = 
			"class Point { int x, y; }"+
			"class WhitePoint extends Point  { int white; }"+
			"class ColorPoint extends WhitePoint { int black; }//correct! \n"; 		
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);				
		} catch (Exception e) {
			// as expected!!
		}
	}
	
	public void testClassMethodMatch1(){//method declaration doesn't match super class's
		String cuText =
			"class animal{"+
			"	int x = 0, y = 0 , age;"+
			"	void grow(int dx, int dy){ x += dx;y += dy;}"+
			"	int getX() {return x;}"+
			"	int getY() {return y;}"+
			"}"+
			"class ant extends animal{ "+
			"	float x = 0.0f , y = 0.0f;"+
			"	void grow (int dx, int dy){grow((float)dx,(float)dy);}"+
			"	void grow(float dx,float dy){x+= dx; y+= dy;}"+
			"	float getX(){return x;}"+
			"	float getY(){return y;}"+
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);
			//TODO
			//fail();
		} catch (Exception e) {
			// as expected!!
		}
	}


	public void testRawInnerTypes1() {
		String cuText = 
			"class Outer<T>{"+
			"	class Inner<S> {"+
			"		S s;"+
			"	}"+
			"}" +
			"" +
			"class A {" +
			"	Outer.Inner<Double> x = null; // error!\n" +
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);
			fail();
		} catch (GenericsUseException te) {
			// as expected!!
		}
	}
	
	public void testRawOuterTypes1() {
		String cuText = 
			"class Outer<T>{"+
			"	class Inner<S> {"+
			"		S s;"+
			"	}"+
			"}" +
			"" +
			"class A {" +
			"	Outer<String>.Inner x = null; // error!\n" +
			"}";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);
		} catch (GenericsUseException te) {
			// as expected!!
		}
	}
	
	public void testRawOKTypes1() {
		String cuText = 		
			"class Outer<T>{"+
			"	class Inner<S> {"+
			"		S s;"+
			"	}"+
			"}" +
			"" +
			"class A {" +
			"   Outer<Double>.Inner<Double> y = null; // ok!\n" +
			"}";

		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);						
		} catch (ModelException e) {
			assertTrue(e.getMessage(),true);// as expected!!
		}
	}
	
	public void testRawOKTypes2() {
		String cuText = 		
			"class A {"+
			  "class B<T> { "+
			  	"void foo() {"+
			  		" A.B<String> ab = new A.B<String>();"+
			  	"}"+
			  "}"+
			"}";

		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);						
		} catch (Exception e) {
			assertTrue(e.getMessage(),true);// as expected!!
		}
	}
	
	public void testIf() {
		String cuText = 
			"class A {\n" +
			"	void foo() {\n" +
			"		if (new Object()) { }" +
			"	}\n" +
			"}\n";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);
			fail();
		} catch (TypingException te) {
			// as expected!!
		}
	}
	
	public void testEnhancedFor() {
		String cuText =
			"class A {\n" +
			"	void foo() {\n" +
			"		for (String s: new Object[3]) {\n" +
			"		}" +
			"	}\n" +
			"}\n";
		CompilationUnit cu = null;
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		try {
			new SemanticsChecker(sc).check(cu);
			fail();
		} catch (TypingException te) {
			// as expected!!
		}
		cuText = 
			"class A {\n" +
			"	void foo() {\n" +
			"		for (String s: new String[3]) {\n" +
			"		}" +
			"	}\n" +
			"}\n";
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		new SemanticsChecker(sc).check(cu);
		cuText = 
			"class A {\n" +
			"	void foo() {\n" +
			"		for (Object o: new String[3]) {\n" +
			"		}" +
			"	}\n" +
			"}\n";
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		
		// now test collection types!
		cuText = 
			"class A {\n" +
			"	void foo() {\n" +
			"		for (String s: new java.util.ArrayList<String>()) {\n" +
			"		}" +
			"	}\n" +
			"}\n";
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		new SemanticsChecker(sc).check(cu);
		cuText = 
			"class A {\n" +
			"	static final <E> void foo(java.util.Collection<E> c) {\n"+
			"		java.util.List<E> list = new java.util.ArrayList<E>();\n"+
			"		for (E e : c) {}\n"+
			"	}\n"+
			"}\n";
		try {
			cu = runIt(cuText).get(0);
		} catch (ParserException e) {
			fail(e.getMessage());
		}
		new SemanticsChecker(sc).check(cu);
	}
	
	public void testSwitch() throws Exception {
		String cuText = "class A{\n" +
				"	void foo(byte b) {\n" +
				"		switch (b) {\n" +
				"			case 130: break; \n" + // should fail
				"		}\n" +
				"	}\n" +
				"}\n";
		try {
			runIt(cuText);
			new SemanticsChecker(sc).checkAllCompilationUnits();
			fail("Switch-constant should be reported as out-of-bounds...");
		} catch (ModelException e) {
			// as expected.
		}
	}
	public void testSwitch2() throws Exception {
		String cuText = "class A{\n" +
				"	void foo(int i) {\n" +
				"		switch (i) {\n" +
				"			case 2L: break; \n" + // should fail (in range, but declared as long)
				"		}\n" +
				"	}\n" +
				"}\n";
		try {
			runIt(cuText);
			new SemanticsChecker(sc).checkAllCompilationUnits();
			fail("Switch-constant must not be of type Long...");
		} catch (ModelException e) {
			// as expected.
		}
	}
	public void testSwitch3() throws Exception {
		String cuText = "class A{\n" +
				"	public static final short X = 3;\n" +
				"	void foo(short s) {\n" +
				"		switch (s) {\n" +
				"			case X: break; \n" +
				"			case 33: break;" + 
				"		}\n" +
				"	}\n" +
				"}\n";
		runIt(cuText);
		new SemanticsChecker(sc).checkAllCompilationUnits();
	}
}
