package gov.samhsa.mhc.dss.infrastructure.valueset.dto;

public class ConceptCodeAndCodeSystemOidDto {
    /**
     * The concept code.
     */
    private String conceptCode;

    /**
     * The code system oid.
     */
    private String codeSystemOid;

    public ConceptCodeAndCodeSystemOidDto() {
    }

    public ConceptCodeAndCodeSystemOidDto(String conceptCode, String codeSystemOid) {
        this.conceptCode = conceptCode;
        this.codeSystemOid = codeSystemOid;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
    }

    public String getCodeSystemOid() {
        return codeSystemOid;
    }

    public void setCodeSystemOid(String codeSystemOid) {
        this.codeSystemOid = codeSystemOid;
    }


}
