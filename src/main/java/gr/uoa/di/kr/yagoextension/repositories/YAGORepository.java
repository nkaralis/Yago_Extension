package gr.uoa.di.kr.yagoextension.repositories;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import gr.uoa.di.kr.yagoextension.model.Entity;
import gr.uoa.di.kr.yagoextension.readers.TSVReader;
import gr.uoa.di.kr.yagoextension.vocabulary.YAGOVocabulary;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

class YAGORepository extends Repository<Entity> implements TSVReader {

  YAGORepository(String path) {
    super(path);
  }

  @Override
  public void read() {
    this.readTSV();
  }

  @Override
  public void readTSV() {

    File yagoFile = new File(this.inputFile);
    Map<String, Set<String>> labels = new HashMap<>();
    Map<String, String> latitudes = new HashMap<>();
    Map<String, String> longitudes = new HashMap<>();

    /* read the triples of the input yago TSV file */
    try{
      CSVFormat csvFileFormat = CSVFormat.TDF.withQuote(null);
      CSVParser parser = CSVParser.parse(yagoFile, StandardCharsets.UTF_8, csvFileFormat);
      for(CSVRecord record : parser.getRecords()) {
        String subject = record.get(0);
        subject = subject.replace("<", "").replace(">","");
        String predicate = record.get(1);
        if(predicate.contains(YAGOVocabulary.LABEL_TSV)) {
          String object = record.get(2);
          String label = object.substring(object.indexOf("\"")+1, object.lastIndexOf("\"")); // keep the part that is between the quotes
          if(labels.containsKey(subject))
            labels.get(subject).add(label);
          else
            labels.put(subject, new HashSet<>(Collections.singletonList(label)));
        }
        else if(predicate.contains(YAGOVocabulary.HAS_LATITUDE_TSV)) {
          String object = record.get(3);
          latitudes.put(subject, object);
        }
        else if(predicate.contains(YAGOVocabulary.HAS_LONGITUDE_TSV)) {
          String object = record.get(3);
          longitudes.put(subject, object);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    /* populate the repository */
    WKTReader wktReader = new WKTReader();
    for(String subject : latitudes.keySet()) {
      /* create the wkt serialization from the given (long, lat) pair */
      String latitude = latitudes.get(subject);
      String longitude = longitudes.get(subject);
      String wkt = "POINT( "+longitude+" "+latitude+" )";
      try {
        entities.add(new Entity(YAGOVocabulary.NAMESPACE+subject, labels.get(subject), wktReader.read(wkt)));
      } catch (ParseException e) {
        e.printStackTrace();
        System.exit(1);
      }
    }
  }

  @Override
  public void generate(Map<String, String> matches, OutputStream datasetFile) {
    throw new RuntimeException("Unsupported Operation: YAGO repository does not create new RDF datasets.");
  }

}
