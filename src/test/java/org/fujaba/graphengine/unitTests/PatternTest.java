package org.fujaba.graphengine.unitTests;

import org.junit.Test;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.fujaba.graphengine.pattern.PatternRules;
import org.junit.Assert;

public class PatternTest {
	
//	/**
//	 * String representing the graph of the ferryman's problem
//	 */
//	private String ferrymansProblemJSON = "{\"nodes\":[{\"id\":\"w\",\"type\":\"Cargo\",\"attributes\":[{\"name\":\"species\",\"value\":\"Wolf\"}],\"edges\":[{\"name\":\"at\",\"target\":\"north\"},{\"name\":\"eats\",\"target\":\"g\"}]},{\"id\":\"g\",\"type\":\"Cargo\",\"attributes\":[{\"name\":\"species\",\"value\":\"Goat\"}],\"edges\":[{\"name\":\"at\",\"target\":\"north\"},{\"name\":\"eats\",\"target\":\"c\"}]},{\"id\":\"c\",\"type\":\"Cargo\",\"attributes\":[{\"name\":\"species\",\"value\":\"Cabbage\"}],\"edges\":[{\"name\":\"at\",\"target\":\"north\"}]},{\"id\":\"ferry\",\"type\":\"Ferry\",\"attributes\":[],\"edges\":[{\"name\":\"at\",\"target\":\"north\"}]},{\"id\":\"north\",\"type\":\"Bank\",\"attributes\":[],\"edges\":[{\"name\":\"opposite\",\"target\":\"south\"}]},{\"id\":\"south\",\"type\":\"Bank\",\"attributes\":[],\"edges\":[{\"name\":\"opposite\",\"target\":\"north\"}]}]}";
	
	/**
	 * String representing the transport rule of the ferryman's problem
	 */
	private String ferrymansProblemTransportRuleJSON = "{\"rules\":[{\"nodes\":[{\"action\":\"match\",\"id\":\"w\",\"type\":\"Cargo\",\"attributes\":[],\"edges\":[{\"action\":\"remove\",\"name\":\"at\",\"target\":\"north\"},{\"action\":\"create\",\"name\":\"at\",\"target\":\"south\"}]},{\"action\":\"match\",\"id\":\"ferry\",\"type\":\"Ferry\",\"attributes\":[],\"edges\":[{\"action\":\"remove\",\"name\":\"at\",\"target\":\"north\"},{\"action\":\"create\",\"name\":\"at\",\"target\":\"south\"}]},{\"action\":\"match\",\"id\":\"north\",\"type\":\"Bank\",\"attributes\":[],\"edges\":[{\"action\":\"match\",\"name\":\"opposite\",\"target\":\"south\"}]},{\"action\":\"match\",\"id\":\"south\",\"type\":\"Bank\",\"attributes\":[],\"edges\":[]}]}]}";
	
	@Test
	public void testPatternDeserialization() {
		PatternRules patternRules = new PatternRules(ferrymansProblemTransportRuleJSON);
		checkFerrymansProblemTransportRule(patternRules);
	}
	
	@Test
	public void testPatternSerialization() {
		PatternRules patternRules = new PatternRules(ferrymansProblemTransportRuleJSON);
		patternRules = new PatternRules(patternRules.toString());
		checkFerrymansProblemTransportRule(patternRules);
	}	
	
