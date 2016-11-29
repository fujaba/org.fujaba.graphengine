package org.fujaba.graphengine.graph;

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
	 * This function checks for this graph and a given sub-graph,
	 * if the sub-graph is isomorph to a sub-graph of this graph and returns the mapping.
	 * 
	 * @param graph the given base-graph
	 * @param subGraph the given sub-graph
	 * @return a mapping from the given sub-graph to nodes of this graph if possible, or null
	 */
	public HashMap<Node, Node> mappingFrom(Graph subGraph) {
		if (subGraph.getNodes().size() > this.getNodes().size()) {
			return null;
		}
		ArrayList<ArrayList<Integer>> couldMatch = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < subGraph.getNodes().size(); ++i) {
			Node subNode = subGraph.getNodes().get(i);
			couldMatch.add(new ArrayList<Integer>());
nodeMatch:	for (int j = 0; j < this.getNodes().size(); ++j) {
				Node node = this.getNodes().get(j);
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
				Node node = this.getNodes().get(couldMatch.get(i).get(currentTry.get(i)));
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
edgesMatch:		for (int i = 0; i < subGraph.nodes.size(); ++i) {
					Node sourceSubNode = subGraph.nodes.get(i);
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
	
	public boolean hasIsomorphicSubGraph(Graph subGraph) {
		return mappingFrom(subGraph) != null;
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
		HashMap<Node, Node> mapping = mappingFrom(other);
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
	public ArrayList<Graph> split() {
		ArrayList<Graph> result = new ArrayList<Graph>();
		if (nodes.size() <= 1) {
			result.add(this.clone());
			return result;
		}
		int count = 0;
		Graph clone = clone();
		while (count < nodes.size()) {
			ArrayList<Node> subGraphNodes = nodesConnectedTo(clone.nodes.get(0));
			count += subGraphNodes.size();
			Graph subGraph = new Graph();
			subGraph.nodes.addAll(subGraphNodes);
			for (int i = 0; i < subGraphNodes.size(); ++i) {
				clone.removeNode(subGraphNodes.get(i));
			}
			result.add(subGraph.clone());
		}
		return result;
	}
	
	/**
	 * Returns true if there are no separate graphs (parts of the graph with no edges in between). 
	 * @return true if there are no separate graphs (parts of the graph with no edges in between), otherwise false.
	 */
	public boolean isConnected() {
		if (nodes.size() <= 1) {
			return true;
		}
		return nodesConnectedTo(nodes.get(0)).size() == nodes.size();
	}
	
	/**
	 * This function basically does a search for all nodes connected to the given node and returns them in an ArrayList<Node>
	 * @param node the node to do the check with
	 * @return all nodes connected to the given node in an ArrayList<Node>
	 */
	private ArrayList<Node> nodesConnectedTo(Node node) {
		ArrayList<Node> open = new ArrayList<Node>();
		ArrayList<Node> closed = new ArrayList<Node>();
		if (!nodes.contains(node)) {
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
			for (Node ingoing: nodes) {
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
