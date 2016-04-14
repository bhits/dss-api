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
package gov.samhsa.mhc.dss.service.document.redact.base;

import gov.samhsa.mhc.brms.domain.ClinicalFact;
import gov.samhsa.mhc.brms.domain.XacmlResult;
import gov.samhsa.mhc.common.document.accessor.DocumentAccessor;
import gov.samhsa.mhc.common.log.Logger;
import gov.samhsa.mhc.common.log.LoggerFactory;
import gov.samhsa.mhc.dss.service.document.dto.RedactionHandlerResult;
import gov.samhsa.mhc.dss.service.document.redact.RedactionHandlerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * The Class AbstractRedactionHandler.
 */
public abstract class AbstractRedactionHandler {

    /**
     * The document accessor.
     */
    @Autowired
    protected DocumentAccessor documentAccessor;
    private Logger logger = LoggerFactory.getLogger(this);

    /**
     * Instantiates a new abstract callback.
     *
     * @param documentAccessor the document accessor
     */
    public AbstractRedactionHandler(DocumentAccessor documentAccessor) {
        super();
        this.documentAccessor = documentAccessor;
    }

    protected AbstractRedactionHandler() {
    }

    /**
     * Adds the nodes to list.
     *
     * @param xmlDocument the xml document
     * @param xPathExpr   the x path expr
     * @param values      the values
     * @return the RedactionHandlerResult
     */
    protected final RedactionHandlerResult addNodesToList(Document xmlDocument,
                                                          String xPathExpr,
                                                          String... values) {
        try {
            Stream<Node> nodeStream = documentAccessor
                    .getNodeListAsStream(xmlDocument, xPathExpr, values);
            final List<Node> listOfNodes = nodeStream
                    .map(this::markRedactForTryPolicyIfElement)
                    .collect(toList());
            RedactionHandlerResult redactionHandlerResult = new RedactionHandlerResult();
            redactionHandlerResult.setRedactNodeList(listOfNodes);
            if (!listOfNodes.isEmpty() && values != null && values.length > 0) {
                redactionHandlerResult.getRedactSectionCodesAndGeneratedEntryIds().add(values[0]);
            }
            return redactionHandlerResult;
        } catch (XPathExpressionException e) {
            throw new RedactionHandlerException(e);
        }
    }

    protected Optional<Node> nullSafeRemove(Node node) {
        Optional<Node> response = Optional.empty();
        if (Objects.nonNull(node)) {
            Node parent = node.getParentNode();
            if (Objects.nonNull(parent)) {
                try {
                    response = Optional.ofNullable(parent.removeChild(node));
                } catch (NullPointerException e) {
                    logger.info(
                            () -> new StringBuilder().append("Node Name: '")
                                    .append(node.getNodeName())
                                    .append("'").append("; Node Value: '")
                                    .append(node.getNodeValue())
                                    .append("'")
                                    .append("; has already been removed.")
                                    .toString(), e);
                }
            }
        }
        return response;
    }

    protected Node markRedactForTryPolicyIfElement(Node node) {
        if (Node.ELEMENT_NODE == node.getNodeType()) {
            Element element = (Element) node;
            element.setAttribute("redact", "redact");
        }
        return node;
    }

    /**
     * Contains any.
     *
     * @param obligations the obligations
     * @param categories  the categories
     * @return the string
     */
    protected final String containsAny(List<String> obligations,
                                       Set<String> categories) {
        if (obligations != null && categories != null) {
            for (String category : categories) {
                if (obligations.contains(category)) {
                    return category;
                }
            }
        }
        return null;
    }

    /**
     * Find matching category.
     *
     * @param xacmlResult the xacml result
     * @param fact        the fact
     * @return the string
     */
    protected final String findMatchingCategory(XacmlResult xacmlResult,
                                                ClinicalFact fact) {
        return containsAny(xacmlResult.getPdpObligations(),
                fact.getValueSetCategories());
    }

    protected final Optional<String> findMatchingCategoryAsOptional(XacmlResult xacmlResult,
                                                                    ClinicalFact fact) {
        return Optional.ofNullable(findMatchingCategory(xacmlResult, fact));
    }
}
