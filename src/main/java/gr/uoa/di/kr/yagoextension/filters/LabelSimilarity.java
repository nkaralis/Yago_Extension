package gr.uoa.di.kr.yagoextension.filters;

/**
 * This class is part of the Yago Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import org.apache.commons.text.similarity.LevenshteinDistance;
import gr.uoa.di.kr.yagoextension.structures.Entity;
import gr.uoa.di.kr.yagoextension.structures.LabelMatchesStructure;
import gr.uoa.di.kr.yagoextension.structures.MatchesStructure;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** 
 *  Finds similar labels between Yago entities and the entities of the second data source.
 *  An entity of Yago can be matched with multiple entities for the second data source in this filter.
 *  Levenshtein distance is used in order to find similarity between labels. 
 */

public class LabelSimilarity {
	
	private double threshold = 0.8;
	private int nThreads;
	private MatchesStructure matches;
	private List<Entity> yago;
	private List<Entity> ds;

	
	public LabelSimilarity(List<Entity> yago, List<Entity> ds, int threads) {
		this.yago = yago;
		this.ds = ds;
		this.nThreads = threads;
		this.matches = new LabelMatchesStructure();
	}
	
	public MatchesStructure run() throws InterruptedException {
		/**
		 * Initialize threads and split the list of yago entities in nThreads parts  
		 */
		int yagoSize = yago.size();
		ExecutorService exec = Executors.newFixedThreadPool(nThreads);
		int minItemsPerThread = yagoSize / nThreads;
    int maxItemsPerThread = minItemsPerThread + 1;
    int threadsWithMaxItems = yagoSize - nThreads * minItemsPerThread;
    int start = 0;
		for(int i = 0; i < nThreads; i++) {
			int itemsCount = (i < threadsWithMaxItems ? maxItemsPerThread : minItemsPerThread);
			int end = start + itemsCount;
			List<Entity> yagoPart = yago.subList(start, end);
			Runnable filterTask = new Runnable(){
				public void run(){
					filter(yagoPart);
				}
			};
			exec.submit(filterTask);
			start = end;
		}
		exec.shutdown();
		exec.awaitTermination(100000, TimeUnit.MINUTES);
		return matches;
	}
	
	private void filter(List<Entity> yagoPart) {
		/**
		 * Input: Sublist of yago list
		 * Output: Matches produced by the label similarity filter
		 */
		
		Integer lvDist;
		Double lvRatio;
		
		LevenshteinDistance lv = new LevenshteinDistance();
		for(Entity yagoEnt : yagoPart) {
			String yagoKey = yagoEnt.getID();
			for(Entity dsEnt : ds) {
				String dsKey = dsEnt.getID();
				for(String yagoLabel : yagoEnt.getLabels()) {
					for(String dsLabel : dsEnt.getLabels()) {
						lvDist = lv.apply(yagoLabel, dsLabel);
						lvRatio = levenshteinRatio(lvDist, Math.max(yagoLabel.length(), dsLabel.length()));
						if(lvRatio >= threshold) {
							synchronized(matches) {
								matches.addMatch(yagoKey, dsKey, lvRatio);
							}
						}
					}
				}
			}
		}
	}
	
	private static double levenshteinRatio(int dist, int len) {
		return 1-((double)dist/len);
	}

}
