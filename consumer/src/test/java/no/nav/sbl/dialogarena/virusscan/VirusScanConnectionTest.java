package no.nav.sbl.dialogarena.virusscan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class VirusScanConnectionTest {

    @Mock
    RestTemplate operations;

    @Mock
    VirusScanConfig config;

    @InjectMocks
    VirusScanConnection connection;

    private String filnavn = "ikke-virustest";
    private byte[] data = new byte[]{};

    @Before
    public void setUp() {
        when(config.getUri()).thenReturn(URI.create("test-uri"));
    }

    @Test
    public void scanFile_filenameIsVirustest_isInfected() {
        assertThat(connection.isInfected("virustest", data)).isTrue();
        verify(operations, times(0)).exchange(any(RequestEntity.class), any(Class.class));
    }

    @Test
    public void scanFile_resultatHasWrongLength_isNotInfected() {
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(new ScanResult[]{
            new ScanResult("test", Result.FOUND),
            new ScanResult("test", Result.FOUND),
        }));
        assertThat(connection.isInfected(filnavn, data)).isFalse();
        verify(operations, times(1)).exchange(any(RequestEntity.class), any(Class.class));
    }

    @Test
    public void scanFile_resultatIsOK_isNotInfected() {
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(new ScanResult[]{
            new ScanResult("test", Result.OK),
        }));
        assertThat(connection.isInfected(filnavn, data)).isFalse();
        verify(operations, times(1)).exchange(any(RequestEntity.class), any(Class.class));
    }

    @Test
    public void scanFile_resultatIsNotOK_isInfected() {
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(new ScanResult[]{
            new ScanResult("test", Result.FOUND),
        }));
        assertThat(connection.isInfected(filnavn, data)).isTrue();
        verify(operations, times(1)).exchange(any(RequestEntity.class), any(Class.class));
    }
}
