package no.nav.sbl.dialogarena.retry;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlConsumer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;

import static no.nav.sbl.dialogarena.retry.RetryUtils.retryConfig;
import static no.nav.sbl.dialogarena.retry.RetryUtils.withRetry;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

@RunWith(MockitoJUnitRunner.class)
public class RetryUtilsTest {

    private static final Logger log = getLogger(RetryUtilsTest.class);
    private static final String URL = "test.com";
    private static final int MAX_ATTEMPTS = 2;

    @Mock
    private PdlConsumer pdlConsumer;

    @Test
    public void skalForsokeLikeMangeGangerSomMaxAttempts() {
        when(pdlConsumer.hentPerson(anyString())).thenThrow(new InternalServerErrorException());

        var retry = retryConfig(URL, MAX_ATTEMPTS, 0, new Class[]{WebApplicationException.class}, log);

        Assert.assertThrows(InternalServerErrorException.class, () -> withRetry(retry, () -> pdlConsumer.hentPerson("ident")));
        verify(pdlConsumer, times(MAX_ATTEMPTS)).hentPerson(anyString());
    }

    @Test
    public void skalIkkeRetryVedAnnenException() {
        when(pdlConsumer.hentPerson(anyString())).thenThrow(new IllegalStateException());

        var retry = retryConfig(URL, MAX_ATTEMPTS, 0, new Class[]{WebApplicationException.class}, log);

        Assert.assertThrows(IllegalStateException.class, () -> withRetry(retry, () -> pdlConsumer.hentPerson("ident")));
        verify(pdlConsumer, times(1)).hentPerson(anyString());
    }
}