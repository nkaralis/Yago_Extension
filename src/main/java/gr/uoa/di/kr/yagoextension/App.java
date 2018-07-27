package gr.uoa.di.kr.yagoextension;

/**
 * This class is part of the Yago Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gr.uoa.di.kr.yagoextension.filters.GeometryDistance;
import gr.uoa.di.kr.yagoextension.filters.LabelSimilarity;
import gr.uoa.di.kr.yagoextension.readers.*;
import gr.uoa.di.kr.yagoextension.structures.Entity;
import gr.uoa.di.kr.yagoextension.structures.MatchesStructure;
import gr.uoa.di.kr.yagoextension.writers.*;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class App {
	
	private static String mode;
	private static Reader yago;
	private static Reader datasource;	
	private static String outputFile;
	private static int threads = 1;
	final static Logger logger = LogManager.getLogger(App.class);	
	
	public static void main( String[] args ) {
				
		parseArgs(args);
		if(mode.equals("matching")) {
			logger.info("Starting Matching phase");
			match();
		}
		
	}
	
	private static void usage() {
		System.out.println("---Yago Extension---");
		System.out.println("-----Arguments------");
		System.out.println();
		System.out.println("matching");
		System.out.println("\t--yago=<path_to_yago_file> (formats:tsv, ttl, nt)");
		System.out.println("\t--datasource=<path_to_dataset_file> (e.g. GADM, OSM) (formats:tsv, ttl, nt)");
		System.out.println("\t--threads=<number_of_threads> (default=1)");
		System.out.println("\t--output=<path_to_output_file>");
		System.out.println("generation");
		System.out.println("\t--matches=<path_to_matches_file>");
		System.out.println("\t--output=<path_to_matches_file>");
		System.out.println();
		System.out.println("Example: yago_extension matching --yago=geoclass_first-order_administrative_division.ttl "
				+ "--datasource=gadm_admLevel1.nt --output=1level_matches.ttl --threads=4");
		System.exit(0);
	}
	
	private static void parseArgs(String args[]) {
		if(args.length < 1)
			usage();
		mode = args[0];
		if(mode.equals("matching")) {
			for(int i = 1; i < args.length; i++) {
				/** yago */
				String value = args[i].split("=")[1];
				if(args[i].contains("--yago")) {
					if(value.contains(".ttl") || value.contains(".nt")){
						yago = new RDFReader(value);
					}
					else if(value.contains(".tsv")) {
						yago = new TSVReader(value);
					}
					else
						usage();
				}
				/** gadm, osm, etc. */
				else if(args[i].contains("--datasource")) {
					if(value.contains(".ttl") || value.contains(".nt")){
						datasource = new RDFReader(value);
					}
					else if(value.contains(".tsv")) {
						datasource = new TSVReader(value);
					}
					// TO-DO shapefiles
					else 
						usage();
				}
				/** output */
				else if(args[i].contains("--output")) 
					outputFile = value;
				/** threads */
				else if(args[i].contains("--threads"))
					threads = Integer.parseInt(value);
				else
					usage();
			}
		}
		else {
			usage();
			System.exit(0);
		}
				
	}

	private static void match() {
		try {
			logger.info("Started reading data");
			yago.read();
			datasource.read();
			Map<String, Entity> yagoEntities = yago.getEntities();
			Map<String, Entity> dsEntities = datasource.getEntities();
			logger.info("Finished reading data");
			logger.info("Number of Yago Entities: "+yagoEntities.size());
			logger.info("Number of Datasource Entities: "+dsEntities.size());
			LabelSimilarity ls = new LabelSimilarity(new ArrayList<Entity>(yagoEntities.values()), new ArrayList<Entity>(dsEntities.values()), threads);
			MatchesStructure labelMatches = ls.run();
			logger.info("Finished Label Similarity Filter");
			logger.info("Number of Label Similarity Matches: "+labelMatches.size());
			MatchesStructure geomMatches = GeometryDistance.filter(labelMatches, yagoEntities, dsEntities);
			logger.info("Finished Geometry Distance Filter");
			logger.info("Number of Matches: "+geomMatches.size());
			Writer matchesWriter = new MatchesWriter(outputFile, geomMatches);
			matchesWriter.write();
			
		} catch (InterruptedException | FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}		
	}
	
	private static void datasetGeneration() {
		
	}
	
}
