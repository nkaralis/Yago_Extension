package gr.uoa.di.kr.yagoextension.writers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gr.uoa.di.kr.yagoextension.structures.MatchesStructure;

public class DatasetWriter {
	
	private String outputFileMatched;
	private String outputFileUnmatched;
	private String matchesFile;
	private String data;
	private MatchesStructure matches;
	private String entPrefix;
	final static Logger logger = LogManager.getLogger(DatasetWriter.class);
	/** common RDF namespaces */
	final private String[] namespaces = {"http://www.opengis.net/ont/geosparql#", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", 
	                                     "http://www.w3.org/2000/01/rdf-schema#", "http://www.w3.org/2002/07/owl#"};

	public DatasetWriter(String pathMatched, String pathUnmatched, String matches, String data, String prefix) {
		this.outputFileMatched = pathMatched;
		this.outputFileUnmatched = pathUnmatched;
		this.matchesFile = matches;
		this.data = data;
		this.entPrefix = prefix;
	}
	
	public DatasetWriter(String pathMatched, String pathUnmatched, MatchesStructure matches, String data, String prefix) {
		this.outputFileMatched = pathMatched;
		this.outputFileUnmatched = pathUnmatched;
		this.matches = matches;
		this.data = data;
		this.entPrefix = prefix;
	}

	public void write() throws IOException {
		if(matches == null)
			writeFromFile();
		else
			writeFromStruct();
	}

	private void writeFromFile() throws IOException {
		
		String yagoextensionNS = "http://kr.di.uoa.gr/yago-extension/";
		
		logger.info("Started reading matches and data");
		/** store matches and data into jena models */
		Model modelMatches = RDFDataMgr.loadModel(matchesFile);
		Model modelData = RDFDataMgr.loadModel(data);
		logger.info("Finished reading matches and data");
		
		List<Triple> triplesMatched = new ArrayList<Triple>();
		List<Triple> triplesUnmatched = new ArrayList<Triple>();
		StmtIterator dataIter;
		
		/** open files */
		FileOutputStream outMatched = new FileOutputStream(outputFileMatched);
		FileOutputStream outUnmatched = new FileOutputStream(outputFileUnmatched);
		
		/** iterate over data */
		ResIterator subjIter = modelData.listSubjects();
		while(subjIter.hasNext()) {
			
			Resource dataEnt = subjIter.next();
			if(modelData.contains(null, null, dataEnt)) continue; // skip geometry resources. such resources are handled later
			RDFNode yagoEnt = null;
			if(modelMatches.listObjectsOfProperty(dataEnt, null).hasNext()) 
				yagoEnt = modelMatches.listObjectsOfProperty(dataEnt, null).next();
			dataIter = modelData.listStatements(dataEnt, null, (RDFNode)null);
			while(dataIter.hasNext()) {
				Statement s = dataIter.next();
				Property pred = s.getPredicate();
				RDFNode obj = s.getObject();
				
				/** do not change the namespace of common predicated (e.g. rdf:type, geo:hasGeometry, etc.) */
				if(Arrays.asList(namespaces).contains(pred.getNameSpace())){
					/** extract asWKT triple in case there is a hasGeometry fact */
					if(pred.getLocalName().equals("hasGeometry")) {
						Property asWKT = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#", "asWKT");
						RDFNode wkt = modelData.listObjectsOfProperty(obj.asResource(), null).next();
						obj = ResourceFactory.createResource(yagoextensionNS+obj.asResource().getLocalName());
						if(yagoEnt == null)
							triplesUnmatched.add(new Triple(obj.asNode(), asWKT.asNode(), wkt.asNode()));
						else
							triplesMatched.add(new Triple(obj.asNode(), asWKT.asNode(), wkt.asNode()));
					}
					if(yagoEnt == null)
						triplesUnmatched.add(new Triple(ResourceFactory.createResource(yagoextensionNS+entPrefix+dataEnt.getLocalName()).asNode(), pred.asNode(), obj.asNode()));
					else
						triplesMatched.add(new Triple(yagoEnt.asNode(), pred.asNode(), obj.asNode()));
				}
				/** set new namespace for the predicates of the dataset */
				else {
					if(yagoEnt == null)
						triplesUnmatched.add(new Triple(ResourceFactory.createResource(yagoextensionNS+entPrefix+dataEnt.getLocalName()).asNode(),
								ResourceFactory.createProperty(yagoextensionNS, pred.getLocalName()).asNode(), obj.asNode()));
					else
						triplesMatched.add(new Triple(
								yagoEnt.asNode(), ResourceFactory.createProperty(yagoextensionNS, pred.getLocalName()).asNode(), obj.asNode()));
				}
			}
			
			
		}
		
		/** write knowledge graphs to files */
		logger.info("Writing to files");
		RDFDataMgr.writeTriples(outMatched, triplesMatched.iterator());
		RDFDataMgr.writeTriples(outUnmatched, triplesUnmatched.iterator());
		
		outMatched.close();
		outUnmatched.close();
	}
	
	private void writeFromStruct() {
		
		//TO-DO
		
	}
	
}
