package org.fujaba.graphengine.pattern;

import java.util.HashMap;

/**
 * A graph rewrite rule in form of a graph for use in graph transformation systems.
 * 
 * @author Philipp Kolodziej
 */
public class PatternGraph {

	private HashMap<String, PatternNode> patternNodes = new HashMap<String, PatternNode>();

	/**
	 * A constructor to build a PatternGraph from its given PatternNodes
	 * 
	 * @param patternNodes the PatternGraph's PatternNodes
	 */
	public PatternGraph(HashMap<String, PatternNode> patternNodes) {
		this.patternNodes = patternNodes;
	}

	public HashMap<String, PatternNode> getPatternNodes() {
		return patternNodes;
	}
	public PatternNode getPatternNode(String id) {
		return patternNodes.get(id);
	}
	public void setPatternNodes(HashMap<String, PatternNode> patternNodes) {
		this.patternNodes = patternNodes;
	}
	
	@Override
	public String toString() {
		String result = "{";
		result += "\"nodes\":" + this.getPatternNodes().values();
		return result + "}";
	}

}
