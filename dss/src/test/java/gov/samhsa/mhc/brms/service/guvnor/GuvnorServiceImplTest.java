package gov.samhsa.mhc.brms.service.guvnor;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class GuvnorServiceImplTest {

    private static String GUNVOR_REST_URL;

    private static String USERNAME;

    private static String PASSWORD;

    @Before
    public void setUp() throws URISyntaxException, IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL resource = classloader.getResource("guvnor.properties");
        File file = new File(resource.toURI());
        FileInputStream fis = new FileInputStream(file);
        Properties props = new Properties();
        props.load(fis);
        GUNVOR_REST_URL = props.getProperty("guvnor.service");
        USERNAME = props.getProperty("guvnor.username");
        PASSWORD = props.getProperty("guvnor.password");
    }

    @Test
    public void testGetVersionedRulesFromPackage() throws IOException {
        // Arrange
        String mockString = "mockString";
        GuvnorServiceImpl sut = new GuvnorServiceImpl(GUNVOR_REST_URL, USERNAME, PASSWORD);
        GuvnorServiceImpl spy = spy(sut);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(spy.openConnection(GUNVOR_REST_URL)).thenReturn(mockConnection);
        InputStream mockInputStream = new ByteArrayInputStream(mockString.getBytes("UTF-8"));
        when(mockConnection.getInputStream()).thenReturn(mockInputStream);

        // Act
        String response = spy.getVersionedRulesFromPackage();

        // Assert
        verify(mockConnection, times(1)).setRequestMethod("GET");
        verify(mockConnection, times(1)).connect();
        verify(mockConnection, times(1)).getInputStream();
        assertNotNull(response);
        assertNotEquals(mockString, response);
    }
}
