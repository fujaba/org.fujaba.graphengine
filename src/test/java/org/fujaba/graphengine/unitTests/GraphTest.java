package org.fujaba.graphengine.unitTests;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandler;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerCombinatorial;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * This class is for testing Graphs.
 * 
 * @author Philipp Kolodziej
 */
public class GraphTest {
	
	@Test
	public void testGraph() {
		Gson gson = GraphEngine.getGson();
		// build ferryman's problem graph:
		Graph original = getFerrymansGraph();

		String toJson = gson.toJson(original); // serialized json
		Graph fromJson = gson.fromJson(toJson, Graph.class); // deserialized graph
		String backToJson = gson.toJson(fromJson); // serialized back to json

		// original graph and deserialized graph must be isomorph:
		Assert.assertTrue(GraphEngine.isIsomorphTo(original, fromJson));
		// serialized json and re-serialized json should be identical:
		Assert.assertEquals(toJson, backToJson);

		// test graph for isomorphy and isomorphic sub-graph (expecting positive results):
		Graph graph = original.clone();
		Graph subGraph = original.clone();
		Assert.assertTrue(GraphEngine.isIsomorphTo(graph, subGraph));
		Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		do { // all nodes will be deleted here, one after another
			int randomIndex = (int)(Math.random() * subGraph.getNodes().size());
			subGraph.removeNode(subGraph.getNodes().get(randomIndex));
			Assert.assertTrue(GraphEngine.isIsomorphicSubGraph(subGraph, graph));
		} while (subGraph.getNodes().size() > 0);
		// test graph for isomorphy and isomorphic sub-graph (expecting positive results):
		subGraph = original.clone();
		Assert.assertTrue(GraphEngine.isIsomorphTo(graph, subGraph));
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
		Assert.assertTrue(GraphEngine.isIsomorphTo(graph, subGraph));
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
		Assert.assertTrue(GraphEngine.isIsomorphTo(graph,subGraph));
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
		Assert.assertTrue(GraphEngine.isIsomorphTo(graph,subGraph));
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
		Assert.assertTrue(GraphEngine.isIsomorphTo(graph, subGraph));
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
	
	static ArrayList<Graph> createTestGraphs(int nodeCount, int subGraphNodeCount, int edgeTypeCount, int edgeCountPerNode, int attributeTypeCount, int attributeValueCount, int attributeCountPerNode) {
		if (nodeCount < 0) {
			nodeCount = 0;
		}
		if (subGraphNodeCount > nodeCount) {
			subGraphNodeCount = nodeCount;
		}
		if (edgeTypeCount < 1) {
			edgeTypeCount = 1;
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
	
	static Graph constructBigGraph(int nodeCount, int edgeTypeCount, int edgeCountPerNode, int attributeTypeCount, int attributeValueCount, int attributeCountPerNode) {
		if (nodeCount < 0) {
			nodeCount = 0;
		}
		if (edgeTypeCount < 1) {
			edgeTypeCount = 1;
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
	
	@Test
	public void testIsomorphismTestGraph() {
		
		Graph one = new Graph();
		
		ArrayList<Node> as = new ArrayList<Node>();
		ArrayList<Node> bs = new ArrayList<Node>();
		ArrayList<Node> cs = new ArrayList<Node>();
		
		int size = 50;
		
		for (int i = 0; i < size; ++i) {
			as.add(new Node().setAttribute("type", "NodeTypeA").setAttribute("attra", "a"));
			bs.add(new Node().setAttribute("type", "NodeTypeB").setAttribute("attrb", "b"));
			cs.add(new Node().setAttribute("type", "NodeTypeC").setAttribute("attrc", "c"));
		}
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				as.get(i).addEdge("atob", bs.get(j));
				bs.get(i).addEdge("btoc", cs.get(j));
				cs.get(i).addEdge("ctoa", as.get(j));
			}
			one.addNode(as.get(i)).addNode(bs.get(i)).addNode(cs.get(i));
		}
		
		Graph two = one.clone();
		
		IsomorphismHandler isomorphismHandler = null;
//		isomorphismHandler = GraphEngine.getMainIsomorphismHandler();
//		isomorphismHandler = new IsomorphismHandlerCSPLowHeuristics();
//		isomorphismHandler = new IsomorphismHandlerSorting();
		isomorphismHandler = new IsomorphismHandlerCombinatorial();
	    long start = System.nanoTime();
	    Assert.assertTrue(isomorphismHandler.isIsomorphTo(two, one));
	    long duration = System.nanoTime() - start;
	    System.out.println("test ismorphic check performance: " + duration / 1e6 + "ms");
		
	}
		
//	@Test
//	public void PhilippKolodziejIsomorphismTestGraph() {
//	    Storyboard storyboard = new Storyboard();
//	    ClassModel model = new ClassModel("org.sdmlib.test.examples.reachabilitygraphs.philippkolodziejisomorphismgraphtest");
//	    Clazz a = model.createClazz("NodeTypeA")
//	        .withAttribute("attra", DataType.STRING);
//	    Clazz b = model.createClazz("NodeTypeB")
//	        .withAttribute("attrb", DataType.STRING);
//	    Clazz c = model.createClazz("NodeTypeC")
//	        .withAttribute("attrc", DataType.STRING);
//	    a.withUniDirectional(b, "atob", Cardinality.MANY);
//	    b.withUniDirectional(c, "btoc", Cardinality.MANY);
//	    c.withUniDirectional(a, "ctoa", Cardinality.MANY);
//	    storyboard.addClassDiagram(model);
//	    model.generate("src/test/java");
//	    storyboard.dumpHTML();
//    }
//
//	@Test
//	public void PhilippKolodziejIsomorphismTest() {
//		Storyboard storyboard1 = new Storyboard();
//		Storyboard storyboard2 = new Storyboard();
//		// ================================================
//		storyboard1.add("initial situation:");
//		storyboard2.add("initial situation:");
//		int size = 50;
//		ArrayList<NodeTypeA> as1 = new ArrayList<NodeTypeA>();
//		ArrayList<NodeTypeB> bs1 = new ArrayList<NodeTypeB>();
//		ArrayList<NodeTypeC> cs1 = new ArrayList<NodeTypeC>();
//		ArrayList<NodeTypeA> as2 = new ArrayList<NodeTypeA>();
//		ArrayList<NodeTypeB> bs2 = new ArrayList<NodeTypeB>();
//		ArrayList<NodeTypeC> cs2 = new ArrayList<NodeTypeC>();
//		for (int i = 0; i < size; ++i) {
//			as1.add(new NodeTypeA());
//			bs1.add(new NodeTypeB());
//			cs1.add(new NodeTypeC());
//			as2.add(new NodeTypeA());
//			bs2.add(new NodeTypeB());
//			cs2.add(new NodeTypeC());
//		}
//		for (int i = 0; i < size; ++i) {
//			as1.get(i).setAttra("a");
//			bs1.get(i).setAttrb("b");
//			cs1.get(i).setAttrc("c");
//			as2.get(i).setAttra("a");
//			bs2.get(i).setAttrb("b");
//			cs2.get(i).setAttrc("c");
//			for (int j = 0; j < size; ++j) {
//				as1.get(i).withAtob(bs1.get(j));
//				bs1.get(i).withBtoc(cs1.get(j));
//				cs1.get(i).withCtoa(as1.get(j));
//				as2.get(i).withAtob(bs2.get(j));
//				bs2.get(i).withBtoc(cs2.get(j));
//				cs2.get(i).withCtoa(as2.get(j));
//			}			
//		}
//		NodeTypeA root1 = as1.get(0);
//		NodeTypeA root2 = as2.get(0);
//	    storyboard1.addObjectDiagram(root1);
//	    storyboard2.addObjectDiagram(root1);
//	    storyboard1.add("compute certificates");
//	    storyboard1.add("compute certificates");
//	    ReachableState rs1 = new ReachableState().withGraphRoot(root1);
//	    ReachableState rs2 = new ReachableState().withGraphRoot(root2);
//	    NodeTypeACreator cc1 = new NodeTypeACreator();
//	    NodeTypeACreator cc2 = new NodeTypeACreator();
//	    IdMap map1 = cc1.createIdMap("s");
//	    IdMap map2 = cc2.createIdMap("s");
//	    map1.with(ReachabilityGraphCreator.createIdMap("rg"));
//	    map2.with(ReachabilityGraphCreator.createIdMap("rg"));
//	    String s1cert = rs1.computeCertificate(map1);
//	    String s2cert = rs2.computeCertificate(map2);
//	    storyboard1.add(s1cert);
//	    storyboard2.add(s2cert);
//	    ReachabilityGraph reachabilityGraph1 = new ReachabilityGraph()
//	       .withMasterMap(map1).withStates(rs1).withTodo(rs1).withStateMap(s1cert, rs1);
//	    ReachabilityGraph reachabilityGraph2 = new ReachabilityGraph()
//	 	   .withMasterMap(map2).withStates(rs2).withTodo(rs2).withStateMap(s2cert, rs2);
//	    long start = System.nanoTime();
//	    Assert.assertNotNull(IsomorphismComputation.calculateMatch(rs1, rs2, map1));
//	    long duration = System.nanoTime() - start;
//	    System.out.println(duration / 1e6 + "ms");
//	}
	
	@Test
	public void testGraphSplitting() {
		Graph a = getFerrymansGraph();
		Graph b = getFerrymansGraph();
		GraphEngine.isIsomorphTo(a, b);
		Graph c = getFerrymansGraph();
		GraphEngine.isIsomorphTo(b, c);
		// ok, sure graphs a, b and c are each the same
		Graph joined = new Graph();
		joined.getNodes().addAll(a.getNodes());
		Assert.assertEquals(1, GraphEngine.split(joined).size());
		// ok, the graph is connected
		joined.getNodes().addAll(b.getNodes());
		Assert.assertEquals(2, GraphEngine.split(joined).size());
		// ok, 2 times the graph are 2 connected graphs
		joined.getNodes().addAll(c.getNodes());

		ArrayList<Graph> splitted = GraphEngine.split(joined);
		Assert.assertEquals(3, splitted.size());
		// ok, 3 times the graph are 3 connected graphs

		GraphEngine.isIsomorphTo(a, splitted.get(0));
		// ok, after splitting, the first splitted part is the original graph, that was added 3 times
		GraphEngine.isIsomorphTo(a, splitted.get(1));
		// ok, after splitting, the second splitted part is the original graph, that was added 3 times
		GraphEngine.isIsomorphTo(a, splitted.get(2));
		// ok, after splitting, the third splitted part is the original graph, that was added 3 times
	}

	/* 
	 * A: 				 B:	__
	 * (1)--(2) 	vs   (1)  (2)
	 *    \/                ¯¯
	 *    /\                __
	 * (3)--(4)          (3)  (4)
	 * 						¯¯
	 */
	@Test
	public void testNormalizationNotDoingWickedStuff() {
		
	    Graph a = new Graph();
	    Node a1 = new Node();
	    Node a2 = new Node();
	    Node a3 = new Node();
	    Node a4 = new Node();
	    a1.addEdge("a", a2);
	    a1.addEdge("b", a4);
	    a3.addEdge("a", a4);
	    a3.addEdge("b", a2);
	    a.addNode(a1, a2, a3, a4);
	    
	    Graph b = new Graph();
	    Node b1 = new Node();
	    Node b2 = new Node();
	    Node b3 = new Node();
	    Node b4 = new Node();
	    b1.addEdge("a", b2);
	    b1.addEdge("b", b2);
	    b3.addEdge("a", b4);
	    b3.addEdge("b", b4);
	    b.addNode(b1, b2, b3, b4);
	    
	    Graph hell = new Graph();
	    Node hell1 = new Node();
	    Node hell2 = new Node();
	    Node hell3 = new Node();
	    Node hell4 = new Node();
	    Node hell5 = new Node();
	    Node hell6 = new Node();
	    Node hell7 = new Node();
	    Node hell8 = new Node();
	    Node hell9 = new Node();
	    Node hell10 = new Node();
	    Node hell11 = new Node();
	    Node hell12 = new Node();
	    hell1.addEdge("a", hell2);
	    hell2.addEdge("b", hell3);
	    hell2.addEdge("c", hell7);
	    hell3.addEdge("a", hell4);
	    hell4.addEdge("b", hell5);
	    hell4.addEdge("c", hell11);
	    hell5.addEdge("a", hell6);
	    hell6.addEdge("b", hell1);
	    hell6.addEdge("c", hell9);
	    hell7.addEdge("a", hell8);
	    hell8.addEdge("b", hell9);
	    hell8.addEdge("c", hell1);
	    hell9.addEdge("a", hell10);
	    hell10.addEdge("b", hell11);
	    hell10.addEdge("c", hell5);
	    hell11.addEdge("a", hell12);
	    hell12.addEdge("b", hell7);
	    hell12.addEdge("c", hell3);
	    hell.addNode(hell1, hell2, hell3, hell4, hell5, hell6, hell7, hell8, hell9, hell10, hell11, hell12);
	    ArrayList<Graph> hellishSubGraphs = new ArrayList<Graph>();
	    for (int i = 0; i < 3; ++i) {
	    	hellishSubGraphs.add(hell.clone());
	    }
	    hell.getNodes().clear();
	    for (Graph hellishSubGraph: hellishSubGraphs) {
	    	hell.getNodes().addAll(hellishSubGraph.getNodes());
	    }

//	    System.out.println();
	    String lastSerialization = null;
	    for (int i = 0; i < 100; ++i) {
	    	Collections.shuffle(a.getNodes());
	    	for (Node n: a.getNodes()) {
	    		for (String key: n.getEdges().keySet()) {
	    			Collections.shuffle(n.getEdges(key));
	    		}
	    	}
	    	a = GraphEngine.normalized(a);
	    	if (lastSerialization == null) {
	    		lastSerialization = GraphEngine.getGson().toJson(a);
	    	} else {
	    		String currentSerialization = GraphEngine.getGson().toJson(a);
	    		Assert.assertEquals(lastSerialization, currentSerialization);
	    		lastSerialization = currentSerialization;
	    	}
//		    System.out.println(lastSerialization);
	    }
//	    System.out.println();
	    lastSerialization = null;
	    for (int i = 0; i < 100; ++i) {
	    	Collections.shuffle(b.getNodes());
	    	for (Node n: b.getNodes()) {
	    		for (String key: n.getEdges().keySet()) {
	    			Collections.shuffle(n.getEdges(key));
	    		}
	    	}
	    	b = GraphEngine.normalized(b);
	    	if (lastSerialization == null) {
	    		lastSerialization = GraphEngine.getGson().toJson(b);
	    	} else {
	    		String currentSerialization = GraphEngine.getGson().toJson(b);
	    		Assert.assertEquals(lastSerialization, currentSerialization);
	    		lastSerialization = currentSerialization;
	    	}
//		    System.out.println(lastSerialization);
	    }
//	    System.out.println();
	    lastSerialization = null;
	    for (int i = 0; i < 10; ++i) {
	    	Collections.shuffle(hell.getNodes());
	    	for (Node n: hell.getNodes()) {
	    		for (String key: n.getEdges().keySet()) {
	    			Collections.shuffle(n.getEdges(key));
	    		}
	    	}
	    	hell = GraphEngine.normalized(hell);
	    	if (lastSerialization == null) {
	    		lastSerialization = GraphEngine.getGson().toJson(hell);
	    	} else {
	    		String currentSerialization = GraphEngine.getGson().toJson(hell);
	    		Assert.assertEquals(lastSerialization, currentSerialization);
	    		lastSerialization = currentSerialization;
	    	}
//		    System.out.println(lastSerialization);
	    }
	    
	}
	
	/**
	 * Method to obtain the initial situation of the ferryman's problem as a graph.
	 * @return the initial situation of the ferryman's problem as a graph.
	 */
	private Graph getFerrymansGraph() {
		Graph ferrymansGraph = new Graph();
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