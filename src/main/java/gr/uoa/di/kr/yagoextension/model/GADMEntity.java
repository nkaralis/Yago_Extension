package gr.uoa.di.kr.yagoextension.model;

import org.locationtech.jts.geom.Geometry;

import java.util.Set;

public class GADMEntity extends Entity {

  private String gadmID;
  private String nationalLevel;
  private String upperLevelUnit;

  public GADMEntity(String id, Set<String> labels, Geometry geom, String gadmID, String level, String upperLevelUnit) {
    super(id, labels, geom);
    this.gadmID = gadmID;
    this.nationalLevel = level;
    this.upperLevelUnit = upperLevelUnit;
  }

  public String getGadmID() {
    return gadmID;
  }

  public String getNationalLevel() {
    return nationalLevel;
  }

  public String getUpperLevelUnit() {
    return upperLevelUnit;
  }
}
