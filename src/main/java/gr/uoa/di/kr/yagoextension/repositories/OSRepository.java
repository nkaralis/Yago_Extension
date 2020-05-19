package gr.uoa.di.kr.yagoextension.repositories;

import gr.uoa.di.kr.yagoextension.model.*;
import gr.uoa.di.kr.yagoextension.vocabulary.RDFVocabulary;
import gr.uoa.di.kr.yagoextension.vocabulary.YAGO2geoVocabulary;
import org.apache.jena.riot.RDFFormat;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import gr.uoa.di.kr.yagoextension.readers.RDFReader;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class OSRepository extends Repository<OSEntity> implements RDFReader {

  OSRepository(String path) {
    super(path);
  }

  @Override
  public void read() {
    this.readRDF();
  }

  @Override
  public void readRDF() {

    String id = "http://data.ordnancesurvey.co.uk/ontology/hasOS_ID";
    String label = "http://data.ordnancesurvey.co.uk/ontology/hasOS_Name";
    String descriptionProperty = "http://data.ordnancesurvey.co.uk/ontology/hasOS_Description";
    String areaCodeProperty = "http://data.ordnancesurvey.co.uk/ontology/hasOS_AreaCode";
    String asWkt = RDFVocabulary.AS_WKT;

    WKTReader wktReader = new WKTReader();
    Model os = RDFDataMgr.loadModel(this.inputFile);
    ResIterator subjects = os.listSubjects();
    /* iterate over the subjects of the input rdf file */
    while(subjects.hasNext()) {

      Resource subject = subjects.next();
      String subjectURI = subject.getURI();
      Set<String> labels = new HashSet<>();
      String osID = null;
      String description = null;
      String areaCode = null;
      String wkt = null;
      StmtIterator subjectStmts = os.listStatements(subject, null, (RDFNode) null);

      while(subjectStmts.hasNext()) {
        Statement stmt = subjectStmts.next();
        String predicate = stmt.getPredicate().getURI();
        String object = stmt.getObject().asLiteral().getString();
        if(predicate.equals(label))
          labels.add(object);
        else if(predicate.equals(descriptionProperty))
          description = "OS_"+object.replace(" ","");
        else if(predicate.equals(id))
          osID = object;
        else if(predicate.equals(areaCodeProperty))
          areaCode = object;
        else if(predicate.equals(asWkt))
          wkt = object.replace("<http://www.opengis.net/def/crs/EPSG/0/4326>", "");
      }
      if(labels.size() == 0)
        continue;

      /* create a new entity and add it to the repository */
      try {
        entities.add(new OSEntity(subjectURI, labels, wktReader.read(wkt), osID, description, areaCode));
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

  }

  @Override
  public void generate(Map<String, String> matches, OutputStream datasetFile) {

    Model matched = ModelFactory.createDefaultModel();
    Set<String> matchedEntities = matches.keySet();

    Property areaCode = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasOS_AreaCode");
    Property name = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasOS_Name");
    Property id = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasOS_ID");
    Property type = ResourceFactory.createProperty(RDFVocabulary.TYPE);
    Property hasGeometry = ResourceFactory.createProperty(RDFVocabulary.HAS_GEOMETRY);
    Property asWKT = ResourceFactory.createProperty(RDFVocabulary.AS_WKT);

    for(OSEntity osEntity : this.entities) {
      Resource yagoEntity;
      if(matchedEntities.contains(osEntity.getURI()))
        yagoEntity = ResourceFactory.createResource(matches.get(osEntity.getURI()));
      else
        yagoEntity = ResourceFactory.createResource(YAGO2geoVocabulary.RESOURCE+"osentity_"+osEntity.getOsID());
      Resource geometry = ResourceFactory.createResource(YAGO2geoVocabulary.RESOURCE+"Geometry_os_"+osEntity.getOsID());
      matched.add(yagoEntity, type, ResourceFactory.createResource(YAGO2geoVocabulary.ONTOLOGY+osEntity.getDescription()));
      matched.add(yagoEntity, areaCode, ResourceFactory.createTypedLiteral(osEntity.getAreaCode()));
      matched.add(yagoEntity, id, ResourceFactory.createTypedLiteral(osEntity.getOsID()));
      osEntity.getLabels().forEach(label -> matched.add(yagoEntity, name, ResourceFactory.createStringLiteral(label)));
      matched.add(yagoEntity, hasGeometry, geometry);
      matched.add(geometry, asWKT, ResourceFactory.createPlainLiteral(osEntity.getGeometry().toText()));
    }
    RDFDataMgr.write(datasetFile, matched, RDFFormat.TURTLE_BLOCKS);
  }

}
