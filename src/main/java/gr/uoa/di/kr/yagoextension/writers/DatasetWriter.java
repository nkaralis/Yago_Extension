package gr.uoa.di.kr.yagoextension.writers;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
	private String source;
	private String yagoClass = null;
	final static Logger logger = LogManager.getRootLogger();

	public DatasetWriter(String pathMatched, String pathUnmatched, String matches, String data, String source, String yagoClass) {
		this.outputFileMatched = pathMatched;
		this.outputFileUnmatched = pathUnmatched;
		this.matchesFile = matches;
		this.data = data;
		this.source = source;
		this.yagoClass = yagoClass;
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
		String yagoNS = "http://yago-knowledge.org/resource/";
		
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
		/** RDF type property */
		Property type = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
		
		/** iterate over data */
		ResIterator subjIter = modelData.listSubjects();
		while(subjIter.hasNext()) {
			
			Resource dataEnt = subjIter.next();
			if(dataEnt.getLocalName() == null)
				continue;
			if(modelData.contains(null, null, dataEnt) && modelData.contains(dataEnt, asWKT)) continue; // skip geometry resources. such resources are handled later
			String localName = dataEnt.getURI().split("/")[dataEnt.getURI().split("/").length-1];
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
					if(predLN.equals("hasGADM_ID")) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = ResourceFactory.createStringLiteral(obj.asLiteral().getValue().toString());
					}
					else if(predLN.equals("hasGADM_Name")) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = obj;
					}
//					else if(predLN.equals("hasGADM_NationalLevel") || predLN.equals("hasGADM_UpperLevelUnit")) {
//            newPred = ResourceFactory.createProperty(extensionONS, predLN);
//            newObj = obj;
//          }
					else if(predLN.equals("type") && predNS.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#")) {
						newPred = ResourceFactory.createProperty(extensionONS, "hasGADM_Type");
						newObj = ResourceFactory.createStringLiteral(obj.asResource().getLocalName());
					}
					else if(predLN.equals("hasGeometry") && predNS.equals("http://www.opengis.net/ont/geosparql#")) {
						newPred = pred;
						newObj = ResourceFactory.createResource(extensionRNS+obj.asResource().getLocalName().replaceAll("Geometry_", "Geometry_gadm_"));
						RDFNode wkt = modelData.listObjectsOfProperty(obj.asResource(), null).next();
						if(yagoEnt != null)
							triplesMatched.add(new Triple(ResourceFactory.createResource(extensionRNS+newObj.asResource().getLocalName()).asNode(), 
									asWKT.asNode(), wkt.asNode()));
						else
							triplesMatched.add(new Triple(ResourceFactory.createResource(extensionRNS+newObj.asResource().getLocalName()).asNode(), 
									asWKT.asNode(), wkt.asNode()));
					}
					else
						continue;
				}
				else if(source.toLowerCase().equals("kallikratis")) {
					/** check if the predicate is part of the Kallikratis ontology */
					if(predLN.equals("hasKallikratis_ID")) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = ResourceFactory.createStringLiteral(obj.asLiteral().getValue().toString());
					}
					else if(predLN.equals("hasKallikratis_Name")) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = obj;
					}
