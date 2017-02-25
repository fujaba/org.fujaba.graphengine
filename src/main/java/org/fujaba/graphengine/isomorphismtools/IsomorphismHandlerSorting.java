package org.fujaba.graphengine.isomorphismtools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.isomorphismtools.sort.NodeSortTree;

public class IsomorphismHandlerSorting extends IsomorphismHandler {

	@Override
	public boolean isIsomorphTo(Graph one, Graph other) {
		if (one.getNodes().size() != other.getNodes().size()) {
			return false;
		}
		Graph first = normalized(one);
		Graph second = normalized(other);
		return GraphEngine.getGson().toJson(first).equals(GraphEngine.getGson().toJson(second));
	}
	
	@Override
	public HashMap<Node, Node> mappingFrom(Graph subGraph, Graph baseGraph) {
		return GraphEngine.getMappingFallback().mappingFrom(subGraph, baseGraph);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Graph normalized(Graph graph) {
		Graph nf = graph.clone();
		// obtain a list of memory-intensive nodeSortTrees to sort all data of the graph:
		ArrayList<NodeSortTree> nodeSortTrees = new ArrayList<NodeSortTree>();
		for (Node node: graph.getNodes()) {
			nodeSortTrees.add(new NodeSortTree(graph, node));
		}
		ArrayList<NodeSortTree> nodeSortTreesCopy = null;
//		// sort the list of nodeSortTrees:
//		while (!nodeSortTrees.equals(nodeSortTreesCopy)) {
//			nodeSortTreesCopy = (ArrayList<NodeSortTree>)nodeSortTrees.clone();
//			Collections.sort(nodeSortTrees);
//			for (NodeSortTree nodeSortTree: nodeSortTrees) {
//				nodeSortTree.doInnerSort(nodeSortTrees);
//			}
//		}
		// sort the list of nodeSortTrees:
		ArrayList<String> nodeSortTreesStrings = new ArrayList<String>();
		for (NodeSortTree nst: nodeSortTrees) {
			nodeSortTreesStrings.add(GraphEngine.getGson().toJson(nst));
		}
		ArrayList<String> nodeSortTreesCopyStrings = null;
		while (!nodeSortTreesStrings.equals(nodeSortTreesCopyStrings)) {
			nodeSortTreesCopyStrings = (ArrayList<String>)nodeSortTreesStrings.clone();
			HashMap<String, NodeSortTree> mapping = new HashMap<String, NodeSortTree>();
			for (NodeSortTree nst: nodeSortTrees) {
				mapping.put(GraphEngine.getGson().toJson(nst), nst);
			}
			Collections.sort(nodeSortTreesStrings);
			nodeSortTreesCopy = new ArrayList<NodeSortTree>();
			for (String s: nodeSortTreesStrings) {
				nodeSortTreesCopy.add(mapping.get(s));
			}
			nodeSortTrees = nodeSortTreesCopy;
			for (NodeSortTree nodeSortTree: nodeSortTrees) {
				nodeSortTree.doInnerSort(nodeSortTrees);
			}
			nodeSortTreesStrings = new ArrayList<String>();
			for (NodeSortTree nst: nodeSortTrees) {
				nodeSortTreesStrings.add(GraphEngine.getGson().toJson(nst));
			}
		}
//
//
//
		nf.getNodes().clear();
		for (int i = 0; i < nodeSortTrees.size(); ++i) {
			Node node = nodeSortTrees.get(i).getRootNode();
			for (String key: node.getEdges().keySet()) {
				ArrayList<Node> sortedTargets = new ArrayList<Node>();
				for (int j = 0; j < nodeSortTrees.size(); ++j) {
					for (int k = 0; k < node.getEdges(key).size(); ++k) {
						Node target = node.getEdges(key).get(k);
						if (target == nodeSortTrees.get(j).getRootNode()) {
							sortedTargets.add(target);
						}
					}
				}
				node.getEdges(key).clear();
				node.getEdges(key).addAll(sortedTargets);
			}
			nf.getNodes().add(node);
		}
		return nf;
	}

	@Override
	public String toString() {
		return "'normalization/sorting'-based isomorphism handler";
	}

}
