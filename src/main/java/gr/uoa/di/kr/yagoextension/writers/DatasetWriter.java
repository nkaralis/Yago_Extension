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
import org.apache.jena.util.ResourceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gr.uoa.di.kr.yagoextension.structures.MatchesStructure;

public class DatasetWriter {
	
	private String outputFileMatched;
	private String outputFileUnmatched;
	private String matchesFile;
	private String data;
	private MatchesStructure matches;
	private String source;
	final static Logger logger = LogManager.getLogger(DatasetWriter.class);
	/** common RDF namespaces */
	final private String[] namespaces = {"http://www.opengis.net/ont/geosparql#", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", 
	                                     "http://www.w3.org/2000/01/rdf-schema#", "http://www.w3.org/2002/07/owl#"};

	public DatasetWriter(String pathMatched, String pathUnmatched, String matches, String data, String source) {
		this.outputFileMatched = pathMatched;
		this.outputFileUnmatched = pathUnmatched;
		this.matchesFile = matches;
		this.data = data;
		this.source = source;
	}
	
	public DatasetWriter(String pathMatched, String pathUnmatched, MatchesStructure matches, String data, String source) {
		this.outputFileMatched = pathMatched;
		this.outputFileUnmatched = pathUnmatched;
		this.matches = matches;
		this.data = data;
		this.source = source;
	}

	public void write() throws IOException {
		if(matches == null)
			writeFromFile();
		else
			writeFromStruct();
	}

	private void writeFromFile() throws IOException {
		
		String extensionRNS = "http://kr.di.uoa.gr/yago-extension/resource/";
		String extensionONS = "http://kr.di.uoa.gr/yago-extension/ontology/";
		
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
		
		/** OGC asWKT property */
		Property asWKT = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#", "asWKT");
		/** OGC hasGeometry property */
		Property hasGeo = ResourceFactory.createProperty("http://www.opengis.net/ont/geosparql#", "hasGeometry");
		
		/** iterate over data */
		ResIterator subjIter = modelData.listSubjects();
		while(subjIter.hasNext()) {
			
			Resource dataEnt = subjIter.next();
			if(modelData.contains(null, null, dataEnt) && modelData.contains(dataEnt, asWKT)) continue; // skip geometry resources. such resources are handled later
			RDFNode yagoEnt = null;
			if(modelMatches.listObjectsOfProperty(dataEnt, null).hasNext()) 
				yagoEnt = modelMatches.listObjectsOfProperty(dataEnt, null).next();
			dataIter = modelData.listStatements(dataEnt, null, (RDFNode)null);
			while(dataIter.hasNext()) {
				Statement s = dataIter.next();
				Property pred = s.getPredicate();
				String predNS = pred.getNameSpace();
				String predLN = pred.getLocalName();
				RDFNode obj = s.getObject();
				Property newPred = null;
				RDFNode newObj = null;
				
				/** handle each data source differently */
				if(source.toLowerCase().equals("gadm")) {
					/** check if the predicate is part of the GADM ontology */
					if(predLN.equals("hasGADM_ID") || predLN.equals("hasGADM_Name") || 
							predLN.equals("hasGADM_NationalLevel") || predLN.equals("hasGADM_UpperLevelUnit")) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = obj;
					}
					else if(predLN.equals("type") && predNS.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#")) {
						newPred = pred;
						newObj = ResourceFactory.createResource(extensionONS+obj.asResource().getLocalName());
					}
					else if(predLN.equals("hasGeometry") && predNS.equals("http://www.opengis.net/ont/geosparql#")) {
						newPred = pred;
						newObj = ResourceFactory.createResource(extensionRNS+obj.asResource().getLocalName());
						RDFNode wkt = modelData.listObjectsOfProperty(obj.asResource(), null).next();
						if(yagoEnt != null)
							triplesMatched.add(new Triple(ResourceFactory.createResource(extensionRNS+obj.asResource().getLocalName()).asNode(), 
									asWKT.asNode(), wkt.asNode()));
						else
							triplesMatched.add(new Triple(ResourceFactory.createResource(extensionRNS+obj.asResource().getLocalName()).asNode(), 
									asWKT.asNode(), wkt.asNode()));
					}
					else
						continue;
				}
				else if(source.toLowerCase().equals("kallikratis")) {
					/** check if the predicate is part of the Kallikratis ontology */
					if(predLN.equals("hasKallikratis_ID") || predLN.equals("hasKallikratis_Name") || 
							predLN.equals("hasKallikratis_Population")){
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = obj;
					}
					else if(predLN.equals("asWKT")) {
						newPred = hasGeo;
						RDFNode geom = ResourceFactory.createResource(extensionRNS+"Geometry_"+dataEnt.getLocalName());
						newObj = geom;
						if(yagoEnt != null)
							triplesMatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
						else
							triplesUnmatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
					}
					else
						continue;
					
				}
				
				else if(source.toLowerCase().equals("osm")) {
					/** check if the predicate is part of the OpenStreetMap ontology */
					if(predLN.equals("hasOSM_ID") || predLN.equals("hasOSM_FClass") || predLN.equals("hasOSM_Name")) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = obj;
					}
					else if(predLN.equals("asWKT") && predNS.equals("http://www.opengis.net/ont/geosparql#")) {
						newPred = hasGeo;
						RDFNode geom = ResourceFactory.createResource(extensionRNS+"Geometry_"+dataEnt.getLocalName());
						newObj = geom;
						if(yagoEnt != null)
							triplesMatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
						else
							triplesUnmatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
					}
					else
						continue;
				}
				
				else if(source.toLowerCase().equals("os")) {
					/** check if the predicate is part of the OrdnanceSurvey ontology */
					if(predLN.equals("hasOS_AreaCode") || predLN.equals("hasOS_Description") || 
							predLN.equals("hasOS_ID") || predLN.equals("hasOS_Name") ) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = obj;
					}
					else if(predLN.equals("asWKT") && predNS.equals("http://www.opengis.net/ont/geosparql#")) {
						newPred = hasGeo;
						RDFNode geom = ResourceFactory.createResource(extensionRNS+"Geometry_"+dataEnt.getLocalName());
						newObj = geom;
						if(yagoEnt != null)
							triplesMatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
						else
							triplesUnmatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
					}
					else
						continue;
				}
				
