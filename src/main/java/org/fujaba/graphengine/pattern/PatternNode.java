package org.fujaba.graphengine.pattern;

import java.util.HashSet;

/**
 * A pattern for a single node, for matching against, creation or removal of a node.
 * 
 * @author Philipp Kolodziej
 */
public class PatternNode extends org.fujaba.graphengine.pattern.Pattern {

	/**
	 * the id of this PatternNode
	 */
	private String id;
	/**
	 * the type of this PatternNode
	 */
	private String type;
	/**
	 * the outgoing PatternEdges of this PatternNode
	 */
	private HashSet<PatternEdge> outgoingEdges = new HashSet<PatternEdge>();
	/**
	 * the ingoing PatternEdges of this PatternNode
	 */
	private HashSet<PatternEdge> ingoingEdges = new HashSet<PatternEdge>();
	/**
	 * the PatternAttributes of this PatternNode
	 */
	private HashSet<PatternAttribute> attributes = new HashSet<PatternAttribute>();
	
	protected PatternNode(String id, String type, String action) {
		this.setId(id);
		this.setType(type);
		this.setAction(action);
	}

	public String getId() {
		return this.id;
	}
	private void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return this.type;
	}
	private void setType(String type) {
		this.type = type;
	}
	public HashSet<PatternEdge> getOutgoingEdges() {
		return this.outgoingEdges;
	}
//	private void setOutgoingEdges(HashSet<PatternEdge> outgoingEdges) {
//		this.outgoingEdges = outgoingEdges;
//	}
	protected void addOutgoingEdge(PatternEdge edge) {
		if (this.outgoingEdges == null) {
			this.outgoingEdges = new HashSet<PatternEdge>();
		}
		this.outgoingEdges.add(edge);
		if (edge != null && edge.getTarget() != null) {
			edge.getTarget().addIngoingEdge(edge);
		}
	}
	public HashSet<PatternEdge> getIngoingEdges() {
		return this.ingoingEdges;
	}
//	private void setIngoingEdges(HashSet<PatternEdge> ingoingEdges) {
//		this.ingoingEdges = ingoingEdges;
//	}
	private void addIngoingEdge(PatternEdge edge) {
		if (this.ingoingEdges == null) {
			this.ingoingEdges = new HashSet<PatternEdge>();
		}
		this.ingoingEdges.add(edge);
	}
	public HashSet<PatternAttribute> getAttributes() {
		return this.attributes;
	}
//	private void setAttributes(HashSet<PatternAttribute> attributes) {
//		this.attributes = attributes;
//	}
	protected void addAttribute(PatternAttribute attribute) {
		if (this.attributes == null) {
			this.attributes = new HashSet<PatternAttribute>();
		}
		this.attributes.add(attribute);
	}
	
	@Override
	public String toString() {
		String result = "{";
		result += "\"action\":\"" + this.getAction() + "\", ";
		result += "\"id\":\"" + this.getId() + "\", ";
		result += "\"type\":\"" + this.getType() + "\", ";
		result += "\"attributes\":" + this.getAttributes() + ", ";
		result += "\"edges\":" + this.getOutgoingEdges() + "";
		return result + "}";
	}
	
}
