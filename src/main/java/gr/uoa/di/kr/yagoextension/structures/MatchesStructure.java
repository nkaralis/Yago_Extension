package gr.uoa.di.kr.yagoextension.structures;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class MatchesStructure {
	
	protected Map<String, List<String>> matchesMap;
	
	public MatchesStructure() {
		matchesMap = new HashMap<String, List<String>>();
	}
	
	public abstract void addMatch(String key, String value, double d);
	
	public Set<String> getKeys() {
		return matchesMap.keySet();
	}
	
	public Iterator<String> getKeysIterator() {
		return matchesMap.keySet().iterator();
	}
	
	public List<String> getValueByKey(String key) {
		return matchesMap.get(key);
	}
	
	public int size(){
		return matchesMap.size();
	}

}
