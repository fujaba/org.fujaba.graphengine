package org.fujaba.graphengine;

import java.util.HashMap;

import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;

import net.sourceforge.jeval.Evaluator;

/**
 * A Match is a single match of a PatternGraph in a Graph.
 * Every PatternNode, PatternEdge and PatternAttribute is mapped to a Node,
 * or one of the edges or attributes inside of it.
 * 
 * @author Philipp Kolodziej
 */
public class Match {

	/**
	 * the graph, this match did match on
	 */
	private Graph graph;
	/**
	 * the pattern that was matched
	 */
	private PatternGraph pattern;
	/**
	 * the mapping from pattern nodes to graph nodes
	 */
	private HashMap<PatternNode, Node> nodeMatch;
	
	private HashMap<String, String> edgeMatch;

	/**
	 * the evaluator that's used for wildcard labels within the GTR
	 */
	private Evaluator edgeEvaluator;

	public static Evaluator buildEdgeEvaluator(HashMap<String, String> labelMatches) {
		Evaluator evaluator = new Evaluator();
		for (String key: labelMatches.keySet()) {
			evaluator.putVariable(key, "" + labelMatches.get(key));
		}
		return evaluator;
	}

	/**
	 * the constructor for this Match.
	 * @param graph the graph, this match did match on
	 * @param pattern the pattern that was matched
	 * @param nodeMatch the mapping from pattern nodes to graph nodes
	 */
	public Match(Graph graph, PatternGraph pattern, HashMap<PatternNode, Node> nodeMatch, HashMap<String, String> edgeMatch) {
		this.graph = graph;
		this.pattern = pattern;
		this.nodeMatch = nodeMatch;
		this.edgeEvaluator = buildEdgeEvaluator(edgeMatch);
		this.edgeMatch = edgeMatch;
	}
	
	public Graph getGraph() {
		return graph;
	}
	public void setGraph(Graph graph) {
		this.graph = graph;
	}
	public PatternGraph getPattern() {
		return pattern;
	}
	public void setPattern(PatternGraph pattern) {
		this.pattern = pattern;
	}
	public HashMap<PatternNode, Node> getNodeMatch() {
		return nodeMatch;
	}
	public void setNodeMatch(HashMap<PatternNode, Node> nodeMatch) {
		this.nodeMatch = nodeMatch;
	}
	public Evaluator getEdgeEvaluator() {
		return edgeEvaluator;
	}
	public void setEdgeEvaluator(Evaluator labelEvaluator) {
		this.edgeEvaluator = labelEvaluator;
	}
	public HashMap<String, String> getEdgeMatch() {
		return edgeMatch;
	}
	public void setEdgeMatch(HashMap<String, String> edgeMatch) {
		this.edgeMatch = edgeMatch;
	}
	
}
