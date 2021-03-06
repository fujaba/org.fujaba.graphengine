package org.fujaba.graphengine.graph;

import java.util.ArrayList;
import java.util.HashMap;

import org.fujaba.graphengine.GraphEngine;

/**
 * This is a Node for use in graph transformation systems.
 * 
 * @author Philipp Kolodziej
 */
public class Node implements Cloneable
{
   public static final String TYPE_ATTRIBUTE = "_____TYPE_____";

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

    public Node removeAttribute(String... names) {
    	for (String name: names) {
            if (this.attributes == null) {
                continue;
            }
            this.attributes.remove(name);
    	}
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

    public Node addEdge(String name, Node... targets) {
    	for (Node target: targets) {
            if (this.edges == null) {
                this.edges = new HashMap<String, ArrayList<Node>>();
            }
            if (this.edges.get(name) == null) {
                this.edges.put(name, new ArrayList<Node>());
            }
            if (!this.edges.get(name).contains(target)) {
                this.edges.get(name).add(target);
            }
    	}
        return this;
    }

    public Node removeEdge(String name, Node... targets) {
	    for (Node target: targets) {
	        if (target == null) {
	            continue;
	        }
	        if (this.edges == null) {
	            continue;
	        }
	        if (this.edges.get(name) == null) {
	            continue;
	        }
	        this.edges.get(name).remove(target);
	        if (this.edges.get(name).size() == 0) {
	            this.edges.remove(name);
	        }
	    }
        return this;
    }

    public Node removeEdgesTo(Node... targets) {
	    for (Node target: targets) {
	        ArrayList<String> toRemove = new ArrayList<String>();
	        for (String key : this.edges.keySet()) {
	            if (this.edges.get(key).contains(target)) {
	                toRemove.add(key);
	            }
	        }
	        for (String key : toRemove) {
	            while (this.edges.get(key).contains(target)) {
	                this.edges.get(key).remove(target);
	            }
	            if (this.edges.get(key).size() == 0) {
	                this.edges.remove(key);
	            }
	        }
	    }
        return this;
    }


    @Override
   public Node clone()
   {
        HashMap<String, Object> clonedAttributes = new HashMap<String, Object>();
      for (String key : attributes.keySet())
      {
            clonedAttributes.put(key, attributes.get(key)); // attribute value won't be duplicated
        }
        Node clone = new Node(clonedAttributes);
        return clone;
    }


   @Override
   public String toString()
   {
      return GraphEngine.getGson().toJson(this);
   }

}
