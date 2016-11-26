package org.fujaba.graphengine.pattern;

public abstract class Pattern {

	/**
	 * whether this Pattern is negative or not
	 */
	private boolean negative;

	public boolean isNegative() {
		return negative;
	}
	public Pattern setNegative(boolean negative) {
		this.negative = negative;
		return this;
	}
	
}
