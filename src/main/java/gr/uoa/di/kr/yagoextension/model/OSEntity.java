package gr.uoa.di.kr.yagoextension.model;

import org.locationtech.jts.geom.Geometry;

import java.util.Set;

public class OSEntity extends Entity {

  private String osID;
  private String description;
  private String areaCode;

  public OSEntity(String id, Set<String> labels, Geometry geom, String osID, String desc, String code) {
    super(id, labels, geom);
    this.osID = osID;
    this.description = desc;
    this.areaCode = code;
  }

  public String getOsID() {
    return osID;
  }

  public String getDescription() {
    return description;
  }

  public String getAreaCode() {
    return areaCode;
  }
}
