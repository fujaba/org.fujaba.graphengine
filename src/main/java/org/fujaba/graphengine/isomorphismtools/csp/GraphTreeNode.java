package org.fujaba.graphengine.isomorphismtools.csp;

import java.util.ArrayList;
import java.util.HashSet;

import org.fujaba.graphengine.graph.Node;

public class GraphTreeNode {

	/**
	 * parent GraphTreeNode
	 */
	private GraphTreeNode parentGraphTreeNode = null;
	/**
	 * subGraph Node (to find a match for)
	 */
	private Node subGraphNode = null;
	/**
	 * baseGraph Node candidates (that could be a match)
	 */
	private ArrayList<Node> baseGraphNodeCandidates = new ArrayList<Node>();
	/**
	 * current baseGraph Node candidate index (that could be the one of the match)
	 */
	private int currentBaseGraphNodeCandidateIndex = 0;
	/**
	 * children GraphTreeNodes
	 */
	private ArrayList<GraphTreeNode> childrenGraphTreeNodes = new ArrayList<GraphTreeNode>();

	public GraphTreeNode(GraphTreeNode parentGraphTreeNode, Node subGraphNode, ArrayList<Node> baseGraphNodeCandidates, int currentBaseGraphNodeCandidateIndex) {
		this.parentGraphTreeNode = parentGraphTreeNode;
		this.subGraphNode = subGraphNode;
		this.baseGraphNodeCandidates = baseGraphNodeCandidates;
		this.currentBaseGraphNodeCandidateIndex = currentBaseGraphNodeCandidateIndex;
		if (parentGraphTreeNode != null) {
			parentGraphTreeNode.addChildGraphTreeNode(this);
		}
	}
	
	/**
	 * returns the current base-graph node candidate as a match for this GraphTreeNode's sub-graph node
	 * based on its baseGraphNodeCandidates and the currentBaseGraphNodeCandidateIndex
	 * @return the current base-graph node candidate as a match for this GraphTreeNode's sub-graph node
	 * based on its baseGraphNodeCandidates and the currentBaseGraphNodeCandidateIndex
	 */
	public Node getCurrentBaseGraphNodeCandidate() {
		if (baseGraphNodeCandidates == null || currentBaseGraphNodeCandidateIndex < 0 || currentBaseGraphNodeCandidateIndex >= baseGraphNodeCandidates.size()) {
			return null;
		}
		return baseGraphNodeCandidates.get(currentBaseGraphNodeCandidateIndex);
	}

	/**
	 * Finds the GraphTreeNode containing a Node that was specified. It look in this GraphTreeNode
	 * and all children GraphTreeNodes. It then returns the GraphTreeNode containing a Node that was specified
	 * or else null.
	 * @param subGraphNode the Node that is contained in the GraphTreeNode that's searched
	 * @return the GraphTreeNode containing a Node that was specified
	 * or else null.
	 */
	public GraphTreeNode findGraphTreeNodeFromSubGraphNode(Node subGraphNode) {
		return findGraphTreeNodeFromSubGraphNode(this, subGraphNode);
	}
	/**
	 * Finds the GraphTreeNode containing a Node that was specified. It look in the specified GraphTreeNode
	 * and all children GraphTreeNodes. It then returns the GraphTreeNode containing a Node that was specified
	 * or else null.
	 * @param rootGraphTreeNode the GraphTreeNode to start the search in
	 * @param subGraphNode the Node that is contained in the GraphTreeNode that's searched
	 * @return the GraphTreeNode containing a Node that was specified
	 * or else null.
	 */
	public static GraphTreeNode findGraphTreeNodeFromSubGraphNode(GraphTreeNode rootGraphTreeNode, Node subGraphNode) {
		if (rootGraphTreeNode.getSubGraphNode() == subGraphNode) {
			return rootGraphTreeNode;
		}
		if (rootGraphTreeNode.getChildrenGraphTreeNodes() != null) {
			for (int i = 0; i < rootGraphTreeNode.getChildrenGraphTreeNodes().size(); ++i) {
				GraphTreeNode check = findGraphTreeNodeFromSubGraphNode(rootGraphTreeNode.getChildrenGraphTreeNodes().get(i), subGraphNode);
				if (check != null) {
					return check;
				}
			}
		}
		return null;
	}
	
