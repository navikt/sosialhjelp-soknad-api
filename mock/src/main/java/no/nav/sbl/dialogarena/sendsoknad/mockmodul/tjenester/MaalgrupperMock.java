package no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester;

import no.nav.tjeneste.virksomhet.maalgruppe.v1.MaalgruppeV1;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.informasjon.WSMaalgruppe;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.informasjon.WSMaalgruppetyper;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.meldinger.WSFinnMaalgruppeinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.meldinger.WSFinnMaalgruppeinformasjonListeResponse;
import org.joda.time.LocalDate;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MaalgrupperMock {

    public static MaalgruppeV1 maalgruppeinformasjonV1() {
        MaalgruppeV1 maalgruppeMock = mock(MaalgruppeV1.class);

        try {
            WSFinnMaalgruppeinformasjonListeResponse response = new WSFinnMaalgruppeinformasjonListeResponse();
            response.withMaalgruppeListe(new WSMaalgruppe()
                    .withMaalgruppenavn("Arbeidssøker")
                    .withGyldighetsperiode(new WSPeriode().withFom(new LocalDate("2015-01-01")))
                    .withMaalgruppetype(new WSMaalgruppetyper().withValue("ARBSOKERE")));

            when(maalgruppeMock.finnMaalgruppeinformasjonListe(any(WSFinnMaalgruppeinformasjonListeRequest.class)))
                    .thenReturn(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return maalgruppeMock;
    }
}
