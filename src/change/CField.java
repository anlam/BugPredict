package change;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import treeMapping.LineDiff;
import treeMapping.MapAstNode;
import treeMapping.MapSourceFile;
import treeMapping.TreeMapper;
import utils.Pair;
import utils.PairDescendingOrder;
import utils.StringProcessor;

public class CField extends ChangeEntity {
	private static double thresholdSimilarity = 0.75;
	
	private CClass cClass;
	private int modifiers;
	private HashSet<String> sModifiers = new HashSet<String>();
	private String annotation = "";
	private String name;
	private String type;
	private Expression initializer;
	private CField mappedField = null;
	private HashSet<String> types, fields;
	private HashSet<String> literals = new HashSet<String>();
	
	@SuppressWarnings("unchecked")
	public CField(CClass cClass, FieldDeclaration field, String type, VariableDeclarationFragment fragment) {
		this.cClass = cClass;
		this.modifiers = field.getModifiers();
		for (int i = 0; i < field.modifiers().size(); i++) {
			ASTNode modifier = (ASTNode) field.modifiers().get(i);
			if (modifier.getNodeType() == ASTNode.MARKER_ANNOTATION) {
				this.annotation = ((MarkerAnnotation) modifier).getTypeName().toString();
				if (!this.annotation.toLowerCase().contains("null"))
					this.annotation = "";
			}
			else {
				this.sModifiers.add(modifier.toString());
			}
		}
		name = fragment.getName().getIdentifier();
		this.type = type;
		initializer = fragment.getInitializer();
		if (initializer != null) {
			this.vector = new HashMap<Integer, Integer>((HashMap<Integer, Integer>) initializer.getProperty(VectorVisitor.propertyVector));
			fragment.setProperty(VectorVisitor.propertyVector, null);
		}
		else
			this.vector = new HashMap<Integer, Integer>();
		computeVectorLength();
		//System.out.println("\t\t\tField: " + name + "\t" + fragment.getParent().getClass().getSimpleName());
	}

	public int getModifiers() {
		return modifiers;
	}

	public HashSet<String> getSModifiers() {
		return sModifiers;
	}

