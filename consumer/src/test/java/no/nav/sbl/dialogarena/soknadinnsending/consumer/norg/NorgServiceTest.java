package no.nav.sbl.dialogarena.soknadinnsending.consumer.norg;

import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NavEnhet;
import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.getTestOrganisasjonsnummer;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NorgServiceTest {

    private static final String GT = "0101";
    private static final String ENHETSNUMMER = "0701";
    private static final String ORGNUMMER_PROD = "202020";
    private static final String ORGNUMMER_TEST = getTestOrganisasjonsnummer(ENHETSNUMMER);

    @Mock
    private NorgConsumer norgConsumer;
    @InjectMocks
    private NorgService norgService;

    @Test
    public void finnEnhetForGtBrukerTestOrgNrForTest() {
        setProperty("environment.name", "t5");
        when(norgConsumer.finnEnhetForGeografiskTilknytning(GT)).thenReturn(lagRsNorgEnhet());

        NavEnhet navEnhet = norgService.finnEnhetForGt(GT);

        assertThat(navEnhet.sosialOrgnr, is(ORGNUMMER_TEST));
    }

    @Test
    public void finnEnhetForGtBrukerOrgNrFraNorgForProd() {
        setProperty("environment.name", "p");
        when(norgConsumer.finnEnhetForGeografiskTilknytning(GT)).thenReturn(lagRsNorgEnhet());

        NavEnhet navEnhet = norgService.finnEnhetForGt(GT);

        assertThat(navEnhet.sosialOrgnr, is(ORGNUMMER_PROD));
    }

    @After
    public void teardown() {
        clearProperty("environment.name");
    }

    private NorgConsumer.RsNorgEnhet lagRsNorgEnhet() {
        NorgConsumer.RsNorgEnhet rsNorgEnhet = new NorgConsumer.RsNorgEnhet();
        rsNorgEnhet.enhetNr = ENHETSNUMMER;
        rsNorgEnhet.navn = "Nav Enhet";
        rsNorgEnhet.orgNrTilKommunaltNavKontor = ORGNUMMER_PROD;
        return rsNorgEnhet;
    }

}