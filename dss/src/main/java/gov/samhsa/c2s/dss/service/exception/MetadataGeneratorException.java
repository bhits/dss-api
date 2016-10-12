package gov.samhsa.c2s.dss.service.exception;

public class MetadataGeneratorException extends RuntimeException {

    /**
     * Instantiates a new d s4 p exception.
     */
    public MetadataGeneratorException() {
        super();
    }

    /**
     * Instantiates a new d s4 p exception.
     *
     * @param arg0 the arg0
     * @param arg1 the arg1
     */
    public MetadataGeneratorException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Instantiates a new d s4 p exception.
     *
     * @param arg0 the arg0
     */
    public MetadataGeneratorException(String arg0) {
        super(arg0);
    }

    /**
     * Instantiates a new d s4 p exception.
     *
     * @param arg0 the arg0
     */
    public MetadataGeneratorException(Throwable arg0) {
        super(arg0);
    }
}
