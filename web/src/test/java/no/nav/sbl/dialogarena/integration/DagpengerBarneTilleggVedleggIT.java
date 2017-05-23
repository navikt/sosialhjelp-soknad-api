package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static java.util.Collections.singletonMap;

public class DagpengerBarneTilleggVedleggIT extends AbstractIT {

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
    public void skalHaX8VedleggVedBarn() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("barn", null, singletonMap("vedlegg", "true"))
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("X8");
    }

}
