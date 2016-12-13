package org.fujaba.graphengine.isomorphismtools.csp;

import java.util.ArrayList;
import java.util.HashMap;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.isomorphismtools.sort.GraphEngineSortBased;

public class GraphEngineCSPBased extends GraphEngine {

	public static HashMap<Node, Node> mappingFrom(Graph subGraph, Graph baseGraph) {
		if (subGraph.getNodes().size() == 0) {
			return new HashMap<Node, Node>();
		}
		// to start, candidates are matched, just as in the 'combinatorics problem' approach:
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
		if (!isConnected(subGraph)) {
			try {
				throw new Exception("please don't use graphs that ain't connected for now!");
			} catch (Throwable t) {
				t.printStackTrace();
				return null; // TODO: make this compatible for non-connected graphs
			}
		}
		/*
		 * Okay, now the actual search algorithm starts:
		 */
		GraphTree graphTree = new GraphTree(subGraph, subGraph.getNodes().get(0));
		for (int i = 0; i < couldMatch.size(); ++i) {
			GraphTreeNode current = graphTree.getRootGraphTreeNode().findGraphTreeNodeFromSubGraphNode(subGraph.getNodes().get(i));
			ArrayList<Node> currentCandidates = new ArrayList<Node>();
			for (int j = 0; j < couldMatch.get(i).size(); ++j) {
				currentCandidates.add(subGraph.getNodes().get(couldMatch.get(i).get(j)));
			}
			current.setBaseGraphNodeCandidates(currentCandidates);
		}
		/*
		 * now we have a tree, suitable to be used for a depth-first search with backtracking
		 * 
		 * how to use it?
		 * 
		 * if it works:
		 * 1. get that GraphTree's root GraphTreeNode with .getRootGraphTreeNode()
		 * 2. start checking connections!
		 * 2.1. check for all children GraphTreeNodes of the GraphTreeNode, if there are outgoing or ingoing
		 *      connections between the sub-graph Nodes of the children and parent GraphTreeNode. If so,
		 *      do check if those connections are the same between their current base-graph candidates.
		 *      If one of these connections is missing => CALL .backtrack() on the child GraphTreeNode
		 *      and continue the search on the parent of the returned GraphTreeNode, or on the GraphTreeNode itself,
		 *      if it is root. If null is returned by .backtrack(), the whole check failed. If all connections work out,
		 *      check the next node until all checks went positive (MATCH!), or .backtrack() ever returned null (FAIL!).
		 * (about something like that -> gotta figure that out...)
		 */
		GraphTreeNode rootGraphTreeNode = graphTree.getRootGraphTreeNode();
		
		while (true) {
			GraphTreeNode firstFaultyConnection = rootGraphTreeNode.firstChildGraphTreeNodeWithFaultyConnection();
			if (firstFaultyConnection == null) {
				return mappingFromMatchedGraphTree(graphTree);
			} else if (firstFaultyConnection.backtrack() == null) {
				return null;
			} else {
				continue;
			}
		}
	}
	
	private static HashMap<Node, Node> mappingFromMatchedGraphTree(GraphTree graphTree) {
		HashMap<Node, Node> mapping = new HashMap<Node, Node>();
		for (GraphTreeNode graphTreeNode: graphTree.getRootGraphTreeNode().getAllConnectedGraphTreeNodes()) {
			mapping.put(graphTreeNode.getSubGraphNode(), graphTreeNode.getCurrentBaseGraphNodeCandidate());
		}
		return mapping;
	}
	
	/*
	 * THE FOLLOWING METHODS ARE A 100% IDENTICAL COPY FROM ITS SUPERCLASS, CUZ EXTENDING STATIC CLASSES SUCKS
	 */
	
	public static boolean isIsomorphicSubGraph(Graph subGraph, Graph baseGraph) {
		return mappingFrom(subGraph, baseGraph) != null;
	}
	
	public static boolean isIsomorphTo(Graph one, Graph other) {
		if (one.getNodes().size() != other.getNodes().size()) {
			return false;
		}
		System.out.println(one); // TODO: remove debug
		System.out.println(other); // TODO: remove debug
		one = GraphEngineSortBased.normalized(one); // TODO: remove debug
		other = GraphEngineSortBased.normalized(other); // TODO: remove debug
		System.out.println(one); // TODO: remove debug
		System.out.println(other); // TODO: remove debug
		HashMap<Node, Node> mapping = mappingFrom(other, one);
		if (mapping != null && mappingIsReversable(mapping)) {
			return true;
		} else{
			return false;
		}
	}
	
	private static boolean mappingIsReversable(HashMap<Node, Node> mapping) {
		HashMap<Node, Node> reverseMapping = new HashMap<Node, Node>();
		// reverse the mapping
		for (Node subNode: mapping.keySet()) {
			reverseMapping.put(mapping.get(subNode), subNode);
		}
		for (Node newSubNode: reverseMapping.keySet()) {
			// check attributes
			for (String attributeName: newSubNode.getAttributes().keySet()) {
				Object attr1 = reverseMapping.get(newSubNode).getAttribute(attributeName);
				Object attr2 = newSubNode.getAttribute(attributeName);
				if ((attr1 == null && attr2 != null) || (attr1 != null && attr2 == null) || !attr1.equals(attr2)) {
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
	
}
