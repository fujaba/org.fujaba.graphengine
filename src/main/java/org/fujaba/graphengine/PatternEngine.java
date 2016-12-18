package org.fujaba.graphengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.naming.spi.DirStateFactory.Result;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;

import net.sourceforge.jeval.Evaluator;

/**
 * The PatternEngine contains all logic concerning PatternGraphs.
 * 
 * @author Philipp Kolodziej
 */
public class PatternEngine {
	
	/**
	 * Calculates a reachability graph based on a graph of the initial situation
	 * and a prioritized list of patterns to match and apply on the graph and resulting graphs.
	 * @param graph the initial graph
	 * @param patterns a prioritized list of patterns. the first list is the highest priority-level and so on.
	 * @return returns the reachability graph, that was calculated
	 */
	public static Graph calculateReachabilityGraph(Graph graph, ArrayList<ArrayList<PatternGraph>> patterns) {
		/*
		 * ok right now i think im going for this priority concept, where i have an arraylist of 'priority-levels'
		 * inside each priority-level, there's a list of patterns to match.
		 * 
		 * the first level, where there's a match found inside is used -> children of that graph will be all graphs
		 * with the applied matches of that level and that level alone.
		 * 
		 * with this approach im kind of very flexible on whatever priority concept really will be needed!
		 */
		
		/*
		 * the graph itself is made up like this:
		 * 
		 * each node represents a graph and has an attribute 'graph' and as value the serialized graph.
		 * the edges represent the application of a certain pattern-graph, with its serialization as a node-name.
		 * 
		 */
		
		/*
		 * the algorithm for calculating the graph itself is like this:
		 * 
		 * first add the base-graph itself to the RG. and add it to a list with unprocessed nodes.
		 * from then on go though all unprocessed nodes and check for successors,
		 * add them according to the naming scheme to the RG and add em to the list of unprocessed nodes.
		 * and so on.
		 * 
		 * just take care when finding successors, to always check if they were already existing,
		 * if they did, make the edge go to the previously added nodes in the EG and DON'T add them to the list
		 * of unprocessed nodes. just as simple as that...
		 */
		
		// the first rg-node is the base-graph:
		Graph rg = new Graph().addNode(new Node().setAttribute("graph", graph.toString()));
		ArrayList<Graph> added = new ArrayList<Graph>(); // a list with graphs that were added
		ArrayList<Graph> unprocessed = new ArrayList<Graph>(); // a list with currently unprocessed graphs
		added.add(graph);
		unprocessed.add(graph);
		
		// as long as a single graph wasn't checked for successors, the search continues:
		while (unprocessed.size() > 0) {
			// looking for matches:
			ArrayList<Match> matches = calculateReachabilityNodeMatches(unprocessed.get(0), patterns);
			// look up the rg-node, that represents the unprocessed graph:
			Node source = findGraphInReachabilityGraph(rg, unprocessed.get(0));
			// now handle matches:
			for (Match match: matches) {
				// construct the graph, that's the result of this match:
				Graph successor = applyMatch(match);
				// check if the graph was previously added:
				int index = indexOf(added, successor);
				if (index != -1) {
					// yes, the graph already did exist => just build edge to an existing node
					Node target = findGraphInReachabilityGraph(rg, successor);
					source.addEdge(match.getPattern().toString(), target); // new edge
				} else {
//					System.out.println("reached new state with '" + match.getPattern().getName() + "'"); // TODO: remove debug
					// no, the graph didn't exist before => add a new node
					Node target = new Node().setAttribute("graph", successor.toString()); // new node
					rg.addNode(target);
					source.addEdge(match.getPattern().toString(), target); // edge to new node
					added.add(successor);
					unprocessed.add(successor);
				}
			}
			unprocessed.remove(0);
		}
		// done
		return rg;
	}
	
	/**
	 * Function to find a graph as a node inside of a reachability graph.
	 * @param rg the reachability graph
	 * @param node the graph that is to be found a node inside of the reachability graph
	 * @return the node representing the graph inside of the reachability graph if present, or else null
	 */
	public static Node findGraphInReachabilityGraph(Graph rg, Graph node) {
		String serialization = node.toString();
		for (Node found: rg.getNodes()) {
			if (serialization.equals(found.getAttribute("graph"))) {
				return found;
			}
		}
		return null;
	}
	
	/**
	 * Private helper function that calculates a list of matches for a graph based on a prioritized list of patterns.
	 * the matches that will be found, will all be from the highest priority-level that had a match.
	 * @param graph the graph to match patterns on
	 * @param patterns a prioritized list of patterns
	 * @return a list of matches from the highest priority-level that matched anything, or else an empty list.
	 */
	private static ArrayList<Match> calculateReachabilityNodeMatches(Graph graph, ArrayList<ArrayList<PatternGraph>> patterns) {
		ArrayList<Match> result = new ArrayList<Match>();
		for (int i = 0; i < patterns.size(); ++i) {
			ArrayList<PatternGraph> priorityLevel = patterns.get(i);
			for (int j = 0; j < priorityLevel.size(); ++j) {
				PatternGraph pattern = priorityLevel.get(j);
				result.addAll(matchPattern(graph, pattern, false));
			}
			if (result.size() > 0) {
				return result;
			}
		}
		return result;
	}
	
