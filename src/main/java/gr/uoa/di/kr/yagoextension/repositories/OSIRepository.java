package gr.uoa.di.kr.yagoextension.repositories;

import gr.uoa.di.kr.yagoextension.model.OSIEntity;
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

class OSIRepository extends Repository<OSIEntity> implements RDFReader {

  OSIRepository(String path) {
    super(path);
  }

  @Override
  public void read() {
    this.readRDF();
  }

  @Override
  public void readRDF() {

    String label = RDFVocabulary.LABEL;
    String hasGeometry = RDFVocabulary.HAS_GEOMETRY;
    String asWkt = RDFVocabulary.AS_WKT;
    String type = RDFVocabulary.TYPE;

    WKTReader wktReader = new WKTReader();
    Model osi = RDFDataMgr.loadModel(this.inputFile);
    ResIterator subjects = osi.listSubjects();

    /* iterate over the subjects of the input rdf file */
    while(subjects.hasNext()) {

      Resource subject = subjects.next();
      String subjectURI = subject.getURI();
      Set<String> labels = new HashSet<>();
      String division = null;
      String wkt = null;
      StmtIterator subjectStmts = osi.listStatements(subject, null, (RDFNode) null);

      while(subjectStmts.hasNext()) {
        Statement stmt = subjectStmts.next();
        String predicate = stmt.getPredicate().getURI();
        RDFNode object = stmt.getObject();
        if(predicate.equals(label))
          labels.add(object.asLiteral().getString());
        else if(predicate.equals(type))
          division = "OSI_"+object.asResource().getLocalName();
        else if(predicate.equals(hasGeometry)) {
          wkt = osi.listObjectsOfProperty(object.asResource(), ResourceFactory.createProperty(asWkt))
            .next().asLiteral().getString();
        }

      }
      if(labels.size() == 0)
        continue;
      /* id is not defined explicitly in the dataset -> use local name */
      String osiID = subjectURI.substring(subjectURI.lastIndexOf("/")+1);

      /* create a new entity and add it to the repository */
      try {
        entities.add(new OSIEntity(subjectURI, labels, wktReader.read(wkt), osiID, division));
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

  }

  @Override
  public void generate(Map<String, String> matches, OutputStream datasetFile) {

    Model dataset = ModelFactory.createDefaultModel();
    Set<String> matchedEntities = matches.keySet();

    Property name = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasOSI_Name");
    Property id = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasOSI_ID");
    Property type = ResourceFactory.createProperty(RDFVocabulary.TYPE);
    Property hasGeometry = ResourceFactory.createProperty(RDFVocabulary.HAS_GEOMETRY);
    Property asWKT = ResourceFactory.createProperty(RDFVocabulary.AS_WKT);

    for(OSIEntity osiEntity : this.entities) {
      Resource subject;
      if(matchedEntities.contains(osiEntity.getURI()))
        subject = ResourceFactory.createResource(matches.get(osiEntity.getURI()));
      else
        subject = ResourceFactory.createResource(YAGO2geoVocabulary.RESOURCE+"osientity_"+osiEntity.getOsiID());
      Resource geometry = ResourceFactory.createResource(YAGO2geoVocabulary.RESOURCE+"Geometry_osi_"+osiEntity.getOsiID());
      dataset.add(subject, type, ResourceFactory.createResource(YAGO2geoVocabulary.ONTOLOGY+osiEntity.getDivision()));
      dataset.add(subject, id, ResourceFactory.createTypedLiteral(osiEntity.getOsiID()));
      osiEntity.getLabels().forEach(label -> dataset.add(subject, name, ResourceFactory.createStringLiteral(label)));
      dataset.add(subject, hasGeometry, geometry);
      dataset.add(geometry, asWKT, ResourceFactory.createPlainLiteral(osiEntity.getGeometry().toText()));
    }
    RDFDataMgr.write(datasetFile, dataset, RDFFormat.TURTLE_BLOCKS);
  }

}
