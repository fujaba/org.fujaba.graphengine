package org.fujaba.graphengine.pattern.adapter;

import java.io.IOException;

import org.fujaba.graphengine.pattern.PatternNode;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The PatternNodeAdapter is a gson TypeAdapter used to serialize and deserialize PatternNodes to and from JSON.
 * Though this Object doesn't make sense to serialize without its parent context - so it is prohibited by this Adapter.
 * 
 * @author Philipp Kolodziej
 */
public class PatternNodeAdapter extends TypeAdapter<PatternNode> {

	@Override
	public void write(JsonWriter out, PatternNode node) throws IOException {
		throw new IOException("single PatternNodes cannot be written. try writing the whole PatternGraph!");
	}

	@Override
	public PatternNode read(JsonReader in) throws IOException {
		throw new IOException("single PatternNodes cannot be read. try reading the whole PatternGraph!");
	}

}
