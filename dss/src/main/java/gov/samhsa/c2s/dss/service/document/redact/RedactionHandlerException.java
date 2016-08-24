package gov.samhsa.c2s.dss.service.document.redact;

public class RedactionHandlerException extends RuntimeException {
    public RedactionHandlerException() {
    }

    public RedactionHandlerException(String message) {
        super(message);
    }

    public RedactionHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedactionHandlerException(Throwable cause) {
        super(cause);
    }

    public RedactionHandlerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
