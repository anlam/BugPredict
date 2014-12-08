/**
 * 
 */
package data;

import java.util.List;

/**
 * @author Anh
 *
 */
public class LexemeInfo {
	public String content;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	public LexemeInfo(String tokenContent) {
		this.content = tokenContent.trim().intern();
	}
	
	public static String getLexemeList(List<LexemeInfo> lexemeList){
		StringBuffer sb = new StringBuffer();
		for(LexemeInfo lexemeInfo: lexemeList){
			sb.append(lexemeInfo.content + " ");
		}
		return sb.toString();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
//		builder.append("t=");
		builder.append(content);
//		builder.append(",");
		return builder.toString();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LexemeInfo other = (LexemeInfo) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		return true;
	}

	
}
