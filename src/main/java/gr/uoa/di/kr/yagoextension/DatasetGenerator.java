package gr.uoa.di.kr.yagoextension;

import gr.uoa.di.kr.yagoextension.model.Entity;
import gr.uoa.di.kr.yagoextension.repositories.Repository;
import gr.uoa.di.kr.yagoextension.repositories.RepositoryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DatasetGenerator {

  public static void run(Properties properties) throws IOException {

    if(!properties.containsKey("datasource") || !properties.containsKey("datasource_file") ||
      !properties.containsKey("dataset") || !properties.containsKey("matches_file")) {
      System.out.println("Properties datasource, datasource_file, matches_file and dataset_extended are mandatory");
    }
    String datasourceFile = properties.getProperty("datasource_file");
    if(Files.notExists(Paths.get(datasourceFile))) {
      throw new IOException("Path of the input datasource file does not exist");
    }
    String matchesFile = properties.getProperty("matches_file");
    if(Files.notExists(Paths.get(datasourceFile))) {
      throw new IOException("Path of the input datasource file does not exist");
    }

    Repository<Entity> datasource =
      RepositoryFactory.createDatasourceRepository(properties.getProperty("datasource"), datasourceFile);

    datasource.read();
    Model matches = RDFDataMgr.loadModel(matchesFile);
    StmtIterator stmts = matches.listStatements();
    Map<String, String> matchesMap = new HashMap<>();
    while (stmts.hasNext()) {
      Statement stmt = stmts.next();
      String dsEntity = stmt.getObject().asResource().getURI();
      String yagoEntity = stmt.getSubject().asResource().getURI();
      matchesMap.put(dsEntity, yagoEntity);
    }

    OutputStream matchedDataset = new FileOutputStream(new File(properties.getProperty("dataset")));
    datasource.generate(matchesMap, matchedDataset);
    matchedDataset.close();
  }

}
