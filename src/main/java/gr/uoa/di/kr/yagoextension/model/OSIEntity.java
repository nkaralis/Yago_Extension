package gr.uoa.di.kr.yagoextension.model;

import org.locationtech.jts.geom.Geometry;

import java.util.Set;

public class OSIEntity extends Entity {

  private String osiID;
  private String division;

  public OSIEntity(String id, Set<String> labels, Geometry geom, String osiID, String division) {
    super(id, labels, geom);
    this.osiID = osiID;
    this.division = division;
  }

  public String getOsiID() {
    return osiID;
  }

  public String getDivision() {
    return division;
  }
}
