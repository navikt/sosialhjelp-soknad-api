package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerGjenopptakInformasjon;
import org.junit.Before;
import org.junit.Test;

public class DagpengerTidligereArbeidsforholdVedleggIT extends AbstractIT {

    private String dagpengerSkjemaNummer = new DagpengerGjenopptakInformasjon().getSkjemanummer().get(1);

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
    public void skalHaG2VedleggVedJobbetSammenhengende() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("tidligerearbeidsforhold.permittert").withValue("permittert").utforEndring()
                .faktum("tidligerearbeidsforhold.tidligerearbeidsgiver.jobbetsidensist").withValue("false").utforEndring()
                .faktum("tidligerearbeidsforhold.tidligerearbeidsgiver.jobbetsammenhengende").withValue("false").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("G2");
    }

    @Test
    public void skalHaG2VedleggVedJobbetSammenhengendeIFiskeindustrien() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("tidligerearbeidsforhold.permittert").withValue("permittertFiske").utforEndring()
                .faktum("tidligerearbeidsforhold.tidligerearbeidsgiver.jobbetsidensist").withValue("false").utforEndring()
                .faktum("tidligerearbeidsforhold.tidligerearbeidsgiver.jobbetsammenhengende.fiske").withValue("false").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("G2");
    }
}
