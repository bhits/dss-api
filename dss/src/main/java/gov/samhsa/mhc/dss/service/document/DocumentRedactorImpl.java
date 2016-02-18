/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.samhsa.mhc.dss.service.document;

import gov.samhsa.acs.brms.domain.ClinicalFact;
import gov.samhsa.acs.brms.domain.FactModel;
import gov.samhsa.acs.brms.domain.RuleExecutionContainer;
import gov.samhsa.acs.brms.domain.XacmlResult;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessorException;
import gov.samhsa.mhc.common.document.converter.DocumentXmlConverter;
import gov.samhsa.mhc.common.log.Logger;
import gov.samhsa.mhc.common.log.LoggerFactory;
import gov.samhsa.mhc.common.marshaller.SimpleMarshaller;
import gov.samhsa.mhc.dss.service.document.dto.RedactedDocument;
import gov.samhsa.mhc.dss.service.document.redact.base.AbstractClinicalFactLevelRedactionHandler;
import gov.samhsa.mhc.dss.service.document.redact.base.AbstractDocumentLevelRedactionHandler;
import gov.samhsa.mhc.dss.service.document.redact.base.AbstractObligationLevelRedactionHandler;
import gov.samhsa.mhc.dss.service.document.redact.base.AbstractPostRedactionLevelRedactionHandler;
import gov.samhsa.mhc.dss.service.exception.DocumentSegmentationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The Class DocumentRedactorImpl.
 */
@Service
public class DocumentRedactorImpl implements DocumentRedactor {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory
            .getLogger(this.getClass());

    /**
     * The marshaller.
     */
    @Autowired
    private SimpleMarshaller marshaller;

    /**
     * The document xml converter.
     */
    @Autowired
    private DocumentXmlConverter documentXmlConverter;

    /**
     * The document accessor.
     */
    @Autowired
    private DocumentAccessor documentAccessor;

    /**
     * The document level redaction handlers.
     */
    @Autowired
    private Set<AbstractDocumentLevelRedactionHandler> documentLevelRedactionHandlers;

    /**
     * The obligation level redaction handlers.
     */
    @Autowired
    private Set<AbstractObligationLevelRedactionHandler> obligationLevelRedactionHandlers;

    /**
     * The clinical fact level redaction handlers.
     */
    @Autowired
    private Set<AbstractClinicalFactLevelRedactionHandler> clinicalFactLevelRedactionHandlers;

    /**
     * The post redaction level redaction handlers.
     */
    @Autowired
    private Set<AbstractPostRedactionLevelRedactionHandler> postRedactionLevelRedactionHandlers;

