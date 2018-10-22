package gr.uoa.di.kr.yagoextension.writers;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import gr.uoa.di.kr.yagoextension.structures.MatchesStructure;

public class MatchesWriter {
	
	private String outputFile;
	private MatchesStructure matches;
	
	public MatchesWriter(String path, MatchesStructure m) {
		this.outputFile = path;
		this.matches = m;
	}

	public void write() throws FileNotFoundException, UnsupportedEncodingException {
		
		String prefix = "http://yago-knowledge.org/resource/";
		PrintWriter out = new PrintWriter(outputFile, "UTF-8");
		Model matchesModel = ModelFactory.createDefaultModel();
		/** iterate over the matches and store them into a jena model */
		for(String x : matches.getKeys()) {
			Resource subj = ResourceFactory.createResource(x);
			Resource obj = ResourceFactory.createResource(prefix+matches.getValueByKey(x).get(0).replace("<","").replace(">", "")); // remove < and > that are read from tsv files
			Property pred = ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#sameAs");
			Statement s = ResourceFactory.createStatement(subj, pred, obj);
			matchesModel.add(s);
		}
		/** write the jena model to the output file */
		matchesModel.write(out, "NTRIPLES");
		out.close();
	}

}
