package org.fujaba.graphengine.graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a Node for use in graph transformation systems.
 * 
 * @author Philipp Kolodziej
 */
public class Node implements Cloneable {
	/**
	 * the attributes of this node
	 */
	private HashMap<String, Object> attributes = new HashMap<String, Object>();
	/**
	 * the nodes, that are connected with an outgoing edge that has a specific label
	 */
	private HashMap<String, ArrayList<Node>> edges = new HashMap<String, ArrayList<Node>>();
	


	public Node() {
	}
	
	public Node(HashMap<String, Object> attributes) {
		this.setAttributes(attributes);
	}
	

	public HashMap<String, Object> getAttributes() {
		if (this.attributes == null) {
			this.attributes = new HashMap<String, Object>();
		}
		return this.attributes;
	}
	public Object getAttribute(String name) {
		if (this.attributes == null) {
			this.attributes = new HashMap<String, Object>();
		}
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
		if (this.edges == null) {
			this.edges = new HashMap<String, ArrayList<Node>>();
		}
		return this.edges;
	}
	public ArrayList<Node> getEdges(String name) {
		if (this.edges == null) {
			this.edges = new HashMap<String, ArrayList<Node>>();
		}
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
		if (this.edges.get(name).size() == 0) {
			this.edges.remove(name);
		}
		return this;
	}
	public Node removeEdgesTo(Node target) {
		ArrayList<String> toRemove = new ArrayList<String>();
		for (String key: this.edges.keySet()) {
			if (this.edges.get(key).contains(target)) {
				toRemove.add(key);
			}
		}
		for (String key: toRemove) {
			while (this.edges.get(key).contains(target)) {
				this.edges.get(key).remove(target);
			}
			if (this.edges.get(key).size() == 0) {
				this.edges.remove(key);
			}
		}
		return this;
	}
	
	@Override
	public Node clone() {
		HashMap<String, Object> clonedAttributes = new HashMap<String, Object>();
		for (String key: attributes.keySet()) {
			clonedAttributes.put(key, attributes.get(key)); // attribute value won't be duplicated
		}
		Node clone = new Node(clonedAttributes);
		return clone;
	}

}
