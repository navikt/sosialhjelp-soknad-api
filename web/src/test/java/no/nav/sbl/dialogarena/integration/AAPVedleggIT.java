package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;

public class AAPVedleggIT extends AbstractIT {

    private String aapOrdinaerSkjemaNummer = new AAPOrdinaerInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void andreYtelserFraArbeidsgiverVedlegg() {
        soknadMedDelstegstatusOpprettet(aapOrdinaerSkjemaNummer)
                .faktum("soknadstype").withValue("ordinaer").utforEndring()
                .faktum("andreytelser.fraarbeidsgiver").withValue("false").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K6");
    }

    @Test
    public void andreYtelserFraAndreOmsorgslonnVedlegg() {
        soknadMedDelstegstatusOpprettet(aapOrdinaerSkjemaNummer)
                .faktum("andreytelser.fraandre.omsorgslonn").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("K5", "omsorgslonn");
    }

    @Test
    public void andreYtelserFraAndreFosterhjemgodtgjørelseVedlegg() {
        soknadMedDelstegstatusOpprettet(aapOrdinaerSkjemaNummer)
                .faktum("andreytelser.fraandre.fosterhjemsgodtgjorelse").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedleggMedSkjemaNummerTillegg("K5", "fosterhjemsgodtgjorelse");

    }

    @Test
    public void andreYtelserFraAndreUtenlandsketrygdemyndigheterVedlegg() {
        soknadMedDelstegstatusOpprettet(aapOrdinaerSkjemaNummer)
                .faktum("andreytelser.fraandre.utenlandsketrygdemyndigheter").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("K1");
    }

    @Test
    public void barneVedlegg() {
        soknadMedDelstegstatusOpprettet(aapOrdinaerSkjemaNummer)
                .alleFaktum("barn").skalVareAntall(1).skalVareSystemFaktum()
                .soknad()
                .hentPaakrevdeVedlegg()
                .skalIkkeHaVedlegg("X8")
                .soknad()
                .nyttFaktum("barn").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("X8");
    }

    @Test
    public void ekstraVedlegg() {
        soknadMedDelstegstatusOpprettet(aapOrdinaerSkjemaNummer)
                .nyttFaktum("ekstraVedlegg").withValue("true").opprett()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("N6");
    }

}
