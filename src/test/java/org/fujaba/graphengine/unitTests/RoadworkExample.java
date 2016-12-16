package org.fujaba.graphengine.unitTests;

import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.PatternEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.isomorphismtools.IsomorphismHandlerCombinatorial;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.junit.Test;

public class RoadworkExample
{
   Graph getStartGraph()
   {
      Graph graph = new Graph();

      Node map = new Node();

      Node north1 = new Node().setAttribute("type", "road");
      Node north2 = new Node().setAttribute("type", "road");
      Node north3 = new Node().setAttribute("type", "road");
      Node north4 = new Node().setAttribute("type", "road");
      Node north5 = new Node().setAttribute("type", "road");
      Node north6 = new Node().setAttribute("type", "road");
      Node north7 = new Node().setAttribute("type", "road");

      Node south1 = new Node().setAttribute("type", "road");
      Node south2 = new Node().setAttribute("type", "road");
      Node south6 = new Node().setAttribute("type", "road");
      Node south7 = new Node().setAttribute("type", "road");

      graph.addNode(north1, north2, north3, north4, north5, north6, north7, south1, south2, south6, south7);

      north1.addEdge("west", north2);
      north2.addEdge("west", north3);
      north3.addEdge("west", north4);
      north4.addEdge("west", north5);
      north5.addEdge("west", north6);
      north6.addEdge("west", north7);

      south1.addEdge("east", south2);
      south2.addEdge("east", north3);
      north3.addEdge("east", north4);
      north4.addEdge("east", north5);
      north5.addEdge("east", south6);
      south6.addEdge("east", south7);

      Node westernSignal = new Node().setAttribute("type", "signal").setAttribute("pass", "true");
      Node eaternSignal = new Node().setAttribute("type", "signal").setAttribute("pass", "true");

      south2.addEdge("signal", westernSignal);
      north6.addEdge("signal", eaternSignal);

      map.setAttribute("type", "map");

      map.addEdge("roads", north1, north2, north3, north4, north5, north6, north7, south1, south2, south6, south7);

      return graph;
   }


   private PatternGraph createCarsPattern()
   {
      PatternGraph graph = new PatternGraph();

      PatternNode prevRoadElement = new PatternNode().setAction("!=").setAttributeMatchExpression("#{type} == 'road'");
      PatternNode roadElement = new PatternNode().setAttributeMatchExpression("#{type} == 'road'");
      PatternNode succElement = new PatternNode().setAttributeMatchExpression("#{type} == 'road'");

      PatternNode notCarElement = new PatternNode().setAction("!=").setAttributeMatchExpression("#{type} == 'car'");
      PatternNode carToCreateElement = new PatternNode().setAction("+");
      carToCreateElement.addPatternAttribute(new PatternAttribute().setAction("+").setName("type").setValue("car"));
      carToCreateElement.addPatternAttribute(new PatternAttribute().setAction("+").setName("direction").setValue("west"));

      prevRoadElement.addPatternEdge(new PatternEdge().setSource(prevRoadElement).setTarget(roadElement).setName("west"));
      roadElement.addPatternEdge(new PatternEdge().setSource(roadElement).setTarget(succElement).setName("west"));

      roadElement.addPatternEdge(new PatternEdge().setSource(roadElement).setTarget(notCarElement).setName("car"));

      roadElement.addPatternEdge(new PatternEdge().setSource(roadElement).setTarget(carToCreateElement).setName("car"));

      return graph;
   }


   @Test
   public void test()
   {
      Graph startGraph = getStartGraph();

      PatternGraph createCarsPattern = createCarsPattern();

      ArrayList<ArrayList<PatternGraph>> arrayList = new ArrayList<>();
      arrayList.add(new ArrayList<>());

      arrayList.get(0).add(createCarsPattern);

      GraphEngine.setMainIsomorphismHandler(new IsomorphismHandlerCombinatorial());

      Graph reachabilityGraph = PatternEngine.calculateReachabilityGraph(startGraph, arrayList);

      String json = GraphEngine.getGson().toJson(startGraph);

      System.out.println(json);

   }
}
