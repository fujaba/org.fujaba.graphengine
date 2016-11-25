package org.fujaba.graphengine.graph;

import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;

import com.google.gson.Gson;

/**
 * This is a graph for use in graph transformation systems.
 * 
 * @author Philipp Kolodziej
 */
public class Graph implements Cloneable, Comparable<Graph> {

	/**
	 * the nodes of this graph (in an ArrayList)
	 */
	private ArrayList<Node> nodes;

	/**
	 * A constructor to create an empty graph
	 */
	public Graph() { }
	/**
	 * A constructor to build a graph from its JSON representation
	 * 
	 * @param json the graph's JSON representation
	 */
	public Graph(String json) {
		Graph that = new Gson().fromJson(json, Graph.class);
		this.nodes = that.nodes;
	}
	/* METHODS TO HANDLE NODES GENERALLY */
	public ArrayList<Node> getNodes() {
		return this.nodes;
	}
	public Graph addNode(Node node) {
		if (this.nodes == null) {
			this.nodes = new ArrayList<Node>();
		}
		this.nodes.add(node);
		return this;
	}
	public Graph removeNode(Node node) {
		if (this.nodes == null) {
			return this;
		}
		this.nodes.remove(node);
		return this;
	}
	
	@Override
	public String toString() {
		return GraphEngine.getGson().toJson(this);
	}
	
	@Override
	public Graph clone() {
		Graph clone = new Graph();
		ArrayList<Node> clonedNodes = new ArrayList<Node>();
		for (Node node: nodes) {
			clonedNodes.add(node.clone().setGraph(clone));
		}
		clone.nodes = clonedNodes;
		return clone;
	}
	
	@Override
	public int compareTo(Graph o) {
		return this.toString().compareTo(o.toString());
	}

}
