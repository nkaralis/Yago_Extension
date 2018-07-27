package gr.uoa.di.kr.yagoextension.readers;

/**
 * This class is part of the Yago Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import java.util.ArrayList;
import com.vividsolutions.jts.io.ParseException;
import gr.uoa.di.kr.yagoextension.structures.Entity;

public class RDFReader extends Reader {

	public RDFReader(String path) {
		super(path);
	}
		
	public void read() {
				
		/** create the Model and read the data */	
	  Model model = RDFDataMgr.loadModel(inputFile);
	  
	  /** iterate over all subjects */
	  ResIterator subjIter = model.listSubjects();
	  while(subjIter.hasNext()) {
	  	ArrayList<String> subjLabels = new ArrayList<String>();
	  	boolean flag = false;
	    Resource subject = subjIter.next();
	    String subjWKT = null;
	    String lati = null;
	    String longi = null;
	    String subjID = subject.getURI().split("/")[4];
	    /** iterate over the facts of the subject */
	    StmtIterator facts = model.listStatements(subject, null, (RDFNode) null);
	    while(facts.hasNext()) {
	    	Statement triple = facts.next();
	    	String pred = triple.getPredicate().getLocalName();
	    	if(pred.contains("label") || pred.contains("hasName"))
	    		subjLabels.add(triple.getObject().asLiteral().getString());
	    	else if(pred.contains("hasGeometry")) { 
	    		subjWKT = model.listStatements(triple.getObject().asResource(), null, null, null).next().getObject().asLiteral().getString();
	    		flag = true; // create entity if it contains geometry
	    	}
	    	else if(pred.contains("hasLatitude")) {
	    		lati = triple.getObject().asLiteral().getLexicalForm();
	    		flag = true; // create entity if it contains geometry
	    	}
	    	else if(pred.contains("hasLongitude"))
	    		longi = triple.getObject().asLiteral().getLexicalForm();
	    }
	    
	    /** create a new entity */
	    if(flag) {
	    	try {
	    		Entity newEntity;
	    		if(subjWKT == null)
						newEntity = new Entity(subjID, subjLabels, lati, longi);
	    		else
	    			newEntity = new Entity(subjID, subjLabels, subjWKT);
	    		entities.put(subjID, newEntity);
	    	} catch (ParseException e) {
	    		e.printStackTrace();
	    	}
	    }
	  }
	}
}
