package org.fujaba.graphengine.isomorphismtools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.isomorphismtools.heuristics.NodeWithConflict;

public class IsomorphismHandlerCSPWithMoreHeuristics extends IsomorphismHandler {
	
	private static ArrayList<Node> getDepthFirstSortedNodeList(Graph graph) {
		// obtain all parts of the graph - where each part's nodes are connected with each other:
		ArrayList<Graph> splitted = GraphEngine.split(graph, true);
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Graph connectedGraph: splitted) {
			// add the nodes to the node-list (note: each part's nodes are already sorted in a depth-first explore's order):
			nodes.addAll(connectedGraph.getNodes());
		}
		return nodes;
	}

	@Override
	/**
	 * This function checks for this graph and a given sub-graph,
	 * if the sub-graph is isomorph to a sub-graph of this graph and returns the mapping.
	 * 
	 * @param graph the given base-graph
	 * @param subGraph the given sub-graph
	 * @return a mapping from the given sub-graph to nodes of this graph if possible, or null
	 */
	public HashMap<Node, Node> mappingFrom(Graph subGraphInitial, Graph baseGraph) {
		if (subGraphInitial.getNodes().size() == 0) {
			// no nodes in sub-graph => empty mapping is the match (success)
			return new HashMap<Node, Node>();
		}
		if (subGraphInitial.getNodes().size() > baseGraph.getNodes().size()) {
			// too many sub-graph nodes => fail
			return null;
		}
		// now I'm trying to find 'loosely matched candidates':
		Graph subGraph = subGraphInitial.clone();
		ArrayList<ArrayList<Node>> couldMatch2 = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < subGraph.getNodes().size(); ++i) {
			Node subNode = subGraph.getNodes().get(i);
			couldMatch2.add(new ArrayList<Node>());
nodeMatch:	for (int j = 0; j < baseGraph.getNodes().size(); ++j) {
				Node node = baseGraph.getNodes().get(j);
				// check existence of outgoing edges and their count:
				for (String key: subNode.getEdges().keySet()) {
					int currentSubNodeEdgeCount = subNode.getEdges(key).size();
					int currentNodeEdgeCount = (node.getEdges(key) == null ? 0 : node.getEdges(key).size());
					if (currentNodeEdgeCount < currentSubNodeEdgeCount) {
						continue nodeMatch;
					}
				}
				// check attributes:
				for (String key: subNode.getAttributes().keySet()) {
					if (!subNode.getAttribute(key).equals(node.getAttribute(key))) {
						continue nodeMatch;
					}
				}
				couldMatch2.get(couldMatch2.size() - 1).add(node);
			}
			if (couldMatch2.get(couldMatch2.size() - 1).size() == 0) {
				return null; // no mapping for this node => fail
			}
		}
		couldMatch2 = removeImpossibleCandidates(couldMatch2);
		if (couldMatch2 == null) {
			// after removing 'impossible' candidates, there's no match anymore => fail
			return null;
		}
		if (subGraph.getNodes().size() == 1) {
			// a single node with a candidate is a match => success
			HashMap<Node, Node> singleNodeMapping = new HashMap<Node, Node>();
			singleNodeMapping.put(subGraph.getNodes().get(0), couldMatch2.get(0).get(0));
			return singleNodeMapping;
		}
		/*
		 * here I'm starting the application of the heuristics of the maximum restricted variable (H1) and the minimum node order (H2):
		 */
		// first save the old order of the matches:
		HashMap<Node, Integer> oldIndices = new HashMap<Node, Integer>();
		for (int i = 0; i < subGraph.getNodes().size(); ++i) {
			oldIndices.put(subGraph.getNodes().get(i), i);
		}
		// now check for the maximum restricted variables (H1):
		ArrayList<Integer> minimumIndices = new ArrayList<Integer>();
		int minimumValue = Integer.MAX_VALUE;
		for (int i = 0; i < subGraph.getNodes().size(); ++i) { // minimum candidates
			if (couldMatch2.get(i).size() <= minimumValue) {
				if (couldMatch2.get(i).size() < minimumValue) {
					minimumIndices = new ArrayList<Integer>();
					minimumValue = couldMatch2.get(i).size();
				}
				minimumIndices.add(i);
			}
		}
		// now check within those for the minimum node order (H2):
		int indicesIndex = -1;
		minimumValue = Integer.MAX_VALUE;
		for (int i = 0; i < minimumIndices.size(); ++i) { // minimum node order (outgoing)
			int outgoingCount = 0;
			Node currentNode = subGraph.getNodes().get(minimumIndices.get(i));
			for (String key: currentNode.getEdges().keySet()) {
				outgoingCount += currentNode.getEdges(key).size();
			}
			if (outgoingCount < minimumValue) {
				minimumValue = outgoingCount;
				indicesIndex = i;
			}
		}
		// here we have the 'best' node to start with:
		Node heuristicallySelectedFirstNode = subGraph.getNodes().get(minimumIndices.get(indicesIndex));
		subGraph.getNodes().remove(heuristicallySelectedFirstNode); // remove from old position
		subGraph.getNodes().add(0, heuristicallySelectedFirstNode); // put in front
		// now order the nodes in a depth-first fashion, with the heuristically selected first node as 'root':
		ArrayList<Node> sortedNodes = getDepthFirstSortedNodeList(subGraph);
		// restore the matches to the new order:
		ArrayList<ArrayList<Node>> couldMatch = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < sortedNodes.size(); ++i) {
			couldMatch.add(couldMatch2.get(oldIndices.get(sortedNodes.get(i))));
		}
		// and build candidates into objects, that contain an additional conflict-value and are sortable
		ArrayList<ArrayList<NodeWithConflict>> couldMatchWithConflict = new ArrayList<ArrayList<NodeWithConflict>>();
		for (int i = 0; i < couldMatch.size(); ++i) {
			ArrayList<Node> nodesToAdd = couldMatch.get(i);
			couldMatchWithConflict.add(new ArrayList<NodeWithConflict>());
			for (int j = 0; j < nodesToAdd.size(); ++j) {
				couldMatchWithConflict.get(i).add(new NodeWithConflict(nodesToAdd.get(j)));
			}
		}
		HashMap<Node, Node> mapping = new HashMap<Node, Node>();
		// now going through all valid combinations (that make sense) of those loosely fitted candidates to find a match:
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		for (int i = 0; i < couldMatchWithConflict.size(); ++i) {
			currentTry.add(0);
			mapping.put(sortedNodes.get(i), couldMatchWithConflict.get(i).get(0).getNode());
		}
		/*
		 * only check this index against previous ones,
		 * if ok, increment and check only that one, and so on
		 */
		int checkIndex = 1;
		int lastIndex = checkIndex - 1;
