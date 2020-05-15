package gr.uoa.di.kr.yagoextension.filters;

import java.util.List;
import java.util.Set;

import gr.uoa.di.kr.yagoextension.model.LabelMatches;
import org.locationtech.jts.geom.Geometry;

import gr.uoa.di.kr.yagoextension.model.Entity;
import gr.uoa.di.kr.yagoextension.model.GeometryMatches;

/*
 *  Input: Matches produced by label similarity filter.
 *  Output: Matches between entities that are near to each other (threshold)
 *  Every entity can be matched with at most one other entity
 */

public class GeometryDistance {
	
	public static double threshold = 0.2;
	
	public static GeometryMatches filter(LabelMatches labelMatches) {
		
		GeometryMatches geomMatches = new GeometryMatches();
		
		Set<Entity> lmKeys = labelMatches.getKeys();
		
		for(Entity yagoEntity : lmKeys) {
			Geometry yagoGeom = yagoEntity.getGeometry();
			List<Entity> datasourceEntities = labelMatches.getValueByKey(yagoEntity);
			double bestDist = threshold+1;
			Entity best = null;
			
			for(Entity datasourceEntity : datasourceEntities) {
				double curDist = yagoGeom.distance(datasourceEntity.getGeometry());
				if(curDist < bestDist) {
					bestDist = curDist;
					best = datasourceEntity;
				}
			}
			
			if(bestDist <= threshold) {
				geomMatches.addMatch(best, yagoEntity, bestDist);
			}
			
		}
		return geomMatches;
	}

}
