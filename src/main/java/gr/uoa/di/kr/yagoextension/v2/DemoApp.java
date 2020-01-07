package gr.uoa.di.kr.yagoextension.v2;

import gr.uoa.di.kr.yagoextension.domain.v2.YagoExtEntity;
import gr.uoa.di.kr.yagoextension.reader.v2.base.YagoExtReader;
import gr.uoa.di.kr.yagoextension.reader.v2.impl.TsvReader;

import java.io.File;
import java.util.Map;

public class DemoApp {

    private static final String DEMO_YAGO_FILE = "";
    private static final String DEMO_DATASOURCE_FILE = "";

    private static final String DEMO_SIMILARITY_METHOD = "jarowinkler";
    private static final Double DEMO_SIMILARITY_THRESHOLD = 0.82D;


    public static void main(String[] args) {
        YagoExtReader yagoReader = new TsvReader(new File(DEMO_YAGO_FILE));
        Map<String, YagoExtEntity> yagoEntityMap = yagoReader.read();

        YagoExtReader dsReader = new TsvReader(new File(DEMO_DATASOURCE_FILE));
        Map<String, YagoExtEntity> dsEntityMap = dsReader.read();

        for (YagoExtEntity yagoEntity : yagoEntityMap.values()) {
            for (YagoExtEntity dsEntity : dsEntityMap.values()) {
                yagoEntity.putIfHasSimilarLabelsTo(dsEntity, DEMO_SIMILARITY_METHOD, DEMO_SIMILARITY_THRESHOLD);
            }
        }
    }
}


