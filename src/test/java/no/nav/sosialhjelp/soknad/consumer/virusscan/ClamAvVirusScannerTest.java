package no.nav.sosialhjelp.soknad.consumer.virusscan;

import no.nav.sosialhjelp.soknad.domain.model.exception.OpplastingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class ClamAvVirusScannerTest {

    private final URI uri = URI.create("www.test.com");
    private final RestTemplate restTemplate = mock(RestTemplate.class);

    private final ClamAvVirusScanner virusScanner = new ClamAvVirusScanner(uri, restTemplate);

    private String filnavn = "virustest";
    private String behandlingsId = "1100001";
    private byte[] data = new byte[]{};

    @Before
    public void setUp() throws Exception {
        setField(virusScanner, "enabled", true);
    }

    @After
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    public void scanFile_scanningIsEnabled_throwsException() {
        when(restTemplate.exchange(any(RequestEntity.class), eq(ScanResult[].class)))
                .thenReturn(ResponseEntity.ok(new ScanResult[]{new ScanResult(filnavn, Result.FOUND)}));

        assertThatExceptionOfType(OpplastingException.class)
                .isThrownBy(() -> virusScanner.scan(filnavn, data, behandlingsId, "pdf"))
                .withMessageStartingWith("Fant virus");
    }

    @Test
    public void scanFile_scanningIsNotEnabled_doesNotThrowException() {
        setField(virusScanner, "enabled", false);

        assertThatCode(() -> virusScanner.scan(filnavn, data, behandlingsId, "pdf"))
                .doesNotThrowAnyException();
    }

    @Test
    public void scanFile_filenameIsVirustest_isInfected() {
        System.setProperty("environment.name", "test");

        assertThatExceptionOfType(OpplastingException.class)
            .isThrownBy(() -> virusScanner.scan("virustest", data, behandlingsId, "pdf"));

        verify(restTemplate, times(0)).exchange(any(RequestEntity.class), eq(ScanResult[].class));
    }

    @Test
    public void scanFile_resultatHasWrongLength_isNotInfected() {
        when(restTemplate.exchange(any(RequestEntity.class), eq(ScanResult[].class)))
                .thenReturn(ResponseEntity.ok(new ScanResult[]{
                        new ScanResult("test", Result.FOUND),
                        new ScanResult("test", Result.FOUND)
                }));

        assertThatCode(() -> virusScanner.scan(filnavn, data, behandlingsId, "png"))
            .doesNotThrowAnyException();

        verify(restTemplate, times(1)).exchange(any(RequestEntity.class), eq(ScanResult[].class));
    }

    @Test
    public void scanFile_resultatIsOK_isNotInfected() {
        when(restTemplate.exchange(any(RequestEntity.class), eq(ScanResult[].class)))
                .thenReturn(ResponseEntity.ok(new ScanResult[]{new ScanResult("test", Result.OK)}));

        assertThatCode(() -> virusScanner.scan(filnavn, data, behandlingsId, "jpg"))
                .doesNotThrowAnyException();

        verify(restTemplate, times(1)).exchange(any(RequestEntity.class), eq(ScanResult[].class));
    }

    @Test
    public void scanFile_resultatIsNotOK_isInfected() {
        when(restTemplate.exchange(any(RequestEntity.class), eq(ScanResult[].class)))
                .thenReturn(ResponseEntity.ok(new ScanResult[]{new ScanResult("test", Result.FOUND)}));

        assertThatExceptionOfType(OpplastingException.class)
                .isThrownBy(() -> virusScanner.scan(filnavn, data, behandlingsId, "pdf"));

        verify(restTemplate, times(1)).exchange(any(RequestEntity.class), eq(ScanResult[].class));
    }
}
