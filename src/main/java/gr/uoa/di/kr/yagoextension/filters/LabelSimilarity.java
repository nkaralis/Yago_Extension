package gr.uoa.di.kr.yagoextension.filters;

/**
 * This class is part of the Yago Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.util.Iterator;
import java.util.List;
import java.lang.Math;
import org.apache.commons.text.similarity.LevenshteinDistance;
import gr.uoa.di.kr.yagoextension.structures.LabelMatchesStructure;
import gr.uoa.di.kr.yagoextension.structures.LabelStructure;

public class LabelSimilarity {
	
	private static double threshold = 0.8;
	/** Finds similar labels between Yago entities and the second data source.
	 *  An entity of Yago can be matched with multiple entities for the second data source in this filter.
	 *  Levenshtein distance is used in order to find similarity between labels. 
	 */
	public static LabelMatchesStructure filter(LabelStructure yago, LabelStructure ds) {
		
		LabelMatchesStructure matches = new LabelMatchesStructure();
		Integer lvDist;
		Double lvRatio;
		LevenshteinDistance lv = new LevenshteinDistance();
		Iterator<String> yagoIter = yago.getKeys();
		while(yagoIter.hasNext()) {
			
			String yagoKey = yagoIter.next();
			List<String> yagoLabels = yago.getValueByKey(yagoKey);
			Iterator<String> dsIter = ds.getKeys();
			while(dsIter.hasNext()) {
				
				String dsKey = dsIter.next();
				List<String> dsLabels = ds.getValueByKey(dsKey);
				for(String yagoLabel : yagoLabels) {
					for(String dsLabel : dsLabels) {
						lvDist = lv.apply(yagoLabel, dsLabel);
						lvRatio = levenshteinRatio(lvDist, Math.max(yagoLabel.length(), dsLabel.length()));
						if(lvRatio >= threshold) {
							matches.addMatch(yagoKey, dsLabel, lvRatio);
//							System.out.println(yagoKey+" "+yagoLabel+" "+dsLabel+" "+lvRatio.toString()+" "+lvDist.toString());
						}
					}
				}
				
			}
			
		}
		
		return matches;
	}
	
	private static double levenshteinRatio(int dist, int len) {
		return 1-((double)dist/len);
	}

}