				/** add triple to the corresponding list */
				if(yagoEnt != null) 
					triplesMatched.add(new Triple(yagoEnt.asNode(), newPred.asNode(), newObj.asNode()));
				else
					triplesUnmatched.add(new Triple(ResourceFactory.createResource(extensionRNS+source+"entity_"+dataEnt.getLocalName()).asNode(), 
							newPred.asNode(), newObj.asNode()));
				
				
				/** do not change the namespace of common predicated (e.g. rdf:type, geo:hasGeometry, etc.) */
//				if(Arrays.asList(namespaces).contains(pred.getNameSpace())){
//					/** extract asWKT triple in case there is a hasGeometry fact */
//					if(pred.getLocalName().equals("hasGeometry")) {
//						RDFNode wkt = modelData.listObjectsOfProperty(obj.asResource(), null).next();
//						obj = ResourceFactory.createResource(yagoextensionRNS+obj.asResource().getLocalName());
//						if(yagoEnt == null)
//							triplesUnmatched.add(new Triple(obj.asNode(), asWKT.asNode(), wkt.asNode()));
//						else
//							triplesMatched.add(new Triple(obj.asNode(), asWKT.asNode(), wkt.asNode()));
//					}
//					/** change the namespace of the class */
//					if(yagoEnt == null) {
//						/** change the namespace of the class in order to follow the Yago schema */
//						if(pred.getLocalName().equals("type") && !Arrays.asList(namespaces).contains(obj.asResource().getNameSpace()))
//							triplesUnmatched.add(new Triple(ResourceFactory.createResource(yagoextensionONS+entPrefix+dataEnt.getLocalName()).asNode(), 
//									pred.asNode(), ResourceFactory.createResource(yagoextensionONS+obj.asNode().getLocalName()).asNode()));
//						else
//							triplesUnmatched.add(new Triple(
//									ResourceFactory.createResource(yagoextensionRNS+entPrefix+dataEnt.getLocalName()).asNode(), pred.asNode(), obj.asNode()));
//					}
//					else {
//						/** change the namespace of the class in order to follow the Yago schema */
//						if(pred.getLocalName().equals("type") && !Arrays.asList(namespaces).contains(obj.asResource().getNameSpace()))
//							triplesMatched.add(new Triple(yagoEnt.asNode(), 
//									pred.asNode(), ResourceFactory.createResource(yagoextensionONS+obj.asNode().getLocalName()).asNode()));
//						else
//							triplesMatched.add(new Triple(yagoEnt.asNode(), pred.asNode(), obj.asNode()));
//					}
//				}
//				/** set new namespace for the predicates of the dataset */
//				else {
//					if(yagoEnt == null)
//						triplesUnmatched.add(new Triple(ResourceFactory.createResource(yagoextensionRNS+entPrefix+dataEnt.getLocalName()).asNode(),
//								ResourceFactory.createProperty(yagoextensionRNS, pred.getLocalName()).asNode(), obj.asNode()));
//					else
//						triplesMatched.add(new Triple(
//								yagoEnt.asNode(), ResourceFactory.createProperty(yagoextensionRNS, pred.getLocalName()).asNode(), obj.asNode()));
//				}
//			}
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
