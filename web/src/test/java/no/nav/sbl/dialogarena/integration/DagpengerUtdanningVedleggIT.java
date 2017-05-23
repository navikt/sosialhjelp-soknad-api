package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;

public class DagpengerUtdanningVedleggIT extends AbstractIT {
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
    public void skalhaT2VedleggVedAvsluttetUtdanning() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("utdanning").withValue("avsluttetUtdanning").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("T2");
    }

    @Test
    public void skalhaT1VedleggVedAvsluttetUtdanningKveld() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("utdanning").withValue("underUtdanning").utforEndring()
                .faktum("utdanning.underutdanning").withValue("true").utforEndring()
                .faktum("utdanning.kveld").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T1", "kveld");
    }

    @Test
    public void skalHaT1VedleggVedKortvarigUtdanning() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("utdanning").withValue("underUtdanning").utforEndring()
                .faktum("utdanning.underutdanning").withValue("true").utforEndring()
                .faktum("utdanning.kortvarig").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T1", "kortvarig");
    }

    @Test
    public void skalHaT1VedleggVedNorskUtdanning() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("utdanning").withValue("underUtdanning").utforEndring()
                .faktum("utdanning.underutdanning").withValue("true").utforEndring()
                .faktum("utdanning.norsk").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T1", "norsk");
    }

    @Test
    public void skalHaT1VedleggVedIntroduksjon() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("utdanning").withValue("underUtdanning").utforEndring()
                .faktum("utdanning.underutdanning").withValue("true").utforEndring()
                .faktum("utdanning.introduksjon").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T1", "introduksjon");
    }

    @Test
    public void skalHaT1VedleggVedAnnet() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("utdanning").withValue("underUtdanning").utforEndring()
                .faktum("utdanning.underutdanning").withValue("true").utforEndring()
                .faktum("utdanning.annet").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T1", "annet");
    }
}
