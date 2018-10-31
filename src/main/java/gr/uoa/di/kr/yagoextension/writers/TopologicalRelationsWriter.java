package gr.uoa.di.kr.yagoextension.writers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import com.vividsolutions.jts.geom.Geometry;
import gr.uoa.di.kr.yagoextension.structures.Entity;
import me.tongfei.progressbar.ProgressBar;

public class TopologicalRelationsWriter {

	private String outputFile;
	private Map<String, Entity> entities;
	private Property borders = ResourceFactory.createProperty("http://kr.di.uoa.gr/yago-extension/ontology/", "bordersWith");
	private Property belongs = ResourceFactory.createProperty("http://kr.di.uoa.gr/yago-extension/ontology/", "belongsTo");
	private ProgressBar pb;
	
	public TopologicalRelationsWriter(Map<String, Entity> ent, String path) {
		this.entities = ent;
		this.outputFile = path;
		pb = new ProgressBar("Topology", ent.size());
	}
	
	public void write() throws FileNotFoundException {
		
		FileOutputStream out = new FileOutputStream(outputFile);
		List<Triple> topoRelations = new ArrayList<Triple>();
		Set<String> ents = entities.keySet();
		Set<String> seen = new HashSet<String>();
		
		/** iterate over the entities to find topological relations */
		for(String key1 : ents) {
			Geometry geom1 = entities.get(key1).getGeometry().buffer(0.0);
			seen.add(key1);
			for(String key2 : ents) {
				if(seen.contains(key2)) continue;
				Geometry geom2 = entities.get(key2).getGeometry().buffer(0.0);
				/** run geospatial predicates in order to generate topological relations */
				/** RCC8 EC*/
				if(geom1.touches(geom2)) {
					topoRelations.add(
							new Triple(ResourceFactory.createResource(key1).asNode(), borders.asNode(), ResourceFactory.createResource(key2).asNode()));
					topoRelations.add(
							new Triple(ResourceFactory.createResource(key2).asNode(), borders.asNode(), ResourceFactory.createResource(key1).asNode()));
				}
				/** RCC8 TPP & nTPP */
				else if(geom1.contains(geom2))
					topoRelations.add(
							new Triple(ResourceFactory.createResource(key2).asNode(), belongs.asNode(), ResourceFactory.createResource(key1).asNode()));					
				else if(geom2.contains(geom1))
					topoRelations.add(
							new Triple(ResourceFactory.createResource(key1).asNode(), belongs.asNode(), ResourceFactory.createResource(key2).asNode()));
			}
			pb.step();
		}
		pb.close();
		/** write topological relations to file */
		RDFDataMgr.writeTriples(out, topoRelations.iterator());
	}
	
}
