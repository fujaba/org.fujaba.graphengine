package org.fujaba.graphengine.pattern;

/**
 * This abstract class is the superclass for all content classes of the PatternGraph:
 * PatternNode, PatternEdge and PatternAttribute.
 * 
 * @author Philipp Kolodziej
 */
public abstract class PatternElement {

	/**
	 * whether this Pattern is negative or not
	 */
	private boolean negative;
	/**
	 * an action to apply to this PatternElement (currently "match", "remove" or "create")
	 */
	private String action;

	public String getAction() {
		return action;
	}
	public PatternElement setAction(String action) {
		this.action = action;
		return this;
	}
	public boolean isNegative() {
		return negative;
	}
	public PatternElement setNegative(boolean negative) {
		this.negative = negative;
		return this;
	}
	
}
