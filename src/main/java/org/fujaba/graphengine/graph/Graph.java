package org.fujaba.graphengine.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.IdManager;

/**
 * This is a graph for use in graph transformation systems.
 * 
 * @author Philipp Kolodziej
 */
public class Graph implements Cloneable, Comparable<Graph> {

	/**
	 * the nodes of this graph (in an ArrayList)
	 */
	private ArrayList<Node> nodes;

	/**
	 * A constructor to create an empty graph
	 */
	public Graph() { }
	/**
	 * A constructor to build a graph from its JSON representation
	 * 
	 * @param json the graph's JSON representation
	 */
	public Graph(String json) {
		Graph that = GraphEngine.getGson().fromJson(json, Graph.class);
		this.nodes = that.nodes;
	}
	/* METHODS TO HANDLE NODES GENERALLY */
	public ArrayList<Node> getNodes() {
		return this.nodes;
	}
	public Graph addNode(Node node) {
		if (this.nodes == null) {
			this.nodes = new ArrayList<Node>();
		}
		this.nodes.add(node);
		return this;
	}
	public Graph removeNode(Node node) {
		if (this.nodes == null) {
			return this;
		}
		HashMap<Node, String> ingoingEdgesToRemove = new HashMap<Node, String>();
		this.nodes.remove(node);
		for (Node otherNode: this.nodes) {
			for (String edgeName: otherNode.getEdges().keySet()) {
				if (otherNode.getEdges(edgeName).contains(node)) {
					ingoingEdgesToRemove.put(otherNode, edgeName);
				}
			}
		}
		for (Node sourceNode: ingoingEdgesToRemove.keySet()) {
			sourceNode.removeEdge(ingoingEdgesToRemove.get(sourceNode), node);
		}
		return this;
	}
	
	public boolean hasIsomorphicSubGraph(Graph subGraph) {
		return hasIsomorphicSubGraph(this, subGraph, 0);
	}
	
	private static boolean hasIsomorphicSubGraph(Graph graph, Graph subGraph, int currentIndex) { //TODO: finish
		try {
			Graph versionB = graph;
			if (versionB.getNodes() != null && versionB.getNodes().size() > currentIndex) {
				versionB = versionB.clone();
				versionB.removeNode(versionB.getNodes().get(currentIndex));
				if ((graph.getNodes().size() == subGraph.getNodes().size() && advancedIsomorphicSubGraphCheck(graph, subGraph))
						|| (versionB.getNodes().size() == subGraph.getNodes().size() && advancedIsomorphicSubGraphCheck(versionB, subGraph))) {
					return true;
				} else if (graph.getNodes().size() < subGraph.getNodes().size() && versionB.getNodes().size() < subGraph.getNodes().size()) {
					return false;
				}
			} else {
				return advancedIsomorphicSubGraphCheck(versionB, subGraph);
			}
			return hasIsomorphicSubGraph(graph, subGraph, currentIndex + 1) || hasIsomorphicSubGraph(versionB, subGraph, currentIndex);
		} catch (Throwable t) {
			return false;
		}
	}
	
