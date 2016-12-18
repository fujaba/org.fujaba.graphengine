package org.fujaba.graphengine.pattern.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.fujaba.graphengine.IdManager;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The PatternGraphAdapter is a gson TypeAdapter used to serialize and deserialize PatternGraphs to and from JSON.
 * 
 * @author Philipp Kolodziej
 */
public class PatternGraphAdapter extends TypeAdapter<PatternGraph> {

	@Override
	public void write(JsonWriter out, PatternGraph graph) throws IOException {
		writeWithManager(out, graph, new IdManager());
	}
	public void writeWithManager(JsonWriter out, PatternGraph graph, IdManager idManager) throws IOException {
		out.beginObject();
//	    out.name("negative").value(graph.isNegative());
		out.name("nodes");
		out.beginArray();
		for (PatternNode node: graph.getPatternNodes()) {
			// the part for the node begins
		    out.beginObject();
		    out.name("expression").value(node.getAttributeMatchExpression());
	    	out.name("action").value(node.getAction());
		    out.name("id").value(idManager.getId(node));
		    out.name("attributes");
		    out.beginArray();
		    for (PatternAttribute attribute: node.getPatternAttributes()) {
			    out.beginObject();
		    	out.name("action").value(attribute.getAction());
		    	out.name("name").value(attribute.getName());
		    	if (attribute.getValue() instanceof Boolean) {
			    	out.name("value").value((Boolean)attribute.getValue());
		    	} else if (attribute.getValue() instanceof Integer) {
			    	out.name("value").value((Integer)attribute.getValue());
		    	} else if (attribute.getValue() instanceof Long) {
			    	out.name("value").value((Long)attribute.getValue());
		    	} else if (attribute.getValue() instanceof Double) {
			    	out.name("value").value((Double)attribute.getValue());
		    	} else {
			    	out.name("value").value((String)attribute.getValue());
		    	}
		    	out.endObject();
		    }
		    out.endArray();
		    out.name("edges");
		    out.beginArray();
		    for (PatternEdge edge: node.getPatternEdges()) {
		    	out.beginObject();
		    	out.name("action").value(edge.getAction());
		    	out.name("name").value(edge.getName());
		    	out.name("target").value(idManager.getId(edge.getTarget()));
	    		out.endObject();
		    }
		    out.endArray();
		    out.endObject();
			// the part for the node ends
		}
		out.endArray();
//		out.name("subGraphs");
//		out.beginArray();
//		for (PatternGraph subGraph: graph.getSubPatternGraphs()) {
//			writeWithManager(out, subGraph, idManager); // recursively write sub-graphs
//		}
//		out.endArray();
		out.endObject();
	}

