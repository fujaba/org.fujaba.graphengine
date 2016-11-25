package org.fujaba.graphengine.graph;

import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This is a graph for use in graph transformation systems.
 * 
 * @author Philipp Kolodziej
 */
public class Graph {

	/**
	 * the nodes of this graph (in a HashMap with it's id as a key)
	 */
	private HashMap<String, Node> nodesById = new HashMap<String, Node>();
	/**
	 * the nodes of this graph (in a HashMap of HashMaps with it's type as a key)
	 */
	private HashMap<String, HashSet<Node>> nodesByType = new HashMap<String, HashSet<Node>>();
	/**
	 * the nodes of this graph (in a HashMap of HashMaps with one of it's outgoing edges as a key)
	 */
	private HashMap<String, HashSet<Node>> nodesByOutgoingEdge = new HashMap<String, HashSet<Node>>();
	/**
	 * the nodes of this graph (in a HashMap of HashMaps with one of it's ingoing edges as a key)
	 */
	private HashMap<String, HashSet<Node>> nodesByIngoingEdge = new HashMap<String, HashSet<Node>>();
	
	/**
	 * A constructor to build a graph from its JSON representation
	 * 
	 * @param json the graph's JSON representation
	 */
	public Graph(String json) {
		JsonObject graphObject = new JsonParser().parse(json).getAsJsonObject();
		HashMap<String, HashMap<String, String>> outgoingEdges = new HashMap<String, HashMap<String, String>>();
		for (JsonElement nodeElement: graphObject.getAsJsonArray("nodes")) {
			HashMap<String, String> attributes = new HashMap<String, String>();
			String id = nodeElement.getAsJsonObject().get("id").getAsString();
			String type = nodeElement.getAsJsonObject().get("type").getAsString();
			for (JsonElement attributeElement: nodeElement.getAsJsonObject().getAsJsonArray("attributes")) {
				String name = attributeElement.getAsJsonObject().get("name").getAsString();
				String value = attributeElement.getAsJsonObject().get("value").getAsString();
				attributes.put(name, value);
			}
			Node newNode = new Node(this, id, type, attributes);
			this.addNodeById(newNode);
			this.addNodeByType(newNode);
			outgoingEdges.put(id, new HashMap<String, String>());
			for (JsonElement edgeElement: nodeElement.getAsJsonObject().getAsJsonArray("edges")) {
				String name = edgeElement.getAsJsonObject().get("name").getAsString();
				String target = edgeElement.getAsJsonObject().get("target").getAsString();
				outgoingEdges.get(id).put(name, target);
			}
		}
		for (String sourceId: outgoingEdges.keySet()) {
			HashMap<String, String> currentOutgoingEdges = outgoingEdges.get(sourceId);
			for (String edgeName: currentOutgoingEdges.keySet()) {
				String targetId = currentOutgoingEdges.get(edgeName);
				Node sourceNode = this.nodesById.get(sourceId);
				Node targetNode = this.nodesById.get(targetId);
				sourceNode.addOutgoingEdge(edgeName, targetNode);
			}
		}
	}
	/* METHODS TO HANDLE NODES GENERALLY */
	public HashSet<Node> getNodes() {
		return new HashSet<Node>(this.nodesById.values());
	}
	public void addNode(Node node) {
		this.addNodeById(node);
		this.addNodeByType(node);
		if (node.getOutgoingEdges() != null) {
			for (String outgoingEdgeName: node.getOutgoingEdges().keySet()) {
				this.addNodeByOutgoingEdge(outgoingEdgeName, node);
			}
		}
		if (node.getIngoingEdges() != null) {
			for (String ingoingEdgeName: node.getIngoingEdges().keySet()) {
				this.addNodeByIngoingEdge(ingoingEdgeName, node);
			}
		}
	}
	public void removeNode(Node node) {
		this.removeNodeById(node);
		this.removeNodeByType(node);
		HashMap<String, HashSet<Node>> toRemove = new HashMap<String, HashSet<Node>>();
		for (String outgoingEdgeName: node.getOutgoingEdges().keySet()) {
			for (Node target: node.getOutgoingEdges(outgoingEdgeName)) {
				if (toRemove.get(outgoingEdgeName) == null) {
					toRemove.put(outgoingEdgeName, new HashSet<Node>());
				}
				toRemove.get(outgoingEdgeName).add(target);
			}
			this.removeNodeByOutgoingEdge(outgoingEdgeName, node);
		}
		for (String removeKey: toRemove.keySet()) {
			for (Node removeNode: toRemove.get(removeKey)) {
				node.removeOutgoingEdge(removeKey, removeNode);
			}
		}
		HashMap<String, HashSet<Node>> toRemoveFrom = new HashMap<String, HashSet<Node>>();
		for (String ingoingEdgeName: node.getIngoingEdges().keySet()) {
			for (Node source: node.getIngoingEdges(ingoingEdgeName)) {
				if (toRemoveFrom.get(ingoingEdgeName) == null) {
					toRemoveFrom.put(ingoingEdgeName, new HashSet<Node>());
				}
				toRemoveFrom.get(ingoingEdgeName).add(source);
			}
			this.removeNodeByIngoingEdge(ingoingEdgeName, node);
		}
		for (String removeFromKey: toRemoveFrom.keySet()) {
			for (Node removeFromNode: toRemoveFrom.get(removeFromKey)) {
				removeFromNode.removeOutgoingEdge(removeFromKey, node);
			}
		}
	}
	
