package gr.uoa.di.kr.yagoextension.reader.v2.base;

import gr.uoa.di.kr.yagoextension.domain.v2.YagoExtEntity;
import gr.uoa.di.kr.yagoextension.exception.YagoExtMissingFileException;

import java.io.File;
import java.util.Map;

public interface YagoExtReader {

    File getInputFile();

    Map<String, YagoExtEntity> read();

    default void validateInputFile() {
        if (!getInputFile().exists()) {
            throw new YagoExtMissingFileException(String.format("File %s was not found", getInputFile().getAbsolutePath()));
        }
    }

}
