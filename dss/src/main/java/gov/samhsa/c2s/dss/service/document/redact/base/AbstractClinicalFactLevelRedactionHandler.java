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
package gov.samhsa.c2s.dss.service.document.redact.base;

import gov.samhsa.c2s.brms.domain.ClinicalFact;
import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessor;
import gov.samhsa.c2s.common.document.accessor.DocumentAccessorException;
import gov.samhsa.c2s.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.c2s.dss.service.document.redact.RedactionHandlerException;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.stream.Stream;

/**
 * The Class AbstractClinicalFactLevelRedactionHandler.
 */
public abstract class AbstractClinicalFactLevelRedactionHandler extends
        AbstractRedactionHandler {

    /**
     * The Constant XPATH_REFERENCES_BY_ENTRY.
     */
    public static final String XPATH_REFERENCES_BY_ENTRY = "//EntryReference[entry='%1']/reference/text()";

    /**
     * Instantiates a new abstract clinical fact level callback.
     *
     * @param documentAccessor the document accessor
     */
    public AbstractClinicalFactLevelRedactionHandler(DocumentAccessor documentAccessor) {
        super(documentAccessor);
    }

    /**
     * Execute.
     *
     * @param xmlDocument            the xml document
     * @param xacmlResult            the xacml result
     * @param factModel              the fact model
     * @param factModelDocument      the fact model document
     * @param fact                   the fact
     * @param ruleExecutionContainer the rule execution container
     * @return RedactionHandlerResult
     */
    public abstract RedactionHandlerResult execute(Document xmlDocument, XacmlResult xacmlResult,
                                                   FactModel factModel, Document factModelDocument, ClinicalFact fact,
                                                   RuleExecutionContainer ruleExecutionContainer);

    /**
     * Gets the entry reference id node list.
     *
     * @param factModelDocument the fact model document
     * @param fact              the fact
     * @return the entry reference id node list
     */
    protected final NodeList getEntryReferenceIdNodeList(
            Document factModelDocument, ClinicalFact fact) {
        try {
            NodeList references = documentAccessor.getNodeList(factModelDocument,
                    XPATH_REFERENCES_BY_ENTRY, fact.getEntry());
            return references;
        } catch (DocumentAccessorException e) {
            throw new RedactionHandlerException(e);
        }
    }

    protected final Stream<Node> getEntryReferenceIdNodeListAsStream(
            Document factModelDocument, ClinicalFact fact) {
        try {
            Stream<Node> references = documentAccessor.getNodeListAsStream(factModelDocument,
                    XPATH_REFERENCES_BY_ENTRY, fact.getEntry());
            return references;
        } catch (DocumentAccessorException e) {
            throw new RedactionHandlerException(e);
        }
    }

    protected RedactionHandlerResult addNodesToListForSensitiveCategory(String foundCategory,
                                                                        Document xmlDocument,
                                                                        String xPathExpr,
                                                                        String... values) {
        final RedactionHandlerResult redactionHandlerResult = addNodesToList(xmlDocument, xPathExpr, values);
        if (StringUtils.hasText(foundCategory)) {
            redactionHandlerResult.getRedactCategorySet().add(foundCategory);
        }
        return redactionHandlerResult;
    }
}
