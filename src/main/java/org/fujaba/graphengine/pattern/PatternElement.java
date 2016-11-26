package org.fujaba.graphengine.pattern;

public abstract class PatternElement extends Pattern {
	
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

}
