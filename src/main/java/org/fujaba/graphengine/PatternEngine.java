package org.fujaba.graphengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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

	/**
	 * finds matches for a pattern in a graph.
	 * 
	 * @param graph the graph to match the pattern on
	 * @param pattern the pattern to match
	 * @return a list of matches for the pattern in the graph
	 */
	public static ArrayList<Match> matchPattern(Graph graph, PatternGraph pattern, boolean single) {
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
					// edges that shouldn't exist are skipped ("+" and "!=")
					if ("+".equals(patternEdge.getAction()) || "!=".equals(patternEdge.getAction())) {
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
			if (!"!=".equals(patternNode.getAction()) && couldMatch.get(i).size() < 1) {
				return matches;
			}
		}
		// step 2: verify the possible matches
		boolean canTryAnother = false;
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		// we initialize the indices for candidates for each node of the pattern:
		for (int i = 0; i < couldMatch.size(); ++i) {
			currentTry.add(0);
		}
		do {
			canTryAnother = false;
			HashMap<PatternNode, Node> mapping = new HashMap<PatternNode, Node>(); 
			// it's not allowed for two nodes of the pattern to match the same node of the graph:
			HashSet<Node> usedNodes = new HashSet<Node>();
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
negativeCheck:		do {
						canTryAnotherNegative = false;
						HashMap<PatternNode, Node> negativeMapping = new HashMap<PatternNode, Node>(); 
						// it's not allowed for two nodes of the pattern to match the same node of the graph:
						HashSet<Node> usedNegativeNodes = new HashSet<Node>();
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
						for (int i = 0; i < currentNegativeTry.size(); ++i) { //TODO: do the thing that drastically improved performance of isomorphic checks!
							if (currentNegativeTry.get(i) < negativeCouldMatch.get(i).size() - 1) {
								currentNegativeTry.set(i, currentNegativeTry.get(i) + 1);
								for (int j = 0; j < i; ++j) {
									currentNegativeTry.set(j, 0);
								}
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
			for (int i = 0; i < currentTry.size(); ++i) { //TODO: do the thing that drastically improved performance of isomorphic checks!
				if (currentTry.get(i) < couldMatch.get(i).size() - 1) {
					currentTry.set(i, currentTry.get(i) + 1);
					for (int j = 0; j < i; ++j) {
						currentTry.set(j, 0);
					}
					canTryAnother = true;
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
		
		Graph clonedGraph = match.getGraph().clone();
		HashMap<PatternNode, Node> clonedNodeMatch = new HashMap<PatternNode, Node>();
		for (PatternNode patternNode: match.getNodeMatch().keySet()) {
			clonedNodeMatch.put(patternNode, clonedGraph.getNodes().get(match.getGraph().getNodes().indexOf(match.getNodeMatch().get(patternNode))));
		}
		
		for (PatternNode patternNode: match.getPattern().getPatternNodes()) {
			Node matchedNode;
			switch (patternNode.getAction()) {
			case "-": // remove
				clonedGraph.removeNode(clonedNodeMatch.get(patternNode));
				continue;
			case "+": // create
				matchedNode = new Node();
				clonedGraph.addNode(matchedNode);
				break;
			default: // match
				matchedNode = clonedNodeMatch.get(patternNode);
			}
			for (PatternAttribute patternAttribute: patternNode.getPatternAttributes()) {
				switch (patternAttribute.getAction()) {
				case "-": // remove
					matchedNode.removeAttribute(patternAttribute.getName());
					break;
				case "+": // create
					matchedNode.setAttribute(patternAttribute.getName(), patternAttribute.getValue());
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
