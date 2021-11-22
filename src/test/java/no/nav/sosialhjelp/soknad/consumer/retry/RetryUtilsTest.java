//package no.nav.sosialhjelp.soknad.consumer.retry;
//
//import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlHentPersonConsumer;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.slf4j.Logger;
//
//import javax.ws.rs.InternalServerErrorException;
//import javax.ws.rs.WebApplicationException;
//
//import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.retryConfig;
//import static no.nav.sosialhjelp.soknad.consumer.retry.RetryUtils.withRetry;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.slf4j.LoggerFactory.getLogger;
//
//@ExtendWith(MockitoExtension.class)
//class RetryUtilsTest {
//
//    private static final Logger log = getLogger(RetryUtilsTest.class);
//    private static final String URL = "test.com";
//    private static final int MAX_ATTEMPTS = 2;
//
//    @Mock
//    private PdlHentPersonConsumer pdlHentPersonConsumer;
//
//    @Test
//    void skalForsokeLikeMangeGangerSomMaxAttempts() {
//        when(pdlHentPersonConsumer.hentPerson(anyString())).thenThrow(new InternalServerErrorException());
//
//        var retry = retryConfig(URL, MAX_ATTEMPTS, 1, 2.0, new Class[]{WebApplicationException.class}, log);
//
//        assertThatExceptionOfType(InternalServerErrorException.class)
//                .isThrownBy(() -> withRetry(retry, () -> pdlHentPersonConsumer.hentPerson("ident")));
//        verify(pdlHentPersonConsumer, times(MAX_ATTEMPTS)).hentPerson(anyString());
//    }
//
//    @Test
//    void skalIkkeRetryVedAnnenException() {
//        when(pdlHentPersonConsumer.hentPerson(anyString())).thenThrow(new IllegalStateException());
//
//        var retry = retryConfig(URL, MAX_ATTEMPTS, 1, 2.0, new Class[]{WebApplicationException.class}, log);
//
//        assertThatExceptionOfType(IllegalStateException.class)
//                .isThrownBy(() -> withRetry(retry, () -> pdlHentPersonConsumer.hentPerson("ident")));
//        verify(pdlHentPersonConsumer, times(1)).hentPerson(anyString());
//    }
//}