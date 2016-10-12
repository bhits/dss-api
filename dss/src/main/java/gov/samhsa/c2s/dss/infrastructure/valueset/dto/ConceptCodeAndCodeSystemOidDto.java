package gov.samhsa.c2s.dss.infrastructure.valueset.dto;

import lombok.Data;

@Data
public class ConceptCodeAndCodeSystemOidDto {

    private String conceptCode;
    private String codeSystemOid;

    public ConceptCodeAndCodeSystemOidDto() {
    }

    public ConceptCodeAndCodeSystemOidDto(String conceptCode, String codeSystemOid) {
        this.conceptCode = conceptCode;
        this.codeSystemOid = codeSystemOid;
    }

}
