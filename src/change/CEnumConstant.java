package change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import treeMapping.LineDiff;
import treeMapping.MapAstNode;
import treeMapping.MapSourceFile;
import treeMapping.TreeMapper;
import utils.Pair;
import utils.PairDescendingOrder;
import utils.StringProcessor;

public class CEnumConstant extends ChangeEntity {
	private static double thresholdSimilarity = 0.75;
	
	private CClass cClass;
	private HashSet<String> sModifiers = new HashSet<String>();
	private String annotation = "";
	private String name;
	private EnumConstantDeclaration declaration;
	private CEnumConstant mappedEnumConstant = null;
	
	@SuppressWarnings("unchecked")
	public CEnumConstant(CClass cClass, EnumConstantDeclaration enc) {
		this.cClass = cClass;
		this.declaration = enc;
		this.name = enc.getName().getIdentifier();
		for (int i = 0; i < enc.modifiers().size(); i++) {
			ASTNode modifier = (ASTNode) enc.modifiers().get(i);
			if (modifier.getNodeType() == ASTNode.MARKER_ANNOTATION) {
				this.annotation = ((MarkerAnnotation) modifier).getTypeName().toString();
				if (!this.annotation.toLowerCase().contains("null"))
					this.annotation = "";
			}
			else {
				this.sModifiers.add(modifier.toString());
			}
		}
		this.vector = new HashMap<Integer, Integer>((HashMap<Integer, Integer>) enc.getProperty(VectorVisitor.propertyVector));
		enc.setProperty(VectorVisitor.propertyVector, null);
		this.vector = new HashMap<Integer, Integer>();
		computeVectorLength();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public CFile getCFile() {
		return this.cClass.getCFile();
	}

	@Override
	public String getQualName() {
		return this.cClass.getFullQualName() + "." + this.name;
	}

	@Override
	public CClass getCClass() {
		return cClass;
	}

	public CEnumConstant getMappedEnumConstant() {
		return mappedEnumConstant;
	}

	public void setMappedEnumConstant(CEnumConstant mappedEnumConstant) {
		this.mappedEnumConstant = mappedEnumConstant;
	}
	
	private double[] computeSimilarity(CEnumConstant other) {
		double[] sim = new double[4];
		double signature = 0;
		double body = 0;
		ArrayList<String> seq1 = StringProcessor.serialize(this.name), seq2 = StringProcessor.serialize(other.getName());
		ArrayList<Integer> lcsM = new ArrayList<Integer>(), lcsN = new ArrayList<Integer>();
		StringProcessor.doLCS(seq1, seq2, 0, 0, lcsM, lcsN);
		double simName = lcsM.size() * 2.0 / (seq1.size() + seq2.size());
		signature = simName;
		
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

	public static void setMap(CEnumConstant constM, CEnumConstant constN) {
		if (constM.getMappedEnumConstant() != null)
			constM.getMappedEnumConstant().setMappedEnumConstant(null);
		if (constN.getMappedEnumConstant() != null)
			constN.getMappedEnumConstant().setMappedEnumConstant(null);
		constM.setMappedEnumConstant(constN);
		constN.setMappedEnumConstant(constM);
	}
	
	public static void map(HashSet<CEnumConstant> constsM, HashSet<CEnumConstant> constsN,
							HashSet<CEnumConstant> mappedConstsM, HashSet<CEnumConstant> mappedConstsN) {
		HashMap<CEnumConstant, HashSet<Pair>> pairsOfConsts1 = new HashMap<CEnumConstant, HashSet<Pair>>();
		HashMap<CEnumConstant, HashSet<Pair>> pairsOfConsts2 = new HashMap<CEnumConstant, HashSet<Pair>>();
		ArrayList<Pair> pairs = new ArrayList<Pair>();
		PairDescendingOrder comparator = new PairDescendingOrder();
		for(CEnumConstant ceM : constsM)
		{
			HashSet<Pair> pairs1 = new HashSet<Pair>();
			for(CEnumConstant ceN : constsN)
			{
				double[] sim = ceM.computeSimilarity(ceN);
				if(sim[0] >= thresholdSimilarity && sim[1] >= thresholdSimilarity)
				{
					Pair pair = new Pair(ceM, ceN, sim[3]);
					pairs1.add(pair);
					HashSet<Pair> pairs2 = pairsOfConsts2.get(ceN);
					if(pairs2 == null)
						pairs2 = new HashSet<Pair>();
					pairs2.add(pair);
					pairsOfConsts2.put(ceN, pairs2);
					int index = Collections.binarySearch(pairs, pair, comparator);
					if(index < 0)
						pairs.add(-1-index, pair);
					else
						pairs.add(index, pair);
				}
			}
			pairsOfConsts1.put(ceM, pairs1);
		}
		while(!pairs.isEmpty())
		{
			Pair pair = pairs.get(0);
			CEnumConstant ceM = (CEnumConstant)pair.getObj1(), ceN = (CEnumConstant)pair.getObj2();
			setMap(ceM, ceN);
			mappedConstsM.add(ceM);
			mappedConstsN.add(ceN);
			for(Pair p : pairsOfConsts1.get(pair.getObj1()))
				pairs.remove(p);
					for(Pair p : pairsOfConsts2.get(pair.getObj2()))
						pairs.remove(p);
		}
	}

	public static double[] mapAll(HashSet<CEnumConstant> constsM, HashSet<CEnumConstant> constsN, 
			HashSet<CEnumConstant> mappedConstsM, HashSet<CEnumConstant> mappedConstsN) {
		int commonSize = 0, totalSize = 0;
		// map fields with same names
		HashMap<String, CEnumConstant> constWithNameM = new HashMap<String, CEnumConstant>(), constWithNameN = new HashMap<String, CEnumConstant>();
		for (CEnumConstant cf : constsM)
		{
			constWithNameM.put(cf.getName(), cf);
		}
		for (CEnumConstant cf : constsN)
		{
			constWithNameN.put(cf.getName(), cf);
		}
		HashSet<String> interNames = new HashSet<String>(constWithNameM.keySet());
		interNames.retainAll(constWithNameN.keySet());
		for (String name : interNames)
		{
			CEnumConstant cfM = constWithNameM.get(name), cfN = constWithNameN.get(name);
			setMap(cfM, cfN);
			mappedConstsM.add(cfM); mappedConstsN.add(cfN);
			constsM.remove(cfM); constsN.remove(cfN);
			commonSize++;
			totalSize++;
		}

		// map other fields
		map(constsM, constsN, mappedConstsM, mappedConstsN);
		commonSize += mappedConstsM.size();
		totalSize += mappedConstsM.size();
		
		return new double[]{commonSize, totalSize};
	}

	public void deriveChanges() {
		CEnumConstant cfN = mappedEnumConstant;
		MapSourceFile mapFileM = this.cClass.getCFile().getSourceFile();
		MapAstNode mapNodeM = new MapAstNode(mapFileM.getFileContent(), this.declaration, mapFileM.getLineNumber(declaration.getStartPosition()));
		MapSourceFile mapFileN = cfN.getCClass().getCFile().getSourceFile();
		MapAstNode mapNodeN = new MapAstNode(mapFileN.getFileContent(), cfN.declaration, mapFileN.getLineNumber(cfN.declaration.getStartPosition()));
		LineDiff lineDiff = new LineDiff(mapNodeM, mapNodeN);
		ArrayList<ArrayList<Integer>> diff = lineDiff.doDiff();
		if (diff != null) {
			mapNodeM.setUnChangedLines(diff.get(0));
			mapNodeN.setUnChangedLines(diff.get(1));
			TreeMapper mapper = new TreeMapper();
			mapper.map(mapNodeM, mapNodeN);
			this.setCType(Type.Modified);
			cfN.setCType(Type.Modified);
			/*System.out.println("Diff of field " + this.getFullQualName());
			System.out.println(mapper.printTree(mapNodeM.getTree(), this.initializer));
			System.out.println(mapper.printTree(mapNodeN.getTree(), cfN.getInitializer()));*/
		}
	}

}