//					else if(predLN.equals("hasKallikratis_Population")) {
//            newPred = ResourceFactory.createProperty(extensionONS, predLN);
//            newObj = obj;
//          }
					else if(predLN.equals("asWKT")) {
						newPred = hasGeo;
						RDFNode geom = ResourceFactory.createResource(extensionRNS+"Geometry_kallikratis_"+localName);
						newObj = geom;
						if(yagoEnt != null)
							triplesMatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
						else
							triplesUnmatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
					}
					else
						continue;
					
				}
				
				else if(source.toLowerCase().equals("osmshp")) {
					/** check if the predicate is part of the OpenStreetMap (shapefiles) ontology */
					if(predLN.equals("hasOSM_ID") || predLN.equals("hasOSM_FClass") || predLN.equals("hasOSM_Name")) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = obj;
					}
					else if(predLN.equals("asWKT") && predNS.equals("http://www.opengis.net/ont/geosparql#")) {
						newPred = hasGeo;
						RDFNode geom = ResourceFactory.createResource(extensionRNS+"Geometry_osm_"+localName.split("_")[localName.split("_").length-1]);
						newObj = geom;
						if(yagoEnt != null)
							triplesMatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
						else
							triplesUnmatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
					}
					else
						continue;
				}

				else if(source.toLowerCase().equals("osmpbf")) {
					/** TripleGeo categories map */
					HashMap<String, String> tgCategories = new HashMap<String, String>();
					tgCategories.put("33cad1f4-426e-3b58-9e98-32d44f40bd16", "bay");
					tgCategories.put("62b95212-b08c-37c3-abeb-67c1082ff98c", "beach");
					tgCategories.put("905fb6d4-965b-32e6-8cec-7ddfc04f6181", "canal");
					tgCategories.put("51cb0b75-829a-3c75-919b-970067ef1f51", "city");
					tgCategories.put("5b261732-d5a3-3978-9222-9f80e4c1a98b", "island");
					tgCategories.put("18fc7ecd-f71a-3ab5-9461-21e194b4663f", "forest");
					tgCategories.put("5a1fbf10-7ac0-3b82-94bb-0d4b9a78947c", "lagoon");
					tgCategories.put("2d99610e-1206-33a3-a283-eb3fc521d923", "lake");
					tgCategories.put("b083bcc2-1c65-3b11-a924-5e1cc4257c6b", "locality");
					tgCategories.put("75c70f0a-579f-3a34-b2ca-15bba83bcb9a", "nature_reserve");
					tgCategories.put("9a0eaccb-d16b-3a08-bd90-865fa202444e", "oxbow");
					tgCategories.put("d07f8667-aee9-3576-809a-bf803b432b60", "park");
					tgCategories.put("f8b7902b-7339-3f65-97df-324b5c185b2b", "stream");
					tgCategories.put("759eca10-f3bb-3c2f-b30f-08efc6864559", "town");
					tgCategories.put("45be85d6-d2bf-30e2-b366-a0b6414c0f9f", "reservoir");
					tgCategories.put("314ce8c5-f5e8-3380-997c-c22098f7a7cb", "village");
					/** check if the predicate is part of the OpenStreetMap (pbf files) ontology */
					if(predLN.equals("poiRef")) {
						newPred = ResourceFactory.createProperty(extensionONS, "hasOSM_ID");
						newObj = ResourceFactory.createStringLiteral(obj.asLiteral().getValue().toString());
					}
					else if(predLN.equals("nameValue")) {
						newPred = ResourceFactory.createProperty(extensionONS, "hasOSM_Name");
						newObj = obj;
					}
					else if(predLN.equals("category")) {
						newPred = ResourceFactory.createProperty(extensionONS, "hasOSM_FClass");
						newObj = ResourceFactory.createStringLiteral(tgCategories.get(obj.asResource().getLocalName()));

					}
					else if(predLN.equals("asWKT") && predNS.equals("http://www.opengis.net/ont/geosparql#")) {
						newPred = hasGeo;
						RDFNode geom = ResourceFactory.createResource(extensionRNS+"Geometry_osm_"+localName.split("_")[localName.split("_").length-1]);
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
					/** check if the predicate is part of the Ordnance Survey ontology */
					if(predLN.equals("hasOS_ID")) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = ResourceFactory.createStringLiteral(obj.asLiteral().getValue().toString());
					}
					else if(predLN.equals("hasOS_Name")) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = obj;
					}
					else if(predLN.equals("hasOS_Description")) {
            newPred = ResourceFactory.createProperty(extensionONS, "hasGADM_Type");
            newObj = obj;
          }
					else if(predLN.equals("asWKT") && predNS.equals("http://www.opengis.net/ont/geosparql#")) {
						newPred = hasGeo;
						/** keep the id for the geometry */
						RDFNode geom = ResourceFactory.createResource(extensionRNS+"Geometry_os_"+localName.split("_")[localName.split("_").length-1]);
						newObj = geom;
						if(yagoEnt != null)
							triplesMatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
						else
							triplesUnmatched.add(new Triple(geom.asNode(), asWKT.asNode(), obj.asNode()));
					}
					else
						continue;
				}
				
				else if(source.toLowerCase().equals("osni")) {
					/** check if the predicate is part of the Ordnance Survey Northern Ireland ontology */
					if(predLN.equals("hasOSNI_ID")) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = ResourceFactory.createStringLiteral(obj.asLiteral().getValue().toString());
					}
					else if(predLN.equals("hasOSNI_Name")) {
						newPred = ResourceFactory.createProperty(extensionONS, predLN);
						newObj = obj;
					}
