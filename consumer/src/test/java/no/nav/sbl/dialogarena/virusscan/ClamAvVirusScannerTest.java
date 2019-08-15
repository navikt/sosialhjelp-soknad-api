package no.nav.sbl.dialogarena.virusscan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClamAvVirusScannerTest {

    @Mock
    private VirusScanConfig config;

    @InjectMocks
    private ClamAvVirusScanner virusScanner;

    private String filnavn = "virustest";
    private byte[] data = new byte[]{};

    @Test
    public void scanFil_scanningErAktivert_returnererFalse() {
        when(config.isEnabled()).thenReturn(true);
        assertThat(virusScanner.scan(filnavn, data)).isFalse();
    }

    @Test
    public void scanFil_scanningErDeaktivert_returnererTrue() {
        when(config.isEnabled()).thenReturn(false);
        assertThat(virusScanner.scan(filnavn, data)).isTrue();
    }
}
