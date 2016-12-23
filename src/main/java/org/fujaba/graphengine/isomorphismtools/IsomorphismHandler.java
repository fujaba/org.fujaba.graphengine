package org.fujaba.graphengine.isomorphismtools;

import java.util.HashMap;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

abstract public class IsomorphismHandler {

	abstract public Graph normalized(Graph graph);
	abstract public HashMap<Node, Node> mappingFrom(Graph subGraph, Graph baseGraph);
	abstract public String toString();
	
	public boolean isIsomorphicSubGraph(Graph subGraph, Graph baseGraph) {
		return mappingFrom(subGraph, baseGraph) != null;
	}
	
	public boolean isIsomorphTo(Graph one, Graph other) {
		if (one.getNodes().size() != other.getNodes().size()) {
			return false;
		}
		HashMap<Node, Node> mapping = mappingFrom(other, one);
		if (mapping != null && mappingIsReversable(mapping)) {
			return true;
		} else{
			return false;
		}
	}
	
	private boolean mappingIsReversable(HashMap<Node, Node> mapping) {
		HashMap<Node, Node> reverseMapping = new HashMap<Node, Node>();
		// reverse the mapping
		for (Node subNode: mapping.keySet()) {
			reverseMapping.put(mapping.get(subNode), subNode);
		}
		for (Node newSubNode: reverseMapping.keySet()) {
			// check attributes
			for (String attributeName: newSubNode.getAttributes().keySet()) {
				Object attr1 = reverseMapping.get(newSubNode).getAttribute(attributeName);
				Object attr2 = newSubNode.getAttribute(attributeName);
				if ((attr1 == null && attr2 != null) || (attr1 != null && attr2 == null) || !attr1.equals(attr2)) {
					return false;
				}
			}
			// check edges
			for (String edgeName: newSubNode.getEdges().keySet()) {
				for (Node newTargetSubNode: newSubNode.getEdges(edgeName)) {
					if (!reverseMapping.get(newSubNode).getEdges(edgeName).contains(reverseMapping.get(newTargetSubNode))) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
}
