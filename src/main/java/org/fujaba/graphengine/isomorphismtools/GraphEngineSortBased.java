package org.fujaba.graphengine.isomorphismtools;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;

public class GraphEngineSortBased extends GraphEngine {

	public static boolean isIsomorphTo(Graph one, Graph other) {
		Graph first = normalized(one);
		Graph second = normalized(other);
		return getGson().toJson(first).equals(getGson().toJson(second));
	}
	
	private static Graph normalized(Graph graph) {
		Graph nf = graph.clone();
		// TODO: sort graph element to obtain its 'normal form'
		return nf;
	}
	
}
