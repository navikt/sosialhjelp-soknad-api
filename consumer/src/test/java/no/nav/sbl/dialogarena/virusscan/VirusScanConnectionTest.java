package no.nav.sbl.dialogarena.virusscan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
    public void scanFil_filnavnErVirustest_returnererFalse() {
        assertThat(connection.scan("virustest", data)).isFalse();
        verify(operations, times(0)).exchange(any(RequestEntity.class), any(Class.class));
    }

    @Test
    public void scanFil_resultatHarFeilLengde_returnererTrue() {
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(new ScanResult[]{
            new ScanResult("test", Result.FOUND),
            new ScanResult("test", Result.FOUND),
        }));
        assertThat(connection.scan(filnavn, data)).isTrue();
        verify(operations, times(1)).exchange(any(RequestEntity.class), any(Class.class));
    }

    @Test
    public void scanFil_resultatErOK_returnererTrue() {
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(new ScanResult[]{
            new ScanResult("test", Result.OK),
        }));
        assertThat(connection.scan(filnavn, data)).isTrue();
        verify(operations, times(1)).exchange(any(RequestEntity.class), any(Class.class));
    }

    @Test
    public void scanFil_resultatErIkkeOK_returnererFalse() {
        when(operations.exchange(any(), any(Class.class))).thenReturn(ResponseEntity.ok(new ScanResult[]{
            new ScanResult("test", Result.FOUND),
        }));
        assertThat(connection.scan(filnavn, data)).isFalse();
        verify(operations, times(1)).exchange(any(RequestEntity.class), any(Class.class));
    }
}
