package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto.BostotteDto;
import no.nav.sbl.dialogarena.types.Pingable;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
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

    @Before
    public void setUp() {
        when(config.getUri()).thenReturn("uri");
    }

    @Test
    public void hentBostotte_testUrl_riktigUrlBlirSendtInnTilRestKallet() {
        // Variabler:
        String configUrl = "http://magicUri";
        BostotteDto bostotteDto = new BostotteDto();
        String personIdentifikator = "121212123456";
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(config.getUri()).thenReturn(configUrl);
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(bostotteDto));

        // Testkjøring:
        assertThat(bostotte.hentBostotte(personIdentifikator, "", fra,til)).isEqualTo(bostotteDto);
        verify(operations).exchange(captor.capture(), any(Class.class));
        assertThat(captor.getValue().getUrl().toString()).startsWith(configUrl);
    }

    @Test
    public void hentBostotte_testUrl_urlHarRiktigTilOgFraDato() {
        // Variabler:
        String configUrl = "http://magicUri";
        BostotteDto bostotteDto = new BostotteDto();
        String personIdentifikator = "121212123456";
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(config.getUri()).thenReturn(configUrl);
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(bostotteDto));

        // Testkjøring:
        assertThat(bostotte.hentBostotte(personIdentifikator, "", fra,til)).isEqualTo(bostotteDto);
        verify(operations).exchange(captor.capture(), any(Class.class));
        assertThat(captor.getValue().getUrl().toString()).contains("fra=" + fra.toString());
        assertThat(captor.getValue().getUrl().toString()).contains("til=" + til.toString());
    }

    @Test
    public void hentBostotte_testJson_testingJsonTranslation() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream resourceAsStream = ClassLoader.getSystemResourceAsStream("husbankenSvar.json");
        assertThat(resourceAsStream).isNotNull();
        String jsonString = IOUtils.toString(resourceAsStream);

        BostotteDto bostotteDto = objectMapper.readValue(jsonString, BostotteDto.class);

        assertThat(bostotteDto.getSaker()).hasSize(3);
        assertThat(bostotteDto.getSaker().get(0).getVedtak().getType()).isEqualTo("INNVILGET");
        assertThat(bostotteDto.getUtbetalinger()).hasSize(2);
        assertThat(bostotteDto.getUtbetalinger().get(0).getUtbetalingsdato()).isEqualTo(LocalDate.of(2019,7,20));
        assertThat(bostotteDto.getUtbetalinger().get(0).getBelop().doubleValue()).isEqualTo(4300.5);
        assertThat(bostotteDto.getUtbetalinger().get(1).getUtbetalingsdato()).isEqualTo(LocalDate.of(2019,8,20));
        assertThat(bostotteDto.getUtbetalinger().get(1).getBelop().doubleValue()).isEqualTo(4300);
    }

    @Test
    public void hentBostotte_testUrl_overlevNullUrl() {
        // Variabler:
        String personIdentifikator = "121212123456";
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(operations.exchange(any(), any(Class.class))).thenThrow(new ResourceAccessException("TestException"));

        // Testkjøring:
        assertThat(bostotte.hentBostotte(personIdentifikator, "", fra,til)).isEqualTo(null);
    }

    @Test
    public void hentBostotte_testUrl_overlevBadConnection() {
        // Variabler:
        String personIdentifikator = "121212123456";
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(operations.exchange(any(), any(Class.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // Testkjøring:
        assertThat(bostotte.hentBostotte(personIdentifikator, "", fra,til)).isEqualTo(null);
    }

    @Test
    public void hentBostotte_testUrl_overlevBadData() {
        // Variabler:
        String personIdentifikator = "121212123456";
        LocalDate fra = LocalDate.now().minusDays(30);
        LocalDate til = LocalDate.now();

        // Mocks:
        when(operations.exchange(any(), any(Class.class))).thenThrow(new HttpMessageNotReadableException("TestException"));

        // Testkjøring:
        assertThat(bostotte.hentBostotte(personIdentifikator, "", fra,til)).isEqualTo(null);
    }

    @Test
    public void hentBostotte_opprettHusbankenPing() {
        // Mocks:
        when(operations.exchange(any(), any(Class.class))).thenThrow(new HttpMessageNotReadableException("TestException"));

        // Testkjøring:
        Pingable pingable = BostotteImpl.opprettHusbankenPing(config, new RestTemplate());
        assertThat(pingable).isNotNull();
        assertThat(pingable.ping()).isNotNull();
    }}
