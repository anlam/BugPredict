package apidescription;

import java.io.Serializable;

public class APIDescription implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7690706006078808227L;
	
	public String id;
	public String class_url;
	public String description;
	
	public APIDescription(String id, String class_url, String description) 
	{
		this.id = id;
		this.class_url = class_url;
		this.description = description;
	}
	
	@Override
	public String toString() 
	{
		StringBuilder builder = new StringBuilder();
		//builder.append("JSONInfo [name=");
		builder.append(id);
		builder.append(",");
		builder.append(class_url);
		builder.append("");
		builder.append(description);
		return builder.toString();
	}
}
