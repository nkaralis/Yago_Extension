package gr.uoa.di.kr.yagoextension.repositories;

import gr.uoa.di.kr.yagoextension.model.GADMEntity;
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

class GADMRepository extends Repository<GADMEntity>  implements RDFReader {

  GADMRepository(String path) {
    super(path);
  }

  @Override
  public void read() {
    this.readRDF();
  }

  @Override
  public void readRDF() {

    String label = "http://www.app-lab.eu/gadm/ontology/hasGADM_Name";
    String id = "http://www.app-lab.eu/gadm/ontology/hasGADM_ID";
    String division = "http://www.app-lab.eu/gadm/ontology/hasGADM_NationalLevel";
    String upperLevel = "http://www.app-lab.eu/gadm/ontology/hasGADM_UpperLevelUnit";
    String hasGeometry = RDFVocabulary.HAS_GEOMETRY;
    String asWkt = RDFVocabulary.AS_WKT;

    WKTReader wktReader = new WKTReader();
    Model gadm = RDFDataMgr.loadModel(this.inputFile);
    ResIterator subjects = gadm.listSubjects();
    /* iterate over the subjects of the input rdf file */
    while(subjects.hasNext()) {

      Resource subject = subjects.next();
      String subjectURI = subject.getURI();
      Set<String> labels = new HashSet<>();
      String gadmID = null;
      String nationalLevel = null;
      String upperLevelUnit = null;
      String wkt = null;
      StmtIterator subjectStmts = gadm.listStatements(subject, null, (RDFNode) null);

      /* get the information that is available for the current entity */
      while (subjectStmts.hasNext()) {
        Statement stmt = subjectStmts.next();
        String predicate = stmt.getPredicate().getURI();
        RDFNode object = stmt.getObject();
        if(predicate.equals(label))
          labels.add(object.asLiteral().getString());
        else if(predicate.equals(id))
          gadmID = object.asLiteral().getString();
        else if(predicate.equals(asWkt))
          wkt = object.asLiteral().getString();
        else if(predicate.equals(division))
          nationalLevel = "GADM_" + object.asLiteral().getString();
        else if(predicate.equals(upperLevel))
          upperLevelUnit = object.asLiteral().getString();
        else if(predicate.equals(hasGeometry)) {
          wkt = gadm.listObjectsOfProperty(object.asResource(), ResourceFactory.createProperty(asWkt))
            .next().asLiteral().getString().replace("<http://www.opengis.net/def/crs/EPSG/0/4326>", "");
        }
      }
      if(labels.size() == 0)
        continue;
      /* create a new entity and add it to the repository */
      try {
        entities.add(new GADMEntity(subjectURI, labels, wktReader.read(wkt), gadmID, nationalLevel, upperLevelUnit));
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

  }


  @Override
  public void generate(Map<String, String> matches, OutputStream datasetFile) {

    Model dataset = ModelFactory.createDefaultModel();
    Set<String> matchedEntities = matches.keySet();

    Property upperLevel = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasGADM_UpperLevel");
    Property name = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasGADM_Name");
    Property id = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasGADM_ID");
    Property type = ResourceFactory.createProperty(RDFVocabulary.TYPE);
    Property hasGeometry = ResourceFactory.createProperty(RDFVocabulary.HAS_GEOMETRY);
    Property asWKT = ResourceFactory.createProperty(RDFVocabulary.AS_WKT);

    for(GADMEntity gadmEntity : this.entities) {
      Resource subject;
      if(matchedEntities.contains(gadmEntity.getURI()))
        subject = ResourceFactory.createResource(matches.get(gadmEntity.getURI()));
      else
          subject = ResourceFactory.createResource(YAGO2geoVocabulary.RESOURCE+"gadmentity_"+gadmEntity.getGadmID());
      Resource geometry = ResourceFactory.createResource(YAGO2geoVocabulary.RESOURCE+"Geometry_gadm_"+gadmEntity.getGadmID());
      dataset.add(subject, type, ResourceFactory.createResource(YAGO2geoVocabulary.ONTOLOGY+gadmEntity.getNationalLevel()));
      dataset.add(subject, upperLevel, ResourceFactory.createTypedLiteral(gadmEntity.getUpperLevelUnit()));
      dataset.add(subject, id, ResourceFactory.createTypedLiteral(gadmEntity.getGadmID()));
      gadmEntity.getLabels().forEach(label -> dataset.add(subject, name, ResourceFactory.createStringLiteral(label)));
      dataset.add(subject, hasGeometry, geometry);
      dataset.add(geometry, asWKT, ResourceFactory.createPlainLiteral(gadmEntity.getGeometry().toText()));
    }
    RDFDataMgr.write(datasetFile, dataset, RDFFormat.TURTLE_BLOCKS);
  }

}
