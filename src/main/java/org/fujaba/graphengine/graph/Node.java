package org.fujaba.graphengine.graph;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This is a Node for use in graph transformation systems.
 * 
 * @author Philipp Kolodziej
 */
public class Node {
	/**
	 * the graph containing this node
	 */
	private Graph graph;
	/**
	 * the ID of this node
	 */
	private String id;
	/**
	 * the type of this node
	 */
	private String type;
	/**
	 * the attributes of this node
	 */
	private HashMap<String, String> attributes = new HashMap<String, String>();
	/**
	 * the nodes, that are connected with an outgoing edge that has a specific label
	 */
	private HashMap<String, HashSet<Node>> outgoingEdges = new HashMap<String, HashSet<Node>>();
	/**
	 * the nodes, that are connected with an ingoing edge that has a specific label
	 */
	private HashMap<String, HashSet<Node>> ingoingEdges = new HashMap<String, HashSet<Node>>();
	

	
	public Node(Graph graph, String id, String type, HashMap<String, String> attributes) {
		this.setGraph(graph);
		this.setId(id);
		this.setType(type);
		this.setAttributes(attributes);
	}
	

	public Graph getGraph() {
		return this.graph;
	}
	public void setGraph(Graph graph) {
		this.graph = graph;
	}
	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return this.type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public HashMap<String, String> getAttributes() {
		return this.attributes;
	}
	public String getAttribute(String name) {
		return this.attributes.get(name);
	}
	public void setAttributes(HashMap<String, String> attributes) {
		this.attributes = attributes;
	}
	public void setAttribute(String name, String value) {
		if (this.attributes == null) {
			this.attributes = new HashMap<String, String>();
		}
		this.attributes.put(name, value);
	}
	public void removeAttribute(String name) {
		if (this.attributes == null) {
			return;
		}
		this.attributes.remove(name);
	}
	public HashMap<String, HashSet<Node>> getOutgoingEdges() {
		return this.outgoingEdges;
	}
	public HashSet<Node> getOutgoingEdges(String name) {
		return this.outgoingEdges.get(name);
	}
//	private void setOutgoingEdges(HashMap<String, HashSet<Node>> outgoingEdges) {
//		this.outgoingEdges = outgoingEdges;
//	}
	public void addOutgoingEdge(String name, Node target) {
		if (this.outgoingEdges == null) {
			this.outgoingEdges = new HashMap<String, HashSet<Node>>();
		}
		if (this.outgoingEdges.get(name) == null) {
			this.outgoingEdges.put(name, new HashSet<Node>());
		}
		this.outgoingEdges.get(name).add(target);
		this.getGraph().addNodeByOutgoingEdge(name, this);
		target.addIngoingEdge(name, this);
	}
	public void removeOutgoingEdge(String name, Node target) {
		if (target == null) {
			return;
		}
		if (this.outgoingEdges == null) {
			return;
		}
		if (this.outgoingEdges.get(name) == null) {
			return;
		}
		this.outgoingEdges.get(name).remove(target);
		if (this.outgoingEdges.get(name).size() == 0) {
			this.getGraph().removeNodeByOutgoingEdge(name, this);
		}
		target.removeIngoingEdge(name, this);
	}
	public HashMap<String, HashSet<Node>> getIngoingEdges() {
		return this.ingoingEdges;
	}
	public HashSet<Node> getIngoingEdges(String name) {
		return this.ingoingEdges.get(name);
	}
//	private void setIngoingEdges(HashMap<String, HashSet<Node>> ingoingEdges) {
//		this.ingoingEdges = ingoingEdges;
//	}
	private void addIngoingEdge(String name, Node source) {
		if (this.ingoingEdges == null) {
			this.ingoingEdges = new HashMap<String, HashSet<Node>>();
		}
		if (this.ingoingEdges.get(name) == null) {
			this.ingoingEdges.put(name, new HashSet<Node>());
		}
		this.ingoingEdges.get(name).add(source);
		this.getGraph().addNodeByIngoingEdge(name, this);
	}
	private void removeIngoingEdge(String name, Node source) {
		if (source == null) {
			return;
		}
		if (this.ingoingEdges == null) {
			return;
		}
		if (this.ingoingEdges.get(name) == null) {
			return;
		}
		this.ingoingEdges.get(name).remove(source);
		if (this.ingoingEdges.get(name).size() == 0) {
			this.getGraph().removeNodeByIngoingEdge(name, this);
		}
	}
	
	@Override
	public String toString() {
		String result = "{";
		result += "\"id\":\"" + this.id + "\",";
		result += "\"type\":\"" + this.type + "\",";
		result += "\"attributes\":[";
		boolean first = true;
		for (String name: this.attributes.keySet()) {
			result += (!first ? "," : "") + "{\"name\":\"" + name + "\",";
			result += "\"value\":\"" + this.attributes.get(name) + "\"}";
			first = false;
		}
		result += "],";
		result += "\"edges\":[";
		first = true;
		for (String name: this.outgoingEdges.keySet()) {
			for (Node node: this.outgoingEdges.get(name)) {
				result += (!first ? "," : "") + "{\"name\":\"" + name + "\",";
				result += "\"target\":\"" + node.getId() + "\"}";
				first = false;
			}
		}
		result += "]";
		return result + "}";
	}

}
