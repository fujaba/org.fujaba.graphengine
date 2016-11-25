package org.fujaba.graphengine.unitTests;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.junit.Test;

import com.google.gson.Gson;

public class GraphTest {
	
	@Test
	public void testGraphDeserialization() {
		// build ferryman's problem graph
		Graph ferrymansGraph = new Graph();
		Node wolf = new Node(), goat = new Node(), cabbage = new Node(), ferry = new Node(),north = new Node(), south = new Node();
		ferrymansGraph.addNode(wolf).addNode(goat).addNode(cabbage).addNode(ferry).addNode(north).addNode(south);
		wolf.setAttribute("type", "Cargo").setAttribute("species", "Wolf").addEdge("eats", goat).addEdge("at", north);
		goat.setAttribute("type", "Cargo").setAttribute("species", "Goat").addEdge("eats", cabbage).addEdge("at", north);
		cabbage.setAttribute("type", "Cargo").setAttribute("species", "Cabbage").addEdge("at", north);
		ferry.setAttribute("type", "Ferry").addEdge("at", north);
		north.setAttribute("type", "Bank").addEdge("opposite", south);
		south.setAttribute("type", "Bank");//.addEdge("opposite", north);
		
		System.out.println(new Gson().toJson(ferrymansGraph));
	}
	
	@Test
	public void testGraphSerialization() {
		// TODO: implement
	}
	
}