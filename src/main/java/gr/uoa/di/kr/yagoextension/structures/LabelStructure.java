package gr.uoa.di.kr.yagoextension.structures;

/**
 * This class is part of the Yago Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LabelStructure {
	
	private Map<String, List<String>> labelsMap;
	
	public LabelStructure() {
		labelsMap = new HashMap<String, List<String>>();
	}
	
	public void addPair(String key, String value) {
		if(!labelsMap.containsKey(key))
			labelsMap.put(key, new ArrayList<String>(Arrays.asList(value)));
		else
			labelsMap.get(key).add(value);
	}
	
	public Iterator<String> getKeys() {
		return labelsMap.keySet().iterator();
	}
	
	public List<String> getValueByKey(String key) {
		return labelsMap.get(key);
	}
	
	public int size(){
		return labelsMap.size();
	}

}
