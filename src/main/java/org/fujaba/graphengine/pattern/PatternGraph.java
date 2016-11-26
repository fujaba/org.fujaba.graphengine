package org.fujaba.graphengine.pattern;

import java.util.ArrayList;

public class PatternGraph extends Pattern {
	
	/**
	 * the sub-PatternGraphs of this PatternGraph
	 */
	private ArrayList<PatternGraph> subPatternGraphs = new ArrayList<PatternGraph>();
	/**
	 * the PatternNode contained in this PatternGraph (not in sub-PatternGraphs)
	 */
	private ArrayList<PatternNode> patternNodes = new ArrayList<PatternNode>();
	
	public ArrayList<PatternGraph> getSubPatternGraphs() {
		return subPatternGraphs;
	}
	public PatternGraph setSubPatternGraphs(ArrayList<PatternGraph> subPatternGraphs) {
		this.subPatternGraphs = subPatternGraphs;
		return this;
	}
	public PatternGraph addSubPatternGraph(PatternGraph subPatternGraph) {
		this.subPatternGraphs.add(subPatternGraph);
		return this;
	}
	public ArrayList<PatternNode> getPatternNodes() {
		return patternNodes;
	}
	public PatternGraph setPatternNodes(ArrayList<PatternNode> patternNodes) {
		this.patternNodes = patternNodes;
		return this;
	}
	public PatternGraph addPatternNode(PatternNode patternNode) {
		this.patternNodes.add(patternNode);
		return this;
	}
	@Override
	public PatternGraph setNegative(boolean negative) {
		super.setNegative(negative);
		return this;
	}

}
