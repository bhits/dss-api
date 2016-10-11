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
package gov.samhsa.c2s.dss.service.document;

import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.common.document.converter.DocumentXmlConverter;
import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import gov.samhsa.c2s.dss.service.exception.DocumentSegmentationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.security.Key;

@Service
public class DocumentMaskerImpl implements DocumentMasker {

    private final Logger logger = LoggerFactory
            .getLogger(this.getClass());

    /**
     * The document xml converter.
     */
    @Autowired
    private DocumentXmlConverter documentXmlConverter;

    public DocumentMaskerImpl() {
    }

    /**
     * Instantiates a new document masker impl.
     *
     * @param documentXmlConverter the document xml converter
     */
    @Autowired
    public DocumentMaskerImpl(DocumentXmlConverter documentXmlConverter) {
        super();
        this.documentXmlConverter = documentXmlConverter;
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.samhsa.c2s.dss.service.document
     * .DocumentMasker#maskDocument(java.lang.String, java.security.Key,
     * gov.samhsa.c2s.common.bean.RuleExecutionContainer,
     * gov.samhsa.c2s.common.bean.XacmlResult)
     */
    @Override
    public String maskDocument(String document, Key deSedeMaskKey,
                               RuleExecutionContainer ruleExecutionContainer,
                               XacmlResult xacmlResult) {
        Document xmlDocument = null;
        String xmlString = null;

        try {
            xmlDocument = documentXmlConverter.loadDocument(document);
            xmlString = documentXmlConverter.convertXmlDocToString(xmlDocument);

        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new DocumentSegmentationException(e.toString(), e);
        }
        return xmlString;
    }

}
