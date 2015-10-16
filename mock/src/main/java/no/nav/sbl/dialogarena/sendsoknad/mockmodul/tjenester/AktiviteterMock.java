package no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester;

import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.SakOgAktivitetInformasjonV1;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.informasjon.WSAktivitet;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.meldinger.WSFinnAktivitetsinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.meldinger.WSFinnAktivitetsinformasjonListeResponse;
import org.joda.time.LocalDate;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AktiviteterMock {

    public SakOgAktivitetInformasjonV1 sakOgAktivitetInformasjonV1Mock() {
        SakOgAktivitetInformasjonV1 mock = mock(SakOgAktivitetInformasjonV1.class);

        try {
            WSFinnAktivitetsinformasjonListeResponse response = new WSFinnAktivitetsinformasjonListeResponse();
            WSPeriode periode = new WSPeriode().withFom(new LocalDate("2015-01-15")).withTom(new LocalDate("2015-02-15"));
            WSAktivitet aktivitet = new WSAktivitet()
                    .withAktivitetId("9999")
                    .withAktivitetsnavn("Arbeidspraksis i ordinær virksomhet")
                    .withPeriode(periode);

            WSPeriode periode2 = new WSPeriode().withFom(new LocalDate("2015-02-28"));
            WSAktivitet aktivitet2 = new WSAktivitet()
                    .withAktivitetId("8888")
                    .withAktivitetsnavn("Arbeid med bistand")
                    .withPeriode(periode2);


            response.withAktivitetListe(aktivitet, aktivitet2);

            when(mock.finnAktivitetsinformasjonListe(any(WSFinnAktivitetsinformasjonListeRequest.class))).thenReturn(response);
        } catch (Exception e) {
        }

        return mock;
    }
}
