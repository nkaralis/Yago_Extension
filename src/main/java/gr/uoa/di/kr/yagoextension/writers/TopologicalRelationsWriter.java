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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;
import gr.uoa.di.kr.yagoextension.structures.Entity;

public class TopologicalRelationsWriter {

	private String outputFile;
	private List<Entity> entities;
	private Property touches;
	private Property within;
	private int kgsize;
	private int nThreads;
	private Integer position;
	private FileOutputStream out;
	final static Logger rootLogger = LogManager.getRootLogger();
	
	public TopologicalRelationsWriter(List<Entity> ents, String path, int threads) {
		this.entities = ents;
		this.kgsize = ents.size();
		this.outputFile = path;
		this.nThreads = threads;
		touches = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#", "sfTouches");
		within = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#", "sfWithin");
		position = 0;
	}
	
	public void write() throws InterruptedException, IOException {

		/** file that in which the results will be written */
		out = new FileOutputStream(outputFile);
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
		/** terminate progress bar and close file */
		out.close();
	}
	
	private void spatialOps() {
		List<Triple> topoRelations = new ArrayList<Triple>();
		int current = 0;
		while(true) {
			/** check if the end of the list has been reached by one of the threads */
			synchronized(position) {
				if(position >= kgsize - 1)
					break;
				else {
					current = position;
					position ++;
				}
				if(position % 1000 == 0)
					rootLogger.info("Processed "+position+"/"+kgsize+" elements.");
			}
			/** Generate topological relations between current entity and the entities that are stored in positions > current.
			 *  This way, we avoid duplicate results .
			 */
			Entity ent = entities.get(current);
			rootLogger.debug("Processing "+ent.getID());
			Geometry geom = ent.getGeometry();
			String id = ent.getID();
			for(int i = current + 1; i < kgsize; i++) {
				Entity qEnt = entities.get(i);
				Geometry qGeom = qEnt.getGeometry();
				String qID = qEnt.getID();
				/** sf-touches */
				try {
					if(geom.touches(qGeom)) {
						topoRelations.add(
								new Triple(ResourceFactory.createResource(id).asNode(), touches.asNode(), ResourceFactory.createResource(qID).asNode()));
						topoRelations.add(
								new Triple(ResourceFactory.createResource(qID).asNode(), touches.asNode(), ResourceFactory.createResource(id).asNode()));
					}
				} catch(TopologyException e) {
					if(geom.buffer(0.0).touches(qGeom.buffer(0.0))) {
						topoRelations.add(
								new Triple(ResourceFactory.createResource(id).asNode(), touches.asNode(), ResourceFactory.createResource(qID).asNode()));
						topoRelations.add(
								new Triple(ResourceFactory.createResource(qID).asNode(), touches.asNode(), ResourceFactory.createResource(id).asNode()));
					}
				}
				/** sf-within */
				try {
					if(geom.within(qGeom)) {
						topoRelations.add(
								new Triple(ResourceFactory.createResource(id).asNode(), within.asNode(), ResourceFactory.createResource(qID).asNode()));
					}
					else if(qGeom.within(geom)) {
						topoRelations.add(
							new Triple(ResourceFactory.createResource(qID).asNode(), within.asNode(), ResourceFactory.createResource(id).asNode()));
					}
				} catch(TopologyException e) {
					if(geom.buffer(0.0).within(qGeom.buffer(0.0))) {
						topoRelations.add(
								new Triple(ResourceFactory.createResource(id).asNode(), within.asNode(), ResourceFactory.createResource(qID).asNode()));
					}
					else if(qGeom.buffer(0.0).within(geom.buffer(0.0))) {
						topoRelations.add(
							new Triple(ResourceFactory.createResource(qID).asNode(), within.asNode(), ResourceFactory.createResource(id).asNode()));
					}
				}
			}
//			synchronized(pb) {
//				pb.step();
//			}
		}
		synchronized(out) {
			rootLogger.info("Writing results to file");
			RDFDataMgr.writeTriples(out, topoRelations.iterator());
		}
	}
	
}
