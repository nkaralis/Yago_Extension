package gr.uoa.di.kr.yagoextension.repositories;

import java.io.OutputStream;
import java.util.HashSet;
import gr.uoa.di.kr.yagoextension.model.Entity;

import java.util.Map;
import java.util.Set;

public abstract class Repository<T extends Entity> {
	
	protected String inputFile;
	protected Set<T> entities;
	
	Repository(String path) {
		inputFile = path;
		entities = new HashSet<>();
	}
		
	public Set<T> getEntities() {
		return entities;
	}

	public abstract void read();

	public abstract void generate(Map<String, String> matches, OutputStream datasetFile);

}
