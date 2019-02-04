package gr.uoa.di.kr.yagoextension.util;

import gr.uoa.di.kr.yagoextension.structures.MatchesStructure;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis
 * kr.di.uoa.gr
 */

public class Evaluation {

  public static void generate(MatchesStructure matches, int n) {

    List<String> keys  = new ArrayList(matches.getKeys());
    Collections.shuffle(keys);
    System.out.println("hello");
    List<String> keysEval = keys.subList(0, n);
    for (String key : keysEval) {
      System.out.println(key+"\t"+matches.getValueByKey(key).get(0));
    }
  }

}
