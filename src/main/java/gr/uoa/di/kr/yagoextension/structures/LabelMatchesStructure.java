package gr.uoa.di.kr.yagoextension.structures;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LabelMatchesStructure extends MatchesStructure {
	
	private Map<String, Double> ratios;
	public LabelMatchesStructure() {
		super();
		ratios = new HashMap<String, Double>();
	}
	
	public void addMatch(String key, String value, double ratio) {
		/** if the key is already in use, update the list if the ratio is same or better */
		if(ratios.containsKey(key)) {
			if(ratio > ratios.get(key)) {
				ratios.put(key, ratio);
				matchesMap.put(key, new ArrayList<String>(Arrays.asList(value)));
			}
			else if(ratio == ratios.get(key))
				matchesMap.get(key).add(value);
		}
		else {
			ratios.put(key, ratio);
			matchesMap.put(key, new ArrayList<String>(Arrays.asList(value)));
		}
	}
	
}
