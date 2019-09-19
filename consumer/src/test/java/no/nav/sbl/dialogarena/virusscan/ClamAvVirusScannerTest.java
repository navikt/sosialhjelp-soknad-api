package no.nav.sbl.dialogarena.virusscan;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
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
    public void scanFile_scanningIsEnabled_throwsException() {
        when(config.isEnabled()).thenReturn(true);
        assertThatExceptionOfType(OpplastingException.class).isThrownBy(
            () -> virusScanner.scan(filnavn, data)
        ).withMessageStartingWith("Fant virus");
    }

    @Test
    public void scanFile_scanningIsNotEnabled_doesNotThrowException() {
        when(config.isEnabled()).thenReturn(false);
        assertThatCode(
            () -> virusScanner.scan(filnavn, data)
        ).doesNotThrowAnyException();
    }
}
