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
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "sagtoppavarbeidsgiver").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "sagtoppavarbeidsgiver");
    }

    @Test
    public void skalHaT8VedleggVedSagtOppSelv() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "sagtoppselv").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "sagtoppselv");
    }

    @Test
    public void skalHaT8VedleggVedKontraktUtgaatt() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "kontraktutgaatt").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "kontraktutgaatt");
    }

    @Test
    public void skalHaT8VedleggVedRedusertArbeidstid() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "redusertarbeidstid").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "redusertarbeidstid");
    }

    @Test
    public void skalHaT8VedleggVedAvskjediget() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "avskjediget").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("T8", "avskjediget");
    }

    @Test
    public void skalHaO2Vedlegg() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("O2");
    }

    @Test
    public void skalHaU1VedleggVedArbeidsgiverIEOS() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("eosland", "true").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("U1");
    }

    @Test
    public void skalHaA2OgM7VedleggVedArbeidsgiverKonkurs() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "arbeidsgivererkonkurs").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("A2", "M7");
    }

    @Test
    public void skalHaT6VedleggVedPermittert() {
        soknadMedDelstegstatusOpprettet(dagpengerSkjemaNummer)
                .nyttFaktum("arbeidsforhold").withValue("true").withProperty("type", "permittert").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("T6");
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
