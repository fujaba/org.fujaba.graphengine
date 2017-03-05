package org.fujaba.graphengine.isomorphismtools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.isomorphismtools.sort.NodeSortTree;
import org.fujaba.graphengine.isomorphismtools.sort.NodeSortTreeNode;

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
	
	private void sortTargets(Graph g) {
		for (Node node: g.getNodes()) {
			for (String key: node.getEdges().keySet()) {
				ArrayList<Node> targets = node.getEdges(key);
				ArrayList<Node> sortedTargetNodes = new ArrayList<Node>();
targetMatch:	for (Node nSorted: g.getNodes()) {
					for (Node nTarget: targets) {
						if (nSorted == nTarget) {
							sortedTargetNodes.add(nSorted);
							continue targetMatch;
						}
					}
				}
				targets.clear();
				targets.addAll(sortedTargetNodes);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Graph normalized(Graph graph) {
		if (graph.getNodes().isEmpty()) {
			return graph.clone();
		}
		ArrayList<Graph> parts = new ArrayList<Graph>();
		// ##### 1.: split #####
		for (Graph subGraph: GraphEngine.split(graph)) {
			// ##### 2.: do the node-sort-tree sort: #####
			ArrayList<NodeSortTree> nodeSortTrees = new ArrayList<NodeSortTree>();
			for (Node node: subGraph.getNodes()) {
				nodeSortTrees.add(new NodeSortTree(subGraph, node));
			}
			ArrayList<NodeSortTree> nodeSortTreesCopy = null;
			ArrayList<String> nodeSortTreesStrings = new ArrayList<String>();
			for (NodeSortTree nst: nodeSortTrees) {
				nodeSortTreesStrings.add(GraphEngine.getGson().toJson(nst));
			}
			ArrayList<String> nodeSortTreesCopyStrings = null;
			while (!nodeSortTreesStrings.equals(nodeSortTreesCopyStrings)) {
				nodeSortTreesCopyStrings = (ArrayList<String>)nodeSortTreesStrings.clone();
				HashMap<String, NodeSortTree> mapping = new HashMap<String, NodeSortTree>();
				for (NodeSortTree nst: nodeSortTrees) { // TODO: shouldn't this be done just once initially???
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
			// ##### 3.: sort sub-graph by layout of the 'first' node-sort-tree: #####
			NodeSortTree baseNST = nodeSortTrees.get(0);
			ArrayList<Node> sortedNodes = getSortedNodes(baseNST, subGraph.getNodes());
			subGraph.getNodes().clear();
			subGraph.getNodes().addAll(sortedNodes);
			sortTargets(subGraph);
			parts.add(subGraph);
		}
		// ##### 4.: merge sub-graphs in order based on their (single) serializations: #####
		ArrayList<String> partsSerialized = new ArrayList<String>();
		HashMap<String, ArrayList<Graph>> graphMap = new HashMap<String, ArrayList<Graph>>();
		for (Graph g: parts) {
			String serialization = GraphEngine.getGson().toJson(g);
			if (graphMap.get(serialization) == null) {
				graphMap.put(serialization, new ArrayList<Graph>());
			}
			graphMap.get(serialization).add(g);
			partsSerialized.add(serialization);
		}
		Collections.sort(partsSerialized);
		Graph nf = new Graph();
		ArrayList<String> used = new ArrayList<String>();
		for (String key: partsSerialized) {
			if (used.contains(key)) {
				continue;
			}
			used.add(key);
			for (Graph g: graphMap.get(key)) {
				nf.getNodes().addAll(g.getNodes());
			}
		}
		return nf;
	}

	private ArrayList<Node> getSortedNodes(NodeSortTree baseNST, ArrayList<Node> nodes) {
		ArrayList<Node> result = new ArrayList<Node>();
		ArrayList<NodeSortTreeNode> open = new ArrayList<NodeSortTreeNode>();
		ArrayList<NodeSortTreeNode> closed = new ArrayList<NodeSortTreeNode>();
		open.add(baseNST.getRootNodeSortTreeNode());
		while (!open.isEmpty()) {
			NodeSortTreeNode current = open.remove(0);
			closed.add(current);
			for (NodeSortTreeNode nst: current.getChildrenNodeSortTreeNodes()) {
				if (!open.contains(nst) && !closed.contains(nst)) {
					open.add(nst);
//					open.add(0, nst);
				}
			}
			Node node = current.getNode();
			if (!result.contains(node)) {
				result.add(node);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "'normalization/sorting'-based isomorphism handler";
	}

}
