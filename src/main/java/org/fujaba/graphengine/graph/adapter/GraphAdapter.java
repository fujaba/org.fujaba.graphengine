package org.fujaba.graphengine.graph.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.fujaba.graphengine.IdManager;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The GraphAdapter is a gson TypeAdapter used to serialize and deserialize Graphs to and from JSON.
 * 
 * @author Philipp Kolodziej
 */
public class GraphAdapter extends TypeAdapter<Graph> {

	@Override
	public void write(JsonWriter out, Graph graph) throws IOException {
		IdManager idManager = new IdManager();
		out.beginObject();
		out.name("nodes");
		out.beginArray();
		for (Node node: graph.getNodes()) {
			// the part for the node begins
		    out.beginObject();
		    out.name("id").value(idManager.getId(node));
		    out.name("attributes");
		    out.beginObject();
		    for (String key: node.getAttributes().keySet()) {
		    	Object value = node.getAttribute(key);
		    	if (value instanceof Integer) {
			    	out.name(key).value((Integer)node.getAttribute(key));
		    	} else if (value instanceof Long) {
			    	out.name(key).value((Long)node.getAttribute(key));
		    	} else if (value instanceof Double) {
			    	out.name(key).value((Double)node.getAttribute(key));
		    	} else if (value instanceof Boolean) {
			    	out.name(key).value((Boolean)node.getAttribute(key));
		    	} else if (value instanceof String) {
			    	out.name(key).value((String)node.getAttribute(key));
//		    	} else if (value == null) {
//			    	out.name(key).value((Integer)null);
		    	} else {
		    		throw new IOException("invalid type of attribute value for key " + key + " in Node " + node + ": " + value);
		    	}
		    }
		    out.endObject();
		    out.name("edges");
		    out.beginArray();
		    for (String key: node.getEdges().keySet()) {
		    	out.beginObject();
		    	out.name(key);
	    		out.beginArray();
		    	for (Node targetNode: node.getEdges(key)) {
		    		out.value(idManager.getId(targetNode));
		    	}
	    		out.endArray();
	    		out.endObject();
		    }
		    out.endArray();
		    out.endObject();
			// the part for the node ends
		}
		out.endArray();
		out.endObject();
	}

	@Override
	public Graph read(JsonReader in) throws IOException {
		IdManager idManager = new IdManager();
		HashMap<Long, HashMap<String, ArrayList<Long>>> edgesToBuild = new HashMap<Long, HashMap<String, ArrayList<Long>>>();
	    final Graph graph = new Graph();
	    in.beginObject();
	    while (in.hasNext()) {
	    	switch (in.nextName()) {
	    	case "nodes":
	    		in.beginArray();
	    	    while (in.hasNext()) {
	    			// the part for the node begins
	    		    Node node = new Node();
	    	    	in.beginObject();
		    	    while (in.hasNext()) {
		    	    	switch (in.nextName()) {
		    	    	case "id":
		    	    		idManager.tellId(in.nextLong(), node);
		    	    		break;
		    	    	case "attributes":
		    	    		in.beginObject();
				    	    while (in.hasNext()) {
				    	    	String nextName = in.nextName();
				    	    	try {
					    	    	node.setAttribute(nextName, in.nextInt());
				    	    	} catch (Throwable t1) {
				    	    		try {
						    	    	node.setAttribute(nextName, in.nextLong());
				    	    		} catch (Throwable t2) {
				    	    			try {
							    	    	node.setAttribute(nextName, in.nextDouble());
					    	    		} catch (Throwable t3) {
					    	    			try {
								    	    	node.setAttribute(nextName, in.nextBoolean());
						    	    		} catch (Throwable t4) {
						    	    			try {
									    	    	node.setAttribute(nextName, in.nextString());
							    	    		} catch (Throwable t5) {
							    	    			throw t5;
							    	    		}
						    	    		}
					    	    		}
				    	    		}
				    	    	}
				    	    }
		    	    		in.endObject();
		    	    		break;
		    	    	case "edges":
		    	    		in.beginArray();
		    	    		Long sourceId = idManager.getId(node);
		    	    		edgesToBuild.put(sourceId, new HashMap<String, ArrayList<Long>>());
				    	    while (in.hasNext()) {
				    	    	in.beginObject();
				    	    	String edgeName = in.nextName();
				    	    	in.beginArray();
				    	    	edgesToBuild.get(sourceId).put(edgeName, new ArrayList<Long>());
					    	    while (in.hasNext()) {
					    	    	Long targetId = in.nextLong();
					    	    	edgesToBuild.get(sourceId).get(edgeName).add(targetId);
					    	    }
				    	    	in.endArray();
				    	    	in.endObject();
				    	    }
		    	    		in.endArray();
		    	    		break;
		    	    	}
			    	}
	    	    	in.endObject();
	    	    	graph.addNode(node);
	    			// the part for the node ends
		    	}
	    		in.endArray();
	    		break;
	    	}
	    }
	    for (Long sourceKey: edgesToBuild.keySet()) {
	    	for (String edgeName: edgesToBuild.get(sourceKey).keySet()) {
	    		for (Long targetKey: edgesToBuild.get(sourceKey).get(edgeName)) {
	    			((Node)idManager.getObject(sourceKey)).addEdge(
	    				edgeName,
	    				((Node)idManager.getObject(targetKey))
	    			);
	    		}
	    	}
	    }
	    in.endObject();
	    return graph;
	}

}
