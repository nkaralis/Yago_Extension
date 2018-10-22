package gr.uoa.di.kr.yagoextension.readers;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import com.vividsolutions.jts.io.ParseException;
import gr.uoa.di.kr.yagoextension.structures.Entity;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class TSVReader extends Reader {
	
	public TSVReader(String path) {
		super(path);
	}

	public void read() {
		
		/** prepare data */
		File tsvFile = new File(inputFile);
		Map<String, List<String>> labelsMap = new HashMap<String, List<String>>();
		Map<String, String> latiMap = new HashMap<String, String>();
		Map<String, String> longiMap = new HashMap<String, String>();
		
		try {
			CSVFormat csvFileFormat = CSVFormat.TDF.withQuote(null);
			CSVParser parser = CSVParser.parse(tsvFile, StandardCharsets.UTF_8, csvFileFormat);
			for(CSVRecord x : parser.getRecords()) {
				if(x.get(1).contains("label")) {
					String label = x.get(2).substring(x.get(2).indexOf("\"")+1, x.get(2).lastIndexOf("\"")); // keep the part that is between the quotes
					if(labelsMap.containsKey(x.get(0)))
						labelsMap.get(x.get(0)).add(label);
					else
						labelsMap.put(x.get(0), new ArrayList<String>(Arrays.asList(label)));
				}
				else if(x.get(1).contains("Latitude")){
					latiMap.put(x.get(0), x.get(3));
				}
				else if(x.get(1).contains("Longitude"))
					longiMap.put(x.get(0), x.get(3));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/** create new entities */
		for(String key : latiMap.keySet()) {
			if(labelsMap.get(key) == null) continue;
			try {
				entities.put(key, new Entity(key, labelsMap.get(key), latiMap.get(key), longiMap.get(key)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

}
