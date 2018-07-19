package gr.uoa.di.kr.yagoextension.readers;

/**
 * This class is part of the Yago Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;

public class RDFReader extends Reader {

	public RDFReader(String path) {
		super(path);
		if(inputFile.contains(".ttl"))
			type = "Turtle";
		else if(inputFile.contains(".nt"))
			type = "N-Triples";
	}
	
	public void read() {
		/** create the Model and read the data */	
	  Model model = RDFDataMgr.loadDataset(inputFile).getDefaultModel();
		/** extract the labels of each resource */
	  Property labelProp = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
	  if(!model.contains(null, labelProp))
	  	labelProp = ResourceFactory.createProperty("http://www.app-lab.eu/gadm/ontology/hasName");
	  StmtIterator labelIter = model.listStatements(null, labelProp, null, null);
	  /** save (entity, label) pairs in the LabelStructure */
	  while(labelIter.hasNext()) {
	  	Statement triple = labelIter.next();
	  	String entity = triple.getSubject().getURI().split("/")[4];
	  	String label = triple.getLiteral().getString();
	  	entLabelPairs.addPair(entity, label);
	  }
	}
}
