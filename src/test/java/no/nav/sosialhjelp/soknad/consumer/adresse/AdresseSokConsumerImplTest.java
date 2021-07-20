package no.nav.sosialhjelp.soknad.consumer.adresse;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import no.nav.sosialhjelp.soknad.business.service.adressesok.Sokedata;
import no.nav.sosialhjelp.soknad.consumer.LoggingTestUtils;
import no.nav.sosialhjelp.soknad.consumer.adresse.AdresseSokConsumer.AdressesokRespons;
import no.nav.sosialhjelp.soknad.consumer.concurrency.RestCallContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdresseSokConsumerImplTest {

    @Test
    public void simpleRestCallWith404() {
        final ClientMock mock = mockClient();
        when(mock.response.getStatus()).thenReturn(404);

        final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl(simpleRestCallContext(mock), "foobar");
        final AdressesokRespons adressesokRespons = adresseSok.sokAdresse(new Sokedata().withAdresse("Testeveien"));
        
        assertThat(adressesokRespons.adresseDataList).isEmpty();
        assertThat(adressesokRespons.flereTreff).isFalse();
    }
    
    @Test
    public void skalGiExceptionVed500() {
        final ClientMock mock = mockClient();
        when(mock.response.getStatus()).thenReturn(500);

        final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl(simpleRestCallContext(mock), "foobar");
        
        try  {
            adresseSok.sokAdresse(new Sokedata().withAdresse("Testeveien"));
            fail("Forventer exception");
        } catch (RuntimeException e) {
            // expected.
        }
    }

    @Disabled//Feiler sporadisk pga flere logback som starter opp, Stian jobber med en fiks.
    @Test
    public void mdcParametersAreAccessible() {
        final ListAppender<ILoggingEvent> listAppender = LoggingTestUtils.createTestLogAppender(Level.INFO);
        
        final ClientMock mock = mockClient();
        when(mock.response.getStatus()).thenReturn(500);

        final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl(simpleRestCallContext(mock), "foobar");
        
        try (MDCCloseable c = MDC.putCloseable("lala", "testverdi")) {
            adresseSok.sokAdresse(new Sokedata().withAdresse("Testeveien"));
            fail("Forventer exception");
        } catch (RuntimeException e) {
            // expected.
        }
        
        System.out.println("Følgende meldinger har blitt logget: " + Arrays.toString(listAppender.list.toArray()));

        final ILoggingEvent logEvent = listAppender.list.stream()
                .filter(e -> e.getLevel() == Level.INFO)
                .findFirst()
                .get();
        
        assertThat(logEvent.getMDCPropertyMap().get("lala")).isEqualTo("testverdi");
        
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
        
        adresseSok.sokAdresse(new Sokedata().withAdresse("restCallContext1"));
        
        try {
            adresseSok.sokAdresse(new Sokedata().withAdresse("restCallContext2"));
            fail("Forventer exception");
        } catch (RuntimeException e) {
            // Forventer exception siden restCallContext2 alltid gir 500 svar.
        }
    }
    
    @Test
    public void girTimeout() {
        final CountDownLatch done = new CountDownLatch(1);
        try {
            final ClientMock mock = mockClientWithWait(done);
            
            final RestCallContext restCallContext = new RestCallContext.Builder()
                    .withClient(mock.client)
                    .withExecutorTimeoutInMilliseconds(1)
                    .build();
            final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl(restCallContext, "foobar");
            
            try {
                adresseSok.sokAdresse(new Sokedata().withAdresse("Testsveien"));
                fail("Forventet exception.");
            } catch (RuntimeException e) {
                assertThat(e.getCause()).isInstanceOf(TimeoutException.class);
            }
        } finally {
            done.countDown();
        }
    }
    
    @Test
    public void skalAvviseSokNarKoenErFull() throws Exception {
        skalAvviseSokNarKoenErFullMed(1, 1);
    }
    
    @Test
    public void skalAvviseSokNarKoenErFullMedFlereSamtidigeKallOgOppgaverIKo() throws Exception {
        skalAvviseSokNarKoenErFullMed(4, 12);
    }
    
    private void skalAvviseSokNarKoenErFullMed(int numberOfConcurrentCalls, int queueSize) throws Exception {
        final CountDownLatch requiredNumberOfConcurrentCalls = new CountDownLatch(numberOfConcurrentCalls);
        final CountDownLatch done = new CountDownLatch(1);
        
        try {
            final ClientMock mock = mockClientWithWait(requiredNumberOfConcurrentCalls, done);
            when(mock.response.getStatus()).thenReturn(404);
            
            final RestCallContext restCallContext = new RestCallContext.Builder()
                    .withClient(mock.client)
                    .withConcurrentRequests(numberOfConcurrentCalls)
                    .withMaximumQueueSize(queueSize)
                    .withExecutorTimeoutInMilliseconds(60000)
                    .build();
            final AdresseSokConsumer adresseSok = new AdresseSokConsumerImpl(restCallContext, "foobar");
            final Runnable adressesokCall = () -> {
                adresseSok.sokAdresse(new Sokedata().withAdresse("Testeveien 1"));
            };
            
            executeMaximumNumberOfConcurrentCalls(numberOfConcurrentCalls, adressesokCall);
            requiredNumberOfConcurrentCalls.await();
            fillQueue(queueSize, adressesokCall);
            awaitQueueFilled(queueSize, restCallContext);
            
            try {
                adressesokCall.run();
                fail("Forventer exception.");
            } catch (RejectedExecutionException e) {
                // Forventet.
            }
        } finally {
            done.countDown();
        }
    }

    private void awaitQueueFilled(int queueSize, final RestCallContext restCallContext) throws InterruptedException {
        /*
         * Størrelse på kø kan først sjekkes når antallet samtidige kall
         * man ønsker har blitt oppnådd (ref. requiredNumberOfConcurrentCalls).
         * Dette fordi kallene som skal kjøres samtidig først ligger på køen.
         */
        
        while (restCallContext.currentQueueSize() != queueSize) {
            Thread.sleep(10);
        }
    }

    
    private static void executeMaximumNumberOfConcurrentCalls(int numberOfConcurrentCalls, Runnable r) {
        runTimesInParallell(numberOfConcurrentCalls, r);
    }
    
    private static void fillQueue(int queueSize, Runnable r) {
        runTimesInParallell(queueSize, r);
    }

    private static void runTimesInParallell(int times, Runnable r) {
        for (int i=0; i<times; i++) {
            new Thread(r).start();
        }
    }
    
    private static RestCallContext simpleRestCallContext(final ClientMock mock) {
        return new RestCallContext.Builder()
                .withClient(mock.client)
                .build();
    }

    private static ClientMock mockClientWithWait(CountDownLatch done) {
        return mockClientWithWait(new CountDownLatch(1), done);
    }
    
    private static ClientMock mockClientWithWait(CountDownLatch requiredNumberOfConcurrentCalls, CountDownLatch done) {
        final ClientMock mock = mockClient();
        when(mock.builder.get()).thenAnswer(new Answer<Response>() {
            public Response answer(InvocationOnMock invocation) throws Throwable {
                requiredNumberOfConcurrentCalls.countDown();
                done.await();
                return mock.response;
            }
        });
        return mock;
    }
    
    private static ClientMock mockClient() {
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

    private static final class ClientMock {
        Client client;
        @SuppressWarnings("unused")
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
