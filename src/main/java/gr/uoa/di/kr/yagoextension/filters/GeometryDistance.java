package gr.uoa.di.kr.yagoextension.filters;

/**
 * This class is part of the YAGO Extension Project
 * Author: Nikos Karalis 
 * kr.di.uoa.gr
 */

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

import gr.uoa.di.kr.yagoextension.structures.Entity;
import gr.uoa.di.kr.yagoextension.structures.GeometryMatchesStructure;
import gr.uoa.di.kr.yagoextension.structures.MatchesStructure;

/** 
 *  Input: Matches produced by label similarity filter.
 *  Output: Matches between entities that are near to each other (threshold)
 *  Every entity can be matched with only one other entity
 */

public class GeometryDistance {
	
	public static double threshold = 0.2;
	
	public static MatchesStructure filter(MatchesStructure labelMatches, Map<String, Entity> yago, Map<String, Entity> ds) {
		
		MatchesStructure geomMatches = new GeometryMatchesStructure();
		
		Set<String> lmKeys = labelMatches.getKeys();
		
		for(String lmKey : lmKeys) {
			Geometry yagoGeom = yago.get(lmKey).getGeometry();
			List<String> lmValue = labelMatches.getValueByKey(lmKey);
			double bestDist = yagoGeom.distance(ds.get(lmValue.get(0)).getGeometry());
			String best = lmValue.get(0);
			
			for(String l : lmValue.subList(1, lmValue.size())) {
				double curDist = yagoGeom.distance(ds.get(l).getGeometry());
				if(curDist < bestDist) {
					bestDist = curDist;
					best = l;
				}
			}
			
			if(bestDist <= threshold) {
				geomMatches.addMatch(lmKey, best, bestDist);
			}
			
		}
		return geomMatches;
	}

}
