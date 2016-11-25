package org.fujaba.graphengine.graph;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class NodeAdapter extends TypeAdapter<Node> {

	@Override
	public void write(JsonWriter out, Node node) throws IOException {
		throw new IOException("single nodes cannot be written. try writing the whole graph!");
	}

	@Override
	public Node read(JsonReader in) throws IOException {
		throw new IOException("single nodes cannot be read. try reading the whole graph!");
	}

}
