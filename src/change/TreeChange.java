package change;

public class TreeChange {
	public byte type = -1, height = 0;
	CTree tree1, tree2;
	
	public TreeChange(byte type, byte height) {
		this.type = type;
		this.height = height;
	}
	
	public byte getType() {
		return type;
	}

	public byte getHeight() {
		return height;
	}

	public CTree getTree1() {
		return tree1;
	}

	public CTree getTree2() {
		return tree2;
	}

	@Override
	public String toString() {
		return "[[" + this.tree1 + "]|[" + this.tree2 + "]]";
	}

	public boolean isChanged() {
		return !this.tree1.equals(this.tree2);
	}

	public void abstractout() {
		if (this.tree1 != null)
			this.tree1.abstractout();
		if (this.tree2 != null)
			this.tree2.abstractout();
	}

	/*public void doIndexing() {
		if (this.tree1 != null)
			this.tree1.doIndexing();
		if (this.tree2 != null)
			this.tree2.doIndexing();
		
	}*/
}
