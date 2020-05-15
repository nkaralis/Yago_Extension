package gr.uoa.di.kr.yagoextension;

import gr.uoa.di.kr.yagoextension.filters.GeometryDistance;
import gr.uoa.di.kr.yagoextension.filters.LabelSimilarity;
import gr.uoa.di.kr.yagoextension.model.*;
import gr.uoa.di.kr.yagoextension.repositories.*;
import gr.uoa.di.kr.yagoextension.util.Blacklist;
import gr.uoa.di.kr.yagoextension.util.Evaluation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MatchingPhase {

  public static void run(Properties properties) throws IOException, RuntimeException, InterruptedException {

    if(!properties.containsKey("yago") || !properties.containsKey("datasource") ||
      !properties.containsKey("datasource_file") || !properties.containsKey("output")) {
      System.out.println("Properties yago, datasource and output are mandatory");
    }
    /* check if the paths provided by the properties file are valid */
    String yagoFile = properties.getProperty("yago");
    if(Files.notExists(Paths.get(yagoFile))) {
      throw new IOException("Path of the input YAGO file does not exist");
    }
    String datasourceFile = properties.getProperty("datasource_file");
    if(Files.notExists(Paths.get(datasourceFile))) {
      throw new IOException("Path of the input datasource file does not exist");
    }
    String outputFile = properties.getProperty("output");
    if(Files.notExists(Paths.get(outputFile.substring(0, outputFile.lastIndexOf("/")+1)))) {
      throw new IOException("Path of the output file does not exist");
    }
    /* read properties file */
    int threads = Integer.parseInt(Optional.ofNullable(properties.getProperty("threads")).orElse("1"));
    String strSimMethod = Optional.ofNullable(properties.getProperty("similarity_method")).orElse(null);
    String preprocess = Optional.ofNullable(properties.getProperty("preprocess")).orElse(null);
    int evaluationSize = Integer.parseInt(Optional.ofNullable(properties.getProperty("evaluation_size")).orElse("0"));

    /* read data */
    Logger logger = LogManager.getLogger(MatchingPhase.class);
    Repository<Entity> yago = RepositoryFactory.createYAGORepository(yagoFile);
    Repository<Entity> datasource =
      RepositoryFactory.createDatasourceRepository(properties.getProperty("datasource"), datasourceFile);
    logger.info("Started reading data");
    yago.read();
    datasource.read();
    logger.info("Finished reading data");
    Set<Entity> yagoEntities = yago.getEntities();
    Set<Entity> datasourceEntities = datasource.getEntities();
    /* remove entities that have already been matched */
    if(properties.containsKey("blacklist")){
      String[] matchesFiles = properties.getProperty("blacklist").split(",");
      Blacklist.removeMatchedEntities(yagoEntities, datasourceEntities, matchesFiles);
    }
    logger.info("Number of Yago Entities: "+yagoEntities.size());
    logger.info("Number of Datasource Entities: "+datasourceEntities.size());

    /* matching phase starts here */
    LabelMatches labelMatches = LabelSimilarity.run(yagoEntities, datasourceEntities, preprocess, strSimMethod);
    logger.info("Finished Label Similarity Filter");
    logger.info("Number of Label Similarity Matches: "+labelMatches.size());
    GeometryMatches geomMatches = GeometryDistance.filter(labelMatches);
    logger.info("Finished Geometry Distance Filter");
    logger.info("Number of Matches: "+geomMatches.size());

    /* write matches to file */
    OutputStream matchesFile = new FileOutputStream(new File(properties.getProperty("output")));
    geomMatches.writeToFile(matchesFile);
    matchesFile.close();

    /* generate a file for the evaluation of the produced matches (optional) */
    if(evaluationSize > 0) {
      String evalOut = outputFile.replace(".ttl", "_eval.txt");
      logger.info("Generating a random subset of the matches for evaluation");
      Evaluation.run(geomMatches, evaluationSize, evalOut, strSimMethod, preprocess);
      logger.info("Evaluation file: " + evalOut);
    }

    /* generate dataset */
    if(properties.containsKey("dataset")) {
      OutputStream dataset = new FileOutputStream(new File(properties.getProperty("dataset")));
      datasource.generate(geomMatches.getUriMatches(), dataset);
      dataset.close();
    }
  }

}
