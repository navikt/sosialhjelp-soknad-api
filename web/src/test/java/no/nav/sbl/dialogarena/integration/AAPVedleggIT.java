package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPOrdinaerInformasjon;
import org.junit.Test;

import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static org.mockito.Matchers.any;

public class AAPVedleggIT extends AbstractIT {

    @Test
    public void alleVedleggsKrav() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();

        String aapOrdinaerSkjemaNummer = new AAPOrdinaerInformasjon().getSkjemanummer().get(0);
        soknadMedDelstegstatusOpprettet(aapOrdinaerSkjemaNummer)
                .hentPaakrevdeVedlegg()
                .skalIkkeKreveNoenVedlegg()
                .soknad()
                .faktum("soknadstype").withValue("ordinaer").utforEndring()
                .faktum("tilknytningnorge.oppholdinorgesistetreaar").withValue("false").utforEndring()
                .faktum("tilknytningnorge.oppholdinorgesistetreaar.false.registrertflyktning").withValue("true").utforEndring()
                .faktum("ungufor").withValue("false").utforEndring()
                .faktum("andreytelser.fraarbeidsgiver").withValue("false").utforEndring()
                .faktum("andreytelser.fraandre.omsorgslonn").withValue("true").utforEndring()
                .faktum("andreytelser.fraandre.fosterhjemsgodtgjorelse").withValue("true").utforEndring()
                .faktum("andreytelser.fraandre.utenlandsketrygdemyndigheter").withValue("true").utforEndring()
                .alleFaktum("barn").skalVareAntall(1).skalVareSystemFaktum()
                .soknad()
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("L9", "P2", "V1", "K6", "K1")
                .skalHaVedleggMedSkjemaNummerTillegg("K5", "omsorgslonn")
                .skalHaVedleggMedSkjemaNummerTillegg("K5", "fosterhjemsgodtgjorelse")
                .skalIkkeHaVedlegg("X8", "N6")
                .soknad()
                .opprettFaktumWithValue("barn", null)
                .opprettFaktumWithValue("ekstraVedlegg", "true")
                .hentPaakrevdeVedlegg()
                .skalHaVedlegg("X8", "N6");
    }

}
