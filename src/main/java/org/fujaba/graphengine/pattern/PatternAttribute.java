package org.fujaba.graphengine.pattern;

/**
 * A Pattern for a single attribute, for matching (also negatively) against, creation of or removal of an attribute.
 * 
 * @author Philipp Kolodziej
 */
public class PatternAttribute extends org.fujaba.graphengine.pattern.Pattern {
	
	/**
	 * the PatternNode of this PatternAttribute
	 */
	private PatternNode node;
	/**
	 * the name of the attribute to be matched against, to be created or removed - depending on context
	 */
	private String name;
	/**
	 * the value of an attribute to be created
	 */
	private String value;
	/**
	 * whether or not this attribute condition should be handled as a negative attribute condition (NAC)
	 */
	private boolean isNegative;
	/**
	 * a regular expression to match the attribute against.
	 */
	private java.util.regex.Pattern expression;
	
	protected PatternAttribute(PatternNode node, String name, String value, boolean isNegative, String expression, String action) {
		this.setNode(node);
		this.setName(name);
		this.setValue(value);
		this.setNegative(isNegative);
		this.setExpression(java.util.regex.Pattern.compile(expression));
		this.setAction(action);
	}

	public PatternNode getNode() {
		return node;
	}
	public void setNode(PatternNode node) {
		this.node = node;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isNegative() {
		return isNegative;
	}
	public void setNegative(boolean isNegative) {
		this.isNegative = isNegative;
	}
	public java.util.regex.Pattern getExpression() {
		return expression;
	}
	public void setExpression(java.util.regex.Pattern expression) {
		this.expression = expression;
	}
	
	@Override
	public String toString() {
		String result = "{";
		result += "\"action\":\"" + this.getAction() + "\", ";
		result += "\"negative\":\"" + this.isNegative() + "\", ";
		result += "\"expression\":\"" + this.getExpression() + "\", ";
		result += "\"name\":\"" + this.getName() + "\", ";
		result += "\"value\":\"" + this.getValue() + "\"";
		return result + "}";
	}

}
