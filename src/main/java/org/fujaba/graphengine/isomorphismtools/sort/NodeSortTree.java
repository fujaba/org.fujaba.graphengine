package org.fujaba.graphengine.isomorphismtools.sort;

import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

public class NodeSortTree implements Comparable<NodeSortTree> {
	
	private Graph connectedGraph = null;
	private Node rootNode = null;
	private NodeSortTreeNode rootNodeSortTreeNode = null;
	
	public NodeSortTree(Graph connectedGraph, Node rootNode) {
		// construct an initial NodeSortTree for further sorting
		this.connectedGraph = connectedGraph;
		this.rootNode = rootNode;
		this.rootNodeSortTreeNode = new NodeSortTreeNode(null, rootNode);
		buildNodeSortTreeNodesFromConnectedGraphAndRootNode();
	}

	public void doInnerSort(ArrayList<NodeSortTree> nodeSortTrees) {
		if (rootNodeSortTreeNode != null) {
			rootNodeSortTreeNode.doInnerSort(nodeSortTrees);
		}
	}
	
	private void buildNodeSortTreeNodesFromConnectedGraphAndRootNode() {
		if (connectedGraph == null || rootNode == null || rootNodeSortTreeNode == null) {
			return;
		}
		// do an 'explore' within the graph, building a tree, that later can be used for further checks:
		ArrayList<NodeSortTreeNode> open = new ArrayList<NodeSortTreeNode>();
		ArrayList<Node> openNodes = new ArrayList<Node>();
		ArrayList<NodeSortTreeNode> openNextLevel = new ArrayList<NodeSortTreeNode>();
		ArrayList<Node> openNextLevelNodes = new ArrayList<Node>();
		ArrayList<NodeSortTreeNode> closed = new ArrayList<NodeSortTreeNode>();
		ArrayList<Node> closedNodes = new ArrayList<Node>();
		open.add(rootNodeSortTreeNode);
//		System.out.println(); // TODO: remove debug
//		System.out.println(connectedGraph); // TODO: remove debug
		while (open.size() > 0 || openNextLevel.size() > 0) {
			if (open.size() == 0) {
				open.addAll(openNextLevel);
				openNodes.addAll(openNextLevelNodes);
				openNextLevel.clear();
				openNextLevelNodes.clear();
			}
			NodeSortTreeNode current = open.remove(0);
//			System.out.println("current: " + current.getNode()); // TODO: remove debug
			Node currentNode = current.getNode();
			closed.add(current);
			closedNodes.add(currentNode);
			ArrayList<NodeSortTreeNode> succ = new ArrayList<NodeSortTreeNode>();
			ArrayList<Node> succNodes = new ArrayList<Node>();
			for (String edgeName: currentNode.getEdges().keySet()) {
				for (Node outgoingNode: currentNode.getEdges(edgeName)) {
					if (!openNodes.contains(outgoingNode) && !closedNodes.contains(outgoingNode) && !succNodes.contains(outgoingNode)) {
						succ.add(new NodeSortTreeNode(current, outgoingNode));
						succNodes.add(outgoingNode);
//						System.out.println("outgoing add"); // TODO: remove debug
					}
				}
			}
			for (Node ingoingNode: connectedGraph.getNodes()) {
				for (String edgeName: ingoingNode.getEdges().keySet()) {
					if (ingoingNode.getEdges(edgeName).contains(current.getNode())
							&& !openNodes.contains(ingoingNode) && !closedNodes.contains(ingoingNode) && !succNodes.contains(ingoingNode)) {
						succ.add(new NodeSortTreeNode(current, ingoingNode));
						succNodes.add(ingoingNode);
//						System.out.println("ingoing add"); // TODO: remove debug
						break;
					}
				}
			}
//			System.out.println("succ: " + succNodes); // TODO: remove debug
			openNextLevel.addAll(0, succ); // add at the beginning for breadth-first search
			openNextLevelNodes.addAll(0, succNodes); // add at the beginning for breadth-first search
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
	public NodeSortTreeNode getRootNodeSortTreeNode() {
		return rootNodeSortTreeNode;
	}
	public void setRootNodeSortTreeNode(NodeSortTreeNode rootNodeSortTreeNode) {
		this.rootNodeSortTreeNode = rootNodeSortTreeNode;
	}

	@Override
	public int compareTo(NodeSortTree o) {
		return GraphEngine.getGson().toJson(this).compareTo(GraphEngine.getGson().toJson(o));
	}

}
