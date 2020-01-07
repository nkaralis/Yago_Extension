package gr.uoa.di.kr.yagoextension.exception;

import gr.uoa.di.kr.yagoextension.exception.base.YagoExtRuntimeException;

public class YagoExtMissingFileException extends YagoExtRuntimeException {

    public YagoExtMissingFileException() {
    }

    public YagoExtMissingFileException(String message) {
        super(message);
    }

    public YagoExtMissingFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public YagoExtMissingFileException(Throwable cause) {
        super(cause);
    }

    public YagoExtMissingFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
