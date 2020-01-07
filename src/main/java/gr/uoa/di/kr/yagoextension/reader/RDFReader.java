package gr.uoa.di.kr.yagoextension.reader;

/**
 * This class is part of the YAGO Extension Project
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
import gr.uoa.di.kr.yagoextension.domain.Entity;

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
	  	boolean flagG = false;
	  	boolean flagL = false;
	    Resource subject = subjIter.next();
	    String subjWKT = null;
	    String lati = null;
	    String longi = null;
	    String subjID = subject.getURI();
	    /** iterate over the facts of the subject */
	    StmtIterator facts = model.listStatements(subject, null, (RDFNode) null);
	    while(facts.hasNext()) {
	    	Statement triple = facts.next();
	    	String pred = triple.getPredicate().getLocalName();
	    	if(pred.toLowerCase().contains("label") || pred.toLowerCase().contains("name")){
	    		subjLabels.add(triple.getObject().asLiteral().getString());
	    		flagL = true; // create entity if it has a label and a geometry
	    	}
	    	else if(pred.contains("hasGeometry") && model.listStatements(triple.getObject().asResource(), null, null, null).hasNext()) {
	    		subjWKT = model.listStatements(triple.getObject().asResource(), null, null, null).next().getObject().asLiteral().getString();
	    		flagG = true; // create entity if it has a label and a geometry
	    	}
	    	else if(pred.contains("asWKT")) {
	    		subjWKT = triple.getObject().asLiteral().getString();
	    		flagG = true; // create entity if it has a label and a geometry
	    	}
	    	else if(pred.contains("hasLatitude")) {
	    		lati = triple.getObject().asLiteral().getLexicalForm();
	    		flagG = true; // create entity if it has a label and a geometry
	    	}
	    	else if(pred.contains("hasLongitude"))
	    		longi = triple.getObject().asLiteral().getLexicalForm();
	    }

	    /** create a new entity */
	    if(flagG && flagL) {
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
