package org.fujaba.graphengine.isomorphismtools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

public class IsomorphismHandlerParallel extends IsomorphismHandler {

	private HashMap<Node, Node> mapping = null;
	private Semaphore mappingSem = new Semaphore(1);
	private ArrayList<ArrayList<Integer>> assignments = null;
	private Semaphore assignmentsSem = new Semaphore(1);
	
	private ArrayList<Integer> getAssignment() {
		try {
			assignmentsSem.acquire();
			ArrayList<Integer> assignment = null;
			if (assignments != null && assignments.size() > 0) {
				assignment = assignments.remove(0);
			}
			assignmentsSem.release();
//			System.out.println(assignments.size() + " assignments left"); // TODO: remove debug
			return assignment;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void setCorrectMapping(HashMap<Node, Node> mapping) {
		try {
			assignmentsSem.acquire();
			mappingSem.acquire();
			if (this.mapping == null && mapping != null) {
				this.mapping = mapping;
				this.assignments.clear();
			}
			mappingSem.release();
			assignmentsSem.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
	public HashMap<Node, Node> mappingFrom(Graph subGraph, Graph baseGraph) {
		if (subGraph.getNodes().size() > baseGraph.getNodes().size()) {
			return null;
		}
		ArrayList<ArrayList<Node>> couldMatch = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < subGraph.getNodes().size(); ++i) {
			Node subNode = subGraph.getNodes().get(i);
			couldMatch.add(new ArrayList<Node>());
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
				couldMatch.get(couldMatch.size() - 1).add(node);
			}
			if (couldMatch.get(couldMatch.size() - 1).size() == 0) {
				return null; // no mapping for this node => fail
			}
		}
		final ArrayList<ArrayList<Node>> reducedMatches = GraphEngine.removeImpossibleCandidates(couldMatch);
		if (reducedMatches == null) {
			return null;
		}
		
		/**
		 * init:
		 */
		// set number of threads:
		int NTHREADS = 4; 
		// set number of checks per assignment:
		int checksPerAssignment = 10;
		
		// calculate number of checks;
		int totalCheckCount = 1;
		for (int i = 0; i < reducedMatches.size(); ++i) {
			totalCheckCount *= reducedMatches.get(i).size();
		}
		// reset resulting mapping:
		this.mapping = null;
		
		// build assignments:
		this.assignments = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i * checksPerAssignment < totalCheckCount; ++i) {
			ArrayList<Integer> assignment = new ArrayList<Integer>();
			assignment.add(i * checksPerAssignment);
			if ((i + 1) * checksPerAssignment >= totalCheckCount) {
				assignment.add(totalCheckCount);
			} else {
				assignment.add((i + 1) * checksPerAssignment);
			}
			this.assignments.add(assignment);
		}
		
		/**
		 * setup workers:
		 */
		ArrayList<Thread> workers = new ArrayList<Thread>();
		for (int i = 0; i < NTHREADS; ++i) {
			workers.add(new Thread() {
				public void run() {
					ArrayList<Integer> assignment = getAssignment();
					while (assignment != null) {
						if (assignment.size() != 2) {
							continue;
						}
						int checkFrom = assignment.get(0);
						int checkUntil = assignment.get(1);
//						System.out.println("check from " + checkFrom + " until " + checkUntil); // TODO: remove debug
						// do the assigned checks:
						ArrayList<Integer> currentTry = new ArrayList<Integer>();
						for (int i = checkFrom; i < checkUntil; ++i) {
							int index = i;
							if (i == checkFrom) {
//							currentTry = new ArrayList<Integer>();
								for (int j = 0; j < reducedMatches.size(); ++j) {
									int nextIndex = index % reducedMatches.get(j).size();
									index /= reducedMatches.get(j).size();
									currentTry.add(nextIndex);
								}
							} else {
								index = 0;
								for (int j = reducedMatches.size() - 1; j >= 0 ; --j) {
									index *= reducedMatches.get(j).size();
									index += currentTry.get(j);
								}
								if (index >= checkUntil) {
									break;
								}
							}
//							System.out.println(currentTry); // TODO: remove debug
							
							
							HashMap<Node, Node> mapping = new HashMap<Node, Node>(); 
							HashSet<Node> usedNodes = new HashSet<Node>();
							boolean duplicateChoice = false;
							for (int j = 0; j < reducedMatches.size(); ++j) {
								Node subNode = subGraph.getNodes().get(j);
								Node node = reducedMatches.get(j).get(currentTry.get(j));
								mapping.put(subNode, node);
								if (usedNodes.contains(node)) {
									duplicateChoice = true;
									break;
								}
								usedNodes.add(node);
							}
							if (!duplicateChoice) {
								// check targets of outgoing edges:
								boolean mismatch = false; 
				edgesMatch:		for (int j = 0; j < subGraph.getNodes().size(); ++j) {
									Node sourceSubNode = subGraph.getNodes().get(j);
									for (String edgeName: sourceSubNode.getEdges().keySet()) {
										for (Node targetSubNode: sourceSubNode.getEdges(edgeName)) {
											Node sourceCheckNode = mapping.get(sourceSubNode);
											Node targetCheckNode = mapping.get(targetSubNode);
											if (!sourceCheckNode.getEdges(edgeName).contains(targetCheckNode)) {
												mismatch = true;
												break edgesMatch;
											}
										}
									}
								}
								if (!mismatch) {
									setCorrectMapping(mapping); // the mapping was found => success
								}
							}
							for (int j = 0; j < currentTry.size(); ++j) {
								int currentIndex = currentTry.get(j);
								int maxIndex = reducedMatches.get(j).size() - 1;
								if (currentIndex < maxIndex) {
									currentTry.set(j, currentIndex + 1);
									for (int k = 0; k < j; ++k) {
										currentTry.set(k, 0);
									}
									usedNodes = new HashSet<Node>(); // use next duplicate-free configuration (begin)
				fix:				for (int l = currentTry.size() - 1; l >= 0; --l) {
										while (usedNodes.contains(reducedMatches.get(l).get(currentTry.get(l)))) {
											if (currentTry.get(l) >= reducedMatches.get(l).size() - 1) {
												break fix;
											}
											currentTry.set(l, currentTry.get(l) + 1);
										}
										usedNodes.add(reducedMatches.get(l).get(currentTry.get(l)));
									} // use next duplicate-free configuration (end)
									break;
								}
							}
						}
						assignment = getAssignment();
					}
				}
			});
		}
		/**
		 * start workers (first thread is handled by this thread)
		 */
		boolean first = true;
		for (Thread thread: workers) {
			if (first) {
				first = false;
				continue;
			}
			thread.start();
		}
		workers.get(0).run();
		for (Thread thread: workers) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		/**
		 * ok done
		 */
		return this.mapping;
	}

	@Override
	public Graph normalized(Graph graph) {
		return GraphEngine.getNormalizationFallback().normalized(graph);
	}

	@Override
	public String toString() {
		return "'parallel combinatorics'-based isomorphism handler";
	}

}
