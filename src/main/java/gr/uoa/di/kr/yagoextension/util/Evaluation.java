package gr.uoa.di.kr.yagoextension.util;

import gr.uoa.di.kr.yagoextension.model.Entity;
import gr.uoa.di.kr.yagoextension.model.GeometryMatches;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Evaluation {

  public static void run(GeometryMatches matches, int n, String file, String method, String preprocess)
    throws FileNotFoundException, UnsupportedEncodingException {

    PrintWriter out = new PrintWriter(file, "UTF-8");
    /* get a random sublist (subset) of the matches */
    List<Entity> dsEntities  = new ArrayList<>(matches.getKeys());
    int toEval = Integer.max(n, dsEntities.size()/100 );
    Collections.shuffle(dsEntities);
    List<Entity> keysEval = dsEntities.subList(0, Integer.min(dsEntities.size(), toEval));
    /* for each match find the labels that produce the best label similarity score */
    for(Entity dsEntity : keysEval) {
      Entity yagoEntity = matches.getValueByKey(dsEntity);
      double best = 0.0;
      String dsBest = null;
      String yagoBest = null;
      for(String dsLabel : dsEntity.getLabels()) {
        for(String yagoLabel : yagoEntity.getLabels()) {
          double sim = StringSimilarity.similarity(dsLabel, yagoLabel, method);
          double upperCaseSim =
            StringSimilarity.similarity(yagoLabel.toUpperCase(), dsLabel.toUpperCase(), method);
          sim = Math.max(upperCaseSim, sim);
          if(preprocess != null) {
            String ylProc = LabelProcessing.processYagoLabel(yagoLabel);
            String dlProc = LabelProcessing.processDataSourceLabel(dsLabel, preprocess);
            double tempSim = StringSimilarity.similarity(ylProc, dlProc, method);
            if(tempSim > sim)
              sim = tempSim;
          }
          if(sim > best) {
            dsBest = dsLabel;
            yagoBest = yagoLabel;
            best = sim;
          }
        }
      }
      out.format("%1$-40s | %2$-40s | %3$s %n", yagoBest, dsBest, yagoEntity.getURI());
    }
    out.close();
  }

}
