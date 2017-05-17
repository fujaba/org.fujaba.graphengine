package org.fujaba.graphengine.stateelimination;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.fujaba.graphengine.IdManager;
import org.fujaba.graphengine.graph.Graph;
import org.fujaba.graphengine.graph.Node;

/**
 * 
 * thanks to google:
 * 
 * http://www.tutorialspoint.com/java_xml/java_stax_parse_document.htm
 * 
 * @author hoechp
 *
 */
public class TTCStateCaseGraphLoader {
    public static Graph load(String path) {
	   Graph graph = new Graph();
       try {
           XMLInputFactory factory = XMLInputFactory.newInstance();
           XMLEventReader eventReader =
           factory.createXMLEventReader(new FileReader(path));
           IdManager idManager = new IdManager();
           while (eventReader.hasNext()) {
        	   XMLEvent event = eventReader.nextEvent();
        	   switch (event.getEventType()) {
        	   case XMLStreamConstants.START_ELEMENT:
        		   StartElement startElement = event.asStartElement();
                   String qName = startElement.getName().getLocalPart();
                   if (qName.equalsIgnoreCase("states")) {
                	   Node node = new Node();
                	   @SuppressWarnings("unchecked")
					   Iterator<Attribute> attributes = startElement.getAttributes();
                	   while (attributes.hasNext()) {
                		   Attribute currentAttribute = attributes.next();
                		   String attributeName = currentAttribute.getName().toString();
                		   String attributeValue = currentAttribute.getValue();
                		   if (attributeName.equalsIgnoreCase("isInitial")) {
                			   // telling the idManager that this node has id == 0:
                			   /**
                			    * in the paper i though it said there could be multiple initial states.
                			    * but there's always just one and it has no id...
                			    * though still it is references as id == 0. so I'll just assume it to be that way.
                			    */
                			   idManager.tellId(new Long(0), node);
                			   
                			   node.setAttribute("initial", true); // set attribute for initial state
                		   } else if (attributeName.equalsIgnoreCase("id")) {
                			   // telling the idManager that this node has that id:
                			   idManager.tellId(Long.parseLong(attributeValue), node);
                		   } else if (attributeName.equalsIgnoreCase("outgoing")) {
                			   // shouldn't matter right here
                		   } else if (attributeName.equalsIgnoreCase("ingoing")) {
                			   // shouldn't matter right here
                		   } else if (attributeName.equalsIgnoreCase("isFinal")) {
                			   node.setAttribute("final", true); // set attribute for final state
                		   }
                	   }
                	   graph.addNode(node);
                   } else if (qName.equalsIgnoreCase("transitions")) {
                	   @SuppressWarnings("unchecked")
					   Iterator<Attribute> attributes = startElement.getAttributes();
                	   String label = "";
                	   long sourceId = -1;
                	   long targetId = -1;
//                	   double probability = -1;
                	   while (attributes.hasNext()) {
                		   Attribute currentAttribute = attributes.next();
                		   String attributeName = currentAttribute.getName().toString();
                		   String attributeValue = currentAttribute.getValue();
                		   if (attributeName.equalsIgnoreCase("label")) {
                			   // so we have an edge label
                			   label = attributeValue;
                		   } else if (attributeName.equalsIgnoreCase("source")) {
                			   // the source id
                			   sourceId = Long.parseLong(attributeValue.substring(attributeValue.lastIndexOf('.') + 1));
                		   } else if (attributeName.equalsIgnoreCase("target")) {
                			   // the target id
                			   targetId = Long.parseLong(attributeValue.substring(attributeValue.lastIndexOf('.') + 1));
                		   } else if (attributeName.equalsIgnoreCase("probability")) {
                			   // the probability (not used for now)
//                			   probability = Double.parseDouble(attributeValue); // TODO: also use the probability
	            		   }
                	   }
                	   ((Node)idManager.getObject(sourceId)).addEdge(label, ((Node)idManager.getObject(targetId)));
                   }		        
                   break;
               case XMLStreamConstants.CHARACTERS:
                   // shouldn't even happen
                   break;
               case  XMLStreamConstants.END_ELEMENT:
                   // shouldn't really matter
                   break;
               }		    
           }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return graph;
    }
}