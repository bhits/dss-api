package gov.samhsa.mhc.dss.web;

import ch.qos.logback.audit.AuditException;
import gov.samhsa.mhc.common.validation.exception.XmlDocumentReadFailureException;
import gov.samhsa.mhc.dss.service.DocumentSegmentation;
import gov.samhsa.mhc.dss.service.dto.DSSRequest;
import gov.samhsa.mhc.dss.service.dto.DSSResponse;
import gov.samhsa.mhc.dss.service.exception.InvalidSegmentedClinicalDocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class DocumentSegmentationRestController {

    @Autowired
    private DocumentSegmentation documentSegmentation;

    @RequestMapping("/")
    public String index() {
        return "Welcome to Document Segmentation Service";
    }

    @RequestMapping(value = "/segmentedDocument", method = RequestMethod.POST)
    public DSSResponse segment(@Valid @RequestBody DSSRequest request) throws InvalidSegmentedClinicalDocumentException, AuditException, XmlDocumentReadFailureException {
        return documentSegmentation.segmentDocument(request);
    }
}
