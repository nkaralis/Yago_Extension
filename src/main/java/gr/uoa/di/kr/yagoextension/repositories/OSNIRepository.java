package gr.uoa.di.kr.yagoextension.repositories;

import gr.uoa.di.kr.yagoextension.model.OSNIEntity;
import gr.uoa.di.kr.yagoextension.readers.RDFReader;
import gr.uoa.di.kr.yagoextension.vocabulary.RDFVocabulary;
import gr.uoa.di.kr.yagoextension.vocabulary.YAGO2geoVocabulary;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class OSNIRepository extends Repository<OSNIEntity> implements RDFReader {

  OSNIRepository(String path) {
    super(path);
  }

  @Override
  public void read() {
    this.readRDF();
  }

  @Override
  public void readRDF() {

    String id = "http://www.nidirect.gov.uk/ontology/hasOSNI_ID";
    String label = "http://www.nidirect.gov.uk/ontology/hasOSNI_Name";
    String divisionProperty = "http://www.nidirect.gov.uk/ontology/hasOSNI_Class";
    String areaProperty = "http://www.nidirect.gov.uk/ontology/hasOSNI_Area";
    String areaSqKmProperty = "http://www.nidirect.gov.uk/ontology/hasOSNI_AreaSqKM";
    String perimeterProperty = "http://www.nidirect.gov.uk/ontology/hasOSNI_Perimeter";
    String hasGeometry = RDFVocabulary.HAS_GEOMETRY;
    String asWkt = RDFVocabulary.AS_WKT;

    WKTReader wktReader = new WKTReader();
    Model osni = RDFDataMgr.loadModel(this.inputFile);
    ResIterator subjects = osni.listSubjects();
    /* iterate over the subjects of the input rdf file */
    while(subjects.hasNext()) {

      Resource subject = subjects.next();
      String subjectURI = subject.getURI();
      Set<String> labels = new HashSet<>();
      String osniID = null;
      String division = null;
      Double area = null;
      Double areasqkm = null;
      Double perimeter = null;
      String wkt = null;
      StmtIterator subjectStmts = osni.listStatements(subject, null, (RDFNode) null);

      /* get the information that is available for the current entity */
      while (subjectStmts.hasNext()) {
        Statement stmt = subjectStmts.next();
        String predicate = stmt.getPredicate().getURI();
        RDFNode object = stmt.getObject();
        if(predicate.equals(label))
          labels.add(object.asLiteral().getString());
        else if(predicate.equals(id))
          osniID = object.asLiteral().getString();
        else if(predicate.equals(asWkt))
          wkt = object.asLiteral().getString();
        else if(predicate.equals(areaSqKmProperty))
          areasqkm = object.asLiteral().getDouble();
        else if(predicate.equals(areaProperty))
          area = object.asLiteral().getDouble();
        else if(predicate.equals(perimeterProperty))
          perimeter = object.asLiteral().getDouble();
        else if(predicate.equals(divisionProperty))
          division = "OSNI_" + object.asLiteral().getString().replace(" ", "_");
        else if(predicate.equals(hasGeometry)) {
          wkt = osni.listObjectsOfProperty(object.asResource(), ResourceFactory.createProperty(asWkt))
            .next().asLiteral().getString().replace("<http://www.opengis.net/def/crs/EPSG/0/4326>", "");
        }
      }
      if(labels.size() == 0)
        continue;
      /* create a new entity and add it to the repository */
      try {
        entities.add(new OSNIEntity(subjectURI, labels, wktReader.read(wkt), osniID, division, area, areasqkm, perimeter));
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

  }

  @Override
  public void generate(Map<String, String> matches, OutputStream datasetFile) {

    Model dataset = ModelFactory.createDefaultModel();
    Set<String> matchedEntities = matches.keySet();

    Property area = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasOSNI_Area");
    Property areasqkm = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasOSNI_AreaSqKm");
    Property perimeter = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasOSNI_Perimeter");
    Property type = ResourceFactory.createProperty(RDFVocabulary.TYPE);
    Property name = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasONSI_Name");
    Property id = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasOSNI_ID");
    Property hasGeometry = ResourceFactory.createProperty(RDFVocabulary.HAS_GEOMETRY);
    Property asWKT = ResourceFactory.createProperty(RDFVocabulary.AS_WKT);

    for(OSNIEntity osniEntity : this.entities) {
      Resource subject;
      if(matchedEntities.contains(osniEntity.getURI()))
        subject = ResourceFactory.createResource(matches.get(osniEntity.getURI()));
      else
        subject = ResourceFactory.createResource(YAGO2geoVocabulary.RESOURCE+"osnientity_"+osniEntity.getOsniID());
      Resource geometry = ResourceFactory.createResource(YAGO2geoVocabulary.ONTOLOGY+"Geometry_osni_"+osniEntity.getOsniID());
      if(osniEntity.getArea() != null)
        dataset.add(subject, area, ResourceFactory.createTypedLiteral(osniEntity.getArea()));
      if(osniEntity.getAreasqkm() != null)
        dataset.add(subject, areasqkm, ResourceFactory.createTypedLiteral(osniEntity.getAreasqkm()));
      if(osniEntity.getPerimeter() != null)
        dataset.add(subject, perimeter, ResourceFactory.createTypedLiteral(osniEntity.getPerimeter()));
      dataset.add(subject, id, ResourceFactory.createTypedLiteral(osniEntity.getOsniID()));
      osniEntity.getLabels().forEach(label -> dataset.add(subject, name, ResourceFactory.createStringLiteral(label)));
      dataset.add(subject, type, ResourceFactory.createResource(YAGO2geoVocabulary.ONTOLOGY+osniEntity.getDivision()));
      dataset.add(subject, hasGeometry, geometry);
      dataset.add(geometry, asWKT, ResourceFactory.createPlainLiteral(osniEntity.getGeometry().toText()));
    }
    RDFDataMgr.write(datasetFile, dataset, RDFFormat.TURTLE_BLOCKS);
  }
}
