package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.DagpengerOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;


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
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "sagtoppavarbeidsgiver").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "sagtoppavarbeidsgiver")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "sagtoppavarbeidsgiver");
    }

    @Test
    public void skalHaT8OgO2VedleggVedSagtOppSelv() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "sagtoppselv").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "sagtoppselv")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "sagtoppselv");
    }

    @Test
    public void skalHaT8OgO2VedleggVedKontraktUtgaatt() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true")
                .withProperty("type", "kontraktutgaatt")
                .withProperty("tilbudomjobbannetsted", "true")
                .withProperty("skalHaT8Vedlegg", "true").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "kontraktutgaatt")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "kontraktutgaatt");
    }

    @Test
    public void skalHaT8OgO2VedleggVedRedusertArbeidstid() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "redusertarbeidstid").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "redusertarbeidstid")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "redusertarbeidstid");
    }

    @Test
    public void skalHaT8OgO2VedleggVedAvskjediget() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "avskjediget").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "avskjediget")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "avskjediget");
    }

    @Test
    public void skalHaU1VedleggVedArbeidsgiverIEOS() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("eosland", "true").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("U1");
    }

    @Test
    public void skalHaA2OgM7OgO2VedleggVedArbeidsgiverKonkurs() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "arbeidsgivererkonkurs").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("A2", "M7")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "arbeidsgivererkonkurs");
    }

    @Test
    public void skalHaT6OgO2VedleggVedPermittert() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "permittert").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("T6")
                .skalHaVedleggMedSkjemaNummerTillegg("O2", "permittert");
    }

    @Test
    public void skalHaM6VedleggVedRotasjonsTurnus() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("rotasjonskiftturnus", "jarotasjon").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("M6");
    }

    @Test
    public void skalHaG2VedleggVedPermitteringsperiode() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "permittert").opprett()
                .nyttFaktum("arbeidsforhold.permitteringsperiode").withParentFaktum("arbeidsforhold").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("G2");
    }

}
