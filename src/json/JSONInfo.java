/**
 * 
 */
package json;

import java.io.Serializable;

/**
 * @author Anh
 *
 */
public class JSONInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3673191169576544849L;
	
	public String name="";
	public String shortdesc="";
	public String desc = "";
	public String status = "";
	public String topics = "";
	public String language = "";
	public String audience = "";
	public String categories = "";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public JSONInfo(String name, String shortdesc, String desc, String status,
			String topics, String language, String audience, String categories) {
		this.name = name;
		this.shortdesc = shortdesc;
		this.desc = desc;
		this.status = status;
		this.topics = topics;
		this.language = language;
		this.audience = audience;
		this.categories = categories;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("JSONInfo [name=");
		builder.append(name);
		builder.append(", shortdesc=");
		builder.append(shortdesc);
		builder.append(", desc=");
		builder.append(desc);
		builder.append(", status=");
		builder.append(status);
		builder.append(", topics=");
		builder.append(topics);
		builder.append(", language=");
		builder.append(language);
		builder.append(", audience=");
		builder.append(audience);
		builder.append(", categories=");
		builder.append(categories);
		builder.append("]");
		return builder.toString();
	}

	
	
}
