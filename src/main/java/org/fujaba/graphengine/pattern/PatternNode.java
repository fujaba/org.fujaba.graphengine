package org.fujaba.graphengine.pattern;

import java.util.ArrayList;

public class PatternNode extends PatternElement {
	
	/**
	 * the PatternAttributes of this PatternNode
	 */
	private ArrayList<PatternAttribute> patternAttributes = new ArrayList<PatternAttribute>();
	/**
	 * the outgoing PatternEdges of this PatternNode
	 */
	private ArrayList<PatternEdge> patternEdges = new ArrayList<PatternEdge>();

	public ArrayList<PatternAttribute> getPatternAttributes() {
		return patternAttributes;
	}
	public PatternAttribute getPatternAttribute(String name) {
		for (int i = 0; i < patternAttributes.size(); ++i) {
			if (patternAttributes.get(i).getName() == name) {
				return patternAttributes.get(i);
			}
		}
		return null;
	}
	public PatternNode setPatternAttributes(ArrayList<PatternAttribute> patternAttributes) {
		this.patternAttributes = patternAttributes;
		return this;
	}
	public PatternNode setPatternAttribute(String name, String value) {
		for (int i = 0; i < patternAttributes.size(); ++i) {
			if (patternAttributes.get(i).getName() == name) {
				patternAttributes.get(i).setValue(value);
				return this;
			}
		}
		patternAttributes.add(new PatternAttribute().setName(name).setValue(value));
		return this;
	}
	public PatternNode addPatternAttribute(PatternAttribute patternAttribute) {
		this.patternAttributes.add(patternAttribute);
		return this;
	}
	public PatternNode removePatternAttribute(String name) {
		for (int i = 0; i < patternAttributes.size(); ++i) {
			if (patternAttributes.get(i).getName() == name) {
				patternAttributes.remove(i);
				return this;
			}
		}
		return this;
	}
	public ArrayList<PatternEdge> getPatternEdges() {
		return patternEdges;
	}
	public PatternNode setPatternEdges(ArrayList<PatternEdge> patternEdges) {
		this.patternEdges = patternEdges;
		return this;
	}
	public PatternNode addPatternEdge(PatternEdge patternEdge) {
		this.patternEdges.add(patternEdge);
		return this;
	}
	@Override
	public PatternNode setNegative(boolean negative) {
		super.setNegative(negative);
		return this;
	}
	@Override
	public PatternNode setAction(String action) {
		super.setAction(action);
		return this;
	}

}
