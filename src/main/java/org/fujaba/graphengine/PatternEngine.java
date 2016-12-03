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
	 * matches and directly applies a pattern as often as possible
	 * until the optional limit is met, without revisiting graphs,
	 * that were already used (preventing endless cycles).
	 * 
	 * @param graph the graph to apply the pattern on
	 * @param pattern the pattern to apply
	 * @param maxCount the maximum number of times the pattern should be applied to the graph
	 * @return the resulting graph
	 */
	public static Graph applyPattern(Graph graph, PatternGraph pattern, int maxCount) { // TODO: implement
		Graph result = graph.clone();
		
		return result;
	}

	/**
	 * finds matches for a pattern in a graph.
	 * 
	 * @param graph the graph to match the pattern on
	 * @param pattern the pattern to match
	 * @return a list of matches for the pattern in the graph
	 */
	public static ArrayList<Match> matchPattern(Graph graph, PatternGraph pattern) {
		ArrayList<Match> matches = new ArrayList<Match>();
		// step 1: for every PatternNode, find all possible Nodes:
		ArrayList<ArrayList<Node>> couldMatch = new ArrayList<ArrayList<Node>>();
		for (int i = 0; i < pattern.getPatternNodes().size(); ++i) {
			PatternNode patternNode = pattern.getPatternNodes().get(i);
			if (patternNode.getAction() == "create") {
				continue;
			}
			couldMatch.add(new ArrayList<Node>());
nodes:		for (int j = 0; j < graph.getNodes().size(); ++j) {
				Node node = graph.getNodes().get(j);
				for (int k = 0; k < patternNode.getPatternAttributes().size(); ++k) {
					PatternAttribute patternAttribute = patternNode.getPatternAttributes().get(k);
					if (patternAttribute.getAction() == "create") {
						continue;
					}
					boolean attributeValueMatch = patternAttribute.getValue().equals(node.getAttribute(patternAttribute.getName()));
					if ((patternAttribute.isNegative() && attributeValueMatch) || (!patternAttribute.isNegative() && !attributeValueMatch)) {
						continue nodes;
					}
				}
				for (int k = 0; k < patternNode.getPatternEdges().size(); ++k) {
					PatternEdge patternEdge = patternNode.getPatternEdges().get(k);
					if (patternEdge.getAction() == "create" || patternEdge.isNegative()) {
						continue;
					}
					if (!node.getEdges().containsKey(patternEdge.getName())) {
						continue nodes;
					}
				}
				couldMatch.get(i).add(node);
			}
			if (couldMatch.get(i).size() < 1) {
				return matches;
			}
		}
		// step 2: verify the possible matches
		boolean canTryAnother = false;
		ArrayList<Integer> currentTry = new ArrayList<Integer>();
		for (int i = 0; i < couldMatch.size(); ++i) {
			currentTry.add(0);
		}
		do {
			canTryAnother = false;
			HashMap<PatternNode, Node> mapping = new HashMap<PatternNode, Node>(); 
			HashSet<Node> usedNodes = new HashSet<Node>();
			boolean duplicateChoice = false;
			for (int i = 0; i < couldMatch.size(); ++i) {
				PatternNode patternNode = pattern.getPatternNodes().get(i);
				Node node = couldMatch.get(i).get(currentTry.get(i));
				mapping.put(patternNode, node);
				if (usedNodes.contains(node)) {
					duplicateChoice = true;
					break;
				}
				usedNodes.add(node);
			}
			if (!duplicateChoice) {
				boolean fail = false;
nodes:			for (int i = 0; i < pattern.getPatternNodes().size(); ++i) {
					PatternNode sourcePatternNode = pattern.getPatternNodes().get(i);
					for (int j = 0; j < sourcePatternNode.getPatternEdges().size(); ++j) {
						PatternEdge patternEdge = sourcePatternNode.getPatternEdges().get(j);
						if (patternEdge.getAction() == "create") {
							continue;
						}
						boolean isThere = mapping.get(sourcePatternNode).getEdges(patternEdge.getName()).contains(mapping.get(patternEdge.getTarget()));
						if ((patternEdge.isNegative() && isThere) || (!patternEdge.isNegative() && !isThere)) {
							fail = true;
							break nodes;
						}
					}
				}
				if (!fail) {
					matches.add(new Match(graph, pattern, mapping));
				}
			}
			for (int i = 0; i < currentTry.size(); ++i) {
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
		for (PatternNode patternNode: match.getPattern().getPatternNodes()) {
			Node matchedNode;
			switch (patternNode.getAction()) {
			case "remove": // remove
				match.getGraph().removeNode(match.getNodeMatch().get(patternNode));
				continue;
			case "create": // create
				matchedNode = new Node();
				break;
			default: // match
				matchedNode = match.getNodeMatch().get(patternNode);
			}
			for (PatternAttribute patternAttribute: patternNode.getPatternAttributes()) {
				switch (patternAttribute.getAction()) {
				case "remove": // remove
					matchedNode.removeAttribute(patternAttribute.getName());
					break;
				case "create": // create
					matchedNode.setAttribute(patternAttribute.getName(), patternAttribute.getValue());
				}
			}
			for (PatternEdge patternEdge: patternNode.getPatternEdges()) {
				switch (patternEdge.getAction()) {
				case "remove": // remove
					matchedNode.removeEdge(patternEdge.getName(), match.getNodeMatch().get(patternEdge.getTarget()));
					break;
				case "create": // create
					matchedNode.addEdge(patternEdge.getName(), match.getNodeMatch().get(patternEdge.getTarget()));
				}
			}
		}
		return match.getGraph();
	}
	
}