	/**
	 * matches and directly applies a pattern as often as possible,
	 * without revisiting graphs, that were already used (preventing endless cycles).
	 * (but it won't apply all possible matches, so not every reachable graph will be visited)
	 * 
	 * @param graph the graph to apply the pattern on
	 * @param patterns the patterns to apply
	 * @return the resulting graph
	 */
	
	
	private static int indexOf(ArrayList<Graph> graphs, Graph graph) {
		for (int i = 0; i < graphs.size(); ++i) {
			Graph g = graphs.get(i);
			if (GraphEngine.isIsomorphTo(g, graph)) {
				return i;
			}
		}
		return -1;
	}
	
	private static ArrayList<ArrayList<PatternNode>> doDepthFirstExplore(ArrayList<PatternNode> patternNodes) {
		ArrayList<ArrayList<PatternNode>> result = new ArrayList<ArrayList<PatternNode>>();
		ArrayList<PatternNode> unprocessed = new ArrayList<PatternNode>(patternNodes);
		while (unprocessed.size() > 0) {
			PatternNode start = unprocessed.remove(0);
			ArrayList<PatternNode> open = new ArrayList<PatternNode>();
			ArrayList<PatternNode> closed = new ArrayList<PatternNode>();
			open.add(start);
			while (open.size() > 0) {
				PatternNode current = open.remove(0);
				closed.add(current);
				ArrayList<PatternNode> succ = new ArrayList<PatternNode>();
				for (PatternEdge patternEdge: current.getPatternEdges()) {
					if (!open.contains(patternEdge.getTarget())
							&& !closed.contains(patternEdge.getTarget())
							&& !succ.contains(patternEdge.getTarget())
							&& unprocessed.contains(patternEdge.getTarget())) {
						succ.add(patternEdge.getTarget());
					}
				}
				for (PatternNode ingoing: unprocessed) {
					for (PatternEdge patternEdge: ingoing.getPatternEdges()) {
						if (patternEdge.getTarget() == current
								&& !open.contains(ingoing)
								&& !closed.contains(ingoing)
								&& !succ.contains(ingoing)) {
							succ.add(ingoing);
							break;
						}
					}
				}
				open.addAll(succ);
			}
			unprocessed.removeAll(closed);
			result.add(closed);
		}
		return result;
	}
	
