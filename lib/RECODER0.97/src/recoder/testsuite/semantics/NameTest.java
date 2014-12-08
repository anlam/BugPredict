package recoder.testsuite.semantics;


/**
 * @author Ya Liu
 *
 */


public class NameTest {

	
	public String testAmbiguousName1(){//The field i is ambiguous
		String cuText = 
			"interface I1{int i = 1;}"+
			"interface I2{int i = 0;}"+
			"class Test implements I1,I2{"+
			"	int j = i; //error!\n"+
			"}";
		return cuText;
	}
	

	public String testAmbiguousName2(){//The import declaration is ambiguous
		String cuText = 
			"import java.lang.Object;"+
			"class Object{"+
			"}";
		return cuText;		
	}
	
	
	public String testAbstractClass3(){//abstract class can't instantiated
		String cuText = 
			"abstract class A{}"+
			"class Test extends A{"+
			"	public static void main(){"+
			"		A a1 = new Test(); //correct !\n"+
			"		A a2 =  new A();//error!\n"+
			"	}"+
			"}";
		return cuText;		
	}
	
	
	public String testAccessModifier4(){//The class can be either abstract or final, not both
		String cuText = 
			"abstract final class A{}//error!\n";	
		return cuText;		
	}
	
	
	public String testClassInherited5(){//final can't have sub class
		String cuText = 
			"final class A{}"+
			"class Test extends A{"+
			"}";
		return cuText;	
	}
	
	
	//The field can only set one modifier, such as public / protected / private
	public String testAccessModifier6(){
		String cuText = 
			"class Test{"+
			"	public private int x;//error!\n"+
			"}";
		return cuText;
	}
	
	
	public String testAccessModifier7(){//The final variable can be either final or volatile, not both
		String cuText = 
			"class Test{"+
			"	final volatile int x;//error!\n"+
			"}";
		return cuText;
	}
	
	public String testFieldReference8(){//Cannot reference a field before it is defined
		String cuText = 
			"class Test{"+
			"	int x = y;//error!\n"+
			"	int y = 9;"+
			"}";
		return cuText;
	}	
	
	public String testFieldReferenceOK8(){
		String cuText = 
			"class Test{"+
			"	int x = 9;"+
			"	int y = x;"+
			"}";
		return cuText;
	}
	
	public String testAmbiguousName9(){//Field define is ambiguous
		String cuText = 
			"interface I1 {int i = 9;}"+
			"interface I2 {int i = 10;}"+
			"class Test implements I1,I2{"+
			"	int j = i;//error!\n"+
			"}";
		return cuText;
	}
	
	public String testDuplicateName10(){//Duplicate method 1
		String cuText = 
			"class Test{"+
			"	void set(){}//error!\n"+
			"	int set(){}//error!\n"+
			"}";
		return cuText;
	}
	
	public String testDuplicateName11(){//Duplicate method 2
		String cuText = 
			"abstract class Test{"+
			"	abstract void set();"+
			"	void set(){}//error!\n"+
			"}";
		return cuText;
	}
	
	public String testMethodModifier12(){//Method Modifiers
		String cuText = 
			"class Test{"+
			"	public protected void set1();//error!\n"+
			"	public private void set2();//error!\n"+
			"	private protected void set3();//error!\n"+
			"}";
		return cuText;
	}
	
	public String testMethodModifier13(){//Method Modifiers
		String cuText = 
			"abstract class Test{"+		
			"	abstract private void set2();//error!\n"+		
			"	native strictfp void set3();//error!\n"+
			"	abstract static void set4();//error!\n"+
			"	abstract final void set5();//error!\n"+
			"	abstract synchronized void set6();//error!\n"+
			"	abstract native void set7();//error!\n"+
			"}";
		return cuText;
	}
	
	public String testMethodModifierOK13(){//Method Modifiers
		String cuText = 
			"abstract class Test{"+
			"	abstract public void set1();//correct!\n"+			
			"	abstract protected void set2();//correct!\n"+		
			"}";
		return cuText;
	}

	public String testMethodModifier14(){//Method  Modifiers
		String cuText = 
			"abstract class Test{"+	
			"	abstract public protected void set2();//error!\n"+				
			"}";
		return cuText;
	}

	public String testMethodModifier15(){//Method Dupilcate Modifiers
		String cuText = 
			"interface I{"+
			"	public public void set();//error!\n"+			
			"}";
		return cuText;
	}
	public String testMethodModifier17(){//Method  Modifiers
		String cuText = 
			"interface I{"+		
			"	private void set();//error!\n"+		
			"}";
		return cuText;
	}
	
	public String testMethodModifier18(){//Method Dupilcate Modifiers
		String cuText = 
			"interface I{"+				
			"	abstract abstract void set();//error!\n"+		
			"}";
		return cuText;
	}

