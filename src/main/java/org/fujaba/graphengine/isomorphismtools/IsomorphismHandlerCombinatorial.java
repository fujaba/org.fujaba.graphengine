package org.fujaba.graphengine.isomorphismtools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

public class IsomorphismHandlerCombinatorial extends IsomorphismHandler {
	
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
		ArrayList<ArrayList<Node>> couldMatch = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < subGraph.getNodes().size(); ++i) {
			Node subNode = subGraph.getNodes().get(i);
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
		// now going through all valid combinations of those loosely fitted candidates to find a match:
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		for (int i = 0; i < couldMatch.size(); ++i) {
			currentTry.add(0);
		}
		HashSet<Node> usedNodes = new HashSet<Node>();
		// use next duplicate-free configuration (begin)
fix:	for (int k = currentTry.size() - 1; k >= 0; --k) {
			while (usedNodes.contains(couldMatch.get(k).get(currentTry.get(k)))) {
				if (currentTry.get(k) >= couldMatch.get(k).size() - 1) {
					break fix;
				}
				currentTry.set(k, currentTry.get(k) + 1);
			}
			usedNodes.add(couldMatch.get(k).get(currentTry.get(k)));
		} // use next duplicate-free configuration (end)
		boolean canTryAnother = false;
		do {
			canTryAnother = false;
			HashMap<Node, Node> mapping = new HashMap<Node, Node>(); 
			usedNodes = new HashSet<Node>();
			boolean duplicateChoice = false;
			for (int i = 0; i < couldMatch.size(); ++i) {
				Node subNode = subGraph.getNodes().get(i);
				Node node = couldMatch.get(i).get(currentTry.get(i));
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
					usedNodes = new HashSet<Node>(); // use next duplicate-free configuration (begin)
fix:				for (int k = currentTry.size() - 1; k >= 0; --k) {
						while (usedNodes.contains(couldMatch.get(k).get(currentTry.get(k)))) {
							if (currentTry.get(k) >= couldMatch.get(k).size() - 1) {
								break fix;
							}
							currentTry.set(k, currentTry.get(k) + 1);
						}
						usedNodes.add(couldMatch.get(k).get(currentTry.get(k)));
					} // use next duplicate-free configuration (end)
					break;
				}
			}
		} while (canTryAnother);
		return null; // nothing left to check => fail
	}

	@Override
	public Graph normalized(Graph graph) {
		return GraphEngine.getNormalizationFallback().normalized(graph);
	}

	@Override
	public String toString() {
		return "'combinatorics'-based isomorphism handler";
	}

}
