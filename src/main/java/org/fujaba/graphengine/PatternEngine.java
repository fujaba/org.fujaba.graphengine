package org.fujaba.graphengine;

import java.util.ArrayList;

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
	public static ArrayList<Match> matchPattern(Graph graph, PatternGraph pattern) { // TODO: implement
		ArrayList<Match> matches = new ArrayList<Match>();
		
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
