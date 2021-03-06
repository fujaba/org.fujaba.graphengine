package org.fujaba.graphengine.isomorphismtools;

import java.util.ArrayList;
import java.util.HashMap;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

public class IsomorphismHandlerCSPLowHeuristics extends IsomorphismHandler {
	
	private static ArrayList<Node> getDepthFirstSortedNodeList(Graph graph) {
		// obtain all parts of the graph - where each part's nodes are connected with each other:
		ArrayList<Graph> splitted = GraphEngine.split(graph, true);
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Graph connectedGraph: splitted) {
			// add the nodes to the node-list (note: each part's nodes are already sorted in a depth-first explore's order):
			nodes.addAll(connectedGraph.getNodes());
		}
		return nodes;
	}
	
	@Override
	public boolean isIsomorphTo(Graph aInitial, Graph b) {
		if (aInitial.getNodes().size() != b.getNodes().size()) {
			// too many sub-graph nodes => fail
			return false;
		}
		if (aInitial.getNodes().size() == 0) {
			// no nodes in sub-graph => empty mapping is the match (success)
			return true;
		}
		// now I'm trying to find 'loosely matched candidates':
		Graph a = aInitial.clone();
		ArrayList<ArrayList<Node>> couldMatch2 = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < a.getNodes().size(); ++i) {
			Node aNode = a.getNodes().get(i);
			couldMatch2.add(new ArrayList<Node>());
nodeMatch:	for (int j = 0; j < b.getNodes().size(); ++j) {
				Node bNode = b.getNodes().get(j);
				// check existence of outgoing edges and their count:
				for (String key: aNode.getEdges().keySet()) {
					int currentSubNodeEdgeCount = aNode.getEdges(key).size();
					int currentNodeEdgeCount = (bNode.getEdges(key) == null ? 0 : bNode.getEdges(key).size());
					if (currentNodeEdgeCount != currentSubNodeEdgeCount) {
						continue nodeMatch;
					}
				}
				// check existence of outgoing edges and their count (other side):
				for (String key: bNode.getEdges().keySet()) {
					int currentSubNodeEdgeCount = bNode.getEdges(key).size();
					int currentNodeEdgeCount = (aNode.getEdges(key) == null ? 0 : aNode.getEdges(key).size());
					if (currentNodeEdgeCount != currentSubNodeEdgeCount) {
						continue nodeMatch;
					}
				}
				// check attributes:
				for (String key: aNode.getAttributes().keySet()) {
					if (!aNode.getAttribute(key).equals(bNode.getAttribute(key))) {
						continue nodeMatch;
					}
				}
				// check attributes (other side):
				for (String key: bNode.getAttributes().keySet()) {
					if (!bNode.getAttribute(key).equals(aNode.getAttribute(key))) {
						continue nodeMatch;
					}
				}
				couldMatch2.get(couldMatch2.size() - 1).add(bNode);
			}
			if (couldMatch2.get(couldMatch2.size() - 1).size() == 0) {
				return false; // no mapping for this node => fail
			}
		}
		couldMatch2 = removeImpossibleCandidates(couldMatch2);
		if (couldMatch2 == null) {
			// after removing 'impossible' candidates, there's no match anymore => fail
			return false;
		}
		if (a.getNodes().size() == 1) {
			// a single node with a candidate is a match => success
			HashMap<Node, Node> singleNodeMapping = new HashMap<Node, Node>();
			singleNodeMapping.put(a.getNodes().get(0), couldMatch2.get(0).get(0));
			return true;
		}
		/*
		 * here I'm starting the application of the heuristics of the maximum restricted variable (H1) and the minimum node order (H2):
		 */
		// first save the old order of the matches:
		HashMap<Node, Integer> oldIndizes = new HashMap<Node, Integer>();
		for (int i = 0; i < a.getNodes().size(); ++i) {
			oldIndizes.put(a.getNodes().get(i), i);
		}
		// now check for the maximum restricted variables (H1):
		ArrayList<Integer> minimumIndices = new ArrayList<Integer>();
		int minimumValue = Integer.MAX_VALUE;
		for (int i = 0; i < a.getNodes().size(); ++i) { // minimum candidates
			if (couldMatch2.get(i).size() <= minimumValue) {
				if (couldMatch2.get(i).size() < minimumValue) {
					minimumIndices = new ArrayList<Integer>();
					minimumValue = couldMatch2.get(i).size();
				}
				minimumIndices.add(i);
			}
		}
		// now check within those for the minimum node order (H2):
		int indicesIndex = -1;
		minimumValue = Integer.MAX_VALUE;
		for (int i = 0; i < minimumIndices.size(); ++i) { // minimum node order (outgoing)
			int outgoingCount = 0;
			Node currentNode = a.getNodes().get(minimumIndices.get(i));
			for (String key: currentNode.getEdges().keySet()) {
				outgoingCount += currentNode.getEdges(key).size();
			}
			if (outgoingCount < minimumValue) {
				minimumValue = outgoingCount;
				indicesIndex = i;
			}
		}
		// here we have the 'best' node to start with:
		Node heuristicallySelectedFirstNode = a.getNodes().get(minimumIndices.get(indicesIndex));
		a.getNodes().remove(heuristicallySelectedFirstNode); // remove from old position
		a.getNodes().add(0, heuristicallySelectedFirstNode); // put in front
		// now order the nodes in a depth-first fashion, with the heuristically selected first node as 'root':
		ArrayList<Node> sortedNodes = getDepthFirstSortedNodeList(a);
		// restore the matches to the new order:
		ArrayList<ArrayList<Node>> couldMatch = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < sortedNodes.size(); ++i) {
			couldMatch.add(couldMatch2.get(oldIndizes.get(sortedNodes.get(i))));
		}
		HashMap<Node, Node> mapping = new HashMap<Node, Node>();
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
					for (String key: mapping.get(currentSubNode).getEdges().keySet()) { // now other side
						if (mapping.get(currentSubNode).getEdges(key).contains(otherSubNode)) {
							if (currentSubNode.getEdges(key) == null || !currentSubNode.getEdges(key).contains(mapping.get(otherSubNode))) {
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
					for (String key: mapping.get(otherSubNode).getEdges().keySet()) { // now other side
						if (mapping.get(otherSubNode).getEdges(key).contains(currentSubNode)) {
							if (otherSubNode.getEdges(key) == null || !otherSubNode.getEdges(key).contains(mapping.get(currentSubNode))) {
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
			return true; // it ran through with no errors => success
		}
		return false; // nothing left to check => fail
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
	public HashMap<Node, Node> mappingFrom(Graph subGraphInitial, Graph baseGraph) {
		if (subGraphInitial.getNodes().size() == 0) {
			// no nodes in sub-graph => empty mapping is the match (success)
			return new HashMap<Node, Node>();
		}
		if (subGraphInitial.getNodes().size() > baseGraph.getNodes().size()) {
			// too many sub-graph nodes => fail
			return null;
		}
		// now I'm trying to find 'loosely matched candidates':
		Graph subGraph = subGraphInitial.clone();
		ArrayList<ArrayList<Node>> couldMatch2 = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < subGraph.getNodes().size(); ++i) {
			Node subNode = subGraph.getNodes().get(i);
			couldMatch2.add(new ArrayList<Node>());
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
				couldMatch2.get(couldMatch2.size() - 1).add(node);
			}
			if (couldMatch2.get(couldMatch2.size() - 1).size() == 0) {
				return null; // no mapping for this node => fail
			}
		}
		couldMatch2 = removeImpossibleCandidates(couldMatch2);
		if (couldMatch2 == null) {
			// after removing 'impossible' candidates, there's no match anymore => fail
			return null;
		}
		if (subGraph.getNodes().size() == 1) {
			// a single node with a candidate is a match => success
			HashMap<Node, Node> singleNodeMapping = new HashMap<Node, Node>();
			singleNodeMapping.put(subGraph.getNodes().get(0), couldMatch2.get(0).get(0));
			return singleNodeMapping;
		}
		/*
		 * here I'm starting the application of the heuristics of the maximum restricted variable (H1) and the minimum node order (H2):
		 */
		// first save the old order of the matches:
		HashMap<Node, Integer> oldIndizes = new HashMap<Node, Integer>();
		for (int i = 0; i < subGraph.getNodes().size(); ++i) {
			oldIndizes.put(subGraph.getNodes().get(i), i);
		}
		// now check for the maximum restricted variables (H1):
		ArrayList<Integer> minimumIndices = new ArrayList<Integer>();
		int minimumValue = Integer.MAX_VALUE;
		for (int i = 0; i < subGraph.getNodes().size(); ++i) { // minimum candidates
			if (couldMatch2.get(i).size() <= minimumValue) {
				if (couldMatch2.get(i).size() < minimumValue) {
					minimumIndices = new ArrayList<Integer>();
					minimumValue = couldMatch2.get(i).size();
				}
				minimumIndices.add(i);
			}
		}
		// now check within those for the minimum node order (H2):
		int indicesIndex = -1;
		minimumValue = Integer.MAX_VALUE;
		for (int i = 0; i < minimumIndices.size(); ++i) { // minimum node order (outgoing)
			int outgoingCount = 0;
			Node currentNode = subGraph.getNodes().get(minimumIndices.get(i));
			for (String key: currentNode.getEdges().keySet()) {
				outgoingCount += currentNode.getEdges(key).size();
			}
			if (outgoingCount < minimumValue) {
				minimumValue = outgoingCount;
				indicesIndex = i;
			}
		}
		// here we have the 'best' node to start with:
		Node heuristicallySelectedFirstNode = subGraph.getNodes().get(minimumIndices.get(indicesIndex));
		subGraph.getNodes().remove(heuristicallySelectedFirstNode); // remove from old position
		subGraph.getNodes().add(0, heuristicallySelectedFirstNode); // put in front
		// now order the nodes in a depth-first fashion, with the heuristically selected first node as 'root':
		ArrayList<Node> sortedNodes = getDepthFirstSortedNodeList(subGraph);
		// restore the matches to the new order:
		ArrayList<ArrayList<Node>> couldMatch = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < sortedNodes.size(); ++i) {
			couldMatch.add(couldMatch2.get(oldIndizes.get(sortedNodes.get(i))));
		}
		HashMap<Node, Node> mapping = new HashMap<Node, Node>();
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
		return "'contraint satisfaction problem'-based isomorphism handler (using heuristics just for the start)";
	}

}