loop:	while (checkIndex != -1) {
			if (checkIndex > lastIndex) {
				
				
				int startThisHeuristicsAtIndex = checkIndex;
				
				
				
				
				
				
	
				
				
				/*
				 * here I'm starting the application of the heuristics of the maximum restricted variable (H1) and the minimum node order (H2):
				 */
				// first save the old order of the matches:
				oldIndices = new HashMap<Node, Integer>();
				for (int i = 0; i < sortedNodes.size(); ++i) {
					oldIndices.put(sortedNodes.get(i), i);
				}
				// now check for the maximum restricted variables (H1):
				minimumIndices = new ArrayList<Integer>();
				minimumValue = Integer.MAX_VALUE;
				for (int i = startThisHeuristicsAtIndex; i < sortedNodes.size(); ++i) { // minimum candidates
					if (couldMatchWithConflict.get(i).size() <= minimumValue) {
						if (couldMatchWithConflict.get(i).size() < minimumValue) {
							minimumIndices = new ArrayList<Integer>();
							minimumValue = couldMatchWithConflict.get(i).size();
						}
						minimumIndices.add(i);
					}
				}
				// now check within those for the minimum node order (H2):
				indicesIndex = -1;
				minimumValue = Integer.MAX_VALUE;
				for (int i = 0; i < minimumIndices.size(); ++i) { // minimum node order (outgoing)
					int outgoingCount = 0;
					Node currentNode = sortedNodes.get(minimumIndices.get(i));
					for (String key: currentNode.getEdges().keySet()) {
						outgoingCount += currentNode.getEdges(key).size();
					}
					if (outgoingCount < minimumValue) {
						minimumValue = outgoingCount;
						indicesIndex = i;
					}
				}
				// here we have the 'best' node to start with:
				heuristicallySelectedFirstNode = subGraph.getNodes().get(minimumIndices.get(indicesIndex));
//				sortedNodes.remove(heuristicallySelectedFirstNode); // remove from old position
//				sortedNodes.add(startThisHeuristicsAtIndex, heuristicallySelectedFirstNode); // put in front
				// restore the matches to the new order:
				ArrayList<ArrayList<NodeWithConflict>> couldMatchWithConflict2 = new ArrayList<ArrayList<NodeWithConflict>>();
				for (int i = 0; i < sortedNodes.size(); ++i) {
					couldMatchWithConflict2.add(couldMatchWithConflict.get(oldIndices.get(sortedNodes.get(i))));
				}
				couldMatchWithConflict.clear();
				couldMatchWithConflict.addAll(couldMatchWithConflict2);
				

				
				
				
				
				
				
				
				
				
				
				
//				/*
//				 * we now use the heuristics of minimal conflict (H3) - regarding the initial situation,
//				 * to sort the 'couldMatch'-lists by that measure.
//				 */
//				ArrayList<NodeWithConflict> nodesWithConflictToCheck = couldMatchWithConflict.get(startThisHeuristicsAtIndex);
//				for (int index = 0; index < nodesWithConflictToCheck.size(); ++index) {
//					NodeWithConflict nodeWithConflictToCheckMapped = nodesWithConflictToCheck.get(index);
//					Node nodeToCheckMapped = nodeWithConflictToCheckMapped.getNode();
//					// check available outgoing candidates for conflict:
//					for (int k = startThisHeuristicsAtIndex + 1; k < sortedNodes.size(); ++k) {
//						for (String key: sortedNodes.get(startThisHeuristicsAtIndex).getEdges().keySet()) {
//							for (Node target: sortedNodes.get(startThisHeuristicsAtIndex).getEdges(key)) {
//								if (target == sortedNodes.get(k)) {
//									ArrayList<NodeWithConflict> mappingsToTarget = couldMatchWithConflict.get(sortedNodes.indexOf(sortedNodes.get(startThisHeuristicsAtIndex)));
//									for (int j = 0; j < mappingsToTarget.size(); ++j) {
//										NodeWithConflict mappingToTarget = mappingsToTarget.get(j);
//										Node nodeMappedToTarget = mappingToTarget.getNode();
//										if (!nodeToCheckMapped.getEdges(key).contains(nodeMappedToTarget)) {
//											nodeWithConflictToCheckMapped.incrementConflict();
//										}
//									}
//								}
//							}
//						}
//					}
//					// check available incoming candidates for conflict:
//					for (int k = startThisHeuristicsAtIndex + 1; k < sortedNodes.size(); ++k) {
//						for (String key: sortedNodes.get(k).getEdges().keySet()) {
//							for (Node target: sortedNodes.get(k).getEdges(key)) {
//								if (target == sortedNodes.get(startThisHeuristicsAtIndex)) {
//									ArrayList<NodeWithConflict> mappingsToSource = couldMatchWithConflict.get(sortedNodes.indexOf(sortedNodes.get(k)));
//									for (int j = 0; j < mappingsToSource.size(); ++j) {
//										NodeWithConflict mappingToSource = mappingsToSource.get(j);
//										Node nodeMappedToSource = mappingToSource.getNode();
//										if (!nodeMappedToSource.getEdges(key).contains(nodeToCheckMapped)) {
//											nodeWithConflictToCheckMapped.incrementConflict();
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//				Collections.sort(nodesWithConflictToCheck);
				// ok, done the candidates are sorted according to minimum conflict heuristics (H3)	
			}
			for (int i = checkIndex; i < sortedNodes.size(); ++i) {
				/*
				 * check sortedNodes.get(i) only against all previous nodes,
				 * if it is duplicate, or any edge (outgoing or incoming) is missing.
				 * if it fails: count this nodes candidate up (++currentTry.get(i)) if possible,
				 * if it can't be counted up, go one level back (i-1) and try increment there and so on.
				 * if nothing can't be counted up, return null (or set checkIndex to -1 and break);
				 * after incrementing a candidate, reset all currentTry-elements after it to 0,
				 * and set the checkIndex to the index of the increment currentTry-element, finally break
				 */
				Node currentSubNode = sortedNodes.get(i);
				boolean fail = false;
match:			for (int j = 0; j < i; ++j) {
					Node otherSubNode = sortedNodes.get(j);
					if (mapping.get(currentSubNode) == mapping.get(otherSubNode)) {
						fail = true; // found duplicate!
						break match;
					}
					for (String key: currentSubNode.getEdges().keySet()) {
						if (currentSubNode.getEdges(key).contains(otherSubNode)) {
							if (!mapping.get(currentSubNode).getEdges(key).contains(mapping.get(otherSubNode))) {
								fail = true; // missing outgoing edge
								break match;
							}
						}
					}
					for (String key: otherSubNode.getEdges().keySet()) {
						if (otherSubNode.getEdges(key).contains(currentSubNode)) {
							if (!mapping.get(otherSubNode).getEdges(key).contains(mapping.get(currentSubNode))) {
								fail = true; // missing incoming edge
								break match;
							}
						}
					}
				}
				if (fail) {
					// found an error with the 'new' candidate at index i
					/*
					 * change candidate of node[i] or if not possible, the next possible earlier one,
					 * reset the ones after it (also update the mapping)
					 * and set checkIndex to the new index to check (the one that got incremented)
					 */
					lastIndex = checkIndex;
					checkIndex = i;
					while (checkIndex >= 0 && currentTry.get(checkIndex) == couldMatchWithConflict.get(checkIndex).size() - 1) {
						--checkIndex;
					}
					if (checkIndex >= 0) {
						currentTry.set(checkIndex, currentTry.get(checkIndex) + 1);
						mapping.put(sortedNodes.get(checkIndex), couldMatchWithConflict.get(checkIndex).get(currentTry.get(checkIndex)).getNode());
						for (int j = checkIndex + 1; j < sortedNodes.size(); ++j) {
							currentTry.set(j, 0);
							mapping.put(sortedNodes.get(j), couldMatchWithConflict.get(j).get(0).getNode());
						}
					}
					continue loop;
				}
			}
			return mapping; // it ran through with no errors => success
		}
		return null; // nothing left to check => fail
	}
	
	private ArrayList<ArrayList<Node>> removeImpossibleCandidates(ArrayList<ArrayList<Node>> couldMatch) {
		ArrayList<ArrayList<Node>> cantMatch = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < couldMatch.size(); ++i) {
			cantMatch.add(new ArrayList<Node>());
		}
		// go through all candidates:
		for (int i = 0; i < couldMatch.size(); ++i) {
			if (couldMatch.get(i).size() == 1) {
				// one node has only one candidate, all other nodes can't have this candidate
				for (int j = 0; j < couldMatch.size(); ++j) {
					if (i != j) {
						// 'tell' all other nodes, they can't have this candidate:
						cantMatch.get(j).add(couldMatch.get(i).get(0));
					}
				}
			}
		}
		// go through all candidatzes again:
		for (int i = 0; i < couldMatch.size(); ++i) {
			// remove all impossible candidates:
			for (int j = 0; j < cantMatch.get(i).size(); ++j) {
				couldMatch.get(i).remove(cantMatch.get(i).get(j));
			}
			// if one node has no candidates anymore, return null
			if (couldMatch.get(i).size() < 1) {
				return null;
			}
		}
		return couldMatch;
	}

	@Override
	public Graph normalized(Graph graph) {
		return GraphEngine.getNormalizationFallback().normalized(graph);
	}

}
