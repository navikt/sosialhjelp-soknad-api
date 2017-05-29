package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;

public class DagpengerArbeidsforholdVedleggIT extends AbstractIT {
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
    public void skalHaT8VedleggVedSagtOppAvArbeidsgiver() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "sagtoppavarbeidsgiver"))
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "sagtoppavarbeidsgiver");
    }

    @Test
    public void skalHaT8VedleggVedSagtOppSelv() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "sagtoppselv"))
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "sagtoppselv");
    }

    @Test
    public void skalHaT8VedleggVedKontraktUtgaatt() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "kontraktutgaatt"))
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "kontraktutgaatt");
    }

    @Test
    public void skalHaT8VedleggVedRedusertArbeidstid() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "redusertarbeidstid"))
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "redusertarbeidstid");
    }

    @Test
    public void skalHaT8VedleggVedAvskjediget() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "avskjediget"))
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "avskjediget");
    }

    @Test
    public void skalHaO2Vedlegg() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValue("arbeidsforhold", "true")
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("O2");
    }

    @Test
    public void skalHaU1VedleggVedArbeidsgiverIEOS() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("eosland", "true"))
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("U1");
    }

    @Test
    public void skalHaA2OgM7VedleggVedArbeidsgiverKonkurs() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "arbeidsgivererkonkurs"))
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("A2", "M7");
    }

    @Test
    public void skalHaT6VedleggVedPermittert() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "permittert"))
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("T6");
    }

    @Test
    public void skalHaM6VedleggVedRotasjonsTurnus() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("rotasjonskiftturnus", "jarotasjon"))
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("M6");
    }

    @Test
    public void skalHaG2VedleggVedPermitteringsperiode() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "permittert"))
                .opprettFaktumWithValueAndParent("arbeidsforhold.permitteringsperiode", null, "arbeidsforhold")
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("G2");
    }

}
