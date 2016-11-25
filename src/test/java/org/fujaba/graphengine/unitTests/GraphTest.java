package org.fujaba.graphengine.unitTests;

import org.junit.Test;

import java.util.HashSet;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.junit.Assert;

public class GraphTest {
	
	/**
	 * String representing the graph of the ferryman's problem
	 */
	private String ferrymansProblemJSON = "{\"nodes\":[{\"id\":\"w\",\"type\":\"Cargo\",\"attributes\":[{\"name\":\"species\",\"value\":\"Wolf\"}],\"edges\":[{\"name\":\"at\",\"target\":\"north\"},{\"name\":\"eats\",\"target\":\"g\"}]},{\"id\":\"g\",\"type\":\"Cargo\",\"attributes\":[{\"name\":\"species\",\"value\":\"Goat\"}],\"edges\":[{\"name\":\"at\",\"target\":\"north\"},{\"name\":\"eats\",\"target\":\"c\"}]},{\"id\":\"c\",\"type\":\"Cargo\",\"attributes\":[{\"name\":\"species\",\"value\":\"Cabbage\"}],\"edges\":[{\"name\":\"at\",\"target\":\"north\"}]},{\"id\":\"ferry\",\"type\":\"Ferry\",\"attributes\":[],\"edges\":[{\"name\":\"at\",\"target\":\"north\"}]},{\"id\":\"north\",\"type\":\"Bank\",\"attributes\":[],\"edges\":[{\"name\":\"opposite\",\"target\":\"south\"}]},{\"id\":\"south\",\"type\":\"Bank\",\"attributes\":[],\"edges\":[{\"name\":\"opposite\",\"target\":\"north\"}]}]}";
	
	@Test
	public void testGraphDeserialization() {
		// build graph with JSON representation:
		Graph ferrymansProblemGraph = new Graph(ferrymansProblemJSON);

		// check all nodes if they were created and if they have the right ID, TYPE and ATTRIBUTES:
		Node wolf = ferrymansProblemGraph.getNodeById("w");
		Assert.assertEquals("w", wolf.getId());
		Assert.assertEquals("Cargo", wolf.getType());
		Assert.assertEquals("Wolf", wolf.getAttribute("species"));
		
		Node goat = ferrymansProblemGraph.getNodeById("g");
		Assert.assertEquals("g", goat.getId());
		Assert.assertEquals("Cargo", goat.getType());
		Assert.assertEquals("Goat", goat.getAttribute("species"));
		
		Node cabbage = ferrymansProblemGraph.getNodeById("c");
		Assert.assertEquals("c", cabbage.getId());
		Assert.assertEquals("Cargo", cabbage.getType());
		Assert.assertEquals("Cabbage", cabbage.getAttribute("species"));

		Node ferry = ferrymansProblemGraph.getNodeById("ferry");
		Assert.assertEquals("ferry", ferry.getId());
		Assert.assertEquals("Ferry", ferry.getType());
		
		Node north = ferrymansProblemGraph.getNodeById("north");
		Assert.assertEquals("north", north.getId());
		Assert.assertEquals("Bank", north.getType());
		
		Node south = ferrymansProblemGraph.getNodeById("south");
		Assert.assertEquals("south", south.getId());
		Assert.assertEquals("Bank", south.getType());
		
		// check if all edges were correctly created:
		Assert.assertTrue(wolf.getOutgoingEdges("eats").contains(goat)); // wolf eats goat
		Assert.assertTrue(goat.getOutgoingEdges("eats") == null
				|| !goat.getOutgoingEdges("eats").contains(wolf)); // but goat doesn't eat wolf
		Assert.assertTrue(goat.getOutgoingEdges("eats").contains(cabbage)); // goat eats cabbage
		Assert.assertTrue(cabbage.getOutgoingEdges("eats") == null
				|| !cabbage.getOutgoingEdges("eats").contains(goat)); // but cabbage doesn't eat goat
		Assert.assertTrue(wolf.getOutgoingEdges("at").contains(north)); // wolf is at north
		Assert.assertTrue(north.getOutgoingEdges("at") == null
				|| !north.getOutgoingEdges("at").contains(wolf)); // but north is not at wolf
		Assert.assertTrue(goat.getOutgoingEdges("at").contains(north)); // goat is at north
		Assert.assertTrue(north.getOutgoingEdges("at") == null
				|| !north.getOutgoingEdges("at").contains(goat)); // but north is not at goat
		Assert.assertTrue(cabbage.getOutgoingEdges("at").contains(north));
		Assert.assertTrue(north.getOutgoingEdges("at") == null
				|| !north.getOutgoingEdges("at").contains(cabbage)); // cabbage is at north
		Assert.assertTrue(ferry.getOutgoingEdges("at").contains(north));
		Assert.assertTrue(north.getOutgoingEdges("at") == null
				|| !north.getOutgoingEdges("at").contains(ferry)); // but north is not at cabbage
		Assert.assertTrue(north.getOutgoingEdges("opposite").contains(south)); // north is opposite of south
		Assert.assertTrue(south.getOutgoingEdges("opposite").contains(north)); // south is opposite of north
		
		// check if those nodes are exactly the set of nodes in the graph:
		HashSet<Node> testedNodes = new HashSet<Node>();
		testedNodes.add(wolf);
		testedNodes.add(goat);
		testedNodes.add(cabbage);
		testedNodes.add(ferry);
		testedNodes.add(north);
		testedNodes.add(south);
		Assert.assertTrue(ferrymansProblemGraph.getNodes().containsAll(testedNodes));
		Assert.assertTrue(testedNodes.containsAll(ferrymansProblemGraph.getNodes()));
		
		// okay, deserialization works!
	}
	
