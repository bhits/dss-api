package gov.samhsa.c2s.brms.service;

import gov.samhsa.c2s.brms.domain.ClinicalFact;
import gov.samhsa.c2s.brms.domain.FactModel;
import gov.samhsa.c2s.brms.domain.RuleExecutionContainer;
import gov.samhsa.c2s.brms.domain.XacmlResult;
import gov.samhsa.c2s.brms.service.dto.AssertAndExecuteClinicalFactsResponse;
import gov.samhsa.c2s.brms.service.guvnor.GuvnorService;
import gov.samhsa.c2s.common.marshaller.SimpleMarshaller;
import gov.samhsa.c2s.common.marshaller.SimpleMarshallerException;
import gov.samhsa.c2s.common.unit.io.ResourceFileReader;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RuleExecutionServiceImplTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private GuvnorService guvnorServiceMock;
    @Mock
    private SimpleMarshaller marshallerMock;
    @Spy
    @InjectMocks
    private RuleExecutionServiceImpl sut;

    @Test
    public void testAssertAndExecuteClinicalFacts() throws JAXBException {
        // Arrange
        StatefulKnowledgeSession sessionMock = mock(StatefulKnowledgeSession.class);
        doReturn(sessionMock).when(sut).createStatefulKnowledgeSession();
        String factModelStringMock = "factModelStringMock";
        FactModel factModelMock = mock(FactModel.class);
        when(
                marshallerMock.unmarshalFromXml(FactModel.class,
                        factModelStringMock)).thenReturn(factModelMock);
        XacmlResult xacmlResultMock = mock(XacmlResult.class);
        when(factModelMock.getXacmlResult()).thenReturn(xacmlResultMock);
        LinkedList<ClinicalFact> clinicalFactListMock = new LinkedList<>();
        ClinicalFact clinicalFactMock1 = mock(ClinicalFact.class);
        ClinicalFact clinicalFactMock2 = mock(ClinicalFact.class);
        clinicalFactListMock.add(clinicalFactMock1);
        clinicalFactListMock.add(clinicalFactMock2);
        when(factModelMock.getClinicalFactList()).thenReturn(
                clinicalFactListMock).thenReturn(clinicalFactListMock);
        when(sessionMock.insert(clinicalFactMock1)).thenReturn(null);
        when(sessionMock.insert(clinicalFactMock2)).thenReturn(null);
        doNothing().when(sessionMock).addEventListener(
                isA(DefaultAgendaEventListener.class));
        when(sessionMock.fireAllRules()).thenReturn(0);
        RuleExecutionContainer ruleExecutionContainerMock = mock(RuleExecutionContainer.class);
        when(sessionMock.getGlobal("ruleExecutionContainer")).thenReturn(
                ruleExecutionContainerMock);
        String executionResponseContainerXMLStringMock = "executionResponseContainerXMLStringMock";
        when(marshallerMock.marshal(ruleExecutionContainerMock)).thenReturn(
                executionResponseContainerXMLStringMock);

        // Act
        AssertAndExecuteClinicalFactsResponse response = sut
                .assertAndExecuteClinicalFacts(factModelStringMock);

        // Assert
        assertNotNull(response);
        assertEquals(executionResponseContainerXMLStringMock,
                response.getRuleExecutionResponseContainer());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssertAndExecuteClinicalFacts_Marshaller_Throws_JAXBException()
            throws Throwable {
        // Arrange
        StatefulKnowledgeSession sessionMock = mock(StatefulKnowledgeSession.class);
        doReturn(sessionMock).when(sut).createStatefulKnowledgeSession();
        String factModelStringMock = "factModelStringMock";
        when(
                marshallerMock.unmarshalFromXml(FactModel.class,
                        factModelStringMock)).thenThrow(SimpleMarshallerException.class);

        // Act
        AssertAndExecuteClinicalFactsResponse response = sut
                .assertAndExecuteClinicalFacts(factModelStringMock);

        // Assert
        assertNotNull(response);
        assertNull(response.getRuleExecutionResponseContainer());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAssertAndExecuteClinicalFacts_Marshaller_Throws_JAXBException2()
            throws JAXBException {
        // Arrange
        StatefulKnowledgeSession sessionMock = mock(StatefulKnowledgeSession.class);
        doReturn(sessionMock).when(sut).createStatefulKnowledgeSession();
        String factModelStringMock = "factModelStringMock";
        FactModel factModelMock = mock(FactModel.class);
        when(
                marshallerMock.unmarshalFromXml(FactModel.class,
                        factModelStringMock)).thenReturn(factModelMock);
        XacmlResult xacmlResultMock = mock(XacmlResult.class);
        when(factModelMock.getXacmlResult()).thenReturn(xacmlResultMock);
        LinkedList<ClinicalFact> clinicalFactListMock = new LinkedList<>();
        ClinicalFact clinicalFactMock1 = mock(ClinicalFact.class);
        ClinicalFact clinicalFactMock2 = mock(ClinicalFact.class);
        clinicalFactListMock.add(clinicalFactMock1);
        clinicalFactListMock.add(clinicalFactMock2);
        when(factModelMock.getClinicalFactList()).thenReturn(
                clinicalFactListMock).thenReturn(clinicalFactListMock);
        when(sessionMock.insert(clinicalFactMock1)).thenReturn(null);
        when(sessionMock.insert(clinicalFactMock2)).thenReturn(null);
        doNothing().when(sessionMock).addEventListener(
                isA(DefaultAgendaEventListener.class));
        when(sessionMock.fireAllRules()).thenReturn(0);
        RuleExecutionContainer ruleExecutionContainerMock = mock(RuleExecutionContainer.class);
        when(sessionMock.getGlobal("ruleExecutionContainer")).thenReturn(
                ruleExecutionContainerMock);
        when(marshallerMock.marshal(ruleExecutionContainerMock)).thenThrow(
                JAXBException.class);

        // Act
        AssertAndExecuteClinicalFactsResponse response = sut
                .assertAndExecuteClinicalFacts(factModelStringMock);

        // Assert
        assertNotNull(response);
        assertNull(response.getRuleExecutionResponseContainer());
    }

    @Test
    public void testCreateStatefulKnowledgeSession() throws IOException {
        // Arrange
        String rulesMock = ResourceFileReader.getStringFromResourceFile("unitTestRules.txt");
        when(guvnorServiceMock.getVersionedRulesFromPackage()).thenReturn(
                rulesMock);

        // Act
        StatefulKnowledgeSession session = sut.createStatefulKnowledgeSession();

        // Assert
        assertNotNull(session);
    }
}