	public String getAnnotation() {
		return annotation;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getQualName() {
		return this.cClass.getSimpleName() + "." + this.name;
	}

	public String getFullQualName() {
		return this.getCClass().getFullQualName() + "." + this.name;
	}

	public String getType() {
		return type;
	}

	public Expression getInitializer() {
		return initializer;
	}

	public CField getMappedField() {
		return mappedField;
	}

	public void setMappedField(CField mappedField) {
		this.mappedField = mappedField;
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
	
	@Override
	public CFile getCFile() {
		return this.cClass.getCFile();
	}

	@Override
	public CClass getCClass() {
		return this.cClass;
	}
	
	private double[] computeSimilarity(CField other) {
		double[] sim = new double[4];
		double signature = 0;
		double body = 0;
		ArrayList<String> seq1 = StringProcessor.serialize(this.type), seq2 = StringProcessor.serialize(other.getType());
		ArrayList<Integer> lcsM = new ArrayList<Integer>(), lcsN = new ArrayList<Integer>();
		StringProcessor.doLCS(seq1, seq2, 0, 0, lcsM, lcsN);
		double simType = lcsM.size() * 2.0 / (seq1.size() + seq2.size());
		seq1 = StringProcessor.serialize(this.name); seq2 = StringProcessor.serialize(other.getName());
		lcsM = new ArrayList<Integer>(); lcsN = new ArrayList<Integer>();
		StringProcessor.doLCS(seq1, seq2, 0, 0, lcsM, lcsN);
		double simName = lcsM.size() * 2.0 / (seq1.size() + seq2.size());
		signature = (simType + 2.0 * simName) / 3.0;
		
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

	public static void setMap(CField fieldM, CField fieldN) {
		/*if (fieldM.getMappedField() != null)
			fieldM.getMappedField().setMappedField(null);
		if (fieldN.getMappedField() != null)
			fieldN.getMappedField().setMappedField(null);*/
		fieldM.setMappedField(fieldN);
		fieldN.setMappedField(fieldM);
	}
	
	public static void map(HashSet<CField> fieldsM, HashSet<CField> fieldsN,
							HashSet<CField> mappedFieldsM, HashSet<CField> mappedFieldsN) {
		HashMap<CField, HashSet<Pair>> pairsOfMethods1 = new HashMap<CField, HashSet<Pair>>();
		HashMap<CField, HashSet<Pair>> pairsOfMethods2 = new HashMap<CField, HashSet<Pair>>();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		PairDescendingOrder comparator = new PairDescendingOrder();
		for(CField cfM : fieldsM)
		{
			HashSet<Pair> pairs1 = new HashSet<Pair>();
			for(CField cfN : fieldsN)
			{
				double[] sim = cfM.computeSimilarity(cfN);
				if(sim[0] >= thresholdSimilarity && sim[1] >= thresholdSimilarity)
				{
					Pair pair = new Pair(cfM, cfN, sim[3]);
					pairs1.add(pair);
					HashSet<Pair> pairs2 = pairsOfMethods2.get(cfN);
					if(pairs2 == null)
						pairs2 = new HashSet<Pair>();
					pairs2.add(pair);
					pairsOfMethods2.put(cfN, pairs2);
					int index = Collections.binarySearch(pairs, pair, comparator);
					if(index < 0)
						pairs.add(-1-index, pair);
					else
						pairs.add(index, pair);
				}
			}
			pairsOfMethods1.put(cfM, pairs1);
		}
		while(!pairs.isEmpty())
		{
			Pair pair = pairs.get(0);
			CField cfM = (CField)pair.getObj1(), cfN = (CField)pair.getObj2();
			setMap(cfM, cfN);
			mappedFieldsM.add(cfM);
			mappedFieldsN.add(cfN);
			for(Pair p : pairsOfMethods1.get(pair.getObj1()))
				pairs.remove(p);
					for(Pair p : pairsOfMethods2.get(pair.getObj2()))
						pairs.remove(p);
		}
	}

	public static double[] mapAll(HashSet<CField> fieldsM,
								HashSet<CField> fieldsN, HashSet<CField> mappedFieldsM, HashSet<CField> mappedFieldsN) {
		int commonSize = 0, totalSize = 0;
		// map fields with same names
		HashMap<String, CField> fieldWithNameM = new HashMap<String, CField>(), fieldWithNameN = new HashMap<String, CField>();
		for (CField cf : fieldsM)
		{
			fieldWithNameM.put(cf.getName(), cf);
		}
		for (CField cf : fieldsN)
		{
			fieldWithNameN.put(cf.getName(), cf);
		}
		HashSet<String> interNames = new HashSet<String>(fieldWithNameM.keySet());
		interNames.retainAll(fieldWithNameN.keySet());
		for (String name : interNames)
		{
			CField cfM = fieldWithNameM.get(name), cfN = fieldWithNameN.get(name);
			setMap(cfM, cfN);
			mappedFieldsM.add(cfM); mappedFieldsN.add(cfN);
			fieldsM.remove(cfM); fieldsN.remove(cfN);
			commonSize++;
			totalSize++;
		}

		// map other fields
		map(fieldsM, fieldsN, mappedFieldsM, mappedFieldsN);
		commonSize += mappedFieldsM.size();
		totalSize += mappedFieldsM.size();
		
		return new double[]{commonSize, totalSize};
	}

	public void deriveChanges() {
		CField cfN = mappedField;
		boolean same = true;
		if (this.initializer == null && cfN.getInitializer() != null) {
			if (cfN.getInitializer().getNodeType() != ASTNode.NULL_LITERAL)
				same = false;
		}
		else if (this.initializer != null && cfN.getInitializer() == null) {
			if (this.initializer.getNodeType() != ASTNode.NULL_LITERAL)
				same = false;
		}
		else if (this.initializer != null && cfN.getInitializer() != null) {
			same = false;
			MapSourceFile mapFileM = this.cClass.getCFile().getSourceFile();
			MapAstNode mapNodeM = new MapAstNode(mapFileM.getFileContent(), this.initializer, mapFileM.getLineNumber(initializer.getStartPosition()));
			MapSourceFile mapFileN = cfN.cClass.getCFile().getSourceFile();
			MapAstNode mapNodeN = new MapAstNode(mapFileN.getFileContent(), cfN.getInitializer(), mapFileN.getLineNumber(cfN.getInitializer().getStartPosition()));
			LineDiff lineDiff = new LineDiff(mapNodeM, mapNodeN);
			ArrayList<ArrayList<Integer>> diff = lineDiff.doDiff();
			if (diff == null)
				same = true;
			else {
				mapNodeM.setUnChangedLines(diff.get(0));
				mapNodeN.setUnChangedLines(diff.get(1));
				TreeMapper mapper = new TreeMapper();
				mapper.map(mapNodeM, mapNodeN);
				this.tree = new HashMap<ASTNode, ArrayList<ASTNode>>(mapper.getTreeM());
				cfN.tree = new HashMap<ASTNode, ArrayList<ASTNode>>(mapper.getTreeN());
				/*System.out.println("Diff of field " + this.getFullQualName());
				System.out.println(mapper.printTree(mapNodeM.getTree(), this.initializer));
				System.out.println(mapper.printTree(mapNodeN.getTree(), cfN.getInitializer()));*/
			}
		}
		if (!same) {
			this.setCType(Type.Modified);
			cfN.setCType(Type.Modified);
		}
	}

	public void printChanges(PrintStream ps) {
		if (getCType() != Type.Unchanged) {
			ps.println("\t\t\tField: " + getName() + " --> " + (this.mappedField == null ? "null" : this.mappedField.getName()));
		}
	}
	
	@Override
	public String toString() {
		return getQualName();
	}
}
