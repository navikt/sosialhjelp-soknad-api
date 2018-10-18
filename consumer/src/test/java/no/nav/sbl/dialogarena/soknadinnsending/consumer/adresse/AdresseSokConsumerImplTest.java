package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import javax.security.auth.Subject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdressesokRespons;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LoggingTestUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallContext;

public class AdresseSokConsumerImplTest {

    /**
     * Angir ventetid i millisekunder ved testing av asynkron/parallell kode. 
     * 
     * Verdien bør økes hvis testene sporadisk feiler.
     */
    private static final int EXECUTION_DELAY = 500;

    private static final String PROPERTY_SUBJECT_HANDLER = "no.nav.modig.core.context.subjectHandlerImplementationClass";
    
    private static String oldSubjectHandlerImplementationClass;
    
    
    @BeforeClass
    public static void beforeClass() {
        oldSubjectHandlerImplementationClass = System.getProperty(PROPERTY_SUBJECT_HANDLER, "");
        System.setProperty(PROPERTY_SUBJECT_HANDLER, TestSubjectHandler.class.getName());
    }
    
    @AfterClass
    public static void afterClass() {
        System.setProperty(PROPERTY_SUBJECT_HANDLER, oldSubjectHandlerImplementationClass);
    }
    
    
    @Test
    public void simpleRestCallWith404() {
        final ClientMock mock = mockClient();
        when(mock.response.getStatus()).thenReturn(404);

        final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl(simpleRestCallContext(mock), "foobar");
        
        final AdressesokRespons adressesokRespons = adresseSok.sokAdresse(new Sokedata()
                .withAdresse("Testeveien")
                .withPostnummer("1337"));
        
        Assert.assertTrue(adressesokRespons.adresseDataList.isEmpty());
        Assert.assertEquals(false, adressesokRespons.flereTreff);
    }
    
    @Test
    public void skalGiExceptionVed500() {
        final ClientMock mock = mockClient();
        when(mock.response.getStatus()).thenReturn(500);

        final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl(simpleRestCallContext(mock), "foobar");
        
        try  {
            adresseSok.sokAdresse(new Sokedata());
            Assert.fail("Forventer exception");
        } catch (RuntimeException e) {
            // expected.
        }
    }
    
    @Test
    public void mdcParametersAreAccessible() {
        final ListAppender<ILoggingEvent> listAppender = LoggingTestUtils.createTestLogAppender();
        
        final ClientMock mock = mockClient();
        when(mock.response.getStatus()).thenReturn(500);

        final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl(simpleRestCallContext(mock), "foobar");
        
        try (MDCCloseable c = MDC.putCloseable("lala", "foobar")) {
            adresseSok.sokAdresse(new Sokedata());
            Assert.fail("Forventer exception");
        } catch (RuntimeException e) {
            // expected.
        }
        
        Assert.assertEquals("foobar", listAppender.list.get(0).getMDCPropertyMap().get("lala"));
        
        listAppender.stop();
    }
    
    @Test
    public void skalKunneBrukeForskjelligeRestCallContext() {
        final ClientMock mock1 = mockClient();
        when(mock1.response.getStatus()).thenReturn(404);
        final RestCallContext restCallContext1 = simpleRestCallContext(mock1);
        
        final ClientMock mock2 = mockClient();
        when(mock2.response.getStatus()).thenReturn(500);
        final RestCallContext restCallContext2 = simpleRestCallContext(mock2);
        
        final Function<Sokedata, RestCallContext> restCallContextSelector = (sokedata) -> 
                sokedata.adresse.equals("restCallContext1") ? restCallContext1 : restCallContext2;

        final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl(restCallContextSelector, "foobar");
        
        adresseSok.sokAdresse(new Sokedata()
                .withAdresse("restCallContext1")
                .withPostnummer("1337"));
        
        try {
            adresseSok.sokAdresse(new Sokedata()
                    .withAdresse("restCallContext2")
                    .withPostnummer("1337"));
            Assert.fail("Forventer exception");
        } catch (RuntimeException e) {
            // Forventer exception siden restCallContext2 alltid gir 500 svar.
        }
    }
    
