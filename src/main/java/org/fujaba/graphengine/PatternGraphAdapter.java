package org.fujaba.graphengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.PatternNode;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class PatternGraphAdapter extends TypeAdapter<PatternGraph> {

	@Override
	public void write(JsonWriter out, PatternGraph graph) throws IOException {
		writeWithManager(out, graph, new IdManager());
	}
	public void writeWithManager(JsonWriter out, PatternGraph graph, IdManager idManager) throws IOException {
		out.beginObject();
	    out.name("negative").value(graph.isNegative());
		out.name("nodes");
		out.beginArray();
		for (PatternNode node: graph.getPatternNodes()) {
			// the part for the node begins
		    out.beginObject();
		    out.name("negative").value(node.isNegative());
	    	out.name("action").value(node.getAction());
		    out.name("id").value(idManager.getId(node));
		    out.name("attributes");
		    out.beginArray();
		    for (PatternAttribute attribute: node.getPatternAttributes()) {
			    out.beginObject();
		    	out.name("negative").value(attribute.isNegative());
		    	out.name("action").value(attribute.getAction());
		    	out.name("name").value(attribute.getName());
		    	out.name("value").value(attribute.getValue());
		    	out.endObject();
		    }
		    out.endArray();
		    out.name("edges");
		    out.beginArray();
		    for (PatternEdge edge: node.getPatternEdges()) {
		    	out.beginObject();
		    	out.name("negative").value(edge.isNegative());
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
		out.name("subGraphs");
		out.beginArray();
		for (PatternGraph subGraph: graph.getSubPatternGraphs()) {
			writeWithManager(out, subGraph, idManager); // recursively write sub-graphs
		}
		out.endArray();
		out.endObject();
	}

	@Override
	public PatternGraph read(JsonReader in) throws IOException {
		return readWithManager(in, new IdManager());
	}
	public PatternGraph readWithManager(JsonReader in, IdManager idManager) throws IOException {
		HashMap<Long, HashMap<String, ArrayList<Long>>> edgesToBuild = new HashMap<Long, HashMap<String, ArrayList<Long>>>();
		HashMap<Long, HashMap<String, ArrayList<Boolean>>> edgesToBuildNegative = new HashMap<Long, HashMap<String, ArrayList<Boolean>>>();
		HashMap<Long, HashMap<String, ArrayList<String>>> edgesToBuildAction = new HashMap<Long, HashMap<String, ArrayList<String>>>();
	    final PatternGraph graph = new PatternGraph();
	    in.beginObject();
	    while (in.hasNext()) {
	    	switch (in.nextName()) {
	    	case "negative":
	    		graph.setNegative(in.nextBoolean());
	    		break;
	    	case "nodes":
	    		in.beginArray();
	    	    while (in.hasNext()) {
	    			// the part for the node begins
	    		    PatternNode node = new PatternNode();
	    	    	in.beginObject();
		    	    while (in.hasNext()) {
		    	    	switch (in.nextName()) {
		    	    	case "negative":
		    	    		node.setNegative(in.nextBoolean());
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
					    	    	case "negative":
					    	    		attribute.setNegative(in.nextBoolean());
					    	    		break;
					    	    	case "action":
					    	    		attribute.setAction(in.nextString());
					    	    		break;
					    	    	case "name":
					    	    		attribute.setName(in.nextString());
					    	    		break;
					    	    	case "value":
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
		    	    		edgesToBuildNegative.put(sourceId, new HashMap<String, ArrayList<Boolean>>());
		    	    		edgesToBuildAction.put(sourceId, new HashMap<String, ArrayList<String>>());
				    	    while (in.hasNext()) {
				    	    	in.beginObject();
				    	    	PatternEdge edge = new PatternEdge().setSource(node);
					    	    while (in.hasNext()) {
					    	    	switch (in.nextName()) {
					    	    	case "negative":
					    	    		edge.setNegative(in.nextBoolean());
					    	    		break;
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
					    	    	edgesToBuildNegative.get(sourceId).put(edge.getName(), new ArrayList<Boolean>());
					    	    	edgesToBuildAction.get(sourceId).put(edge.getName(), new ArrayList<String>());
					    	    }
					    	    edgesToBuild.get(sourceId).get(edge.getName()).add(idManager.getId(edge.getTarget()));
					    	    edgesToBuildNegative.get(sourceId).get(edge.getName()).add(edge.isNegative());
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
	    	case "subGraphs":
	    		in.beginArray();
	    	    while (in.hasNext()) {
	    	    	graph.addSubPatternGraph(readWithManager(in, idManager)); // recursively read the sub-graphs
	    	    }
	    		in.endArray();
	    		break;
	    	}
	    }
	    for (Long sourceKey: edgesToBuild.keySet()) {
	    	for (String edgeName: edgesToBuild.get(sourceKey).keySet()) {
		    	for (int i = 0; i < edgesToBuild.get(sourceKey).get(edgeName).size(); ++i) {
	    			Long targetKey = edgesToBuild.get(sourceKey).get(edgeName).get(i);
	    			boolean negative = edgesToBuildNegative.get(sourceKey).get(edgeName).get(i);
	    			String action = edgesToBuildAction.get(sourceKey).get(edgeName).get(i);
	    			((PatternNode)idManager.getObject(sourceKey)).addPatternEdge(
	    				new PatternEdge()
	    					.setNegative(negative)
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
