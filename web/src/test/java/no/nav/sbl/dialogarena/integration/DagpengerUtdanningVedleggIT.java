package no.nav.sbl.dialogarena.integration;


import no.nav.sbl.dialogarena.config.IntegrationConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class DagpengerUtdanningVedleggIT extends AbstractIT {
    private String dagpengerSkjemaNummer = new DagpengerOrdinaerInformasjon().getSkjemanummer().get(0);

    private NavMessageSource navMessageSource;

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
        navMessageSource = IntegrationConfig.getMocked("navMessageSource");
        when(navMessageSource.finnTekst(eq("utdanning.underutdanning"), any(), any())).thenReturn("whatever");
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
        when(navMessageSource.finnTekst(eq("utdanning.kveld"), any(), any())).thenReturn("whatever");
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("utdanning").withValue("underUtdanning").utforEndring()
                .faktum("utdanning.underutdanning").withValue("true").utforEndring()
                .faktum("utdanning.kveld").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T1", "kveld");
    }

    @Test
    public void skalHaT1VedleggVedKortvarigUtdanning() {
        when(navMessageSource.finnTekst(eq("utdanning.kortvarig"), any(), any())).thenReturn("whatever");
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("utdanning").withValue("underUtdanning").utforEndring()
                .faktum("utdanning.underutdanning").withValue("true").utforEndring()
                .faktum("utdanning.kortvarig").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T1", "kortvarig");
    }

    @Test
    public void skalHaT1VedleggVedNorskUtdanning() {
        when(navMessageSource.finnTekst(eq("utdanning.norsk"), any(), any())).thenReturn("whatever");
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .faktum("utdanning").withValue("underUtdanning").utforEndring()
                .faktum("utdanning.underutdanning").withValue("true").utforEndring()
                .faktum("utdanning.norsk").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T1", "norsk");
    }

    @Test
    public void skalHaT1VedleggVedIntroduksjon() {
        when(navMessageSource.finnTekst(eq("utdanning.introduksjon"), any(), any())).thenReturn("whatever");
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