	public String testMethodModifier19(){//Method Modifiers
		String cuText = 
			"interface I{"+		
			"	abstract protected void set();//error!\n"+
			"}";
		return cuText;
	}
	
	public String testMethodModifier20(){//Method  Modifiers
		String cuText = 
			"interface I{"+		
			"	abstract private void set();//error!\n"+
			"}";
		return cuText;
	}
	
	public String testMethodModifier21(){//Method Modifiers
		String cuText = 
			"abstract class Test{"+		
			"	private abstract int set();//error!\n"+				
			"}";
		return cuText;
	}
	
	public String testMethodModifier22(){//Method Modifiers
		String cuText = 
			"class Test{"+		
			"	private abstract int set();//error!\n"+				
			"}";
		return cuText;
	}
	
	public String testMethodModifier23(){//Method Modifiers
		String cuText = 
			"class Test{"+		
			"	private  private public int set(){return 1;}//error!\n"+				
			"}";
		return cuText;
	}
	
	//The instance method cannot override the static method from super class
	public String testOverrideName14(){//Method override1
		String cuText = 
			"class A{"+
			"	static void set(){}"+
			"}"+
			"class Test extends A{"+			
			"	void set(){}//error!\n"+		
			"}";
		return cuText;
	}
	
	
	//Can't reduce the visibility of the inherited method from super class
	public String testOverrideName15(){//Method override2
		String cuText = 
			"public class A{"+
			"	public void set(){}"+
			"	protected void get(){}"+
			"}"+
			"class Test extends A{"+			
			"	private void set(){}//error!\n"+	
			"	private void get(){}//error!\n"+
			"}";
		return cuText;
	}
	
	public String testOverrideNameOK15(){//Method override2
		String cuText = 
			"public class A{"+
			"	public void set(){}"+
			"	protected void get(){}"+
			"}"+
			"class B extends A{"+			
			"	public void set(){}//correct!\n"+	
			"	public void get(){}//correct!\n"+
			"}"+
			"class C extends A{"+	
			"	protected void get(){}//correct!\n"+
			"}";
		return cuText;
	}
	
	public String testMethodModifier16(){//Illegal modifier for the interface method
		String cuText = 
			"interface I {"+
			"	final void m1();//error!\n"+
			"	static void m2();//error!\n"+
			"	protected void m3();//error!\n"+
			"	private void m4();//error!\n"+
			"}";			
		return cuText;
	}
	
	public String testMethodModifierOK16(){//correct modifier for the interface method
		String cuText = 
			"interface I {"+
			"	abstract void m1();//correct!\n"+	
			"	public void m2();//correct!\n"+
			"}";			
		return cuText;
	}
	
	public String testOverrideName17(){//overriding of Throws 1
		String cuText = 
			"class A {"+
			"	void m(){}"+	
			"}"+
			"class B extends A{"+
			"	public void m() throws Exception{//error!\n"+
			"		throw new Exception();"+
			"	}"+
			"}";			
		return cuText;
	}

	public String testOverrideName18(){//overriding of Throws 2
		String cuText = 
			"class A {"+
			"	void m(){}"+	
			"}"+
			"class B extends A{"+
			"	public void m() {"+
			"		throw new Exception();//error!\n"+
			"	}"+
			"}";			
		return cuText;
	}
	public String testInterfaceName19(){//interface naming
		//The public type I must be defined in its own file
		String cuText = 
			"package test;"+
			"public interface I {//error!\n"+
			"	int a;"+
			"}";			
		return cuText;
	}
	public String testInterfaceName20(){//interface naming 
		//interface can't depends on itself
		String cuText = 
			"interface I  extends I{//error!\n"+
			"	int a;"+
			"}";			
		return cuText;
	}
	
	public String testInterfaceNameOK20(){//interface naming 
		String cuText = 
			"interface I {"+
			"	void m();"+
			"}"+
			"class Test implements I{"+
			"	public final void m(){ }//error!\n"+
			"}";			
		return cuText;
	}
	
	public String testFieldReferenceName21(){
		//Cannot reference a field before it is defined
		String cuText = 
			"class Test{"+
			"	int a =  a + 1;//error!\n"+
			"}";			
		return cuText;
	}
	
	public String testDuplicateName22(){
		//The nested type Local cannot hide an enclosing type
		String cuText = 
			"class Test{"+
			"	class Local{"+
			"		class Local{ }//error!\n"+
			"	}"+
			"}";			
		return cuText;
	}
	
	public String testFieldReferenceName23(){
		//The local variable may not have been initialized
		String cuText = 
			"class Test{"+
			"	int x;"+
			"	void m(){"+
			"		int x = x;//error!\n"+
			"	}"+
			"}";			
		return cuText;
	}
	
