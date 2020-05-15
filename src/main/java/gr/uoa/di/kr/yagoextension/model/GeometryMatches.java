package gr.uoa.di.kr.yagoextension.model;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.OutputStream;
import java.util.*;

public class GeometryMatches {

	private Map<Entity, Entity> matches;
	private Map<Entity, Double> distances;
	
	public GeometryMatches() {
		this.matches = new HashMap<>();
		this.distances = new HashMap<>();
	}

	public Set<Entity> getKeys() {
		return matches.keySet();
	}

	public Entity getValueByKey(Entity key) {
		return matches.get(key);
	}

	public void addMatch(Entity key, Entity value, double dist) {
		/* if the key is already in use: update the list if the distance is same or better */
		if(this.distances.containsKey(key)) {
			if(dist <= this.distances.get(key)) {
				this.distances.put(key, dist);
				this.matches.put(key, value);
			}
		}
		else {
			this.distances.put(key, dist);
			this.matches.put(key, value);
		}
	}

	public int size() {
		return matches.size();
	}

	public void writeToFile(OutputStream os) {

		Model matchesModel = ModelFactory.createDefaultModel();
		Property sameAs = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#", "sameAs");
		this.matches.forEach((dsEntity, yagoEntity) -> {
				Resource subject = ResourceFactory.createResource(yagoEntity.getURI());
				Resource object = ResourceFactory.createResource(dsEntity.getURI());
				matchesModel.add(subject, sameAs, object);
			});
		RDFDataMgr.write(os, matchesModel, RDFFormat.TURTLE_FLAT);

	}

	public Map<String, String> getUriMatches() {
	  Map<String, String> uriMatches = new HashMap<>();
	  matches.forEach((k, v) -> {
	    uriMatches.put(k.getURI(), v.getURI());
    });
	  return uriMatches;
  }

}
