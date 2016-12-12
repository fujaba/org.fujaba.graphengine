package org.fujaba.graphengine.isomorphismtools;

import java.util.ArrayList;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

/**
 * TODO: fix this (comments, code, everything)
 * 
 * right now, what i envision is that i use this object for a
 * depth-first search with backtracking (search-based isomorphism check),
 * and also for base object to use for a reliable node-sorting (sort-based isomorphism check)
 * 
 * @author Philipp Kolodziej
 *
 */
public class GraphTree {

	private Graph connectedGraph = null;
	private Node rootNode = null;
	private GraphTreeNode rootGraphTreeNode = null;
	
	public GraphTree(Graph connectedGraph, Node rootNode) {
		this.connectedGraph = connectedGraph;
		this.rootNode = rootNode;
		this.rootGraphTreeNode = new GraphTreeNode(null, rootNode, null, 0);
		this.buildGraphTreeNodesFromConnectedGraphAndRootNode();
		this.rootGraphTreeNode.reset();
		this.rootGraphTreeNode.skipGraphTreeNodesWithDuplicateCurrentBaseGraphNodeCandidate();
	}
	
	private void buildGraphTreeNodesFromConnectedGraphAndRootNode() {
		if (connectedGraph == null || rootNode == null || rootGraphTreeNode == null) {
			return;
		}
		// do an 'explore' within the graph, building a tree, that later can be used for further checks:
		ArrayList<GraphTreeNode> open = new ArrayList<GraphTreeNode>();
		ArrayList<Node> openNodes = new ArrayList<Node>();
		ArrayList<GraphTreeNode> closed = new ArrayList<GraphTreeNode>();
		ArrayList<Node> closedNodes = new ArrayList<Node>();
		open.add(rootGraphTreeNode);
		while (open.size() > 0) {
			GraphTreeNode current = open.remove(0);
			Node currentNode = current.getSubGraphNode();
			closed.add(current);
			closedNodes.add(currentNode);
			ArrayList<GraphTreeNode> succ = new ArrayList<GraphTreeNode>();
			ArrayList<Node> succNodes = new ArrayList<Node>();
			for (String edgeName: currentNode.getEdges().keySet()) {
				for (Node outgoingNode: currentNode.getEdges(edgeName)) {
					if (!openNodes.contains(outgoingNode) && !closedNodes.contains(outgoingNode) && !succNodes.contains(outgoingNode)) {
						succ.add(new GraphTreeNode(current, outgoingNode, null, 0));
						succNodes.add(outgoingNode);
					}
				}
			}
			for (Node ingoingNode: connectedGraph.getNodes()) {
				for (String edgeName: ingoingNode.getEdges().keySet()) {
					if (ingoingNode.getEdges(edgeName) != null && ingoingNode.getEdges(edgeName).contains(current.getSubGraphNode())
							&& !openNodes.contains(ingoingNode) && !closedNodes.contains(ingoingNode) && !succNodes.contains(ingoingNode)) {
						succ.add(new GraphTreeNode(current, ingoingNode, null, 0));
						succNodes.add(ingoingNode);
						break;
					}
				}
			}
			open.addAll(succ);
			openNodes.addAll(succNodes);
		}
	}

	public Graph getConnectedGraph() {
		return connectedGraph;
	}
	public void setConnectedGraph(Graph connectedGraph) {
		this.connectedGraph = connectedGraph;
	}
	public Node getRootNode() {
		return rootNode;
	}
	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}
	public GraphTreeNode getRootGraphTreeNode() {
		return rootGraphTreeNode;
	}
	public void setRootGraphTreeNode(GraphTreeNode rootGraphTreeNode) {
		this.rootGraphTreeNode = rootGraphTreeNode;
	}
	
}
