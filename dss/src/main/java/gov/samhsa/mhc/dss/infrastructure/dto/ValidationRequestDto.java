/**
 * Created by Jiahao.Li on 4/13/2016.
 */
package gov.samhsa.mhc.dss.infrastructure.dto;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ValidationRequestDto {

    public static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.toString();

    @NotNull
    private byte[] document;
    private String documentEncoding = DEFAULT_ENCODING;

    public byte[] getDocument() {
        return document;
    }

    public void setDocument(byte[] document) {
        this.document = document;
    }

    public String getDocumentEncoding() {
        return documentEncoding;
    }

    public void setDocumentEncoding(String documentEncoding) {
        this.documentEncoding = documentEncoding;
    }
}
