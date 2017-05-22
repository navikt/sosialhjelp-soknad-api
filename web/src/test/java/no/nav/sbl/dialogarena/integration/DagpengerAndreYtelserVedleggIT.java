package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;

public class DagpengerAndreYtelserVedleggIT extends AbstractIT {
    private String dagpengerSkjemaNummer = new DagpengerOrdinaerInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void skalIkkeKreveNoenVedleggVedStart() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .hentPaakrevdeVedlegg()
                .skalIkkeKreveNoenVedlegg();
    }

    @Test
    public void skalHaK1VedleggVedOffentligTjenestePensjon() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.offentligtjenestepensjon").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedPrivatTjenestePensjon() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.privattjenestepensjon").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedStonadFisker() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.stonadfisker").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedGarantiLott() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.garantilott").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedEtterlonn() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.etterlonn").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedVartPenger() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.vartpenger").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedDagpengerEOS() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("andreytelser.ytelser.dagpengereos").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void skalHaK1VedleggVedAnnenYtelse() {
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
