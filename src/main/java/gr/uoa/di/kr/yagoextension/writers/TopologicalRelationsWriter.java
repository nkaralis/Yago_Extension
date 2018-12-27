package gr.uoa.di.kr.yagoextension.writers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;
import gr.uoa.di.kr.yagoextension.structures.Entity;
import me.tongfei.progressbar.ProgressBar;

public class TopologicalRelationsWriter {

	private String outputFile;
	private List<Entity> entities;
	private Property touches;
	private Property within;
	private List<Triple> topoRelations;
	private int kgsize;
	private ProgressBar pb;
	private int nThreads;
	private Integer position;
	
	public TopologicalRelationsWriter(List<Entity> ents, String path, int threads) {
		this.entities = ents;
		this.kgsize = ents.size();
		this.outputFile = path;
		this.topoRelations = new ArrayList<Triple>();
		this.nThreads = threads;
		pb = new ProgressBar("TopologicalRelations", kgsize);
		touches = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#", "sf-touches");
		within = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#", "sf-within");
		position = 0;
	}
	
	public void write() throws InterruptedException, IOException {
		
		/** file that in which the results will be written */
		FileOutputStream out = new FileOutputStream(outputFile);
		/** initialized thread pool and threads */
		ExecutorService exec = Executors.newFixedThreadPool(nThreads);
		for(int i = 0; i < nThreads; i++) {
			Runnable spatialOpsTask = new Runnable(){
				public void run(){
					spatialOps();
				}
			};
			exec.submit(spatialOpsTask);
		}
		exec.shutdown();
		exec.awaitTermination(10000000, TimeUnit.MINUTES);
		/** write to file and the close it */
		pb.close();
		RDFDataMgr.writeTriples(out, topoRelations.iterator());
		out.close();
	}
	
	private void spatialOps() {
		
		int current = 0;
		while(true) {
			/** check if the end of the list has been reached by one of the threads */
			synchronized(position) {
				if(position >= kgsize)
					break;
				else {
					current = position;
					position ++;
				}
			}
			/** Generate topological relations between current entity and the entities that are stored in positions > current.
			 *  This way, we avoid duplicate results .
			 */
			Entity ent = entities.get(current);
			Geometry geom = ent.getGeometry();
			String id = ent.getID();
			for(int i = current + 1; i < kgsize; i++) {
				Entity qEnt = entities.get(i);
				Geometry qGeom = qEnt.getGeometry();
				String qID = qEnt.getID();
				/** sf-touches */
				try {
					if(geom.touches(qGeom)) {
						synchronized(topoRelations) {
							topoRelations.add(
									new Triple(ResourceFactory.createResource(id).asNode(), touches.asNode(), ResourceFactory.createResource(qID).asNode()));
							topoRelations.add(
									new Triple(ResourceFactory.createResource(qID).asNode(), touches.asNode(), ResourceFactory.createResource(id).asNode()));
						}
					}
				} catch(TopologyException e) {
					if(geom.buffer(0.0).touches(qGeom.buffer(0.0))) {
						synchronized(topoRelations) {
							topoRelations.add(
									new Triple(ResourceFactory.createResource(id).asNode(), touches.asNode(), ResourceFactory.createResource(qID).asNode()));
							topoRelations.add(
									new Triple(ResourceFactory.createResource(qID).asNode(), touches.asNode(), ResourceFactory.createResource(id).asNode()));
						}
					}
				}
				try {
					if(geom.within(qGeom)) {
						synchronized(topoRelations) {
							topoRelations.add(
									new Triple(ResourceFactory.createResource(id).asNode(), within.asNode(), ResourceFactory.createResource(qID).asNode()));
						}
					}
					else if(qGeom.within(geom)) {
						synchronized(topoRelations) {
							topoRelations.add(
								new Triple(ResourceFactory.createResource(qID).asNode(), within.asNode(), ResourceFactory.createResource(id).asNode()));
						}
					}
				} catch(TopologyException e) {
					if(geom.buffer(0.0).within(qGeom.buffer(0.0))) {
						synchronized(topoRelations) {
							topoRelations.add(
									new Triple(ResourceFactory.createResource(id).asNode(), within.asNode(), ResourceFactory.createResource(qID).asNode()));
						}
					}
					else if(qGeom.buffer(0.0).within(geom.buffer(0.0))) {
						synchronized(topoRelations) {
							topoRelations.add(
								new Triple(ResourceFactory.createResource(qID).asNode(), within.asNode(), ResourceFactory.createResource(id).asNode()));
						}
					}
				}
			}
			synchronized(pb) {
				pb.step();
			}
		}
	}
	
}
