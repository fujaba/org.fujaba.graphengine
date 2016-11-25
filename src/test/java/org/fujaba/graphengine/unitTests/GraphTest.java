package org.fujaba.graphengine.unitTests;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.GraphAdapter;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.graph.NodeAdapter;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GraphTest {
	
	@Test
	public void testGraphSerializationAndDeserialization() {
		Gson gson = GraphEngine.getGson();
		// build ferryman's problem graph
		Graph original = new Graph();
		Node wolf = new Node(), goat = new Node(), cabbage = new Node(), ferry = new Node(),north = new Node(), south = new Node();
		original.addNode(wolf).addNode(goat).addNode(cabbage).addNode(ferry).addNode(north).addNode(south);
		wolf.setAttribute("type", "Cargo").setAttribute("species", "Wolf").addEdge("eats", goat).addEdge("at", north);
		goat.setAttribute("type", "Cargo").setAttribute("species", "Goat").addEdge("eats", cabbage).addEdge("at", north);
		cabbage.setAttribute("type", "Cargo").setAttribute("species", "Cabbage").addEdge("at", north);
		ferry.setAttribute("type", "Ferry").addEdge("at", north);
		north.setAttribute("type", "Bank").addEdge("opposite", south);
		south.setAttribute("type", "Bank").addEdge("opposite", north);

		String toJson = gson.toJson(original);
		Graph fromJson = gson.fromJson(toJson, Graph.class);
		String backToJson = gson.toJson(fromJson);

		Assert.assertEquals(0, original.compareTo(fromJson));
		Assert.assertEquals(toJson, backToJson);
		System.out.println(backToJson);
	}
	
	@Test
	public void testGraphSerialization() {
		// TODO: implement
	}
	
}