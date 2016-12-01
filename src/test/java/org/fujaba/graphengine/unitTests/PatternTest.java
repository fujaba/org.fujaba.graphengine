package org.fujaba.graphengine.unitTests;

import org.junit.Test;
import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.junit.Assert;

public class PatternTest {
	
	@Test
	public void testPattern() {
		PatternNode cargo = new PatternNode(), ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode();
		PatternGraph patternGraph = new PatternGraph()
				.addPatternNode(cargo.setAction("match").addPatternAttribute(new PatternAttribute()
						.setAction("match")
						.setName("type")
						.setValue("Cargo")
				).addPatternEdge(new PatternEdge()
						.setAction("remove")
						.setSource(cargo)
						.setName("at")
						.setTarget(bankHere)
				).addPatternEdge(new PatternEdge()
						.setAction("create")
						.setSource(cargo)
						.setName("at")
						.setTarget(bankThere)
				)).addPatternNode(ferry.setAction("match").addPatternAttribute(new PatternAttribute()
						.setAction("match")
						.setName("type")
						.setValue("Ferry")
				).addPatternEdge(new PatternEdge()
						.setAction("remove")
						.setSource(ferry)
						.setName("at")
						.setTarget(bankHere)
				).addPatternEdge(new PatternEdge()
						.setAction("create")
						.setSource(ferry)
						.setName("at")
						.setTarget(bankThere)
				)).addPatternNode(bankHere.setAction("match").addPatternAttribute(new PatternAttribute()
						.setAction("match")
						.setName("type")
						.setValue("Bank")
				).addPatternEdge(new PatternEdge()
						.setAction("match")
						.setSource(bankHere)
						.setName("opposite")
						.setTarget(bankThere)
				)).addPatternNode(bankThere.setAction("match").addPatternAttribute(new PatternAttribute()
						.setAction("match")
						.setName("type")
						.setValue("Bank")
				));
		String toJson = GraphEngine.getGson().toJson(patternGraph); // hand-made graph to json
		
		PatternGraph fromJson = GraphEngine.getGson().fromJson(toJson, PatternGraph.class); // json from hand-made to object
		String backToJson = GraphEngine.getGson().toJson(fromJson); // automaticly built object to json
		
		PatternGraph fromJson2 = GraphEngine.getGson().fromJson(backToJson, PatternGraph.class); // json from automaticly built object to object
		String backToJson2 = GraphEngine.getGson().toJson(fromJson2); // automaticly built object to json
		
		Assert.assertEquals(backToJson, backToJson2);
		// TODO: implement better test for PatternGraph comparison
		
//		System.out.println(patternGraph);
	}
	
}