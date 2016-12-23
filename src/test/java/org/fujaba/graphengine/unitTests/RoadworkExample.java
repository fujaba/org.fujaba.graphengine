package org.fujaba.graphengine.unitTests;

import java.util.ArrayList;
import java.util.HashMap;

import org.fujaba.graphengine.GraphDumper;
import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.Match;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandler;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerDepthFirstBacktracking;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerSorting;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerCSPLowHeuristics;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerCSPHighHeuristics;
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

		Node signalW = new Node().setAttribute("type", "signal").setAttribute("pass", false);
		Node signalE = new Node().setAttribute("type", "signal").setAttribute("pass", false);
		
		s2.addEdge("signal", signalW);
		n6.addEdge("signal", signalE);
		g.addNode(signalW, signalE);
		
//		Node map = new Node();
//		map.setAttribute("type", "map");
//		map.addEdge("roads", n1, n2, n3, n4, n5, n6, n7, s1, s2, s6, s7);
//		g.addNode(map);
		
		return g;
	}
	Graph getMinimalStartGraph() {
		Graph g = new Graph();
		
		Node n1 = new Node().setAttribute("type", "road");
		Node n2 = new Node().setAttribute("type", "road");
		Node n3 = new Node().setAttribute("type", "road");
		
		Node s1 = new Node().setAttribute("type", "road");
		Node s3 = new Node().setAttribute("type", "road");
		
		g.addNode(n1, n2, n3,
		          s1,     s3);
		
		n3.addEdge("west", n2);
		n2.addEdge("west", n1);
		
		s1.addEdge("east", n2);
		n2.addEdge("east", s3);

		Node signalW = new Node().setAttribute("type", "signal").setAttribute("pass", false);
		Node signalE = new Node().setAttribute("type", "signal").setAttribute("pass", false);
		
		s1.addEdge("signal", signalW);
		n3.addEdge("signal", signalE);
		g.addNode(signalW, signalE);
		
//		Node map = new Node();
//		map.setAttribute("type", "map");
//		map.addEdge("road", n1, n2, n3, s1, s3);
//		g.addNode(map);
		
		return g;
	}
	Graph getMinimalPlusOneStartGraph() {
		Graph g = new Graph();
		
		Node n1 = new Node().setAttribute("type", "road");
		Node n2 = new Node().setAttribute("type", "road");
		Node n3 = new Node().setAttribute("type", "road");
		Node n4 = new Node().setAttribute("type", "road");
		
		Node s1 = new Node().setAttribute("type", "road");
		Node s4 = new Node().setAttribute("type", "road");
		
		g.addNode(n1, n2, n3, n4,
		          s1,         s4);

		n4.addEdge("west", n3);
		n3.addEdge("west", n2);
		n2.addEdge("west", n1);

		s1.addEdge("east", n2);
		n2.addEdge("east", n3);
		n3.addEdge("east", s4);

		Node signalW = new Node().setAttribute("type", "signal").setAttribute("pass", false);
		Node signalE = new Node().setAttribute("type", "signal").setAttribute("pass", false);
		
		s1.addEdge("signal", signalW);
		n4.addEdge("signal", signalE);
		g.addNode(signalW, signalE);
		
//		Node map = new Node();
//		map.setAttribute("type", "map");
//		map.addEdge("roads", n1, n2, n3, n4, n5, n6, n7, s1, s2, s6, s7);
//		g.addNode(map);
		
		return g;
	}

    private PatternGraph createCarWestPattern() {
	    PatternGraph graph = new PatternGraph("createCarWestPattern");
	
	    PatternNode prevRoad = new PatternNode("#{type} == 'road'").setAction("!=");
	    PatternNode thisRoad = new PatternNode("#{type} == 'road'");
	    PatternNode nextRoad = new PatternNode("#{type} == 'road'");
	    prevRoad.addPatternEdge("west", thisRoad);
	    thisRoad.addPatternEdge("west", nextRoad);
	    graph.addPatternNode(prevRoad, thisRoad, nextRoad);
	
		PatternNode existingCarAtEntrance = new PatternNode("#{type} == 'car'").setAction("!=");
		thisRoad.addPatternEdge("car", existingCarAtEntrance);
		graph.addPatternNode(existingCarAtEntrance);
		
		PatternNode nonExistingCarAtEntrance = new PatternNode().setAction("+");
		nonExistingCarAtEntrance.addPatternAttribute(new PatternAttribute().setName("type").setValue("car").setAction("+"));
		nonExistingCarAtEntrance.addPatternAttribute(new PatternAttribute().setName("direction").setValue("west").setAction("+"));
		thisRoad.addPatternEdge(new PatternEdge().setSource(thisRoad).setName("car").setTarget(nonExistingCarAtEntrance).setAction("+"));
		graph.addPatternNode(nonExistingCarAtEntrance);
	      
	    return graph;
    }
    
    private PatternGraph moveCarWestPattern() {
	    PatternGraph graph = new PatternGraph("moveCarWestPattern");

	    PatternNode oldRoad = new PatternNode("#{type} == 'road'");
	    PatternNode newRoad = new PatternNode("#{type} == 'road'");
	    PatternNode carGoingWest = new PatternNode("#{type} == 'car' && #{direction} == 'west'");
	    PatternNode carInFront = new PatternNode("#{type} == 'car'").setAction("!=");
	    PatternNode redSignal = new PatternNode("#{type} == 'signal' && !#{pass}").setAction("!=");

	    oldRoad.addPatternEdge("-", "car", carGoingWest);
	    oldRoad.addPatternEdge("signal", redSignal);
	    oldRoad.addPatternEdge("west", newRoad);
	    newRoad.addPatternEdge("+", "car", carGoingWest);
	    newRoad.addPatternEdge("car", carInFront);
	    
	    graph.addPatternNode(oldRoad, newRoad, carGoingWest, carInFront, redSignal);
	    
    	return graph; 
    }
    
    private PatternGraph deleteCarWestPattern() {
	    PatternGraph graph = new PatternGraph("deleteCarWestPattern");
	    
	    PatternNode car = new PatternNode("#{type} == 'car' && #{direction} == 'west'").setAction("-");
	    PatternNode thisRoad = new PatternNode("#{type} == 'road'");
	    PatternNode nextRoad = new PatternNode("#{type} == 'road'").setAction("!=");
	    
	    thisRoad.addPatternEdge("car", car);
	    thisRoad.addPatternEdge("west", nextRoad);
	    
	    graph.addPatternNode(car, thisRoad, nextRoad);
	    
    	return graph; 
    }

    private PatternGraph createCarEastPattern() {
	    PatternGraph graph = new PatternGraph("createCarEastPattern");
	
	    PatternNode prevRoad = new PatternNode("#{type} == 'road'").setAction("!=");
	    PatternNode thisRoad = new PatternNode("#{type} == 'road'");
	    PatternNode nextRoad = new PatternNode("#{type} == 'road'");
	    prevRoad.addPatternEdge("east", thisRoad);
	    thisRoad.addPatternEdge("east", nextRoad);
	    graph.addPatternNode(prevRoad, thisRoad, nextRoad);
	
		PatternNode existingCarAtEntrance = new PatternNode("#{type} == 'car'").setAction("!=");
		thisRoad.addPatternEdge("car", existingCarAtEntrance);
		graph.addPatternNode(existingCarAtEntrance);
		
		PatternNode nonExistingCarAtEntrance = new PatternNode().setAction("+");
		nonExistingCarAtEntrance.addPatternAttribute(new PatternAttribute().setName("type").setValue("car").setAction("+"));
		nonExistingCarAtEntrance.addPatternAttribute(new PatternAttribute().setName("direction").setValue("east").setAction("+"));
		thisRoad.addPatternEdge(new PatternEdge().setSource(thisRoad).setName("car").setTarget(nonExistingCarAtEntrance).setAction("+"));
		graph.addPatternNode(nonExistingCarAtEntrance);
	      
	    return graph;
    }
    
    private PatternGraph moveCarEastPattern() {
	    PatternGraph graph = new PatternGraph("moveCarEastPattern");

	    PatternNode oldRoad = new PatternNode("#{type} == 'road'");
	    PatternNode newRoad = new PatternNode("#{type} == 'road'");
	    PatternNode carGoingWest = new PatternNode("#{type} == 'car' && #{direction} == 'east'");
	    PatternNode carInFront = new PatternNode("#{type} == 'car'").setAction("!=");
	    PatternNode redSignal = new PatternNode("#{type} == 'signal' && !#{pass}").setAction("!=");

	    oldRoad.addPatternEdge("-", "car", carGoingWest);
	    oldRoad.addPatternEdge("signal", redSignal);
	    oldRoad.addPatternEdge("east", newRoad);
	    newRoad.addPatternEdge("+", "car", carGoingWest);
	    newRoad.addPatternEdge("car", carInFront);
	    
	    graph.addPatternNode(oldRoad, newRoad, carGoingWest, carInFront, redSignal);
	    
    	return graph; 
    }
    
    private PatternGraph deleteCarEastPattern() {
	    PatternGraph graph = new PatternGraph("deleteCarEastPattern");
	    
	    PatternNode car = new PatternNode("#{type} == 'car' && #{direction} == 'east'").setAction("-");
	    PatternNode thisRoad = new PatternNode("#{type} == 'road'");
	    PatternNode nextRoad = new PatternNode("#{type} == 'road'").setAction("!=");
	    
	    thisRoad.addPatternEdge("car", car);
	    thisRoad.addPatternEdge("east", nextRoad);
	    
	    graph.addPatternNode(car, thisRoad, nextRoad);
	    
    	return graph; 
    }
    
    private PatternGraph signalToGreenPattern() {
	    PatternGraph graph = new PatternGraph("signalToGreenPattern");
	    
	    PatternNode signalAboutToTurnGreen = new PatternNode("#{type} == 'signal' && !#{pass}");
	    PatternNode signalStaysRed = new PatternNode("#{type} == 'signal' && !#{pass}");
	    PatternNode carNeedsGreen = new PatternNode("#{type} == 'car'");
	    PatternNode roadAtCar = new PatternNode("#{type} == 'road'");

	    roadAtCar.addPatternEdge("car", carNeedsGreen);
	    roadAtCar.addPatternEdge("signal", signalAboutToTurnGreen);
	    
	    signalAboutToTurnGreen.setPatternAttribute("+", "pass", true);
	    
	    graph.addPatternNode(signalAboutToTurnGreen, signalStaysRed, carNeedsGreen, roadAtCar);
	    
    	return graph; 
    }
    
    private PatternGraph signalToRedPattern() {
	    PatternGraph graph = new PatternGraph("signalToRedPattern");
	    
	    PatternNode signalAboutToTurnRed = new PatternNode("#{type} == 'signal' && #{pass}")
	    		.setPatternAttribute("+", "pass", false);
	    
	    PatternNode carAtBidirectionRoad = new PatternNode("#{type} == 'car'").setAction("!=");
	    PatternNode easternRoad = new PatternNode("#{type} == 'road'").setAction("!=");
	    PatternNode westernRoad = new PatternNode("#{type} == 'road'").setAction("!=");
	    PatternNode bidirectionalRoad = new PatternNode("#{type} == 'road'").setAction("!=")
	    		.addPatternEdge("east", easternRoad)
	    		.addPatternEdge("west", westernRoad)
	    		.addPatternEdge("car", carAtBidirectionRoad);
	    
	    graph.addPatternNode(signalAboutToTurnRed, carAtBidirectionRoad, easternRoad, westernRoad, bidirectionalRoad);
	    
    	return graph; 
    }
    
    private double testRoadworkExample(boolean debug, IsomorphismHandler ih, int fromLevel, int toLevel, boolean drawSigmaJs, boolean drawAlchemyJs) {
	    ArrayList<ArrayList<PatternGraph>> patterns = new ArrayList<>();
	    patterns.add(new ArrayList<>());

	    patterns.get(0).add(signalToRedPattern());
	    patterns.get(0).add(signalToGreenPattern());

	    patterns.get(0).add(deleteCarEastPattern());
	    patterns.get(0).add(createCarEastPattern());
	    patterns.get(0).add(moveCarEastPattern());
	    
	    patterns.get(0).add(deleteCarWestPattern());
	    patterns.get(0).add(createCarWestPattern());
	    patterns.get(0).add(moveCarWestPattern());
	    
	    IsomorphismHandler ihBefore = GraphEngine.getMainIsomorphismHandler();
	    
	    GraphEngine.setMainIsomorphismHandler(ih);
	    long begin = 0;
	    Graph reachabilityGraph = null;
	    double total = 0;

	    if (debug) {
		    System.out.println("\nusing " + ih + "...");
	    }
	    if (fromLevel <= 1 && toLevel >= 1) {
		    if (debug) {
			    System.out.println("\nbuild reachability graph for RoadworkExample (minimal map with 5 pieces of road)...");
		    }
		    begin = System.nanoTime();
		    reachabilityGraph = PatternEngine.calculateReachabilityGraph(getMinimalStartGraph(), patterns);
		    total += (System.nanoTime() - begin) / 1e6;
		    if (debug) {
			    System.out.println("-> done building reachability graph for RoadworkExample after " + ((System.nanoTime() - begin) / 1e9) + " s == " + ((System.nanoTime() - begin) / 1e9 / 60) + " m");
			    System.out.println("   " + reachabilityGraph.getNodes().size() + " node" + (reachabilityGraph.getNodes().size() != 1 ? "s" : "") + " in the 'reachabilityGraph'");
		    }
	    }
	    if (fromLevel <= 2 && toLevel >= 2) {
		    if (debug) {
			    System.out.println("\nbuild reachability graph for RoadworkExample (reduced map with 6 pieces of road)...");
		    }
		    begin = System.nanoTime();
		    reachabilityGraph = PatternEngine.calculateReachabilityGraph(getMinimalPlusOneStartGraph(), patterns);
		    total += (System.nanoTime() - begin) / 1e6;
		    if (debug) {
			    System.out.println("-> done building reachability graph for RoadworkExample after " + ((System.nanoTime() - begin) / 1e9) + " s == " + ((System.nanoTime() - begin) / 1e9 / 60) + " m");
			    System.out.println("   " + reachabilityGraph.getNodes().size() + " node" + (reachabilityGraph.getNodes().size() != 1 ? "s" : "") + " in the 'reachabilityGraph'");
		    }
	    }
	    if (fromLevel <= 3 && toLevel >= 3) {
		    if (debug) {
			    System.out.println("\nbuild reachability graph for RoadworkExample (original map with 11 pieces of road)...");
		    }
		    begin = System.nanoTime();
		    reachabilityGraph = PatternEngine.calculateReachabilityGraph(getStartGraph(), patterns);
		    total += (System.nanoTime() - begin) / 1e6;
		    if (debug) {
			    System.out.println("-> done building reachability graph for RoadworkExample after " + ((System.nanoTime() - begin) / 1e9) + " s == " + ((System.nanoTime() - begin) / 1e9 / 60) + " m");
			    System.out.println("   " + reachabilityGraph.getNodes().size() + " node" + (reachabilityGraph.getNodes().size() != 1 ? "s" : "") + " in the 'reachabilityGraph'");
		    }
	    }
	    
	    if (drawAlchemyJs) {
		    new GraphDumper(reachabilityGraph).dumpGraph("roadwork.html");
	    }
	    if (drawSigmaJs) {
		    GraphEngine.prepareGraphAsJsonFileForSigmaJs(
		    	GraphEngine.getGson().fromJson(
		    		(String)reachabilityGraph.getNodes()
		    				.get(reachabilityGraph.getNodes().size() - 1)
		    				.getAttribute("graph"),
		    		Graph.class
		    	),
		    	"data.json"
		    );
	    }
	    
	    GraphEngine.setMainIsomorphismHandler(ihBefore);
	    return total;
    }

    @Test
    public void testRoadworkExample() {
    	
    	ArrayList<IsomorphismHandler> toTest = new ArrayList<IsomorphismHandler>();
    	toTest.add(new IsomorphismHandlerCSPHighHeuristics());
    	toTest.add(new IsomorphismHandlerCSPLowHeuristics());
    	toTest.add(new IsomorphismHandlerDepthFirstBacktracking());
//    	toTest.add(new IsomorphismHandlerSorting());
//    	toTest.add(new IsomorphismHandlerCombinatorial());

    	boolean debug = false;
    	int fromLevel = 2;
    	int toLevel = 2;
    	boolean drawSigmaJs = false;
    	boolean drawAlchemyJs = false;
    	
    	boolean warmUp = false;

    	if (warmUp) {
        	for (int i = 0; i < 3; ++i) {
        		for (IsomorphismHandler ih: toTest) {
            		testRoadworkExample(false, ih, 1, 1, false, false);
        		}
        	}
    	}
    	for (IsomorphismHandler ih: toTest) {
    		double time = testRoadworkExample(debug, ih, fromLevel, toLevel, drawSigmaJs, drawAlchemyJs);
    		System.out.println(time + "ms == " + (time / 1e3 / 60) + "m <- " + ih); 
    	}
    	
    }
    
}
