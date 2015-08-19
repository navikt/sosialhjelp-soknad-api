package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetsinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetsinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSAktivitet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeResponse;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Service
public class AktiviteterService {

    @Inject
    @Named("sakOgAktivitetInformasjonEndpoint")
    private SakOgAktivitetV1 aktivitetWebService;

    private AktiviteterTransformer transformer = new AktiviteterTransformer();

    public List<Faktum> hentAktiviteter(String fodselnummer) {
        try {
            return Lists.transform(Optional.fromNullable(aktivitetWebService.finnAktivitetsinformasjonListe(lagAktivitetsRequest(fodselnummer))).or(new WSFinnAktivitetsinformasjonListeResponse()).getAktivitetListe(), transformer);
        } catch (FinnAktivitetsinformasjonListePersonIkkeFunnet | FinnAktivitetsinformasjonListeSikkerhetsbegrensning e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private WSFinnAktivitetsinformasjonListeRequest lagAktivitetsRequest(String fodselnummer) {
        return new WSFinnAktivitetsinformasjonListeRequest()
                .withPersonident(fodselnummer)
                .withPeriode(new WSPeriode().withFom(LocalDate.now().minusYears(1)));
    }

    private class AktiviteterTransformer implements Function<WSAktivitet, Faktum> {

        @Override
        public Faktum apply(WSAktivitet wsAktivitet) {
            Faktum faktum = new Faktum()
                    .medKey("aktivitet")
                    .medProperty("id", wsAktivitet.getAktivitetId())
                    .medProperty("navn", wsAktivitet.getAktivitetsnavn());

            WSPeriode periode = wsAktivitet.getPeriode();
            faktum.medProperty("fom", datoTilString(periode.getFom()));
            faktum.medProperty("tom", datoTilString(periode.getTom()));

            return faktum;
        }

        private String datoTilString(LocalDate date) {
            return date != null ? date.toString("yyyy-MM-dd") : "";
        }
    }
}
