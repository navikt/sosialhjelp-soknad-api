package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPOrdinaerInformasjon;
import org.junit.Before;
import org.junit.Test;

import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static org.mockito.Matchers.any;

public class AAPVedleggIT extends AbstractIT {

    private String aapOrdinaerSkjemaNummer = new AAPOrdinaerInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void skalAlltidKreveLegeErklæring() {
        soknadMedDelstegstatusOpprettet(aapOrdinaerSkjemaNummer)
                .faktum("soknadstype").withValue("ordinaer").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("L9");
    }

    @Test
    public void registrertFlyktningVedlegg() {
        soknadMedDelstegstatusOpprettet(aapOrdinaerSkjemaNummer)
                .faktum("tilknytningnorge.oppholdinorgesistetreaar").withValue("false").utforEndring()
                .faktum("tilknytningnorge.oppholdinorgesistetreaar.false.registrertflyktning").withValue("true").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("P2");
    }

    @Test
    public void unguforSpesialistErklaring() {
        soknadMedDelstegstatusOpprettet(aapOrdinaerSkjemaNummer)
                .faktum("ungufor").withValue("false").utforEndring()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("V1");
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
                .opprettFaktumWithValue("barn", null)
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("X8");
    }

    @Test
    public void ekstraVedlegg() {
        soknadMedDelstegstatusOpprettet(aapOrdinaerSkjemaNummer)
                .opprettFaktumWithValue("ekstraVedlegg", "true")
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("N6");
    }

}