	/**
	 * Increments the current GraphTreeNode's currentBaseGraphNodeCandidateIndex if possible.
	 * Else it sets it to 0 and goes up a level to do the same and so on.
	 * It then returns the GraphTreeNode, of which the currentBaseGraphNodeCandidateIndex
	 * was increased, or null if no currentBaseGraphNodeCandidateIndex was increased.
	 * @return the GraphTreeNode, of which the currentBaseGraphNodeCandidateIndex
	 * was increased, or null if no currentBaseGraphNodeCandidateIndex was increased.
	 */
	public GraphTreeNode backtrack() {
		if (baseGraphNodeCandidates != null && currentBaseGraphNodeCandidateIndex < baseGraphNodeCandidates.size() - 1) {
			resetChildren();
			++currentBaseGraphNodeCandidateIndex;
			return skipGraphTreeNodesWithDuplicateCurrentBaseGraphNodeCandidate();
		} else {
			if (parentGraphTreeNode != null) {
				return parentGraphTreeNode.backtrack();
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Resets the currentBaseGraphNodeCandidateIndex of all children GraphTreeNodes of this GraphTreeNode
	 */
	public void resetChildren() {
		if (childrenGraphTreeNodes != null) {
			for (GraphTreeNode graphTreeNode: childrenGraphTreeNodes) {
				graphTreeNode.reset();
			}
		}
	}
	
	/**
	 * Resets the currentBaseGraphNodeCandidateIndex of this GraphTreeNode
	 * and all it's children GraphTreeNodes
	 */
	public void reset() {
		currentBaseGraphNodeCandidateIndex = 0;
		if (childrenGraphTreeNodes != null) {
			for (GraphTreeNode graphTreeNode: childrenGraphTreeNodes) {
				graphTreeNode.reset();
			}
		}
	}
	
	public GraphTreeNode firstChildGraphTreeNodeWithFaultyConnection() { // null means ok
		if (childrenGraphTreeNodes == null || childrenGraphTreeNodes.size() == 0) {
			return null;
		}
		for (GraphTreeNode childGraphTreeNode: childrenGraphTreeNodes) {
			// check the connections between nodes of 'this' and 'childGraphTreeNode':
			Node parentSubNode = this.getSubGraphNode();
			Node childSubNode = childGraphTreeNode.getSubGraphNode();
			Node parentBaseNode = this.getCurrentBaseGraphNodeCandidate();
			Node childBaseNode = childGraphTreeNode.getCurrentBaseGraphNodeCandidate();
			/* 
			 * Note: there has to be any connection between the parentSubNode and the childSubNode,
			 * or they wouldn't be parent and child.
			 * At least one of them has at least one outgoing edge to the other one!
			 * 
			 * If any found connection is not existant in the same way
			 * between parentBaseNode and childBaseNode => 'return childGraphTreeNode; // !!!'
			 */
			for (String key: parentSubNode.getEdges().keySet()) {
				for (Node subNodeTarget: parentSubNode.getEdges(key)) {
					if (subNodeTarget == childSubNode) {
						// there is an edge named 'key' from 'parentSubNode' to 'childSubNode'
						boolean found = false;
						if (parentBaseNode.getEdges(key) != null) {
							for (Node baseNodeTarget: parentBaseNode.getEdges(key)) {
								if (baseNodeTarget == childBaseNode) {
									found = true;
									break;
								}
							}
						} else {
							if (parentBaseNode.getEdges(key) == null && childBaseNode.getEdges(key) == null) {
								found = true;
							}
						}
						if (!found) {
							return childGraphTreeNode;
						}
					}
				}
			}
			for (String key: childSubNode.getEdges().keySet()) {
				for (Node subNodeTarget: childSubNode.getEdges(key)) {
					if (subNodeTarget == parentSubNode) {
						// there is an edge named 'key' from 'childSubNode' to 'parentSubNode'
						boolean found = false;
						if (childBaseNode.getEdges(key) != null) {
							for (Node baseNodeTarget: childBaseNode.getEdges(key)) {
								if (baseNodeTarget == parentBaseNode) {
									found = true;
									break;
								}
							}
						} else {
							if (parentBaseNode.getEdges(key) == null && childBaseNode.getEdges(key) == null) {
								found = true;
							}
						}
						if (!found) {
							return childGraphTreeNode;
						}
					}
				}
			}
			GraphTreeNode firstChildGraphTreeNodeWithFaultyConnection = childGraphTreeNode.firstChildGraphTreeNodeWithFaultyConnection();
			if (firstChildGraphTreeNodeWithFaultyConnection != null) {
				return firstChildGraphTreeNodeWithFaultyConnection;
			}
		}
		return null;
	}
	
	public GraphTreeNode skipGraphTreeNodesWithDuplicateCurrentBaseGraphNodeCandidate() { // null means total fail
		GraphTreeNode skipped = firstGraphTreeNodeWithDuplicateCurrentBaseGraphNodeCandidate(); // null means ok
		if (skipped == null) {
			return this;
		}
		GraphTreeNode backtracked = null;
		while (skipped != null) {
			backtracked = skipped.backtrack(); // null means total fail
			if (backtracked == null) {
				return null; // null means total fail
			}
			skipped = firstGraphTreeNodeWithDuplicateCurrentBaseGraphNodeCandidate(); // null means ok
		}
		return backtracked; // hopefully this is the right one (next valid configuration)
	}

	public GraphTreeNode firstGraphTreeNodeWithDuplicateCurrentBaseGraphNodeCandidate() { // null means ok
		ArrayList<GraphTreeNode> allConnectedGraphTreeNodes = getAllConnectedGraphTreeNodes();
		HashSet<Node> usedCurrentBaseGraphNodeCandidates = new HashSet<Node>();
		for (GraphTreeNode graphTreeNode: allConnectedGraphTreeNodes) {
			Node currentBaseGraphNodeCandidate = graphTreeNode.getCurrentBaseGraphNodeCandidate();
			if (usedCurrentBaseGraphNodeCandidates.contains(currentBaseGraphNodeCandidate)) {
				return graphTreeNode;
			} else {
				usedCurrentBaseGraphNodeCandidates.add(currentBaseGraphNodeCandidate);
			}
		}
		return null;
	}

	public ArrayList<GraphTreeNode> getAllConnectedGraphTreeNodes() {
		ArrayList<GraphTreeNode> result = new ArrayList<GraphTreeNode>();
		GraphTreeNode root = getRootGraphTreeNode();
		result.add(root);
		result.addAll(root.getAllChildrenGraphTreeNodes());
		return result;
	}
	private ArrayList<GraphTreeNode> getAllChildrenGraphTreeNodes() {
		ArrayList<GraphTreeNode> result = new ArrayList<GraphTreeNode>();
		if (childrenGraphTreeNodes == null) {
			return result;
		}
		result.addAll(childrenGraphTreeNodes);
		for (GraphTreeNode graphTreeNode: childrenGraphTreeNodes) {
			result.addAll(graphTreeNode.getAllChildrenGraphTreeNodes());
		}
		return result;
	}
	
	/**
	 * returns the root GraphTreeNode of this GraphTreeNode
	 * @return the root GraphTreeNode of this GraphTreeNode
	 */
	public GraphTreeNode getRootGraphTreeNode() {
		GraphTreeNode current = this;
		while (current.getParentGraphTreeNode() != null) {
			current = current.getParentGraphTreeNode();
		}
		return current;
	}
	
	public GraphTreeNode getParentGraphTreeNode() {
		return parentGraphTreeNode;
	}
	public void setParentGraphTreeNode(GraphTreeNode parentGraphTreeNode) {
		this.parentGraphTreeNode = parentGraphTreeNode;
	}
	public Node getSubGraphNode() {
		return subGraphNode;
	}
	public void setSubGraphNode(Node subGraphNode) {
		this.subGraphNode = subGraphNode;
	}
	public ArrayList<Node> getBaseGraphNodeCandidates() {
		return baseGraphNodeCandidates;
	}
	public void setBaseGraphNodeCandidates(ArrayList<Node> baseGraphNodeCandidates) {
		this.baseGraphNodeCandidates = baseGraphNodeCandidates;
	}
	public int getCurrentBaseGraphNodeCandidateIndex() {
		return currentBaseGraphNodeCandidateIndex;
	}
	public void setCurrentBaseGraphNodeCandidateIndex(int currentBaseGraphNodeCandidateIndex) {
		this.currentBaseGraphNodeCandidateIndex = currentBaseGraphNodeCandidateIndex;
	}
	public ArrayList<GraphTreeNode> getChildrenGraphTreeNodes() {
		return childrenGraphTreeNodes;
	}
	public void setChildrenGraphTreeNodes(ArrayList<GraphTreeNode> childrenGraphTreeNodes) {
		this.childrenGraphTreeNodes = childrenGraphTreeNodes;
	}
	public void addChildGraphTreeNode(GraphTreeNode childGraphTreeNode) {
		this.childrenGraphTreeNodes.add(childGraphTreeNode);
	}
	
}
