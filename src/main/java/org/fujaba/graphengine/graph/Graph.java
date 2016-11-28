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
	private ArrayList<Node> nodes = new ArrayList<Node>();

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
		this.nodes.remove(node);
		for (Node otherNode: this.nodes) {
			otherNode.removeEdgesTo(node);
		}
		return this;
	}
	
	/**
	 * This function returns true if the given subGraph is isomorph to a sub-graph of this graph.
	 * 
	 * @param subGraph the given sub-graph to check for in this graph
	 * @return true if the given subGraph is isomorph to a sub-graph of this graph, or else false.
	 */
	public boolean hasIsomorphicSubGraph(Graph subGraph) {
		if (subGraph.getNodes().size() > this.getNodes().size()) {
			return false;
		}
		if (subGraph.getNodes().size() == 0) {
			return true;
		}
		Graph reducedGraph = this.withNodesThatCouldBeMappedToFrom(subGraph);
		if (subGraph.getNodes().size() > reducedGraph.getNodes().size()) {
			return false;
		}
		/* 
		 * just start the recursive search for sub-graphs of this base-graph
		 * with the same number of nodes as the given sub-graph:
		 */
		return hasIsomorphicSubGraph(reducedGraph, subGraph, 0);
	}
	
	/**
	 * This helper-function gives a copy of this graph, where all nodes have been removed,
	 * which can't possible be a match for any nodes of the given sub-graph.
	 * 
	 * This is for use in checks for isomorphic sub-graphs, to drasticly increase performance in many cases.
	 * 
	 * @param subGraph the given sub-graph to be checked against
	 * @return a copy of this graph, where all nodes have been removed,
	 * which can't possible be a match for any nodes of the given sub-graph.
	 */
	private Graph withNodesThatCouldBeMappedToFrom(Graph subGraph) {
		Graph resultingGraph = this.clone();
		ArrayList<Node> nodesToRemove = new ArrayList<Node>();
		for (Node node: resultingGraph.getNodes()) {
			boolean foundMatch = false;
match:		for (Node subNode: subGraph.getNodes()) {
				for (String key: subNode.getEdges().keySet()) {
					int currentSubNodeEdgeCount = subNode.getEdges(key).size();
					int currentNodeEdgeCount = (node.getEdges(key) == null ? 0 : node.getEdges(key).size());
					if (currentNodeEdgeCount < currentSubNodeEdgeCount) {
						continue match;
					}
				}
				for (String key: subNode.getAttributes().keySet()) {
					if (!subNode.getAttribute(key).equals(node.getAttribute(key))) {
						continue match;
					}
				}
				foundMatch = true;
				break match;
			}
			if (!foundMatch) {
				nodesToRemove.add(node);
			}
		}
		for (Node node: nodesToRemove) {
			resultingGraph.removeNode(node);
		}
		return resultingGraph;
	}
	
	/**
	 * Recursive helper-function to check for an isomorphic sub-graph.
	 * @param graph the given base-graph
	 * @param subGraph the given sub-graph
	 * @param currentIndex the index of nodes of the base-graph to start working with
	 * @return true if the given subGraph is isomorph to a sub-graph of this graph, or else false.
	 */
	private static boolean hasIsomorphicSubGraph(Graph graph, Graph subGraph, int currentIndex) {
//		System.out.println("hasIsomorphicSubGraph " + currentIndex + "/" + graph.getNodes().size());
		try {
			if (graph.getNodes().size() > currentIndex) {
				Graph clone = graph.clone();
				clone.removeNode(clone.getNodes().get(currentIndex));
				if ((graph.getNodes().size() == subGraph.getNodes().size() && advancedIsomorphicSubGraphCheck(graph, subGraph) != null)
						|| (clone.getNodes().size() == subGraph.getNodes().size() && advancedIsomorphicSubGraphCheck(clone, subGraph) != null)) {
					return true; // either with or without the node currentIndex, it worked
				} else if (graph.getNodes().size() < subGraph.getNodes().size()) {
					return false; // too many nodes were removed
				} else {
					return hasIsomorphicSubGraph(clone, subGraph, currentIndex) || hasIsomorphicSubGraph(graph, subGraph, currentIndex + 1);
				}
			} else {
				return false; // all combinations were tried
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * This helper-function checks for a given base-graph and a given sub-graph with the same number of nodes,
	 * if the sub-graph is isomorph to a sub-graph of the base-graph.
	 * 
	 * @param graph the given base-graph with the same number of nodes as the sub-graph
	 * @param subGraph the given sub-graph with the same number of nodes as the base-graph
	 * @return true if the given subGraph is isomorph to a sub-graph of this graph, or else false.
	 * @throws IOException is thrown if the graphs don't have the same number of nodes.
	 */
	private static HashMap<Node, Node> advancedIsomorphicSubGraphCheck(Graph graph, Graph subGraph) throws IOException {
//		System.out.println("advancedIsomorphicSubGraphCheck");
		if (graph.getNodes().size() != subGraph.getNodes().size()) {
			throw new IOException("wrong input - both graphs need to have the same number of nodes for this check!");
		}
		// both graphs must have to same amount of nodes to even get here!
		/*
		 * in the following structure there will be all possible matches of the given sub-graph's nodes
		 * to the constructed sub-graph's nodes:
		 */
//		System.out.println("\ngraph");
//		System.out.println(graph);
//		System.out.println("subGraph");
//		System.out.println(subGraph);
//		if ((graph.toString().equals(subGraph.toString()))) {
//			System.out.println("BANG!");
//		}
		ArrayList<ArrayList<Integer>> couldMatch = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < subGraph.getNodes().size(); ++i) {
			Node subNode = subGraph.getNodes().get(i);
			couldMatch.add(new ArrayList<Integer>());
nodeMatch:	for (int j = 0; j < graph.getNodes().size(); ++j) {
				Node node = graph.getNodes().get(j);
				/*
				 * the possible match must have at least as much outgoing edges of each type,
				 * as the original node from the given sub-graph.
				 */
				for (String key: subNode.getEdges().keySet()) {
					int currentSubNodeEdgeCount = subNode.getEdges(key).size();
					int currentNodeEdgeCount = (node.getEdges(key) == null ? 0 : node.getEdges(key).size());
					if (currentNodeEdgeCount < currentSubNodeEdgeCount) {
						continue nodeMatch;
					}
				}
				/*
				 * the possible match must also have each attribute of the given sub-graph's node,
				 * including the same value.
				 */
				for (String key: subNode.getAttributes().keySet()) {
					if (!subNode.getAttribute(key).equals(node.getAttribute(key))) {
						continue nodeMatch;
					}
				}
				couldMatch.get(couldMatch.size() - 1).add(j);
			}
			/*
			 * if some node has no possible match, the check already failed:
			 */
			if (couldMatch.get(couldMatch.size() - 1).size() == 0) {
				return null;
			}
		}
		/*
		 * now that the possible matches for each node are known,
		 * what's left is a depth-first search with backtracking to find the right ones:
		 */
		/*
		 * the following structure contains the current index of possible matches for each node.
		 */
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		for (int i = 0; i < couldMatch.size(); ++i) {
			currentTry.add(0);
		}
		boolean canTryAnother = false;
		do {
			canTryAnother = false;
			/*
			 * the following structure contains the currently associated nodes
			 * to each node from the given sub-graph
			 */
			HashMap<Node, Node> sameNodes = new HashMap<Node, Node>(); 
			/*
			 * the following structure contains all nodes from the constructed sub-graph,
			 * that are already target of association - to track if no nodes is used multiple times.
			 */
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
				/*
				 * attributes are already checked, the number of edges with a certain name, too.
				 * but what's left now is to check if the edges have the right targets:
				 */
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
					/*
					 * if there was no mismatch, this is actually the valid mapping
					 * from the given sub-graph to a constructed sub-graph
					 * of the original base-graph.
					 */
					return sameNodes;
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
		/*
		 * there are no possible matches left, so the check has failed.
		 */
		return null;
	}
	
	/**
	 * This function returns true if the given valid mapping from nodes of a subgraph work in both directions
	 * 
	 * @param knownMapping the valid mapping from nodes of a subgraph to nodes of another graph
	 * @return true if the mapping works in both directions, or else false
	 */
	private boolean reverseIsomorphicSubGraphCheck(HashMap<Node, Node> nodeMapping) {
		/*
		 * if advancedIsomorphicSubGraphCheck has already found a mapping
		 * from a sub-graph to its base-graph,
		 * then you could interchange the base- and sub-graph and check for an
		 * isomorphic sub-graph again, to see if both graphs are isomorphic too each other.
		 * 
		 * but it saves some time to take the returned mapping and just check,
		 * if the attributes and edges match the other way around, too.
		 */
		HashMap<Node, Node> reverseMapping = new HashMap<Node, Node>();
		/*
		 * reverse the mapping:
		 */
		for (Node subNode: nodeMapping.keySet()) {
			reverseMapping.put(nodeMapping.get(subNode), subNode);
		}
		for (Node newSubNode: reverseMapping.keySet()) {
			/*
			 * check the attributes for each node:
			 */
			for (String attributeName: newSubNode.getAttributes().keySet()) {
				if (reverseMapping.get(newSubNode).getAttribute(attributeName) != newSubNode.getAttribute(attributeName)) {
					return false;
				}
			}
			/*
			 * check the edges for each node:
			 */
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
	
	/**
	 * This function returns true if the other graph is isomorph to this graph.
	 * 
	 * @param other the other graph
	 * @return true if the graphs are isomorph
	 */
	public boolean isIsomorphTo(Graph other) {
		if (this.getNodes().size() != other.getNodes().size()) {
			return false;
		}
		/*
		 * the check is done by using advancedIsomorphicSubGraphCheck
		 * and then checking for the other way around with reverseIsomorphicSubGraphCheck:
		 */
		try {
			HashMap<Node, Node> mapping = advancedIsomorphicSubGraphCheck(this, other);
			if (mapping != null && reverseIsomorphicSubGraphCheck(mapping)) {
				return true;
			}
		} catch (Throwable t) {
		}
		return false;
	}

	public boolean isConnected() {
		if (nodes.size() == 0) {
			return true;
		}
		return isConnected(nodes.get(0));
	}
	private boolean isConnected(Node node) {
		if (!nodes.contains(node)) {
			return false;
		}
		ArrayList<Node> open = new ArrayList<Node>();
		ArrayList<Node> closed = new ArrayList<Node>();
		open.add(node);
		while (open.size() > 0) {
//			System.out.println("open = " + open);
//			System.out.println("closed = " + closed);
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
			for (Node ingoing: nodes) {
				for (String edgeName: ingoing.getEdges().keySet()) {
					if (ingoing.getEdges(edgeName) != null && ingoing.getEdges(edgeName).contains(current) && !open.contains(ingoing) && !closed.contains(ingoing) && !succ.contains(ingoing)) {
						succ.add(ingoing);
						break;
					}
				}
			}
			open.addAll(succ);
		}
		return closed.size() == nodes.size();
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
		for (int i = 0; i < nodes.size(); ++i) {
			Node node = nodes.get(i);
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
	public int compareTo(Graph other) {
		if (isIsomorphTo(other)) {
			return 0;
		}
		return GraphEngine.getGson().toJson(this).compareTo(GraphEngine.getGson().toJson(other));
	}

}
