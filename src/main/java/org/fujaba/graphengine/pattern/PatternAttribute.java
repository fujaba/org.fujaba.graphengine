package org.fujaba.graphengine.pattern;

public class PatternAttribute extends PatternElement {
	
	/**
	 * the name of this PatternAttribute
	 */
	private String name;
	/**
	 * the value of this PatternAttribute - it can be an expression or a value depending on context
	 */
	private String value;
	
	public String getName() {
		return name;
	}
	public PatternAttribute setName(String name) {
		this.name = name;
		return this;
	}
	public String getValue() {
		return value;
	}
	public PatternAttribute setValue(String value) {
		this.value = value;
		return this;
	}
	@Override
	public PatternAttribute setNegative(boolean negative) {
		super.setNegative(negative);
		return this;
	}
	@Override
	public PatternAttribute setAction(String action) {
		super.setAction(action);
		return this;
	}

}
