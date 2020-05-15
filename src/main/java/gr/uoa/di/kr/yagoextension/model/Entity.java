package gr.uoa.di.kr.yagoextension.model;

import java.util.Set;
import org.locationtech.jts.geom.Geometry;


public class Entity {
	
	private String uri;
	private Set<String> labels;
	private Geometry geom;
	
	public Entity(String id,  Set<String> labels, Geometry geom) {
		this.uri = id;
		this.labels = labels;
		this.geom = geom;
	}
	
	public Set<String> getLabels() {
		return labels;
	}
	
	public Geometry getGeometry() {
		return geom;
	}
	
	public String getURI() {
		return uri;
	}
	

}
