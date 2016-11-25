package org.fujaba.graphengine.pattern;

/**
 * A pattern for a single edge, for matching against, creation or removal of an edge.
 * 
 * @author Philipp Kolodziej
 */
public class PatternEdge extends org.fujaba.graphengine.pattern.Pattern {
	
	/**
	 * the name of the edge
	 */
	private String name;
	/**
	 * the PatternNode that is source of this PatternEdge
	 */
	private PatternNode source;
	/**
	 * the PatternNode that is target of this PatternEdge
	 */
	private PatternNode target;

	protected PatternEdge(PatternNode source, PatternNode target, String name, String action) {
		this.setSource(source);
		this.setTarget(target);
		this.setName(name);
		this.setAction(action);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public PatternNode getSource() {
		return source;
	}
	public void setSource(PatternNode source) {
		this.source = source;
	}
	public PatternNode getTarget() {
		return target;
	}
	public void setTarget(PatternNode target) {
		this.target = target;
	}
	
	@Override
	public String toString() {
		String result = "{";
		result += "\"action\":\"" + this.getAction() + "\", ";
		result += "\"name\":\"" + this.getName() + "\", ";
		result += "\"target\":\"" + this.getTarget().getId() + "\"";
		return result + "}";
	}
	
}
