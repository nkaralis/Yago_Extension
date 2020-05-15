package gr.uoa.di.kr.yagoextension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import gr.uoa.di.kr.yagoextension.vocabulary.RDFVocabulary;
import javafx.util.Pair;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.Lock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class TopologicalRelationsGenerator {

	public static void run(Properties properties) throws IOException {

		if (!properties.containsKey("data") || !properties.containsKey("output")) {
			System.out.println("Properties yago, datasource and output are mandatory");
		}

		String dataFile = properties.getProperty("data");
		if (Files.notExists(Paths.get(dataFile))) {
			throw new IOException("Path of the input file does not exist");
		}
		String outputFile = properties.getProperty("output");
		if (Files.notExists(Paths.get(outputFile.substring(0, outputFile.lastIndexOf("/") + 1)))) {
			throw new IOException("Path of the output file does not exist");
		}
		Logger logger = LogManager.getLogger(TopologicalRelationsGenerator.class);
		logger.info("Reading data");
		Model data = RDFDataMgr.loadModel(dataFile);
		Model topologicalRelations = ModelFactory.createDefaultModel();
		topologicalRelations.setNsPrefix("geo", "http://www.opengis.net/ont/geosparql#");
		logger.info("Finished reading data");
		Property sfWithin = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#sfWithin");
		Property sfTouches = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#sfTouches");
		Property hasGeometry = ResourceFactory.createProperty(RDFVocabulary.HAS_GEOMETRY);
		Property asWkt = ResourceFactory.createProperty(RDFVocabulary.AS_WKT);

		logger.info("Preparing data");
		List<Pair<String, String>> subjGeomPairs = new ArrayList<>();
		StmtIterator stmtsIter = data.listStatements(null, hasGeometry ,(RDFNode)null);
		while (stmtsIter.hasNext()) {
			Statement statement = stmtsIter.next();
			String uri = statement.getSubject().getURI();
			Resource geometry = statement.getObject().asResource();
			String wkt = geometry.getProperty(asWkt).getObject().asLiteral().getString();
			subjGeomPairs.add(new Pair<>(uri, wkt));
		}
		logger.info("Generating topological relations");
		WKTReader wktReader = new WKTReader();
		final Integer[] progress = new Integer[]{0, 1};
		List<Pair<String, String>> shuffledSGP = new ArrayList<>(subjGeomPairs);
		Collections.shuffle(shuffledSGP);
		shuffledSGP.parallelStream().forEach(pair -> {
			try {
				int pairPos = subjGeomPairs.indexOf(pair);
				String subj = pair.getKey();
				logger.info("Processing item: "+pairPos);
				Geometry geom = wktReader.read(pair.getValue());
				for (int i = pairPos + 1; i < subjGeomPairs.size() - 1; i++) {
					String curSubj = subjGeomPairs.get(i).getKey();
					Geometry curGeom = wktReader.read(subjGeomPairs.get(i).getValue());
					if(geom.buffer(0.0).touches(curGeom.buffer(0.0))) {
						topologicalRelations.enterCriticalSection(Lock.WRITE);
						topologicalRelations.add(ResourceFactory.createResource(subj), sfTouches, ResourceFactory.createResource(curSubj));
						topologicalRelations.leaveCriticalSection();
					}
					if(geom.buffer(0.0).within(curGeom.buffer(0.0))) {
						topologicalRelations.enterCriticalSection(Lock.WRITE);
						topologicalRelations.add(ResourceFactory.createResource(subj), sfWithin, ResourceFactory.createResource(curSubj));
						topologicalRelations.leaveCriticalSection();
					}
					else if(curGeom.buffer(0.0).within(geom.buffer(0.0))) {
						topologicalRelations.enterCriticalSection(Lock.WRITE);
						topologicalRelations.add(ResourceFactory.createResource(curSubj), sfWithin, ResourceFactory.createResource(subj));
						topologicalRelations.leaveCriticalSection();
					}
				}
				synchronized(progress) {
					progress[0] += 1;
					if(subjGeomPairs.size() * 0.1 * progress[1] < progress[0]) {
						logger.info("Progress: " + progress[1]*10 + "%");
						progress[1] += 1;
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		});
		logger.info("Writing results");
		OutputStream outputStream = new FileOutputStream(new File(outputFile));
		RDFDataMgr.write(outputStream, topologicalRelations, RDFFormat.TURTLE_FLAT);
		outputStream.close();
	}

}
