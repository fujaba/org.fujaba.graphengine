package org.fujaba.graphengine.unitTests;

import java.io.FileReader;
import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class GraphTest {
	
	@Test
	public void testGraph() {
		Gson gson = GraphEngine.getGson();
		// ferryman's problem graph aufbauen:
		Graph original = new Graph(); // originaler graph
		Node wolf = new Node(), goat = new Node(), cabbage = new Node(), ferry = new Node(), north = new Node(), south = new Node();
		original.addNode(wolf).addNode(goat).addNode(cabbage).addNode(ferry).addNode(north).addNode(south);
		wolf.setAttribute("type", "Cargo").setAttribute("species", "Wolf").addEdge("eats", goat).addEdge("at", north);
		goat.setAttribute("type", "Cargo").setAttribute("species", "Goat").addEdge("eats", cabbage).addEdge("at", north);
		cabbage.setAttribute("type", "Cargo").setAttribute("species", "Cabbage").addEdge("at", north);
		ferry.setAttribute("type", "Ferry").addEdge("at", north);
		north.setAttribute("type", "Bank").addEdge("opposite", south);
		south.setAttribute("type", "Bank").addEdge("opposite", north);

		String toJson = gson.toJson(original); // serialisiertes json
		Graph fromJson = gson.fromJson(toJson, Graph.class); // deserialisierter graph
		String backToJson = gson.toJson(fromJson); // zurück-serialisiertes json

		// graph und zurück-deserialisierter graph müssen isomorph sein:
		Assert.assertEquals(0, original.compareTo(fromJson));
		// serialisiertes json und zurück-serialisiertes json sollten identisch sein:
		Assert.assertEquals(toJson, backToJson);

		// graph auf isomorphie und isomorphe teilgraphen testen (mit positiv-erwartetem ergebnis):
		Graph graph = original.clone();
		Graph subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
		do { // hier werden nach und nach alle knoten gelöscht
			int randomIndex = (int)(Math.random() * subGraph.getNodes().size());
			subGraph.removeNode(subGraph.getNodes().get(randomIndex));
			Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
		} while (subGraph.getNodes().size() > 0);
		// graph auf isomorphie und isomorphe teilgraphen testen (mit positiv-erwartetem ergebnis):
		subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
		int totalEdgeCount;
		do { // hier werden nach und nach alle kanten gelöscht 
			int currentEdgeCount;
			int randomNodeIndex;
			do {
				randomNodeIndex = (int)(Math.random() * subGraph.getNodes().size());
				currentEdgeCount = subGraph.getNodes().get(randomNodeIndex).getEdges().keySet().size();
			} while (currentEdgeCount < 1);
			for (String key: subGraph.getNodes().get(randomNodeIndex).getEdges().keySet()) {
				for (Node target: subGraph.getNodes().get(randomNodeIndex).getEdges().get(key)) {
					subGraph.getNodes().get(randomNodeIndex).removeEdge(key, target);
					break;
				}
				break;
			}
			Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
			totalEdgeCount = 0;
			for (Node node: subGraph.getNodes()) {
				for (String key: node.getEdges().keySet()) {
					totalEdgeCount += node.getEdges().get(key).size();
				}
			}
		} while (totalEdgeCount > 0);
		// graph auf isomorphie und isomorphe teilgraphen testen (mit positiv-erwartetem ergebnis):
		subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
		int totalAttributeCount;
		do { // hier werden nach und nach alle attribute gelöscht 
			int currentAttributeCount;
			int randomNodeIndex;
			do {
				randomNodeIndex = (int)(Math.random() * subGraph.getNodes().size());
				currentAttributeCount = subGraph.getNodes().get(randomNodeIndex).getAttributes().keySet().size();
			} while (currentAttributeCount < 1);
			for (String key: subGraph.getNodes().get(randomNodeIndex).getAttributes().keySet()) {
				subGraph.getNodes().get(randomNodeIndex).removeAttribute(key);
				break;
			}
			Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
			totalAttributeCount = 0;
			for (Node node: subGraph.getNodes()) {
				totalAttributeCount += node.getAttributes().keySet().size();
			}
		} while (totalAttributeCount > 0);

		// graph auf isomorphie und isomorphe teilgraphen testen (mit negativ-erwartetem ergebnis):
		subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
		while (subGraph.getNodes().size() > 2) { // hier werden nach und nach die meisten knoten gelöscht
			int randomIndex = (int)(Math.random() * subGraph.getNodes().size());
			subGraph.removeNode(subGraph.getNodes().get(randomIndex));
			Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
		}
		// hier kommt jetzt aber ein knoten mit bisher unbekannter kante hinein:
		subGraph.addNode(new Node().addEdge("parent", subGraph.getNodes().get(0)));
		Assert.assertFalse(graph.hasIsomorphicSubGraph(subGraph));
		// graph auf isomorphie und isomorphe teilgraphen testen (mit negativ-erwartetem ergebnis):
		subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
		while (subGraph.getNodes().size() > 2) { // hier werden nach und nach die meisten knoten gelöscht
			int randomIndex = (int)(Math.random() * subGraph.getNodes().size());
			subGraph.removeNode(subGraph.getNodes().get(randomIndex));
			Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
		}
		// hier kommt jetzt aber ein bisher unbekanntes attribute hinein:
		subGraph.getNodes().get(0).setAttribute("count", 2);
		Assert.assertFalse(graph.hasIsomorphicSubGraph(subGraph));
		// graph auf isomorphie und isomorphe teilgraphen testen (mit negativ-erwartetem ergebnis):
		subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
		// hier bekommt jetzt aber ein attribut einen anderen wert:
		for (Node n: subGraph.getNodes()) {
			for (String key: n.getAttributes().keySet()) {
				if (n.getAttribute(key) == "Wolf") {
					n.setAttribute(key, "Tiger");
				}
			}
		}
		Assert.assertFalse(graph.hasIsomorphicSubGraph(subGraph));
		
//		createTestGraphs();
		
		String filename = "src/test/resources/g5000.json";
		Graph bigBaseGraph = new Graph();
		try {
			bigBaseGraph = GraphEngine.getGson().fromJson(new JsonReader(new FileReader(filename)), Graph.class);
//			System.out.println(GraphEngine.getGson().toJson(bigBaseGraph));
		} catch (Throwable t) {
			Assert.fail();
		}
		filename = "src/test/resources/g5000s50.json";
		Graph bigSubGraph = new Graph();
		try {
			bigSubGraph = GraphEngine.getGson().fromJson(new JsonReader(new FileReader(filename)), Graph.class);
//			System.out.println(GraphEngine.getGson().toJson(bigSubGraph));
		} catch (Throwable t) {
			Assert.fail();
		}
		long beginMeasure, endMeasure;
		System.out.println("test graph15.json isomorph to itself...");
		beginMeasure = System.nanoTime();
		Assert.assertTrue(bigBaseGraph.isIsomorphTo(bigBaseGraph));
		endMeasure = System.nanoTime();
		System.out.println("measured " + ((endMeasure - beginMeasure) / 1e6) + "ms");
		System.out.println("test graph15sub10.json isomorph to itself...");
		beginMeasure = System.nanoTime();
		Assert.assertTrue(bigSubGraph.isIsomorphTo(bigSubGraph));
		endMeasure = System.nanoTime();
		System.out.println("measured " + ((endMeasure - beginMeasure) / 1e6) + "ms");
		System.out.println("test graph15.json has isomorph sub-graph graph15sub10.json...");
		beginMeasure = System.nanoTime();
		Assert.assertTrue(bigBaseGraph.hasIsomorphicSubGraph(bigSubGraph));
		endMeasure = System.nanoTime();
		System.out.println("measured " + ((endMeasure - beginMeasure) / 1e6) + "ms");
	}
	
	void createTestGraphs() {
		/*
		 * hier erzeuge ich einen großen basis-graphen
		 */
		Graph bigBaseGraph = constructBigGraph(5000, 10, 4, 10, 10, 4);
		System.out.println(GraphEngine.getGson().toJson(bigBaseGraph));
		/*
		 * ich will hier möglichst knoten löschen, so dass ein weithin zusammenhängender sub-graph entsteht
		 */
		while (bigBaseGraph.getNodes().size() > 50) {
			int minEdgeCount = Integer.MAX_VALUE;
			int minIndex = -1;
			for (int i = 0; i < bigBaseGraph.getNodes().size(); ++i) {
				int currentEdgeCount = 0;
				for (String key: bigBaseGraph.getNodes().get(i).getEdges().keySet()) {
					currentEdgeCount += bigBaseGraph.getNodes().get(i).getEdges(key).size();
				}
				if (currentEdgeCount < minEdgeCount) {
					minEdgeCount = currentEdgeCount;
					minIndex = i;
				}
			}
			bigBaseGraph.removeNode(bigBaseGraph.getNodes().get(minIndex));
		}
		System.out.println(GraphEngine.getGson().toJson(bigBaseGraph));
	}
	
//	String generateString(int length) {
//		String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
//		String result = "";
//		for (int i = 0; i < length; ++i) {
//			result += alphabet.charAt((int)(Math.random() * alphabet.length()));
//		}
//		return result;
//	}
	
	Graph constructBigGraph(int nodeCount, int edgeTypeCount, int edgeCountPerNode, int attributeTypeCount, int attributeValueCount, int attributeCountPerNode) {
		ArrayList<String> edgeTypes = new ArrayList<String>();
		for (int i = 0; i < edgeTypeCount; ++i) {
			edgeTypes.add("e" + i);
		}
		ArrayList<String> attributeTypes = new ArrayList<String>();
		for (int i = 0; i < attributeTypeCount; ++i) {
			attributeTypes.add("a" + i);
		}
		ArrayList<String> attributeValues = new ArrayList<String>();
		for (int i = 0; i < attributeValueCount; ++i) {
			attributeValues.add("v" + i);
		}
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < nodeCount; ++i) {
			Node node = new Node();
			do {
				node.setAttribute(attributeTypes.get((int)(Math.random() * attributeTypeCount)), attributeValues.get((int)(Math.random() * attributeValueCount)));
			} while (node.getAttributes().size() < attributeCountPerNode);
			nodes.add(node);
		}
		Graph graph = new Graph();
		for (int i = 0; i < nodeCount; ++i) {
			Node node = nodes.get(i);
			int achievedEdgeCount = Integer.MAX_VALUE;
			do {
				node.addEdge(edgeTypes.get((int)(Math.random() * edgeTypeCount)), nodes.get((int)(Math.random() * nodeCount)));
				achievedEdgeCount = 0;
				for (String key: node.getEdges().keySet()) {
					achievedEdgeCount += node.getEdges(key).size();
				}
			} while (achievedEdgeCount < edgeCountPerNode);
			graph.addNode(node);
		}
		return graph;
	}
	
}