package org.fujaba.graphengine.pattern;

/**
 * The PatternAttribute is a node of the PatternGraph.
 * 
 * @author Philipp Kolodziej
 */
public class PatternAttribute extends PatternElement {
	
	/**
	 * public constructor, setting default value of action-attribute to "==" (match).
	 */
	public PatternAttribute() {
		setAction("==");
	}
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
	public PatternAttribute setAction(String action) {
		super.setAction(action);
		return this;
	}

}
