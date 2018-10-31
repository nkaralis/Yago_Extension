package gr.uoa.di.kr.yagoextension;

/**
 * This class is part of the YAGO Extension Project
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
import gr.uoa.di.kr.yagoextension.util.Blacklist;
import gr.uoa.di.kr.yagoextension.writers.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class App {
	
	private static String mode;
	private static Reader yago;
	private static Reader datasource;
	private static String data;
	private static String outputMatches;
	private static String outputMatched;
	private static String outputUnmatched;
	private static String matchesFile;
	private static String origin;
	private static int threads = 1;
	private static String preprocess = null;
	private static String blacklist = null;
	private static Reader extendedKG;
	private static String outputTopology;
	final static Logger logger = LogManager.getLogger(App.class);
	
	public static void main( String[] args ) {
		
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF); // suppress Jena's log4j WARN messages 
		parseArgs(args);
		if(mode.equals("matching")) 
			match();
		else if(mode.equals("generation"))
			datasetGeneration();
		else if(mode.equals("topology"))
			generateTopologicalRelations();

	}
	
	private static void usage() {
		
		System.out.println("---Yago Extension---");
		System.out.println("-----Arguments------");
		System.out.println();
		System.out.println("matching");
		System.out.println("\t--yago=<path_to_yago_file> (formats: tsv, ttl, nt)");
		System.out.println("\t--datasource=<path_to_dataset_file> (e.g., GADM, OSM) (formats: tsv, ttl, nt)");
		System.out.println("\t--threads=<number_of_threads> (default=1)");
		System.out.println("\t--output=<path_to_output_file>");
		System.out.println("\t--preprocess=<data_source> (OPTIONAL) (options: kallikratis, os, osi)");
		System.out.println("\t--blacklist=<matches_file> (OPTIONAL)");
		System.out.println("generation");
		System.out.println("\t--matches=<path_to_matches_file>");
		System.out.println("\t--data=<path_to_dataset_file> (e.g. GADM, OSM) (formats: ttl, nt)");
		System.out.println("\t--matched=<path_to_outputfile_matched>");
		System.out.println("\t--unmatched=<path_to_outputfile_unmatched>");
		System.out.println("\t--origin=<datasource> (e.g., GADM, OSM)");
		System.out.println();
		System.out.println("Example: yago_extension matching --yago=geoclass_first-order_administrative_division.ttl "
				+ "--datasource=gadm_admLevel1.nt --output=1level_matches.ttl --threads=4");
		System.exit(0);
	}
	
	private static void parseArgs(String args[]) {
		
		if(args.length < 1)
			usage();
		mode = args[0];
		/** matching mode */
		if(mode.equals("matching")) {
			if (args.length < 4)
				usage();
			for(int i = 1; i < args.length; i++) {
				/** yago */
				String value = args[i].split("=")[1];
				if(args[i].contains("--yago")) {
					if(value.contains(".ttl") || value.contains(".nt") || value.contains(".n3")){
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
					if(value.contains(".ttl") || value.contains(".nt") || value.contains(".n3")){
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
					outputMatches = value;
				/** threads */
				else if(args[i].contains("--threads"))
					threads = Integer.parseInt(value);
				/** preprocess */
				else if(args[i].contains("--preprocess"))
					preprocess = value;
				else if(args[i].contains("--blacklist"))
					blacklist = value;
				else
					usage();
			}
		}
		/** generation mode */
		else if(mode.equals("generation")) {
			if (args.length < 5)
				usage();
			for(int i = 1; i < args.length; i++) {
				String value = args[i].split("=")[1];
				if(args[i].contains("--matches"))
					matchesFile = value;
				else if(args[i].contains("--matched"))
					outputMatched = value;
				else if(args[i].contains("--unmatched"))
					outputUnmatched = value;
				else if(args[i].contains("--data"))
					data = value;
				else if(args[i].contains("--origin"))
					origin = value;
				else
					usage();
			}
		}
		/** topology mode */
		else if(mode.equals("topology")) {
			for(int i = 1; i < args.length; i++) {
				String value = args[i].split("=")[1];
				if(args[i].contains("--kg"))
					extendedKG = new RDFReader(value);
				else if(args[i].contains("--output"))
					outputTopology = value;
			}
		}
		else {
			usage();
			System.exit(0);
		}
				
	}

	private static void match() {
		
		try {
			System.setProperty("org.geotools.referencing.forceXY", "true"); // force (long lat) in geotools 
			logger.info("Matching phase");
			logger.info("Started reading data");
			yago.read();
			datasource.read();
			Map<String, Entity> yagoEntities = yago.getEntities();
			Map<String, Entity> dsEntities = datasource.getEntities();
			logger.info("Finished reading data");
			/** remove entities that are already matched */
			if(blacklist != null)
				Blacklist.removeMatchedEntities(yagoEntities, dsEntities, blacklist);
			logger.info("Number of Yago Entities: "+yagoEntities.size());
			logger.info("Number of Datasource Entities: "+dsEntities.size());
			LabelSimilarity ls = new LabelSimilarity(
					new ArrayList<Entity>(yagoEntities.values()), new ArrayList<Entity>(dsEntities.values()), threads, preprocess);
			MatchesStructure labelMatches = ls.run();
			logger.info("Finished Label Similarity Filter");
			logger.info("Number of Label Similarity Matches: "+labelMatches.size());
			MatchesStructure geomMatches = GeometryDistance.filter(labelMatches, yagoEntities, dsEntities);
			logger.info("Finished Geometry Distance Filter");
			logger.info("Number of Matches: "+geomMatches.size());
			MatchesWriter matchesWriter = new MatchesWriter(outputMatches, geomMatches);
			matchesWriter.write();
			
		} catch (InterruptedException | FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(2);
		}		
	}
	
	private static void datasetGeneration() {
		
		logger.info("Generating new Knowledge Graphs");
		DatasetWriter ds = new DatasetWriter(outputMatched, outputUnmatched, matchesFile, data, origin);
		try {
			ds.write();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
		logger.info("Generated new Knowledge Graphs");
	}
	
	private static void generateTopologicalRelations() {
		
		logger.info("Generation of Topological Relations");
		logger.info("Started reading data");
		extendedKG.read();
		logger.info("Generating Topological Relations");
		Map<String, Entity> extEntities = extendedKG.getEntities();
		TopologicalRelationsWriter topoWriter = new TopologicalRelationsWriter(extEntities, outputTopology);
		try {
			topoWriter.write();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(2);
		}
		logger.info("Generated Topological Relations");
	}
	
}
