package gr.uoa.di.kr.yagoextension.repositories;

import gr.uoa.di.kr.yagoextension.model.GAGEntity;
import gr.uoa.di.kr.yagoextension.vocabulary.RDFVocabulary;
import gr.uoa.di.kr.yagoextension.vocabulary.YAGO2geoVocabulary;
import org.apache.jena.riot.RDFFormat;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import gr.uoa.di.kr.yagoextension.readers.RDFReader;
import org.geotools.referencing.CRS;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.geotools.geometry.jts.JTS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class GAGRepository extends Repository<GAGEntity> implements RDFReader {

  GAGRepository(String path) {
    super(path);
  }

  @Override
  public void read() {
    this.readRDF();
  }

  @Override
  public void readRDF() {

    String label = "http://geo.linkedopendata.gr/gag/ontology/έχει_επίσημο_όνομα";
    String populationPredicate = "http://geo.linkedopendata.gr/gag/ontology/έχει_πληθυσμό";
    String belongsToPredicate = "http://geo.linkedopendata.gr/gag/ontology/ανήκει_σε";
    String asWkt = "http://geo.linkedopendata.gr/gag/ontology/έχει_γεωμετρία";
    String id = "http://geo.linkedopendata.gr/gag/ontology/έχει_κωδικό";
    String type = RDFVocabulary.TYPE;

    WKTReader wktReader = new WKTReader();
    Model gag = RDFDataMgr.loadModel(this.inputFile);
    ResIterator subjects = gag.listSubjects();
    /* iterate over the subjects of the input rdf file */
    while(subjects.hasNext()) {

      Resource subject = subjects.next();
      String subjectURI = subject.getURI();
      Set<String> officialNames = new HashSet<>();
      Integer gagID = null;
      String belongsToUnit = null;
      String wkt = null;
      String division = null;
      Integer population = null;
      StmtIterator subjectStmts = gag.listStatements(subject, null, (RDFNode) null);

      /* get the information that is available for the current entity */
      while (subjectStmts.hasNext()) {
        Statement stmt = subjectStmts.next();
        String predicate = stmt.getPredicate().getURI();
        RDFNode object = stmt.getObject();
        if(predicate.equals(label))
          officialNames.add(object.asLiteral().getString());
        else if(predicate.equals(id))
          gagID = object.asLiteral().getInt();
        else if(predicate.equals(asWkt))
          wkt = object.asLiteral().getString();
        else if(predicate.equals(belongsToPredicate))
          belongsToUnit = object.asResource().getURI();
        else if(predicate.equals(populationPredicate))
          population = object.asLiteral().getInt();
        else if(predicate.equals(type))
          division = "GAG_"+translate(object.asResource().getLocalName());
      }
      if(gagID == null) {
        /* the id of some entities is not given by a triple -> get local name */
        if(subjectURI.contains("/id/"))
          gagID = Integer.parseInt(subjectURI.substring(subjectURI.lastIndexOf("/")+1));
      }
      if(officialNames.size() == 0 || gagID == null)
        continue;

      /* create a new entity and add it to the repository */
      try {
        /* change coordinate system */
        Geometry gagGeometry = null;
        if(wkt != null) {
           gagGeometry = wktReader.read(wkt);
          try {
            CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:2100");
            CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
            gagGeometry = JTS.transform(gagGeometry, transform);
            gagGeometry.geometryChanged();
          } catch (FactoryException | MismatchedDimensionException | TransformException e) {
            e.printStackTrace();
            System.err.println("Transformation of the geometry failed!");
          }
        }
        entities.add(new GAGEntity(subjectURI, officialNames, gagGeometry, population, gagID, division, belongsToUnit));
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

  }

  @Override
  public void generate(Map<String, String> matches, OutputStream datasetFile) {

    Model dataset = ModelFactory.createDefaultModel();
    Set<String> matchedEntities = matches.keySet();

    Property population = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasGAG_Population");
    Property name = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasGAG_Name");
    Property id = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "hasGAG_ID");
    Property belongsToProperty = ResourceFactory.createProperty(YAGO2geoVocabulary.ONTOLOGY, "GAG_BelongsTo");
    Property type = ResourceFactory.createProperty(RDFVocabulary.TYPE);
    Property hasGeometry = ResourceFactory.createProperty(RDFVocabulary.HAS_GEOMETRY);
    Property asWKT = ResourceFactory.createProperty(RDFVocabulary.AS_WKT);

    for(GAGEntity gagEntity : this.entities) {
      Resource subject;
      if(matchedEntities.contains(gagEntity.getURI()))
        subject = ResourceFactory.createResource(matches.get(gagEntity.getURI()));
      else
        subject = ResourceFactory.createResource(YAGO2geoVocabulary.RESOURCE+"gagentity_"+gagEntity.getGagID());
      Resource geometry = ResourceFactory.createResource(YAGO2geoVocabulary.RESOURCE+"Geometry_gag_"+gagEntity.getGagID());

      /* if the upper level unit is matched, we have to use the YAGO entity's uri
       * if the upper level unit is not matched, we have to use the new entity's uri */
      Resource belongsToUnit = null;
      String unitURI = gagEntity.getBelongsTo();
      if(unitURI != null) {
        if (matchedEntities.contains(unitURI))
          belongsToUnit = ResourceFactory.createResource(matches.get(unitURI));
        else
          belongsToUnit = ResourceFactory.createResource(
            YAGO2geoVocabulary.RESOURCE+"gagentity_"+ unitURI.substring(unitURI.lastIndexOf("/")+1));
      }

      if(gagEntity.getPopulation() != null)
        dataset.add(subject, population, ResourceFactory.createTypedLiteral(gagEntity.getPopulation()));
      dataset.add(subject, id, ResourceFactory.createTypedLiteral(gagEntity.getGagID()));
      dataset.add(subject, type, ResourceFactory.createResource(YAGO2geoVocabulary.ONTOLOGY+gagEntity.getDivision()));
      gagEntity.getLabels().forEach(label -> dataset.add(subject, name, ResourceFactory.createStringLiteral(label)));
      if (gagEntity.getGeometry() != null) {
        dataset.add(subject, hasGeometry, geometry);
        dataset.add(geometry, asWKT, ResourceFactory.createPlainLiteral(gagEntity.getGeometry().toText()));
      }
      if(belongsToUnit != null)
        dataset.add(subject, belongsToProperty, belongsToUnit);
    }
    RDFDataMgr.write(datasetFile, dataset, RDFFormat.TURTLE_BLOCKS);
  }

  private String translate(String input) {
    String translation = null;
    switch(input) {
      case "Δήμος":
        translation = "Municipality";
        break;
      case "Περιφέρεια":
        translation = "Region";
        break;
      case "Αποκεντρωμένη_Διοίκηση":
        translation = "Decentralized_Administration";
        break;
      case "Περιφερειακή_Ενότητα":
        translation = "Regional_Unit";
        break;
      case "Δημοτική_Ενότητα":
        translation = "Municipal_Unit";
        break;
      case "Δημοτική_Κοινότητα":
        translation = "Municipal_Community";
        break;
    }
    return translation;
  }

}
