package gr.uoa.di.kr.yagoextension.util;

import gr.uoa.di.kr.yagoextension.structures.Entity;
import gr.uoa.di.kr.yagoextension.structures.MatchesStructure;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis
 * kr.di.uoa.gr
 */

public class Evaluation {

  public static void generate(MatchesStructure matches, int n, Map<String, Entity> yagoEnts, Map<String, Entity> dsEnts,
                              String file, String method) throws FileNotFoundException, UnsupportedEncodingException {

    PrintWriter out = new PrintWriter(file, "UTF-8");
    List<String> keys  = new ArrayList<String>(matches.getKeys());
    Collections.shuffle(keys);
    List<String> keysEval = keys.subList(0, Integer.min(keys.size(), n));
    for(String key : keysEval) {
    	String yagoEnt = matches.getValueByKey(key).get(0);
    	double best = 0.0;
    	String dsBest = null;
    	String yagoBest = null;
    	for(String dsLabel : dsEnts.get(key).getLabels()) {
    	  for(String yagoLabel : yagoEnts.get(yagoEnt).getLabels()) {
    	    double sim = StringSimilarity.similarity(dsLabel, yagoLabel, method);
    	    if(sim > best) {
    	      dsBest = dsLabel;
    	      yagoBest = yagoLabel;
    	      best = sim;
          }
        }
        out.println(dsBest+"\t"+yagoBest);
      }
    }
    out.close();
  }

}
