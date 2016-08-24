package gov.samhsa.c2s.dss.web;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.c2s.dss.service.DocumentSegmentation;
import gov.samhsa.c2s.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.c2s.dss.service.ClinicalDocumentValidation;
import gov.samhsa.c2s.dss.service.dto.ClinicalDocumentValidationRequest;
import gov.samhsa.c2s.dss.service.dto.ClinicalDocumentValidationResult;
import gov.samhsa.c2s.dss.service.dto.DSSRequest;
import gov.samhsa.c2s.dss.service.dto.DSSResponse;
import gov.samhsa.c2s.dss.service.exception.InvalidOriginalClinicalDocumentException;
import gov.samhsa.c2s.dss.service.exception.InvalidSegmentedClinicalDocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

@RestController
public class DocumentSegmentationRestController {

    @Autowired
    private DocumentSegmentation documentSegmentation;

    @Autowired
    private ClinicalDocumentValidation clinicalDocumentValidation;

    @RequestMapping("/")
    public String index() {
        return "Welcome to Document Segmentation Service";
    }

    @RequestMapping(value = "/segmentedDocument", method = RequestMethod.POST)
    public DSSResponse segment(@Valid @RequestBody DSSRequest request) throws InvalidSegmentedClinicalDocumentException, AuditException, XmlDocumentReadFailureException, InvalidOriginalClinicalDocumentException {
        return documentSegmentation.segmentDocument(request);
    }

    @RequestMapping(value = "/validateDocument", method = RequestMethod.POST)
    public ClinicalDocumentValidationResult validateClinicalDocument(
            @Valid @RequestBody ClinicalDocumentValidationRequest validationRequest) throws InvalidSegmentedClinicalDocumentException, AuditException, IOException, InvalidOriginalClinicalDocumentException {
        return clinicalDocumentValidation.validateClinicalDocument(validationRequest);
    }
}
