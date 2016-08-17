/*******************************************************************************
 * Open Behavioral Health Information Technology Architecture (OBHITA.org)
 * <p/>
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
 * <p/>
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
package gov.samhsa.c2s.dss.service.document.redact.impl.clinicalfactlevel;

import gov.samhsa.c2s.brms.domain.ClinicalFact;
import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.base.AbstractClinicalFactLevelRedactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Objects;
import java.util.Optional;

/**
 * The Class HumanReadableContentElementById.
 */
@Service
public class HumanReadableContentElementById extends
        AbstractClinicalFactLevelRedactionHandler {

    /**
     * The Constant XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_REFERENCE.
     */
    public static final String XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_REFERENCE = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section[child::hl7:entry[child::hl7:generatedEntryId/text()='%1']]/hl7:text/hl7:content[@ID='%2']";

    /**
     * The Constant XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE.
     */
    public static final String XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section[child::hl7:entry[child::hl7:generatedEntryId/text()='%1']]/hl7:text/hl7:content[@ID='%2']/following-sibling::node()[position()=1 and self::text()]";

    /**
     * Instantiates a new document node collector for human readable content
     * element by id.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public HumanReadableContentElementById(
            DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult,
                                          FactModel factModel, Document factModelDocument, ClinicalFact fact,
                                          RuleExecutionContainer ruleExecutionContainer) {
        return findMatchingCategoryAsOptional(xacmlResult, fact)
                .flatMap(foundCategory -> collectingContentRedactionResults(foundCategory, xmlDocument, factModelDocument, fact))
                .orElseGet(RedactionHandlerResult::new);
    }

    private Optional<RedactionHandlerResult> collectingContentRedactionResults(String foundCategory, Document xmlDocument, Document factModelDocument, ClinicalFact fact) {
        return getEntryReferenceIdNodeListAsStream(factModelDocument, fact)
                .filter(Objects::nonNull)
                .map(Node::getNodeValue)
                .filter(StringUtils::hasText)
                .map(nodeValue -> addNodesToListForSensitiveCategory(
                        foundCategory, xmlDocument,
                        XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_REFERENCE,
                        fact.getEntry(), nodeValue)
                        .concat(addNodesToListForSensitiveCategory(
                                foundCategory, xmlDocument,
                                XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE,
                                fact.getEntry(), nodeValue)))
                .reduce(RedactionHandlerResult::concat);
    }
}
