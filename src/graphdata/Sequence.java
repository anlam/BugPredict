/**
 * 
 */
package graphdata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import utils.HashUtils;

/**
 * @author Anh
 *
 */
public class Sequence implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3459786858743375299L;
	public List<Node> nodeSequence;
	int count;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	
	public Sequence(List<Node> nodeSequence) {
		this.nodeSequence.addAll(nodeSequence);
		this.count = 1;
	}
	
	public Sequence(List<Node> nodeSequence, int count) {
		if (nodeSequence!=null)
		{
			this.nodeSequence = new ArrayList<Node>();
		    this.nodeSequence.addAll(nodeSequence);
		}
		this.count = count;
	}


	public void incCount(){
		count++;
	}
	
	public void decCount(){
		count--;
	}
	public void mergeCount(int otherCount){
		count += otherCount;
	}


	public int level1HashCode() {
		return HashUtils.getLevel1HashCode(this.getIdxSequence());
	}

	public int level2HashCode() {
		return HashUtils.getLevel2HashCode(this.getIdxSequence());
	}
	

	public List<Integer> getIdxSequence(){
		List<Integer> idxSequence = new ArrayList<Integer>();
		for (Node node:nodeSequence){
			idxSequence.add(node.content);
		}
		return idxSequence;
	}
	public boolean roleEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sequence other = (Sequence) obj;
		if (nodeSequence == null) {
			if (other.nodeSequence != null)
				return false;
		} else 
		{
			int nodeListSize = this.nodeSequence.size();
			if (nodeListSize!=other.nodeSequence.size())
				return false;
			else 
				for (int i=0; i<nodeListSize;i++){
					if (!this.nodeSequence.get(i).roleEquals(other.nodeSequence.get(i)))
						return false;
				}
		}
		return true;
	}

	public String getSimpleString(IncGraphDB graphDB){
		StringBuilder builder = new StringBuilder();
//		builder.append("Sequence [nodeSequence=");
		builder.append(getNodeSequenceString(graphDB));
//		builder.append(", count=");
//		builder.append(count);
//		builder.append("]");
		return builder.toString();
	}
	
	private String getNodeSequenceString(IncGraphDB graphDB){
		StringBuilder builder = new StringBuilder();
		for (Node node: this.nodeSequence){
			String content = graphDB.idxTokenMap.get(node.content);
			content = content.replaceAll("\\s", "");
			builder.append(content + " ");
		}
		return builder.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Sequence [nodeSequence=");
		builder.append(nodeSequence);
		builder.append(", count=");
		builder.append(count);
		builder.append("]");
		return builder.toString();
	}
	
	
}
