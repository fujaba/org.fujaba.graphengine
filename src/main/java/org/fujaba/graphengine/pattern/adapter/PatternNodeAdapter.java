package org.fujaba.graphengine.pattern.adapter;

import java.io.IOException;

import org.fujaba.graphengine.IdManager;
import org.fujaba.graphengine.pattern.PatternAttribute;
import org.fujaba.graphengine.pattern.PatternEdge;
import org.fujaba.graphengine.pattern.PatternNode;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The PatternNodeAdapter is a gson TypeAdapter used to serialize and deserialize PatternNodes to and from JSON. Though this Object doesn't make sense to serialize without its parent context - so it is prohibited by this Adapter.
 * 
 * @author Philipp Kolodziej
 */
public class PatternNodeAdapter extends TypeAdapter<PatternNode>
{

   @Override
   public void write(JsonWriter out, PatternNode node) throws IOException
   {
      IdManager idManager = new IdManager();
      out.beginObject();
      out.name("expression").value(node.getAttributeMatchExpression());
      out.name("action").value(node.getAction());
      out.name("id").value(idManager.getId(node));
      out.name("attributes");
      out.beginArray();
      for (PatternAttribute attribute : node.getPatternAttributes())
      {
         out.beginObject();
         out.name("action").value(attribute.getAction());
         out.name("name").value(attribute.getName());
         if (attribute.getValue() instanceof Boolean)
         {
            out.name("value").value((Boolean) attribute.getValue());
         }
         else if (attribute.getValue() instanceof Integer)
         {
            out.name("value").value((Integer) attribute.getValue());
         }
         else if (attribute.getValue() instanceof Long)
         {
            out.name("value").value((Long) attribute.getValue());
         }
         else if (attribute.getValue() instanceof Double)
         {
            out.name("value").value((Double) attribute.getValue());
         }
         else
         {
            out.name("value").value((String) attribute.getValue());
         }
         out.endObject();
      }
      out.endArray();
      out.name("edges");
      out.beginArray();
      for (PatternEdge edge : node.getPatternEdges())
      {
         out.beginObject();
         out.name("action").value(edge.getAction());
         out.name("name").value(edge.getName());
         out.name("target").value(idManager.getId(edge.getTarget()));
         out.endObject();
      }
      out.endArray();
      out.endObject();
   }


   @Override
   public PatternNode read(JsonReader in) throws IOException
   {
      throw new IOException("single PatternNodes cannot be read. try reading the whole PatternGraph!");
   }

}
