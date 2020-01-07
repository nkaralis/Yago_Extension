package gr.uoa.di.kr.yagoextension.domain;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis
 * kr.di.uoa.gr
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * An object of this class contains all labels for every entity
 * of the given data source (e.g. YAGO, GADM, etc.)
 */

public class LabelStructure {

	private Map<String, List<String>> labelsMap;
	private Set<String> keyPool;

	public LabelStructure() {
		labelsMap = new HashMap<String, List<String>>();
		keyPool = new ConcurrentSkipListSet<String>();
	}

	public void addPair(String key, String value) {
		if(!labelsMap.containsKey(key))
			labelsMap.put(key, new ArrayList<String>(Arrays.asList(value)));
		else {
			labelsMap.get(key).add(value);
			keyPool.add(key);
		}
	}

	public Set<String> getKeys() {
		return labelsMap.keySet();
	}

	public Iterator<String> getKeysIterator() {
		return labelsMap.keySet().iterator();
	}

	public List<String> getValueByKey(String key) {
		return labelsMap.get(key);
	}

	public int size(){
		return labelsMap.size();
	}

}
