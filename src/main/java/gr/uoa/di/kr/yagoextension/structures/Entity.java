package gr.uoa.di.kr.yagoextension.structures;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class Entity {
	
	private String id;
	private Set<String> labels;
	private Geometry geom;
	private WKTReader wktReader;
	
	public Entity(String id,  List<String> labels, String wkt) throws ParseException {
		this.wktReader = new WKTReader(); 
		this.id = id;
		if(wkt.contains("<http://www.opengis.net/def/crs/EPSG/0/4326>")) {
			this.geom = this.wktReader.read(wkt.replace("<http://www.opengis.net/def/crs/EPSG/0/4326>", ""));
		}
		/** transform EPSG:2100 to EPSG:4326 */
		else if(wkt.contains("http://www.opengis.net/def/crs/EPSG/0/2100")) {
			this.geom = this.wktReader.read(wkt);
			try {
				CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:2100");
				CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
				MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
				this.geom = JTS.transform(this.geom, transform);
				this.geom.geometryChanged();
			} catch (FactoryException | MismatchedDimensionException | TransformException e) {
				e.printStackTrace();
				System.err.println("Transformation of the geometry failed!");
			}
		}
		else
			this.geom = this.wktReader.read(wkt);
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
