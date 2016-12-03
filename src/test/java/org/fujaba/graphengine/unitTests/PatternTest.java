package org.fujaba.graphengine.unitTests;

import org.junit.Test;

import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.Match;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.junit.Assert;

/**
 * This class is for testing Patterns.
 * 
 * @author Philipp Kolodziej
 */
public class PatternTest {
	
	@Test
	public void testPatternSerialization() {
		PatternGraph patternGraph = getTranportRule();
		String toJson = GraphEngine.getGson().toJson(patternGraph); // hand-made graph to json
		
		PatternGraph fromJson = GraphEngine.getGson().fromJson(toJson, PatternGraph.class); // json from hand-made to object
		String backToJson = GraphEngine.getGson().toJson(fromJson); // automaticly built object to json
		
		PatternGraph fromJson2 = GraphEngine.getGson().fromJson(backToJson, PatternGraph.class); // json from automaticly built object to object
		String backToJson2 = GraphEngine.getGson().toJson(fromJson2); // automaticly built object to json
		
		Assert.assertEquals(backToJson, backToJson2);
		
		// TODO: implement better test for PatternGraph comparison
//		System.out.println(patternGraph);
	}
	
	@Test
	public void testPatternMatching() {
		PatternGraph transportRule = getTranportRule();
		Graph ferrymansGraph = getFerrymansGraph();
		ArrayList<Match> matches = PatternEngine.matchPattern(ferrymansGraph, transportRule, true);
		Assert.assertEquals(1, matches.size());
		
		matches = PatternEngine.matchPattern(ferrymansGraph, transportRule, false);
		Assert.assertEquals(3, matches.size());
	}
	
	@Test
	public void testApplyingMatches() {
		PatternGraph transportRule = getTranportRule();
		Graph ferrymansGraph = getFerrymansGraph();
		
		ArrayList<Match> matches = PatternEngine.matchPattern(ferrymansGraph, transportRule, false);
		
		Assert.assertEquals(3, matches.size());

		Graph wolfTransported = PatternEngine.applyMatch(matches.get(0));
		Graph goatTransported = PatternEngine.applyMatch(matches.get(1));
		Graph cabbageTransported = PatternEngine.applyMatch(matches.get(2));

		Assert.assertNotEquals(ferrymansGraph, wolfTransported);
		Assert.assertNotEquals(ferrymansGraph, goatTransported);
		Assert.assertNotEquals(ferrymansGraph, cabbageTransported);
	}
	
	@Test
	public void testCycleFreeRepetitivePatternApplication() {
		PatternGraph transportRule = getTranportRule();
		PatternGraph emptyTransportRule = getEmptyTranportRule();
		Graph ferrymansGraph = getFerrymansGraph();
		ArrayList<PatternGraph> patterns = new ArrayList<PatternGraph>();
		patterns.add(transportRule);
		patterns.add(emptyTransportRule);

		Graph result = PatternEngine.applyPatterns(ferrymansGraph, patterns, false);
		
		Assert.assertNotEquals(ferrymansGraph, result);
	}
	
	private PatternGraph getTranportRule() {
		PatternNode cargo = new PatternNode(), ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode();
		return new PatternGraph()
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
	}
	
	private PatternGraph getEmptyTranportRule() {
		PatternNode ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode();
		return new PatternGraph()
		.addPatternNode(ferry.setAction("match").addPatternAttribute(new PatternAttribute()
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
	}
	
	private Graph getFerrymansGraph() {
		Graph ferrymansGraph = new Graph(); // original graph
		Node wolf = new Node(), goat = new Node(), cabbage = new Node(), ferry = new Node(), north = new Node(), south = new Node();
		ferrymansGraph.addNode(wolf).addNode(goat).addNode(cabbage).addNode(ferry).addNode(north).addNode(south);
		wolf.setAttribute("type", "Cargo").setAttribute("species", "Wolf").addEdge("eats", goat).addEdge("at", north);
		goat.setAttribute("type", "Cargo").setAttribute("species", "Goat").addEdge("eats", cabbage).addEdge("at", north);
		cabbage.setAttribute("type", "Cargo").setAttribute("species", "Cabbage").addEdge("at", north);
		ferry.setAttribute("type", "Ferry").addEdge("at", north);
		north.setAttribute("type", "Bank").setAttribute("side", "north").addEdge("opposite", south);
		south.setAttribute("type", "Bank").setAttribute("side", "south").addEdge("opposite", north);
		return ferrymansGraph;
	}
	
}