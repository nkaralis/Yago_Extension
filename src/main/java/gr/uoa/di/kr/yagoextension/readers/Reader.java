package gr.uoa.di.kr.yagoextension.readers;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.util.Map;
import gr.uoa.di.kr.yagoextension.structures.Entity;
import java.util.HashMap;

public abstract class Reader {
	
	protected String inputFile;
	protected Map<String, Entity> entities;
	
	Reader(String path) {
		inputFile = path;
		entities = new HashMap<String, Entity>();
	}
		
	public Map<String, Entity> getEntities() {
		return entities;
	}

	abstract public void read();
	
}
