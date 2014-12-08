/**
 * 
 */
package recoder.testsuite.semantics;


/**
 * @author Ya Liu
 *
 */

public class MethodTest{
	private String cuText = null;
	
	public String testThisInStaticMethod(){
		cuText = 
			"class Addtion {" +
			"	static int get(){return get(1);}" +
			"	private static int get(int i) { return 2*i; }" +
			"   public static void main(){"+
			"		this.get();"+
			"	}"+
			"}" ;			
		return cuText;
	}
	
	/* Can't make a static reference to the non-static field */
	public String testStaticMethodInvocation2(){
		cuText = 
			"class TestStatic {" +
			"	int i = 100;//non-static field \n" + 
			"	int a;"+
			"	static void set() {"+
			"		int j = i; //error \n"+		
			"	}"+	
			"}"; 	
		return cuText;
	}
	
	public String testStaticMethodInvocationOK3(){
		cuText = 
			"class TestStatic {" +
			"	static int i = 100;" +
			"	int a;"+
			"	static void set() {"+
			"		int j = i; //correct \n"+		
			"	}"+	
			"}"; 	
		return cuText;
	}
	
	/* abstract method can only define in an abstract class or interface*/
	public String testAbstractMethodInvocation1(){
		cuText = 
			"class IsStatic {" +
			"	abstract void is(){}"+
			"}" ;		
		return cuText;
	}
		
	public String testAbstractMethodInvocationOK2(){
		cuText = 		
			"abstract class IsStatic {" +
			"	abstract void is (){}"+
			"}" +			
			"interface TestStatic {" +
			"	abstract void is(){ }"+
			"}" ;						
		return cuText;
	}
		
	public String testAbstractMethodInvocationOK3(){
		cuText =
			"abstract class ant{"+
			"	abstract void run();"+
			"}"+
			"abstract class queen extends ant{"+
			"	void eat(){  }  //correct! \n"+
			"}";
		return cuText;
	}
	
//	public String testMethodInvocationWrong4(){//java.lang.object
//		cuText =
//			"interface I extends Cloneable { Object clone() throws CloneNotSupportedException; }"+
//			"class Animal implements I {  "+
//			"	Animal animal = new Animal();"+
//			"	class Bee { "+
//			"		void fly() throws Exception {"+
//			"			animal.clone(); //error!\n"+
//			"		}"+
//			"	}"+
//			"}";			
//		return cuText;
//	}
	
	/*correct using super*/
	public String testSuperInConstructorOK1(){
		cuText = 
			"class Parent {"+
	        "	Parent() {}"+
	        "}"+
	        "class Child extends Parent{"+
	        "	Child() {"+
	        " 		super();"+
	        " 	}"+
	        "} ";
		return cuText;
	}
	
	/*correct using this*/
	public String testThisInConstructorOK1(){
		cuText = 
			"class Naming {"+
		    "	String name;"+
		    "	Naming(String input) {"+
		    "        name = input;"+
		    "	}"+
		    "	Naming() {"+
		    "        this("+"\""+"mary"+"\""+");//correct!\n"+
		    "	}"+
		    "}";
		return cuText;
	}
	/* static reference to static method*/
	public String testStaticMethodInvocationOK4(){
		cuText = 
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
		return cuText;
	}
	
	/*static reference to non-static method*/
	public String testStaticMethodInvocation5(){
		cuText = 
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
		return cuText;
	}
	
	/*static reference to non-static method*/
	public String testStaticMethodInvocation6(){
		cuText = 
			"class Test {"+
			"	int number;"+
		    "	void nostaticset(){"+
		    "		number = 3;"+
		    "	}"+
		    "	static void testnostatic(){"+
		    "		nostaticset();//error!\n"+
		    "	}"+
		    "}";
		return cuText;
	}
	
	/*static reference to static method*/
	public String testStaticMethodInvocationOK7(){
		cuText = 
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
		return cuText;
	}
	
	/*lack of one abstract method implement*/
	public String testAbstractMethodInvocationOK4(){//not checked
		cuText = 
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
		return cuText;
	}
	/*correct using abstract and super*/
	public String testSuperInConstructorOK2(){
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
		return cuText;
	}
	
