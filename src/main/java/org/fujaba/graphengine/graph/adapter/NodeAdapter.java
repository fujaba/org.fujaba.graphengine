package org.fujaba.graphengine.graph.adapter;

import java.io.IOException;

import org.fujaba.graphengine.graph.Node;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The NodeAdapter is a gson TypeAdapter used to serialize and deserialize Nodes to and from JSON.
 * Though this Object doesn't make sense to serialize without its parent context - so it is prohibited by this Adapter.
 * 
 * @author Philipp Kolodziej
 */
public class NodeAdapter extends TypeAdapter<Node> {

	@Override
	public void write(JsonWriter out, Node node) throws IOException {
		throw new IOException("single Nodes cannot be written. try writing the whole Graph!");
	}

	@Override
	public Node read(JsonReader in) throws IOException {
		throw new IOException("single Nodes cannot be read. try reading the whole Graph!");
	}

}
