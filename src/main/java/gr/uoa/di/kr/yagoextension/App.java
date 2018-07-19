package gr.uoa.di.kr.yagoextension;

/**
 * This class is part of the Yago Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.util.Iterator;

import gr.uoa.di.kr.yagoextension.filters.LabelSimilarity;
import gr.uoa.di.kr.yagoextension.readers.*;
import gr.uoa.di.kr.yagoextension.structures.LabelMatchesStructure;

public class App {
	
	private static String mode;
	private static String yago;
	private static String datasource;	
	
	public static void main( String[] args ) {
		
		if(args.length < 1) {
			usage();
			System.exit(1);
		}
		
		parseArgs(args);
		
		RDFReader yagoRDF = new RDFReader(yago);
		RDFReader dsRDF = new RDFReader(datasource);
		yagoRDF.read();
		dsRDF.read();
		LabelMatchesStructure labelMatches = LabelSimilarity.filter(yagoRDF.getLabels(), dsRDF.getLabels());
		System.out.println(labelMatches.size());
		
	}
	
	private static void usage() {
		System.out.println("Yago Extension");
		System.out.println("First Argument: Yago File");
		System.out.println("Second Argumnte: Datasource file");
	}
	
	private static void parseArgs(String args[]) {
		
		mode = args[0];
		if(mode.equals("matching")) {
			yago = args[1];
			datasource = args[2];
		}
				
	}

	private static void match() {
		
	}
	
	private static void datasetGeneration() {
		
	}
	
}