    @Test
    public void girTimeout() {
        final ClientMock mock = mockClientWithDelay(EXECUTION_DELAY);
        when(mock.response.getStatus()).thenReturn(404);
        
        final RestCallContext restCallContext = new RestCallContext.Builder()
                .withClient(mock.client)
                .withTimeoutInMilliseconds(EXECUTION_DELAY / 2)
                .build();
        final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl(restCallContext, "foobar");
        
        try {
            adresseSok.sokAdresse(new Sokedata()
                    .withAdresse("Testsveien")
                    .withPostnummer("1337"));
            Assert.fail("Forventet exception.");
        } catch (RuntimeException e) {
            Assert.assertEquals(TimeoutException.class, e.getCause().getClass());
        }
    }
    
    @Test
    public void skalAvviseSokNarKoenErFull() throws Exception {
        final ClientMock mock = mockClientWithDelay(EXECUTION_DELAY);
        when(mock.response.getStatus()).thenReturn(404);
        
        final RestCallContext restCallContext = new RestCallContext.Builder()
                .withClient(mock.client)
                .withConcurrentRequests(1)
                .withMaximumQueueSize(1)
                .withTimeoutInMilliseconds(EXECUTION_DELAY * 2)
                .build();
        final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl(restCallContext, "foobar");
        
        // Ett kall virker:
        adresseSok.sokAdresse(new Sokedata().withAdresse("Testeveien 1")
                .withPostnummer("1337"));
        
        // Full kø:
        new Thread(() -> {
            adresseSok.sokAdresse(new Sokedata().withAdresse("Testeveien 2").withPostnummer("1337"));
        }).start();
        new Thread(() -> {
            adresseSok.sokAdresse(new Sokedata().withAdresse("Testeveien 3")
                    .withPostnummer("1337"));
        }).start();
        Thread.sleep(EXECUTION_DELAY / 2);
        
        try {
            adresseSok.sokAdresse(new Sokedata().withAdresse("Testeveien 4")
                    .withPostnummer("1337"));
            Assert.fail("Forventer exception.");
        } catch (RejectedExecutionException e) {
            // Expected.
        }
    }

    
    private RestCallContext simpleRestCallContext(final ClientMock mock) {
        return new RestCallContext.Builder()
                .withClient(mock.client)
                .build();
    }

    private ClientMock mockClientWithDelay(int delayInMilliseconds) {
        final ClientMock mock = mockClient();
        when(mock.builder.get()).thenAnswer(new Answer<Response>() {
            public Response answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(delayInMilliseconds);
                return mock.response;
            }
        });
        return mock;
    }
    
    private ClientMock mockClient() {
        /* 
         * Denne metoden skal kun returnere mock-instanser. Faktiske returverdier skal defineres av testene.
         * 
         */
        final Client client = mock(Client.class);
        final WebTarget webTarget = mock(WebTarget.class);
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.queryParam(anyString(), any())).thenReturn(webTarget);
        
        final Builder builder = mock(Builder.class);
        when(webTarget.request()).thenReturn(builder);
        when(builder.header(anyString(), any())).thenReturn(builder);
        
        final Response response = mock(Response.class);
        when(builder.get()).thenReturn(response);
        
        return new ClientMock(client, webTarget, builder, response);
    }
    
    public static class TestSubjectHandler extends SubjectHandler {
        @Override
        public Subject getSubject() {
            return null;
        }
    }
    
    @SuppressWarnings("unused")
    private static final class ClientMock {
        Client client;
        WebTarget webTarget;
        Builder builder;
        Response response;
        
        private ClientMock(Client client, WebTarget webTarget, Builder builder, Response response) {
            this.client = client;
            this.webTarget = webTarget;
            this.builder = builder;
            this.response = response;
        }
    }
}
