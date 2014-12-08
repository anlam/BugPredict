package recoder.testsuite.semantics;
/**
 * @author  Ya Liu
 *
 */


public class TypeTest {
	private String text = null;
	
	/*integer type conversion*/	
	public String IntConversion(){
		text = 
			"class Text{"+
			"	public void testCast(){"+
			"		int i ;"+
			"		float f = 8.0f;"+
			"		long l = 9l;"+
			"		double d = 7d;"+
			"		i = f;//error!\n"+
			"		i = l;//error!\n"+
			"		i = d;//error!\n"+
			"		int[] a = new int[2];"+
			"		byte[] b = new byte[2];"+
			"		a = b;//error!\n"+	
			"	}"+
			"}";
		return text;
	}
	
	
	public String IntConversionC(){
		text = 
			"class Text{"+
			"	public void testCast(){"+
			"		int i = 1;"+
			"		float f = 8;"+
			"		long l = i;"+
			"		double d = i;"+
			"		float ff = 8.0f;"+
			"		long ll = 9l;"+
			"		double dd = 7d;"+
			"		int j;"+
			"		j = (int) ff; //correct!\n"+
			"		j = (int) ll; //correct!\n"+
			"		j = (int) dd; //correct!\n"+			
			"	}"+
			"}";
		return text;
	}
	
	/*long type conversion*/
	
	public String LongConversion(){
		text = 
			"class Text{"+
			"	public void testCast(){"+
			"		long l ;"+
			"		float f = 0.0f;"+
			"		double d = 9d;"+
			"		l = f;//error!\n"+
			"		l = d;//error!\n"+
			"		long ll = 1l;"+
			"		short s = ll;//error!\n"+
			"		char c = ll;//error!\n"+
			"		byte b = ll;//error!\n"+
			"	}"+
			"}";
		return text;
	}
	
	
	public String LongConversionC(){
		text = 
			"class Text{"+
			"	public void testCast(){"+
			"		long l = 1l;"+
			"		float f = l;"+
			"		double d = l;"+
			"		short s = (short)l;//correct!\n"+
			"		char c = (char)l;//correct!\n"+
			"		byte b = (byte)l;//correct!\n"+
			"	}"+
			"}";
		return text;
	}
	
	/*Float type conversion*/
	public String FloatConversion(){
		text = 
			"class Text{"+
			"	public void testCast(){"+
			"		int i = 0.0f;//error!\n"+
			"		float f = 2.0f;"+
			"		int sum = sum + f;//error!\n"+
			"		int times = times * f;//error!\n"+
			"		long l = f;//error!\n"+
			"		double d = 9;"+
			"		float ff = d;//error!\n"+			
			"	}"+
			"}";
		return text;
	}
	
	public String FloatConversionC(){
		text = 
			"class Text{"+
			"	public void testCast(){"+
			"		int i = (int) 0.0f;//correct!\n"+
			"		float f = 1.0f;"+
			"		int sum = (int)(sum + f);//correct!\n"+
			"		int times = (int) (times * f);//correct!\n"+
			"		double d = f ;//correct!\n"+
			"		long l = (long)f; //correct!\n"+
			"	}"+
			"}";
		return text;
	}
	
	/*byte type*/
	public String ByteConversion(){
		text = 
			"class Conversion{"+
			"	public void conver(){"+
			"		int i = 1;"+
			"		float f = 9.9f;"+
			"		long l = 89l;"+	
			"		double d = 8.8d;"+
			"		short s = 9;"+
			"		byte b;"+
			"		b = i;//error!\n"+
			"		b = f;//error!\n"+
			"		b = l;//error!\n"+
			"		b = d;//error!\n"+
			"		b = s;//error!\n"+
			"	}"+
			"}";
		return text;
	}
	
	public String ByteConversionC(){
		text = 
			"class Conversion{"+
			"	public void conver(){"+
			"		int i = 1;"+
			"		float f = 9.9f;"+
			"		long l = 89l;"+	
			"		double d = 8.8d;"+
			"		short s = 9;"+
			"		byte b;"+
			"		b = (byte)i;//correct!\n"+
			"		b = (byte)f;//correct!\n"+
			"		b = (byte)l;//correct!\n"+
			"		b = (byte)d;//correct!\n"+
			"		b = (byte)s;//correct!\n"+			
			"	}"+
			"}";
		return text;
	}
	
	/*short type*/
	public String ShortConversion(){
		text = 
			"class Conversion{"+
			"	public void conver(){"+
			"		int i = 1;"+
			"		float f = 9.9f;"+
			"		long l = 89l;"+	
			"		double d = 8.8d;"+
			"		short s;"+
			"		s = i;//error!\n"+
			"		s = f;//error!\n"+
			"		s = l;//error!\n"+
			"		s = d;//error!\n"+
			"	}"+
			"}";
		return text;
	}
	
