package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.FinnAktivitetsinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.FinnAktivitetsinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.SakOgAktivitetInformasjonV1;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.informasjon.WSAktivitet;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.meldinger.WSFinnAktivitetsinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.meldinger.WSFinnAktivitetsinformasjonListeResponse;
import org.apache.commons.collections15.Transformer;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

public class AktiviteterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AktiviteterService.class);

    @Inject
    @Named("sakOgAktivitetInformasjonEndpoint")
    private SakOgAktivitetInformasjonV1 aktivitetWebService;

    AktiviteterTransformer transformer = new AktiviteterTransformer();

    public List<Faktum> hentAktiviteter(String fodselnummer) {
        try {
            return Lists.transform(aktivitetWebService.finnAktivitetsinformasjonListe(lagAktivitetsRequest(fodselnummer)).getAktivitetListe(), transformer);
        } catch (FinnAktivitetsinformasjonListePersonIkkeFunnet | FinnAktivitetsinformasjonListeSikkerhetsbegrensning e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private WSFinnAktivitetsinformasjonListeRequest lagAktivitetsRequest(String fodselnummer) {
        return new WSFinnAktivitetsinformasjonListeRequest().withPersonident(fodselnummer);
    }

    private class AktiviteterTransformer implements Function<WSAktivitet, Faktum> {

        @Override
        public Faktum apply(WSAktivitet wsAktivitet) {
            Faktum faktum = new Faktum()
                    .medKey("aktivitet")
                    .medProperty("id", wsAktivitet.getAktivitetId())
                    .medProperty("navn", wsAktivitet.getAktivitetsnavn());

            WSPeriode periode = wsAktivitet.getPeriode();
            faktum.medProperty("fom", hentDato(periode.getFom()));
            faktum.medProperty("tom", hentDato(periode.getTom()));

            return faktum;
        }

        private String hentDato(LocalDate date) {
            return date != null ? date.toString("yyyy-MM-dd") : "";
        }
    }
}
