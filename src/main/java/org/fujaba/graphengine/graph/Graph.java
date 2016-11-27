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
		/* 
		 * just start the recursive search for sub-graphs of this base-graph
		 * with the same number of nodes as the given sub-graph:
		 */
		return hasIsomorphicSubGraph(withNodesThatCouldBeMappedToFrom(subGraph), subGraph, 0);
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
		try {
			Graph versionB = graph;
			if (versionB.getNodes() != null && versionB.getNodes().size() > currentIndex) {
				/*
				 * if possible, the call splits in two sub-calls,
				 * where either the n-th node is removed or not
				 */
				versionB = versionB.clone();
				versionB.removeNode(versionB.getNodes().get(currentIndex));
				if ((graph.getNodes().size() == subGraph.getNodes().size() && advancedIsomorphicSubGraphCheck(graph, subGraph) != null)
						|| (versionB.getNodes().size() == subGraph.getNodes().size() && advancedIsomorphicSubGraphCheck(versionB, subGraph) != null)) {
					/*
					 * until a sub-graph is found that has the right number of nodes and withstands all further testing.
					 */
					return true;
				} else if (graph.getNodes().size() < subGraph.getNodes().size() && versionB.getNodes().size() < subGraph.getNodes().size()) {
					/*
					 * if the current branch of those recursive calls
					 * already has removed too much nodes, it's discarded
					 */
					return false;
				}
			} else if (versionB.getNodes() != null && versionB.getNodes().size() == currentIndex) {
				return (graph.getNodes().size() == subGraph.getNodes().size() && advancedIsomorphicSubGraphCheck(graph, subGraph) != null);
			} else {
				return false;
			}
			/*
			 * the actual split was just prepared earlier and happens here:
			 */
			if (versionB.getNodes() != null && versionB.getNodes().size() > currentIndex) {
				return hasIsomorphicSubGraph(graph, subGraph, currentIndex + 1) || hasIsomorphicSubGraph(versionB, subGraph, currentIndex);
			} else {
				return hasIsomorphicSubGraph(graph, subGraph, currentIndex + 1);
			}
		} catch (IOException e) {
			/*
			 * the only possible IOException is when this function would be calling
			 * advancedIsomorphicSubGraphCheck with wrong arguments.
			 */
			System.err.println("wrong usage of Graph.hasIsomorphicSubGraph");
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
		if (graph.getNodes().size() != subGraph.getNodes().size()) {
			throw new IOException("wrong input - both graphs need to have the same number of nodes for this check!");
		}
		// both graphs must have to same amount of nodes to even get here!
		/*
		 * in the following structure there will be all possible matches of the given sub-graph's nodes
		 * to the constructed sub-graph's nodes:
		 */
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
	public int compareTo(Graph other) {
		if (isIsomorphTo(other)) {
			return 0;
		}
		return GraphEngine.getGson().toJson(this).compareTo(GraphEngine.getGson().toJson(other));
	}

}
