package org.fujaba.graphengine.algorithm.adapter;

import java.io.IOException;

import org.fujaba.graphengine.GraphEngine;
import org.fujaba.graphengine.algorithm.Algorithm;
import org.fujaba.graphengine.pattern.PatternGraph;
import org.fujaba.graphengine.pattern.adapter.PatternGraphAdapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class AlgorithmAdapter extends TypeAdapter<Algorithm> {

	@Override
	public void write(JsonWriter out, Algorithm algorithm) throws IOException {
		PatternGraphAdapter patternGraphAdapter = new PatternGraphAdapter();
		out.beginObject();
		out.name("name").value(algorithm.getName());
		if (algorithm.isRepeating()) {
			out.name("repeating").value(algorithm.isRepeating());
		}
		if (algorithm.getAtomicAlgorithm() != null) {
//			out.name("atomic").value(GraphEngine.getGson().toJson(algorithm.getAtomicAlgorithm()));
			out.name("atomic");
			patternGraphAdapter.write(out, algorithm.getAtomicAlgorithm());
		}
		if (algorithm.getAlgorithmSteps().size() > 0) {
			out.name("steps");
			out.beginArray();
			for (Algorithm subAlgorithm: algorithm.getAlgorithmSteps()) {
				write(out, subAlgorithm);
			}
			out.endArray();
		}
		out.endObject();
	}

	@Override
	public Algorithm read(JsonReader in) throws IOException {
		PatternGraphAdapter patternGraphAdapter = new PatternGraphAdapter();
		final Algorithm algorithm = new Algorithm("");
		in.beginObject();
		while (in.hasNext()) {
			switch (in.nextName()) {
			case "name":
				algorithm.setName(in.nextString());
				break;
			case "repeating":
				algorithm.setRepeating(in.nextBoolean());
				break;
			case "atomic":
//				algorithm.setAtomicAlgorithm(GraphEngine.getGson().fromJson(in.nextString(), PatternGraph.class));
				algorithm.setAtomicAlgorithm(patternGraphAdapter.read(in));
				break;
			case "steps":
				in.beginArray();
				while (in.hasNext()) {
					algorithm.addAlgorithmStep(read(in));
				}
				in.endArray();
				break;
			}
		}
		in.endObject();
		return algorithm;
	}

}
