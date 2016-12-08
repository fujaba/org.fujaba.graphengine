package org.fujaba.graphengine.pattern;

/**
 * This abstract class is the superclass for all content classes of the PatternGraph:
 * PatternNode, PatternEdge and PatternAttribute.
 * 
 * @author Philipp Kolodziej
 */
public abstract class PatternElement {

	/**
	 * an action to apply to this PatternElement (currently "==", "!=", "+" or "-")
	 * 
	 * TODO: maybe for convenience i should allow "=", "!", "++" and "--", too...
	 */
	private String action;

	public String getAction() {
		return action;
	}
	public PatternElement setAction(String action) {
		this.action = action;
		return this;
	}
	
}
