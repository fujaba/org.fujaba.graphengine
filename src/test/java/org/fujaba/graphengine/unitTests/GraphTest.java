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
		// build ferryman's problem graph:
		Graph original = new Graph(); // original graph
		Node wolf = new Node(), goat = new Node(), cabbage = new Node(), ferry = new Node(), north = new Node(), south = new Node();
		original.addNode(wolf).addNode(goat).addNode(cabbage).addNode(ferry).addNode(north).addNode(south);
		wolf.setAttribute("type", "Cargo").setAttribute("species", "Wolf").addEdge("eats", goat).addEdge("at", north);
		goat.setAttribute("type", "Cargo").setAttribute("species", "Goat").addEdge("eats", cabbage).addEdge("at", north);
		cabbage.setAttribute("type", "Cargo").setAttribute("species", "Cabbage").addEdge("at", north);
		ferry.setAttribute("type", "Ferry").addEdge("at", north);
		north.setAttribute("type", "Bank").addEdge("opposite", south);
		south.setAttribute("type", "Bank").addEdge("opposite", north);

		String toJson = gson.toJson(original); // serialized json
		Graph fromJson = gson.fromJson(toJson, Graph.class); // deserialized graph
		String backToJson = gson.toJson(fromJson); // serialized back to json

		// original graph and deserialized graph must be isomorph:
		Assert.assertEquals(0, original.compareTo(fromJson));
		// serialized json and re-serialized json should be identical:
		Assert.assertEquals(toJson, backToJson);

		// test graph for isomorphy and isomorphic sub-graph (expecting positive results):
		Graph graph = original.clone();
		Graph subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		do { // all nodes will be deleted here, one after another
			int randomIndex = (int)(Math.random() * subGraph.getNodes().size());
			subGraph.removeNode(subGraph.getNodes().get(randomIndex));
			Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		} while (subGraph.getNodes().size() > 0);
		// test graph for isomorphy and isomorphic sub-graph (expecting positive results):
		subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		int totalEdgeCount;
		do { // all edges will be deleted here, one after another
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
			Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
			totalEdgeCount = 0;
			for (Node node: subGraph.getNodes()) {
				for (String key: node.getEdges().keySet()) {
					totalEdgeCount += node.getEdges().get(key).size();
				}
			}
		} while (totalEdgeCount > 0);
		// test graph for isomorphy and isomorphic sub-graph (expecting positive results):
		subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		int totalAttributeCount;
		do { // all attributes will be deleted here, one after another
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
			Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
			totalAttributeCount = 0;
			for (Node node: subGraph.getNodes()) {
				totalAttributeCount += node.getAttributes().keySet().size();
			}
		} while (totalAttributeCount > 0);

		// test graph for isomorphy and isomorphic sub-graph (expecting negative results):
		subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		while (subGraph.getNodes().size() > 2) { // most nodes will be deleted here, one after another
			int randomIndex = (int)(Math.random() * subGraph.getNodes().size());
			subGraph.removeNode(subGraph.getNodes().get(randomIndex));
			Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		}
		// now we add a node with a previously unknown edge
		subGraph.addNode(new Node().addEdge("parent", subGraph.getNodes().get(0)));
		Assert.assertFalse(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		// test graph for isomorphy and isomorphic sub-graph (expecting negative results):
		subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		while (subGraph.getNodes().size() > 2) { // most nodes will be deleted here, one after another
			int randomIndex = (int)(Math.random() * subGraph.getNodes().size());
			subGraph.removeNode(subGraph.getNodes().get(randomIndex));
			Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		}
		// now we add a previously unknown attribute
		subGraph.getNodes().get(0).setAttribute("count", 2);
		Assert.assertFalse(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		// test graph for isomorphy and isomorphic sub-graph (expecting negative results):
		subGraph = original.clone();
		Assert.assertTrue(graph.compareTo(subGraph) == 0);
		Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		// now we change an attribute to a different value
		for (Node n: subGraph.getNodes()) {
			for (String key: n.getAttributes().keySet()) {
				if (n.getAttribute(key) == "Wolf") {
					n.setAttribute(key, "Tiger");
				}
			}
		}
		Assert.assertFalse(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
	}
	
	@Test
	public void testIsomorphSubgraph() {
		String filenameBaseGraph = "src/test/resources/g5000.json";
		Graph baseGraph = new Graph();
		try {
			baseGraph = GraphEngine.getGson().fromJson(new JsonReader(new FileReader(filenameBaseGraph)), Graph.class);
		} catch (Throwable t) {
			Assert.fail();
		}
		String filenameSubGraph = "src/test/resources/g5000s50.json";
		Graph subGraph = new Graph();
		try {
			subGraph = GraphEngine.getGson().fromJson(new JsonReader(new FileReader(filenameSubGraph)), Graph.class);
		} catch (Throwable t) {
			Assert.fail();
		}
		
		long beginMeasure, endMeasure;

		System.out.println("test " + filenameBaseGraph + " has isomorph sub-graph " + filenameSubGraph + "...");
		beginMeasure = System.nanoTime();
		Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, baseGraph));
		endMeasure = System.nanoTime();
		System.out.println("success in " + ((endMeasure - beginMeasure) / 1e6) + "ms");

		Graph emptyGraph = new Graph();
		System.out.println("test " + filenameBaseGraph + " has isomorph sub-graph 'empty graph'...");
		beginMeasure = System.nanoTime();
		Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(emptyGraph, baseGraph));
		endMeasure = System.nanoTime();
		System.out.println("success in " + ((endMeasure - beginMeasure) / 1e6) + "ms");

		Graph singleNodedGraph = new Graph().addNode(baseGraph.getNodes().get(0).clone());
		System.out.println("test " + filenameBaseGraph + " has isomorph sub-graph 'one-noded graph'...");
		beginMeasure = System.nanoTime();
		Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(singleNodedGraph, baseGraph));
		endMeasure = System.nanoTime();
		System.out.println("success in " + ((endMeasure - beginMeasure) / 1e6) + "ms");

		for (int i = 0; i < 10; ++i) {
			int     nodeCount             = 500,//500, // extreme high node count sucks (if edge and attr count are low)
					subGraphNodeCount     = 15,//(int)(Math.random() * nodeCount),
					edgeTypeCount         = 3,//(int)(Math.random() * 9) + 1  + 2,
					edgeCountPerNode      = 2,//(int)(Math.random() * 3) + 1  + 2, // extreme low edge count sucks
					attributeTypeCount    = 3,//(int)(Math.random() * 9) + 1  + 2,
					attributeValueCount   = 10,//(int)(Math.random() * 10)     + 2,
					attributeCountPerNode = 2;//(int)(Math.random() * 4)      + 2; // extreme low attr count sucks
			System.out.println("\ncreateTestGraphs("
			+ "nodeCount=" + nodeCount + ", " 
			+ "subGraphNodeCount=" + subGraphNodeCount + ", " 
			+ "edgeTypeCount=" + edgeTypeCount + ", " 
			+ "edgeCountPerNode=" + edgeCountPerNode + ", " 
			+ "attributeTypeCount=" + attributeTypeCount + ", " 
			+ "attributeValueCount=" + attributeValueCount + ", " 
			+ "attributeCountPerNode=" + attributeCountPerNode + "):");
			ArrayList<Graph> testGraphs = createTestGraphs(nodeCount, subGraphNodeCount, edgeTypeCount, edgeCountPerNode, attributeTypeCount, attributeValueCount, attributeCountPerNode);
			System.out.println("test random base-graph has isomorph sub-graph...");
//			System.out.println("base-graph");
//			System.out.println(testGraphs.get(0));
//			System.out.println("sub-graph");
//			System.out.println(testGraphs.get(1));
			beginMeasure = System.nanoTime();
			Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(testGraphs.get(1), testGraphs.get(0)));
			endMeasure = System.nanoTime();
			System.out.println("success in " + ((endMeasure - beginMeasure) / 1e6) + "ms");
		}
	}
	
	ArrayList<Graph> createTestGraphs(int nodeCount, int subGraphNodeCount, int edgeTypeCount, int edgeCountPerNode, int attributeTypeCount, int attributeValueCount, int attributeCountPerNode) {
		if (nodeCount < 0) {
			nodeCount = 0;
		}
		if (subGraphNodeCount > nodeCount) {
			subGraphNodeCount = nodeCount;
		}
		if (edgeTypeCount < 1) {
			edgeTypeCount = 1;
		}
		while (edgeTypeCount < nodeCount * edgeCountPerNode) {
			++edgeTypeCount;
		}
		if (edgeCountPerNode < 0) {
			edgeCountPerNode = 0;
		}
		if (edgeCountPerNode == 0 && nodeCount > 1) {
			edgeCountPerNode = 1;
		}
		if (attributeTypeCount < 1) {
			attributeTypeCount = 1;
		}
		if (attributeTypeCount < attributeCountPerNode) {
			attributeTypeCount = attributeCountPerNode;
		}
		if (attributeValueCount < 1) {
			attributeValueCount = 1;
		}
		if (attributeCountPerNode < 0) {
			attributeCountPerNode = 0;
		}
		/*
		 * here i build a big base graph
		 */ 
		Graph baseGraph;
		do {
			baseGraph = constructBigGraph(nodeCount, edgeTypeCount, edgeCountPerNode, attributeTypeCount, attributeValueCount, attributeCountPerNode);
		} while (false/*!baseGraph.isConnected()*/);
		/*
		 * here i try to delete nodes that will leave a still connected graph
		 */
		Graph subGraph;
		do {
			subGraph = baseGraph.clone();
			while (subGraph.getNodes().size() > subGraphNodeCount) {
				int minEdgeCount = Integer.MAX_VALUE;
				ArrayList<Integer> minIndizes = new ArrayList<Integer>();
				for (int i = 0; i < subGraph.getNodes().size(); ++i) {
					int currentEdgeCount = 0;
					for (String key: subGraph.getNodes().get(i).getEdges().keySet()) {
						currentEdgeCount += subGraph.getNodes().get(i).getEdges(key).size();
					}
					if (currentEdgeCount <= minEdgeCount) {
						if (currentEdgeCount < minEdgeCount) {
							minIndizes = new ArrayList<Integer>();
							minIndizes.add(i);
						} else {
							minIndizes.add(i);
						}
						minEdgeCount = currentEdgeCount;
					}
				}
				subGraph.removeNode(subGraph.getNodes().get(minIndizes.get((int)(Math.random() * minIndizes.size()))));
			}
		} while (false/*!subGraph.isConnected()*/);
		ArrayList<Graph> result = new ArrayList<Graph>();
		result.add(baseGraph);
		result.add(subGraph);
		return result;
	}
	
	Graph constructBigGraph(int nodeCount, int edgeTypeCount, int edgeCountPerNode, int attributeTypeCount, int attributeValueCount, int attributeCountPerNode) {
		if (nodeCount < 0) {
			nodeCount = 0;
		}
		if (edgeTypeCount < 1) {
			edgeTypeCount = 1;
		}
		while (edgeTypeCount < nodeCount * edgeCountPerNode) {
			++edgeTypeCount;
		}
		if (edgeCountPerNode < 0) {
			edgeCountPerNode = 0;
		}
		if (edgeCountPerNode == 0 && nodeCount > 1) {
			edgeCountPerNode = 1;
		}
		if (attributeTypeCount < 1) {
			attributeTypeCount = 1;
		}
		if (attributeTypeCount < attributeCountPerNode) {
			attributeTypeCount = attributeCountPerNode;
		}
		if (attributeValueCount < 1) {
			attributeValueCount = 1;
		}
		if (attributeCountPerNode < 0) {
			attributeCountPerNode = 0;
		}
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
			if (attributeCountPerNode > 0) {
				do {
					node.setAttribute(attributeTypes.get((int)(Math.random() * attributeTypeCount)), attributeValues.get((int)(Math.random() * attributeValueCount)));
				} while (node.getAttributes().size() < attributeCountPerNode);
			}
			nodes.add(node);
		}
		Graph graph = new Graph();
		for (int i = 0; i < nodeCount; ++i) {
			Node node = nodes.get(i);
			if (edgeCountPerNode > 0) {
				int achievedEdgeCount = Integer.MAX_VALUE;
				do {
					node.addEdge(edgeTypes.get((int)(Math.random() * edgeTypeCount)), nodes.get((int)(Math.random() * nodeCount)));
					achievedEdgeCount = 0;
					for (String key: node.getEdges().keySet()) {
						achievedEdgeCount += node.getEdges(key).size();
					}
				} while (achievedEdgeCount < edgeCountPerNode);
			}
			graph.addNode(node);
		}
		return graph;
	}
	
}