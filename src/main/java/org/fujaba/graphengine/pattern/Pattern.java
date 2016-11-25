package org.fujaba.graphengine.pattern;

/**
 * This class is superclass to all Pattern subclasses, like PatternNode, PatternEdge, PatternAttribute.
 * 
 * Patterns can be part of a match condition or things to remove or create on a match.
 * 
 * @author Philipp Kolodziej
 */
public abstract class Pattern {

	/**
	 * the action for this action: like "match", "create", "remove"
	 */
	private String action;

	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	
}
