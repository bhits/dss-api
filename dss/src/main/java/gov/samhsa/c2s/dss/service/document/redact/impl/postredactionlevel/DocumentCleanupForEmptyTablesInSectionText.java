package gov.samhsa.c2s.dss.service.document.redact.impl.postredactionlevel;

import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorException;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.RedactionHandlerException;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractPostRedactionLevelRedactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Service
public class DocumentCleanupForEmptyTablesInSectionText extends AbstractPostRedactionLevelRedactionHandler {

    public static final String XPATH_SECTION_TEXT_TABLE_WITH_NO_TBODY = "//hl7:structuredBody//hl7:section/hl7:text/hl7:table[not(hl7:tbody)]";
    public static final String XPATH_SECTION_TEXT_TABLE_WITH_NO_TR_IN_TBODY = "//hl7:structuredBody//hl7:section/hl7:text/hl7:table[hl7:tbody[not(hl7:tr)]]";

    @Autowired
    public DocumentCleanupForEmptyTablesInSectionText(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public void execute(Document xmlDocument, XacmlResult xacmlResult, FactModel factModel, Document factModelDocument, RuleExecutionContainer ruleExecutionContainer, RedactionHandlerResult preRedactionResults) {
        try {
            documentAccessor.getNodeListAsStream(xmlDocument, XPATH_SECTION_TEXT_TABLE_WITH_NO_TBODY)
                    .forEach(this::nullSafeRemove);
            documentAccessor.getNodeListAsStream(xmlDocument, XPATH_SECTION_TEXT_TABLE_WITH_NO_TR_IN_TBODY)
                    .forEach(this::nullSafeRemove);
        } catch (DocumentAccessorException e) {
            throw new RedactionHandlerException(e);
        }
    }
}
