package org.fujaba.graphengine.pattern;

import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PatternRules {

	private HashSet<PatternGraph> rules = new HashSet<PatternGraph>();

	public PatternRules(String json) {
		JsonObject graphObject = new JsonParser().parse(json).getAsJsonObject();
		for (JsonElement graphElement: graphObject.getAsJsonArray("rules")) {
			HashMap<String, PatternNode> nodes = new HashMap<String, PatternNode>();
			HashMap<String, HashMap<String, String>> edgeNames = new HashMap<String, HashMap<String, String>>();
			HashMap<String, HashMap<String, String>> edgeActions = new HashMap<String, HashMap<String, String>>();
			for (JsonElement nodeElement: graphElement.getAsJsonObject().getAsJsonArray("nodes")) {
				String nodeAction = nodeElement.getAsJsonObject().get("action").getAsString();
				String nodeId = nodeElement.getAsJsonObject().get("id").getAsString();
				String nodeType = nodeElement.getAsJsonObject().get("type").getAsString();
				PatternNode node = new PatternNode(nodeId, nodeType, nodeAction);
				for (JsonElement attributeElement: nodeElement.getAsJsonObject().getAsJsonArray("attributes")) {
					String attributeAction = attributeElement.getAsJsonObject().get("action").getAsString();
					String attributeName = attributeElement.getAsJsonObject().get("name").getAsString();
					String attributeValue = attributeElement.getAsJsonObject().get("value").getAsString();
					boolean attributeIsNegative = attributeElement.getAsJsonObject().get("negative").getAsBoolean();
					String attributeExpression = attributeElement.getAsJsonObject().get("expression").getAsString();
					node.addAttribute(new PatternAttribute(node, attributeName, attributeValue, attributeIsNegative, attributeExpression, attributeAction));
				}
				edgeNames.put(nodeId, new HashMap<String, String>());
				edgeActions.put(nodeId, new HashMap<String, String>());
				for (JsonElement edgeElement: nodeElement.getAsJsonObject().getAsJsonArray("edges")) {
					String edgeAction = edgeElement.getAsJsonObject().get("action").getAsString();
					String edgeName = edgeElement.getAsJsonObject().get("name").getAsString();
					String edgeTarget = edgeElement.getAsJsonObject().get("target").getAsString();
					edgeNames.get(nodeId).put(edgeTarget, edgeName);
					edgeActions.get(nodeId).put(edgeTarget, edgeAction);
				}
				nodes.put(nodeId, node);
			}
			for (String edgeSource: edgeNames.keySet()) {
				for (String edgeTarget: edgeNames.get(edgeSource).keySet()) {
					PatternNode source = nodes.get(edgeSource);
					PatternNode target = nodes.get(edgeTarget);
					String edgeName = edgeNames.get(edgeSource).get(edgeTarget);
					String edgeAction = edgeActions.get(edgeSource).get(edgeTarget);
					source.addOutgoingEdge(new PatternEdge(source, target, edgeName, edgeAction));
				}
			}
			rules.add(new PatternGraph(nodes));
		}
	}
	
	public HashSet<PatternGraph> getRules() {
		return rules;
	}

	public void setRules(HashSet<PatternGraph> rules) {
		this.rules = rules;
	}
	
	@Override
	public String toString() {
		String result = "{";
		result += "\"rules\":" + this.getRules();
		return result + "}";
	}
	
}
