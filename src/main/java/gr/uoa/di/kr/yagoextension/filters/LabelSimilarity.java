package gr.uoa.di.kr.yagoextension.filters;

import gr.uoa.di.kr.yagoextension.model.Entity;
import gr.uoa.di.kr.yagoextension.model.LabelMatches;
import gr.uoa.di.kr.yagoextension.util.LabelProcessing;
import gr.uoa.di.kr.yagoextension.util.StringSimilarity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/*
 *  Finds similar labels between Yago entities and the entities of the second data source.
 *  An entity of Yago can be matched with multiple entities of the second data source after this filter is applied.
 */

public class LabelSimilarity {
	
	private static double threshold = 0.82;
	private static Logger logger = LogManager.getLogger(LabelSimilarity.class);

	public static LabelMatches run(Set<Entity> yago, Set<Entity> datasource, String preprocess, String strSimilarity) {

		LabelMatches matches = new LabelMatches();
		int yagoSize = yago.size();
		final Integer[] progress = new Integer[]{0, 1};
		yago.parallelStream().forEach(yagoEntity -> {
			double labelSimilarity;
			for(Entity datasourceEntity : datasource) {
				for(String yagoLabel : yagoEntity.getLabels()) {
					for(String datasourceLabel : datasourceEntity.getLabels()) {
						double originalSim = StringSimilarity.similarity(yagoLabel, datasourceLabel, strSimilarity);
						double upperCaseSim =
							StringSimilarity.similarity(yagoLabel.toUpperCase(), datasourceLabel.toUpperCase(), strSimilarity);
						labelSimilarity = Math.max(upperCaseSim, originalSim);
						if(preprocess != null) {
							/* apply label processing rules */
							String ylProc = LabelProcessing.processYagoLabel(yagoLabel);
							String dlProc = LabelProcessing.processDataSourceLabel(datasourceLabel, preprocess);
							double postLabelProcessingSimilarity = StringSimilarity.similarity(ylProc, dlProc, strSimilarity);
							if(postLabelProcessingSimilarity > labelSimilarity)
								labelSimilarity = postLabelProcessingSimilarity;
						}
						if(labelSimilarity >= threshold) {
							synchronized(matches) {
								matches.addMatch(yagoEntity, datasourceEntity, labelSimilarity);
							}
						}
					}
				}
			}
			synchronized(progress) {
				progress[0] += 1;
				if(yagoSize * 0.1 * progress[1] < progress[0]) {
					logger.info("Progress: " + progress[1]*10 + "%");
					progress[1] += 1;
				}
			}
		});
		return matches;
	}
}
