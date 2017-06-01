package org.fujaba.graphengine.pattern;

import java.util.ArrayList;

/**
 * The PatternNode is a node of the PatternGraph.
 * 
 * @author Philipp Kolodziej
 */
public class PatternNode extends PatternElement {
	
	/**
	 * a jeval expression to match attributes
	 */
	private String attributeMatchExpression;
	/**
	 * the PatternAttributes of this PatternNode
	 */
	private ArrayList<PatternAttribute> patternAttributes = new ArrayList<PatternAttribute>();
	/**
	 * the outgoing PatternEdges of this PatternNode
	 */
	private ArrayList<PatternEdge> patternEdges = new ArrayList<PatternEdge>();


	/**
	 * public constructor, setting default value of action-attribute to "==" (match).
	 */
	public PatternNode() {
		setAction("==");
	}
	/**
	 * public constructor, setting default value of action-attribute to "==" (match)
	 * and setting the attributeMatchExpression to the specified value.
	 * 
	 * @param attributeMatchExpression the specified attributeMatchExpression for this PatternNode
	 */
	public PatternNode(String attributeMatchExpression) {
		setAttributeMatchExpression(attributeMatchExpression);
		setAction("==");
	}

	public String getAttributeMatchExpression() {
		return attributeMatchExpression;
	}
	public PatternNode setAttributeMatchExpression(String attributeMatchExpression) {
		this.attributeMatchExpression = attributeMatchExpression;
		return this;
	}
	public ArrayList<PatternAttribute> getPatternAttributes() {
		return patternAttributes;
	}
	public PatternAttribute getPatternAttribute(String name) {
		for (int i = 0; i < patternAttributes.size(); ++i) {
			if (patternAttributes.get(i).getName() == name) {
				return patternAttributes.get(i);
			}
		}
		return null;
	}
	public PatternNode setPatternAttributes(ArrayList<PatternAttribute> patternAttributes) {
		this.patternAttributes = patternAttributes;
		return this;
	}
	public PatternNode setPatternAttribute(String name, Object value) {
		return setPatternAttribute("==", name, value);
	}
	public PatternNode setPatternAttribute(String action, String name, Object value) {
		for (int i = 0; i < patternAttributes.size(); ++i) {
			if (patternAttributes.get(i).getName() == name) {
				patternAttributes.get(i).setValue(value).setAction(action);
				return this;
			}
		}
		patternAttributes.add(new PatternAttribute().setName(name).setValue(value).setAction(action));
		return this;
	}
	public PatternNode addPatternAttribute(PatternAttribute... patternAttributes) {
		for (PatternAttribute patternAttribute: patternAttributes) {
			this.patternAttributes.add(patternAttribute);
		}
		return this;
	}
	public PatternNode removePatternAttribute(String... names) {
		for (String name: names) {
			for (int i = 0; i < patternAttributes.size(); ++i) {
				if (patternAttributes.get(i).getName() == name) {
					patternAttributes.remove(i);
					continue;
				}
			}
		}
		return this;
	}


   public ArrayList<PatternEdge> getPatternEdges()
   {
		return patternEdges;
	}


   public PatternNode setPatternEdges(ArrayList<PatternEdge> patternEdges)
   {
		this.patternEdges = patternEdges;
		return this;
	}


   public PatternNode addPatternEdge(PatternEdge... patternEdges)
   {
      for (PatternEdge patternEdge : patternEdges)
      {
			this.patternEdges.add(patternEdge);
		}
		return this;
	}


   public PatternNode addPatternEdge(String name, PatternNode... targets)
   {
		return addPatternEdge("==", name, targets);
	}


   public PatternNode addPatternEdge(String action, String name, PatternNode... targets)
   {
      for (PatternNode target : targets)
      {
			this.patternEdges.add(new PatternEdge().setSource(this).setName(name).setTarget(target).setAction(action));
		}
		return this;
	}


	@Override
   public PatternNode setAction(String action)
   {
		super.setAction(action);
		return this;
	}


   @Override
   public String toString()
   {
      return GraphEngine.getGson().toJson(this);
   }

}
