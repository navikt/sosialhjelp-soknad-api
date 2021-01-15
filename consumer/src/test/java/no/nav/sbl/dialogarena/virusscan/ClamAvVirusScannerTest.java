package no.nav.sbl.dialogarena.virusscan;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClamAvVirusScannerTest {

    @Mock
    private VirusScanConfig config;

    @InjectMocks
    private ClamAvVirusScanner virusScanner;

    private String filnavn = "virustest";
    private String behandlingsId = "1100001";
    private byte[] data = new byte[]{};

    @After
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    public void scanFile_scanningIsEnabled_throwsException() {
        System.setProperty("environment.name", "test");
        when(config.isEnabled()).thenReturn(true);
        assertThatExceptionOfType(OpplastingException.class).isThrownBy(
            () -> virusScanner.scan(filnavn, data, behandlingsId, "pdf")
        ).withMessageStartingWith("Fant virus");
    }

    @Test
    public void scanFile_scanningIsNotEnabled_doesNotThrowException() {
        when(config.isEnabled()).thenReturn(false);
        assertThatCode(
            () -> virusScanner.scan(filnavn, data, behandlingsId, "pdf")
        ).doesNotThrowAnyException();
    }
}
