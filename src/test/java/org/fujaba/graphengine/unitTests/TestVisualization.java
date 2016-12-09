package org.fujaba.graphengine.unitTests;

import org.junit.Test;

import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;

public class TestVisualization {
	
	@Test
	public void testVisualization() {
		Graph graph = getFerrymansGraph();
		ArrayList<ArrayList<PatternGraph>> patterns = new ArrayList<ArrayList<PatternGraph>>();
		patterns.add(new ArrayList<PatternGraph>()); // highest priority
		patterns.get(patterns.size() - 1).add(getEatingRule());
		patterns.add(new ArrayList<PatternGraph>()); // lower priority
		patterns.get(patterns.size() - 1).add(getTranportRule());
		patterns.get(patterns.size() - 1).add(getEmptyTranportRule());
		graph = PatternEngine.calculateReachabilityGraph(graph, patterns);
		System.out.println(graph.getNodes().size());
		long count = graph.getNodes().size();
		for (int i = 0; i < graph.getNodes().size(); ++i) {
			Node node = graph.getNodes().get(i);
			node.setAttribute("graph", i); // replace exhaustive graph-attribute
			ArrayList<String> keysToChange = new ArrayList<String>();
			ArrayList<Node> targetsToKeep = new ArrayList<Node>();
			for (String key: node.getEdges().keySet()) {
				for (Node targetNode: node.getEdges(key)) {
					keysToChange.add(key);
					targetsToKeep.add(targetNode);
				}
			}
			for (int j = 0; j < keysToChange.size(); ++j) {
				node.removeEdge(keysToChange.get(j), targetsToKeep.get(j));
				node.addEdge("{edge=" + count + "}", targetsToKeep.get(j)); // replace exhaustive edge-name
				++count;
			}
		}
		GraphEngine.prepareGraphAsJsonFileForSigmaJs(graph);
	}
	
	private Graph getFerrymansGraph() {
		Graph ferrymansGraph = new Graph();
		Node wolf = new Node(), goat = new Node(), cabbage = new Node(), ferry = new Node(), north = new Node(), south = new Node();
		ferrymansGraph.addNode(wolf).addNode(goat).addNode(cabbage).addNode(ferry).addNode(north).addNode(south);
		wolf.setAttribute("type", "Cargo").setAttribute("species", "Wolf").setAttribute("count", 1).setAttribute("existant", true).addEdge("eats", goat).addEdge("at", north);
		goat.setAttribute("type", "Cargo").setAttribute("species", "Goat").setAttribute("count", 1).setAttribute("existant", true).addEdge("eats", cabbage).addEdge("at", north);
		cabbage.setAttribute("type", "Cargo").setAttribute("species", "Cabbage").setAttribute("count", 5).setAttribute("existant", true).addEdge("at", north);
		ferry.setAttribute("type", "Ferry").setAttribute("count", 1).setAttribute("existant", true).addEdge("at", north);
		north.setAttribute("type", "Bank").setAttribute("side", "north").setAttribute("count", 1).setAttribute("existant", true).addEdge("opposite", south);
		south.setAttribute("type", "Bank").setAttribute("side", "south").setAttribute("count", 1).setAttribute("existant", true).addEdge("opposite", north);
		return ferrymansGraph;
	}
	
	private PatternGraph getEatingRule() {
		PatternNode cargoEats = new PatternNode(), cargoGetsEaten = new PatternNode(), ferry = new PatternNode(), bank = new PatternNode();
		return new PatternGraph()
		.addPatternNode(cargoEats
				.setAttributeMatchExpression("#{type} == 'Cargo'")
		.addPatternEdge(new PatternEdge()
				.setSource(cargoEats)
				.setName("at")
				.setTarget(bank)
		).addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(cargoEats)
				.setName("eats")
				.setTarget(cargoGetsEaten)
		)).addPatternNode(cargoGetsEaten.setAction("-").addPatternAttribute(new PatternAttribute()
				.setAction("-")
				.setName("type")
				.setValue("#{type} == 'Cargo'")
		).addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(cargoGetsEaten)
				.setName("at")
				.setTarget(bank)
		)).addPatternNode(ferry
				.setAttributeMatchExpression("#{type} == 'Ferry'")
		.addPatternEdge(new PatternEdge()
				.setAction("!=")
				.setSource(ferry)
				.setName("at")
				.setTarget(bank)
		)).addPatternNode(bank
				.setAttributeMatchExpression("#{type} == 'Bank'")
		);
	}
	
	private PatternGraph getTranportRule() {
		PatternNode cargo = new PatternNode(), ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode();
		return new PatternGraph()
		.addPatternNode(cargo
				.setAttributeMatchExpression("#{type} == 'Cargo'")
		.addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(cargo)
				.setName("at")
				.setTarget(bankHere)
		).addPatternEdge(new PatternEdge()
				.setAction("+")
				.setSource(cargo)
				.setName("at")
				.setTarget(bankThere)
		)).addPatternNode(ferry
				.setAttributeMatchExpression("#{type} == 'Ferry'")
		.addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankHere)
		).addPatternEdge(new PatternEdge()
				.setAction("+")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankThere)
		)).addPatternNode(bankHere
				.setAttributeMatchExpression("#{type} == 'Bank'")
		.addPatternEdge(new PatternEdge()
				.setSource(bankHere)
				.setName("opposite")
				.setTarget(bankThere)
		)).addPatternNode(bankThere
				.setAttributeMatchExpression("#{type} == 'Bank'")
		);
	}
	
	private PatternGraph getEmptyTranportRule() {
		PatternNode ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode();
		return new PatternGraph()
		.addPatternNode(ferry
				.setAttributeMatchExpression("#{type} == 'Ferry'")
		.addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankHere)
		).addPatternEdge(new PatternEdge()
				.setAction("+")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankThere)
		)).addPatternNode(bankHere
				.setAttributeMatchExpression("#{type} == 'Bank'")
		.addPatternEdge(new PatternEdge()
				.setSource(bankHere)
				.setName("opposite")
				.setTarget(bankThere)
		)).addPatternNode(bankThere
				.setAttributeMatchExpression("#{type} == 'Bank'")
		);
	}
	
}
