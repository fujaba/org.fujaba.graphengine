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

		Node signalW = new Node().setAttribute("type", "signal").setAttribute("pass", "true");
		Node signalE = new Node().setAttribute("type", "signal").setAttribute("pass", "true");
		
		s2.addEdge("signal", signalW);
		n6.addEdge("signal", signalE);
		g.addNode(signalW, signalE);
		
//		Node map = new Node();
//		map.setAttribute("type", "map");
//		map.addEdge("roads", n1, n2, n3, n4, n5, n6, n7, s1, s2, s6, s7);
//		g.addNode(map);
		
		return g;
	}

    private PatternGraph createCarWestPattern() {
	    PatternGraph graph = new PatternGraph();
	
	    PatternNode prevRoad = new PatternNode().setAttributeMatchExpression("#{type} == 'road'").setAction("!=");
	    PatternNode thisRoad = new PatternNode().setAttributeMatchExpression("#{type} == 'road'");
	    PatternNode nextRoad = new PatternNode().setAttributeMatchExpression("#{type} == 'road'");
	    prevRoad.addPatternEdge("west", thisRoad);
	    thisRoad.addPatternEdge("west", nextRoad);
	    graph.addPatternNode(prevRoad, thisRoad, nextRoad);
	
		PatternNode existingCarAtEntrance = new PatternNode().setAttributeMatchExpression("#{type} == 'car'").setAction("!=");
		thisRoad.addPatternEdge("car", existingCarAtEntrance);
		graph.addPatternNode(existingCarAtEntrance);
		
		PatternNode nonExistingCarAtEntrance = new PatternNode().setAction("+");
		nonExistingCarAtEntrance.addPatternAttribute(new PatternAttribute().setName("type").setValue("car").setAction("+"));
		nonExistingCarAtEntrance.addPatternAttribute(new PatternAttribute().setName("direction").setValue("west").setAction("+"));
		thisRoad.addPatternEdge(new PatternEdge().setSource(thisRoad).setName("car").setTarget(nonExistingCarAtEntrance).setAction("+"));
		graph.addPatternNode(nonExistingCarAtEntrance);
	      
	    return graph;
    }
    
    private PatternGraph carMoveWestPattern() {
	    PatternGraph graph = new PatternGraph();

	    PatternNode oldRoad = new PatternNode().setAttributeMatchExpression("#{type} == 'road'");
	    PatternNode newRoad = new PatternNode().setAttributeMatchExpression("#{type} == 'road'");
	    PatternNode carGoingWest = new PatternNode().setAttributeMatchExpression("#{type} == 'car' && #{direction} == 'west'");
	    PatternNode carInFront = new PatternNode().setAttributeMatchExpression("#{type} == 'car'").setAction("!=");
	    PatternNode redSignal = new PatternNode().setAttributeMatchExpression("#{type} == 'signal' && #{pass} == 'false'").setAction("!=");

	    oldRoad.addPatternEdge("-", "car", carGoingWest);
	    oldRoad.addPatternEdge("signal", redSignal);
	    oldRoad.addPatternEdge("west", newRoad);
	    newRoad.addPatternEdge("+", "car", carGoingWest);
	    newRoad.addPatternEdge("car", carInFront);
	    
	    graph.addPatternNode(oldRoad, newRoad, carGoingWest, carInFront, redSignal);
	    
    	return graph; 
    }

    private PatternGraph createCarEastPattern() {
	    PatternGraph graph = new PatternGraph();
	
	    PatternNode prevRoad = new PatternNode().setAttributeMatchExpression("#{type} == 'road'").setAction("!=");
	    PatternNode thisRoad = new PatternNode().setAttributeMatchExpression("#{type} == 'road'");
	    PatternNode nextRoad = new PatternNode().setAttributeMatchExpression("#{type} == 'road'");
	    prevRoad.addPatternEdge("east", thisRoad);
	    thisRoad.addPatternEdge("east", nextRoad);
	    graph.addPatternNode(prevRoad, thisRoad, nextRoad);
	
		PatternNode existingCarAtEntrance = new PatternNode().setAttributeMatchExpression("#{type} == 'car'").setAction("!=");
		thisRoad.addPatternEdge("car", existingCarAtEntrance);
		graph.addPatternNode(existingCarAtEntrance);
		
		PatternNode nonExistingCarAtEntrance = new PatternNode().setAction("+");
		nonExistingCarAtEntrance.addPatternAttribute(new PatternAttribute().setName("type").setValue("car").setAction("+"));
		nonExistingCarAtEntrance.addPatternAttribute(new PatternAttribute().setName("direction").setValue("east").setAction("+"));
		thisRoad.addPatternEdge(new PatternEdge().setSource(thisRoad).setName("car").setTarget(nonExistingCarAtEntrance).setAction("+"));
		graph.addPatternNode(nonExistingCarAtEntrance);
	      
	    return graph;
    }
    
    private PatternGraph carMoveEastPattern() {
	    PatternGraph graph = new PatternGraph();
	    
//	      MapPO moveeastPO = new MapPO();
//	      moveeastPO.getPattern().setName("moveeastPO");
//	      CarPO carPO = moveeastPO.createCarsPO().createTravelDirectionCondition("east");
//	      TrackPO currentTrackPO = carPO.createTrackPO();
//	      TrackPO eastPO = currentTrackPO.createEastPO();
//	      eastPO.startNAC().createCarPO().endNAC();
//
//	      currentTrackPO.startNAC();
//	      currentTrackPO.createSignalPO().createPassCondition(false);
//	      currentTrackPO.endNAC();
//
//	      eastPO.createCloneOP();
//	      eastPO.createCarLink(carPO, Pattern.CREATE);
//
//	      reachabilityGraph.addToRules(moveeastPO);
//	      page.addPattern(moveeastPO, false);
	    
	    // TODO: implement
	    
    	return graph; 
    }
    
    private PatternGraph signalToGreenPattern() {
	    PatternGraph graph = new PatternGraph();
	    
//	      MapPO signalControlPO = new MapPO();
//
//	      SignalPO signalsPO = signalControlPO.createSignalsPO();
//
//	      signalsPO.createPassCondition(false);
//
//	      TrackPO trackPO = signalsPO.createTrackPO();
//
//	      trackPO.createCarPO();
//	      signalControlPO.createSignalsPO().hasMatchOtherThen(signalsPO).createPassCondition(false);
//
//	      signalControlPO.createCloneOP();
//
//	      signalsPO.createPassAssignment(true);
//
//	      signalControlPO.setRuleName("signalToGreen");
//
//	      reachabilityGraph.addToRules(signalControlPO);
//	      page.addPattern(signalControlPO, false);
	    
	    // TODO: implement
	    
    	return graph; 
    }
    
    private PatternGraph signalToRedPattern() {
	    PatternGraph graph = new PatternGraph();
	    
//	      MapPO signalControlPO = new MapPO();
//
//	      SignalPO signalsPO = signalControlPO.createSignalsPO();
//
//	      signalsPO.createPassCondition(true);
//
//	      TrackPO trackPO = signalsPO.createTrackPO();
//
//	      trackPO.startNAC().createCarPO().endNAC();
//
//	      signalControlPO.createCondition(new Condition<Map>()
//	      {
//	         @Override
//	         public boolean update(Map m)
//	         {
//	            TrackSet bidirectionaltracks = m.getBidirectionaltracks();
//	            for (Track track : bidirectionaltracks)
//	            {
//	               if (track.getCar() != null)
//	                  return false;
//	            }
//	            return true;
//	         }
//	      });
//
//	      signalControlPO.createCloneOP();
//
//	      signalsPO.createPassAssignment(false);
//
//	      signalControlPO.setRuleName("signalToRed");
//
//	      reachabilityGraph.addToRules(signalControlPO);
//	      page.addPattern(signalControlPO, false);
	    
	    // TODO: implement
	    
    	return graph; 
    }
    
    private PatternGraph carDeleteRule() {
	    PatternGraph graph = new PatternGraph();
	    
//	      {
//	          MapPO carDeletePO = new MapPO();
//
//	          CarPO carPO = carDeletePO.createCarsPO().createTravelDirectionCondition("east");
//	          TrackPO currentTrackPO = carPO.createTrackPO();
//	          currentTrackPO.startNAC().createEastPO().endNAC();
//	          carDeletePO.createCloneOP();
//	          // carPO.setModifier(Pattern.DESTROY);
//	          carPO.createMapLink(carDeletePO, Pattern.DESTROY);
//	          carPO.createTrackLink(currentTrackPO, Pattern.DESTROY);
//	          carDeletePO.getPattern().setName("delcareast");
//
//	          reachabilityGraph.addToRules(carDeletePO);
//	          page.addPattern(carDeletePO, false);
//	       }
//	       {
//	          MapPO carDeletePO = new MapPO();
//
//	          CarPO carPO = carDeletePO.createCarsPO().createTravelDirectionCondition("west");
//	          TrackPO currentTrackPO = carPO.createTrackPO();
//	          currentTrackPO.startNAC().createWestPO().endNAC();
//	          carDeletePO.createCloneOP();
//
//	          carPO.createMapLink(carDeletePO, Pattern.DESTROY);
//	          carPO.createTrackLink(currentTrackPO, Pattern.DESTROY);
//	          carDeletePO.getPattern().setName("delcarwest");
//
//	          reachabilityGraph.addToRules(carDeletePO);
//	          page.addPattern(carDeletePO, false);
//	       }
	    
	    // TODO: implement
	    
    	return graph; 
    }

    @Test
    public void testRoadworkExample() {
	    Graph startGraph = getStartGraph();
	
	    ArrayList<ArrayList<PatternGraph>> patterns = new ArrayList<>();
	    patterns.add(new ArrayList<>());
	    
	    /**
	     * following are some lines out of christoph's roadwork example
	     */
//		addCreateCarWestRule(page, reachabilityGraph);  // - [x] (createCarWestPattern)
	    patterns.get(0).add(createCarWestPattern());
//		addCarMoveWestRule(page, reachabilityGraph);    // - [x] (carMoveWestPattern)
	    patterns.get(0).add(carMoveWestPattern());
//		addCreateCarEastRule(page, reachabilityGraph);  // - [x] (createCarEastPattern)
	    patterns.get(0).add(createCarEastPattern());
//		addCarMoveEastRule(page, reachabilityGraph);    // - [ ] (carMoveEastPattern)
//		addSignalRuletoGreen1(page, reachabilityGraph); // - [ ] (signalToGreenPattern)
//		addSignalRuletoRed(page, reachabilityGraph);    // - [ ] (signalToRedPattern)
//		addCarDeleteRule(page, reachabilityGraph);      // - [ ] (carDeleteRule)
	    /**
	     * i should now try to implement the same kind of rules in my project
	     */
	
//        GraphEngine.setMainIsomorphismHandler(new IsomorphismHandlerCombinatorial());

	    Graph reachabilityGraph = PatternEngine.calculateReachabilityGraph(startGraph, patterns);

	    Graph nodeOutOfRG = GraphEngine.getGson().fromJson((String)reachabilityGraph.getNodes().get(reachabilityGraph.getNodes().size() - 1).getAttribute("graph"), Graph.class);
	    String serializedNodeOutOfRG = GraphEngine.getGson().toJson(nodeOutOfRG);
	    System.out.println(serializedNodeOutOfRG);
	    GraphEngine.prepareGraphAsJsonFileForSigmaJs(nodeOutOfRG);

	    System.out.println(reachabilityGraph.getNodes().size() + " node" + (reachabilityGraph.getNodes().size() != 1 ? "s" : "") + " in the 'reachabilityGraph'");
    }
    
}
