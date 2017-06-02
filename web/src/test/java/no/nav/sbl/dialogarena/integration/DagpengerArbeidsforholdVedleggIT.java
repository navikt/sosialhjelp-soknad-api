package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;

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
    public void skalHaT8OgO2VedleggVedSagtOppAvArbeidsgiver() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "sagtoppavarbeidsgiver"))
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "sagtoppavarbeidsgiver")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "sagtoppavarbeidsgiver");
    }

    @Test
    public void skalHaT8OgO2VedleggVedSagtOppSelv() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "sagtoppselv"))
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "sagtoppselv")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "sagtoppselv");
    }

    @Test
    public void skalHaT8OgO2VedleggVedKontraktUtgaatt() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "kontraktutgaatt"))
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "kontraktutgaatt")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "kontraktutgaatt");
    }

    @Test
    public void skalHaT8OgO2VedleggVedRedusertArbeidstid() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "redusertarbeidstid"))
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "redusertarbeidstid")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "redusertarbeidstid");
    }

    @Test
    public void skalHaT8OgO2VedleggVedAvskjediget() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "avskjediget"))
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "avskjediget")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "avskjediget");
    }

    @Test
    public void skalHaU1VedleggVedArbeidsgiverIEOS() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("eosland", "true"))
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("U1");
    }

    @Test
    public void skalHaA2OgM7OgO2VedleggVedArbeidsgiverKonkurs() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "arbeidsgivererkonkurs"))
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("A2", "M7")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "arbeidsgivererkonkurs");
    }

    @Test
    public void skalHaT6OgO2VedleggVedPermittert() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .opprettFaktumWithValueAndProperties("arbeidsforhold", "true", singletonMap("type", "permittert"))
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("T6")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "permittert");
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