	@Override
	public PatternGraph read(JsonReader in) throws IOException {
		return readWithManager(in, new IdManager());
	}
	public PatternGraph readWithManager(JsonReader in, IdManager idManager) throws IOException {
		HashMap<Long, HashMap<String, ArrayList<Long>>> edgesToBuild = new HashMap<Long, HashMap<String, ArrayList<Long>>>();
		HashMap<Long, HashMap<String, ArrayList<String>>> edgesToBuildAction = new HashMap<Long, HashMap<String, ArrayList<String>>>();
	    final PatternGraph graph = new PatternGraph();
	    in.beginObject();
	    while (in.hasNext()) {
	    	switch (in.nextName()) {
//	    	case "negative":
//	    		graph.setNegative(in.nextBoolean());
//	    		break;
	    	case "nodes":
	    		in.beginArray();
	    	    while (in.hasNext()) {
	    			// the part for the node begins
	    		    PatternNode node = new PatternNode();
	    	    	in.beginObject();
		    	    while (in.hasNext()) {
		    	    	switch (in.nextName()) {
		    	    	case "expression":
		    	    		node.setAttributeMatchExpression(in.nextString());
		    	    		break;
		    	    	case "action":
		    	    		node.setAction(in.nextString());
		    	    		break;
		    	    	case "id":
		    	    		idManager.tellId(in.nextLong(), node);
		    	    		break;
		    	    	case "attributes":
		    	    		in.beginArray();
				    	    while (in.hasNext()) {
			    	    		PatternAttribute attribute = new PatternAttribute();
			    	    		in.beginObject();
					    	    while (in.hasNext()) {
					    	    	switch (in.nextName()) {
					    	    	case "action":
					    	    		attribute.setAction(in.nextString());
					    	    		break;
					    	    	case "name":
					    	    		attribute.setName(in.nextString());
					    	    		break;
					    	    	case "value":
					    	    		try {
						    	    		attribute.setValue(in.nextBoolean());
					    	    		} catch (IOException wrongTypeBoolean) {
					    	    			try {
							    	    		attribute.setValue(in.nextInt());
						    	    		} catch (IOException wrongTypeInteger) {
						    	    			try {
								    	    		attribute.setValue(in.nextLong());
							    	    		} catch (IOException wrongTypeLong) {
							    	    			try {
									    	    		attribute.setValue(in.nextDouble());
								    	    		} catch (IOException wrongTypeDouble) {
								    	    			try {
										    	    		attribute.setValue(in.nextString());
									    	    		} catch (IOException wrongTypeString) {
									    	    			wrongTypeString.printStackTrace();
									    	    		}
								    	    		}
							    	    		}
						    	    		}
					    	    		}
					    	    		attribute.setValue(in.nextString());
					    	    		break;
					    	    	}
					    	    }
			    	    		in.endObject();
					    	    node.addPatternAttribute(attribute);
				    	    }
		    	    		in.endArray();
		    	    		break;
		    	    	case "edges":
		    	    		in.beginArray();
		    	    		Long sourceId = idManager.getId(node);
		    	    		edgesToBuild.put(sourceId, new HashMap<String, ArrayList<Long>>());
		    	    		edgesToBuildAction.put(sourceId, new HashMap<String, ArrayList<String>>());
				    	    while (in.hasNext()) {
				    	    	in.beginObject();
				    	    	PatternEdge edge = new PatternEdge().setSource(node);
					    	    while (in.hasNext()) {
					    	    	switch (in.nextName()) {
					    	    	case "action":
					    	    		edge.setAction(in.nextString());
					    	    		break;
					    	    	case "name":
					    	    		edge.setName(in.nextString());
					    	    		break;
					    	    	case "target":
					    	    		edge.setTarget((PatternNode)idManager.getObject(in.nextLong()));
					    	    		break;
					    	    	}
					    	    }
					    	    if (!edgesToBuild.get(sourceId).containsKey(edge.getName())) {
					    	    	edgesToBuild.get(sourceId).put(edge.getName(), new ArrayList<Long>());
					    	    	edgesToBuildAction.get(sourceId).put(edge.getName(), new ArrayList<String>());
					    	    }
					    	    edgesToBuild.get(sourceId).get(edge.getName()).add(idManager.getId(edge.getTarget()));
					    	    edgesToBuildAction.get(sourceId).get(edge.getName()).add(edge.getAction());
				    	    	in.endObject();
				    	    }
		    	    		in.endArray();
		    	    		break;
		    	    	}
			    	}
	    	    	in.endObject();
	    	    	graph.addPatternNode(node);
	    			// the part for the node ends
		    	}
	    		in.endArray();
	    		break;
//	    	case "subGraphs":
//	    		in.beginArray();
//	    	    while (in.hasNext()) {
//	    	    	graph.addSubPatternGraph(readWithManager(in, idManager)); // recursively read the sub-graphs
//	    	    }
//	    		in.endArray();
//	    		break;
	    	}
	    }
	    for (Long sourceKey: edgesToBuild.keySet()) {
	    	for (String edgeName: edgesToBuild.get(sourceKey).keySet()) {
		    	for (int i = 0; i < edgesToBuild.get(sourceKey).get(edgeName).size(); ++i) {
	    			Long targetKey = edgesToBuild.get(sourceKey).get(edgeName).get(i);
	    			String action = edgesToBuildAction.get(sourceKey).get(edgeName).get(i);
	    			((PatternNode)idManager.getObject(sourceKey)).addPatternEdge(
	    				new PatternEdge()
	    					.setAction(action)
	    					.setSource((PatternNode)idManager.getObject(sourceKey))
	    					.setName(edgeName)
	    					.setTarget((PatternNode)idManager.getObject(targetKey))
	    			);
	    		}
	    	}
	    }
	    in.endObject();
	    return graph;
	}

}
