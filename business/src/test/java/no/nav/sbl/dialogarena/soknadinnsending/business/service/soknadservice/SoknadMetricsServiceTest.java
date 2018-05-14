package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.SendesSenere;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SoknadMetricsServiceTest {
    private final String DAGPENGER_SKJEMA = "NAV 04-01.03";
    private ArrayList<Vedlegg> innsendteVedlegg;
    private ArrayList<Vedlegg> ikkeInnsendteVedlegg;

    @InjectMocks
    private SoknadMetricsService soknadMetricsService;

    @Spy
    private MetricsEventFactory metricsEventFactory;

    @Before
    public void setUp() throws Exception {
        innsendteVedlegg = new ArrayList<>();
        ikkeInnsendteVedlegg = new ArrayList<>();
    }

    @Test
    public void testInnsendteSkjemaBlirRapportert() {
        soknadMetricsService.rapporterKompletteOgIkkeKompletteSoknader(innsendteVedlegg, ikkeInnsendteVedlegg, DAGPENGER_SKJEMA, false);

        verifiserAtVedleggRapporteres("soknad.innsendteskjema");
    }

    @Test
    public void testInnsendteVedleggBlirRapportert() {
        innsendteVedlegg.add(vedleggMedStatus(LastetOpp));

        soknadMetricsService.rapporterKompletteOgIkkeKompletteSoknader(innsendteVedlegg, ikkeInnsendteVedlegg, DAGPENGER_SKJEMA, false);

        verifiserAtVedleggRapporteres("soknad.sendtVedlegg");
    }

    @Test
    public void testManglendeVedleggBlirRapportert() {
        ikkeInnsendteVedlegg.add(vedleggMedStatus(SendesSenere));

        soknadMetricsService.rapporterKompletteOgIkkeKompletteSoknader(innsendteVedlegg, ikkeInnsendteVedlegg, DAGPENGER_SKJEMA, false);

        verifiserAtVedleggRapporteres("soknad.ikkeSendtVedlegg");
    }

    @Test
    public void testBareVedleggSomEndrerStatusBlirRapportert() {
        innsendteVedlegg.add(vedleggSomLastesOppVedEttersending());
        ikkeInnsendteVedlegg.add(vedleggSomIkkeLastesOppVedEttersending());

        soknadMetricsService.rapporterKompletteOgIkkeKompletteSoknader(innsendteVedlegg, ikkeInnsendteVedlegg, DAGPENGER_SKJEMA, true);

        verifiserAtKunEttersendtVedleggRapporteres();
    }

    private Vedlegg vedleggSomIkkeLastesOppVedEttersending() {
        return vedleggMedStatus(SendesSenere)
                .medOpprinneligInnsendingsvalg(SendesSenere);
    }

    private Vedlegg vedleggSomLastesOppVedEttersending() {
        return vedleggMedStatus(LastetOpp)
                .medOpprinneligInnsendingsvalg(SendesSenere);
    }

    private Vedlegg vedleggMedStatus(Vedlegg.Status status) {
        return new Vedlegg()
                    .medSkjemaNummer("123")
                    .medInnsendingsvalg(status);
    }

    private void verifiserAtVedleggRapporteres(String eventNavn) {
        verify(metricsEventFactory).createEvent(eventNavn);
    }

    private void verifiserAtKunEttersendtVedleggRapporteres() {
        verifiserAtVedleggRapporteres("soknad.sendtVedlegg");
    }
}