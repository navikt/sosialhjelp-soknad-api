package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;

public class DagpengerReellArbeidssokerVedleggIT extends AbstractIT {

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
    public void villigDeltidEneansvarbarnopptil18aarVedlegg() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("reellarbeidssoker.villigdeltid").withValue("false").utforEndring()
                .faktum("reellarbeidssoker.villigdeltid.grunn").withValue("true").utforEndring()
                .faktum("reellarbeidssoker.villigdeltid.grunn.eneansvarbarnopptil18aar").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("Y2");
    }

    @Test
    public void villigDeltidReduserthelseVedlegg() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("reellarbeidssoker.villigdeltid").withValue("false").utforEndring()
                .faktum("reellarbeidssoker.villigdeltid.grunn").withValue("true").utforEndring()
                .faktum("reellarbeidssoker.villigdeltid.grunn.reduserthelse").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("Y2");
    }

    @Test
    public void villigDeltidOmsorgansvarVedlegg() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("reellarbeidssoker.villigdeltid").withValue("false").utforEndring()
                .faktum("reellarbeidssoker.villigdeltid.grunn").withValue("true").utforEndring()
                .faktum("reellarbeidssoker.villigdeltid.grunn.omsorgansvar").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("Y2");
    }

    @Test
    public void villigDeltidAnnensituasjonVedlegg() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("reellarbeidssoker.villigdeltid").withValue("false").utforEndring()
                .faktum("reellarbeidssoker.villigdeltid.grunn").withValue("true").utforEndring()
                .faktum("reellarbeidssoker.villigdeltid.grunn.annensituasjon").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("Y2");
    }

    @Test
    public void villigPendleEneansvarbarnopptil18aarVedlegg() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("reellarbeidssoker.villigpendle").withValue("false").utforEndring()
                .faktum("reellarbeidssoker.villigpendle.grunn").withValue("true").utforEndring()
                .faktum("reellarbeidssoker.villigpendle.grunn.eneansvarbarnopptil18aar").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("Y2");
    }

    @Test
    public void villigPendleReduserthelseVedlegg() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("reellarbeidssoker.villigpendle").withValue("false").utforEndring()
                .faktum("reellarbeidssoker.villigpendle.grunn").withValue("true").utforEndring()
                .faktum("reellarbeidssoker.villigpendle.grunn.reduserthelse").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("Y2");
    }

    @Test
    public void villigPendleOmsorgansvarVedlegg() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("reellarbeidssoker.villigpendle").withValue("false").utforEndring()
                .faktum("reellarbeidssoker.villigpendle.grunn").withValue("true").utforEndring()
                .faktum("reellarbeidssoker.villigpendle.grunn.omsorgansvar").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("Y2");
    }

    @Test
    public void villigPendleAnnensituasjonVedlegg() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("reellarbeidssoker.villigpendle").withValue("false").utforEndring()
                .faktum("reellarbeidssoker.villigpendle.grunn").withValue("true").utforEndring()
                .faktum("reellarbeidssoker.villigpendle.grunn.annensituasjon").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("Y2");
    }

    @Test
    public void villigHelseVedlegg() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("reellarbeidssoker.villighelse").withValue("false").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("T9");
    }
}
