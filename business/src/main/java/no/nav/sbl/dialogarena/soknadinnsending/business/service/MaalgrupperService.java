package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.FinnMaalgruppeinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.FinnMaalgruppeinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.MaalgruppeinformasjonV1;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.meldinger.WSFinnMaalgruppeinformasjonListeRequest;
import org.joda.time.LocalDate;

public class MaalgrupperService {

    private MaalgruppeinformasjonV1 maalgruppeinformasjon;

    public void hentMaalgrupper(String fodselsnummer) {
        try {
            maalgruppeinformasjon.finnMaalgruppeinformasjonListe(lagRequest(fodselsnummer));
        } catch (FinnMaalgruppeinformasjonListePersonIkkeFunnet | FinnMaalgruppeinformasjonListeSikkerhetsbegrensning e) {
            throw new RuntimeException(e);
        }
    }

    private WSFinnMaalgruppeinformasjonListeRequest lagRequest(String fodselsnummer) {
        WSFinnMaalgruppeinformasjonListeRequest request = new WSFinnMaalgruppeinformasjonListeRequest();
        WSPeriode periode = new WSPeriode().withFom(new LocalDate("2015-01-01"));
        return request.withPersonident(fodselsnummer).withPeriode(periode);
    }
}
