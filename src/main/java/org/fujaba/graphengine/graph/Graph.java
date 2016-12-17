package org.fujaba.graphengine.graph;

import java.util.ArrayList;
import java.util.HashMap;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.IdManager;

/**
 * This is a graph for use in graph transformation systems.
 * 
 * @author Philipp Kolodziej
 */
public class Graph implements Cloneable {

    /**
     * the nodes of this graph (in an ArrayList)
     */
    private ArrayList<Node> nodes = new ArrayList<Node>();

    /**
     * A constructor to create an empty graph
     */
    public Graph() {
    }

    /**
     * A constructor to build a graph from its JSON representation
     * 
     * @param json the graph's JSON representation
     */
    public Graph(String json) {
        Graph that = GraphEngine.getGson().fromJson(json, Graph.class);
        this.nodes = that.nodes;
    }

    public ArrayList<Node> getNodes() {
        return this.nodes;
    }

    public Graph addNode(Node... nodes) {
        if (this.nodes == null) {
            this.nodes = new ArrayList<Node>();
        }
        for (Node node : nodes) {
            this.nodes.add(node);
        }
        return this;
    }

    public Graph removeNode(Node... nodes) {
    	for (Node node: nodes) {
            if (this.nodes == null) {
                return this;
            }
            this.nodes.remove(node);
            for (Node otherNode : this.nodes) {
                otherNode.removeEdgesTo(node);
            }
    	}
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
        HashMap<Long, HashMap<String, ArrayList<Long>>> edgesToAdd = new HashMap<Long, HashMap<String, ArrayList<Long>>>();
        HashMap<Node, Node> newNodes = new HashMap<Node, Node>();
        IdManager idManager = new IdManager();
        for (int i = 0; i < nodes.size(); ++i) {
            Node node = nodes.get(i);
            edgesToAdd.put(idManager.getId(node), new HashMap<String, ArrayList<Long>>());
            newNodes.put(node, node.clone());
            for (String key : node.getEdges().keySet()) {
            	edgesToAdd.get(idManager.getId(node)).put(key, new ArrayList<Long>());
            	for (Node target : node.getEdges().get(key)) {
            		edgesToAdd.get(idManager.getId(node)).get(key).add(idManager.getId(target));
            	}
            }
            clonedNodes.add(newNodes.get(node));
        }
        for (Long sourceKey : edgesToAdd.keySet()) {
        	for (String edgeName : edgesToAdd.get(sourceKey).keySet()) {
        		for (Long targetKey : edgesToAdd.get(sourceKey).get(edgeName)) {
        			newNodes.get(idManager.getObject(sourceKey)).addEdge(edgeName, newNodes.get(idManager.getObject(targetKey)));
        		}
        	}
        }
        clone.nodes = clonedNodes;
        return clone;
    }

    // @Override
    // public int compareTo(Graph other) {
    // if (GraphEngine.isIsomorphTo(other, this)) {
    // return 0;
    // }
    // return GraphEngine.getGson().toJson(this).compareTo(GraphEngine.getGson().toJson(other));
    // }
    //
    // @Override
    // public boolean equals(Object other) {
    // if (!(other instanceof Graph)) {
    // return false;
    // }
    // return compareTo((Graph)other) == 0;
    // }

}
