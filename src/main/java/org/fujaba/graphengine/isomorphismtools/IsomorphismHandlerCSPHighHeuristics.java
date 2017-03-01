package org.fujaba.graphengine.isomorphismtools;

import java.util.ArrayList;
import java.util.HashMap;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

public class IsomorphismHandlerCSPHighHeuristics extends IsomorphismHandler {
	
	@Override
	public boolean isIsomorphTo(Graph a, Graph b) {
		if (a.getNodes().size() != b.getNodes().size()) {
			// different amount of nodes => fail
			return false;
		}
		if (a.getNodes().size() == 0) {
			// no nodes in sub-graph => empty mapping is the match (success)
			return true;
		}
		// now I'm trying to find 'loosely matched candidates':
		ArrayList<ArrayList<Node>> couldMatch = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < a.getNodes().size(); ++i) {
			Node nodeFromA = a.getNodes().get(i);
			couldMatch.add(new ArrayList<Node>());
nodeMatch:	for (int j = 0; j < b.getNodes().size(); ++j) {
				Node nodeFromB = b.getNodes().get(j);
				// check existence of outgoing edges and their count:
				for (String key: nodeFromA.getEdges().keySet()) {
					int currentSubNodeEdgeCount = nodeFromA.getEdges(key).size();
					int currentNodeEdgeCount = (nodeFromB.getEdges(key) == null ? 0 : nodeFromB.getEdges(key).size());
					if (currentNodeEdgeCount != currentSubNodeEdgeCount) {
						continue nodeMatch;
					}
				}
				// the other way around:
				for (String key: nodeFromB.getEdges().keySet()) {
					int currentSubNodeEdgeCount = nodeFromB.getEdges(key).size();
					int currentNodeEdgeCount = (nodeFromA.getEdges(key) == null ? 0 : nodeFromA.getEdges(key).size());
					if (currentNodeEdgeCount != currentSubNodeEdgeCount) {
						continue nodeMatch;
					}
				}
				// check attributes:
				for (String key: nodeFromA.getAttributes().keySet()) {
					if (!nodeFromA.getAttribute(key).equals(nodeFromB.getAttribute(key))) {
						continue nodeMatch;
					}
				}
				// the other way around:
				for (String key: nodeFromB.getAttributes().keySet()) {
					if (!nodeFromB.getAttribute(key).equals(nodeFromA.getAttribute(key))) {
						continue nodeMatch;
					}
				}
				couldMatch.get(couldMatch.size() - 1).add(nodeFromB);
			}
			if (couldMatch.get(couldMatch.size() - 1).size() == 0) {
				return false; // no mapping for this node => fail
			}
		}
		couldMatch = GraphEngine.removeImpossibleCandidates(couldMatch);
		if (couldMatch == null) {
			// after removing 'impossible' candidates, there's no match anymore => fail
			return false;
		}
		if (a.getNodes().size() == 1) {
			// a single node with a candidate is a match => success
			HashMap<Node, Node> singleNodeMapping = new HashMap<Node, Node>();
			singleNodeMapping.put(a.getNodes().get(0), couldMatch.get(0).get(0));
			return true;
		}
		ArrayList<Node> nodeList = new ArrayList<Node>(a.getNodes());
		HashMap<Node, Node> mapping = new HashMap<Node, Node>();
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		for (int i = 0; i < couldMatch.size(); ++i) { // initialize mapping
			currentTry.add(0);
			mapping.put(nodeList.get(i), couldMatch.get(i).get(0));
		}
		heuristicallyChangeNodeOrder(0, nodeList, couldMatch, currentTry);
		int checkIndex = 1;
		int lastIndex = checkIndex - 1;
loop:	while (checkIndex != -1) { // do a depth-first csp-solver backtracking with heuristics:
			if (checkIndex > lastIndex && checkIndex + 1 <= nodeList.size() - 1) {
				heuristicallyChangeNodeOrder(checkIndex + 1, nodeList, couldMatch, currentTry);
			}
			lastIndex = checkIndex;
			for (int i = checkIndex; i < nodeList.size(); ++i) {
				Node currentSubNode = nodeList.get(i);
				boolean fail = false;
match:			for (int j = 0; j < i; ++j) {
					Node otherSubNode = nodeList.get(j);
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
					for (String key: mapping.get(currentSubNode).getEdges().keySet()) { // other way
						if (mapping.get(currentSubNode).getEdges(key).contains(mapping.get(otherSubNode))) {
							if (currentSubNode.getEdges(key) == null || !currentSubNode.getEdges(key).contains(otherSubNode)) {
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
					for (String key: mapping.get(otherSubNode).getEdges().keySet()) { // other way
						if (mapping.get(otherSubNode).getEdges(key).contains(mapping.get(currentSubNode))) {
							if (otherSubNode.getEdges(key) == null || !otherSubNode.getEdges(key).contains(currentSubNode)) {
								fail = true; // missing incoming edge
								break match;
							}
						}
					}
				}
				if (fail) {
					checkIndex = i;
					while (checkIndex >= 0 && currentTry.get(checkIndex) == couldMatch.get(checkIndex).size() - 1) {
						--checkIndex;
					}
					if (checkIndex >= 0) {
						currentTry.set(checkIndex, currentTry.get(checkIndex) + 1);
						mapping.put(nodeList.get(checkIndex), couldMatch.get(checkIndex).get(currentTry.get(checkIndex)));
						for (int j = checkIndex + 1; j < nodeList.size(); ++j) {
							currentTry.set(j, 0);
							mapping.put(nodeList.get(j), couldMatch.get(j).get(0));
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
	public HashMap<Node, Node> mappingFrom(Graph subGraph, Graph baseGraph) {
		if (subGraph.getNodes().size() == 0) {
			// no nodes in sub-graph => empty mapping is the match (success)
			return new HashMap<Node, Node>();
		}
		if (subGraph.getNodes().size() > baseGraph.getNodes().size()) {
			// too many sub-graph nodes => fail
			return null;
		}
		// now I'm trying to find 'loosely matched candidates':
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
		couldMatch = GraphEngine.removeImpossibleCandidates(couldMatch);
		if (couldMatch == null) {
			// after removing 'impossible' candidates, there's no match anymore => fail
			return null;
		}
		if (subGraph.getNodes().size() == 1) {
			// a single node with a candidate is a match => success
			HashMap<Node, Node> singleNodeMapping = new HashMap<Node, Node>();
			singleNodeMapping.put(subGraph.getNodes().get(0), couldMatch.get(0).get(0));
			return singleNodeMapping;
		}
		
		ArrayList<Node> nodeList = new ArrayList<Node>(subGraph.getNodes());
		HashMap<Node, Node> mapping = new HashMap<Node, Node>();
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		for (int i = 0; i < couldMatch.size(); ++i) { // initialize mapping
			currentTry.add(0);
			mapping.put(nodeList.get(i), couldMatch.get(i).get(0));
		}
		heuristicallyChangeNodeOrder(0, nodeList, couldMatch, currentTry);
		int checkIndex = 1;
		int lastIndex = checkIndex - 1;
loop:	while (checkIndex != -1) { // do a depth-first csp-solver backtracking with heuristics:
			if (checkIndex > lastIndex && checkIndex + 1 <= nodeList.size() - 1) {
				heuristicallyChangeNodeOrder(checkIndex + 1, nodeList, couldMatch, currentTry);
			}
			lastIndex = checkIndex;
			for (int i = checkIndex; i < nodeList.size(); ++i) {
				Node currentSubNode = nodeList.get(i);
				boolean fail = false;
match:			for (int j = 0; j < i; ++j) {
					Node otherSubNode = nodeList.get(j);
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
					checkIndex = i;
					while (checkIndex >= 0 && currentTry.get(checkIndex) == couldMatch.get(checkIndex).size() - 1) {
						--checkIndex;
					}
					if (checkIndex >= 0) {
						currentTry.set(checkIndex, currentTry.get(checkIndex) + 1);
						mapping.put(nodeList.get(checkIndex), couldMatch.get(checkIndex).get(currentTry.get(checkIndex)));
						for (int j = checkIndex + 1; j < nodeList.size(); ++j) {
							currentTry.set(j, 0);
							mapping.put(nodeList.get(j), couldMatch.get(j).get(0));
						}
					}
					continue loop;
				}
			}
			return mapping; // it ran through with no errors => success
		}
		return null; // nothing left to check => fail
	}

	protected void heuristicallyChangeNodeOrder(int fromIndex, ArrayList<Node> nodeList, ArrayList<ArrayList<Node>> couldMatch, ArrayList<Integer> currentTry) {
		
		/**
		 * H1 minds the current mapping of earlier nodes, and don't count candidates, that ain't possible right now.
		 * 
		 * H2 only minds edges to future nodes, and minds incoming edges, too!
		 * 
		 */
		
		// now check for the maximum restricted variables (H1):
		ArrayList<Integer> minimumIndices = new ArrayList<Integer>();
		int minimumValue = Integer.MAX_VALUE;
		for (int i = fromIndex; i < nodeList.size(); ++i) { // minimum unassigned candidates for node
			int possibleCandidatesHere = couldMatch.get(i).size();
			for (Node node: couldMatch.get(i)) {
				for (int j = 0; j < fromIndex; ++j) {
					if (couldMatch.get(j).get(currentTry.get(j)) == node) {
						--possibleCandidatesHere;
						break;
					}
				}
			}
			if (possibleCandidatesHere <= minimumValue && possibleCandidatesHere >= 1) {
				if (possibleCandidatesHere < minimumValue) {
					minimumIndices = new ArrayList<Integer>();
					minimumValue = possibleCandidatesHere;
				}
				minimumIndices.add(i);
			}
		}
		if (minimumIndices.size() == 0) { // no nodes to check? then return!
			return;
		}
		// now check within those for the minimum node order (H2):
		int indicesIndex = 0;
		ArrayList<Integer> degrees = new ArrayList<Integer>();
		for (int i = 0; i < nodeList.size(); ++i) {
			degrees.add(0);
		}
		if (minimumIndices.size() > 1) {
			for (int i = fromIndex; i < nodeList.size(); ++i) {
				int outgoingCount = 0;
				for (String key: nodeList.get(i).getEdges().keySet()) {
					for (Node target: nodeList.get(i).getEdges(key)) {
						int targetIndex = nodeList.indexOf(target);
						if (targetIndex >= fromIndex) {
							outgoingCount += 1;
						}
					}
				}
				degrees.set(i, outgoingCount);
			}
			for (int i = fromIndex; i < nodeList.size(); ++i) {
				for (String key: nodeList.get(i).getEdges().keySet()) {
					for (Node target: nodeList.get(i).getEdges(key)) {
						int targetIndex = nodeList.indexOf(target);
						if (targetIndex >= fromIndex) {
							degrees.set(targetIndex, degrees.get(targetIndex) + 1);
						}
					}
				}
			}
		}
		minimumValue = Integer.MAX_VALUE;
		for (int i = 0; i < minimumIndices.size(); ++i) { // minimum node order (outgoing edges only)
			if (degrees.get(i) < minimumValue) {
				minimumValue = degrees.get(i);
				indicesIndex = i;
			}
		}
		// now we switch the node at the given index with a 'better one', that comes later in the list
		int switchIndex = minimumIndices.get(indicesIndex);
		Node tempNode = nodeList.get(fromIndex);
		ArrayList<Node> tempMatches = couldMatch.get(fromIndex);
		nodeList.set(fromIndex, nodeList.get(switchIndex));
		couldMatch.set(fromIndex, couldMatch.get(switchIndex));
		nodeList.set(switchIndex, tempNode);
		couldMatch.set(switchIndex, tempMatches);
	}

	@Override
	public Graph normalized(Graph graph) {
		return GraphEngine.getNormalizationFallback().normalized(graph);
	}

	@Override
	public String toString() {
		return "'contraint satisfaction problem'-based isomorphism handler (using heuristics wherever it helps)";
	}

}