	public String ShortConversionC(){
		text = 
			"class Conversion{"+
			"	public void conver(){"+
			"		int i = 1;"+
			"		float f = 9.9f;"+
			"		long l = 89l;"+	
			"		double d = 8.8d;"+
			"		short s;"+
			"		s = (short)i;//correct!\n"+
			"		s = (short)f;//correct!\n"+
			"		s = (short)l;//correct!\n"+
			"		s = (short)d;//correct!\n"+
			"	}"+
			"}";
		return text;
	}
	
	/*char type*/
	public String CharConversion(){
		text = 
			"class Conversion{"+
			"	public void conver(){"+
			"		int i = 1;"+
			"		float f = 9.9f;"+
			"		long l = 89l;"+	
			"		double d = 8.8d;"+
			"		char c;"+
			"		c = i;//error!\n"+
			"		c = f;//error!\n"+
			"		c = l;//error!\n"+
			"		c = d;//error!\n"+
			"	}"+
			"}";
		return text;
	}
	
	public String CharConversionC(){
		text = 
			"class Conversion{"+
			"	public void conver(){"+
			"		int i = 1;"+
			"		float f = 9.9f;"+
			"		long l = 89l;"+	
			"		double d = 8.8d;"+
			"		char c;"+
			"		c = (char)i;//correct!\n"+
			"		c = (char)f;//correct!\n"+
			"		c = (char)l;//correct!\n"+
			"		c = (char)d;//correct!\n"+
			"	}"+
			"}";
		return text;
	}
	
	
	/*String conversion*/
	public String StringConversion(){
		text = 
			"class Conversion{"+
			"	public void conver(){"+
			"		int i = 1;"+
			"		float f = 9.9f;"+
			"		long l = 89l;"+	
			"		double d = 8.8d;"+
			"		char c = (char)i;"+
			"		String s ;"+
			"		s = i;//error!\n"+
			"		s = f;//error!\n"+
			"		s = l;//error!\n"+
			"		s = d;//error!\n"+
			"		s = c;//error!\n"+		
			"	}"+
			"}";
		return text;
	}
	
	public String StringConversionC1(){
		text = 
			"class Conversion{"+
			"	public void conver(){"+
			"		int i = 1;"+
			"		float f = 9.9f;"+
			"		long l = 89l;"+	
			"		double d = 8.8d;"+
			"		String s = "+"\""+"string"+"\""+";"+
			"		s = s + i + f + l+ d ;"+
			"	}"+
			"}";
		return text;
	}
	
	/*Assignment conversion*/
	public String ClassTypeMismatch1(){
		text = 
			"class A{ }"+
			"class B extends A{ }"+
			"class Test{"+
			"	public static void main(){"+
			"		A a =new A();"+
			"		a = new B();"+
			"		B b = a;//error!\n"+			
			"	}"+
			"}";
		return text;
	}
	
	public String ClassTypeMismatch2(){
		text = 
			"class A{ }"+	
			"class Test{"+
			"	public static void main(){"+
			"		A a =new Integer(2);//error!\n"+		
			"	}"+
			"}";
		return text;
	}
	
	public String ClassTypeMismatch3(){
		text = 
			"class A{ }"+	
			"class Test{"+
			"	public static void main(){"+
			"	int[] ai;"+
			"		A a = ai;//error!\n"+		
			"	}"+
			"}";
		return text;
	}
	
	public String ClassTypeMismatch4(){
		text = 
			"class A{ }"+
			"class B extends A{ }"+
			"class Test{"+
			"	public static void main(){"+
			"		A[] a = new A[10];"+
			"		B[] b = new B[10];"+
			"		a = b;//correct!\n"+					
			"		b = a;//error!\n"+	
			"	}"+
			"}";
		return text;
	}
	
	public String ClassTypeMismatch5(){
		text = 
			"class A{ }"+
			"final class B extends A{ }"+
			"interface I{}"+
			"class Test{"+
			"	public static void main(){"+
			"		A a = new A();"+
			"		B b = new B();"+				
			"		b =(I) a;//error!\n"+	
			"	}"+
			"}";
		return text;
	}
	
	public String ClassTypeMismatchC1(){
		text = 
			"class A{ }"+
			"class B extends A{ }"+
			"class Test{"+
			"	public static void main(){"+
			"		A a =new A();"+
			"		a = new B();"+
			"		B b = (B) a;//correct!\n"+
			"	}"+
			"}";
		return text;
	}
	
	
}
