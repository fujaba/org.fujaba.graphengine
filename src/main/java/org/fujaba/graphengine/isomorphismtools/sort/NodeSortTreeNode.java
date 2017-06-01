package org.fujaba.graphengine.isomorphismtools.sort;

import java.util.ArrayList;

import org.fujaba.graphengine.graph.Node;

public class NodeSortTreeNode {
	/**
	 * parent NodeSortTreeNode
	 */
	private NodeSortTreeNode parentNodeSortTreeNode = null;
	/**
	 * Graph Node
	 */
	private Node node = null;
	/**
	 * children GraphTreeNodes
	 */
	private ArrayList<NodeSortTreeNode> childrenNodeSortTreeNodes = new ArrayList<NodeSortTreeNode>();

	public NodeSortTreeNode(NodeSortTreeNode parentNodeSortTreeNode, Node node) {
		this.parentNodeSortTreeNode = parentNodeSortTreeNode;
		this.node = node;
		if (parentNodeSortTreeNode != null) {
			parentNodeSortTreeNode.addChildNodeSortTreeNode(this);
		}
	}
	
	public void doInnerSort(ArrayList<NodeSortTree> nodeSortTrees) {
		// sort children:
		ArrayList<NodeSortTreeNode> sortedChildrenNodeSortTreeNodes = new ArrayList<NodeSortTreeNode>();
		for (int i = 0; i < nodeSortTrees.size(); ++i) {
			for (int j = 0; j < childrenNodeSortTreeNodes.size(); ++j) {
				if (nodeSortTrees.get(i).getRootNode() == childrenNodeSortTreeNodes.get(j).getNode()) {
					sortedChildrenNodeSortTreeNodes.add(childrenNodeSortTreeNodes.get(j));
					break;
				}
			}
		}
		childrenNodeSortTreeNodes.clear();
		childrenNodeSortTreeNodes.addAll(sortedChildrenNodeSortTreeNodes);
		// sort the nodes target lists:
		for (String key: node.getEdges().keySet()) {
			ArrayList<Node> sortedTargets = new ArrayList<Node>();
			for (int i = 0; i < nodeSortTrees.size(); ++i) {
				for (int j = 0; j < node.getEdges(key).size(); ++j) {
					Node target = node.getEdges(key).get(j);
					if (target == nodeSortTrees.get(i).getRootNode()) {
						sortedTargets.add(target);
						break;
					}
				}
			}
			node.getEdges(key).clear();
			node.getEdges(key).addAll(sortedTargets);
		}
		// let children do their inner sort:
		for (NodeSortTreeNode nodeSortTreeNode: childrenNodeSortTreeNodes) {
			nodeSortTreeNode.doInnerSort(nodeSortTrees);
		}
	}

	public NodeSortTreeNode getParentNodeSortTreeNode() {
		return parentNodeSortTreeNode;
	}
	public void setParentNodeSortTreeNode(NodeSortTreeNode parentNodeSortTreeNode) {
		this.parentNodeSortTreeNode = parentNodeSortTreeNode;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public ArrayList<NodeSortTreeNode> getChildrenNodeSortTreeNodes() {
		return childrenNodeSortTreeNodes;
	}
	public void setChildrenNodeSortTreeNodes(ArrayList<NodeSortTreeNode> childrenNodeSortTreeNodes) {
		this.childrenNodeSortTreeNodes = childrenNodeSortTreeNodes;
	}
	public void addChildNodeSortTreeNode(NodeSortTreeNode childNodeSortTreeNode) {
		this.childrenNodeSortTreeNodes.add(childNodeSortTreeNode);
	}
	
}
