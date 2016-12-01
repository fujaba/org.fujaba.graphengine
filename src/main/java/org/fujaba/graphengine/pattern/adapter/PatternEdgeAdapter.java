package org.fujaba.graphengine.pattern.adapter;

import java.io.IOException;

import org.fujaba.graphengine.pattern.PatternEdge;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The PatternEdgeAdapter is a gson TypeAdapter used to serialize and deserialize PatternEdges to and from JSON.
 * Though this Object doesn't make sense to serialize without its parent context - so it is prohibited by this Adapter.
 * 
 * @author Philipp Kolodziej
 */
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
