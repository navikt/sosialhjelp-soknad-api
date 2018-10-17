package no.nav.sbl.dialogarena.soknadinnsending.consumer.adresse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import javax.security.auth.Subject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.concurrency.RestCallContext;

public class AdresseSokConsumerImplTest {

    @Test
    public void simpleRestCallWith404() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", TestSubjectHandler.class.getName());
        
        final ClientMock mock = mockClient();
        when(mock.response.getStatus()).thenReturn(404);
        
        final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl((lala) -> new RestCallContext.Builder()
                .withClient(mock.client).build(), "foobar");
        
        adresseSok.sokAdresse(new Sokedata()
                .withAdresse("Testsveien")
                .withPostnummer("1337"));
    }
    
    @Test
    public void mdcParametersAreAccessible() {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", TestSubjectHandler.class.getName());
        
        final ListAppender<ILoggingEvent> listAppender = createTestLogAppender();
        
        final ClientMock mock = mockClient();
        when(mock.response.getStatus()).thenReturn(500);

        final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl((lala) -> new RestCallContext.Builder()
                    .withClient(mock.client).withTimeoutInMilliseconds(60000).build(), "foobar");
        
        try (MDCCloseable c = MDC.putCloseable("lala", "foobar")) {
            adresseSok.sokAdresse(new Sokedata());
        } catch (RuntimeException e) {
            // expected.
        }
        
        System.out.println(listAppender.list.get(0));
        System.out.println(listAppender.list.get(1));
        
        Assert.assertEquals(1, listAppender.list.size());
        Assert.assertEquals("foobar", listAppender.list.get(0).getMDCPropertyMap().get("lala"));
        
        listAppender.stop();
    }

    private ListAppender<ILoggingEvent> createTestLogAppender() {
        final ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent e) {
                super.append(e);
                e.prepareForDeferredProcessing();
            }
        };
        listAppender.start();
        
        final Logger logger = (Logger) getLogger(AdresseSokConsumerImpl.class);
        logger.addAppender(listAppender);
        
        return listAppender;
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
