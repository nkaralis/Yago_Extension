package gr.uoa.di.kr.yagoextension.domain.v2;

import com.vividsolutions.jts.geom.Geometry;
import gr.uoa.di.kr.yagoextension.util.v2.GeometryParser;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gr.uoa.di.kr.yagoextension.domain.v2.SimilarityMethod.calculateSim;

@Data
public class YagoExtEntity {

    protected String id;
    protected Set<String> labels = new HashSet<>();
    protected Geometry geometry;

    protected String longitude;
    protected String latitude;

    protected Map<YagoExtEntity, Double> potentialMatches = new HashMap<>();

    public YagoExtEntity(String id) {
        this.id = id;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
        if (this.latitude != null) {
            calcGeometry();
        }
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
        if (this.longitude != null) {
            calcGeometry();
        }
    }

    private void calcGeometry() {
        this.geometry = GeometryParser.extractGeometry(this.longitude, this.latitude).orElse(null);
    }

    public void putIfHasSimilarLabelsTo(YagoExtEntity otherYagoExtEntity, String similarityMethod, Double similarityThreshold) {
        double bestSim = 0.0;
        for (String yagoLabel : this.getLabels()) {
            for (String dsLabel : otherYagoExtEntity.getLabels()) {
                Double labelSimilarity = calculateSim(yagoLabel, dsLabel, similarityMethod);
                if (labelSimilarity.compareTo(similarityThreshold) > 0) {
                    // TODO MISSING LOGIC
                    return;
                }
            }
        }

    }

}
