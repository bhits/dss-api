package gov.samhsa.mhc.dss.service.dto;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Created by Jiahao.Li on 5/26/2016.
 */
public class ClinicalDocumentValidationRequest {
    @NotNull
    private byte[] document;
    private Optional<String> documentEncoding = Optional.empty();

    public byte[] getDocument() {
        return document;
    }

    public void setDocument(byte[] document) {
        this.document = document;
    }

    public Optional<String> getDocumentEncoding() {
        return documentEncoding;
    }

    public void setDocumentEncoding(String documentEncoding) {
        this.documentEncoding = Optional.of(documentEncoding);
    }
}
