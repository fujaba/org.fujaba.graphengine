package org.fujaba.graphengine.isomorphismtools.sort;

import java.util.ArrayList;
import java.util.Collections;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

public class GraphEngineSortBased extends GraphEngine {

	public static boolean isIsomorphTo(Graph one, Graph other) {
		if (one.getNodes().size() != other.getNodes().size()) {
			return false;
		}
		Graph first = normalized(one);
		Graph second = normalized(other);
		return getGson().toJson(first).equals(getGson().toJson(second));
	}
	
	@SuppressWarnings("unchecked")
	public static Graph normalized(Graph graph) {
		Graph nf = graph.clone();
		// obtain a list of memory-intensive nodeSortTrees to sort all data of the graph:
		ArrayList<NodeSortTree> nodeSortTrees = new ArrayList<NodeSortTree>();
		for (Node node: graph.getNodes()) {
			nodeSortTrees.add(new NodeSortTree(graph, node));
		}
		ArrayList<NodeSortTree> nodeSortTreesCopy = null;
		// sort the list of nodeSortTrees:
		while (!nodeSortTrees.equals(nodeSortTreesCopy)) {
			nodeSortTreesCopy = (ArrayList<NodeSortTree>)nodeSortTrees.clone();
			Collections.sort(nodeSortTrees);
			for (NodeSortTree nodeSortTree: nodeSortTrees) {
				nodeSortTree.doInnerSort(nodeSortTrees);
			}
		}
		nf.getNodes().clear();
		for (int i = 0; i < nodeSortTrees.size(); ++i) {
			nf.getNodes().add(nodeSortTrees.get(i).getRootNode());
		}
		return nf;
	}
	
}