	private static ArrayList<ArrayList<PatternNode>> calculateNodeMatchingLists(PatternGraph pattern) {
		ArrayList<ArrayList<PatternNode>> result = new ArrayList<ArrayList<PatternNode>>();
		result.add(new ArrayList<PatternNode>()); // positive list
		
		// first check which nodes are positive and which nodes are negative (don't mind 'neutral' nodes):
		ArrayList<PatternNode> positiveNodes = new ArrayList<PatternNode>();
		ArrayList<PatternNode> negativeNodes = new ArrayList<PatternNode>();
		for (PatternNode node: pattern.getPatternNodes()) {
			switch (node.getAction()) {
			case "==":
			case "-":
				positiveNodes.add(node);
				break;
			case "+":
				break;
			case "!=":
				negativeNodes.add(node);
				break;
			default:
				try {
					throw new IOException("can't handle PatternNode action '" + node.getAction() + "'!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// now do continuous depth-first 'explore's until all positive nodes are in a 'clever' order for later checks:
		ArrayList<ArrayList<PatternNode>> positiveLists = doDepthFirstExplore(positiveNodes);
		for (ArrayList<PatternNode> listPart: positiveLists) {
			result.get(0).addAll(listPart);
		}
		
		// finally do continuous depth-first 'explore's for negative nodes, but this time yielding separate node lists:
		ArrayList<ArrayList<PatternNode>> negativeLists = doDepthFirstExplore(negativeNodes);
		result.addAll(negativeLists);
		
		return result;
	}
	
	private static ArrayList<ArrayList<ArrayList<Node>>> findPossibleMatchesForPositiveAndNegativeNodes(Graph graph, ArrayList<ArrayList<PatternNode>> nodeMatchLists) {
		// now check for 'loosely matched candidates' of nodes to match (level == 0: positive nodes, level > 0: negative node sets):
		ArrayList<ArrayList<ArrayList<Node>>> couldMatch = new ArrayList<ArrayList<ArrayList<Node>>>();
		for (int level = 0; level < nodeMatchLists.size(); ++level) {
			couldMatch.add(new ArrayList<ArrayList<Node>>());
			for (int i = 0; i < nodeMatchLists.get(level).size(); ++i) {
				PatternNode patternNode = nodeMatchLists.get(level).get(i);
				couldMatch.get(level).add(new ArrayList<Node>());
	nodeMatch:	for (int j = 0; j < graph.getNodes().size(); ++j) {
					Node node = graph.getNodes().get(j);
					// check node attribute expression:
					if (!PatternEngine.evaluate(node, patternNode.getAttributeMatchExpression())) {
						continue nodeMatch;
					}
					// check existence of outgoing edges:
					for (PatternEdge patternEdge: patternNode.getPatternEdges()) {
						if ("+".equals(patternEdge.getAction())) {
							continue;
						}
						// TODO verify: we shouldn't mind edges from positive to negative nodes here (yet):
						boolean dontMind = !"!=".equals(patternEdge.getSource().getAction()) && "!=".equals(patternEdge.getTarget().getAction());
						if (!dontMind) {
							boolean exists = !(node.getEdges(patternEdge.getName()) == null || node.getEdges(patternEdge.getName()).size() == 0);
							if (("!=".equals(patternEdge.getAction()) && exists) || !"!=".equals(patternEdge.getAction()) && !exists) {
								continue nodeMatch;
							}
						}
					}
					// check every attribute's expression:
					for (PatternAttribute patternAttribute: patternNode.getPatternAttributes()) {
						if ("+".equals(patternAttribute.getAction())) {
							continue;
						}
						boolean isSame = PatternEngine.evaluate(node, patternAttribute.getValue());
						if (("!=".equals(patternAttribute.getAction()) && isSame) || (!"!=".equals(patternAttribute.getAction()) && !isSame)) {
							continue nodeMatch;
						}
					}
					couldMatch.get(level).get(couldMatch.get(level).size() - 1).add(node);
				}
				if (level == 0 && couldMatch.get(level).get(couldMatch.get(level).size() - 1).size() == 0) {
					return null; // no mapping for this node => fail (only in level == 0)
				}
			}
		}
		return couldMatch;
	}
	
	public static boolean doesntMatchNegativeNodes(HashMap<PatternNode, Node> map, Graph graph, ArrayList<ArrayList<PatternNode>> nodeMatchLists, ArrayList<ArrayList<ArrayList<Node>>> couldMatch) {
level:	for (int level = 1; level < nodeMatchLists.size(); ++level) {
			/*
			 * now we check for each set of negative nodes (here: nodeMatchLists.get(level)),
			 * if there is a possible match within the mapping of the positive match,
			 * until a match for these negative nodes is found => return false!
			 * or all level were completed without matching negative nodes => return true!
			 */
			HashMap<PatternNode, Node> mapping = (HashMap<PatternNode, Node>)map.clone();
			ArrayList<Integer> currentTry = new ArrayList<Integer>();
			for (int i = 0; i < couldMatch.get(level).size(); ++i) {
				if (couldMatch.get(level).get(i) != null && couldMatch.get(level).get(i).size() > 0) {
					currentTry.add(0);
					mapping.put(nodeMatchLists.get(level).get(i), couldMatch.get(level).get(i).get(0));
				} else {
					continue level;
				}
			}
			/*
			 * only check this index against previous ones,
			 * if ok, increment and check only that one, and so on
			 */
			int checkIndex = 0;
	loop:	while (checkIndex > -1) {
				for (int i = checkIndex; i < nodeMatchLists.get(level).size(); ++i) {
					/*
					 * check nodeMatchLists.get(0).get(i) only against all previous nodes,
					 * if it is duplicate, or any edge (outgoing or incoming) is missing.
					 * if it fails: count this nodes candidate up (++currentTry.get(i)) if possible,
					 * if it can't be counted up, go one level back (i-1) and try increment there and so on.
					 * if nothing can't be counted up, return null (or set checkIndex to -1 and break);
					 * after incrementing a candidate, reset all currentTry-elements after it to 0,
					 * and set the checkIndex to the index of the increment currentTry-element, finally break
					 */
					PatternNode currentSubNode = nodeMatchLists.get(level).get(i);
					boolean fail = false;
	match:			for (int j = 0; j < i; ++j) {
						PatternNode otherSubNode = nodeMatchLists.get(level).get(j);
						// check if the negative node has a duplicate mapping to another negative node of this set
						if (mapping.get(currentSubNode) == mapping.get(otherSubNode)) {
							fail = true; // found duplicate!
							break match;
						}
						// check outgoing edges to previous negative nodes
						for (PatternEdge patternEdge: currentSubNode.getPatternEdges()) {
							if ("+".equals(patternEdge.getAction())) {
								continue;
							}
							if (patternEdge.getTarget() == otherSubNode) {
								boolean exists = mapping.get(currentSubNode).getEdges(patternEdge.getName()) != null && mapping.get(currentSubNode).getEdges(patternEdge.getName()).contains(mapping.get(otherSubNode));
								if (("!=".equals(patternEdge.getAction()) && exists) || (!"!=".equals(patternEdge.getAction()) && !exists)) {
									fail = true; // failure at outgoing edge
									break match;
								}
							}
						}
						// check incoming edges from previous negative nodes
						for (PatternEdge patternEdge: otherSubNode.getPatternEdges()) {
							if ("+".equals(patternEdge.getAction())) {
								continue;
							}
							if (patternEdge.getTarget() == currentSubNode) {
								boolean exists = mapping.get(otherSubNode).getEdges(patternEdge.getName()) != null && mapping.get(otherSubNode).getEdges(patternEdge.getName()).contains(mapping.get(currentSubNode));
								if (("!=".equals(patternEdge.getAction()) && exists) || (!"!=".equals(patternEdge.getAction()) && !exists)) {
									fail = true; // failure at incoming edge
									break match;
								}
							}
						}
					}
					// check if the negative node has a duplicate mapping to a positive node
					for (int k = 0; k < nodeMatchLists.get(0).size(); ++k) {
						if (mapping.get(currentSubNode) == mapping.get(nodeMatchLists.get(0).get(k))) {
							fail = true; // found duplicate!
							break;
						}
					}
					// check outgoing edges to positive nodes
					for (int k = 0; k < nodeMatchLists.get(0).size(); ++k) {
						for (PatternEdge patternEdge: currentSubNode.getPatternEdges()) {
							if ("+".equals(patternEdge.getAction())) {
								continue;
							}
							if (patternEdge.getTarget() == nodeMatchLists.get(0).get(k)) {
								boolean exists = mapping.get(currentSubNode).getEdges(patternEdge.getName()) != null && mapping.get(currentSubNode).getEdges(patternEdge.getName()).contains(mapping.get(nodeMatchLists.get(0).get(k)));
								if (("!=".equals(patternEdge.getAction()) && exists) || (!"!=".equals(patternEdge.getAction()) && !exists)) {
									fail = true; // failure at outgoing edge
									break;
								}
							}
						}
					}
					// check incoming edges from positive nodes
					for (int k = 0; k < nodeMatchLists.get(0).size(); ++k) {
						for (PatternEdge patternEdge: nodeMatchLists.get(0).get(k).getPatternEdges()) {
							if ("+".equals(patternEdge.getAction())) {
								continue;
							}
							if (patternEdge.getTarget() == currentSubNode) {
								boolean exists = mapping.get(nodeMatchLists.get(0).get(k)).getEdges(patternEdge.getName()) != null && mapping.get(nodeMatchLists.get(0).get(k)).getEdges(patternEdge.getName()).contains(mapping.get(currentSubNode));
								if (("!=".equals(patternEdge.getAction()) && exists) || (!"!=".equals(patternEdge.getAction()) && !exists)) {
									fail = true; // failure at incoming edge
									break;
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
						checkIndex = i;
						while (checkIndex >= 0 && currentTry.get(checkIndex) == couldMatch.get(level).get(checkIndex).size() - 1) {
							--checkIndex;
						}
						if (checkIndex >= 0) {
							currentTry.set(checkIndex, currentTry.get(checkIndex) + 1);
							mapping.put(nodeMatchLists.get(level).get(checkIndex), couldMatch.get(level).get(checkIndex).get(currentTry.get(checkIndex)));
							for (int j = checkIndex + 1; j < nodeMatchLists.get(level).size(); ++j) {
								currentTry.set(j, 0);
								mapping.put(nodeMatchLists.get(0).get(j), couldMatch.get(level).get(j).get(0));
							}
						}
						continue loop;
					}
				}
				return false;
			}
		}
		return true;
	}
	
	public static ArrayList<Match> matchPattern(Graph graph, PatternGraph pattern, boolean single, ArrayList<ArrayList<PatternNode>> nodeMatchLists, ArrayList<ArrayList<ArrayList<Node>>> couldMatch) {
		ArrayList<Match> matches = new ArrayList<Match>();
		
		HashMap<PatternNode, Node> mapping = new HashMap<PatternNode, Node>();
		// now going through all valid combinations (that make sense) of those loosely fitted candidates to find a match:
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		for (int i = 0; i < couldMatch.get(0).size(); ++i) {
			currentTry.add(0);
			mapping.put(nodeMatchLists.get(0).get(i), couldMatch.get(0).get(i).get(0));
		}
		/*
		 * only check this index against previous ones,
		 * if ok, increment and check only that one, and so on
		 */
		ArrayList<HashMap<PatternNode, Node>> mappings = new ArrayList<HashMap<PatternNode, Node>>();
		int checkIndex = 1;
loop:	while (checkIndex > -1) {
			for (int i = checkIndex; i < nodeMatchLists.get(0).size(); ++i) {
				/*
				 * check nodeMatchLists.get(0).get(i) only against all previous nodes,
				 * if it is duplicate, or any edge (outgoing or incoming) is missing.
				 * if it fails: count this nodes candidate up (++currentTry.get(i)) if possible,
				 * if it can't be counted up, go one level back (i-1) and try increment there and so on.
				 * if nothing can't be counted up, return null (or set checkIndex to -1 and break);
				 * after incrementing a candidate, reset all currentTry-elements after it to 0,
				 * and set the checkIndex to the index of the increment currentTry-element, finally break
				 */
				PatternNode currentSubNode = nodeMatchLists.get(0).get(i);
				boolean fail = false;
match:			for (int j = 0; j < i; ++j) {
					PatternNode otherSubNode = nodeMatchLists.get(0).get(j);
					if (mapping.get(currentSubNode) == mapping.get(otherSubNode)) {
						fail = true; // found duplicate!
						break match;
					}
					for (PatternEdge patternEdge: currentSubNode.getPatternEdges()) {
						if ("+".equals(patternEdge.getAction())) {
							continue;
						}
						if (patternEdge.getTarget() == otherSubNode) {
							boolean exists = mapping.get(currentSubNode).getEdges(patternEdge.getName()).contains(mapping.get(otherSubNode));
							if (("!=".equals(patternEdge.getAction()) && exists) || (!"!=".equals(patternEdge.getAction()) && !exists)) {
								fail = true; // failure at outgoing edge
								break match;
							}
						}
					}
					for (PatternEdge patternEdge: otherSubNode.getPatternEdges()) {
						if ("+".equals(patternEdge.getAction())) {
							continue;
						}
						if (patternEdge.getTarget() == currentSubNode) {
							boolean exists = mapping.get(otherSubNode).getEdges(patternEdge.getName()).contains(mapping.get(currentSubNode));
							if (("!=".equals(patternEdge.getAction()) && exists) || (!"!=".equals(patternEdge.getAction()) && !exists)) {
								fail = true; // failure at incoming edge
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
					checkIndex = i;
					while (checkIndex >= 0 && currentTry.get(checkIndex) == couldMatch.get(0).get(checkIndex).size() - 1) {
						--checkIndex;
					}
					if (checkIndex >= 0) {
						currentTry.set(checkIndex, currentTry.get(checkIndex) + 1);
						mapping.put(nodeMatchLists.get(0).get(checkIndex), couldMatch.get(0).get(checkIndex).get(currentTry.get(checkIndex)));
						for (int j = checkIndex + 1; j < nodeMatchLists.get(0).size(); ++j) {
							currentTry.set(j, 0);
							mapping.put(nodeMatchLists.get(0).get(j), couldMatch.get(0).get(j).get(0));
						}
					}
					continue loop;
				}
			}
			if (doesntMatchNegativeNodes(mapping, graph, nodeMatchLists, couldMatch)) {
				mappings.add((HashMap<PatternNode, Node>)mapping.clone()); // it ran through with no errors => success
				if (single) {
					break loop;
				}
			}
			// even if a match was found: count up, to find the next match:
			checkIndex = nodeMatchLists.get(0).size() - 1;
			while (checkIndex >= 0 && currentTry.get(checkIndex) == couldMatch.get(0).get(checkIndex).size() - 1) {
				--checkIndex;
			}
			if (checkIndex >= 0) {
				currentTry.set(checkIndex, currentTry.get(checkIndex) + 1);
				mapping.put(nodeMatchLists.get(0).get(checkIndex), couldMatch.get(0).get(checkIndex).get(currentTry.get(checkIndex)));
				for (int j = checkIndex + 1; j < nodeMatchLists.get(0).size(); ++j) {
					currentTry.set(j, 0);
					mapping.put(nodeMatchLists.get(0).get(j), couldMatch.get(0).get(j).get(0));
				}
			}
		}
		// nothing left to check => return results
		for (HashMap<PatternNode, Node> successfulMapping: mappings) {
			matches.add(new Match(graph, pattern, successfulMapping));
		}
//		if (matches.size() > 0 && pattern.getName().startsWith("signalTo")) { // TODO: remove debug
//			System.out.println("found " + matches.size() + " matches for '" + pattern.getName() + "'"); // TODO: remove debug
//		} // TODO: remove debug
		return matches;
	}

	/**
	 * finds matches for a pattern in a graph.
	 * 
	 * @param graph the graph to match the pattern on
	 * @param pattern the pattern to match
	 * @return a list of matches for the pattern in the graph
	 */
	public static ArrayList<Match> matchPattern(Graph graph, PatternGraph pattern, boolean single) {
		// first get a 'smart' list of first all positive nodes and then multiple lists of negative nodes that belong together:
		ArrayList<ArrayList<PatternNode>> nodeMatchLists = calculateNodeMatchingLists(pattern);
		
		if (nodeMatchLists.get(0).size() > graph.getNodes().size()) {
			return new ArrayList<Match>(); // more positive nodes to match, than existing -> fail
		}
		
		// now check for 'loosely matched candidates' of nodes to match (level == 0: positive nodes, level > 0: negative node sets):
		ArrayList<ArrayList<ArrayList<Node>>> couldMatch = findPossibleMatchesForPositiveAndNegativeNodes(graph, nodeMatchLists);
		
		// TODO: maybe do something like this 'remove impossible matches'...
		
		if (couldMatch == null) {
			return new ArrayList<Match>(); // some positive node has no match -> fail
		}
		
		// finally find those matches:
		return matchPattern(graph, pattern, single, nodeMatchLists, couldMatch);
	}
	
	public static ArrayList<Match> matchPatternOld(Graph graph, PatternGraph pattern, boolean single) {
		ArrayList<Match> matches = new ArrayList<Match>();
		// step 1: for every PatternNode, find all possible Nodes:
		ArrayList<ArrayList<Node>> couldMatch = new ArrayList<ArrayList<Node>>();
		ArrayList<ArrayList<Node>> negativeCouldMatch = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < pattern.getPatternNodes().size(); ++i) {
			PatternNode patternNode = pattern.getPatternNodes().get(i);
			// (nodes to be created (with action == "+") are skipped)
			if ("+".equals(patternNode.getAction())) {
				continue;
			}
			// for the other nodes (with action == "-" or with action == "==") we try to find matches
			if (!"!=".equals(patternNode.getAction())) {
				couldMatch.add(new ArrayList<Node>());
			} else {
				// for those with action == "!=", we look for matches, too.
				negativeCouldMatch.add(new ArrayList<Node>());
			}
nodes:		for (int j = 0; j < graph.getNodes().size(); ++j) {
				Node node = graph.getNodes().get(j);
				// so first we check the attributes
				for (int k = 0; k < patternNode.getPatternAttributes().size(); ++k) {
					PatternAttribute patternAttribute = patternNode.getPatternAttributes().get(k);
					// attributes to be created ("+") are skipped
					if ("+".equals(patternAttribute.getAction())) {
						continue;
					}
					// other attributes are being checked ("==" and "-" are checked normally; "!=" is checked negatively)
					boolean attributeValueMatch = PatternEngine.evaluate(node, patternAttribute.getValue());
					if (("!=".equals(patternAttribute.getAction()) && attributeValueMatch) || (!"!=".equals(patternAttribute.getAction()) && !attributeValueMatch)) {
						continue nodes;
					}
				}
				if (!PatternEngine.evaluate(node, patternNode.getAttributeMatchExpression())) {
					continue nodes;
				}
				// now edges are being checked 'loosely'
				for (int k = 0; k < patternNode.getPatternEdges().size(); ++k) {
					PatternEdge patternEdge = patternNode.getPatternEdges().get(k);
					// edges that shouldn't exist are skipped ("+" and "!=" plus all edges to nodes with "!=")
					if ("+".equals(patternEdge.getAction()) || "!=".equals(patternEdge.getAction()) || "!=".equals(patternEdge.getTarget().getAction())) {
						continue;
					}
					// other edges ("==" and "-") should at least exist with this name at all...
					if (!node.getEdges().containsKey(patternEdge.getName())) {
						continue nodes;
					}
				}
				// so this node seems to be a candidate for the pattern's node => we add it to a list
				if (!"!=".equals(patternNode.getAction())) {
					couldMatch.get(couldMatch.size() - 1).add(node);
				} else {
					negativeCouldMatch.get(negativeCouldMatch.size() - 1).add(node);
				}
			}
			// if there's not even one loosely matched candidate, there will be no match at all => return empty list!
			if (!"!=".equals(patternNode.getAction()) && couldMatch.get(couldMatch.size() - 1).size() < 1) {
				return matches;
			}
		}
		boolean canTryAnother = false;
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		// we initialize the indices for candidates for each node of the pattern:
		for (int i = 0; i < couldMatch.size(); ++i) {
			currentTry.add(0);
		}
		// step 2: verify the possible matches
		HashSet<Node> usedNodes = new HashSet<Node>();
		// use next duplicate-free configuration (begin)
fix:	for (int k = currentTry.size() - 1; k >= 0; --k) {
			while (usedNodes.contains(couldMatch.get(k).get(currentTry.get(k)))) {
				if (currentTry.get(k) >= couldMatch.get(k).size() - 1) {
					break fix;
				}
				currentTry.set(k, currentTry.get(k) + 1);
			}
			usedNodes.add(couldMatch.get(k).get(currentTry.get(k)));
		} // use next duplicate-free configuration (end)
		do {
			canTryAnother = false;
			HashMap<PatternNode, Node> mapping = new HashMap<PatternNode, Node>(); 
			// it's not allowed for two nodes of the pattern to match the same node of the graph:
			usedNodes = new HashSet<Node>();
			boolean duplicateChoice = false;
			int count = 0;
			for (int i = 0; i < couldMatch.size(); ++i) {
				while ("+".equals(pattern.getPatternNodes().get(count).getAction()) || "!=".equals(pattern.getPatternNodes().get(count).getAction())) {
					++count;
				}
				PatternNode patternNode = pattern.getPatternNodes().get(count);
				++count;
				Node node = couldMatch.get(i).get(currentTry.get(i));
				mapping.put(patternNode, node);
				if (usedNodes.contains(node)) {
					duplicateChoice = true; // two PatternNodes point to the same Node => can't be!
					break;
				}
				usedNodes.add(node);
			}
			if (!duplicateChoice) {
				boolean fail = false;
				// what's missing now is a check for the actual targets of each edge
				count = 0;
nodes:			for (int i = 0; i < couldMatch.size(); ++i) {
					while ("+".equals(pattern.getPatternNodes().get(count).getAction()) || "!=".equals(pattern.getPatternNodes().get(count).getAction())) {
						++count;
					}
					PatternNode sourcePatternNode = pattern.getPatternNodes().get(count);
					++count;
					for (int j = 0; j < sourcePatternNode.getPatternEdges().size(); ++j) {
						PatternEdge patternEdge = sourcePatternNode.getPatternEdges().get(j);
						// of course, edges to be added ("+") are skipped for the check
						if ("+".equals(patternEdge.getAction())) {
							continue;
						}
						// edges with a negative node as target are skipped, too => those are handled later on
						if ("!=".equals(patternEdge.getTarget().getAction())) {
							continue;
						}
						// edges with "==", "-" are checked normally, edges with "!=" are checked negatively:
						boolean isThere = mapping.get(sourcePatternNode).getEdges(patternEdge.getName()).contains(mapping.get(patternEdge.getTarget()));
						if (("!=".equals(patternEdge.getAction()) && isThere) || (!"!=".equals(patternEdge.getAction()) && !isThere)) {
							fail = true;
							break nodes;
						}
					}
				}
				if (!fail) {
					boolean foundNoNastyNegativeNode = true;
					// the absolute final check is to look for those negative nodes we previously skipped
					boolean canTryAnotherNegative = false;
					ArrayList<Integer> currentNegativeTry = new ArrayList<Integer>();
					// we initialize the indices for candidates for each negative node of the pattern:
					for (int i = 0; i < negativeCouldMatch.size(); ++i) {
						currentNegativeTry.add(0);
					}
					HashSet<Node> usedNegativeNodes = new HashSet<Node>();
					// use next duplicate-free configuration (begin)
			fix:	for (int k = currentNegativeTry.size() - 1; k >= 0; --k) {
						while (usedNegativeNodes.contains(negativeCouldMatch.get(k).get(currentNegativeTry.get(k)))) {
							if (currentNegativeTry.get(k) >= negativeCouldMatch.get(k).size() - 1) {
								break fix;
							}
							currentNegativeTry.set(k, currentNegativeTry.get(k) + 1);
						}
						usedNegativeNodes.add(negativeCouldMatch.get(k).get(currentNegativeTry.get(k)));
					} // use next duplicate-free configuration (end)
negativeCheck:		do {
						canTryAnotherNegative = false;
						HashMap<PatternNode, Node> negativeMapping = new HashMap<PatternNode, Node>(); 
						// it's not allowed for two nodes of the pattern to match the same node of the graph:
						usedNegativeNodes = new HashSet<Node>();
						boolean duplicateNegativeChoice = false;
						int negativeCount = 0;
						for (int i = 0; i < negativeCouldMatch.size(); ++i) {
							while (!"!=".equals(pattern.getPatternNodes().get(negativeCount).getAction())) {
								++negativeCount;
							}
							PatternNode patternNode = pattern.getPatternNodes().get(negativeCount);
							++negativeCount;
							Node node = negativeCouldMatch.get(i).get(currentNegativeTry.get(i));
							negativeMapping.put(patternNode, node);
							if (usedNegativeNodes.contains(node) || usedNodes.contains(node)) {
								duplicateNegativeChoice = true; // two PatternNodes point to the same Node => can't be!
								break;
							}
							usedNegativeNodes.add(node);
						}
						if (!duplicateNegativeChoice) {
							boolean negativeFail = false;
							// what's missing now is a check for the actual targets of each edge
							negativeCount = 0;
			nodes:			for (int i = 0; i < negativeCouldMatch.size(); ++i) {
								while (!"!=".equals(pattern.getPatternNodes().get(negativeCount).getAction())) {
									++negativeCount;
								}
								PatternNode sourcePatternNode = pattern.getPatternNodes().get(negativeCount);
								++negativeCount;
								for (int j = 0; j < sourcePatternNode.getPatternEdges().size(); ++j) {
									PatternEdge patternEdge = sourcePatternNode.getPatternEdges().get(j);
									// of course, edges to be added ("+") are skipped for the check
									if ("+".equals(patternEdge.getAction())) {
										continue;
									}
									boolean isThere;
									if (!"!=".equals(patternEdge.getTarget().getAction())) {
										isThere = negativeMapping.get(sourcePatternNode).getEdges(patternEdge.getName()).contains(mapping.get(patternEdge.getTarget()));
									} else {
										isThere = negativeMapping.get(sourcePatternNode).getEdges(patternEdge.getName()).contains(negativeMapping.get(patternEdge.getTarget()));
									}
									// edges with "==", "-" are checked normally, edges with "!=" are checked negatively:
									if (("!=".equals(patternEdge.getAction()) && isThere) || (!"!=".equals(patternEdge.getAction()) && !isThere)) {
										negativeFail = true;
										break nodes;
									}
								}
								// annoyingly, here the incoming edges from non-negative nodes have to be checked, too
								int incomingSourceCount = 0;
								for (int j = 0; j < couldMatch.size(); ++j) {
									while ("+".equals(pattern.getPatternNodes().get(incomingSourceCount).getAction()) || "!=".equals(pattern.getPatternNodes().get(incomingSourceCount).getAction())) {
										++incomingSourceCount;
									}
									PatternNode patternNodeIncomingToNegative = pattern.getPatternNodes().get(incomingSourceCount);
									++incomingSourceCount;
									for (PatternEdge patternEdge: patternNodeIncomingToNegative.getPatternEdges()) {
										// confusing but true: 'sourcePatternNode' is the target here...
										if (!patternEdge.getTarget().equals(sourcePatternNode)) {
											continue;
										}
										boolean isThere = mapping.get(patternNodeIncomingToNegative).getEdges(patternEdge.getName()).contains(negativeMapping.get(sourcePatternNode));
										// edges with "==", "-" are checked normally, edges with "!=" are checked negatively:
										if (("!=".equals(patternEdge.getAction()) && isThere) || (!"!=".equals(patternEdge.getAction()) && !isThere)) {
											negativeFail = true;
											break nodes;
										}
									}
								}
							}
							if (!negativeFail) {
								// a SINGLE negative node was matched -> the WHOLE pattern fails!!
								foundNoNastyNegativeNode = false;
								// make it fail, to continue the search:
								break negativeCheck;
							}
						}
						for (int i = 0; i < currentNegativeTry.size(); ++i) {
							if (currentNegativeTry.get(i) < negativeCouldMatch.get(i).size() - 1) {
								currentNegativeTry.set(i, currentNegativeTry.get(i) + 1);
								for (int j = 0; j < i; ++j) {
									currentNegativeTry.set(j, 0);
								}
								usedNegativeNodes = new HashSet<Node>(); // use next duplicate-free configuration (begin)
			fix:				for (int k = currentNegativeTry.size() - 1; k >= 0; --k) {
									while (usedNegativeNodes.contains(negativeCouldMatch.get(k).get(currentNegativeTry.get(k)))) {
										if (currentNegativeTry.get(k) >= negativeCouldMatch.get(k).size() - 1) {
											break fix;
										}
										currentNegativeTry.set(k, currentNegativeTry.get(k) + 1);
									}
									usedNegativeNodes.add(negativeCouldMatch.get(k).get(currentNegativeTry.get(k)));
								} // use next duplicate-free configuration (end)
								canTryAnotherNegative = true;
								break;
							}
						}
					} while (canTryAnotherNegative);
					boolean noProblemWithNegativeNodes = false;
					if (negativeCouldMatch.size() == 0) {
						noProblemWithNegativeNodes = true;
					}
					for (ArrayList<Node> possible: negativeCouldMatch) {
						if (possible.size() == 0) {
							noProblemWithNegativeNodes = true;
						}
					}
					if (foundNoNastyNegativeNode || noProblemWithNegativeNodes) {
						matches.add(new Match(graph, pattern, mapping));
						if (single) {
							return matches;
						}
					}
				}
			}
			for (int i = 0; i < currentTry.size(); ++i) {
				if (currentTry.get(i) < couldMatch.get(i).size() - 1) {
					currentTry.set(i, currentTry.get(i) + 1);
					for (int j = 0; j < i; ++j) {
						currentTry.set(j, 0);
					}
					canTryAnother = true;
					usedNodes = new HashSet<Node>(); // use next duplicate-free configuration (begin)
fix:				for (int k = currentTry.size() - 1; k >= 0; --k) {
						while (usedNodes.contains(couldMatch.get(k).get(currentTry.get(k)))) {
							if (currentTry.get(k) >= couldMatch.get(k).size() - 1) {
								break fix;
							}
							currentTry.set(k, currentTry.get(k) + 1);
						}
						usedNodes.add(couldMatch.get(k).get(currentTry.get(k)));
					} // use next duplicate-free configuration (end)
					break;
				}
			}
		} while (canTryAnother);
		return matches;
	}
	
	/**
	 * applies a pattern to a match, applying all 'create'- and 'delete'-actions specified in the pattern to the graph.
	 * 
	 * @param match the match that was previously found
	 * @return the resulting graph
	 */
	public static Graph applyMatch(Match match) {
//		System.out.println("applying match for '" + match.getPattern().getName() + "'"); // TODO: remove debug
		Graph clonedGraph = match.getGraph().clone();
		HashMap<PatternNode, Node> clonedNodeMatch = new HashMap<PatternNode, Node>();
		for (PatternNode patternNode: match.getNodeMatch().keySet()) {
			clonedNodeMatch.put(patternNode, clonedGraph.getNodes().get(match.getGraph().getNodes().indexOf(match.getNodeMatch().get(patternNode))));
		}
		
		// first create new nodes, so it can be used for targets of new edges from other nodes and so on:
		for (PatternNode patternNode: match.getPattern().getPatternNodes()) {
			Node matchedNode;
			switch (patternNode.getAction()) {
			case "+": // create
				matchedNode = new Node();
				clonedGraph.addNode(matchedNode);
				clonedNodeMatch.put(patternNode, matchedNode);
			}
		}
		
		// then do the rest, except for creating new attributes, because they could also be removed (could remove a new attribute):
		for (PatternNode patternNode: match.getPattern().getPatternNodes()) {
			Node matchedNode;
			switch (patternNode.getAction()) {
			case "-": // remove
				clonedGraph.removeNode(clonedNodeMatch.get(patternNode));
				continue;
			default: // match or create
				matchedNode = clonedNodeMatch.get(patternNode);
			}
			for (PatternAttribute patternAttribute: patternNode.getPatternAttributes()) {
				switch (patternAttribute.getAction()) {
				case "-": // remove
					matchedNode.removeAttribute(patternAttribute.getName());
				}
			}
			for (PatternEdge patternEdge: patternNode.getPatternEdges()) {
				switch (patternEdge.getAction()) {
				case "-": // remove
					matchedNode.removeEdge(patternEdge.getName(), clonedNodeMatch.get(patternEdge.getTarget()));
					break;
				case "+": // create
					matchedNode.addEdge(patternEdge.getName(), clonedNodeMatch.get(patternEdge.getTarget()));
				}
			}
		}

		// then create new attributes:
		for (PatternNode patternNode: match.getPattern().getPatternNodes()) {
			Node matchedNode;
			switch (patternNode.getAction()) {
			case "+":
			case "==":
				matchedNode = clonedNodeMatch.get(patternNode);
				break;
			default:
				continue;
			}
			for (PatternAttribute patternAttribute: patternNode.getPatternAttributes()) {
				switch (patternAttribute.getAction()) {
				case "+": // create
					matchedNode.setAttribute(patternAttribute.getName(), patternAttribute.getValue());
				}
			}
		}
		return clonedGraph;
	}
	
	public static boolean evaluate(Node node, String expression) {
		return evaluate(buildNodeEvaluator(node), expression);
	}
	public static Evaluator buildNodeEvaluator(Node node) {
		Evaluator evaluator = new Evaluator();
		for (String key: node.getAttributes().keySet()) {
			if (node.getAttribute(key) instanceof String) {
				// Strings are contained in single quotes:
				evaluator.putVariable(key, "'" + node.getAttribute(key) + "'");
			} else if (node.getAttribute(key) instanceof Boolean) {
				// Booleans are handled as 1 (or 1.0) respectively 0 (or 0.0):
				evaluator.putVariable(key, "" + ((boolean)node.getAttribute(key) ? "1.0" : "0.0"));
			} else {
				// other values like numbers are just written as is:
				evaluator.putVariable(key, "" + node.getAttribute(key));
			}
		}
		return evaluator;
	}
	public static boolean evaluate(Evaluator evaluator, String expression) {
		// 'no condition' is always fulfilled:
		if (expression == null || "".equals(expression)) {
			return true;
		}
		boolean matched = false;
		try {
			// the expression must yield a boolean value, so "1.0" means true, "0.0" means false:
			matched = "1.0".equals(evaluator.evaluate(expression)); // calls the expression library
		} catch (Throwable t) {
			// error means, the condition isn't fulfulled:
			matched = false;
		}
		return matched;
	}
	
}