	private void checkFerrymansProblemTransportRule(PatternRules patternRules) {
		Assert.assertEquals(1, patternRules.getRules().size());
		int patternGraphCount = 0;
		for (PatternGraph patternGraph: patternRules.getRules()) {
			PatternNode w = patternGraph.getPatternNode("w");
			Assert.assertEquals("match", w.getAction());
			Assert.assertEquals("w", w.getId());
			Assert.assertEquals("Cargo", w.getType());
			Assert.assertEquals(0, w.getAttributes().size());
			Assert.assertEquals(2, w.getOutgoingEdges().size());
			int firstOptionCount = 0;
			int secondOptionCount = 0;
			int thirdOptionCount = 0;
			for (PatternEdge edge: w.getOutgoingEdges()) {
				if (edge.getTarget().getId().equals("north")) {
					Assert.assertEquals("w", edge.getSource().getId());
					Assert.assertEquals("remove", edge.getAction());
					Assert.assertEquals("at", edge.getName());
					Assert.assertEquals("north", edge.getTarget().getId());
					++firstOptionCount;
				} else {
					Assert.assertEquals("w", edge.getSource().getId());
					Assert.assertEquals("create", edge.getAction());
					Assert.assertEquals("at", edge.getName());
					Assert.assertEquals("south", edge.getTarget().getId());
					++secondOptionCount;
				}
			}
			Assert.assertEquals(1, firstOptionCount);
			Assert.assertEquals(1, secondOptionCount);
			Assert.assertEquals(0, thirdOptionCount);
			Assert.assertEquals(0, w.getIngoingEdges().size());
			PatternNode ferry = patternGraph.getPatternNode("ferry");
			Assert.assertEquals("match", ferry.getAction());
			Assert.assertEquals("ferry", ferry.getId());
			Assert.assertEquals("Ferry", ferry.getType());
			Assert.assertEquals(0, ferry.getAttributes().size());
			Assert.assertEquals(2, ferry.getOutgoingEdges().size());
			firstOptionCount = 0;
			secondOptionCount = 0;
			thirdOptionCount = 0;
			for (PatternEdge edge: ferry.getOutgoingEdges()) {
				if (edge.getTarget().getId().equals("north")) {
					Assert.assertEquals("ferry", edge.getSource().getId());
					Assert.assertEquals("remove", edge.getAction());
					Assert.assertEquals("at", edge.getName());
					Assert.assertEquals("north", edge.getTarget().getId());
					++firstOptionCount;
				} else {
					Assert.assertEquals("ferry", edge.getSource().getId());
					Assert.assertEquals("create", edge.getAction());
					Assert.assertEquals("at", edge.getName());
					Assert.assertEquals("south", edge.getTarget().getId());
					++secondOptionCount;
				}
			}
			Assert.assertEquals(1, firstOptionCount);
			Assert.assertEquals(1, secondOptionCount);
			Assert.assertEquals(0, thirdOptionCount);
			Assert.assertEquals(0, ferry.getIngoingEdges().size());
			PatternNode north = patternGraph.getPatternNode("north");
			Assert.assertEquals("match", north.getAction());
			Assert.assertEquals("north", north.getId());
			Assert.assertEquals("Bank", north.getType());
			Assert.assertEquals(0, north.getAttributes().size());
			Assert.assertEquals(1, north.getOutgoingEdges().size());
			firstOptionCount = 0;
			secondOptionCount = 0;
			thirdOptionCount = 0;
			for (PatternEdge edge: north.getOutgoingEdges()) {
				Assert.assertEquals("north", edge.getSource().getId());
				Assert.assertEquals("match", edge.getAction());
				Assert.assertEquals("opposite", edge.getName());
				Assert.assertEquals("south", edge.getTarget().getId());
				++firstOptionCount;
			}
			Assert.assertEquals(1, firstOptionCount);
			Assert.assertEquals(0, secondOptionCount);
			Assert.assertEquals(0, thirdOptionCount);
			Assert.assertEquals(2, north.getIngoingEdges().size());
			firstOptionCount = 0;
			secondOptionCount = 0;
			thirdOptionCount = 0;
			for (PatternEdge edge: north.getIngoingEdges()) {
				if (edge.getSource().getId().equals("w")) {
					Assert.assertEquals("w", edge.getSource().getId());
					Assert.assertEquals("remove", edge.getAction());
					Assert.assertEquals("at", edge.getName());
					Assert.assertEquals("north", edge.getTarget().getId());
					++firstOptionCount;
				} else {
					Assert.assertEquals("ferry", edge.getSource().getId());
					Assert.assertEquals("remove", edge.getAction());
					Assert.assertEquals("at", edge.getName());
					Assert.assertEquals("north", edge.getTarget().getId());
					++secondOptionCount;
				}
			}
			Assert.assertEquals(1, firstOptionCount);
			Assert.assertEquals(1, secondOptionCount);
			Assert.assertEquals(0, thirdOptionCount);
			PatternNode south = patternGraph.getPatternNode("south");
			Assert.assertEquals("match", south.getAction());
			Assert.assertEquals("south", south.getId());
			Assert.assertEquals("Bank", south.getType());
			Assert.assertEquals(0, south.getAttributes().size());
			Assert.assertEquals(0, south.getOutgoingEdges().size());
			Assert.assertEquals(3, south.getIngoingEdges().size());
			firstOptionCount = 0;
			secondOptionCount = 0;
			thirdOptionCount = 0;
			for (PatternEdge edge: south.getIngoingEdges()) {
				if (edge.getSource().getId().equals("w")) {
					Assert.assertEquals("w", edge.getSource().getId());
					Assert.assertEquals("create", edge.getAction());
					Assert.assertEquals("at", edge.getName());
					Assert.assertEquals("south", edge.getTarget().getId());
					++firstOptionCount;
				} else if (edge.getSource().getId().equals("ferry")) {
					Assert.assertEquals("ferry", edge.getSource().getId());
					Assert.assertEquals("create", edge.getAction());
					Assert.assertEquals("at", edge.getName());
					Assert.assertEquals("south", edge.getTarget().getId());
					++secondOptionCount;
				} else {
					Assert.assertEquals("north", edge.getSource().getId());
					Assert.assertEquals("match", edge.getAction());
					Assert.assertEquals("opposite", edge.getName());
					Assert.assertEquals("south", edge.getTarget().getId());
					++thirdOptionCount;
				}
			}
			Assert.assertEquals(1, firstOptionCount);
			Assert.assertEquals(1, secondOptionCount);
			Assert.assertEquals(1, thirdOptionCount);
			++patternGraphCount;
		}
		Assert.assertEquals(1, patternGraphCount);
	}
	
}