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
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.getOrganisasjonsnummer;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NorgServiceTest {

    private static final String GT = "0101";
    private static final String ENHETSNUMMER = "0701";
    private static final String ORGNUMMER_PROD = getOrganisasjonsnummer(ENHETSNUMMER);
    private static final String ORGNUMMER_TEST = "910940066";

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

    @Test
    public void finnEnhetForLom() {
        setProperty("environment.name", "p");

        String gt = "3434";
        String sosialOrgNummer = "974592274";
        NorgConsumer.RsNorgEnhet norgEnhet = lagRsNorgEnhet();
        norgEnhet.enhetNr = "0513";
        when(norgConsumer.finnEnhetForGeografiskTilknytning(gt)).thenReturn(norgEnhet);

        NavEnhet navEnhet = norgService.finnEnhetForGt(gt);
        assertThat(navEnhet.sosialOrgnr, is(sosialOrgNummer));
    }

    @Test
    public void finnEnhetForSkjaak() {
        setProperty("environment.name", "p");

        String gt = "3432";
        String sosialOrgNummer = "976641175";
        NorgConsumer.RsNorgEnhet norgEnhet = lagRsNorgEnhet();
        norgEnhet.enhetNr = "0513";
        when(norgConsumer.finnEnhetForGeografiskTilknytning(gt)).thenReturn(norgEnhet);

        NavEnhet navEnhet = norgService.finnEnhetForGt(gt);
        assertThat(navEnhet.sosialOrgnr, is(sosialOrgNummer));
    }
}