package gr.uoa.di.kr.yagoextension.model;

import org.locationtech.jts.geom.Geometry;

import java.util.Set;

public class GAGEntity extends Entity {

  private Integer population;
  private Integer gagID;
  private String division;
  private String belongsTo;


  public GAGEntity(String id, Set<String> labels, Geometry geom, Integer population, Integer gagID, String division, String belongsTo) {
    super(id, labels, geom);
    this.population = population;
    this.gagID = gagID;
    this.division = division;
    this.belongsTo = belongsTo;
  }

  public Integer getPopulation() {
    return population;
  }

  public Integer getGagID() {
    return gagID;
  }

  public String getDivision() {
    return division;
  }

  public String getBelongsTo() {
    return belongsTo;
  }

}
