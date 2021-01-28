package projet;

import java.util.HashMap;

public class Main {

	public static void main(String[] args) throws Exception {
		EngineStatique engineStatique = new EngineStatique();
		Transformation transformation = new Transformation();
		EngineParallele engineParallele = new EngineParallele();
		
		HashMap<String, Object> tags = new HashMap<String, Object>();
		tags.put("n",5);
		tags.put("n2",5);
		tags.put("n3", 4);
		tags.put("n4", 2);
		tags.put("puiss",3);
		tags.put("x", 100);
		Calcul calc = new Calcul();
		
		
		//engineStatique.execute("model/Calcul.xmi", calc, tags);
		//engineStatique.execute("model/newCalculSeq.xmi", calc, tags);
		//engineStatique.execute("model/newCalculSeqComplique.xmi", calc, tags);
		
		
		//transformation.transfo("model/Calcul.xmi");
		//transformation.transfo("model/newCalculSeq.xmi");
		transformation.transfo("model/newCalculSeqComplique.xmi");

		engineParallele.execute("model/CalculPara.xmi", calc, tags);
		
	}

}
