package gr.uoa.di.kr.yagoextension.exception.base;

public abstract class YagoExtRuntimeException extends RuntimeException {

    public YagoExtRuntimeException() {
    }

    public YagoExtRuntimeException(String message) {
        super(message);
    }

    public YagoExtRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public YagoExtRuntimeException(Throwable cause) {
        super(cause);
    }

    public YagoExtRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
