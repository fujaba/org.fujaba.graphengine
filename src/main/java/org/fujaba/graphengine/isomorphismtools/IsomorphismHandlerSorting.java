package org.fujaba.graphengine.isomorphismtools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
//			System.out.println("subGraph:" + subGraph); // TODO: remove debug
//			System.out.println("nodes:"); // TODO: remove debug
			for (Node node: subGraph.getNodes()) {
//				System.out.println("#####" + node); // TODO: remove debug
				nodeSortTrees.add(new NodeSortTree(subGraph, node));
			}
			ArrayList<NodeSortTree> nodeSortTreesCopy = null;
			ArrayList<String> nodeSortTreesStrings = new ArrayList<String>();
			for (NodeSortTree nst: nodeSortTrees) {
				nodeSortTreesStrings.add(GraphEngine.getGson().toJson(nst));
			}
			ArrayList<String> nodeSortTreesCopyStrings = null;
			ArrayList<ArrayList<NodeSortTree>> history = new ArrayList<ArrayList<NodeSortTree>>();
			while (!nodeSortTreesStrings.equals(nodeSortTreesCopyStrings)) {
				int loopCycleIndex = history.indexOf(nodeSortTrees);
				if (loopCycleIndex == -1) {
					history.add(nodeSortTrees);
				} else {
					// loop detected
					// TODO: GET A SPECIFIC ONE!!!!
					ArrayList<NodeSortTree> minList = null;
					String minString = "";
					for (ArrayList<NodeSortTree> currentList: history) {
//						System.out.println(currentList.toString());
						if (minList == null || currentList.toString().compareTo(minString) < 0) {
							minList = currentList;
							minString = currentList.toString();
						}
					}
					nodeSortTrees = minList;
					break;
				}
				nodeSortTreesCopyStrings = (ArrayList<String>)nodeSortTreesStrings.clone();
				HashMap<String, ArrayList<NodeSortTree>> mapping = new HashMap<String, ArrayList<NodeSortTree>>();
				for (NodeSortTree nst: nodeSortTrees) { // TODO: shouldn't this be done just once initially???
					String serialization = GraphEngine.getGson().toJson(nst);
					if (mapping.get(serialization) == null) {
						mapping.put(serialization, new ArrayList<NodeSortTree>());
					}
					mapping.get(serialization).add(nst);
				}
				Collections.sort(nodeSortTreesStrings);
				nodeSortTreesCopy = new ArrayList<NodeSortTree>();
				ArrayList<String> used = new ArrayList<String>();
				for (String s: nodeSortTreesStrings) {
					if (!used.contains(s)) {
						used.add(s);
						for (NodeSortTree nst: mapping.get(s)) {
							nodeSortTreesCopy.add(nst);
						}
					}
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
//		System.out.println(); // TODO: remove debug
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
//				System.out.println("node: " + node + "; edges: " + node.getEdges()); // TODO: remove debug
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "'normalization/sorting'-based isomorphism handler";
	}

}
