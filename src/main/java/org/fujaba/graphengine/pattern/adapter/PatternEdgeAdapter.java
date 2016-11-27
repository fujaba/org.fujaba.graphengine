package org.fujaba.graphengine.pattern.adapter;

import java.io.IOException;

import org.fujaba.graphengine.pattern.PatternEdge;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class PatternEdgeAdapter extends TypeAdapter<PatternEdge> {

	@Override
	public void write(JsonWriter out, PatternEdge edge) throws IOException {
		throw new IOException("single PatternEdges cannot be written. try writing the whole PatternGraph!");
	}

	@Override
	public PatternEdge read(JsonReader in) throws IOException {
		throw new IOException("single PatternEdges cannot be read. try reading the whole PatternGraph!");
	}

}
