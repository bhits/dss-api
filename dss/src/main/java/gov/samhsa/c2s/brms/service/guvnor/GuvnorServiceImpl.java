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
package gov.samhsa.c2s.brms.service.guvnor;

import gov.samhsa.c2s.common.log.Logger;
import gov.samhsa.c2s.common.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * The Class GuvnorServiceImpl.
 */
@Service
public class GuvnorServiceImpl implements GuvnorService {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory
            .getLogger(this.getClass());

    /**
     * The endpoint address.
     */
    private String endpointAddress;

    /**
     * The guvnor service username.
     */
    private String guvnorServiceUsername;

    /**
     * The guvnor service password.
     */
    private String guvnorServicePassword;

    @Autowired
    public GuvnorServiceImpl(@Value("${c2s.brms.guvnor.endpointAddress}") String endpointAddress,
                             @Value("${c2s.brms.guvnor.serviceUsername}") String guvnorServiceUsername,
                             @Value("${c2s.brms.guvnor.servicePassword}") String guvnorServicePassword) {
        this.endpointAddress = endpointAddress;
        this.guvnorServiceUsername = guvnorServiceUsername;
        this.guvnorServicePassword = guvnorServicePassword;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gov.samhsa.c2s.brms.RuleExecutionService
     * #getVersionedRulesFromPackage()
     */
    @Override
    public String getVersionedRulesFromPackage() throws IOException {
        final HttpURLConnection connection = openConnection(this.endpointAddress);

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", MediaType.TEXT_PLAIN_VALUE);
        final StringBuilder passwordStringBuilder = new StringBuilder();
        passwordStringBuilder.append(guvnorServiceUsername);
        passwordStringBuilder.append(":");
        passwordStringBuilder.append(guvnorServicePassword);
        final String passwordString = passwordStringBuilder.toString();
        final StringBuilder propertyStringBuilder = new StringBuilder();
        propertyStringBuilder.append("Basic ");
        propertyStringBuilder.append(Base64.getEncoder().encodeToString(passwordString
                .getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Authorization",
                propertyStringBuilder.toString());
        connection.connect();

        final String source = readAsString(connection.getInputStream());
        logger.debug(() -> "DRL source: " + source);
        return source;
    }

    /**
     * Open connection.
     *
     * @param endpointAddress the endpoint address
     * @return the http url connection
     * @throws IOException Signals that an I/O exception has occurred.
     */
    HttpURLConnection openConnection(String endpointAddress) throws IOException {
        final URL url = new URL(endpointAddress);
        final HttpURLConnection connection = (HttpURLConnection) url
                .openConnection();
        return connection;
    }

    /**
     * Read as string.
     *
     * @param inputStream the input stream
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private String readAsString(InputStream inputStream) throws IOException {
        final StringBuffer ret = new StringBuffer();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            final StringBuilder builder = new StringBuilder();
            builder.append(line);
            builder.append("\n");
            ret.append(builder.toString());
        }
        return ret.toString();
    }

}
