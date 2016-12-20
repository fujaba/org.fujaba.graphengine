package org.fujaba.graphengine.isomorphismtools.heuristics;

import org.fujaba.graphengine.graph.Node;

public class NodeWithConflict implements Comparable<NodeWithConflict> {
	
	private Node node = null;
	private int conflict = 0;
	
	public NodeWithConflict(Node node) {
		this.node = node;
	}
	public Node getNode() {
		return node;
	}
	public int getConflict() {
		return conflict;
	}
	public void resetConflict() {
		this.conflict = 0;
	}
	public void incrementConflict() {
		++this.conflict;
	}
	
	@Override
	public int compareTo(NodeWithConflict other) {
		return this.getConflict() - other.getConflict();
	}
	
}
