package org.fujaba.graphengine.isomorphismtools;

import java.util.ArrayList;
import java.util.HashMap;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

public class IsomorphismHandlerDepthFirstBacktracking extends IsomorphismHandler {
	
	private static ArrayList<Node> getDepthFirstSortedNodeList(Graph graph) {
		// obtain all parts of the graph - where each part's nodes are connected with each other:
		ArrayList<Graph> splitted = GraphEngine.split(graph);
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Graph connectedGraph: splitted) {
			// add the nodes to the node-list (note: each part's nodes are already sorted in a depth-first explore's order):
			nodes.addAll(connectedGraph.getNodes());
		}
		return nodes;
	}

	@Override
	/**
	 * This function checks for this graph and a given sub-graph,
	 * if the sub-graph is isomorph to a sub-graph of this graph and returns the mapping.
	 * 
	 * @param graph the given base-graph
	 * @param subGraph the given sub-graph
	 * @return a mapping from the given sub-graph to nodes of this graph if possible, or null
	 */
	public HashMap<Node, Node> mappingFrom(Graph subGraph, Graph baseGraph) {
		if (subGraph.getNodes().size() > baseGraph.getNodes().size()) {
			return null;
		}
		ArrayList<Node> sortedNodes = getDepthFirstSortedNodeList(subGraph);
		ArrayList<ArrayList<Node>> couldMatch = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < sortedNodes.size(); ++i) {
			Node subNode = sortedNodes.get(i);
			couldMatch.add(new ArrayList<Node>());
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
				couldMatch.get(couldMatch.size() - 1).add(node);
			}
			if (couldMatch.get(couldMatch.size() - 1).size() == 0) {
				return null; // no mapping for this node => fail
			}
		}
		couldMatch = removeImpossibleCandidates(couldMatch);
		if (couldMatch == null) {
			return null;
		}
		/**
		 * TODO (optimization):
		 * - we could check each expected edge between each candidate of one node and each candidate of the other node
		 *   (removing those candidates who don't match to any opposite candidate)
		 *   
		 * - we could now again do things like eliminate candidates that are the only candidate for another node
		 */
		// if there's only one node and it has any candidate, that is already a successful match:
		HashMap<Node, Node> mapping = new HashMap<Node, Node>();
		if (couldMatch.size() == 1) {
			mapping.put(sortedNodes.get(0), couldMatch.get(0).get(0));
			return mapping;
		}
		// now going through all valid combinations (that make sense) of those loosely fitted candidates to find a match:
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		for (int i = 0; i < couldMatch.size(); ++i) {
			currentTry.add(0);
			mapping.put(sortedNodes.get(i), couldMatch.get(i).get(0));
		}
		/*
		 * only check this index against previous ones,
		 * if ok, increment and check only that one, and so on
		 */
		int checkIndex = 1;
loop:	while (checkIndex != -1) {
			for (int i = checkIndex; i < sortedNodes.size(); ++i) {
				/*
				 * check sortedNodes.get(i) only against all previous nodes,
				 * if it is duplicate, or any edge (outgoing or incoming) is missing.
				 * if it fails: count this nodes candidate up (++currentTry.get(i)) if possible,
				 * if it can't be counted up, go one level back (i-1) and try increment there and so on.
				 * if nothing can't be counted up, return null (or set checkIndex to -1 and break);
				 * after incrementing a candidate, reset all currentTry-elements after it to 0,
				 * and set the checkIndex to the index of the increment currentTry-element, finally break
				 */
				Node currentSubNode = sortedNodes.get(i);
				boolean fail = false;
match:			for (int j = 0; j < i; ++j) {
					Node otherSubNode = sortedNodes.get(j);
					if (mapping.get(currentSubNode) == mapping.get(otherSubNode)) {
						fail = true; // found duplicate!
						break match;
					}
					for (String key: currentSubNode.getEdges().keySet()) {
						if (currentSubNode.getEdges(key).contains(otherSubNode)) {
							if (!mapping.get(currentSubNode).getEdges(key).contains(mapping.get(otherSubNode))) {
								fail = true; // missing outgoing edge
								break match;
							}
						}
					}
					for (String key: otherSubNode.getEdges().keySet()) {
						if (otherSubNode.getEdges(key).contains(currentSubNode)) {
							if (!mapping.get(otherSubNode).getEdges(key).contains(mapping.get(currentSubNode))) {
								fail = true; // missing incoming edge
								break match;
							}
						}
					}
				}
				if (fail) {
					// found an error with the 'new' candidate at index i
					/*
					 * change candidate of node[i] or if not possible, the next possible earlier one,
					 * reset the ones after it (also update the mapping)
					 * and set checkIndex to the new index to check (the one that got incremented)
					 */
					checkIndex = i;
					while (checkIndex >= 0 && currentTry.get(checkIndex) == couldMatch.get(checkIndex).size() - 1) {
						--checkIndex;
					}
					if (checkIndex >= 0) {
						currentTry.set(checkIndex, currentTry.get(checkIndex) + 1);
						mapping.put(sortedNodes.get(checkIndex), couldMatch.get(checkIndex).get(currentTry.get(checkIndex)));
						for (int j = checkIndex + 1; j < sortedNodes.size(); ++j) {
							currentTry.set(j, 0);
							mapping.put(sortedNodes.get(j), couldMatch.get(j).get(0));
						}
					}
					continue loop;
				}
			}
			return mapping; // it ran through with no errors => success
		}
		return null; // nothing left to check => fail
	}
	
	private ArrayList<ArrayList<Node>> removeImpossibleCandidates(ArrayList<ArrayList<Node>> couldMatch) {
		ArrayList<ArrayList<Node>> cantMatch = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < couldMatch.size(); ++i) {
			cantMatch.add(new ArrayList<Node>());
		}
		// go through all candidates:
		for (int i = 0; i < couldMatch.size(); ++i) {
			if (couldMatch.get(i).size() == 1) {
				// one node has only one candidate, all other nodes can't have this candidate
				for (int j = 0; j < couldMatch.size(); ++j) {
					if (i != j) {
						// 'tell' all other nodes, they can't have this candidate:
						cantMatch.get(j).add(couldMatch.get(i).get(0));
					}
				}
			}
		}
		// go through all candidatzes again:
		for (int i = 0; i < couldMatch.size(); ++i) {
			// remove all impossible candidates:
			for (int j = 0; j < cantMatch.get(i).size(); ++j) {
				couldMatch.get(i).remove(cantMatch.get(i).get(j));
			}
			// if one node has no candidates anymore, return null
			if (couldMatch.get(i).size() < 1) {
				return null;
			}
		}
		return couldMatch;
	}

	@Override
	public Graph normalized(Graph graph) {
		return GraphEngine.getNormalizationFallback().normalized(graph);
	}

	@Override
	public String toString() {
		return "'depth-first backtracking'-based isomorphism handler";
	}

}