	/*abstract method in abstract class, the visibilty only can be public or protected*/
	public String testAbstractMethodInvocation5(){
		cuText =
			"abstract class IsAbstract{"+
			"	abstract final void one();//error \n"+
			"	abstract static void two();//error \n"+
			"}";
		return cuText;
	}
	
	public String testAbstractMethodInvocationOK6(){
		String cuText =
			"abstract class IsAbstract{"+
			"	abstract public void one();//correct \n"+
			"	abstract protected void two();//correct \n"+
			"}";
		return cuText;
	}
	
	
	public String testStaticMethodInvocationOK8(){
		String cuText =
			"class outer{"+
			"	outer(){int i=2;}"+
			"	protected Object clone(){"+
			"		return outer.this.hashCode();"+
			"	}"+
			"	class inner{"+
			"		void set(){	int j=19;}"+
			"	}"+
			"	public static void main(){"+
			"		outer ou=new outer();"+
			"		ou.clone();"+		
			//"   	new outer.inner.set();//error \n"+
			"	}"+
			"}";
		return cuText;
	}
	
	/*can't directly invoke super class's abstract method*/
	public String testSuperMethodInvocation3(){
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
		return cuText;
	}
	
	public String testSuperMethodInvocationOK4(){//correct 
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
		return cuText;
	}
		
	/*can't directly invoke super class's abstract method*/
	public String testSuperMethodInvocation5(){
		String cuText =
			"abstract class ant{"+
			"	abstract void run();"+
			"}"+
			"class queen extends ant{"+
			"	void run(){"+
			"		super.run();//error!\n"+
			"	}"+
			"}";
		return cuText;
	}
	
	public String testSuperMethodInvocation6(){
		String cuText =
			"abstract class ant{"+
			"	abstract void run();"+
			"}"+
			"class queen extends ant{"+
			"	void run(){"+
			"		super.hashCode();"+
			"	}"+
			"}";
		return cuText;
	}
	

	
	
	/* check expression object invoked*/
	public String testStaticMethodInvocationOK9(){
		String cuText =
			"class StringTest {"+
			"	public static void main ( ){"+
			"		String s = "+"\""+"check"+"\""+";"+
			"		if (s.endsWith("+"\""+"k"+"\""+")){//correct!\n"+
			"			s = "+"\""+"ok!"+"\""+";"+
			"		}"+
			"	}"+
			"}";
		return cuText;
	}
	
	/*correct super using*/
	public String testSuperMethodInvocationOK7(){
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
		return cuText;
	}
	
	public String testStaticMethodInvocationOK10(){
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
		return cuText;
	}
	
	/* Cannot use this in a static context*/
	public String testThisMethodInvocation2(){
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
		return cuText;
	}
	
	/*No enclosing instance of the type Hello.H1 is accessible in scope*/
	public String testThisMethodInvocation3(){
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
		return cuText;
	}
	
	/*correct using*/
	public String testMethodInvocationOK27(){
		String cuText =
			"class Hello{"+
			"	void say(){"+
			"		answer();//correct!\n"+
			"	}"+
			"	void answer(){}"+
			"}";
		return cuText;
	}
	
	/* check reference method return type*/
	public String testReturnMethodInvocation1(){
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
		return cuText;
	}
	
	public String testReturnMethodInvocation2(){
		String cuText =
			"import java.util.ArrayList;"+
			"import java.util.List;"+
			"class H{"+
			"	ArrayList getList(){return new ArrayList();}"+
			"	void foo(){"+
			"		List list = foo();"+
			"	}"+
			"}";			
		return cuText;
	}
	
	public String testReturnMethodInvocationOK3(){
		String cuText =
			"class A {"+
			"	static void meet(){ }"+
			"	static A test(){return null;}"+
			"   void test(){return;}"+
			"	public static void main ( ){"+
			"		test().meet();//correct!\n"+
			"	}"+
			"}";
		return cuText;
	}
		
}
