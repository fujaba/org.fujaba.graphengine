package org.fujaba.graphengine.graph.adapter;

import java.io.IOException;

import org.fujaba.graphengine.IdManager;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The GraphToSigmaJsAdapter is a gson TypeAdapter used to serialize Graphs to JSON, meant for use in sigma.js visualization.
 * 
 * @author Philipp Kolodziej
 */
public class GraphToSigmaJsAdapter extends TypeAdapter<Graph> {

	private String getNodeLabel(Node node) {
		return node.getAttributes().toString();
	}

	private double getX(int index, int total) {
		return -Math.sin((double)(index + 0.5) / (double)total * 2 * Math.PI) / 10;
	}
	private double getY(int index, int total) {
		return -Math.cos((double)(index + 0.5) / (double)total * 2 * Math.PI) / 10;
	}
	
	@Override
	public void write(JsonWriter out, Graph graph) throws IOException {
		IdManager idManager = new IdManager();
		out.beginObject();
		out.name("nodes");
		out.beginArray();
		for (int i = 0; i < graph.getNodes().size(); ++i) {
			Node node = graph.getNodes().get(i);
			out.beginObject();
			out.name("id").value(idManager.getId(node));
			out.name("label").value(getNodeLabel(node));
			out.name("x").value(getX(i, graph.getNodes().size()));
			out.name("y").value(getY(i, graph.getNodes().size()));
			out.name("size").value(10);
			if (i == 0) {
				out.name("color").value("#00F");
			} else {
				if (node.getEdges().keySet().size() == 0) {
					out.name("color").value("#F00");
				} else {
					out.name("color").value("#000");
				}
			}
			out.endObject();
		}
		out.endArray();
		out.name("edges");
		out.beginArray();
		long count = graph.getNodes().size();
		for (Node node: graph.getNodes()) {
			for (String key: node.getEdges().keySet()) {
				for (Node targetNode: node.getEdges(key)) {
					out.beginObject();
					out.name("id").value(count);
					out.name("label").value(key);
					out.name("source").value(idManager.getId(node));
					out.name("target").value(idManager.getId(targetNode));
					out.name("size").value(10);
					out.name("color").value("#888");
					out.endObject();
					++count;
				}
			}
		}
		out.endArray();
		out.endObject();
	}

	@Override
	public Graph read(JsonReader in) throws IOException {
		throw new IOException("This adapter is only supposed to output data.");
	}

}
