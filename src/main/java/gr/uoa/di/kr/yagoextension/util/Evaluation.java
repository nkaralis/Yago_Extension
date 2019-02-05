package gr.uoa.di.kr.yagoextension.util;

import gr.uoa.di.kr.yagoextension.structures.Entity;
import gr.uoa.di.kr.yagoextension.structures.MatchesStructure;
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

  public static void generate(MatchesStructure matches, int n, Map<String, Entity> yagoEnts) {

    List<String> keys  = new ArrayList<String>(matches.getKeys());
    Collections.shuffle(keys);
    List<String> keysEval = keys.subList(0, n);
    for (String key : keysEval) {
    	String yagoEnt = matches.getValueByKey(key).get(0);
      System.out.println(key+"\t"+yagoEnts.get(yagoEnt).getLabels().toString());
    }
  }

}
