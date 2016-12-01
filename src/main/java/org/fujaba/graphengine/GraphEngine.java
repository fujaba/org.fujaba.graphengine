package org.fujaba.graphengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.graph.adapter.GraphAdapter;
import org.fujaba.graphengine.graph.adapter.NodeAdapter;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;
import org.fujaba.graphengine.pattern.adapter.PatternEdgeAdapter;
import org.fujaba.graphengine.pattern.adapter.PatternGraphAdapter;
import org.fujaba.graphengine.pattern.adapter.PatternNodeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The GraphEngine is a class that contains methods to handle graphs.
 * 
 * @author Philipp Kolodziej
 */
public class GraphEngine {
	
	private static Gson gson;

	public static Gson getGson() {
		if (gson == null) {
			gson = new GsonBuilder()
					.registerTypeAdapter(Node.class, new NodeAdapter())
					.registerTypeAdapter(Graph.class, new GraphAdapter())
					.registerTypeAdapter(PatternEdge.class, new PatternEdgeAdapter())
					.registerTypeAdapter(PatternNode.class, new PatternNodeAdapter())
					.registerTypeAdapter(PatternGraph.class, new PatternGraphAdapter())
//					.setPrettyPrinting()
//					.serializeNulls()
					.create();
		}
		return gson;
	}
	
	/**
	 * This function checks for this graph and a given sub-graph,
	 * if the sub-graph is isomorph to a sub-graph of this graph and returns the mapping.
	 * 
	 * @param graph the given base-graph
	 * @param subGraph the given sub-graph
	 * @return a mapping from the given sub-graph to nodes of this graph if possible, or null
	 */
	public static HashMap<Node, Node> mappingFrom(Graph subGraph, Graph baseGraph) {
		if (subGraph.getNodes().size() > baseGraph.getNodes().size()) {
			return null;
		}
		ArrayList<ArrayList<Integer>> couldMatch = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < subGraph.getNodes().size(); ++i) {
			Node subNode = subGraph.getNodes().get(i);
			couldMatch.add(new ArrayList<Integer>());
nodeMatch:	for (int j = 0; j < baseGraph.getNodes().size(); ++j) {
				Node node = baseGraph.getNodes().get(j);
				// check existence of outgoing edges and their count:
				for (String key: subNode.getEdges().keySet()) {
					int currentSubNodeEdgeCount = subNode.getEdges(key).size();
					int currentNodeEdgeCount = (node.getEdges(key) == null ? 0 : node.getEdges(key).size());
					if (currentNodeEdgeCount < currentSubNodeEdgeCount) {
						continue nodeMatch;
					}
				}
				// check attributes:
				for (String key: subNode.getAttributes().keySet()) {
					if (!subNode.getAttribute(key).equals(node.getAttribute(key))) {
						continue nodeMatch;
					}
				}
				couldMatch.get(couldMatch.size() - 1).add(j);
			}
			if (couldMatch.get(couldMatch.size() - 1).size() == 0) {
				return null; // no mapping for this node => fail
			}
		}
		
//		int checks = 1; 								// DEBUG
//		for (int i = 0; i < couldMatch.size(); ++i) { 	// DEBUG
//			checks *= couldMatch.get(i).size(); 		// DEBUG
//		} 												// DEBUG
//		System.out.println("there will be " + checks + " checks of " + subGraph.getNodes().size() + " nodes each time:");
		
