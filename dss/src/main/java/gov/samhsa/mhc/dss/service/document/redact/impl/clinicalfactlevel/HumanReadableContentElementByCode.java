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
package gov.samhsa.mhc.dss.service.document.redact.impl.clinicalfactlevel;

import gov.samhsa.mhc.brms.domain.ClinicalFact;
import gov.samhsa.mhc.brms.domain.FactModel;
import gov.samhsa.mhc.brms.domain.RuleExecutionContainer;
import gov.samhsa.mhc.brms.domain.XacmlResult;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.mhc.dss.service.document.redact.base.AbstractClinicalFactLevelRedactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

import java.util.Optional;

/**
 * The Class HumanReadableContentElementByCode.
 */
@Service
public class HumanReadableContentElementByCode extends
        AbstractClinicalFactLevelRedactionHandler {

    /**
     * The Constant XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_DISPLAY_NAME.
     */
    public static final String XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_DISPLAY_NAME = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section[child::hl7:entry[child::hl7:generatedEntryId/text()='%1']]/hl7:text/hl7:content//text()[contains(lower-case(.), '%2')]/ancestor::hl7:content";

    /**
     * The Constant XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE.
     */
    public static final String XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE = "/hl7:ClinicalDocument/hl7:component/hl7:structuredBody/hl7:component/hl7:section[child::hl7:entry[child::hl7:generatedEntryId/text()='%1']]/hl7:text/hl7:content//text()[contains(lower-case(.), '%2')]/ancestor::hl7:content/following-sibling::node()[position()=1 and self::text()]";

    /**
     * Instantiates a new document node collector for human readable content
     * element by code.
     *
     * @param documentAccessor the document accessor
     */
    @Autowired
    public HumanReadableContentElementByCode(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    @Override
    public RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult,
                                          FactModel factModel, Document factModelDocument, ClinicalFact fact,
                                          RuleExecutionContainer ruleExecutionContainer) {
        return Optional.ofNullable(fact.getCode())
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .map(code -> collectingContentRedactionResults(xmlDocument, xacmlResult, fact, code))
                .orElseGet(RedactionHandlerResult::new);
    }

    private RedactionHandlerResult collectingContentRedactionResults(Document xmlDocument, XacmlResult xacmlResult, ClinicalFact fact, String code) {
        // Find matching category
        final Optional<String> foundCategoryAsOptional = findMatchingCategoryAsOptional(xacmlResult, fact);

        // Collect the content element
        final RedactionHandlerResult contentElementResult = foundCategoryAsOptional
                .map(foundCategory -> addNodesToListForSensitiveCategory(foundCategory, xmlDocument, XPATH_HUMAN_READABLE_CONTENT_ELEMENT_BY_DISPLAY_NAME, fact.getEntry(), code))
                .orElseGet(RedactionHandlerResult::new);

        // Collect the text that follows the content element
        // (if exists)s
        final RedactionHandlerResult nextTextNodeOfContentElementResult = foundCategoryAsOptional
                .map(foundCategory -> addNodesToListForSensitiveCategory(foundCategory, xmlDocument, XPATH_HUMAN_READABLE_CONTENT_ELEMENT_NEXT_TEXT_NODE, fact.getEntry(), code))
                .orElseGet(RedactionHandlerResult::new);

        return contentElementResult.concat(nextTextNodeOfContentElementResult);
    }
}