    /*
     * (non-Javadoc)
     *
     * @see gov.samhsa.mhc.dss.service.document.DocumentRedactor#
     * cleanUpEmbeddedClinicalDocumentFromFactModel(java.lang.String)
     */
    @Override
    public String cleanUpEmbeddedClinicalDocumentFromFactModel(
            String factModelXml) {
        try {
            final Document factModel = documentXmlConverter
                    .loadDocument(factModelXml);
            final Element embeddedClinicalDocument = documentAccessor
                    .getElement(factModel, "//hl7:EmbeddedClinicalDocument")
                    .get();

            embeddedClinicalDocument.getParentNode().removeChild(
                    embeddedClinicalDocument);
            return documentXmlConverter.convertXmlDocToString(factModel);
        } catch (final DocumentAccessorException e) {
            logger.error(e.getMessage(), e);
            throw new DocumentSegmentationException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.samhsa.mhc.dss.service.document.DocumentRedactor#
     * cleanUpGeneratedEntryIds(java.lang.String)
     */
    @Override
    public String cleanUpGeneratedEntryIds(String document) {
        // Remove all generatedEntryId elements to clean up the clinical
        // document
        final String xPathExprGeneratedEntryId = "//hl7:generatedEntryId";
        return cleanUpElements(document, xPathExprGeneratedEntryId);
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.samhsa.mhc.dss.service.document.DocumentRedactor#
     * cleanUpGeneratedServiceEventIds(java.lang.String)
     */
    @Override
    public String cleanUpGeneratedServiceEventIds(String document) {
        // Remove all generatedServiceEventId elements to clean up the clinical
        // document
        final String xPathExprGeneratedEntryId = "//hl7:generatedServiceEventId";
        return cleanUpElements(document, xPathExprGeneratedEntryId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gov.samhsa.mhc.dss.service.document.DocumentRedactor#redactDocument
     * (java.lang.String, gov.samhsa.acs.brms.domain.RuleExecutionContainer,
     * gov.samhsa.acs.brms.domain.XacmlResult,
     * gov.samhsa.acs.brms.domain.FactModel)
     */
    @Override
    public RedactedDocument redactDocument(String document,
                                           RuleExecutionContainer ruleExecutionContainer, FactModel factModel) {

        Document xmlDocument = null;
        String tryPolicyDocument = null;
        final List<Node> redactNodeList = new LinkedList<Node>();
        final Set<String> redactSectionCodesAndGeneratedEntryIds = new HashSet<String>();
        final Set<String> redactSectionSet = new HashSet<String>();
        final Set<String> redactCategorySet = new HashSet<String>();
        final XacmlResult xacmlResult = factModel.getXacmlResult();

        try {
            xmlDocument = documentXmlConverter.loadDocument(document);
            final Document factModelDocument = documentXmlConverter
                    .loadDocument(marshaller.marshal(factModel));

            // Document Level redaction handlers
            for (final AbstractDocumentLevelRedactionHandler documentLevelRedactionHandler : documentLevelRedactionHandlers) {
                documentLevelRedactionHandler.execute(xmlDocument,
                        redactSectionCodesAndGeneratedEntryIds, redactNodeList);
            }

            // OBLIGATION LEVEL REDACTION HANDLERS
            for (final String obligation : xacmlResult.getPdpObligations()) {
                for (final AbstractObligationLevelRedactionHandler obligationLevelRedactionHandler : obligationLevelRedactionHandlers) {
                    obligationLevelRedactionHandler.execute(xmlDocument,
                            xacmlResult, factModel, factModelDocument,
                            ruleExecutionContainer, redactNodeList,
                            redactSectionCodesAndGeneratedEntryIds,
                            redactSectionSet, obligation);
                }
            }

            // CLINICAL FACT LEVEL REDACTION HANDLERS
            for (final ClinicalFact fact : factModel.getClinicalFactList()) {
                // For each clinical fact
                for (final AbstractClinicalFactLevelRedactionHandler clinicalFactLevelRedactionHandler : clinicalFactLevelRedactionHandlers) {
                    clinicalFactLevelRedactionHandler.execute(xmlDocument,
                            xacmlResult, factModel, factModelDocument, fact,
                            ruleExecutionContainer, redactNodeList,
                            redactSectionCodesAndGeneratedEntryIds,
                            redactCategorySet);
                }
            }

            // Create tryPolicyDocument before the actual redacting
            tryPolicyDocument = documentXmlConverter
                    .convertXmlDocToString(xmlDocument);

            // REDACTION
            // Redact all nodes in redactNodeList
            // (sections, entries, text nodes)
            for (final Node nodeToBeReadacted : redactNodeList) {
                redactNodeIfNotNull(nodeToBeReadacted);
            }

            // POST REDACTION LEVEL REDACTION HANDLERS
            for (final AbstractPostRedactionLevelRedactionHandler postRedactionRedactionHandler : postRedactionLevelRedactionHandlers) {
                postRedactionRedactionHandler.execute(xmlDocument, xacmlResult,
                        factModel, factModelDocument, ruleExecutionContainer,
                        redactNodeList, redactSectionCodesAndGeneratedEntryIds);
            }

            // Convert redacted document to xml string
            document = documentXmlConverter.convertXmlDocToString(xmlDocument);

            // Debug
            // FileHelper.writeDocToFile(xmlDocument, "Redacted_C32.xml");
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new DocumentSegmentationException(e.toString(), e);
        }
        return new RedactedDocument(document, tryPolicyDocument,
                redactSectionSet, redactCategorySet);
    }

    /**
     * Clean up elements.
     *
     * @param document  the document
     * @param xPathExpr the x path expr
     * @return the string
     */
    private String cleanUpElements(String document, String xPathExpr) {
        Document xmlDocument = null;
        try {
            xmlDocument = documentXmlConverter.loadDocument(document);

            NodeList nodes = null;

            nodes = documentAccessor.getNodeList(xmlDocument, xPathExpr);

            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    final Node node = nodes.item(i);
                    final Element element = (Element) node;
                    element.getParentNode().removeChild(element);
                }
            }
            document = documentXmlConverter.convertXmlDocToString(xmlDocument);
        } catch (final XPathExpressionException e) {
            logger.error(e.getMessage(), e);
            throw new DocumentSegmentationException(e.toString(), e);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new DocumentSegmentationException(e.toString(), e);
        }
        return document;
    }

    /**
     * Redact node if not null.
     *
     * @param nodeToBeRedacted the node to be redacted
     */
    private void redactNodeIfNotNull(Node nodeToBeRedacted) {
        if (nodeToBeRedacted != null) {
            // If displayName contains the code, it will be found twice and can
            // already be removed. Therefore, we need to check the parent
            try {
                nodeToBeRedacted.getParentNode().removeChild(nodeToBeRedacted);
            } catch (final NullPointerException e) {
                logger.info(() -> new StringBuilder()
                        .append("The node value '")
                        .append(nodeToBeRedacted.getNodeValue())
                        .append("' must have been removed already, it cannot be removed again. This might happen if one of the search text contains the other and multiple criterias match to mark the node to be redacted.")
                        .toString());
            }
        }
    }
}
