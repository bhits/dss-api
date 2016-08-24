package gov.samhsa.c2s.dss.service.exception;

public class DocumentSegmentationException extends RuntimeException {

    /**
     * Instantiates a new DocumentSegmentation exception.
     */
    public DocumentSegmentationException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Instantiates a new DocumentSegmentation exception.
     *
     * @param arg0 the arg0
     * @param arg1 the arg1
     */
    public DocumentSegmentationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    /**
     * Instantiates a new DocumentSegmentation exception.
     *
     * @param arg0 the arg0
     */
    public DocumentSegmentationException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * Instantiates a new DocumentSegmentation exception.
     *
     * @param arg0 the arg0
     */
    public DocumentSegmentationException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -6820537577171759098L;
}
