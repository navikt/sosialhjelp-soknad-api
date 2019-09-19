package no.nav.sbl.dialogarena.bostotte;

import no.nav.sbl.dialogarena.bostotte.dto.BostotteDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
    public void hentBostotte_checkUrl_correctUrlReturned() {
        String configUrl = "http://magicUri";
        when(config.getUri()).thenReturn(configUrl);
        BostotteDto bostotteDto = new BostotteDto();
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(bostotteDto));
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();
        assertThat(bostotte.hentBostotte(fra,til)).isEqualTo(bostotteDto);
        Mockito.verify(operations).exchange(captor.capture(), any(Class.class));
        assertThat(captor.getValue().getUrl().toString()).startsWith(configUrl);
    }
}
