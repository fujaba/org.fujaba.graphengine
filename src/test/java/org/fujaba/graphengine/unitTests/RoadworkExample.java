package org.fujaba.graphengine.unitTests;

import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.Match;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerCombinatorial;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.junit.Assert;
import org.junit.Test;

public class RoadworkExample {
	Graph getStartGraph() {
		Graph g = new Graph();
		
		Node n1 = new Node().setAttribute("type", "road");
		Node n2 = new Node().setAttribute("type", "road");
		Node n3 = new Node().setAttribute("type", "road");
		Node n4 = new Node().setAttribute("type", "road");
		Node n5 = new Node().setAttribute("type", "road");
		Node n6 = new Node().setAttribute("type", "road");
		Node n7 = new Node().setAttribute("type", "road");
		
		Node s1 = new Node().setAttribute("type", "road");
		Node s2 = new Node().setAttribute("type", "road");
		Node s6 = new Node().setAttribute("type", "road");
		Node s7 = new Node().setAttribute("type", "road");
		
		g.addNode(n1, n2, n3, n4, n5, n6, n7,
		          s1, s2,             s6, s7);
		
		n7.addEdge("west", n6);
		n6.addEdge("west", n5);
		n5.addEdge("west", n4);
		n4.addEdge("west", n3);
		n3.addEdge("west", n2);
		n2.addEdge("west", n1);
		
		s1.addEdge("east", s2);
		s2.addEdge("east", n3);
		n3.addEdge("east", n4);
		n4.addEdge("east", n5);
		n5.addEdge("east", s6);
		s6.addEdge("east", s7);
		
		//      Node signalW = new Node().setAttribute("type", "signal").setAttribute("pass", true);
		//      Node signalE = new Node().setAttribute("type", "signal").setAttribute("pass", true);
		//      s2.addEdge("signal", signalW);
		//      n6.addEdge("signal", signalE);
		//      g.addNode(signalW, signalE);
		
		//      Node map = new Node();
		//      map.setAttribute("type", "map");
		//      map.addEdge("roads", north1, north2, north3, north4, north5, north6, north7, south1, south2, south6, south7);
		//      graph.addNode(map);
		
		return g;
	}

    private PatternGraph createCarsPattern() {
	    PatternGraph graph = new PatternGraph();
	
	    PatternNode prevRoad = new PatternNode().setAttributeMatchExpression("#{type} == 'road'").setAction("!=");
	    PatternNode thisRoad = new PatternNode().setAttributeMatchExpression("#{type} == 'road'");
	    PatternNode nextRoad = new PatternNode().setAttributeMatchExpression("#{type} == 'road'");
	      
	    prevRoad.addPatternEdge(new PatternEdge().setSource(prevRoad).setName("east").setTarget(thisRoad));
	    thisRoad.addPatternEdge(new PatternEdge().setSource(thisRoad).setName("east").setTarget(nextRoad));
	
	    PatternNode existingCarAtEntrance = new PatternNode().setAttributeMatchExpression("#{type} == 'car'").setAction("!=");
	    PatternNode nonExistingCarAtEntrance = new PatternNode().setAction("+");
	    nonExistingCarAtEntrance.addPatternAttribute(new PatternAttribute().setName("type").setValue("car").setAction("+"));
	    nonExistingCarAtEntrance.addPatternAttribute(new PatternAttribute().setName("direction").setValue("east").setAction("+"));
	
//        thisRoad.addPatternEdge(new PatternEdge().setSource(thisRoad).setName("car").setTarget(existingCarAtEntrance));
//        thisRoad.addPatternEdge(new PatternEdge().setSource(thisRoad).setName("car").setTarget(nonExistingCarAtEntrance).setAction("+"));
	    existingCarAtEntrance.addPatternEdge(new PatternEdge().setSource(existingCarAtEntrance).setName("at").setTarget(thisRoad));
	    nonExistingCarAtEntrance.addPatternEdge(new PatternEdge().setSource(nonExistingCarAtEntrance).setName("at").setTarget(thisRoad).setAction("+"));
	
	    graph.addPatternNode(prevRoad);
	    graph.addPatternNode(thisRoad);
	    graph.addPatternNode(nextRoad);
	    graph.addPatternNode(existingCarAtEntrance);
	    graph.addPatternNode(nonExistingCarAtEntrance);
	      
	    return graph;
    }

    @Test
    public void test() {
	    Graph startGraph = getStartGraph();
	
	    PatternGraph createCarsPattern = createCarsPattern();
	
	    ArrayList<ArrayList<PatternGraph>> arrayList = new ArrayList<>();
	    arrayList.add(new ArrayList<>());
	
	    arrayList.get(0).add(createCarsPattern);
	
//        GraphEngine.setMainIsomorphismHandler(new IsomorphismHandlerCombinatorial());
	
//        System.out.println(createCarsPattern);
//	    ArrayList<Match> matches = PatternEngine.matchPattern(startGraph, createCarsPattern, false);
//	    Graph afterAppliedMatch = PatternEngine.applyMatch(matches.get(0));
//	    Assert.assertTrue(!GraphEngine.isIsomorphTo(startGraph, afterAppliedMatch));
//	      
//	    Graph reachabilityGraph = PatternEngine.calculateReachabilityGraph(startGraph, arrayList);
//	
//	    Graph nodeOutOfRG = GraphEngine.getGson().fromJson((String)reachabilityGraph.getNodes().get(4).getAttribute("graph"), Graph.class);
//	    String serializedNodeOutOfRG = GraphEngine.getGson().toJson(nodeOutOfRG);
//	    System.out.println(serializedNodeOutOfRG);
//	    GraphEngine.prepareGraphAsJsonFileForSigmaJs(nodeOutOfRG);
//	
//	    System.out.println(reachabilityGraph.getNodes().size());
    }
    
}