//					else if(predLN.equals("hasOSNI_Area") || predLN.equals("hasOSNI_AreaSqKM") || predLN.equals("hasOSNI_Perimeter")) {
//            newPred = ResourceFactory.createProperty(extensionONS, predLN);
//            newObj = obj;
//          }
					else if(predLN.equals("hasOSNI_Class")) {
						newPred = ResourceFactory.createProperty(extensionONS, "hasOSNI_Type");
						newObj = ResourceFactory.createStringLiteral(obj.asLiteral().getString());
					}
					else if(predLN.equals("hasGeometry") && predNS.equals("http://www.opengis.net/ont/geosparql#")) {
						newPred = pred;
						newObj = ResourceFactory.createResource(extensionRNS+obj.asResource().getLocalName().replace("Geometry_", "Geometry_osni_"));
						RDFNode wkt = modelData.listObjectsOfProperty(obj.asResource(), asWKT).next();
						if(yagoEnt != null)
							triplesMatched.add(new Triple(newObj.asNode(), asWKT.asNode(), wkt.asNode()));
						else
							triplesUnmatched.add(new Triple(newObj.asNode(), asWKT.asNode(), wkt.asNode()));
					}
					else
						continue;
				}
				
				else if(source.toLowerCase().equals("osi")) {
					/** check if the predicate is part of the Ordnance Survey Ireland ontology */
					if(predLN.equals("type") && predNS.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#") && 
							!obj.asResource().getLocalName().equals("Feature") && !obj.asResource().getLocalName().equals("Geometry")) {
						newPred = ResourceFactory.createProperty(extensionONS, "hasOSI_Type");
						newObj = ResourceFactory.createStringLiteral(obj.asResource().getLocalName());
						Property hasID = ResourceFactory.createProperty(extensionONS, "hasOSI_ID");
						RDFNode osiID = ResourceFactory.createStringLiteral(dataEnt.getLocalName());
						if(yagoEnt != null)
							triplesMatched.add(new Triple(yagoEnt.asNode(), hasID.asNode(), osiID.asNode()));
						else
							triplesUnmatched.add(new Triple(ResourceFactory.createResource(extensionRNS+source+"entity_"+dataEnt.getLocalName()).asNode(), 
									hasID.asNode(), osiID.asNode()));
					}
					else if(predLN.equals("label") && predNS.equals("http://www.w3.org/2000/01/rdf-schema#")) {
						newPred = ResourceFactory.createProperty(extensionONS, "hasOSI_Name");
						newObj = obj;
					}
					else if(predLN.equals("hasGeometry") && predNS.equals("http://www.opengis.net/ont/geosparql#") && 
							modelData.listObjectsOfProperty(obj.asResource(), asWKT).hasNext()) {
						newPred = pred;
						String osiID = dataEnt.getLocalName();
						newObj = ResourceFactory.createResource(extensionRNS+"Geometry_osi_"+osiID);
						RDFNode wkt = modelData.listObjectsOfProperty(obj.asResource(), asWKT).next();
						if(yagoEnt != null)
							triplesMatched.add(new Triple(newObj.asNode(), asWKT.asNode(), wkt.asNode()));
						else
							triplesUnmatched.add(new Triple(newObj.asNode(), asWKT.asNode(), wkt.asNode()));
					}
					else
						continue;
				}
				
				/** add triple to the corresponding list */
				if(yagoEnt != null)
					triplesMatched.add(new Triple(yagoEnt.asNode(), newPred.asNode(), newObj.asNode()));
				else
					triplesUnmatched.add(new Triple(ResourceFactory.createResource(extensionRNS+source+"entity_"+localName).asNode(), 
							newPred.asNode(), newObj.asNode()));

			}
			/** if the class of yago is provided, then make an unmatched entity instance of the provided class */
			if(yagoEnt == null && yagoClass != null)
				triplesUnmatched.add(new Triple(ResourceFactory.createResource(extensionRNS+source+"entity_"+localName).asNode(),
					type.asNode(), ResourceFactory.createResource(yagoNS+yagoClass).asNode()));
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
