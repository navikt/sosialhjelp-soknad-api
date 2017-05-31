package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;

public class DagpengerVernepliktVedleggIT extends AbstractIT {
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
    public void skalHaT3VedleggVedAvtjentVerneplikt() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("ikkeavtjentverneplikt").withValue("false").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("T3");
    }

}
