package no.nav.sbl.dialogarena.bostotte;

import no.nav.sbl.dialogarena.bostotte.dto.BostotteDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class BostotteImplTest {
    @Mock
    private BostotteConfig config;

    @Mock
    private RestOperations operations;

    @InjectMocks
    private BostotteImpl bostotte;

    @Captor
    ArgumentCaptor<RequestEntity> captor;

    @Test
    public void hentBostotte_testUrl_riktigUrlBlirSendtInnTilRestKallet() {
        // Variabler:
        String configUrl = "http://magicUri";
        BostotteDto bostotteDto = new BostotteDto();
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(config.getUri()).thenReturn(configUrl);
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(bostotteDto));

        // Test kjøring:
        assertThat(bostotte.hentBostotte(fra,til)).isEqualTo(bostotteDto);
        verify(operations).exchange(captor.capture(), any(Class.class));
        assertThat(captor.getValue().getUrl().toString()).startsWith(configUrl);
    }

    @Test
    public void hentBostotte_testUrl_urlHarRiktigTilOgFraDato() {
        // Variabler:
        String configUrl = "http://magicUri";
        BostotteDto bostotteDto = new BostotteDto();
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(config.getUri()).thenReturn(configUrl);
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(bostotteDto));

        // Test kjøring:
        assertThat(bostotte.hentBostotte(fra,til)).isEqualTo(bostotteDto);
        verify(operations).exchange(captor.capture(), any(Class.class));
        assertThat(captor.getValue().getUrl().toString()).contains("fra=" + fra.toString());
        assertThat(captor.getValue().getUrl().toString()).contains("til=" + til.toString());
    }
}
