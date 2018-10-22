package gr.uoa.di.kr.yagoextension.structures;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GeometryMatchesStructure extends MatchesStructure {
	
	private Map<String, Double> distances;
	
	public GeometryMatchesStructure() {
		super();
		distances = new HashMap<String, Double>();
	}
	
	public void addMatch(String key, String value, double dist) {
		/** if the key is already in use, update the list if the ratio is same or better */
		if(distances.containsKey(key)) {
			if(dist < distances.get(key)) {
				distances.put(value, dist);
				matchesMap.put(value, new ArrayList<String>(Arrays.asList(key)));
			}
			else if(dist == distances.get(value))
				matchesMap.get(value).add(key);
		}
		else {
			distances.put(value, dist);
			matchesMap.put(value, new ArrayList<String>(Arrays.asList(key)));
		}
	}

}
