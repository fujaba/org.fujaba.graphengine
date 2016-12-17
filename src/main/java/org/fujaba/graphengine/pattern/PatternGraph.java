package org.fujaba.graphengine.pattern;

import java.util.ArrayList;

import org.fujaba.graphengine.GraphEngine;

/**
 * The PatternGraph is a kind of pattern to be used with a Graph.
 * It can contain information for pattern matching,
 * as well as actions to apply when matches were found.
 * 
 * @author Philipp Kolodziej
 */
public class PatternGraph {
	
//	/**
//	 * the sub-PatternGraphs of this PatternGraph
//	 */
//	private ArrayList<PatternGraph> subPatternGraphs = new ArrayList<PatternGraph>();
	/**
	 * the PatternNode contained in this PatternGraph (not in sub-PatternGraphs)
	 */
	private ArrayList<PatternNode> patternNodes = new ArrayList<PatternNode>();
	
//	public ArrayList<PatternGraph> getSubPatternGraphs() {
//		return subPatternGraphs;
//	}
//	public PatternGraph setSubPatternGraphs(ArrayList<PatternGraph> subPatternGraphs) {
//		this.subPatternGraphs = subPatternGraphs;
//		return this;
//	}
//	public PatternGraph addSubPatternGraph(PatternGraph subPatternGraph) {
//		this.subPatternGraphs.add(subPatternGraph);
//		return this;
//	}
	public ArrayList<PatternNode> getPatternNodes() {
		return patternNodes;
	}
	public PatternGraph setPatternNodes(ArrayList<PatternNode> patternNodes) {
		this.patternNodes = patternNodes;
		return this;
	}
	public PatternGraph addPatternNode(PatternNode... patternNodes) {
		for (PatternNode patternNode: patternNodes) {
			this.patternNodes.add(patternNode);
		}
		return this;
	}
	
	@Override
	public String toString() {
		return GraphEngine.getGson().toJson(this);
	}

}
