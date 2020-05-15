package gr.uoa.di.kr.yagoextension.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import gr.uoa.di.kr.yagoextension.model.Entity;

public class Blacklist {
	
	public static void removeMatchedEntities(Set<Entity> yago, Set<Entity> ds, String[] matchesFile) {

		Model matches = ModelFactory.createDefaultModel();
		Arrays.stream(matchesFile).forEach(file -> matches.read(file.trim()));
		Set<Entity> yagoToRemove = new HashSet<>();
		Set<Entity> dsToRemove = new HashSet<>();
		yago.forEach(entity -> {
			RDFNode resource = ResourceFactory.createResource(entity.getURI());
			if(matches.containsResource(resource))
				yagoToRemove.add(entity);
		});
		ds.forEach(entity -> {
			RDFNode resource = ResourceFactory.createResource(entity.getURI());
			if(matches.containsResource(resource))
				dsToRemove.add(entity);
		});
		yago.removeAll(yagoToRemove);
		ds.removeAll(dsToRemove);
	}

}