	/* METHODS TO HANDLE NODES BY ID */
	public HashMap<String, Node> getNodesById() {
		return this.nodesById;
	}
	public Node getNodeById(String id) {
		if (this.nodesById == null) {
			return null;
		}
		return this.nodesById.get(id);
	}
//	private void setNodesById(HashMap<String, Node> nodes) {
//		this.nodesById = nodes;
//	}
	private void addNodeById(Node node) {
		if (this.nodesById == null) {
			this.nodesById = new HashMap<String, Node>();
		}
		this.nodesById.put(node.getId(), node);
	}
	private void removeNodeById(Node node) {
		if (node == null) {
			return;
		}
		if (this.nodesById == null) {
			return;
		}
		this.nodesById.remove(node.getId());
	}

	/* METHODS TO HANDLE NODES BY TYPE */
	public HashMap<String, HashSet<Node>> getNodesByType() {
		return this.nodesByType;
	}
	public HashSet<Node> getNodesByType(String type) {
		if (this.nodesByType == null) {
			return null;
		}
		return this.nodesByType.get(type);
	}
//	private void setNodesByType(HashMap<String, HashSet<Node>> nodes) {
//		this.nodesByType = nodes;
//	}
	private void addNodeByType(Node node) {
		if (this.nodesByType == null) {
			this.nodesByType = new HashMap<String, HashSet<Node>>();
		}
		if (this.nodesByType.get(node.getType()) == null) {
			this.nodesByType.put(node.getType(), new HashSet<Node>());
		}
		this.nodesByType.get(node.getType()).add(node);
	}
	private void removeNodeByType(Node node) {
		if (node == null) {
			return;
		}
		if (this.nodesByType == null) {
			return;
		}
		if (this.nodesByType.get(node.getType()) == null) {
			return;
		}
		this.nodesByType.get(node.getType()).remove(node);
	}

	/* METHODS TO HANDLE NODES BY OUTGOING EDGE */
	public HashMap<String, HashSet<Node>> getNodesByOutgoingEdge() {
		return this.nodesByOutgoingEdge;
	}
	public HashSet<Node> getNodesByOutgoingEdge(String outgoingEdgeName) {
		if (this.nodesByOutgoingEdge == null) {
			return null;
		}
		return this.nodesByOutgoingEdge.get(outgoingEdgeName);
	}
//	private void setNodesByOutgoingEdge(HashMap<String, HashSet<Node>> nodes) {
//		this.nodesByOutgoingEdge = nodes;
//	}
	protected void addNodeByOutgoingEdge(String outgoingEdgeName, Node node) {
		if (this.nodesByOutgoingEdge == null) {
			this.nodesByOutgoingEdge = new HashMap<String, HashSet<Node>>();
		}
		if (this.nodesByOutgoingEdge.get(outgoingEdgeName) == null) {
			this.nodesByOutgoingEdge.put(outgoingEdgeName, new HashSet<Node>());
		}
		this.nodesByOutgoingEdge.get(outgoingEdgeName).add(node);
	}
	protected void removeNodeByOutgoingEdge(String outgoingEdgeName, Node node) {
		if (node == null) {
			return;
		}
		if (this.nodesByOutgoingEdge == null) {
			return;
		}
		if (this.nodesByOutgoingEdge.get(outgoingEdgeName) == null) {
			return;
		}
		this.nodesByOutgoingEdge.get(outgoingEdgeName).remove(node);
	}

	/* METHODS TO HANDLE NODES BY INGOING EDGE */
	public HashMap<String, HashSet<Node>> getNodesByIngoingEdge() {
		return this.nodesByIngoingEdge;
	}
	public HashSet<Node> getNodesByIngoingEdge(String ingoingEdgeName) {
		if (this.nodesByIngoingEdge == null) {
			return null;
		}
		return this.nodesByIngoingEdge.get(ingoingEdgeName);
	}
//	private void setNodesByIngoingEdge(HashMap<String, HashSet<Node>> nodes) {
//		this.nodesByIngoingEdge = nodes;
//	}
	protected void addNodeByIngoingEdge(String ingoingEdgeName, Node node) {
		if (this.nodesByIngoingEdge == null) {
			this.nodesByIngoingEdge = new HashMap<String, HashSet<Node>>();
		}
		if (this.nodesByIngoingEdge.get(ingoingEdgeName) == null) {
			this.nodesByIngoingEdge.put(ingoingEdgeName, new HashSet<Node>());
		}
		this.nodesByIngoingEdge.get(ingoingEdgeName).add(node);
	}
	protected void removeNodeByIngoingEdge(String ingoingEdgeName, Node node) {
		if (node == null) {
			return;
		}
		if (this.nodesByIngoingEdge == null) {
			return;
		}
		if (this.nodesByIngoingEdge.get(ingoingEdgeName) == null) {
			return;
		}
		this.nodesByIngoingEdge.get(ingoingEdgeName).remove(node);
	}
	
	@Override
	public String toString() {
		String result = "{\"nodes\":[";
		boolean first = true;
		for (String id: this.nodesById.keySet()) {
			result += (!first ? "," : "") + this.nodesById.get(id).toString();
			first = false;
		}
		return result + "]}";
	}

}
