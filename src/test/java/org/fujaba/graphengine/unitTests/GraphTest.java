package org.fujaba.graphengine.unitTests;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

public class GraphTest {
	
	@Test
	public void testGraphSerializationAndDeserialization() {
		Gson gson = GraphEngine.getGson();
		// build ferryman's problem graph
		Graph original = new Graph(); // originaler graph
		Node wolf = new Node(), goat = new Node(), cabbage = new Node(), ferry = new Node(),north = new Node(), south = new Node();
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
//			System.out.println("\n" + graph + "\nmatches\n" + subGraph);
			Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
		} while (subGraph.getNodes().size() > 0);
		// graph auf isomorphie und isomorphe teilgraphen testen (mit positiv-erwartetem ergebnis):
		subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
		int totalEdgeCount;
//		System.out.println("##### TEST ISOMORPH SUBGRAPHS (DELETING EDGES) #####");
		do {
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
//			System.out.println("\n" + graph + "\nmatches\n" + subGraph);
			Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
			totalEdgeCount = 0;
			for (Node node: subGraph.getNodes()) {
				for (String key: node.getEdges().keySet()) {
					totalEdgeCount += node.getEdges().get(key).size();
				}
			}
		} while (totalEdgeCount > 0);
		
//		// graph auf isomorphie und isomorphe teilgraphen testen (mit negativ-erwartetem ergebnis):
//		subGraph = original.clone();
//		Assert.assertTrue(graph.compareTo(subGraph) == 0);
//		Assert.assertTrue(graph.hasIsomorphicSubGraph(subGraph));
//		while (subGraph.getNodes().size() > 0) {
//			int randomIndex = (int)(Math.random() * subGraph.getNodes().size());
//			subGraph.removeNode(subGraph.getNodes().get(randomIndex));
//			Assert.assertTrue(graph.hasIsomorphicSubGraph(graph));
//			System.out.println("\n" + graph + "\nmatches\n" + subGraph);
//		}
	}
	
}