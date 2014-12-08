/**
 * 
 */
package data;

/**
 * @author Anh
 *
 */
public class NetWeight {
	public String javaStr = "";
	public String netStr = "";
	public double weight = 0.0;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public NetWeight(String javaStr, String netStr, double weight) {
		super();
		this.javaStr = javaStr.intern();
		this.netStr = netStr.intern();
		this.weight = weight;
	}

	
}
