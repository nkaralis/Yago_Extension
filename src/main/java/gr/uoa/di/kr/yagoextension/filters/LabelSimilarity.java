package gr.uoa.di.kr.yagoextension.filters;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import org.apache.commons.text.similarity.LevenshteinDistance;
import gr.uoa.di.kr.yagoextension.structures.Entity;
import gr.uoa.di.kr.yagoextension.structures.LabelMatchesStructure;
import gr.uoa.di.kr.yagoextension.structures.MatchesStructure;
import gr.uoa.di.kr.yagoextension.util.LabelProcessing;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import me.tongfei.progressbar.ProgressBar;

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
	private ProgressBar pb;
	private String preprocess;

	
	public LabelSimilarity(List<Entity> yago, List<Entity> ds, int threads, String origin) {
		this.yago = yago;
		this.ds = ds;
		this.nThreads = threads;
		this.matches = new LabelMatchesStructure();
		pb = new ProgressBar("LabelSimilarity", yago.size());
		/** pre-processing of labels for the official datasets */
		preprocess = origin;
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
		exec.awaitTermination(10000000, TimeUnit.MINUTES);
		pb.close(); // terminate progress bar
		return matches;
	}
	
	private void filter(List<Entity> yagoPart) {
		/**
		 * Input: Sublist of yago list
		 * Output: Matches produced by the label similarity filter
		 */
		int lvDist;
		double lvRatio; 
		LevenshteinDistance lv = new LevenshteinDistance();
		for(Entity yagoEnt : yagoPart) {
			String yagoKey = yagoEnt.getID();
			for(Entity dsEnt : ds) {
				String dsKey = dsEnt.getID();
				for(String yagoLabel : yagoEnt.getLabels()) {
					for(String dsLabel : dsEnt.getLabels()) {
						int upperCaseSim = lv.apply(yagoLabel, dsLabel);
						int lowerCaseSim = lv.apply(yagoLabel.toUpperCase(), dsLabel);
						lvDist = (upperCaseSim < lowerCaseSim) ? upperCaseSim : lowerCaseSim; 
						lvRatio = levenshteinRatio(lvDist, Math.max(yagoLabel.length(), dsLabel.length()));
						if(preprocess != null) {
							String ylProc = LabelProcessing.processYagoLabel(yagoLabel);
							String dlProc = LabelProcessing.processDataSourceLabel(dsLabel, preprocess);
							int tempLvDist = lv.apply(ylProc, dlProc);
							double tempLvRatio = levenshteinRatio(tempLvDist, Math.max(ylProc.length(), dlProc.length()));
							if(tempLvRatio > lvRatio)
								lvRatio = tempLvRatio;
						}
						if(lvRatio >= threshold) {
							synchronized(matches) {
								matches.addMatch(yagoKey, dsKey, lvRatio);
							}
						}
					}
				}
			}
			synchronized(pb) {
				pb.step(); // add step to the progress bar
			}
		}
	}
	
	private static double levenshteinRatio(int dist, int len) {
		return 1-((double)dist/len);
	}

}
