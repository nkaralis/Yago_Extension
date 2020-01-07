package gr.uoa.di.kr.yagoextension.exception;

import gr.uoa.di.kr.yagoextension.exception.base.YagoExtRuntimeException;

public class YagoExtInvalidCsvException extends YagoExtRuntimeException {

    public YagoExtInvalidCsvException() {
    }

    public YagoExtInvalidCsvException(String message) {
        super(message);
    }

    public YagoExtInvalidCsvException(String message, Throwable cause) {
        super(message, cause);
    }

    public YagoExtInvalidCsvException(Throwable cause) {
        super(cause);
    }

    public YagoExtInvalidCsvException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
