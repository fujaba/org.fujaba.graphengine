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

/**
 * The PatternEngine contains all logic concerning PatternGraphs.
 * 
 * @author Philipp Kolodziej
 */
public class PatternEngine {
	/**
	 * matches and directly applies a pattern as often as possible,
	 * without revisiting graphs, that were already used (preventing endless cycles).
	 * 
	 * @param graph the graph to apply the pattern on
	 * @param pattern the pattern to apply
	 * @param single wheter the pattern should be only applied a single time
	 * @return the resulting graph
	 */
	public static Graph applyPattern(Graph graph, PatternGraph pattern, boolean single) {
		ArrayList<PatternGraph> patterns = new ArrayList<PatternGraph>();
		patterns.add(pattern);
		return applyPatterns(graph, patterns, single);
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
	public static Graph applyPatterns(Graph graph, ArrayList<PatternGraph> patterns, boolean single) {
		if (graph == null) {
			return null;
		}
		ArrayList<Graph> history = new ArrayList<Graph>();
		Graph result = graph;
		if (patterns == null) {
			return result;
		}
		history.add(result);
		boolean canTryAnother = false;
		do {
			canTryAnother = false;
			ArrayList<Match> matches = new ArrayList<Match>();
			boolean foundNewOne = false;
			int currentPatternIndex = 0;
loop:		while (!foundNewOne && currentPatternIndex < patterns.size()) {
				matches = matchPattern(result, patterns.get(currentPatternIndex), false);
				++currentPatternIndex;
				if (matches.size() == 0) {
					continue;
				}
				for (Match match: matches) {
					Graph next = applyMatch(match);
					if (!contains(history, next)) {
						history.add(next.clone());
						result = next;
						if (single) {
							return result;
						}
						canTryAnother = true;
						foundNewOne = true;
						break loop;
					}
				}
			}
		} while (canTryAnother);
		return result;
	}
	
	private static boolean contains(ArrayList<Graph> graphs, Graph graph) {
		for (Graph g: graphs) {
			if (GraphEngine.isIsomorphTo(g, graph)) {
				return true;
			}
		}
		return false;
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
					boolean attributeValueMatch = patternAttribute.getValue().equals(node.getAttribute(patternAttribute.getName()));
					if (("!=".equals(patternAttribute.getAction()) && attributeValueMatch) || (!"!=".equals(patternAttribute.getAction()) && !attributeValueMatch)) {
						continue nodes;
					}
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
					couldMatch.get(i).add(node);
				} else {
					negativeCouldMatch.get(i).add(node);
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
										continue nodes;
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
											continue nodes;
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
					if (foundNoNastyNegativeNode) {
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
	
}
