package gr.uoa.di.kr.yagoextension.structures;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class Entity {
	
	private String id;
	private Set<String> labels;
	private Geometry geom;
	private WKTReader wktReader;
	
	public Entity(String id,  List<String> labels, String wkt) throws ParseException {
		this.wktReader = new WKTReader(); 
		this.id = id;
		this.geom = this.wktReader.read(wkt.replace("<http://www.opengis.net/def/crs/EPSG/0/4326>", ""));
		this.labels = new HashSet<String>();
		for (String x : labels) this.labels.add(x);
	}
	
	public Entity(String id, List<String> labels, String latitude, String longitude) throws ParseException {
		this.wktReader = new WKTReader();
		this.id = id;
		this.geom = this.wktReader.read("POINT( "+longitude+" "+latitude+" )");
		this.labels = new HashSet<String>();
		for (String x : labels) this.labels.add(x);
	}
	
	public Set<String> getLabels() {
		return labels;
	}
	
	public Geometry getGeometry() {
		return geom;
	}
	
	public String getID() {
		return id;
	}
	

}
