package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.FinnMaalgruppeinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.FinnMaalgruppeinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.MaalgruppeinformasjonV1;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.informasjon.WSMaalgruppe;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.meldinger.WSFinnMaalgruppeinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.meldinger.WSFinnMaalgruppeinformasjonListeResponse;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Service
public class MaalgrupperService {

    @Inject
    @Named("maalgruppeinformasjonEndpoint")
    private MaalgruppeinformasjonV1 maalgruppeinformasjon;
    private MaalgruppeTilFaktum maalgruppeTilFaktum = new MaalgruppeTilFaktum();

    public List<Faktum> hentMaalgrupper(String fodselsnummer) {
        try {
            WSFinnMaalgruppeinformasjonListeResponse maalgrupper = maalgruppeinformasjon.finnMaalgruppeinformasjonListe(lagRequest(fodselsnummer));
            return Lists.transform(maalgrupper.getMaalgruppeListe(), maalgruppeTilFaktum);
        } catch (FinnMaalgruppeinformasjonListePersonIkkeFunnet | FinnMaalgruppeinformasjonListeSikkerhetsbegrensning e) {
            throw new RuntimeException(e);
        }
    }

    private WSFinnMaalgruppeinformasjonListeRequest lagRequest(String fodselsnummer) {
        return new WSFinnMaalgruppeinformasjonListeRequest()
                .withPersonident(fodselsnummer)
                .withPeriode(new WSPeriode().withFom(new LocalDate("2015-01-01")));
    }

    private static class MaalgruppeTilFaktum implements Function<WSMaalgruppe, Faktum> {
        public static final String DATOFORMAT = "yyyy-MM-dd";

        @Override
        public Faktum apply(WSMaalgruppe maalgruppe) {
            return new Faktum()
                    .medKey("maalgruppe")
                    .medProperty("navn", maalgruppe.getMaalgruppenavn())
                    .medProperty("fom", maalgruppe.getGyldighetsperiode().getFom().toString(DATOFORMAT))
                    .medProperty("tom", datoTilString(maalgruppe.getGyldighetsperiode().getTom()))
                    .medProperty("kodeverkVerdi", maalgruppe.getMaalgruppetype().getValue());
        }

        private String datoTilString(LocalDate date) {
            return date != null ? date.toString(DATOFORMAT) : "";
        }
    }
}
