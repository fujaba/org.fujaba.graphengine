package org.fujaba.graphengine.graph;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

/**
 * This is a Node for use in graph transformation systems.
 * 
 * @author Philipp Kolodziej
 */
public class Node implements Cloneable, Comparable<Node> {
	/**
	 * the graph containing this node
	 */
	private Graph graph;
	/**
	 * the attributes of this node
	 */
	private HashMap<String, Object> attributes = new HashMap<String, Object>();
	/**
	 * the nodes, that are connected with an outgoing edge that has a specific label
	 */
	private HashMap<String, ArrayList<Node>> edges = new HashMap<String, ArrayList<Node>>();
	

	
	public Node(Graph graph, HashMap<String, Object> attributes) {
		this.setGraph(graph);
		this.setAttributes(attributes);
	}
	

	public Graph getGraph() {
		return this.graph;
	}
	public Node setGraph(Graph graph) {
		this.graph = graph;
		return this;
	}
	public HashMap<String, Object> getAttributes() {
		return this.attributes;
	}
	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}
	public Node setAttributes(HashMap<String, Object> attributes) {
		this.attributes = attributes;
		return this;
	}
	public Node setAttribute(String name, Object value) {
		if (this.attributes == null) {
			this.attributes = new HashMap<String, Object>();
		}
		this.attributes.put(name, value);
		return this;
	}
	public Node removeAttribute(String name) {
		if (this.attributes == null) {
			return this;
		}
		this.attributes.remove(name);
		return this;
	}
	public HashMap<String, ArrayList<Node>> getEdges() {
		return this.edges;
	}
	public ArrayList<Node> getEdges(String name) {
		return this.edges.get(name);
	}
	public Node addEdge(String name, Node target) {
		if (this.edges == null) {
			this.edges = new HashMap<String, ArrayList<Node>>();
		}
		if (this.edges.get(name) == null) {
			this.edges.put(name, new ArrayList<Node>());
		}
		this.edges.get(name).add(target);
		return this;
	}
	public Node removeEdge(String name, Node target) {
		if (target == null) {
			return this;
		}
		if (this.edges == null) {
			return this;
		}
		if (this.edges.get(name) == null) {
			return this;
		}
		this.edges.get(name).remove(target);
		return this;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
	
	@Override
	public Node clone() {
		HashMap<String, Object> clonedAttributes = new HashMap<String, Object>();
		for (String key: attributes.keySet()) {
			clonedAttributes.put(key, attributes.get(key)); // attribute value won't be duplicated
		}
		Node clone = new Node(this.graph, clonedAttributes);
		return clone;
	}

	@Override
	public int compareTo(Node o) {
		return this.toString().compareTo(o.toString());
	}

}