	private static boolean advancedIsomorphicSubGraphCheck(Graph graph, Graph subGraph) throws IOException {
		if (graph.getNodes().size() != subGraph.getNodes().size()) {
			throw new IOException("wrong input - both graphs need to have the same number of nodes for this check!");
		}
		// both graphs must have to same amount of nodes to even get here!
		/**
		 * zu jedem Knoten-Index aus subGraph eine Liste möglicher Knoten-Indizes aus graph:
		 */
		ArrayList<ArrayList<Integer>> couldMatch = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < subGraph.getNodes().size(); ++i) {
			Node subNode = subGraph.getNodes().get(i);
			couldMatch.add(new ArrayList<Integer>());
nodeMatch:	for (int j = 0; j < graph.getNodes().size(); ++j) {
				Node node = graph.getNodes().get(j);
				for (String key: subNode.getEdges().keySet()) {
					int currentSubNodeEdgeCount = subNode.getEdges(key).size();
					int currentNodeEdgeCount = (node.getEdges(key) == null ? 0 : node.getEdges(key).size());
					if (currentNodeEdgeCount < currentSubNodeEdgeCount) {
						continue nodeMatch;
					}
				}
				for (String key: subNode.getAttributes().keySet()) {
					if (node.getAttribute(key) != subNode.getAttribute(key)) {
						continue nodeMatch;
					}
				}
				couldMatch.get(couldMatch.size() - 1).add(j);
			}
			if (couldMatch.get(couldMatch.size() - 1).size() == 0) {
				return false;
			}
		}
		// es gibt mindestens einen möglichen Kandidaten in graph für jeden Knoten aus subGraph
		// -> Tiefensuche mit Backtracking
		/**
		 * zu jedem Knoten-Index aus subGraph der aktuelle Index der Liste möglicher Knoten-Indizes aus graph:
		 */
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		for (int i = 0; i < couldMatch.size(); ++i) {
			currentTry.add(0);
		}
		boolean canTryAnother = false;
		do {
			/**
			 * der im Moment zu jedem subNode zugeordnete node:
			 */
			HashMap<Node, Node> sameNodes = new HashMap<Node, Node>(); 
			HashSet<Node> targetNodes = new HashSet<Node>();
			boolean duplicateChoice = false;
			for (int i = 0; i < couldMatch.size(); ++i) {
				Node subNode = subGraph.getNodes().get(i);
				Node node = graph.getNodes().get(couldMatch.get(i).get(currentTry.get(i)));
				sameNodes.put(subNode, node);
				if (targetNodes.contains(node)) {
					duplicateChoice = true;
					break;
				}
				targetNodes.add(node);
			}
			if (!duplicateChoice) {
				// check if all edges between subNodes are met between assoc. nodes, too:
				boolean mismatch = false; 
edgesMatch:		for (int i = 0; i < subGraph.nodes.size(); ++i) {
					Node sourceSubNode = subGraph.nodes.get(i);
					for (String edgeName: sourceSubNode.getEdges().keySet()) {
						for (Node targetSubNode: sourceSubNode.getEdges(edgeName)) {
							Node sourceCheckNode = sameNodes.get(sourceSubNode);
							Node targetCheckNode = sameNodes.get(targetSubNode);
							if (!sourceCheckNode.getEdges(edgeName).contains(targetCheckNode)) {
								mismatch = true;
								break edgesMatch;
							}
						}
					}
				}
				if (!mismatch) {
					return true;
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
		return false;
	}
	
	@Override
	public String toString() {
		return GraphEngine.getGson().toJson(this);
	}
	
	@Override
	public Graph clone() {
		Graph clone = new Graph();
		ArrayList<Node> clonedNodes = new ArrayList<Node>();
		HashMap<Long, HashMap<String, ArrayList<Long>>> edgesToAdd = new HashMap<Long, HashMap<String, ArrayList<Long>>>();
		HashMap<Node, Node> newNodes = new HashMap<Node, Node>();
		IdManager idManager = new IdManager();
		for (Node node: nodes) {
			edgesToAdd.put(idManager.getId(node), new HashMap<String, ArrayList<Long>>());
			newNodes.put(node, node.clone());
			for (String key: node.getEdges().keySet()) {
				edgesToAdd.get(idManager.getId(node)).put(key, new ArrayList<Long>());
				for (Node target: node.getEdges().get(key)) {
					edgesToAdd.get(idManager.getId(node)).get(key).add(idManager.getId(target));
				}
			}
			clonedNodes.add(newNodes.get(node));
		}
		for (Long sourceKey: edgesToAdd.keySet()) {
			for (String edgeName: edgesToAdd.get(sourceKey).keySet()) {
				for (Long targetKey: edgesToAdd.get(sourceKey).get(edgeName)) {
					newNodes.get(idManager.getObject(sourceKey)).addEdge(edgeName, newNodes.get(idManager.getObject(targetKey)));
				}
			}
		}
		clone.nodes = clonedNodes;
		return clone;
	}
	
	@Override
	public int compareTo(Graph o) {
		try {
			if (advancedIsomorphicSubGraphCheck(this, o) && advancedIsomorphicSubGraphCheck(o, this)) {
				return 0;
			}
		} catch (Throwable t) {
		}
		return GraphEngine.getGson().toJson(this).compareTo(GraphEngine.getGson().toJson(o));
	}

}
