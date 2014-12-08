package change;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import treeMapping.LineDiff;
import treeMapping.MapAstNode;
import treeMapping.MapSourceFile;
import treeMapping.MapVisitor;
import treeMapping.TreeMapper;
import utils.Pair;
import utils.PairDescendingOrder;

public class CInitializer extends ChangeEntity {
	private static double thresholdSimilarity = 0.75;
	private static final double thresholdBodySimilarity = 0.75;
	
	private CClass cClass;
	private String name;
	private Initializer initializer;
	private CInitializer mappedInitializer = null;
	private HashSet<String> types, fields;
	private HashSet<String> literals = new HashSet<String>();
	
	@SuppressWarnings("unchecked")
	public CInitializer(CClass cClass, int staticId, Initializer initializer) {
		this.cClass = cClass;
		this.name = "init#" + staticId;
		this.initializer = initializer;
		if (initializer != null) {
			this.vector = new HashMap<Integer, Integer>((HashMap<Integer, Integer>) initializer.getProperty(VectorVisitor.propertyVector));
			initializer.setProperty(VectorVisitor.propertyVector, null);
			computeVectorLength();
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CFile getCFile() {
		return cClass.getCFile();
	}

	public Initializer getInitializer() {
		return initializer;
	}

	public CInitializer getMappedInitializer() {
		return mappedInitializer;
	}
	
	public void setMappedInitializer(CInitializer ci) {
		this.mappedInitializer = ci;
	}

	public HashMap<Integer, Integer> getVector() {
		return vector;
	}

	public int getVectorLength() {
		return vectorLength;
	}

	public HashSet<String> getTypes() {
		return types;
	}

	public HashSet<String> getFields() {
		return fields;
	}

	public HashSet<String> getLiterals() {
		return literals;
	}

	private double[] computeSimilarity(CInitializer other) {
		double[] sim = new double[4];
		double signature = this.name.equals(other.getName()) ? 1.0 : 0.0;
		double body = 0;
		if(this.vector.size() > 0 || other.getVector().size() > 0)
			body = computeVectorSimilarity(other);
		else
			body = 1.0;
		sim[0] = signature;
		sim[1] = body;
		sim[2] = signature + body;
		sim[3] = Math.round(signature*10) + signature + body;
		
		return sim;
	}

	@Override
	public String getQualName() {
		return cClass.getSimpleName() + "." + name;
	}

	@Override
	public CClass getCClass() {
		return cClass;
	}
	
	public static void setMap(CInitializer initM, CInitializer initN) {
		/*if (initM.getMappedInitializer() != null)
			initM.getMappedInitializer().setMappedInitializer(null);
		if (initN.getMappedInitializer() != null)
			initN.getMappedInitializer().setMappedInitializer(null);*/
		initM.setMappedInitializer(initN);
		initN.setMappedInitializer(initM);
	}
	
	public static void map(HashSet<CInitializer> fieldsM, HashSet<CInitializer> fieldsN,
			HashSet<CInitializer> mappedInitsM, HashSet<CInitializer> mappedInitsN) {
		HashMap<CInitializer, HashSet<Pair>> pairsOfInits1 = new HashMap<CInitializer, HashSet<Pair>>();
		HashMap<CInitializer, HashSet<Pair>> pairsOfInits2 = new HashMap<CInitializer, HashSet<Pair>>();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		PairDescendingOrder comparator = new PairDescendingOrder();
		for(CInitializer ciM : fieldsM)
		{
			HashSet<Pair> pairs1 = new HashSet<Pair>();
			for(CInitializer ciN : fieldsN)
			{
				double[] sim = ciM.computeSimilarity(ciN);
				if(sim[0] >= thresholdSimilarity && sim[1] >= thresholdSimilarity)
				{
					Pair pair = new Pair(ciM, ciN, sim[3]);
					pairs1.add(pair);
					HashSet<Pair> pairs2 = pairsOfInits2.get(ciN);
					if(pairs2 == null)
						pairs2 = new HashSet<Pair>();
					pairs2.add(pair);
					pairsOfInits2.put(ciN, pairs2);
					int index = Collections.binarySearch(pairs, pair, comparator);
					if(index < 0)
						pairs.add(-1-index, pair);
					else
						pairs.add(index, pair);
				}
			}
			pairsOfInits1.put(ciM, pairs1);
		}
		while(!pairs.isEmpty())
		{
			Pair pair = pairs.get(0);
			CInitializer cfM = (CInitializer)pair.getObj1(), cfN = (CInitializer)pair.getObj2();
			setMap(cfM, cfN);
			mappedInitsM.add(cfM);
			mappedInitsN.add(cfN);
			for(Pair p : pairsOfInits1.get(pair.getObj1()))
				pairs.remove(p);
					for(Pair p : pairsOfInits2.get(pair.getObj2()))
						pairs.remove(p);
		}
	}

	public static double[] mapAll(HashSet<CInitializer> initsM,
			HashSet<CInitializer> initsN, HashSet<CInitializer> mappedInitsM, HashSet<CInitializer> mappedInitsN) {
		int commonSize = 0, totalSize = 0;
		map(initsM, initsN, mappedInitsM, mappedInitsN);
		commonSize += mappedInitsM.size();
		totalSize += mappedInitsM.size();

		return new double[]{commonSize, totalSize};
	}

	private String getFullQualName() {
		return getCFile().getPath() + "." + getQualName();
	}

	private boolean isChanged(MapAstNode mapNodeM, MapAstNode mapNodeN) {
		return isChanged(mapNodeM.getAstNode(), mapNodeM.getTree()) || isChanged(mapNodeN.getAstNode(), mapNodeN.getTree());
	}

	private boolean isChanged(ASTNode node, HashMap<ASTNode, ArrayList<ASTNode>> tree) {
		int status = (Integer) node.getProperty(MapVisitor.propertyStatus);
		if (status <= MapVisitor.STATUS_FULLY_CHANGED) {	// not changed --> check children
			ArrayList<ASTNode> children = tree.get(node);
			if (children != null) {
				for (ASTNode child : children) {
					if (isChanged(child, tree))
						return true;
				}
			}
			return false;
		}
		if (node instanceof Javadoc || node instanceof BlockComment || node instanceof LineComment)
			return false;
		if (node instanceof MarkerAnnotation) {
			boolean isSimpleAnnotationM = isSimpleAnnotation(node.toString());
			if (!isSimpleAnnotationM)
				return true;
			boolean isSimpleAnnotationN = true;
			if (node.getProperty(TreeMapper.propertyRevisionMap) != null) {
				isSimpleAnnotationN = isSimpleAnnotation(node.getProperty(TreeMapper.propertyRevisionMap).toString());
			}
			if (isSimpleAnnotationN)
				return false;
			return true;
		}
		return true;
	}
	
	private boolean isSimpleAnnotation(String string) {
		return !string.toLowerCase().contains("null");
	}

	public void printChanges(PrintStream ps) {
		if (getCType() != Type.Unchanged) {
			ps.println("\t\t\tField: " + getName() + " --> " + (this.mappedInitializer == null ? "null" : this.mappedInitializer.getName()));
		}
	}

	public void deriveChanges() {
		CInitializer ciN = this.mappedInitializer;
		boolean isDiffable = (computeVectorSimilarity(ciN) >= thresholdBodySimilarity );
		if (isDiffable) {
			MapSourceFile mapFileM = this.cClass.getCFile().getSourceFile();
			MapAstNode mapNodeM = new MapAstNode(mapFileM.getFileContent(), initializer, mapFileM.getLineNumber(initializer.getStartPosition()));
			MapSourceFile mapFileN = ciN.getCClass().getCFile().getSourceFile();
			MapAstNode mapNodeN = new MapAstNode(mapFileN.getFileContent(), ciN.getInitializer(), mapFileN.getLineNumber(ciN.getInitializer().getStartPosition()));
			LineDiff lineDiff = new LineDiff(mapNodeM, mapNodeN);
			ArrayList<ArrayList<Integer>> diff = lineDiff.doDiff();
			if (diff != null) {
				mapNodeM.setUnChangedLines(diff.get(0));
				mapNodeN.setUnChangedLines(diff.get(1));
				TreeMapper mapper = new TreeMapper();
				int result = mapper.map(mapNodeM, mapNodeN);
				if (result > -1) {
					this.tree = new HashMap<ASTNode, ArrayList<ASTNode>>(mapper.getTreeM());
					ciN.tree = new HashMap<ASTNode, ArrayList<ASTNode>>(mapper.getTreeN());
					isDiffable = true;
					/// TODO removeRenaming();
					if (isChanged(mapNodeM, mapNodeN)) {
						this.setCType(Type.Modified);
						ciN.setCType(Type.Modified);
						/*System.out.println("Diff of method " + this.getFullQualName());
						System.out.println(mapper.printTree(mapNodeM.getTree(), this.declaration));
						System.out.println(mapper.printTree(mapNodeN.getTree(), cmN.getDeclaration()));*/
					}
				}
			}
		}
	}
	
	public ArrayList<TreeChange> getTreeChanges() {
		return getTreeChanges(this.tree, this.initializer, this.mappedInitializer.tree, this.mappedInitializer.initializer);
	}

	@Override
	public String toString() {
		return getQualName();
	}
}
