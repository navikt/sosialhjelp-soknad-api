package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.config.IntegrationConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class DagpengerAndreYtelserVedleggIT extends AbstractIT {
    private String dagpengerSkjemaNummer = new DagpengerOrdinaerInformasjon().getSkjemanummer().get(0);

    private NavMessageSource navMessageSource;

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
        navMessageSource = IntegrationConfig.getMocked("navMessageSource");
    }

    @Test
    public void skalIkkeKreveNoenVedleggVedStart() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .hentPaakrevdeVedlegg()
                .skalIkkeKreveNoenVedlegg();
    }

    @Test
    public void skalHaK1VedleggVedOffentligTjenestePensjon() {
        when(navMessageSource.finnTekst(eq("andreytelser.ytelser.offentligtjenestepensjon"), any(), any())).thenReturn("whatever");
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.offentligtjenestepensjon").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedPrivatTjenestePensjon() {
        when(navMessageSource.finnTekst(eq("andreytelser.ytelser.privattjenestepensjon"), any(), any())).thenReturn("whatever");
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.privattjenestepensjon").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedStonadFisker() {
        when(navMessageSource.finnTekst(eq("andreytelser.ytelser.stonadfisker"), any(), any())).thenReturn("whatever");
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.stonadfisker").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedGarantiLott() {
        when(navMessageSource.finnTekst(eq("andreytelser.ytelser.garantilott"), any(), any())).thenReturn("whatever");
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.garantilott").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedEtterlonn() {
        when(navMessageSource.finnTekst(eq("andreytelser.ytelser.etterlonn"), any(), any())).thenReturn("whatever");
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.etterlonn").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedVartPenger() {
        when(navMessageSource.finnTekst(eq("andreytelser.ytelser.vartpenger"), any(), any())).thenReturn("whatever");
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.vartpenger").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedDagpengerEOS() {
        when(navMessageSource.finnTekst(eq("andreytelser.ytelser.dagpengereos"), any(), any())).thenReturn("whatever");
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.dagpengereos").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedAnnenYtelse() {
        when(navMessageSource.finnTekst(eq("andreytelser.ytelser.annenytelse"), any(), any())).thenReturn("whatever");
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.annenytelse").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaV6VedleggVedIkkeAvtale() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ikkeavtale").withValue("false").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("V6");
    }

}