		// now a depth-first search with backtracking to find the right mapping:
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		for (int i = 0; i < couldMatch.size(); ++i) {
			currentTry.add(0);
		}
		boolean canTryAnother = false;
		do {
			canTryAnother = false;
			HashMap<Node, Node> mapping = new HashMap<Node, Node>(); 
			HashSet<Node> usedNodes = new HashSet<Node>();
			boolean duplicateChoice = false;
			for (int i = 0; i < couldMatch.size(); ++i) {
				Node subNode = subGraph.getNodes().get(i);
				Node node = baseGraph.getNodes().get(couldMatch.get(i).get(currentTry.get(i)));
				mapping.put(subNode, node);
				if (usedNodes.contains(node)) {
					duplicateChoice = true;
					break;
				}
				usedNodes.add(node);
			}
			if (!duplicateChoice) {
				// check targets of outgoing edges:
				boolean mismatch = false; 
edgesMatch:		for (int i = 0; i < subGraph.getNodes().size(); ++i) {
					Node sourceSubNode = subGraph.getNodes().get(i);
					for (String edgeName: sourceSubNode.getEdges().keySet()) {
						for (Node targetSubNode: sourceSubNode.getEdges(edgeName)) {
							Node sourceCheckNode = mapping.get(sourceSubNode);
							Node targetCheckNode = mapping.get(targetSubNode);
							if (!sourceCheckNode.getEdges(edgeName).contains(targetCheckNode)) {
								mismatch = true;
								break edgesMatch;
							}
						}
					}
				}
				if (!mismatch) {
					return mapping; // the mapping was found => success
				}
			}
			// try to find the next valid configuration of currentTry:
			for (int i = 0; i < currentTry.size(); ++i) {
				int currentIndex = currentTry.get(i);
				int maxIndex = couldMatch.get(i).size() - 1;
				if (currentIndex < maxIndex) {
					currentTry.set(i, currentIndex + 1);
					for (int j = 0; j < i; ++j) {
						currentTry.set(j, 0);
					}
					canTryAnother = true;
					break;
				}
			}
		} while (canTryAnother);
		return null; // nothing left to check => fail
	}
	
	/**
	 * This function returns true if the given valid mapping from nodes of a sub-graph work in both directions
	 * 
	 * @param mapping the valid mapping from nodes of a sub-graph to nodes of its base-graph
	 * @return true if the mapping works in both directions, or else false
	 */
	private static boolean mappingIsReversable(HashMap<Node, Node> mapping) {
		HashMap<Node, Node> reverseMapping = new HashMap<Node, Node>();
		// reverse the mapping
		for (Node subNode: mapping.keySet()) {
			reverseMapping.put(mapping.get(subNode), subNode);
		}
		for (Node newSubNode: reverseMapping.keySet()) {
			// check attributes
			for (String attributeName: newSubNode.getAttributes().keySet()) {
				if (reverseMapping.get(newSubNode).getAttribute(attributeName).equals(newSubNode.getAttribute(attributeName))) {
					return false;
				}
			}
			// check edges
			for (String edgeName: newSubNode.getEdges().keySet()) {
				for (Node newTargetSubNode: newSubNode.getEdges(edgeName)) {
					if (!reverseMapping.get(newSubNode).getEdges(edgeName).contains(reverseMapping.get(newTargetSubNode))) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static boolean isIsomorphicSubGraph(Graph subGraph, Graph baseGraph) {
		return mappingFrom(subGraph, baseGraph) != null;
	}
	
	/**
	 * This function returns true if the other graph is isomorph to this graph.
	 * 
	 * @param other the other graph
	 * @return true if the graphs are isomorph
	 */
	public static boolean isIsomorphTo(Graph one, Graph other) {
		if (one.getNodes().size() != other.getNodes().size()) {
			return false;
		}
		HashMap<Node, Node> mapping = mappingFrom(other, one);
		if (mapping != null && mappingIsReversable(mapping)) {
			return true;
		} else{
			return false;
		}
	}

	/**
	 * Returns all separate graphs (parts of this graph with no edges in between) as new graphs.
	 * @return all separate graphs (parts of this graph with no edges in between) as new graphs
	 */
	public static ArrayList<Graph> split(Graph graph) {
		ArrayList<Graph> result = new ArrayList<Graph>();
		if (graph.getNodes().size() <= 1) {
			result.add(graph.clone());
			return result;
		}
		int count = 0;
		Graph clone = graph.clone();
		while (count < graph.getNodes().size()) {
			Graph subGraph = new Graph();
			ArrayList<Node> subGraphNodes = connectedNodes(graph, clone.getNodes().get(0));
			clone.getNodes().removeAll(subGraphNodes);
			subGraph.getNodes().addAll(subGraphNodes);
			result.add(subGraph);
			count += subGraphNodes.size();
		}
		return result;
	}
	
	/**
	 * Returns true if there are no separate graphs (parts of the graph with no edges in between). 
	 * @return true if there are no separate graphs (parts of the graph with no edges in between), otherwise false.
	 */
	public static boolean isConnected(Graph graph) {
		if (graph.getNodes().size() <= 1) {
			return true;
		}
		return connectedNodes(graph, graph.getNodes().get(0)).size() == graph.getNodes().size();
	}
	
	/**
	 * This function basically does a search for all nodes connected to the given node and returns them in an ArrayList<Node>
	 * @param node the node to do the check with
	 * @return all nodes connected to the given node in an ArrayList<Node>
	 */
	private static ArrayList<Node> connectedNodes(Graph graph, Node node) {
		ArrayList<Node> open = new ArrayList<Node>();
		ArrayList<Node> closed = new ArrayList<Node>();
		if (!graph.getNodes().contains(node)) {
			return closed;
		}
		open.add(node);
		while (open.size() > 0) {
			Node current = open.remove(0);
			closed.add(current);
			ArrayList<Node> succ = new ArrayList<Node>();
			for (String edgeName: current.getEdges().keySet()) {
				for (Node outgoing: current.getEdges(edgeName)) {
					if (!open.contains(outgoing) && !closed.contains(outgoing) && !succ.contains(outgoing)) {
						succ.add(outgoing);
					}
				}
			}
			for (Node ingoing: graph.getNodes()) {
				for (String edgeName: ingoing.getEdges().keySet()) {
					if (ingoing.getEdges(edgeName) != null && ingoing.getEdges(edgeName).contains(current)
							&& !open.contains(ingoing) && !closed.contains(ingoing) && !succ.contains(ingoing)) {
						succ.add(ingoing);
						break;
					}
				}
			}
			open.addAll(succ);
		}
		return closed;
	}
	
}
