package gr.uoa.di.kr.yagoextension.model;


import java.util.*;

public class LabelMatches {

	private Map<Entity, List<Entity>> matches;
	private Map<Entity, Double> ratios;

	public LabelMatches() {
		this.matches = new HashMap<>();
		ratios = new HashMap<>();
	}

	public Set<Entity> getKeys() {
		return matches.keySet();
	}

	public List<Entity> getValueByKey(Entity key) {
		return matches.get(key);
	}
	
	public void addMatch(Entity key, Entity value, double ratio) {
		/* if the key is already in use: update the list if the ratio is same or better */
		if(ratios.containsKey(key)) {
			if(ratio > ratios.get(key)) {
				ratios.put(key, ratio);
				matches.put(key, new ArrayList<>(Collections.singletonList(value)));
			}
			else if(ratio == ratios.get(key))
				matches.get(key).add(value);
		}
		else {
			ratios.put(key, ratio);
			matches.put(key, new ArrayList<>(Collections.singletonList(value)));
		}
	}

	public int size() {
		return matches.size();
	}
	
}
