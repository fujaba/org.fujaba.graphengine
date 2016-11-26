package org.fujaba.graphengine;

import java.io.IOException;

import org.fujaba.graphengine.pattern.PatternNode;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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