	@Test
	public void testGraphSerialization() {
		// create the graph from JSON like in testGraphDeserialization():
		Graph ferrymansProblemGraph = new Graph(ferrymansProblemJSON);
		
		// serialize to JSON:
		String serialized = ferrymansProblemGraph.toString();
		
		// deserialize again:
		Graph deserialized = new Graph(serialized);

		// quickly check, that the deserialized graph is not empty, but probably correct:
		Node wolf = deserialized.getNodeById("w");
		Assert.assertEquals("w", wolf.getId());
		Assert.assertEquals("Cargo", wolf.getType());
		Assert.assertEquals("Wolf", wolf.getAttribute("species"));
		
		// finally serialize and check, if it is the same String as after the first serialization:
		String reserialized = deserialized.toString();
		Assert.assertEquals(serialized, reserialized);
		
		// okay, serialization and deserialization both work!
	}
	
	@Test
	public void testGraphGetNodeCollections() {
		// create the graph from JSON like in testGraphDeserialization():
		Graph ferrymansProblemGraph = new Graph(ferrymansProblemJSON);
		Node wolf = ferrymansProblemGraph.getNodeById("w");
		Node goat = ferrymansProblemGraph.getNodeById("g");
		Node cabbage = ferrymansProblemGraph.getNodeById("c");
		Node ferry = ferrymansProblemGraph.getNodeById("ferry");
		Node north = ferrymansProblemGraph.getNodeById("north");
		Node south = ferrymansProblemGraph.getNodeById("south");

		// build sets including the expected nodes
		HashSet<Node> nodesWithTypeCargo = new HashSet<Node>(); // type == "Cargo"
		nodesWithTypeCargo.add(wolf);
		nodesWithTypeCargo.add(goat);
		nodesWithTypeCargo.add(cabbage);
		HashSet<Node> nodesWithTypeFerry = new HashSet<Node>(); // type == "Ferry"
		nodesWithTypeFerry.add(ferry);
		HashSet<Node> nodesWithTypeBank = new HashSet<Node>(); // type == "Bank"
		nodesWithTypeBank.add(north);
		nodesWithTypeBank.add(south);
		HashSet<Node> nodesWithOutgoingEdgesEats = new HashSet<Node>(); // outgoing edge with name == "eats"
		nodesWithOutgoingEdgesEats.add(wolf);
		nodesWithOutgoingEdgesEats.add(goat);
		HashSet<Node> nodesWithOutgoingEdgesAt = new HashSet<Node>(); // outgoing edge with name == "at"
		nodesWithOutgoingEdgesAt.add(wolf);
		nodesWithOutgoingEdgesAt.add(goat);
		nodesWithOutgoingEdgesAt.add(cabbage);
		nodesWithOutgoingEdgesAt.add(ferry);
		HashSet<Node> nodesWithOutgoingEdgesOpposite = new HashSet<Node>(); // outgoing edge with name == "opposite"
		nodesWithOutgoingEdgesOpposite.add(north);
		nodesWithOutgoingEdgesOpposite.add(south);
		HashSet<Node> nodesWithIngoingEdgesEats = new HashSet<Node>(); // ingoing edge with name == "eats"
		nodesWithIngoingEdgesEats.add(goat);
		nodesWithIngoingEdgesEats.add(cabbage);
		HashSet<Node> nodesWithIngoingEdgesAt = new HashSet<Node>(); // ingoing edge with name == "at"
		nodesWithIngoingEdgesAt.add(north);
		HashSet<Node> nodesWithIngoingEdgesOpposite = new HashSet<Node>(); // ingoing edge with name == "opposite"
		nodesWithIngoingEdgesOpposite.add(north);
		nodesWithIngoingEdgesOpposite.add(south);
		
		// check if exactly those nodes are selected:
		
		// getNodesByType:
		Assert.assertTrue(nodesWithTypeCargo.containsAll(ferrymansProblemGraph.getNodesByType("Cargo")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Cargo").containsAll(nodesWithTypeCargo));
		Assert.assertTrue(nodesWithTypeFerry.containsAll(ferrymansProblemGraph.getNodesByType("Ferry")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Ferry").containsAll(nodesWithTypeFerry));
		Assert.assertTrue(nodesWithTypeBank.containsAll(ferrymansProblemGraph.getNodesByType("Bank")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Bank").containsAll(nodesWithTypeBank));
		// getNodesByOutgoingEdge:
		Assert.assertTrue(nodesWithOutgoingEdgesEats.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("eats")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("eats").containsAll(nodesWithOutgoingEdgesEats));
		Assert.assertTrue(nodesWithOutgoingEdgesAt.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("at")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("at").containsAll(nodesWithOutgoingEdgesAt));
		Assert.assertTrue(nodesWithOutgoingEdgesOpposite.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("opposite")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("opposite").containsAll(nodesWithOutgoingEdgesOpposite));
		// getNodesByIngoingEdge:
		Assert.assertTrue(nodesWithIngoingEdgesEats.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("eats")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("eats").containsAll(nodesWithIngoingEdgesEats));
		Assert.assertTrue(nodesWithIngoingEdgesAt.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("at")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("at").containsAll(nodesWithIngoingEdgesAt));
		Assert.assertTrue(nodesWithIngoingEdgesOpposite.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("opposite")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("opposite").containsAll(nodesWithIngoingEdgesOpposite));
	}
	
	@Test
	public void testGraphNodeRemoval() {
		// create the graph from JSON like in testGraphDeserialization():
		Graph ferrymansProblemGraph = new Graph(ferrymansProblemJSON);
		Node wolf = ferrymansProblemGraph.getNodeById("w");
		Node goat = ferrymansProblemGraph.getNodeById("g");
		Node cabbage = ferrymansProblemGraph.getNodeById("c");
		Node ferry = ferrymansProblemGraph.getNodeById("ferry");
		Node north = ferrymansProblemGraph.getNodeById("north");
		Node south = ferrymansProblemGraph.getNodeById("south");
		
		// now remove the node with id == "north"
		ferrymansProblemGraph.removeNode(north);

		// build sets including the expected nodes
		HashSet<Node> nodesWithTypeCargo = new HashSet<Node>(); // type == "Cargo"
		nodesWithTypeCargo.add(wolf);
		nodesWithTypeCargo.add(goat);
		nodesWithTypeCargo.add(cabbage);
		HashSet<Node> nodesWithTypeFerry = new HashSet<Node>(); // type == "Ferry"
		nodesWithTypeFerry.add(ferry);
		HashSet<Node> nodesWithTypeBank = new HashSet<Node>(); // type == "Bank"
		nodesWithTypeBank.add(south);
		HashSet<Node> nodesWithOutgoingEdgesEats = new HashSet<Node>(); // outgoing edge with name == "eats"
		nodesWithOutgoingEdgesEats.add(wolf);
		nodesWithOutgoingEdgesEats.add(goat);
		HashSet<Node> nodesWithOutgoingEdgesAt = new HashSet<Node>(); // outgoing edge with name == "at"
		HashSet<Node> nodesWithOutgoingEdgesOpposite = new HashSet<Node>(); // outgoing edge with name == "opposite"
		HashSet<Node> nodesWithIngoingEdgesEats = new HashSet<Node>(); // ingoing edge with name == "eats"
		nodesWithIngoingEdgesEats.add(goat);
		nodesWithIngoingEdgesEats.add(cabbage);
		HashSet<Node> nodesWithIngoingEdgesAt = new HashSet<Node>(); // ingoing edge with name == "at"
		HashSet<Node> nodesWithIngoingEdgesOpposite = new HashSet<Node>(); // ingoing edge with name == "opposite"
		
		// check if exactly those nodes are selected:
		
		// getNodesByType:
		Assert.assertTrue(nodesWithTypeCargo.containsAll(ferrymansProblemGraph.getNodesByType("Cargo")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Cargo").containsAll(nodesWithTypeCargo));
		Assert.assertTrue(nodesWithTypeFerry.containsAll(ferrymansProblemGraph.getNodesByType("Ferry")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Ferry").containsAll(nodesWithTypeFerry));
		Assert.assertTrue(nodesWithTypeBank.containsAll(ferrymansProblemGraph.getNodesByType("Bank")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Bank").containsAll(nodesWithTypeBank));
		// getNodesByOutgoingEdge:
		Assert.assertTrue(nodesWithOutgoingEdgesEats.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("eats")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("eats").containsAll(nodesWithOutgoingEdgesEats));
		Assert.assertTrue(nodesWithOutgoingEdgesAt.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("at")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("at").containsAll(nodesWithOutgoingEdgesAt));
		Assert.assertTrue(nodesWithOutgoingEdgesOpposite.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("opposite")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("opposite").containsAll(nodesWithOutgoingEdgesOpposite));
		// getNodesByIngoingEdge:
		Assert.assertTrue(nodesWithIngoingEdgesEats.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("eats")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("eats").containsAll(nodesWithIngoingEdgesEats));
		Assert.assertTrue(nodesWithIngoingEdgesAt.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("at")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("at").containsAll(nodesWithIngoingEdgesAt));
		Assert.assertTrue(nodesWithIngoingEdgesOpposite.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("opposite")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("opposite").containsAll(nodesWithIngoingEdgesOpposite));
	}
	
	@Test
	public void testGraphNodeCreation() {
		// create the graph from JSON like in testGraphDeserialization():
		Graph ferrymansProblemGraph = new Graph(ferrymansProblemJSON);
		Node wolf = ferrymansProblemGraph.getNodeById("w");
		Node goat = ferrymansProblemGraph.getNodeById("g");
		Node cabbage = ferrymansProblemGraph.getNodeById("c");
		Node ferry = ferrymansProblemGraph.getNodeById("ferry");
		Node north = ferrymansProblemGraph.getNodeById("north");
		Node south = ferrymansProblemGraph.getNodeById("south");

		// now create a node with id == "east"
		ferrymansProblemGraph.addNode(new Node(ferrymansProblemGraph, "east", "Bank", null));
		
		Node east = ferrymansProblemGraph.getNodeById("east");
		Assert.assertEquals("east", east.getId());
		Assert.assertEquals("Bank", east.getType());

		// build sets including the expected nodes
		HashSet<Node> nodesWithTypeCargo = new HashSet<Node>(); // type == "Cargo"
		nodesWithTypeCargo.add(wolf);
		nodesWithTypeCargo.add(goat);
		nodesWithTypeCargo.add(cabbage);
		HashSet<Node> nodesWithTypeFerry = new HashSet<Node>(); // type == "Ferry"
		nodesWithTypeFerry.add(ferry);
		HashSet<Node> nodesWithTypeBank = new HashSet<Node>(); // type == "Bank"
		nodesWithTypeBank.add(north);
		nodesWithTypeBank.add(south);
		nodesWithTypeBank.add(east);
		HashSet<Node> nodesWithOutgoingEdgesEats = new HashSet<Node>(); // outgoing edge with name == "eats"
		nodesWithOutgoingEdgesEats.add(wolf);
		nodesWithOutgoingEdgesEats.add(goat);
		HashSet<Node> nodesWithOutgoingEdgesAt = new HashSet<Node>(); // outgoing edge with name == "at"
		nodesWithOutgoingEdgesAt.add(wolf);
		nodesWithOutgoingEdgesAt.add(goat);
		nodesWithOutgoingEdgesAt.add(cabbage);
		nodesWithOutgoingEdgesAt.add(ferry);
		HashSet<Node> nodesWithOutgoingEdgesOpposite = new HashSet<Node>(); // outgoing edge with name == "opposite"
		nodesWithOutgoingEdgesOpposite.add(north);
		nodesWithOutgoingEdgesOpposite.add(south);
		HashSet<Node> nodesWithIngoingEdgesEats = new HashSet<Node>(); // ingoing edge with name == "eats"
		nodesWithIngoingEdgesEats.add(goat);
		nodesWithIngoingEdgesEats.add(cabbage);
		HashSet<Node> nodesWithIngoingEdgesAt = new HashSet<Node>(); // ingoing edge with name == "at"
		nodesWithIngoingEdgesAt.add(north);
		HashSet<Node> nodesWithIngoingEdgesOpposite = new HashSet<Node>(); // ingoing edge with name == "opposite"
		nodesWithIngoingEdgesOpposite.add(north);
		nodesWithIngoingEdgesOpposite.add(south);
		
		// check if exactly those nodes are selected:
		
		// getNodesByType:
		Assert.assertTrue(nodesWithTypeCargo.containsAll(ferrymansProblemGraph.getNodesByType("Cargo")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Cargo").containsAll(nodesWithTypeCargo));
		Assert.assertTrue(nodesWithTypeFerry.containsAll(ferrymansProblemGraph.getNodesByType("Ferry")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Ferry").containsAll(nodesWithTypeFerry));
		Assert.assertTrue(nodesWithTypeBank.containsAll(ferrymansProblemGraph.getNodesByType("Bank")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Bank").containsAll(nodesWithTypeBank));
		// getNodesByOutgoingEdge:
		Assert.assertTrue(nodesWithOutgoingEdgesEats.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("eats")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("eats").containsAll(nodesWithOutgoingEdgesEats));
		Assert.assertTrue(nodesWithOutgoingEdgesAt.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("at")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("at").containsAll(nodesWithOutgoingEdgesAt));
		Assert.assertTrue(nodesWithOutgoingEdgesOpposite.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("opposite")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("opposite").containsAll(nodesWithOutgoingEdgesOpposite));
		// getNodesByIngoingEdge:
		Assert.assertTrue(nodesWithIngoingEdgesEats.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("eats")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("eats").containsAll(nodesWithIngoingEdgesEats));
		Assert.assertTrue(nodesWithIngoingEdgesAt.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("at")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("at").containsAll(nodesWithIngoingEdgesAt));
		Assert.assertTrue(nodesWithIngoingEdgesOpposite.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("opposite")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("opposite").containsAll(nodesWithIngoingEdgesOpposite));
	}
	
	@Test
	public void testGraphEdgeRemoval() {
		// create the graph from JSON like in testGraphDeserialization():
		Graph ferrymansProblemGraph = new Graph(ferrymansProblemJSON);
		Node wolf = ferrymansProblemGraph.getNodeById("w");
		Node goat = ferrymansProblemGraph.getNodeById("g");
		Node cabbage = ferrymansProblemGraph.getNodeById("c");
		Node ferry = ferrymansProblemGraph.getNodeById("ferry");
		Node north = ferrymansProblemGraph.getNodeById("north");
		Node south = ferrymansProblemGraph.getNodeById("south");
		
		// now remove the node with id == "north"
		south.removeOutgoingEdge("opposite", north);

		// build sets including the expected nodes
		HashSet<Node> nodesWithTypeCargo = new HashSet<Node>(); // type == "Cargo"
		nodesWithTypeCargo.add(wolf);
		nodesWithTypeCargo.add(goat);
		nodesWithTypeCargo.add(cabbage);
		HashSet<Node> nodesWithTypeFerry = new HashSet<Node>(); // type == "Ferry"
		nodesWithTypeFerry.add(ferry);
		HashSet<Node> nodesWithTypeBank = new HashSet<Node>(); // type == "Bank"
		nodesWithTypeBank.add(north);
		nodesWithTypeBank.add(south);
		HashSet<Node> nodesWithOutgoingEdgesEats = new HashSet<Node>(); // outgoing edge with name == "eats"
		nodesWithOutgoingEdgesEats.add(wolf);
		nodesWithOutgoingEdgesEats.add(goat);
		HashSet<Node> nodesWithOutgoingEdgesAt = new HashSet<Node>(); // outgoing edge with name == "at"
		nodesWithOutgoingEdgesAt.add(wolf);
		nodesWithOutgoingEdgesAt.add(goat);
		nodesWithOutgoingEdgesAt.add(cabbage);
		nodesWithOutgoingEdgesAt.add(ferry);
		HashSet<Node> nodesWithOutgoingEdgesOpposite = new HashSet<Node>(); // outgoing edge with name == "opposite"
		nodesWithOutgoingEdgesOpposite.add(north);
		HashSet<Node> nodesWithIngoingEdgesEats = new HashSet<Node>(); // ingoing edge with name == "eats"
		nodesWithIngoingEdgesEats.add(goat);
		nodesWithIngoingEdgesEats.add(cabbage);
		HashSet<Node> nodesWithIngoingEdgesAt = new HashSet<Node>(); // ingoing edge with name == "at"
		nodesWithIngoingEdgesAt.add(north);
		HashSet<Node> nodesWithIngoingEdgesOpposite = new HashSet<Node>(); // ingoing edge with name == "opposite"
		nodesWithIngoingEdgesOpposite.add(south);
		
		// check if exactly those nodes are selected:
		
		// getNodesByType:
		Assert.assertTrue(nodesWithTypeCargo.containsAll(ferrymansProblemGraph.getNodesByType("Cargo")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Cargo").containsAll(nodesWithTypeCargo));
		Assert.assertTrue(nodesWithTypeFerry.containsAll(ferrymansProblemGraph.getNodesByType("Ferry")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Ferry").containsAll(nodesWithTypeFerry));
		Assert.assertTrue(nodesWithTypeBank.containsAll(ferrymansProblemGraph.getNodesByType("Bank")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Bank").containsAll(nodesWithTypeBank));
		// getNodesByOutgoingEdge:
		Assert.assertTrue(nodesWithOutgoingEdgesEats.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("eats")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("eats").containsAll(nodesWithOutgoingEdgesEats));
		Assert.assertTrue(nodesWithOutgoingEdgesAt.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("at")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("at").containsAll(nodesWithOutgoingEdgesAt));
		Assert.assertTrue(nodesWithOutgoingEdgesOpposite.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("opposite")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("opposite").containsAll(nodesWithOutgoingEdgesOpposite));
		// getNodesByIngoingEdge:
		Assert.assertTrue(nodesWithIngoingEdgesEats.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("eats")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("eats").containsAll(nodesWithIngoingEdgesEats));
		Assert.assertTrue(nodesWithIngoingEdgesAt.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("at")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("at").containsAll(nodesWithIngoingEdgesAt));
		Assert.assertTrue(nodesWithIngoingEdgesOpposite.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("opposite")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("opposite").containsAll(nodesWithIngoingEdgesOpposite));
	}
	
	@Test
	public void testGraphEdgeCreation() {
		// create the graph from JSON like in testGraphDeserialization():
		Graph ferrymansProblemGraph = new Graph(ferrymansProblemJSON);
		Node wolf = ferrymansProblemGraph.getNodeById("w");
		Node goat = ferrymansProblemGraph.getNodeById("g");
		Node cabbage = ferrymansProblemGraph.getNodeById("c");
		Node ferry = ferrymansProblemGraph.getNodeById("ferry");
		Node north = ferrymansProblemGraph.getNodeById("north");
		Node south = ferrymansProblemGraph.getNodeById("south");
		
		// now create an edge "growsAt" from cabbage to south
		cabbage.addOutgoingEdge("growsAt", south);

		// build sets including the expected nodes
		HashSet<Node> nodesWithTypeCargo = new HashSet<Node>(); // type == "Cargo"
		nodesWithTypeCargo.add(wolf);
		nodesWithTypeCargo.add(goat);
		nodesWithTypeCargo.add(cabbage);
		HashSet<Node> nodesWithTypeFerry = new HashSet<Node>(); // type == "Ferry"
		nodesWithTypeFerry.add(ferry);
		HashSet<Node> nodesWithTypeBank = new HashSet<Node>(); // type == "Bank"
		nodesWithTypeBank.add(north);
		nodesWithTypeBank.add(south);
		HashSet<Node> nodesWithOutgoingEdgesEats = new HashSet<Node>(); // outgoing edge with name == "eats"
		nodesWithOutgoingEdgesEats.add(wolf);
		nodesWithOutgoingEdgesEats.add(goat);
		HashSet<Node> nodesWithOutgoingEdgesAt = new HashSet<Node>(); // outgoing edge with name == "at"
		nodesWithOutgoingEdgesAt.add(wolf);
		nodesWithOutgoingEdgesAt.add(goat);
		nodesWithOutgoingEdgesAt.add(cabbage);
		nodesWithOutgoingEdgesAt.add(ferry);
		HashSet<Node> nodesWithOutgoingEdgesOpposite = new HashSet<Node>(); // outgoing edge with name == "opposite"
		nodesWithOutgoingEdgesOpposite.add(north);
		nodesWithOutgoingEdgesOpposite.add(south);
		HashSet<Node> nodesWithOutgoingEdgesGrowsAt = new HashSet<Node>(); // outgoing edge with name == "opposite"
		nodesWithOutgoingEdgesGrowsAt.add(cabbage);
		HashSet<Node> nodesWithIngoingEdgesEats = new HashSet<Node>(); // ingoing edge with name == "eats"
		nodesWithIngoingEdgesEats.add(goat);
		nodesWithIngoingEdgesEats.add(cabbage);
		HashSet<Node> nodesWithIngoingEdgesAt = new HashSet<Node>(); // ingoing edge with name == "at"
		nodesWithIngoingEdgesAt.add(north);
		HashSet<Node> nodesWithIngoingEdgesOpposite = new HashSet<Node>(); // ingoing edge with name == "opposite"
		nodesWithIngoingEdgesOpposite.add(north);
		nodesWithIngoingEdgesOpposite.add(south);
		HashSet<Node> nodesWithIngoingEdgesGrowsAt = new HashSet<Node>(); // ingoing edge with name == "opposite"
		nodesWithIngoingEdgesGrowsAt.add(south);
		
		// check if exactly those nodes are selected:
		
		// getNodesByType:
		Assert.assertTrue(nodesWithTypeCargo.containsAll(ferrymansProblemGraph.getNodesByType("Cargo")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Cargo").containsAll(nodesWithTypeCargo));
		Assert.assertTrue(nodesWithTypeFerry.containsAll(ferrymansProblemGraph.getNodesByType("Ferry")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Ferry").containsAll(nodesWithTypeFerry));
		Assert.assertTrue(nodesWithTypeBank.containsAll(ferrymansProblemGraph.getNodesByType("Bank")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByType("Bank").containsAll(nodesWithTypeBank));
		// getNodesByOutgoingEdge:
		Assert.assertTrue(nodesWithOutgoingEdgesEats.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("eats")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("eats").containsAll(nodesWithOutgoingEdgesEats));
		Assert.assertTrue(nodesWithOutgoingEdgesAt.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("at")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("at").containsAll(nodesWithOutgoingEdgesAt));
		Assert.assertTrue(nodesWithOutgoingEdgesOpposite.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("opposite")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("opposite").containsAll(nodesWithOutgoingEdgesOpposite));
		Assert.assertTrue(nodesWithOutgoingEdgesGrowsAt.containsAll(ferrymansProblemGraph.getNodesByOutgoingEdge("growsAt")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByOutgoingEdge("growsAt").containsAll(nodesWithOutgoingEdgesGrowsAt));
		// getNodesByIngoingEdge:
		Assert.assertTrue(nodesWithIngoingEdgesEats.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("eats")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("eats").containsAll(nodesWithIngoingEdgesEats));
		Assert.assertTrue(nodesWithIngoingEdgesAt.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("at")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("at").containsAll(nodesWithIngoingEdgesAt));
		Assert.assertTrue(nodesWithIngoingEdgesOpposite.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("opposite")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("opposite").containsAll(nodesWithIngoingEdgesOpposite));
		Assert.assertTrue(nodesWithIngoingEdgesGrowsAt.containsAll(ferrymansProblemGraph.getNodesByIngoingEdge("growsAt")));
		Assert.assertTrue(ferrymansProblemGraph.getNodesByIngoingEdge("growsAt").containsAll(nodesWithIngoingEdgesGrowsAt));
	}
	
}