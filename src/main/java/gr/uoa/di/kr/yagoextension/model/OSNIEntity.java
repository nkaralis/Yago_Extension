package gr.uoa.di.kr.yagoextension.model;

import org.locationtech.jts.geom.Geometry;

import java.util.Set;

public class OSNIEntity extends Entity {

  private String osniID;
  private String division;
  private Double area;
  private Double areasqkm;
  private Double perimeter;

  public OSNIEntity(String id, Set<String> labels, Geometry geom, String osniID, String div, Double area, Double areasqkm, Double perimeter) {
    super(id, labels, geom);
    this.osniID = osniID;
    this.division = div;
    this.area = area;
    this.areasqkm = areasqkm;
    this.perimeter = perimeter;
  }

  public String getOsniID() {
    return osniID;
  }

  public String getDivision() {
    return division;
  }

  public Double getArea() {
    return area;
  }

  public Double getAreasqkm() {
    return areasqkm;
  }

  public Double getPerimeter() {
    return perimeter;
  }
}
