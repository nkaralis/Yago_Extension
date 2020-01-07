package gr.uoa.di.kr.yagoextension.reader.v2.impl;

import gr.uoa.di.kr.yagoextension.domain.v2.YagoExtEntity;
import gr.uoa.di.kr.yagoextension.exception.YagoExtInvalidCsvException;
import gr.uoa.di.kr.yagoextension.reader.v2.base.YagoExtReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;


public class TsvReader implements YagoExtReader {

    public static final String LABEL_STR = "label";
    public static final String LONGITUDE_STR = "Longitude";
    public static final String LATITUDE_STR = "Latitude";
    private File inputFile;

    public TsvReader(File inputFile) {
        this.inputFile = inputFile;
        validateInputFile();
    }

    @Override
    public File getInputFile() {
        return inputFile;
    }

    @Override
    public Map<String, YagoExtEntity> read() {
        Map<String, YagoExtEntity> yagoExtEntityMap = new HashMap<>();
        try {
            CSVFormat csvFileFormat = CSVFormat.TDF.withQuote(null);
            CSVParser parser = CSVParser.parse(getInputFile(), StandardCharsets.UTF_8, csvFileFormat);
            for (CSVRecord csvRecord : parser.getRecords()) {
                String id = csvRecord.get(0);
                YagoExtEntity yagoExtEntity = requireNonNull(yagoExtEntityMap.putIfAbsent(id, new YagoExtEntity(id)));
                if (isLabel(csvRecord)) {
                    String label = csvRecord.get(2);
                    label = label.substring(label.indexOf("\"") + 1, label.lastIndexOf("\""));
                    requireNonNull(yagoExtEntity).getLabels().add(label);
                } else if (isLongitude(csvRecord)) {
                    yagoExtEntity.setLongitude(csvRecord.get(3));
                } else if (isLatitude(csvRecord)) {
                    yagoExtEntity.setLatitude(csvRecord.get(3));
                }
            }
        } catch (IOException e) {
            // TODO Should add logger here
            e.printStackTrace();
            throw new YagoExtInvalidCsvException(e.getMessage());
        }
        return yagoExtEntityMap;
    }

    private boolean isLabel(CSVRecord csvRecord) {
        return csvRecord.get(1).contains(LABEL_STR);
    }

    private boolean isLongitude(CSVRecord csvRecord) {
        return csvRecord.get(1).contains(LONGITUDE_STR);
    }

    private boolean isLatitude(CSVRecord csvRecord) {
        return csvRecord.get(1).contains(LATITUDE_STR);
    }

}