	public String testName24(){
		//switch Only permitted int values or enum constants 
		String cuText = 
			"class Test{"+
			"	void m1(String s){"+
			"		switch(s){//error!\n"+
			"			case 1:" +
			"       		break;"+
			"		}"+
			"	}"+
			"	void m2(char c){"+
			"		switch(c){//error!\n"+
			"			case 1:" +
			"       		break;"+
			"		}"+
			"	}"+
			"	void m3(float f){"+
			"		switch(f){//error!\n"+
			"			case 1:" +
			"       		break;"+
			"		}"+
			"	}"+
			"	void m4(byte b){"+
			"		switch(b){//error!\n"+
			"			case 1:" +
			"       		break;"+
			"		}"+
			"	}"+
			"}";			
		return cuText;
	}
	
	public String testName25(){//switch statement
		//Type mismatch: cannot convert from String to int
		String cuText = 
			"class Test{"+
			"	void m(int n){"+
			"		switch(n){"+
			"			case"+"\""+"\""+":"+"//error!\n"+
			"				break;"+
			"		}"+
			"	}"+
			"}";			
		return cuText;
	}
	
	
	public String testReturnName26(){//return statement
		//Void methods cannot return a value
		String cuText = 
			"class Test{"+
			"	void m(){"+
			"		return 1;//error!\n"+
			"	}"+
			"}";			
		return cuText;
	}
	
	public String testReturnName27(){//return statement
		//can't return from static initializers
		String cuText = 
			"class Test{"+
			"	static{"+
			"		return ;//error!\n"+
			"	}"+
			"}";			
		return cuText;
	}
	
	public String testReturnName28(){//return statement
		//can't return from instance initializers
		String cuText = 
			"class Test{"+
			"	{"+
			"		return ;//error!\n"+
			"	}"+
			"}";			
		return cuText;
	}
	
	public String testStaticInitializer29(){//this in static initializer
		//Cannot use this in a static context
		String cuText = 
			"class Test{"+
			"	static{"+
			"		this.getClass() ;//error!\n"+
			"	}"+
			"}";			
		return cuText;
	}
	
	public String testStaticInitializer30(){//super in static initializer
		//Cannot use super in a static context
		String cuText = 
			"class A{"+
			"	void m(){}"+
			"}"+
			"class Test extends A{"+
			"	static{"+
			"		super.m();//error!\n"+
			"	}"+
			"}";			
		return cuText;
	}
	
	public String testThisInConstructor31(){//this in constructor
		//this must be the first statement in a constructor
		String cuText = 
			"class Test {"+
			"	int num;"+
			"	int number;"+
			"	Test(int n){"+
			"		num = n;"+
			"	}"+
			"	Test(){"+
			"		number = 9;"+
			"		this(2);//error!\n"+
			"	}"+
			"}";			
		return cuText;
	}
	
	public String testThisInConstructorOK31(){//this in constructor
		//this must be the first statement in a constructor
		String cuText = 
			"class Test {"+
			"	int num;"+
			"	int number;"+
			"	Test(int n){"+
			"		num = n;"+
			"	}"+
			"	Test(){"+
			"		this(2);//correct!\n"+
			"	}"+
			"}";			
		return cuText;
	}
	public String testAbstractClassOK32(){//correct using
		String cuText = 
			"abstract class Test {"+
			"	@Deprecated public abstract void get();"+
			"}";			
		return cuText;
	}	
	
	public String testAbstractClass33(){	
		String cuText = 
			"abstract class Test {"+
			" 	public abstract strictfp void foo2();"+	
			" 	public protected abstract void foo3();"+	
			"}";			
		return cuText;
	}

	public String testFieldOK34(){//correct using field in static context	
		String cuText = 
			"class Test {"+
			" 	Test tfield;"+
			"	static void foo(Test t){"+
			"		t.tfield = null;"+
			"	}"+
			"}";				
		return cuText;
	}	
	
	public String testFieldOK35(){//correct using field in static context	
		String cuText = 
			"class Test {"+
			" 	Test tfield;"+
			"	static void foo(Test t){"+
			"		{"+
			"			t.tfield = null;"+
			"		}"+
			"	}"+
			"}";				
		return cuText;
	}
	
	public String testField36(){
		String cuText =
			"class H{"+			
			"	H h;"+
			"	public static void go(H h){"+
			"		H.h = null;"+
			"	}"+
			"}";
		return cuText;
	}
	
	public String testDupilcateName37(){//duplicate name of method
		String cuText =
			"class Father{"+
			"	static int foo(){"+
			"		return 1;"+
			"	}"+
			"}"+
			"class Child extends Father{"+
			"	int foo(){//error!\n"+
			"		return 1;"+
			"	}"+	
			"}";
		return cuText;
	}
	
}
