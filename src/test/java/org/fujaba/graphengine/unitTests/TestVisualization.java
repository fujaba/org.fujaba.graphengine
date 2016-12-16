package org.fujaba.graphengine.unitTests;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;

public class TestVisualization {
	
	@Test
	public void testVisualization() {
		Graph graph = getFerrymansGraph();
		ArrayList<ArrayList<PatternGraph>> patterns = new ArrayList<ArrayList<PatternGraph>>();
		patterns.add(new ArrayList<PatternGraph>()); // highest priority
		patterns.get(patterns.size() - 1).add(getCorrectTranportRule());
		patterns.get(patterns.size() - 1).add(getCorrectEmptyTranportRule());
		graph = PatternEngine.calculateReachabilityGraph(graph, patterns);
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
	
	@Test
	public void testBigGraphVisualization() {
		GraphEngine.prepareGraphAsJsonFileForSigmaJs(GraphTest.constructBigGraph(20, 1, 2, 1, 3, 1));
	}
	
	/**
	 * Method to obtain the initial situation of the ferryman's problem as a graph.
	 * @return the initial situation of the ferryman's problem as a graph.
	 */
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
	
	/**
	 * Method to obtain the (corrected) 'transport rule' of the ferryman's problem graph.
	 * Note: This rule doesn't allow the ferryman to leave two species alone, that eat each other!
	 * @return the (corrected) 'transport rule' of the ferryman's problem graph
	 */
	private PatternGraph getCorrectTranportRule() {
		PatternGraph pattern = new PatternGraph();
		PatternNode cargo = new PatternNode(), ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode(), eater = new PatternNode(), getsEaten = new PatternNode();
		pattern.addPatternNode(cargo).addPatternNode(ferry).addPatternNode(bankHere).addPatternNode(bankThere).addPatternNode(eater).addPatternNode(getsEaten);
		cargo.setAttributeMatchExpression("#{type} == 'Cargo'");
		cargo.addPatternEdge(new PatternEdge()
			.setAction("-")
			.setSource(cargo)
			.setName("at")
			.setTarget(bankHere)
		);
		cargo.addPatternEdge(new PatternEdge()
			.setAction("+")
			.setSource(cargo)
			.setName("at")
			.setTarget(bankThere)
		);
		ferry.setAttributeMatchExpression("#{type} == 'Ferry'");
		ferry.addPatternEdge(new PatternEdge()
			.setAction("-")
			.setSource(ferry)
			.setName("at")
			.setTarget(bankHere)
		);
		ferry.addPatternEdge(new PatternEdge()
			.setAction("+")
			.setSource(ferry)
			.setName("at")
			.setTarget(bankThere)
		);
		bankHere.setAttributeMatchExpression("#{type} == 'Bank'");
		bankHere.addPatternEdge(new PatternEdge()
			.setSource(bankHere)
			.setName("opposite")
			.setTarget(bankThere)
		);
		bankThere.setAttributeMatchExpression("#{type} == 'Bank'");
		
		eater.setAttributeMatchExpression("#{type} == 'Cargo'");
		eater.addPatternEdge(new PatternEdge()
				.setSource(eater)
				.setName("eats")
				.setTarget(getsEaten));
		eater.addPatternEdge(new PatternEdge()
				.setSource(eater)
				.setName("at")
				.setTarget(bankHere));
		eater.setAction("!=");
		getsEaten.setAttributeMatchExpression("#{type} == 'Cargo'");
		getsEaten.addPatternEdge(new PatternEdge()
				.setTarget(getsEaten)
				.setName("at")
				.setTarget(bankHere));
		getsEaten.setAction("!=");
		return pattern;
	}
	
	/**
	 * Method to obtain the (corrected) 'empty transport rule' of the ferryman's problem graph.
	 * Note: This rule doesn't allow the ferryman to leave two species alone, that eat each other!
	 * @return the (corrected) 'empty transport rule' of the ferryman's problem graph
	 */
	private PatternGraph getCorrectEmptyTranportRule() {
		PatternGraph pattern = new PatternGraph();
		PatternNode ferry = new PatternNode(), bankHere = new PatternNode(), bankThere = new PatternNode(), eater = new PatternNode(), getsEaten = new PatternNode();
		pattern.addPatternNode(ferry).addPatternNode(bankHere).addPatternNode(bankThere).addPatternNode(eater).addPatternNode(getsEaten);
		ferry.setAttributeMatchExpression("#{type} == 'Ferry'");
		ferry.addPatternEdge(new PatternEdge()
				.setAction("-")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankHere)
		);
		ferry.addPatternEdge(new PatternEdge()
				.setAction("+")
				.setSource(ferry)
				.setName("at")
				.setTarget(bankThere)
		);
		bankHere.setAttributeMatchExpression("#{type} == 'Bank'");
		bankHere.addPatternEdge(new PatternEdge()
				.setSource(bankHere)
				.setName("opposite")
				.setTarget(bankThere)
		);
		bankThere.setAttributeMatchExpression("#{type} == 'Bank'");
		
		eater.setAttributeMatchExpression("#{type} == 'Cargo'");
		eater.addPatternEdge(new PatternEdge()
				.setSource(eater)
				.setName("eats")
				.setTarget(getsEaten));
		eater.addPatternEdge(new PatternEdge()
				.setSource(eater)
				.setName("at")
				.setTarget(bankHere));
		eater.setAction("!=");
		getsEaten.setAttributeMatchExpression("#{type} == 'Cargo'");
		getsEaten.addPatternEdge(new PatternEdge()
				.setTarget(getsEaten)
				.setName("at")
				.setTarget(bankHere));
		getsEaten.setAction("!=");
		return pattern;
	}
	
}
