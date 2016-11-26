package org.fujaba.graphengine.pattern;

public class PatternEdge extends PatternElement {

	/**
	 * the name of this PatternEdge
	 */
	private String name;
	/**
	 * the source of this PatternEdge
	 */
	private PatternNode source;
	/**
	 * the target of this PatternEdge
	 */
	private PatternNode target;
	
	public String getName() {
		return name;
	}
	public PatternEdge setName(String name) {
		this.name = name;
		return this;
	}
	public PatternNode getSource() {
		return source;
	}
	public PatternEdge setSource(PatternNode source) {
		this.source = source;
		return this;
	}
	public PatternNode getTarget() {
		return target;
	}
	public PatternEdge setTarget(PatternNode target) {
		this.target = target;
		return this;
	}
	@Override
	public PatternEdge setNegative(boolean negative) {
		super.setNegative(negative);
		return this;
	}
	@Override
	public PatternEdge setAction(String action) {
		super.setAction(action);
		return this;
	}

}
